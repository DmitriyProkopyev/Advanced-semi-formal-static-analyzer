package iu.sna.GraphCreator;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class Main {

  final static String cpp = "/home/aziz/Projects/D-Bus-Configuration-Managed" +
          "/src";
  final static String java =
          "/home/aziz/Projects/Advanced-semi-formal-static-analyzer/PipelineLLM";

  public static void main(final String[] qwe) throws IOException {
    FileGraph graph = new FileGraph("/home/aziz/Projects/Advanced-semi-formal" +
            "-static-analyzer");
    graph.buildGraph();
    var edges = graph.getEdges();
    for (FileGraph.Edge edge : edges) {

      System.out.println(edge.getFrom().getFilename() + "-- " + edge.getCompoundWeight()+ " --" + edge.getTo().getFilename());
    }

//    try (
//      Repository repo = new RepositoryBuilder().setGitDir(new File(
//              "/home/aziz/Projects/Advanced-semi-formal-static-analyzer/.git"
//              ))
//              .build();
//    ) {
//      List<List<ChangedFile>> list =
//              new GitCommitParser(repo).getChangeFilesInFirstNcommits(20);
//      list.forEach((changedFile) -> {
//        System.out.println("CommitNo: ");
//
//        changedFile.forEach(file -> System.out.println(file.getPath()));
//      });
//    }
  }

}
