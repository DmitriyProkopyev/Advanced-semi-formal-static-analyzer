package iu.sna.GraphCreator.LanguageAnalyzer;

import java.io.IOException;
import java.util.List;

public interface LanguageAnalyzer {
  void analyzeDependencies(List<String> fileString) throws IOException;
  String getLanguage();

}
