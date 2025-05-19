package com.domain.repository_scanner.stack_analysis_core.core;

import java.io.File;

public class BasicAnalyzer {
    private final String name;

    public BasicAnalyzer(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public AnalysisResult analyze(File projectDir) {
        return new AnalysisResult();
    }
}
