package org.SNA.core.interfaces;

import org.SNA.core.ToolResult;
import org.SNA.core.exceptions.*;

public interface IAnalysisTool { 
    String getName();
    ToolResult analyze(String projectPath) throws AnalysisException, Exception;
}