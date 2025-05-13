package org.SNA.analyzers;

import java.util.ArrayList;
import java.util.List;

import org.SNA.core.ToolResult;
import org.SNA.core.exceptions.AnalysisException;
import org.SNA.core.interfaces.IAnalysisTool;

import edu.umd.cs.findbugs.BugCollection;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.BugReporterObserver;
import edu.umd.cs.findbugs.FindBugs2;
import edu.umd.cs.findbugs.Project;
import edu.umd.cs.findbugs.ProjectStats;
import edu.umd.cs.findbugs.SortedBugCollection;
import edu.umd.cs.findbugs.classfile.ClassDescriptor;
import edu.umd.cs.findbugs.classfile.MethodDescriptor;
import edu.umd.cs.findbugs.config.UserPreferences;

public class SpotBugsAnalyzer implements IAnalysisTool {
    @Override
    public String getName() {
        return "SpotBugs";
    }

    @Override
    public ToolResult analyze(String projectPath) throws AnalysisException {
        try (FindBugs2 findBugs = new FindBugs2();) {
            Project project = new Project();
            // Add project sources/files using string paths
            project.addFile(projectPath); // Assuming projectPath points to a jar, class file, or directory of classes
            // If projectPath is a source directory, use project.addSourceDir(projectPath);
            // You might need more sophisticated logic depending on what projectPath represents.

            CustomBugReporter reporter = new CustomBugReporter();
            findBugs.setProject(project);
            findBugs.setBugReporter(reporter);
            findBugs.setUserPreferences(UserPreferences.createDefaultUserPreferences());
            findBugs.execute();

            return new ToolResult(
                getName(),
                reporter.getBugCount(),
                0,
                reporter.getMessages()
            );
        } catch (Exception e) {
            throw new AnalysisException("SpotBugs analysis failed"+ e.getMessage() + e.getCause(), e);
        }
    }
    private static class CustomBugReporter implements BugReporter {
        private final List<String> messages;
        private final List<BugInstance> bugs;

        public CustomBugReporter() {
            this.messages = new ArrayList<>();
            this.bugs = new ArrayList<>();
        }

        @Override
        public void reportBug(BugInstance bugInstance) {
            bugs.add(bugInstance);
            // Format message similar to PMD for consistency
            String filename = bugInstance.getPrimarySourceLineAnnotation().getSourcePath();
            int line = bugInstance.getPrimarySourceLineAnnotation().getStartLine();
            String message = String.format("%s:%d - %s (%s)",
                    filename,
                    line,
                    bugInstance.getMessage(),
                    bugInstance.getType()); // Use bug type as equivalent to rule name
            messages.add(message);
        }

        public int getBugCount() {
            return bugs.size();
        }

        public List<String> getMessages() {
            return messages;
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
            return new ProjectStats();
        }
        @Override public BugCollection getBugCollection() {
            // Potentially could return a BugCollection constructed from 'bugs' list if needed elsewhere
            return new SortedBugCollection(); // Return an empty one for now, or build one from 'bugs'
        }
        @Override public void setErrorVerbosity(int level) {}
        @Override public void reportSkippedAnalysis(MethodDescriptor method) {}
    }
}