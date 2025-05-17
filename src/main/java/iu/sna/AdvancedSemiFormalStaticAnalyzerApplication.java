package iu.sna;

import iu.sna.GraphCreator.FileGraph;
import iu.sna.GraphCreator.LanguageAnalyzer.PydepsAnalyzer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootApplication
public class AdvancedSemiFormalStaticAnalyzerApplication {


  public static void main(String[] args) throws IOException {
    ConfigurableApplicationContext context = SpringApplication.run(
            iu.sna.AdvancedSemiFormalStaticAnalyzerApplication.class, args);
  }
}

