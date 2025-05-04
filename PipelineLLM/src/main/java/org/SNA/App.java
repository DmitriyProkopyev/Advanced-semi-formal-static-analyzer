package org.SNA;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Hello world!
 */
public class App {
  public static void main(final String[] args) throws IOException {
    BufferedReader reader = new BufferedReader(
            new FileReader("src/main/java/org/SNA/api-keys.txt"));
    final String key = reader.readLine();
    final String model = "gemini-2.0-flash";
    PipelineLLM pipe = new PipelineLLM(key, model);
    pipe.sendPrompt("3");
  }
}
