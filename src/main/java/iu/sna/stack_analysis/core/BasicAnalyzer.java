package iu.sna.stack_analysis.core;

import java.io.File;

public class BasicAnalyzer {
    private final String name;

    public BasicAnalyzer(String name) {
        this.name = name;
    }

    public AnalysisResult analyze(File projectDir) {
        return new AnalysisResult();
    }
}
