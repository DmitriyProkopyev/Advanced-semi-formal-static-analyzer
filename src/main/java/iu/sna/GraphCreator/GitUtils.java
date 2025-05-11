package iu.sna.GraphCreator;

import org.eclipse.jgit.lib.ObjectId;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface GitUtils {

  Map<String, List<Path>> getChangeFilesInFirstNcommits(
          final int n);


  Map<Path, Integer> getChangedFilesInCommit(
          final ObjectId commitId);

}
