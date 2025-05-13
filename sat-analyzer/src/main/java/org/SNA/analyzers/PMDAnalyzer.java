package org.SNA.analyzers;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.SNA.core.ToolResult;
import org.SNA.core.exceptions.AnalysisException;
import org.SNA.core.interfaces.IAnalysisTool;

import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.PmdAnalysis;
import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.lang.LanguageVersion;
import net.sourceforge.pmd.lang.rule.RulePriority;
import net.sourceforge.pmd.reporting.Report;
import net.sourceforge.pmd.reporting.RuleViolation;

public class PMDAnalyzer implements IAnalysisTool {
    private static final String RULESET_FILE = "sat-analyzer/config/pmd-rules.xml";

    @Override
    public String getName() {
        return "PMD";
    }

    @Override
    public ToolResult analyze(String projectPath) throws AnalysisException {
        try {
            Path inputPath = Paths.get(projectPath);
            if (!inputPath.toFile().exists()) {
                System.err.println("Warning: PMD input path does not exist: " + projectPath);
                return new ToolResult(getName(), 0, 0, Collections.emptyList());
            }

            Language java = LanguageRegistry.PMD.getLanguageById("java");
            LanguageVersion javaVersion = java.getVersion("21");
            if (javaVersion == null) {
                throw new AnalysisException("Could not determine default Java language version for PMD", new IllegalStateException());
            }

            // Configure PMD
            PMDConfiguration config = new PMDConfiguration();
            config.setInputPathList(Collections.singletonList(Paths.get(inputPath.toString())));
            config.setRuleSets(Collections.singletonList(RULESET_FILE));
            config.setDefaultLanguageVersion(javaVersion);
            config.setIgnoreIncrementalAnalysis(true);
            config.setMinimumPriority(RulePriority.MEDIUM);

            List<String> messages = new ArrayList<>();

            // And Start analysis
            try (PmdAnalysis pmd = PmdAnalysis.create(config)) {
                Report report = pmd.performAnalysisAndCollectReport();

                Path projectPathNorm = inputPath.toAbsolutePath().normalize();

                for (RuleViolation violation : report.getViolations()) {
                    String reportedFilename = violation.getLocation().getFileId().getFileName();
                    Path violationFilePath = Paths.get(reportedFilename);
                    String displayPath;

                    try {
                        Path absoluteViolationPath = violationFilePath.toAbsolutePath().normalize();
                        File projectFile = inputPath.toFile();
                        Path basePathToRelativizeAgainst = projectFile.isDirectory() ? projectPathNorm : projectPathNorm.getParent();

                        if (basePathToRelativizeAgainst != null && absoluteViolationPath.startsWith(basePathToRelativizeAgainst)) {
                            displayPath = basePathToRelativizeAgainst.relativize(absoluteViolationPath).toString();
                            if (displayPath.isEmpty() && projectFile.isFile()
                                    && projectFile.getName().equals(absoluteViolationPath.getFileName().toString())) {
                                displayPath = projectFile.getName();
                            }
                        } else if (projectFile.isFile() && absoluteViolationPath.equals(projectPathNorm)) {
                            displayPath = projectFile.getName();
                        } else {
                            displayPath = reportedFilename;
                        }
                    } catch (Exception e) {
                        System.err.println("Warning: Could not determine relative path for PMD violation in '" +
                                reportedFilename + "'. Using original path. Error: " + e.getMessage());
                        displayPath = reportedFilename;
                    }

                    String ruleName = violation.getRule() != null ? violation.getRule().getName() : "misc-rule";
                    String message = String.format("%s:%d - %s (%s)",
                            displayPath,
                            violation.getLocation().getStartLine(),
                            violation.getDescription().replace("%", "%%"),
                            ruleName);

                    messages.add(message);
                }
                
                return new ToolResult(getName(), messages.size(), 0, messages);
            }

        } catch (Exception e) {
            throw new AnalysisException("PMD analysis failed: " + e.getMessage(), e);
        }
    }
}
