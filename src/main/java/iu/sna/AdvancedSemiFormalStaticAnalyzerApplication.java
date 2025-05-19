package iu.sna;

<<<<<<< HEAD
import iu.sna.cli.validator.ValidationException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.shell.command.annotation.CommandScan;

import java.util.Optional;

@CommandScan
@SpringBootApplication
public class AdvancedSemiFormalStaticAnalyzerApplication {
    public static void main(String[] args) {
        try {
            SpringApplication.run(AdvancedSemiFormalStaticAnalyzerApplication.class, args);
        } catch (ValidationException exception) {
            System.out.println("[WARN]: " + logMessage(exception));
        } catch (Exception exception) {
            System.out.println("[ERROR]: " + logMessage(exception));
        } finally {
            return;
        }
    }

    private static String logMessage(Exception exception) {
        return exception.getClass().getSimpleName() +
                " - " +
                exception.getMessage() +
                " - " +
                Optional.ofNullable(exception.getCause())
                        .map(Throwable::getClass)
                        .map(Class::getSimpleName)
                        .orElse("No Throwable passed") +
                "\n";
    }
}
=======
import iu.sna.GraphCreator.FileGraph;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

@SpringBootApplication
public class AdvancedSemiFormalStaticAnalyzerApplication {


  public static void main(String[] args) throws IOException {
    ConfigurableApplicationContext context = SpringApplication.run(
            iu.sna.AdvancedSemiFormalStaticAnalyzerApplication.class, args);
//    MadgeAnalyzer madgeAnalyzer = context.getBean(MadgeAnalyzer.class);
//    madgeAnalyzer.
    FileGraph f = context.getBean(FileGraph.class);
    f.buildGraph();
    
    // Получаем все ребра через getEdges() из базового класса Graph
    var edges = f.getEdges();
    for (var edgeInfo : edges) {
        FileGraph.Edge edge = edgeInfo.edgeData();
        FileGraph.Vertex from = edgeInfo.source();
        FileGraph.Vertex to = edgeInfo.destination();
        
        System.out.println(
            from.getFilename() + " -> " + 
            to.getFilename() + 
            " (weight: " + edge.getCompoundWeight() + ")"
        );
    }
  }
}

>>>>>>> implement_language_specific_analsis
