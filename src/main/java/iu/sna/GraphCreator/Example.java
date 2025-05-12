package iu.sna.GraphCreator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

@SpringBootApplication
public class Example {


  public static void main(String[] args) throws IOException {
    ConfigurableApplicationContext context = SpringApplication.run(
            Example.class, args);
// NB! Set directory with .git folder in application.yaml
    Example app =
            context.getBean(Example.class);
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

