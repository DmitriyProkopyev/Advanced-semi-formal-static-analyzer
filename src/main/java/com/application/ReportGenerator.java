package com.application;

import com.application.llm_stages.ValidationBlock;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

public class ReportGenerator {
    private final String titlePrefix;
    private final String titlePostfix;
    private final Path location;

    public ReportGenerator(String titlePrefix, String titlePostfix, Path location) {
        this.titlePrefix = titlePrefix;
        this.titlePostfix = titlePostfix;
        this.location = location;
    }

    public File generateReport(String name, Map<ValidationBlock, String> content) {
        var report = this.location.resolve(name).toFile();
        // transform the content into markdown file, write it to the report
        return report;
    }
}
