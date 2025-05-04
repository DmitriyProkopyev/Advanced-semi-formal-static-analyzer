package org.SNA.core;

public interface AnalysisTool {
    String getName();
    ToolResult analyze(String projectPath) throws CustomExceptions;
}