package iu.sna.stack_analysis.interfaces;

import java.io.File;

import iu.sna.stack_analysis.core.AnalysisResult;

public interface IAnalyzer {
    AnalysisResult analyze(File projectDir);
}