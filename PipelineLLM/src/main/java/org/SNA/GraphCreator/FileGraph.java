package org.SNA.GraphCreator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
      pathFilename = walk
              .filter(Files::isRegularFile)
              .filter(path -> !path.getFileName()
                      .toString()
                      .startsWith(".")) // ignore files started with dots (
              // .gitignore)
              .collect(Collectors.toMap(
                      path -> path,
                      path -> path.getFileName().toString(),
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
        fileContents.put(
                filepath,
                Files.readString(filepath)
        );
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

          final String nameWoExtension = file1.substring(
                  0,
                  file1.lastIndexOf(".")
          );

          // TODO: в c++ у нас есть name.cpp и name.hpp...

          // check if filename1 is mention in the file2 content
          if (fileContent.contains(nameWoExtension)) {
            System.out.println(file2 + " содержит " + file1);
            this.addEdge(
                    new Vertex(
                            file1, filepath1),
                    new Vertex(file2, filepath2)
            );

          }
          // catch and ignore binary files
        } catch (Exception e) {
//          System.out.println("Got Error " + e);
        }

      }
    }

  }



  public void pareseComits() {

  }




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


  class Vertex {
    private final String filename;
    private final Path filepath;
    private ArrayList<Vertex> adjVerticies = new ArrayList<>();

    private Vertex(final String file, final Path path) {
      this.filename = file;
      this.filepath = path;
    }
  }
}
