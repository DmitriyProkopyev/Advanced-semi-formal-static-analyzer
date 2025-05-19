package com.domain.repository_scanner.stack_analysis_core.analyzers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.domain.repository_scanner.stack_analysis_core.core.AnalysisResult;
import com.domain.repository_scanner.stack_analysis_core.core.BasicAnalyzer;
import com.domain.repository_scanner.stack_analysis_core.interfaces.IAnalyzer;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LinguistAnalyzer extends BasicAnalyzer implements IAnalyzer {
    public LinguistAnalyzer() {
        super("LinguistAnalyzer");
    }

    @Override
    public AnalysisResult analyze(File filePath) {
        AnalysisResult result = new AnalysisResult();
        try {
            // Linguist CLI launch 
            ProcessBuilder pb = new ProcessBuilder(
                "github-linguist",
                "--json",
                filePath.getAbsolutePath()
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Read output
            StringBuilder jsonOutput = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonOutput.append(line);
                }

                // Wait for completion
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    throw new RuntimeException("Linguist failed with exit code " + exitCode);
                }

                // Parse JSON
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> linguistData = mapper.readValue(
                    jsonOutput.toString(),
                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {}
                );

                // Get language percentages
                Map<String, Double> languagePercentages = new HashMap<>();
                // Iterate over all languages and their details
                for (Map.Entry<String, Object> entry : linguistData.entrySet()) {
                    String language = entry.getKey();
                    Map<String, Object> details = (Map<String, Object>) entry.getValue();

                    String percentageStr = (String) details.get("percentage");
                    Double percentage = Double.parseDouble(percentageStr);
                    
                    languagePercentages.put(language, percentage);
                }
                result.setLanguagePercentages(languagePercentages);

            } catch (Exception e) {
                throw new RuntimeException("Failed to analyze with Linguist: ", e);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to start Linguist process: ", e);
        }
        return result;
    }
}
