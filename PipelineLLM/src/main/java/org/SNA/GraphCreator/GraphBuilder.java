package org.SNA.GraphCreator;

import org.SNA.GraphCreator.GraphBuilder.Vertex;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class GraphBuilder implements Graph {

  /*
  <filename,path>
   */
  private Map<String, Path> filenameAndPath;

  private ArrayList<Edge> edges;
  private ArrayList<Vertex> vertices;
  private final Path projectRoot;

  public GraphBuilder(final String root) {
    this.projectRoot = Paths.get(root);
    this.filenameAndPath = new HashMap<>();
    this.edges = new ArrayList<>();
    this.vertices = new ArrayList<>();
  }

  /**
   * assume that filenames
   * are unique
   **/
  public void parseFiles() throws IOException {
    try (Stream<Path> walk = Files.walk(projectRoot)) {
      filenameAndPath = walk
              .filter(Files::isRegularFile)
              .filter(path -> !path.getFileName()
                      .toString()
                      .startsWith(".")) // ignore files started with dots (
              // .gitignore)
              .collect(Collectors.toMap(
                      path -> path.getFileName()
                              .toString(),
                      path -> path,
                      // if we havedublicate keys -> pick the last one
                      (oldPath, newPath) -> newPath
              ));

    }
    // list of all found files
    List<String> filenames = new ArrayList<>(filenameAndPath.keySet());

    // hash map: <filename><file content>
    Map<String, String> fileContents = new HashMap<>();

    // File content
    for (String filename : filenames) {
      Path path = filenameAndPath.get(filename);
      // if we have binary file - skip
      try {
        fileContents.put(
                filename,
                Files.readString(path)
        );
      } catch (Exception e) {
//        System.out.println("Can't open the file. " + e);
      }
    }
    for (String file1 : filenames) {
      for (String file2 : filenames) {
        if (file1.equals(file2)) {
          continue;
        }
        try {
          final String fileContent = fileContents.get(file2);
          //looking for the whole word
          //String regex = "\\b" + Pattern.quote(file1) + "\\b";
          //Pattern pattern = Pattern.compile(regex);
          //Matcher matcher = pattern.matcher(fileContent);
          final String nameWoExtension = file1.substring(
                  0,
                  file1.lastIndexOf(".")
          );
          if (fileContent.contains(nameWoExtension)) {
            System.out.println(file2 + " содержит " + file1);
            edges.add(new Edge(
                    new Vertex(
                            file1, filenameAndPath.get(file1)),
                    new Vertex(file2, filenameAndPath.get(file2)), 1
            ));

          }
        } catch (Exception e) {
//          System.out.println("Got Error " + e);
        }

      }
    }

  }



  public void pareseComits() {

  }

  public static boolean isBinary(final Path file) throws IOException {
    byte[] bytes = Files.readAllBytes(file);
    boolean hasZeroByte =
            IntStream.range(0, bytes.length)
                    .anyMatch(x -> bytes[x] == 0);
    if (hasZeroByte) {
      return true;
    }
    return false;
  }
  /*
  At the end after analyzing path and commit
  dependencies, the complete graph return
   */
//  public ArrayList<Edge> build() {
//    System.out.println("TODO");
//  }


  // Graph logic


  private void addVertex(final String fileName, final Path filePath) {
    this.vertices.add(new Vertex(fileName, filePath));

  }

  private void addEdge(final Vertex from, final Vertex to) {
    this.edges.add(new Edge(from, to, 1));

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

    public Edge(
            final Vertex fromFile, final Vertex toFile,
            final int influence) {
      this.from = fromFile;
      this.to = toFile;
      this.weight = influence;
    }


  }


  private class Vertex {
    private final String filename;
    private final Path filepath;
    private ArrayList<Vertex> adjVerticies = new ArrayList<>();

    private Vertex(final String file, final Path path) {
      this.filename = file;
      this.filepath = path;
    }
  }
}
