package com.domain.repository_scanner;

import java.nio.file.Path;
import java.util.List;

import com.domain.repository_scanner.stack_analysis_core.tool.StackAnalyzer;
import com.infrastructure.Tree;

public class RepositoryScanner {
    private final Tree<Path> directories;
    private final StackAnalyzer analyzer;

    public RepositoryScanner(Tree<Path> directories) {
        this.directories = directories;
        this.analyzer = new StackAnalyzer();
    }

    public List<FileTechnologyStack> scan() {
        // Analyze all files in the repository
        Path rootPath = directories.getRoot().getData();
        return analyzer.analyzeProjectFiles(rootPath.toString());
    }
}