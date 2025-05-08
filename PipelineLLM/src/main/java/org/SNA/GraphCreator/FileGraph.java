package org.SNA.GraphCreator;

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

public class FileGraph implements Graph {

  /*
  <Path, Filename>
   */
  private Map<Path, String> pathFilename;
  private ArrayList<Edge> edges;
  private ArrayList<Vertex> vertices;
  private final Path projectRoot;

  public FileGraph(final String root) {
    this.projectRoot = Paths.get(root);
    this.pathFilename = new HashMap<>();
    this.edges = new ArrayList<>();
    this.vertices = new ArrayList<>();
  }

  /**
   * assume that filenames
   * are unique
   **/
  public void parseFiles() throws IOException {
    try (Stream<Path> walk = Files.walk(projectRoot)) {
      pathFilename = walk.filter(Files::isRegularFile)
              .filter(path -> !path.getFileName()
                      .toString()
                      .startsWith(".")) // ignore files started with dots (
              // .gitignore)
              .collect(Collectors.toMap(
                      path -> path, path -> path.getFileName()
                              .toString(),
                      // if we have dublicate keys -> pick the last one
                      (oldPath, newPath) -> newPath
              ));

    }
    // list of all found files
    List<Path> filepaths = new ArrayList<>(pathFilename.keySet());

    // hash map: <filename><file content>
    Map<Path, String> fileContents = new HashMap<>();

    // File content
    for (Path filepath : filepaths) {
      // if we have binary file - skip
      try {
        fileContents.put(filepath, Files.readString(filepath));
      } catch (Exception e) {
//        System.out.println("Can't open the file. " + e);
      }
    }
    for (Path filepath1 : filepaths) {
      // take filename1 from its path
      String file1 = this.pathFilename.get(filepath1);
      for (Path filepath2 : filepaths) {
        if (filepath1.equals(filepath2)) {
          continue;
        }
        try {

          // take filename2 from its path
          String file2 = this.pathFilename.get(filepath2);

          // fetching file content
          final String fileContent = fileContents.get(filepath2);


          //looking for the whole word
          //String regex = "\\b" + Pattern.quote(file1) + "\\b";
          //Pattern pattern = Pattern.compile(regex);
          //Matcher matcher = pattern.matcher(fileContent);

          final String nameWoExtension =
                  file1.substring(0, file1.lastIndexOf("."));

          // TODO: в c++ у нас есть name.cpp и name.hpp...

          // check if filename1 is mention in the file2 content
          if (fileContent.contains(nameWoExtension)) {
            System.out.println(file2 + " содержит " + file1);

            Vertex from = addVertex(file1, filepath1);
            Vertex to = addVertex(file2, filepath2);
            this.addEdge(from, to);

          }
          // catch and ignore binary files
        } catch (Exception e) {
//          System.out.println("Got Error " + e);
        }

      }
    }

  }

  Vertex findVertex(Path path) {
    return this.vertices.stream()
            .filter(vertex -> vertex.getFilepath()
                    .equals(path))
            .findFirst()
            .orElse(null);
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

  final int COMMIT_lIMIT = 10;

  public void parseComits(final String repositoryDir) throws IOException {
    try (
            Repository repository =
                    new RepositoryBuilder().setGitDir(new File(repositoryDir))
                            .build();
    ) {

      GitCommitParser parser = new GitCommitParser(repository);
      // fetching commit history in a list format
      LinkedHashMap<String, List<Path>> commitHistoryList =
              new LinkedHashMap<>(
                      parser.getChangeFilesInFirstNCommits(COMMIT_lIMIT));

      // Convert List into Array list
      Map<String, ArrayList<Path>> commitHistory =
              new LinkedHashMap<>();
      commitHistoryList.forEach(
              (key, value) -> commitHistory.put(key, new ArrayList<>(value))
      );
      commitHistory.forEach(
              // for each commit
              // update connection power
              // between files
              // i.e. add + 1 to vertex commit counters
              // add +1 to edge commonCommit counter
              (commit, filepath) -> {
                for (int i = 0; i < filepath.size(); i++) {
                  Vertex from = this.findVertex(filepath.get(i));
                  from.incrementCountCommits();

                  for (int j = i + 1; j < filepath.size(); j++) {
                    Vertex to = this.findVertex(filepath.get(j));
                    Edge edge = this.findEdge(from, to);
                    to.incrementCountCommits();
                    edge.incrementCountCommonCommits();

                  }
                }
              }
      );
    }
  }


  private Vertex addVertex(final String fileName, final Path filePath) {
    Vertex newVertex = new Vertex(fileName, filePath);
    this.vertices.add(newVertex);
    return newVertex;

  }

  private Edge addEdge(final Vertex from, final Vertex to) {
    Edge newEdge = new Edge(from, to, 1);
    this.edges.add(newEdge);
    from.getAdjVerticies()
            .add(to);
    return newEdge;


  }


  public class Edge {

    private Vertex from;


    private Vertex to;


    /**
     * influence power.
     * Assume if file A contains file B,
     * then A -> B with init weight = 1
     **/
    private int weight;
    private int countCommonCommits;

    public Edge(
            final Vertex fromFile, final Vertex toFile,
            final int influence) {
      this.from = fromFile;
      this.to = toFile;
      this.weight = influence;
      this.countCommonCommits = 0;
    }

    public Vertex getFrom() {
      return from;
    }

    public Vertex getTo() {
      return to;
    }

    public int incrementCountCommonCommits() {
      return this.countCommonCommits += 1;
    }
  }


  class Vertex {
    private final String filename;
    private final Path filepath;


    private ArrayList<Vertex> adjVerticies = new ArrayList<>();
    private int countCommits;

    private Vertex(final String file, final Path path) {
      this.filename = file;
      this.filepath = path;
    }

    public ArrayList<Vertex> getAdjVerticies() {
      return adjVerticies;
    }

    public String getFilename() {
      return filename;
    }

    public Path getFilepath() {
      return filepath;
    }

    public int getCountCommits() {
      return countCommits;
    }

    public int incrementCountCommits() {
      return this.countCommits += 1;
    }
  }
}
