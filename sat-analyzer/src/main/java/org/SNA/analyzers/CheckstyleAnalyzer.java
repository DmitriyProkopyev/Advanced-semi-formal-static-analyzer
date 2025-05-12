package org.SNA.analyzers;

import java.io.File;
import java.util.Collections;

import com.puppycrawl.tools.checkstyle.*;
import com.puppycrawl.tools.checkstyle.api.*;

import org.SNA.core.ToolResult;
import org.SNA.core.exceptions.*;
import org.SNA.core.interfaces.IAnalysisTool;


public class CheckstyleAnalyzer implements IAnalysisTool {
    private String name = "Checkstyle";
    public ToolResult analyze(String projectPath) throws Exception {
        // Load config
        Configuration config = ConfigurationLoader.loadConfiguration(
            "/config/checkstyle.xml", null
        );
        
        Checker checker = new Checker();
        checker.setModuleClassLoader(Checker.class.getClassLoader());
        checker.configure(config);
        
        try {
            AuditCounter counter = new AuditCounter();
            checker.addListener(counter);
            
            File[] files = new File(projectPath).listFiles(
                (dir, name) -> name.endsWith(".java")
            );
            
            if (files != null) {
                for (File file : files) {
                    checker.process(Collections.singletonList(file));
                }
            }
            
            ToolResult result = new ToolResult();
            result.setErrorCount(counter.getErrorCount());
            result.setWarningCount(counter.getWarningCount());
            
            return result;
        } catch (Exception e) {
            throw new AnalysisException(this.name + " analysis failed", e);
        } finally {
            checker.destroy();
        }
    }

    @Override
    public String getName() {
        return this.name;
    } 
    
    class AuditCounter implements AuditListener {
        private int errors;
        private int warnings;
        
        @Override public void addError(AuditEvent event) {
            if (event.getSeverityLevel() == SeverityLevel.ERROR) errors++;
            else warnings++;
        }
        
        @Override
        public void addException(AuditEvent event, Throwable throwable) {
            errors++;
        }
        
        @Override public void auditStarted(AuditEvent event) {}
        @Override public void auditFinished(AuditEvent event) {}
        @Override public void fileStarted(AuditEvent event) {}
        @Override public void fileFinished(AuditEvent event) {}

        public int getErrorCount() { return errors; }
        public int getWarningCount() { return warnings; }
    }
}