package iu.sna.application.llm_stages;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import iu.sna.infrastructure.LLM;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class CrossReferenceStandardGenerator {
  private final String prompt;
  private final LLM llm;

  public CrossReferenceStandardGenerator(
          LLM llm,
          Collection<String> abstractStandards,
          Collection<Path> context) throws IOException {
    this.llm = llm;

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
                  
                  You are given with context as well:
                  
                  %s
                  
                  Your task is use given context and abstract standards
                  to derive concrete standards for each technology (i.e. any framework, instrument, library).\n
                 
                  The output should be in format <standard1> <standard2> <standardN> and contain only standards!!!

                  """.formatted(standards.toString(), contextString.toString());
    // load the prompt and combine with abstract standards
    // use context


  }

  public CrossReferenceStandardGenerator(Collection<String> abstractStandards, LLM llm) {
    this.llm = llm;
    StringBuilder standards = new StringBuilder();


    for (String standard : abstractStandards) {
      standards.append(standard).append(" ");
    }

    this.prompt = """
                  You are The best expert in analyzing software development practices and standards.
                  You are given with the following abstract standards:
                  
                  %s
                  
                  Your task is to derive concrete standards from given abstract standards for the technology (i.e. any framework, instrument, library).
             
                  The output should be in format <standard1> <standard2> <standardN>.
                  """.formatted(standards.toString());
  }

  // TODO: тут должен же быть list???
  public Map<String, List<String>> generateFor(Collection<String> technologies) {
    // use LLM to generate technology-specific standard
    StringBuilder prompt = new StringBuilder();
    Map <String, List<String>>res = new HashMap<>();
    for (String technology : technologies) {
    prompt.setLength(0);
      prompt.append("You analyzing the following technology: \n");
      prompt.append(technology).append("\n").append(this.prompt);

      String response = this.llm.nextModel().chat(ChatRequest.builder().messages(UserMessage.from(prompt.toString())).build()).aiMessage().toString();

      List <String> recieveStandards = Arrays.stream(response.split("\\s+")).filter(s -> !s.isEmpty()).toList();

      res.put(technology, recieveStandards);
    }


    // return standards for each technology
    return res;
  }
}
