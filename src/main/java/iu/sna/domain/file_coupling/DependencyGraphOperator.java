package iu.sna.domain.file_coupling;

import java.io.File;
import java.util.Collection;

public class DependencyGraphOperator {
    private final DependencyGraph graph;

    public DependencyGraphOperator(DependencyGraph graph) {
        this.graph = graph;
    }

    public Iterable<Collection<File>> extractClusters(int contextSize, int maxClusters) {
        // call the graph clustering algorithm
        return null;
    }
}
