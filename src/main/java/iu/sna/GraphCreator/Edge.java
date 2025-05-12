package iu.sna.GraphCreator;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Edge {

  private Vertex from;

  private double FILE_LOCATION_COEF;
  private Vertex to;


  private double compoundWeight = 0;
  private int countCommonCommits = 0;
  private int countCommonChangedLines = 0;

  //    @Value("${constants.location_value_coefficient}")
  private double locationValueCoeficient = 1;

  public Edge(final Vertex fromFile, final Vertex toFile) {
    this.from = fromFile;
    this.to = toFile;
    initFileLocationCoef(
            fromFile.getFilepath()
                    .toString(),
            toFile.getFilepath()
                    .toString()
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
    while (s1[i].equals(s2[i]) && i < minLen) {
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
