package org.SNA.analyzers;

import org.SNA.core.interfaces.IAnalysisTool;
import org.SNA.core.ToolResult;
import org.SNA.core.exceptions.AnalysisException;

import edu.umd.cs.findbugs.*;
import edu.umd.cs.findbugs.classfile.ClassDescriptor;
// import edu.umd.cs.findbugs.classfile.IErrorLogger;
import edu.umd.cs.findbugs.classfile.MethodDescriptor;

// import java.io.File;
import java.util.Objects;

// import javax.annotation.CheckForNull;
import java.util.ArrayList;

public class SpotBugsAnalyzer implements IAnalysisTool {
    private final String name = "SpotBugs";

    @Override
    public ToolResult analyze(String projectPath) throws AnalysisException {
        ToolResult result = new ToolResult();
        ArrayList<String> pDirs = new ArrayList<>();
        pDirs.add(projectPath);

        try (FindBugs2 findBugs = new FindBugs2()) {
            // Init project
            Project project = new Project();
            project.addFile(projectPath);
            project.addSourceDirs(pDirs);
            
            // Setup FindBugs
            findBugs.setProject(project);
            findBugs.setBugReporter(new CustomBugReporter(result));
            
            // Finally start the analysis
            findBugs.execute();
            
            return result;
            
        } catch (Exception e) {
            throw new AnalysisException(this.name + " analysis failed", e);
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    private static class CustomBugReporter implements BugReporter {
        private final ToolResult result;
        private int errorCount = 0;
        private int warningCount = 0;

        public CustomBugReporter(ToolResult result) {
            this.result = Objects.requireNonNull(result);
        }

        @Override
        public void reportBug(BugInstance bugInstance) {
            if (bugInstance.getPriority() <= Priorities.NORMAL_PRIORITY) {
                errorCount++;
            } else {
                warningCount++;
            }
            result.setErrorCount(errorCount);
            result.setWarningCount(warningCount);
        }

        @Override public void observeClass(ClassDescriptor classDescriptor) {}
        @Override public void logError(String message) {
            System.err.println("SpotBugs Error: " + message);
        }
        @Override public void logError(String message, Throwable e) {
            System.err.println("SpotBugs Error: " + message);
            e.printStackTrace();
        }
        @Override public void reportMissingClass(ClassNotFoundException ex) {
            logError("Missing class: " + ex.getMessage());
        }
        @Override public void reportMissingClass(ClassDescriptor classDescriptor) {
            logError("Missing class: " + classDescriptor.getClassName());
        }
        @Override public void finish() {
            System.out.println("SpotBugs analysis completed");
        }
        @Override public void reportQueuedErrors() {}
        @Override public void addObserver(BugReporterObserver observer) {}
        @Override public void setPriorityThreshold(int threshold) {}
        @Override public ProjectStats getProjectStats() {
            return null;
        }
        @Override public BugCollection getBugCollection() {
            return null;
        }
        @Override public void setErrorVerbosity(int level) {}
        @Override public void reportSkippedAnalysis(MethodDescriptor method) {}
    }
}