package iu.sna.domain.repository_scanner;

import iu.sna.infrastructure.Tree;

import java.nio.file.Path;
import java.util.Collection;

public class RepositoryScanner {
    private final Tree<Path> directories;

    public RepositoryScanner(Tree<Path> directories) {
        this.directories = directories;
    }

    public Collection<String> getAllLanguages() {
        // aggregate and deduplicate all languages found across the project directories
        return null;
    }

    public Collection<String> getAllTechnologies() {
        // aggregate and deduplicate all technologies found across the project directories
        return null;
    }

    public Collection<FileTechnologyStack> scan() {
        // launch scanning tools and parse their results
        // aggregate the results and translate into a collection of FileTechnologyStack records
        return null;
    }
}
