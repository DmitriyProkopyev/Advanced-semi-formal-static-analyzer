package iu.sna.application.llm_stages;

import iu.sna.infrastructure.LLM;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

public class CrossReferenceStandardGenerator {
  private final String prompt;


  public CrossReferenceStandardGenerator(
          LLM llm,
          Collection<String> abstractStandards,
          Collection<Path> context) throws IOException {

    StringBuilder contextString = new StringBuilder();
    StringBuilder standards = new StringBuilder();


    for (String standard : abstractStandards) {
      standards.append(standard).append(" ");
    }


    try {


      int i = 1;
      for (Path contxt : context) {
        contextString.append("File# ").append(i).append("\n").append(Files.readString(contxt)).append("\n");
        i += 1;
      }
    } catch (IOException e) {
      throw new IOException("Could not open the file. " + e);
    }
    this.prompt = """
                  You are The best expert in analyzing software development practices and standards.
                  You are given with the following abstract standards:
                  
                  %s
                  
                  You are given with context as well.:
                  
                  %s
                  
                  Your task is use given context and abstract standards
                  to derive concrete standards. 
                  Return only name of the standards separated by space.
                  """.formatted(standards.toString(), contextString.toString());
    // load the prompt and combine with abstract standards
    // use context


  }

  public CrossReferenceStandardGenerator(Collection<String> abstractStandards) {
    StringBuilder standards = new StringBuilder();


    for (String standard : abstractStandards) {
      standards.append(standard).append(" ");
    }

    this.prompt = """
                  You are The best expert in analyzing software development practices and standards.
                  You are given with the following abstract standards:
                  
                  %s
                  
                  Your task is to derive concrete standards from given abstract standards
                  Return only name of the standards separated by space.
                  """.formatted(standards.toString());
  }

  public Map<String, String> generateFor(Collection<String> technologies) {
    // use LLM to generate technology-specific standard
    // return standards for each technology
    return null;
  }
}
