package org.SNA.GraphCreator;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {

  final static String cpp = "/home/aziz/Projects/D-Bus-Configuration-Managed";
  final static String java =
          "/home/aziz/Projects/Advanced-semi-formal-static-analyzer/PipelineLLM/src";

  public static void main(final String[] qwe) throws IOException {
//    GraphBuilder graphBuilder = new GraphBuilder(java);
//    graphBuilder.parseFiles();
    try (
      Repository repo = new RepositoryBuilder().setGitDir(new File(
              "/home/aziz/Projects/Advanced-semi-formal-static-analyzer/.git"
              ))
              .build();
    ) {
      Map<String, List<Path>> list =
              GitUtils.getChangeFilesInFirstNCommits(repo, 20);
      list.forEach((commitId, files) -> {
        System.out.println("CommitID: " + commitId);
        files.forEach(file -> System.out.println(file.toString()));
      });
    }
  }

}
