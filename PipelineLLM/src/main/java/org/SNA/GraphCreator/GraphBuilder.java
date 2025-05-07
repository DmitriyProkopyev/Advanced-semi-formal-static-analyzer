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
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class GraphBuilder implements Graph {

  /*
  <filename,path>
   */
  private Map<String, Path> filenameAndPath;

  private ArrayList<Edge> graph;

  private final Path projectRoot;

  public GraphBuilder(final String root) {
    this.projectRoot = Paths.get(root);
    this.filenameAndPath = new HashMap<>();
    this.graph = new ArrayList<>();
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
                      path -> {
                        String fileName = path.getFileName()
                                .toString();
                        int dotIndex = fileName.lastIndexOf('.');
                        return (dotIndex != -1) ? fileName.substring(
                                0, dotIndex) : fileName;
                      },
                      path -> path,
                      // if we havedublicate keys -> pick the last one
                      (oldPath, newPath) -> newPath
              ));

    }
    // list of all found files
    List<String> filenames = new ArrayList<>(filenameAndPath.keySet());

    // hash map: <filename><file content>
    Map<String, String> fileContents = new HashMap<>();
    for (String filename : filenames) {
      Path path = filenameAndPath.get(filename);

      try {
        fileContents.put(
                filename,
                Files.readString(path)
        );
      } catch (Exception e) {
        System.out.println("Can't open the file. " + e);
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
          if (fileContent.contains(file1)) {
            System.out.println(file2 + " содержит " + file1);
            graph.add(new Edge(
                    file1, file2, filenameAndPath.get(file1),
                    filenameAndPath.get(file2)
            ));
          }
        } catch (Exception e) {
//          System.out.println("Got Error " + e);
        }

      }
    }

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


  public class Edge {
    /**
     * fromFile is influence on to File
     **/
    private final String fromFile;

    /**
     * toFile is dependent on from file
     */
    private final String toFile;
    /**
     * path to fromFile
     */
    private final Path fromFilePath;
    /**
     * path to toFile
     */
    private final Path toFilePath;

    /**
     * influence power.
     * Assume if file A contains file B,
     * then A -> B with init weight = 1
     **/
    private int weight;


    public Edge(
            final String file1, final String file2, final Path file1Path,
            final Path file2Path) {
      this.fromFile = file1;
      this.toFile = file2;
      this.fromFilePath = file1Path;
      this.toFilePath = file2Path;
      this.weight = 1;
    }
  }
}
