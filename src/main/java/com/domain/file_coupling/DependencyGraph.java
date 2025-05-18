package com.domain.file_coupling;

import com.domain.repository_scanner.FileTechnologyStack;
import com.infrastructure.Graph;

import java.io.File;
import java.util.Collection;

public class DependencyGraph extends Graph<FileTechnologyStack, Double> {
    public DependencyGraph(Collection<FileTechnologyStack> files) {
        // build the entire graph
    }


}
