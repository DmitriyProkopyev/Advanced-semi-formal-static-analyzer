package iu.sna.GraphCreator;

import lombok.Getter;

import java.nio.file.Path;
@Getter
public class ChangedFile {
  Path path;



  Integer changedLines;


  public ChangedFile (Path filepath, int changes) {
    this.path = filepath;
    this.changedLines = changes;
  }

}
