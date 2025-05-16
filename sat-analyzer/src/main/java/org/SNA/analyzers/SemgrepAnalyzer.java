package org.SNA.analyzers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.SNA.core.ToolResult;
import org.SNA.core.exceptions.AnalysisException;
import org.SNA.core.interfaces.IAnalysisTool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SemgrepAnalyzer implements IAnalysisTool {
    private static final String ANALYZER_NAME = "Semgrep";
    // private static final String RULES_PATH = "config/semgrep-ruleset/";

    @Override
    public String getName() {
        return ANALYZER_NAME;
    }

    @Override
    public ToolResult analyze(String projectPath) throws AnalysisException {
        try {
            // Semgrep would try to use custom rules. If not present, it would use default ones (from "p/<lang>")
            ProcessBuilder pb = new ProcessBuilder(
                "semgrep",
                "--config", "p/java", // Just for example
                "--json",
                projectPath
            );
            pb.redirectErrorStream(true);

            // Start parsing th out
            // We need ONLY JSON!!
            Process process = pb.start();
            StringBuilder fullOutput = new StringBuilder();
            StringBuilder jsonOutput = new StringBuilder();
            boolean jsonStarted = false;

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    fullOutput.append(line).append("\n");
                    // Parse ONLY JSON
                    if (!jsonStarted && line.trim().startsWith("{")) {
                        jsonStarted = true;
                    }
                    if (jsonStarted) {
                        jsonOutput.append(line).append("\n");
                    }
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new AnalysisException(ANALYZER_NAME + " exited with code: " + exitCode, null);
            }

            // Return results            
            SemgrepOutputParser parser = new SemgrepOutputParser(jsonOutput.toString());
            ToolResult result = new ToolResult(
                ANALYZER_NAME,
                parser.getErrorCount(),
                parser.getWarningCount(),
                parser.getFindingsAsList()
            );
            
            return result;
        } catch (Exception e) {
            throw new AnalysisException(ANALYZER_NAME + " analysis failed: " + e.getMessage(), e);
        }
    }

    private static class SemgrepOutputParser {
        private final String jsonOutput;
        private int errorCount = 0;
        private int warningCount = 0;
        private final List<String> findings = new ArrayList<>();

        public SemgrepOutputParser(String jsonOutput) {
            this.jsonOutput = jsonOutput;
            parse();
        }

        // Extract ALL Important data from JSON
        private void parse() {
            ObjectMapper mapper = new ObjectMapper();
            try {
                JsonNode root = mapper.readTree(jsonOutput);
                JsonNode results = root.path("results");
    
                Map<String, List<String>> fileToFindings = new LinkedHashMap<>();
    
                for (JsonNode result : results) {
                    String severity = result.path("extra").path("severity").asText();
                    String path = result.path("path").asText();
                    int line = result.path("start").path("line").asInt();
                    String message = result.path("extra").path("message").asText();
                    String checkId = result.path("check_id").asText();
                    String fix = result.path("extra").path("fix").asText(null);
    
                    // List metadata
                    JsonNode metadata = result.path("extra").path("metadata");
                    List<String> cwe = extractList(metadata, "cwe");
                    List<String> owasp = extractList(metadata, "owasp");
                    List<String> references = extractList(metadata, "references");
    
                    // Count severities
                    if ("ERROR".equalsIgnoreCase(severity)) {
                        errorCount++;
                    } else if ("WARNING".equalsIgnoreCase(severity)) {
                        warningCount++;
                    }
    
                    // Form beautiful~~ text for a finding
                    StringBuilder sb = new StringBuilder();
                    sb.append("[").append(severity).append("] Line ").append(line).append(" — ").append(message).append("\n");
                    sb.append("Rule: ").append(checkId).append("\n");
    
                    if (!cwe.isEmpty()) sb.append("CWE: ").append(String.join(", ", cwe)).append("\n");
                    if (!owasp.isEmpty()) sb.append("OWASP: ").append(String.join(", ", owasp)).append("\n");
                    if (fix != null && !fix.isBlank()) sb.append("Suggested Fix:\n").append(fix).append("\n");
                    if (!references.isEmpty()) {
                        sb.append("References:\n");
                        for (String ref : references) {
                            sb.append(" - ").append(ref).append("\n");
                        }
                    }
    
                    fileToFindings.computeIfAbsent(path, k -> new ArrayList<>()).add(sb.toString().trim());
                }
    
                // Build final list of findings by files
                for (Map.Entry<String, List<String>> entry : fileToFindings.entrySet()) {
                    StringBuilder group = new StringBuilder();
                    group.append("File: ").append(entry.getKey()).append("\n");
                    group.append("────────────────────────────\n");
                    for (String f : entry.getValue()) {
                        group.append(f).append("\n\n");
                    }
                    findings.add(group.toString().trim());
                }
    
            } catch (Exception e) {
                System.err.println("[SemgrepOutputParser] Failed to parse Semgrep JSON: " + e.getMessage());
            }
        }
    

        private List<String> extractList(JsonNode metadata, String field) {
            List<String> values = new ArrayList<>();
            if (metadata != null && metadata.has(field)) {
                for (JsonNode item : metadata.get(field)) {
                    values.add(item.asText());
                }
            }
            return values;
        }

        public int getErrorCount() {
            return errorCount;
        }

        public int getWarningCount() {
            return warningCount;
        }

        public List<String> getFindingsAsList() {
            return new ArrayList<>(findings);
        }
    }
}