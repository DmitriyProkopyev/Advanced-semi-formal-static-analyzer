package iu.sna.domain.repository_scanner.stack_analysis_core.interfaces;

import java.io.File;

import iu.sna.domain.repository_scanner.stack_analysis_core.core.AnalysisResult;

public interface IAnalyzer {
    AnalysisResult analyze(File projectDir);
}