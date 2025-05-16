package iu.sna.stack_analysis.tool;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import iu.sna.stack_analysis.analyzers.LinguistAnalyzer;
import iu.sna.stack_analysis.core.AnalysisResult;
import iu.sna.stack_analysis.interfaces.IAnalyzer;

public class StackAnalyzer {
    private final List<IAnalyzer> analyzers = new ArrayList<>();
    
    public StackAnalyzer() {
        analyzers.add(new LinguistAnalyzer());
        // analyzers.add(new ScanCodeAnalyzer());
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
    }
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java -jar stack_inspection.jar <project_path>");
            return;
        }

        StackAnalyzer analyzer = new StackAnalyzer();
        AnalysisResult result = analyzer.analyzeProject(args[0]);

        // Visualize result
        for (Map.Entry<String, Double> entry : result.getLanguagePercentages().entrySet()) {
            System.out.println(entry.getKey() + " - " + entry.getValue());
        }
    }
}