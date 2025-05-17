package iu.sna.GraphCreator.LanguageAnalyzer;

import iu.sna.GraphCreator.FileGraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PydepsAnalyzer implements LanguageAnalyzer {
  private final FileGraph fileGraph;
  @Value("${project.pythonVenv}")
  private String PATH_TO_VENV;
  private static final Pattern DEPENDENCY_PATTERN = Pattern.compile("(\\w+)" +
          "\\s*->\\s*(\\w+)\\s*\\[.*?\\];");
  @Autowired
  public PydepsAnalyzer(FileGraph graph) {
    this.fileGraph = graph;
  }

  public String runTool(List<String> fileParam) throws IOException {
    List<String> command = new ArrayList<>();
    command.add(PATH_TO_VENV + "/bin/pydeps");
    command.addAll(fileParam);

    command.add("--show-dot");
    command.add("--noshow");
    ProcessBuilder processBuilder = new ProcessBuilder(command);
    processBuilder.redirectErrorStream(true);

    Process process = processBuilder.start();

    StringBuffer output = new StringBuffer();

    try (BufferedReader reader =
                 new BufferedReader(
                         new InputStreamReader(process.getInputStream()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        output.append(line)
                .append("\n");
      }

      int exitCode = process.waitFor();
    } catch (InterruptedException e) {
      throw new RuntimeException("Pydeps failed with error: " + e);
    }

    return output.toString();
  }

  @Override
  public void analyzeDependencies(List<String> fileString) throws IOException {
    String toolOutput = runTool(fileString);
    Matcher matcher = DEPENDENCY_PATTERN.matcher(toolOutput);
    while (matcher.find()) {
      //
    }
  }

  @Override
  public String getLanguage() {
    return "";
  }
}
