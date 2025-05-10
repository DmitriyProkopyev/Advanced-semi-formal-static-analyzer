package iu.sna.GraphCreator;

import org.eclipse.jgit.lib.ObjectId;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface GitUtils {

  Map<String, List<Path>> getChangeFilesInFirstNCommits(
          final int n);


  List<Path> getChangedFilesInCommit(
          final ObjectId commitId);

}
