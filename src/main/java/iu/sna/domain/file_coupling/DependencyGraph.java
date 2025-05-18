package iu.sna.domain.file_coupling;

import iu.sna.domain.repository_scanner.FileTechnologyStack;
import iu.sna.infrastructure.Graph;

import java.util.Collection;

public class DependencyGraph extends Graph<FileTechnologyStack, Double> {
    public DependencyGraph(Collection<FileTechnologyStack> files) {
        // build the entire graph
    }
}
