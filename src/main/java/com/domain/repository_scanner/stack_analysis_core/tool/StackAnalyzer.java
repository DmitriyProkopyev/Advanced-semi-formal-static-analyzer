package com.domain.repository_scanner.stack_analysis_core.tool;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.domain.repository_scanner.FileTechnologyStack;
import com.domain.repository_scanner.stack_analysis_core.analyzers.ScanCodeAnalyzer;
import com.domain.repository_scanner.stack_analysis_core.core.AnalysisResult;
import com.domain.repository_scanner.stack_analysis_core.interfaces.IAnalyzer;

public class StackAnalyzer {
    private final List<IAnalyzer> analyzers = new ArrayList<>();
    
    public StackAnalyzer() {
        // analyzers.add(new LinguistAnalyzer());
        analyzers.add(new ScanCodeAnalyzer());
        // analyzers.add(new AppInspectorAnalyzer());
    }
    
    public AnalysisResult analyzeProject(String projectPath) {
        File projectDir = new File(projectPath);
        AnalysisResult combinedResult = new AnalysisResult();
        
        for (IAnalyzer analyzer : analyzers) {
            AnalysisResult result = analyzer.analyze(projectDir);
            combinedResult.merge(result);
        }

        return combinedResult;

        // // Visualize result
        // System.out.println("Languages found:");
        // for (Map.Entry<String, Double> entry : result.getLanguagePercentages().entrySet()) {
        //     System.out.println(entry.getKey() + " - " + entry.getValue());
        // }
        // System.out.println("\nTech stack found:");
        // for (Map.Entry<String, String> entry : result.getTechnologies().entrySet()) {
        //     System.out.println(entry.getKey() + " - " + entry.getValue());
        // }
        // System.out.println("\nProject licenses:");
        // for (Map.Entry<String, String> entry : result.getLicenses().entrySet()) {
        //     System.out.println(entry.getKey() + " - " + entry.getValue());
        // }
    }

    /**
     * Анализирует проект и возвращает список FileTechnologyStack, а также выводит их на экран
     */
    public List<FileTechnologyStack> analyzeProjectFiles(String projectPath) {
        ScanCodeAnalyzer sca = new ScanCodeAnalyzer();
        List<FileTechnologyStack> stacks = sca.analyzeFiles(new File(projectPath));
        ScanCodeAnalyzer.print_pisun(stacks);
        return stacks;
    }
}