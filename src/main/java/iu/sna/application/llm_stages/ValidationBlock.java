package iu.sna.application.llm_stages;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import iu.sna.infrastructure.LLM;

import java.io.File;
import java.util.Collection;

public class ValidationBlock {
  public final String name;
  private final Collection<String> languages;
  private final Collection<String> technologies;
  private final String standards;
  private final String instructions;
  private final LLM llm;

  public ValidationBlock(
          String name,
          Collection<String> languages,
          Collection<String> technologies,
          String standards,
          LLM llm) {
    this.name = name;
    this.languages = languages;
    this.technologies = technologies;
    this.standards = standards;
    this.llm = llm;
    this.instructions = generateInstructions();
  }

  public ValidationBlock(
          String name,
          Collection<String> languages,
          Collection<String> technologies,
          String standards,
          LLM llm,
          String instructions) {
    this.name = name;
    this.languages = languages;
    this.technologies = technologies;
    this.standards = standards;
    this.llm = llm;
    this.instructions = instructions;
  }

  public String applyOn(Collection<File> files) {
    // evaluate the given files based on the context of the validation block
    // return the criticism from the LLM
    return null;
  }

  public String unify(Collection<String> criticism) {
    // aggregate all the criticism points into a concise,
    // deduplicated, and clear markdown list
    return null;
  }

  private String generateInstructions() {
    // generate detailed actionable instructions
    String systemPrompt = """
                          You are the expert in analyzing projects for standards and best practices violation
                          """;
    String userPrompt = """
                        You are giving with the following technologies:
                        
                        %s
                        
                        You are giving with the following languages:
                        
                        %s
                        
                        You are giving with the following standards:
                        
                        %s
                        Your task is to generate instruction.Instruction sets should contain specific actionable steps and checklists that allow to
                        validate compliance with the requirements of given validation block and generate a clear
                        list of misalignment's from these requirements
                        """;

    return this.llm.nextModel().chat(
            ChatRequest.builder().messages(UserMessage.from(userPrompt), SystemMessage.from(systemPrompt)).build()).aiMessage().toString();
  }
}
