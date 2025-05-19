package iu.sna.GraphCreator;

import com.domain.repository_scanner.FileTechnologyStack;
import com.infrastructure.Graph;
import iu.sna.GraphCreator.LanguageAnalyzer.*;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.jgit.lib.Repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

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
@Getter
public class FileGraph extends Graph<FileGraph.Vertex, FileGraph.Edge> {
  private final Map<Path, String> pathToFilename;
  private final ConfigReader config;
  private final Repository repository;
  // Конфигурационные параметры
  private final double LANGUAGE_SPECIFIC_ANALYSIS_CONSTANT;
  private final double LANGUAGE_SPECIFIC_ANALYSIS_COEF;
  private final double COMMIT_IMPORTANCE_COEFFICIENT;
  private final int COMMIT_LIMIT;
  private final double LOCATION_VALUE_COEFFICIENT;

  private LanguageAnalyzerService languageAnalyzerService;
  private Collection<FileTechnologyStack> allFiles;

  public FileGraph(Collection<FileTechnologyStack> files, String configPath, Repository repository) throws IOException {
    this.config = new ConfigReader(configPath);
    this.pathToFilename = new HashMap<>();
    this.repository = repository;

    // Загружаем конфигурацию

    this.LANGUAGE_SPECIFIC_ANALYSIS_CONSTANT = config.getDouble("constants.LANGUAGE_SPECIFIC_ANALYSIS_CONSTANT");
    this.LANGUAGE_SPECIFIC_ANALYSIS_COEF = config.getDouble("constants.LANGUAGE_SPECIFIC_ANALYSIS_COEF");
    this.COMMIT_IMPORTANCE_COEFFICIENT = config.getDouble("constants.COMMIT_IMPORTANCE_COEFFICIENT");
    this.COMMIT_LIMIT = config.getInt("constants.COMMIT_LIMIT");
    this.LOCATION_VALUE_COEFFICIENT = config.getDouble("constants.LOCATION_VALUE_COEFFICIENT");

    this.languageAnalyzerService = new LanguageAnalyzerService(
            List.of(new PydepsAnalyzer(), new MadgeAnalyzerJavaScript(), new MadgeAnalyzerTypeScript(), new JavaParserAnalyzer()));

    Collection<File> extractedFiles = new ArrayList<>();
    files.forEach(file -> extractedFiles.add(file.file()));
    this.allFiles = files;
    // Инициализируем граф из переданных файлов
    initializeGraph(extractedFiles);
  }


  public void initializeGraph(Collection<File> files) throws IOException {
    for (File file : files) {
      Path path = file.toPath();
      pathToFilename.put(path, file.getName());
      addVertex(file.getName(), path);
    }
    // смотрим по упоминании имени
    buildFileRelationships();

    // смотрим по упоминании в коммитах
    parseCommits();

    // считаем веса
    updateCompoundPower();
    // применяем специфический инструмент к группе файлов на одном языке
    applyLanguageSpecificAnalisis();


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
    return Files.isRegularFile(path) && !path.getFileName().toString().startsWith(".");
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
    // scanning for file mentioning
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
//            System.out.println(filepath2 + " содержит " + filepath1);
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
    return fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf(".")) : fileName;
  }

  /**
   * Update edge parameters based on file changes.
   *
   * @param from Source vertex
   * @param to   Target vertex
   */
  private void updateEdgeParameters(Vertex from, Vertex to) {
    Edge edge = findEdge(from, to);
    if (edge == null) {
      edge = new Edge(from, to);
    }
    edge.incrementCountCommonCommits();
    edge.incrementCountCommonChangedLines(from.getTotalChangedLines() + to.getTotalChangedLines());
  }

  /**
   * Parse commit history and update graph relationships.
   *
   * @throws IOException if commit operations fail
   */
  public void parseCommits() throws IOException {
    List<List<ChangedFile>> commitHistory = getCommitHistory();
    processCommitHistory(commitHistory);
  }

  /**
   * Process commit history to update graph relationships.
   *
   * @param commitHistory List of commits with changed files
   */
  private void processCommitHistory(List<List<ChangedFile>> commitHistory) {
    commitHistory.forEach(changedFiles -> {
      // changed files in commit
      List<ChangedFile> changedFileList = new ArrayList<>(changedFiles);

      for (int i = 0; i < changedFileList.size(); i++) {
        ChangedFile file1 = changedFileList.get(i);
        Vertex from = findVertex(file1.getPath());
        if (from == null) {
          continue;
        }

        from.incrementCountCommits();
        from.incrementTotalChangedLines(file1.getChangedLines());
        for (int j = i + 1; j < changedFileList.size(); j++) {
          ChangedFile file2 = changedFileList.get(j);
          Vertex to = findVertex(file2.path);

          // если эти файлы были в инпуте - обновляем
          if (to != null) {
            to.incrementCountCommits();
            to.incrementTotalChangedLines(file2.getChangedLines());
            updateEdgeParameters(from, to);
            updateEdgeParameters(to, from);
          }
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
    for (Graph.EdgeInfo<Vertex, Edge> edgeInfo : getEdges()) {
      try {
        double weight = calculateCompoundWeight(edgeInfo.edgeData());
        edgeInfo.edgeData().setCompoundWeight(edgeInfo.edgeData().getCompoundWeight() + weight);
      } catch (ArithmeticException e) {
        System.out.println(
                "ArithmeticException in updateCompoundPower for edge " + edgeInfo.source().getFilename() + " -> " + edgeInfo.destination().getFilename() + ": " + e.getMessage());
        edgeInfo.edgeData().setCompoundWeight(0);
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
            commonFileChangesCounter, avgCommonChangedLines, totalAandBcommits, avgLineChangesInBothFiles,
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
    return ((double) file1.getTotalChangedLines() / file1.getCountCommits()) + ((double) file2.getTotalChangedLines() / file2.getCountCommits());
  }

  /**
   * Calculate compound value based on various factors.
   *
   * @param commonFileChangesCounter  Number of common file changes
   * @param avgCommonChangedLines     Average number of changed lines
   * @param totalAandBcommits         Total number of commits
   * @param avgLineChangesInBothFiles Average line changes in both files
   * @param fileLocationCoef          Location coefficient
   * @return Calculated compound value
   */
  private double calculateCompound(
          int commonFileChangesCounter, double avgCommonChangedLines, int totalAandBcommits,
          double avgLineChangesInBothFiles, double fileLocationCoef) {

    if (commonFileChangesCounter == 0 || totalAandBcommits == 0 || avgLineChangesInBothFiles == 0) {
      return 0.0;
    }

    double denominator = (double) totalAandBcommits * avgLineChangesInBothFiles;
    if (denominator == 0) {
      return 0.0;
    }

    double result = (((double) commonFileChangesCounter * avgCommonChangedLines) / denominator) * fileLocationCoef * COMMIT_IMPORTANCE_COEFFICIENT;

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
//  public void buildGraph() throws IOException {
//    parseFiles();
//    parseCommits();
//    updateCompoundPower();
//    applyLanguageSpecificAnalisis();
////        removeZeroWeightEdges();
//  }


  /**
   * @return map of language:filepaths
   * @throws IOException
   */
//  Map<String, List<String>> parseJson() throws IOException {
//    Map<String, List<String>> res = new HashMap<>();
//    String content =
//            new String(Files.readAllBytes(Paths.get(JSON_WITH_ALL_FILES_PATH)));
//    JSONObject jsonObject = new JSONObject(content);
//    for (String filepath : jsonObject.keySet()) {
//      JSONObject fileInfo = jsonObject.getJSONObject(filepath);
//      String language = fileInfo.getString("language");
//      // did not see the language yet
//      if (!res.containsKey(language)) {
//        res.put(language.toLowerCase(), new ArrayList<>());
//      }
//      res.get(language)
//              .add(filepath);
//    }
//    return res;
//  }
  private Map<String, List<String>> groupFilesByLanguage() {
    Map<String, List<String>> groupedFiles = new HashMap<>();

    for (FileTechnologyStack fileStack : allFiles) {
      String language = fileStack.language().toLowerCase();
      File file = fileStack.file();

      groupedFiles.computeIfAbsent(language, k -> new ArrayList<>()).add(file.getAbsolutePath());
    }

    return groupedFiles;
  }

  /**
   * Supported languages:
   * - python
   * -typescript/javascript
   *
   * @throws IOException
   */
  private void applyLanguageSpecificAnalisis() throws IOException {

    Map<String, List<String>> groupedFiles = groupFilesByLanguage();
    for (String language : groupedFiles.keySet()) {
      List<String> filenames = groupedFiles.get(language);

      List<Map.Entry<Path, Path>> toolOutput = languageAnalyzerService.AnalyzeDependencies(language, filenames);
      for (Map.Entry<Path, Path> entry : toolOutput) {
        Path pFrom = entry.getKey();
        Path pTo = entry.getValue();
        Vertex from = findVertex(pFrom);
        Vertex to = findVertex(pTo);
        Edge edge = findEdge(from, to);
        if (from == null) {
          from = addVertex(pFrom.getFileName().toString(), pFrom);
        }


        if (to == null) {
          to = addVertex(pTo.getFileName().toString(), pTo);

        }
        if (edge == null) {
          edge = addEdge(from, to);
        }
        edge.setCompoundWeight(edge.getCompoundWeight() * LANGUAGE_SPECIFIC_ANALYSIS_COEF + LANGUAGE_SPECIFIC_ANALYSIS_CONSTANT);
//        System.out.println(from.getFilepath() + " -> " + to.getFilepath());
      }
    }
  }

  /**
   * Remove edges with zero weight from the graph.
   */
  /**
   * Get commit history from repository.
   *
   * @return List of commits with changed files
   */
  private List<List<ChangedFile>> getCommitHistory() {
    GitCommitParser parser = new GitCommitParser(repository);
    return new ArrayList<>(parser.getChangeFilesInFirstNcommits(COMMIT_LIMIT));
  }

  /**
   * Find a vertex by its path.
   *
   * @param path Path to search for
   * @return Found vertex or null
   */
  public Vertex findVertex(final Path path) {
    return getNodes().stream().filter(vertex -> vertex.getFilepath().equals(path)).findFirst().orElse(null);
  }

  /**
   * Find an edge between two vertices.
   *
   * @param from Source vertex
   * @param to   Target vertex
   * @return Found edge or null
   */
  public Edge findEdge(Vertex from, Vertex to) {
    return getOutgoingEdges(from).stream().filter(edgeInfo -> edgeInfo.destination().equals(to)).map(Graph.EdgeInfo::edgeData).findFirst().orElse(
            null);
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
    super.addNode(newVertex);
    return newVertex;
  }

  /**
   * Add a new edge to the graph.
   *
   * @param from Source vertex
   * @param to   Target vertex
   * @return Created edge
   */
  private Edge addEdge(final Vertex from, final Vertex to) {
    Edge newEdge = new Edge(from, to);
    super.addEdge(from, to, newEdge);
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

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Vertex vertex = (Vertex) o;
      return Objects.equals(filepath, vertex.filepath);
    }

    @Override
    public int hashCode() {
      return Objects.hash(filepath);
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
     * @param toFile   Target vertex
     */
    Edge(final Vertex fromFile, final Vertex toFile) {
      this.from = fromFile;
      this.to = toFile;
      initFileLocationCoef(fromFile.getFilepath().toString(), toFile.getFilepath().toString());
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

      this.FILE_LOCATION_COEF = (1.5 - depthRatio) * LOCATION_VALUE_COEFFICIENT;
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

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Edge edge = (Edge) o;
      return Objects.equals(from, edge.from) && Objects.equals(to, edge.to);
    }

    @Override
    public int hashCode() {
      return Objects.hash(from, to);
    }
  }
}
