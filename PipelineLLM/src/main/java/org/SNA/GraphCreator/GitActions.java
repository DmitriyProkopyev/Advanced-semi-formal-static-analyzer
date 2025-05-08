package org.SNA.GraphCreator;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface GitActions {

  Map<String, List<Path>> getChangeFilesInFirstNCommits(
          final Repository repository, final int n);


  List<Path> getChangedFilesInCommit(
          final Repository repository, final ObjectId commitId);

}
