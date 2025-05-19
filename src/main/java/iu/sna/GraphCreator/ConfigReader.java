package iu.sna.GraphCreator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ConfigReader {
  private final Map<String, String> config;

  public ConfigReader(String configPath) throws IOException {
    this.config = new HashMap<>();
    loadConfig(configPath);
  }

  private void loadConfig(String configPath) throws IOException {
    Path path = Path.of(configPath);
    Files.lines(path).forEach(line -> {
      if (!line.trim().isEmpty() && !line.startsWith("#")) {
        String[] parts = line.split(":", 2);
        if (parts.length == 2) {
          config.put(parts[0].trim(), parts[1].trim());
        }
      }
    });
  }

  public String getString(String key) {
    return config.get(key);
  }

  public int getInt(String key) {
    return Integer.parseInt(config.get(key));
  }

  public double getDouble(String key) {
    return Double.parseDouble(config.get(key));
  }
}