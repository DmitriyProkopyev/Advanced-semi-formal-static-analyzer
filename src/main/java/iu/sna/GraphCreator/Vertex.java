package iu.sna.GraphCreator;

import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;
import java.util.ArrayList;

@Getter
@Setter
class Vertex {
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
