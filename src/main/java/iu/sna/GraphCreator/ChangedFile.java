package iu.sna.GraphCreator;

import java.nio.file.Path;

public class ChangedFile {
  Path path;



  Integer changedLines;


  public ChangedFile (Path filepath, int changes) {
    this.path = filepath;
    this.changedLines = changes;
  }
  public Integer getChangedLines() {
    return changedLines;
  }

  public Path getPath() {
    return path;
  }
}
