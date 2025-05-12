package org.SNA.analyzers;

import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.PmdAnalysis;
import net.sourceforge.pmd.RuleViolation;

// import java.nio.file.Path;
import java.util.List;

import org.SNA.core.ToolResult;
import org.SNA.core.exceptions.*;
import org.SNA.core.interfaces.IAnalysisTool;

public class PMD implements IAnalysisTool {
    @Override
    public ToolResult analyze(String projectPath) throws AnalysisException {
        PMDConfiguration config = new PMDConfiguration();
        // config.setInputPaths(projectPath);
        // config.setRuleSets("rulesets/java/quickstart.xml");
        
        ToolResult result = new ToolResult();
        
        try (PmdAnalysis pmd = PmdAnalysis.create(config)) {
            List<RuleViolation> violations = pmd.performAnalysisAndCollectReport().getViolations();
            result.setErrorCount(violations.size());
            
            for (RuleViolation violation : violations) {
                System.out.printf("Violation: %s at %s:%d%n",
                    violation.getDescription(),
                    violation.getFilename(),
                    violation.getBeginLine());
            }
        } catch (Exception e) {
            throw new AnalysisException("PMD analysis failed", e);
        }
        return result;
    }

    @Override
    public String getName() {
        return "PMD";
    }
}