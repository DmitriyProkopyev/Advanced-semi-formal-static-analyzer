package iu.sna.GraphCreator;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class FileGraph implements Graph {

  /**
   * <Path, Filename>
   **/
  private Map<Path, String> pathToFilename;
  /**
   * All edges in the graph.
   */
  private ArrayList<Edge> edges;
  /**
   * All vertices in the graph.
   */
  private ArrayList<Vertex> vertices;

  /**
   * Wrapper to staring path
   * to the project root.
   * Notice, that .git directory
   * should be included in root
   */
  private final Path projectRoot;

  /**
   * initialize graph objects.
   *
   * @param root String absolute path to the root
   */
  public FileGraph(final String root) {
    this.projectRoot = Paths.get(root); // wrap string path to Path class
    this.pathToFilename = new HashMap<>();
    this.edges = new ArrayList<>();
    this.vertices = new ArrayList<>();
  }

  private final double COMMIT_COMPOUND_WEIGHT_COEF = 1;

  /**
   * Parse recursively files.
   * The DFS algorithm was used.
   **/
  public void parseFiles() throws IOException {
    try (Stream<Path> walk = Files.walk(projectRoot)) {
      pathToFilename = walk
              // consider only regular files
              // ignore directories started with .
              .filter(path -> {
                for (Path component : path) {
                  if (component.toString()
                          .startsWith(".")) {
                    return false;
                  }
                }
                return true;
              })
              .filter(Files::isRegularFile)
              .filter(path -> !path.getFileName()
                      .toString()
                      // ignore files started with dots
                      // (i.e. .gitignore)
                      .startsWith("."))
              // convert to map
              .collect(Collectors.toMap(
                      path -> path, path -> path.getFileName()
                              .toString(),
                      // if we have duplicate keys -> pick the last one
                      (oldPath, newPath) -> newPath
              ));

    }
    // list of all found filepaths
    List<Path> filepaths = new ArrayList<>(pathToFilename.keySet());

    // hash map: <filename><file content>
    Map<Path, String> fileContents = new HashMap<>();

    // Extract file content
    // TODO: обсудить не лучше ли через Lazy download?
    for (Path filepath : filepaths) {
      try {
        fileContents.put(filepath, Files.readString(filepath));
      } catch (Exception e) {
        // got incorrect filetype (i.e. binary)
      }
    }
    for (Path filepath1 : filepaths) {
      // take filename1 from its path
      String file1 = this.pathToFilename.get(filepath1);
      for (Path filepath2 : filepaths) {
        if (filepath1.equals(filepath2)) {
          continue;
        }
        try {

          // take filename2 from its path
          String file2 = this.pathToFilename.get(filepath2);

          // fetching file2 content
          final String fileContent = fileContents.get(filepath2);


          final String nameWoExtension = file1.contains(".")
                  ? file1.substring(0, file1.lastIndexOf("."))
                  : file1;

          // TODO: в c++ у нас есть name.cpp и name.hpp...

          // check if filename1 is mention in the file2 content
          if (fileContent.contains(nameWoExtension)) {
            System.out.println(filepath2 + " содержит " + filepath1);

            Vertex from = addVertex(file1, filepath1);
            Vertex to = addVertex(file2, filepath2);
            addEdge(from, to);

          }
          // catch and ignore binary files
        } catch (Exception e) {

          // got incorrect filetype (i.e. binary)
        }

      }
    }

  }


  final int COMMIT_lIMIT = 10;
// TODO: тут ОШИБКА!
  private void updateEdgeParameters(Vertex from, Vertex to) {
    Edge edge = findEdge(from, to);
    if (edge == null) {
      edge = addEdge(from, to);
    }
    edge.incrementCountCommonCommits();
    edge.incrementCountCommonChangedLines(
            from.getTotalChangedLines() + to.getTotalChangedLines());

  }

  public void parseComitts() throws IOException {
    try (
            // Obtain .git directory
            Repository repository =
                    new RepositoryBuilder().setGitDir(new File(projectRoot
                                    + "/.git"))
                            .build();
    ) {

      // Fetch commit history
      List<List<ChangedFile>> commitHistory =
              getCommitHistory(repository);

      // Analyze commits
      // Each commit is represented by
      // List of objects ChangedFiles
      commitHistory.forEach(
              // for each commit
              // update couple power
              // between files

              (changedFiles) -> {
                ArrayList<ChangedFile> changedFileArrayList =
                        new ArrayList<>(changedFiles);

                for (int i = 0; i < changedFileArrayList.size(); i++) {

                  // obtain Vertex by filepath
                  Path filepath1 = changedFileArrayList.get(i)
                          .getPath();
                  Integer changedLines1 =
                          changedFileArrayList.get(i)
                                  .getChangedLines();
                  Vertex from = this.findVertex(filepath1);

                  // increment Vertex commit counter
                  from.incrementCountCommits();
                  from.incrementTotalChangedLines(changedLines1);

                  for (int j = i + 1; j < changedFileArrayList.size(); j++) {
                    // First update edge A -> B
                    Path filepath2 = changedFileArrayList.get(j)
                            .getPath();
                    Integer changedLines2 =
                            changedFileArrayList.get(j)
                                    .getChangedLines();
                    Vertex to = this.findVertex(filepath2);
                    updateEdgeParameters(from, to);

                    // Second update edge B->A
                    updateEdgeParameters(to, from);
                  }
                }
              }
      );
    }
  }


  private double calculateCompound(
          int commonFileChangesCounter,
          double avgCommonChangedLines,
          int totalAandBcommits,
          double avgLineChangesInBothFiles) {
    return (((double) commonFileChangesCounter * avgCommonChangedLines)
            / ((double) totalAandBcommits * avgLineChangesInBothFiles));
  }


  private void updateCompoundPower() {
    for (Edge e : edges) {
      Vertex file1 = e.getFrom();
      Vertex file2 = e.getTo();
      int commonFileChangesCounter = e.getCountCommonCommits();
      double avgCommonChangedLines =
              (double) e.getCountCommonChangedLines() / e.getCountCommonCommits();
      int totalAandBcommits =
              file1.getCountCommits() + file2.getCountCommits() - e.getCountCommonCommits();
      double avgLineChangesInBothFiles =
              (double) file1.getTotalChangedLines() / file1.getCountCommits()
                      + (double) file2.getTotalChangedLines() / file2.getCountCommits();
      e.setCompoundWeight(calculateCompound(
              commonFileChangesCounter,
              avgCommonChangedLines,
              totalAandBcommits,
              avgLineChangesInBothFiles
      ) * COMMIT_COMPOUND_WEIGHT_COEF * e.FILE_LOCATION_COEF);
    }
  }

  public void buildGraph() throws IOException {
    parseFiles();
    parseComitts();
    updateCompoundPower();
  }

  private List<List<ChangedFile>> getCommitHistory(
          final Repository repository) {
    GitCommitParser parser = new GitCommitParser(repository);
    // fetching commit history in a list format

    // Convert List into Array list

    return new ArrayList<>(
            parser.getChangeFilesInFirstNcommits(COMMIT_lIMIT));
  }

  Vertex findVertex(final Path path) {
    return this.vertices.stream()
            .filter(vertex -> vertex.getFilepath()
                    .equals(path))
            .findFirst()
            .orElse(addVertex(
                    path.getFileName()
                            .toString()
                    , path
            ));
  }

  Edge findEdge(Vertex from, Vertex to) {
    return this.edges.stream()
            .filter(edge -> edge.getFrom()
                    .equals(from)
                    && edge.getTo()
                    .equals(to))
            .findFirst()
            .orElse(null);
  }

  private Vertex addVertex(final String fileName, final Path filePath) {
    Vertex newVertex = new Vertex(fileName, filePath);
    this.vertices.add(newVertex);
    return newVertex;

  }

  private Edge addEdge(final Vertex from, final Vertex to) {
    Edge newEdge = new Edge(from, to, 0);
    this.edges.add(newEdge);
    from.getAdjVerticies()
            .add(to);
    return newEdge;


  }

  @Getter
  @Setter
  public class Edge {

    private Vertex from;

    private double FILE_LOCATION_COEF;
    private Vertex to;


    private double compoundWeight;
    private int countCommonCommits;
    private int countCommonChangedLines;

    public Edge(
            final Vertex fromFile, final Vertex toFile,
            final int influence) {
      this.from = fromFile;
      this.to = toFile;
      this.compoundWeight = influence;
      this.countCommonCommits = 0;
      this.countCommonChangedLines = 0;


    }


    public int incrementCountCommonCommits() {
      return this.countCommonCommits += 1;
    }

    public int incrementCountCommonChangedLines(int i) {
      return countCommonChangedLines += i;
    }


  }

  @Getter
  @Setter
  class Vertex {
    private final String filename;
    private final Path filepath;

    private int countCommits;
    private int totalChangedLines;

    private ArrayList<Vertex> adjVerticies = new ArrayList<>();


    private Vertex(final String file, final Path path) {
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
}
