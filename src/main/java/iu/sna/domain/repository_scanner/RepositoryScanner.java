package iu.sna.domain.repository_scanner;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import iu.sna.domain.repository_scanner.stack_analysis_core.tool.StackAnalyzer;
import iu.sna.infrastructure.Tree;

public class RepositoryScanner {
    private final Tree<Path> directories;
    private final StackAnalyzer analyzer;
    private Collection<FileTechnologyStack> lastScanResult = null;

    public RepositoryScanner(Tree<Path> directories) {
        this.directories = directories;
        this.analyzer = new StackAnalyzer();
    }

    public Collection<FileTechnologyStack> scan() {
        // Analyze all files in the repository and cache result
        Path rootPath = directories.getRoot().getData();
        lastScanResult = analyzer.analyzeProjectFiles(rootPath.toString());
        return lastScanResult;
    }

    public Set<String> getAllLanguages() {
        if (lastScanResult == null) scan();
        Set<String> langs = new HashSet<>();
        for (FileTechnologyStack stack : lastScanResult) {
            if (stack.language() != null) langs.add(stack.language());
        }
        return langs;
    }

    public Set<String> getAllTechnologies() {
        if (lastScanResult == null) scan();
        Set<String> techs = new HashSet<>();
        for (FileTechnologyStack stack : lastScanResult) {
            techs.addAll(stack.technologies());
        }
        return techs;
    }
    public static void main(String[] args) {
        RepositoryScanner scanner = new RepositoryScanner(
            new Tree<>(Path.of("Z:\\Users\\nblJlecoc\\Desktop\\хуй\\TOMERGE"))
        );
        scanner.scan();
        System.out.println(scanner.getAllLanguages());
        System.out.println(scanner.getAllTechnologies());
    }
}