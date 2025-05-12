package iu.sna;

import iu.sna.GraphCreator.FileGraph;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

@SpringBootApplication
public class AdvancedSemiFormalStaticAnalyzerApplication {
  @Value("${constants.commit_importance_coefficient}")
  private double s;

  public static void main(String[] args) throws IOException {
    ConfigurableApplicationContext context = SpringApplication.run(
            AdvancedSemiFormalStaticAnalyzerApplication.class, args);

    AdvancedSemiFormalStaticAnalyzerApplication app =
            context.getBean(AdvancedSemiFormalStaticAnalyzerApplication.class);
    var graph = context.getBean(FileGraph.class);
    graph.buildGraph();
    var edges = graph.getEdges();
    for (FileGraph.Edge edge : edges) {

      System.out.println(edge.getFrom()
              .getFilename() + "-- " + edge.getCompoundWeight() + " --" + edge.getTo()
              .getFilename());
    }
  }
}

