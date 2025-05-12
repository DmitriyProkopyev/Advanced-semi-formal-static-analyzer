package iu.sna;

import iu.sna.GraphCreator.FileGraph;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

@SpringBootApplication
public class AdvancedSemiFormalStaticAnalyzerApplication {


  public static void main(String[] args) throws IOException {
    ConfigurableApplicationContext context = SpringApplication.run(
            iu.sna.AdvancedSemiFormalStaticAnalyzerApplication.class, args);

  }
}

