package iu.sna.GraphCreator;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.jgit.lib.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of a graph structure for analyzing file relationships in a codebase.
 * This class builds a graph where vertices represent files and edges represent relationships
 * between files based on commit history and file content analysis.
 *
 * <p>The graph is built in several steps:
 * 1. Parse files recursively to find all relevant files
 * 2. Build initial relationships based on file content analysis
 * 3. Analyze commit history to strengthen relationships
 * 4. Calculate compound weights for edges
 * 5. Remove edges with zero weight
 *
 * @author Your Name
 * @version 1.0
 */
@Component
@Getter
public class FileGraph {
    /**
     * Coefficient for commit importance in weight calculations.
     * Loaded from application.yaml.
     */
    @Value("${constants.commit_importance_coefficient}")
    private double commitCompoundWeightCoef;
    
    /**
     * Maximum number of commits to analyze.
     * Defaults to 10 if not specified.
     */
    @Value("${constants.commit_limit:10}")
    private int commitLimit;

    /**
     * Coefficient for location-based calculations.
     * Loaded from application.yaml.
     */
    @Value("${constants.location_value_coefficient}")
    private double locationValueCoeficient;

    /**
     * Maps file paths to their filenames.
     */
    private final Map<Path, String> pathToFilename;
    
    /**
     * All edges in the graph.
     */
    private final ArrayList<Edge> edges;
    
    /**
     * All vertices in the graph.
     */
    private final ArrayList<Vertex> vertices;

    /**
     * Path to the project root directory.
     */
    private final Path projectRoot;

    /**
     * Git repository instance for commit analysis.
     */
    private final Repository repository;

    /**
     * Initialize graph objects.
     *
     * @param projectRoot String absolute path to the root
     * @param repository Git repository instance
     */
    @Autowired
    public FileGraph(@Value("${project.root:${user.dir}}") String projectRoot, Repository repository) {
        if (projectRoot == null || projectRoot.isEmpty()) {
            projectRoot = System.getProperty("user.dir");
        }
        this.projectRoot = Paths.get(projectRoot);
        this.repository = repository;
        this.pathToFilename = new HashMap<>();
        this.edges = new ArrayList<>();
        this.vertices = new ArrayList<>();
    }

    /**
     * Parse files recursively using DFS algorithm.
     * Finds all valid files in the project directory.
     *
     * @throws IOException if file operations fail
     */
    public void parseFiles() throws IOException {
        try (Stream<Path> walk = Files.walk(projectRoot)) {
            pathToFilename.putAll(walk
                    .filter(this::isValidFile)
                    .collect(Collectors.toMap(
                            path -> path,
                            path -> path.getFileName().toString(),
                            (oldPath, newPath) -> newPath
                    )));
        }
        buildFileRelationships();
    }

    /**
     * Check if a file is valid for analysis.
     * Excludes hidden files and directories.
     *
     * @param path Path to check
     * @return true if file is valid, false otherwise
     */
    private boolean isValidFile(Path path) {
        for (Path component : path) {
            if (component.toString().startsWith(".")) {
                return false;
            }
        }
        return Files.isRegularFile(path) && 
               !path.getFileName().toString().startsWith(".");
    }

    /**
     * Build relationships between files based on content analysis.
     * Creates edges when one file references another.
     *
     * @throws IOException if file operations fail
     */
    private void buildFileRelationships() throws IOException {
        List<Path> filepaths = new ArrayList<>(pathToFilename.keySet());
        Map<Path, String> fileContents = new HashMap<>();

        // Extract file content
        for (Path filepath : filepaths) {
            try {
                fileContents.put(filepath, Files.readString(filepath));
            } catch (Exception e) {
                // Skip binary or unreadable files
            }
        }

        for (Path filepath1 : filepaths) {
            String file1 = pathToFilename.get(filepath1);
            for (Path filepath2 : filepaths) {
                if (filepath1.equals(filepath2)) {
                    continue;
                }
                try {
                    String file2 = pathToFilename.get(filepath2);
                    String fileContent = fileContents.get(filepath2);
                    String nameWoExtension = getFileNameWithoutExtension(file1);

                    if (fileContent.contains(nameWoExtension)) {
                        System.out.println(filepath2 + " содержит " + filepath1);
                        Vertex from = addVertex(file1, filepath1);
                        Vertex to = addVertex(file2, filepath2);
                        addEdge(from, to);
                    }
                } catch (Exception e) {
                    // Skip unreadable files
                }
            }
        }
    }

    /**
     * Get filename without extension.
     *
     * @param fileName Name of the file
     * @return Filename without extension
     */
    private String getFileNameWithoutExtension(String fileName) {
        return fileName.contains(".") ? 
               fileName.substring(0, fileName.lastIndexOf(".")) : 
               fileName;
    }

    /**
     * Update edge parameters based on file changes.
     *
     * @param from Source vertex
     * @param to Target vertex
     */
    private void updateEdgeParameters(Vertex from, Vertex to) {
        Edge edge = findEdge(from, to);
        if (edge == null) {
            edge = addEdge(from, to);
        }
        edge.incrementCountCommonCommits();
        edge.incrementCountCommonChangedLines(
                from.getTotalChangedLines() + to.getTotalChangedLines());
    }

    /**
     * Parse commit history and update graph relationships.
     *
     * @throws IOException if commit operations fail
     */
    public void parseCommits() throws IOException {
        List<List<ChangedFile>> commitHistory = getCommitHistory(repository);
        processCommitHistory(commitHistory);
    }

    /**
     * Process commit history to update graph relationships.
     *
     * @param commitHistory List of commits with changed files
     */
    private void processCommitHistory(List<List<ChangedFile>> commitHistory) {
        commitHistory.forEach(changedFiles -> {
            List<ChangedFile> changedFileList = new ArrayList<>(changedFiles);
            
            for (int i = 0; i < changedFileList.size(); i++) {
                ChangedFile file1 = changedFileList.get(i);
                Vertex from = getOrCreateVertex(file1);
                from.incrementCountCommits();
                from.incrementTotalChangedLines(file1.getChangedLines());
                for (int j = i + 1; j < changedFileList.size(); j++) {
                    ChangedFile file2 = changedFileList.get(j);
                    Vertex to = getOrCreateVertex(file2);
                    
                    updateEdgeParameters(from, to);
                    updateEdgeParameters(to, from);
                }
            }
        });
    }

    /**
     * Get or create a vertex for a changed file.
     *
     * @param file Changed file information
     * @return Vertex for the file
     */
    private Vertex getOrCreateVertex(ChangedFile file) {
        Path filepath = file.getPath();
        Vertex vertex = findVertex(filepath);
        
        if (vertex == null) {
            vertex = addVertex(filepath.getFileName().toString(), filepath);
        }
        
        return vertex;
    }

    /**
     * Update compound weights for all edges.
     */
    private void updateCompoundPower() {
        for (Edge edge : edges) {
            try {
                double weight = calculateCompoundWeight(edge);
                edge.setCompoundWeight(weight);
            } catch (ArithmeticException e) {
                System.out.println("ArithmeticException in updateCompoundPower for edge " + 
                    edge.getFrom().getFilename() + " -> " + edge.getTo().getFilename() + ": " + e.getMessage());
                edge.setCompoundWeight(0);
            }
        }
    }

    /**
     * Calculate compound weight for an edge.
     *
     * @param edge Edge to calculate weight for
     * @return Calculated compound weight
     */
    private double calculateCompoundWeight(Edge edge) {
        Vertex file1 = edge.getFrom();
        Vertex file2 = edge.getTo();
        
        int commonFileChangesCounter = edge.getCountCommonCommits();
        double avgCommonChangedLines = (double) edge.getCountCommonChangedLines() / commonFileChangesCounter;
        int totalAandBcommits = file1.getCountCommits() + file2.getCountCommits() - commonFileChangesCounter;
        double avgLineChangesInBothFiles = calculateAverageLineChanges(file1, file2);
        
        return calculateCompound(
                commonFileChangesCounter,
                avgCommonChangedLines,
                totalAandBcommits,
                avgLineChangesInBothFiles,
                edge.getFILE_LOCATION_COEF()
        );
    }

    /**
     * Calculate average line changes for two files.
     *
     * @param file1 First file vertex
     * @param file2 Second file vertex
     * @return Average number of line changes
     */
    private double calculateAverageLineChanges(Vertex file1, Vertex file2) {
        return ((double) file1.getTotalChangedLines() / file1.getCountCommits()) +
               ((double) file2.getTotalChangedLines() / file2.getCountCommits());
    }

    /**
     * Calculate compound value based on various factors.
     *
     * @param commonFileChangesCounter Number of common file changes
     * @param avgCommonChangedLines Average number of changed lines
     * @param totalAandBcommits Total number of commits
     * @param avgLineChangesInBothFiles Average line changes in both files
     * @param fileLocationCoef Location coefficient
     * @return Calculated compound value
     */
    private double calculateCompound(
            int commonFileChangesCounter,
            double avgCommonChangedLines,
            int totalAandBcommits,
            double avgLineChangesInBothFiles,
            double fileLocationCoef) {
        
        if (commonFileChangesCounter == 0 || totalAandBcommits == 0 || avgLineChangesInBothFiles == 0) {
            return 0.0;
        }

        double denominator = (double) totalAandBcommits * avgLineChangesInBothFiles;
        if (denominator == 0) {
            return 0.0;
        }

        double result = (((double) commonFileChangesCounter * avgCommonChangedLines)
                / denominator) * fileLocationCoef * commitCompoundWeightCoef;

        if (Double.isNaN(result) || Double.isInfinite(result)) {
            return 0.0;
        }

        return result;
    }

    /**
     * Build the complete graph by parsing files and commits.
     *
     * @throws IOException if file or commit operations fail
     */
    public void buildGraph() throws IOException {
        parseFiles();
        parseCommits();
        updateCompoundPower();
        removeZeroWeightEdges();
    }

    /**
     * Remove edges with zero weight from the graph.
     */
    private void removeZeroWeightEdges() {
        edges.removeIf(edge -> {
            if (edge.getCompoundWeight() == 0.0) {
                edge.getFrom().getAdjVerticies().remove(edge.getTo());
                return true;
            }
            return false;
        });
    }

    /**
     * Get commit history from repository.
     *
     * @param repository Git repository
     * @return List of commits with changed files
     */
    private List<List<ChangedFile>> getCommitHistory(Repository repository) {
        GitCommitParser parser = new GitCommitParser(repository);
        return new ArrayList<>(parser.getChangeFilesInFirstNcommits(commitLimit));
    }

    /**
     * Find a vertex by its path.
     *
     * @param path Path to search for
     * @return Found vertex or null
     */
    Vertex findVertex(final Path path) {
        return vertices.stream()
                .filter(vertex -> vertex.getFilepath().equals(path))
                .findFirst()
                .orElse(null);
    }

    /**
     * Find an edge between two vertices.
     *
     * @param from Source vertex
     * @param to Target vertex
     * @return Found edge or null
     */
    Edge findEdge(Vertex from, Vertex to) {
        return edges.stream()
                .filter(edge -> edge.getFrom().equals(from) && edge.getTo().equals(to))
                .findFirst()
                .orElse(null);
    }

    /**
     * Add a new vertex to the graph.
     *
     * @param fileName Name of the file
     * @param filePath Path to the file
     * @return Created vertex
     */
    private Vertex addVertex(final String fileName, final Path filePath) {
        Vertex newVertex = new Vertex(fileName, filePath);
        vertices.add(newVertex);
        return newVertex;
    }

    /**
     * Add a new edge to the graph.
     *
     * @param from Source vertex
     * @param to Target vertex
     * @return Created edge
     */
    private Edge addEdge(final Vertex from, final Vertex to) {
        Edge newEdge = new Edge(from, to);
        edges.add(newEdge);
        from.getAdjVerticies().add(to);
        return newEdge;
    }

    /**
     * Represents a file vertex in the graph.
     */
    @Getter
    @Setter
    public class Vertex {
        /**
         * Name of the file.
         */
        private final String filename;
        
        /**
         * Path to the file.
         */
        private final Path filepath;
        
        /**
         * Number of commits affecting this file.
         */
        private int countCommits = 0;
        
        /**
         * Total number of lines changed in this file.
         */
        private int totalChangedLines = 0;
        
        /**
         * List of adjacent vertices.
         */
        private ArrayList<Vertex> adjVerticies = new ArrayList<>();

        /**
         * Create a new vertex.
         *
         * @param file Name of the file
         * @param path Path to the file
         */
        Vertex(final String file, final Path path) {
            this.filename = file;
            this.filepath = path;
        }

        /**
         * Increment commit count.
         *
         * @return New commit count
         */
        public int incrementCountCommits() {
            return this.countCommits += 1;
        }

        /**
         * Increment total changed lines.
         *
         * @param i Number of lines to add
         * @return New total changed lines
         */
        public int incrementTotalChangedLines(int i) {
            return totalChangedLines += i;
        }
    }

    /**
     * Represents an edge between two files in the graph.
     */
    @Getter
    @Setter
    public class Edge {
        /**
         * Source vertex.
         */
        private Vertex from;
        
        /**
         * Target vertex.
         */
        private Vertex to;
        
        /**
         * Location coefficient for weight calculation.
         */
        private double FILE_LOCATION_COEF;
        
        /**
         * Compound weight of the edge.
         */
        private double compoundWeight = 0;
        
        /**
         * Number of common commits.
         */
        private int countCommonCommits = 0;
        
        /**
         * Number of common changed lines.
         */
        private int countCommonChangedLines = 0;

        /**
         * Create a new edge.
         *
         * @param fromFile Source vertex
         * @param toFile Target vertex
         */
        Edge(final Vertex fromFile, final Vertex toFile) {
            this.from = fromFile;
            this.to = toFile;
            initFileLocationCoef(
                    fromFile.getFilepath().toString(),
                    toFile.getFilepath().toString()
            );
        }

        /**
         * Initialize file location coefficient.
         *
         * @param filepath1 Path of first file
         * @param filepath2 Path of second file
         */
        private void initFileLocationCoef(String filepath1, String filepath2) {
            String[] s1 = filepath1.split("/");
            String[] s2 = filepath2.split("/");

            int file1Depth = s1.length - 1;
            int file2Depth = s2.length - 1;

            int commonRootDepth = calcCommonRootDepth(s1, s2);
            double mean = (file1Depth + file2Depth) / 2.0;

            double maxDepth = Math.max(file1Depth, file2Depth);
            double depthRatio = maxDepth > 0 ? (mean - commonRootDepth) / maxDepth : 0;

            this.FILE_LOCATION_COEF = (1.5 - depthRatio) * locationValueCoeficient;
        }

        /**
         * Calculate common root depth of two paths.
         *
         * @param s1 First path components
         * @param s2 Second path components
         * @return Depth of common root
         */
        private int calcCommonRootDepth(String[] s1, String[] s2) {
            int i = 0;
            int minLen = Math.min(s1.length, s2.length);
            while (i < minLen && s1[i].equals(s2[i])) {
                i++;
            }
            return i - 1;
        }

        /**
         * Increment common commit count.
         *
         * @return New common commit count
         */
        public int incrementCountCommonCommits() {
            return this.countCommonCommits += 1;
        }

        /**
         * Increment common changed lines count.
         *
         * @param i Number of lines to add
         * @return New common changed lines count
         */
        public int incrementCountCommonChangedLines(int i) {
            return countCommonChangedLines += i;
        }
    }
}
