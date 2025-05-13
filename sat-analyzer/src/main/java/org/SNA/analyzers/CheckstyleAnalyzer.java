package org.SNA.analyzers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.SNA.core.ToolResult;
import org.SNA.core.exceptions.AnalysisException;
import org.SNA.core.interfaces.IAnalysisTool;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

public class CheckstyleAnalyzer implements IAnalysisTool {
    private static final String CONFIG_FILE = "sat-analyzer/config/checkstyle.xml";

    @Override
    public String getName() {
        return "Checkstyle";
    }

    @Override
    public ToolResult analyze(String projectPath) throws AnalysisException {
        try {
            // Загрузка конфигурации (обновленный способ)
            Configuration config = ConfigurationLoader.loadConfiguration(
                CONFIG_FILE,
                new PropertiesExpander(new Properties()),
                ConfigurationLoader.IgnoredModulesOptions.OMIT
            );

            AuditListener listener = new AuditCounter();
            Checker checker = new Checker();
            try {
                checker.setModuleClassLoader(Checker.class.getClassLoader());
                checker.configure(config);
                checker.addListener(listener);

                List<File> files = getJavaFiles(new File(projectPath));
                for (File file : files) {
                    checker.process(List.of(file));
                }

                AuditCounter counter = (AuditCounter) listener;
                return new ToolResult(
                    getName(),
                    counter.getErrorCount(),
                    counter.getWarningCount(),
                    counter.getMessages()
                );
            } finally {
                checker.destroy();
            }
        } catch (CheckstyleException e) {
            throw new AnalysisException("Checkstyle analysis failed" + e.getMessage() + e.getCause(), e);
        }
    }

    private List<File> getJavaFiles(File directory) {
        List<File> javaFiles = new ArrayList<>();
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        javaFiles.addAll(getJavaFiles(file));
                    } else if (file.getName().endsWith(".java")) {
                        javaFiles.add(file);
                    }
                }
            }
        }
        return javaFiles;
    }

    public static class AuditCounter implements AuditListener {
        private int errorCount;
        private int warningCount;
        private final List<String> messages = new ArrayList<>();

        @Override
        public void auditStarted(AuditEvent event) {
            errorCount = 0;
            warningCount = 0;
            messages.clear();
        }

        @Override
        public void auditFinished(AuditEvent event) {}

        @Override
        public void fileStarted(AuditEvent event) {}

        @Override
        public void fileFinished(AuditEvent event) {}

        @Override
        public void addError(AuditEvent event) {
            if (event.getSeverityLevel() == SeverityLevel.ERROR) {
                errorCount++;
            } else if (event.getSeverityLevel() == SeverityLevel.WARNING) {
                warningCount++;
            }
            messages.add(String.format("%s:%d: %s", 
                event.getFileName(), 
                event.getLine(), 
                event.getMessage()));
        }

        @Override
        public void addException(AuditEvent event, Throwable throwable) {
            messages.add(String.format("Exception in %s: %s", 
                event.getFileName(), 
                throwable.getMessage()));
        }

        public int getErrorCount() {
            return errorCount;
        }

        public int getWarningCount() {
            return warningCount;
        }

        public List<String> getMessages() {
            return messages;
        }
    }
}