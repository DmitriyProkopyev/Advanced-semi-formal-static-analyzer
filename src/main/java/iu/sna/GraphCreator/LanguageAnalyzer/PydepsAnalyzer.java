package iu.sna.GraphCreator.LanguageAnalyzer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PydepsAnalyzer implements LanguageAnalyzer {
  private String PATH_TO_VENV;
  private static final Pattern DEPENDENCY_PATTERN = Pattern.compile("(\\w+)" +
          "\\s*->\\s*(\\w+)\\s*\\[.*?\\];");

  // TODO: что делать с venv?
  private String runTool(List<String> fileParam) throws IOException {
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
  public List<Map.Entry<Path, Path>> analyzeDependencies(
          List<String> fileString) throws IOException {
    String toolOutput = runTool(fileString);
    Matcher matcher = DEPENDENCY_PATTERN.matcher(toolOutput);
    List<Map.Entry<Path, Path>> res = new ArrayList<>();
    while (matcher.find()) {
      Path p1 = Paths.get(matcher.group(1));
      Path p2 = Paths.get(matcher.group(2));
      res.add(Map.entry(p1, p2));

    }
    return res;
  }

  @Override
  public String getLanguage() {
    return "python";
  }
}
