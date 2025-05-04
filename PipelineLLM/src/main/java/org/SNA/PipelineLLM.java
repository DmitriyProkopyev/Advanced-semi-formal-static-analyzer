package org.SNA;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;

/**
 * Implement several sequential prompts to the LLM.
 * Each result of the previous prompt goes to the
 * next prompt input
 */
public class PipelineLLM {
  /**
   * API key of the LLM.
   */
  private final String apiKey;
  /**
   * Model name.
   * Model examples (gemini):
   * gemini-2.0-flash
   * gemini-1.5-flash
   * gemini-1.5-pro
   * gemini-1.0-prow
   * P.S. !USE VPN!
   */
  private final String modelName;

  /**
   * Initializing Pipeline with model and api key.
   *
   * @param key api key of the LLM
   * @param model model examples (gemini): gemini-2.0-flash,
  gemini-1.5-flash, gemini-1.5-pro, gemini-1.0-prow
   */
  public PipelineLLM(final String key, final String model) {
    this.apiKey = key;
    this.modelName = model;
  }

  /**
   * Example.
   * Let us create pipeline,
   * which calculate expression: double the input number,
   * then squared it
   *
   * @param input input value for pipeline.
   */
  public void sendPrompt(final String input) {
    System.out.println("Init value: " + input);
    // Choose model
    ChatLanguageModel model = GoogleAiGeminiChatModel.builder()
            .apiKey(apiKey)
            .modelName(modelName)
            .build();
    // System message defines set of rules before actual model utilization
    final String systemMessage =
            "You are proficient Math professor."
                    + " Your answer should consist only with one number -"
                    + " result of the calculation. "
                    + "Otherwise, if input is incorrect, "
                    + "print 2 words: Invalid Input ";
    // create prompt to double input
    String promptDouble = String.format("double the value %s", input);
    // Send prompt
    String doubledValue = model.chat(ChatRequest.builder()
                    .messages(
                            UserMessage.from(
                                    promptDouble),
                            SystemMessage.from(
                                    systemMessage)
                    )
                    .build())
            .aiMessage()
            .text();
    // Handle invalid input
    if (doubledValue.trim()
            .equals("Invalid Input")) {
      System.out.println(" Invalid input: " + input);
      return;
    }
    System.out.println("Doubled value: " + doubledValue);
    // create prompt to square previous result
    String promptSqrt = String.format("Square the value %s", doubledValue);
    // send prompt
    String sqrtValue = model.chat(ChatRequest.builder()
                    .messages(
                            SystemMessage.from(
                                    systemMessage),
                            UserMessage.from(
                                    promptSqrt)
                    )
                    .build())
            .aiMessage()
            .text();
    System.out.println("Squared value: " + sqrtValue);
  }
}
