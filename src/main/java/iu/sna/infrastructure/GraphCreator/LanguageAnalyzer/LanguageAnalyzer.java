package iu.sna.infrastructure.GraphCreator.LanguageAnalyzer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface LanguageAnalyzer {
    List<Map.Entry<Path, Path>> analyzeDependencies(
            List<String> fileString) throws IOException;

    String getLanguage();

}
