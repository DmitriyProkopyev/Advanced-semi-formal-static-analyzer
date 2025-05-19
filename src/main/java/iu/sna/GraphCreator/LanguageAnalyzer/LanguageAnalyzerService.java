package iu.sna.GraphCreator.LanguageAnalyzer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LanguageAnalyzerService {
  private Map<String, LanguageAnalyzer> analyzers;

  public LanguageAnalyzerService(List<LanguageAnalyzer> analyzerList) {
    analyzers = new HashMap<>();
    for (LanguageAnalyzer analyzer : analyzerList) {
      analyzers.put(analyzer.getLanguage(), analyzer);
    }
  }

  public List<Map.Entry<Path, Path>> AnalyzeDependencies(
          String language,
          List<String> fileString) throws IOException {
    LanguageAnalyzer langAnalyzer = analyzers.get(language);
    if (langAnalyzer != null) {
      return langAnalyzer.analyzeDependencies(fileString);
    } else {
      System.out.println("The analyzer for language "
              + language + " is not implemented yet");
      return null;
    }
  }
}
