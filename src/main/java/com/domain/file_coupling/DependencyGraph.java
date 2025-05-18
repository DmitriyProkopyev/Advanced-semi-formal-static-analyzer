package com.domain.file_coupling;

import com.domain.repository_scanner.FileTechnologyStack;
import com.infrastructure.Graph;
import iu.sna.GraphCreator.FileGraph;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public class DependencyGraph extends Graph<FileTechnologyStack, Double> {
    public DependencyGraph(Collection<FileTechnologyStack> files) throws IOException {
        // Create FileGraph and calculate dependencies
        FileGraph fileGraph = new FileGraph(files, "src/main/resources/application.yaml", null);
        
        // Copy nodes (files)
        for (FileGraph.Vertex vertex : fileGraph.getNodes()) {
            // Find corresponding FileTechnologyStack for this vertex
            FileTechnologyStack fileStack = files.stream()
                .filter(f -> f.file().equals(vertex.getFilepath().toFile()))
                .findFirst()
                .orElse(null);
                
            if (fileStack != null) {
                addNode(fileStack);
            }
        }
        
        // Copy edges with weights
        for (Graph.EdgeInfo<FileGraph.Vertex, FileGraph.Edge> edgeInfo : fileGraph.getEdges()) {
            FileTechnologyStack source = files.stream()
                .filter(f -> f.file().equals(edgeInfo.source().getFilepath().toFile()))
                .findFirst()
                .orElse(null);
                
            FileTechnologyStack destination = files.stream()
                .filter(f -> f.file().equals(edgeInfo.destination().getFilepath().toFile()))
                .findFirst()
                .orElse(null);
                
            if (source != null && destination != null) {
                addEdge(source, destination, edgeInfo.edgeData().getCompoundWeight());
            }
        }
    }
}
