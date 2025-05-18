package org.SNA.tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.SNA.analyzers.CheckstyleAnalyzer;
import org.SNA.analyzers.PMDAnalyzer;
import org.SNA.analyzers.SemgrepAnalyzer;
import org.SNA.analyzers.SpotBugsAnalyzer;
import org.SNA.core.ToolResult;
import org.SNA.core.interfaces.IAnalysisTool;

public class CodeAnalyzer {
    private final List<IAnalysisTool> tools = new ArrayList<>();
    private final String projectPath;
    
    public CodeAnalyzer(String projectPath) {
        this.projectPath = projectPath;
        initializeTools();
    }
    
    private void initializeTools() {
        tools.add(new CheckstyleAnalyzer());
        tools.add(new PMDAnalyzer());
        tools.add(new SpotBugsAnalyzer());
        tools.add(new SemgrepAnalyzer());
        // tools.add(new OpenAPIAnalyzer());
        // tools.add(new GitHistoryAnalyzer());
    }
    
    private AnalysisReport runAnalysis() {
        AnalysisReport report = new AnalysisReport();
        
        for (IAnalysisTool tool : tools) {
            try {
                ToolResult result = tool.analyze(projectPath);
                report.addResult(tool.getName(), result);
            } catch (Exception e) {
                report.addError(tool.getName(), e.getMessage());
            }
        }
        
        return report;
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please, specify project path. Usage: java CodeAnalyzer <project-path>");
            return;
        }
        
        String projectPath = args[0];
        CodeAnalyzer analyzer = new CodeAnalyzer(projectPath);
        AnalysisReport report = analyzer.runAnalysis();
        
        report.printReport();
        
        if (report.hasErrors()) {
            System.exit(1);
        }
    }
}

class AnalysisReport {
    private final Map<String, ToolResult> results = new HashMap<>();
    private final Map<String, String> errors = new HashMap<>();
    
    public void addResult(String toolName, ToolResult result) {
        results.put(toolName, result);
    }
    
    public void addError(String toolName, String error) {
        errors.put(toolName, error);
    }
    
    public void printReport() {
        System.out.println("\n=== Static Code Analysis Report ===\n");
        
        // Print successful tool results
        results.forEach((toolName, result) -> {
            System.out.printf("Tool: %s\n", toolName);
            System.out.printf("  Errors: %d, Warnings: %d\n", 
                result.getErrorCount(), result.getWarningCount());
            
            List<String> messages = result.getMessages();
            if (messages != null && !messages.isEmpty()) {
                System.out.println("  Issues found:");
                messages.forEach(msg -> System.out.println("  - " + msg));
            } else {
                System.out.println("  No issues found");
            }
            System.out.println();
        });
        
        // Print errors from failed tools
        if (!errors.isEmpty()) {
            System.out.println("\n=== Analysis Errors ===");
            errors.forEach((toolName, error) -> {
                System.out.printf("Tool %s failed: %s\n", toolName, error);
            });
        }
        
        // Print summary
        System.out.println("\n=== Summary ===");
        long totalErrors = results.values().stream()
            .mapToInt(ToolResult::getErrorCount)
            .sum();
        long totalWarnings = results.values().stream()
            .mapToInt(ToolResult::getWarningCount)
            .sum();
        System.out.printf("Total errors: %d, Total warnings: %d\n", totalErrors, totalWarnings);
        
        if (!errors.isEmpty()) {
            System.out.printf("%d tools failed to complete analysis\n", errors.size());
        }
    }
    
    public boolean hasErrors() {
        boolean hasAnalysisErrors = results.values().stream()
            .anyMatch(result -> result.getErrorCount() > 0);
        return hasAnalysisErrors || !errors.isEmpty();
    }
}