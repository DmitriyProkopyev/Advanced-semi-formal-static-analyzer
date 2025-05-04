package org.SNA.tool;

import org.SNA.core.AnalysisTool;
import org.SNA.core.ToolResult;

// import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class CodeAnalyzer {
    private List<AnalysisTool> tools = new ArrayList<>();
    private String projectPath;
    
    public CodeAnalyzer(String projectPath) {
        this.projectPath = projectPath;
        initializeTools();
    }
    
    private void initializeTools() {
        // tools.add(new CheckstyleTool());
        // tools.add(new PMDTool());
        // tools.add(new SpotBugsTool());
        // tools.add(new SemgrepTool());
        // tools.add(new OpenAPIValidatorTool());
        // tools.add(new GitValidatorTool());
    }
    
    public AnalysisReport runAnalysis() {
        AnalysisReport report = new AnalysisReport();
        
        for (AnalysisTool tool : tools) {
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
            System.out.println("Usage: java CodeAnalyzer <project-path>");
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
    private Map<String, ToolResult> results = new HashMap<>();
    private Map<String, String> errors = new HashMap<>();
    
    public void addResult(String toolName, ToolResult result) {
        results.put(toolName, result);
    }
    
    public void addError(String toolName, String error) {
        errors.put(toolName, error);
    }
    
    public void printReport() {
        // Вывод отчета
    }
    
    public boolean hasErrors() {
        // Проверка наличия ошибок
        return false;
    }
}