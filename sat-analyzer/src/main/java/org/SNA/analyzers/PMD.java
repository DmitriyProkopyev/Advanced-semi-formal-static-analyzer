package org.SNA.analyzers;

import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.PmdAnalysis;
import net.sourceforge.pmd.RuleViolation;

// import java.nio.file.Path;
import java.util.List;

import org.SNA.core.AnalysisTool;
import org.SNA.core.ToolResult;
import org.SNA.core.CustomExceptions;

public class PMD implements AnalysisTool {
    @Override
    public String getName() {
        return "PMD";
    }

    @Override
    public ToolResult analyze(String projectPath) throws CustomExceptions {
        PMDConfiguration config = new PMDConfiguration();
        // config.setInputPaths(projectPath);
        // config.setRuleSets("rulesets/java/quickstart.xml");
        
        ToolResult result = new ToolResult();
        
        try (PmdAnalysis pmd = PmdAnalysis.create(config)) {
            List<RuleViolation> violations = pmd.performAnalysisAndCollectReport().getViolations();
            result.setErrorCount(violations.size());
            
            // Дополнительная обработка нарушений
            for (RuleViolation violation : violations) {
                System.out.printf("Violation: %s at %s:%d%n",
                    violation.getDescription(),
                    violation.getFilename(),
                    violation.getBeginLine());
            }
        } catch (Exception e) {
            throw new CustomExceptions("PMD analysis failed", e);
        }
        
        return result;
    }
}