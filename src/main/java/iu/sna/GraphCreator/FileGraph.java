package iu.sna.GraphCreator;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.springframework.stereotype.Component;

import java.io.File;
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
 */
@Getter
public class FileGraph implements Graph {
    private static final double COMMIT_COMPOUND_WEIGHT_COEF = 1.0;
    private static final int COMMIT_LIMIT = 10;

    /**
     * Maps file paths to their filenames
     */
    private final Map<Path, String> pathToFilename;
    
    /**
     * All edges in the graph
     */
    private final ArrayList<Edge> edges;
    
    /**
     * All vertices in the graph
     */
    private final ArrayList<Vertex> vertices;

    /**
     * Path to the project root directory
     */
    private final Path projectRoot;

    /**
     * Initialize graph objects.
     *
     * @param root String absolute path to the root
     */
    public FileGraph(final String root) {
        this.projectRoot = Paths.get(root);
        this.pathToFilename = new HashMap<>();
        this.edges = new ArrayList<>();
        this.vertices = new ArrayList<>();
    }


    /**
     * Parse recursively files.
     * The DFS algorithm was used.
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

    private boolean isValidFile(Path path) {
        for (Path component : path) {
            if (component.toString().startsWith(".")) {
                return false;
            }
        }
        return Files.isRegularFile(path) && 
               !path.getFileName().toString().startsWith(".");
               
    }

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

    private String getFileNameWithoutExtension(String fileName) {
        return fileName.contains(".") ? 
               fileName.substring(0, fileName.lastIndexOf(".")) : 
               fileName;
    }

    private void updateEdgeParameters(Vertex from, Vertex to) {
        Edge edge = findEdge(from, to);
        if (edge == null) {
            edge = addEdge(from, to);
        }
        edge.incrementCountCommonCommits();
        edge.incrementCountCommonChangedLines(
                from.getTotalChangedLines() + to.getTotalChangedLines());
    }

    public void parseCommits() throws IOException {
        try (Repository repository = new RepositoryBuilder()
                .setGitDir(new File(projectRoot + "/.git"))
                .build()) {
            
            List<List<ChangedFile>> commitHistory = getCommitHistory(repository);
            processCommitHistory(commitHistory);
        }
    }

    private void processCommitHistory(List<List<ChangedFile>> commitHistory) {
        commitHistory.forEach(changedFiles -> {
            List<ChangedFile> changedFileList = new ArrayList<>(changedFiles);
            
            for (int i = 0; i < changedFileList.size(); i++) {
                ChangedFile file1 = changedFileList.get(i);
                Vertex from = getOrCreateVertex(file1);
                
                for (int j = i + 1; j < changedFileList.size(); j++) {
                    ChangedFile file2 = changedFileList.get(j);
                    Vertex to = getOrCreateVertex(file2);
                    
                    updateEdgeParameters(from, to);
                    updateEdgeParameters(to, from);
                }
            }
        });
    }

    private Vertex getOrCreateVertex(ChangedFile file) {
        Path filepath = file.getPath();
        Vertex vertex = findVertex(filepath);
        
        if (vertex == null) {
            vertex = addVertex(filepath.getFileName().toString(), filepath);
        }
        
        vertex.incrementCountCommits();
        vertex.incrementTotalChangedLines(file.getChangedLines());
        return vertex;
    }

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

    private double calculateAverageLineChanges(Vertex file1, Vertex file2) {
        return ((double) file1.getTotalChangedLines() / file1.getCountCommits()) +
               ((double) file2.getTotalChangedLines() / file2.getCountCommits());
    }

    private double calculateCompound(
            int commonFileChangesCounter,
            double avgCommonChangedLines,
            int totalAandBcommits,
            double avgLineChangesInBothFiles,
            double fileLocationCoef) {
        
        // Handle edge cases to prevent NaN
        if (commonFileChangesCounter == 0 || totalAandBcommits == 0 || avgLineChangesInBothFiles == 0) {
            return 0.0;
        }

        // Ensure we don't divide by zero
        double denominator = (double) totalAandBcommits * avgLineChangesInBothFiles;
        if (denominator == 0) {
            return 0.0;
        }

        double result = (((double) commonFileChangesCounter * avgCommonChangedLines)
                / denominator) * fileLocationCoef * COMMIT_COMPOUND_WEIGHT_COEF;

        // Handle potential NaN or Infinity
        if (Double.isNaN(result) || Double.isInfinite(result)) {
            return 0.0;
        }

        return result;
    }

    public void buildGraph() throws IOException {
        parseFiles();
        parseCommits();
        updateCompoundPower();
        removeZeroWeightEdges();
    }

    private void removeZeroWeightEdges() {
        edges.removeIf(edge -> {
            if (edge.getCompoundWeight() == 0.0) {
                edge.getFrom().getAdjVerticies().remove(edge.getTo());
                return true;
            }
            return false;
        });
    }

    private List<List<ChangedFile>> getCommitHistory(Repository repository) {
        GitCommitParser parser = new GitCommitParser(repository);
        return new ArrayList<>(parser.getChangeFilesInFirstNcommits(COMMIT_LIMIT));
    }

    Vertex findVertex(final Path path) {
        return vertices.stream()
                .filter(vertex -> vertex.getFilepath().equals(path))
                .findFirst()
                .orElse(null);
    }

    Edge findEdge(Vertex from, Vertex to) {
        return edges.stream()
                .filter(edge -> edge.getFrom().equals(from) && edge.getTo().equals(to))
                .findFirst()
                .orElse(null);
    }

    private Vertex addVertex(final String fileName, final Path filePath) {
        Vertex newVertex = new Vertex(fileName, filePath);
        vertices.add(newVertex);
        return newVertex;
    }

    private Edge addEdge(final Vertex from, final Vertex to) {
        Edge newEdge = new Edge(from, to);
        edges.add(newEdge);
        from.getAdjVerticies().add(to);
        return newEdge;
    }

    @Getter
    @Setter
    public class Vertex {
        private final String filename;
        private final Path filepath;
        private int countCommits = 0;
        private int totalChangedLines = 0;
        private ArrayList<Vertex> adjVerticies = new ArrayList<>();

        Vertex(final String file, final Path path) {
            this.filename = file;
            this.filepath = path;
        }

        public int incrementCountCommits() {
            return this.countCommits += 1;
        }

        public int incrementTotalChangedLines(int i) {
            return totalChangedLines += i;
        }
    }

    @Getter
    @Setter
    public class Edge {
        private Vertex from;
        private Vertex to;
        private double FILE_LOCATION_COEF;
        private double compoundWeight = 0;
        private int countCommonCommits = 0;
        private int countCommonChangedLines = 0;
        private double locationValueCoeficient = 1;

        Edge(final Vertex fromFile, final Vertex toFile) {
            this.from = fromFile;
            this.to = toFile;
            initFileLocationCoef(
                    fromFile.getFilepath().toString(),
                    toFile.getFilepath().toString()
            );
        }

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

        private int calcCommonRootDepth(String[] s1, String[] s2) {
            int i = 0;
            int minLen = Math.min(s1.length, s2.length);
            while (i < minLen && s1[i].equals(s2[i])) {
                i++;
            }
            return i - 1;
        }

        public int incrementCountCommonCommits() {
            return this.countCommonCommits += 1;
        }

        public int incrementCountCommonChangedLines(int i) {
            return countCommonChangedLines += i;
        }
    }

}
