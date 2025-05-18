package com.domain.file_coupling;

import com.infrastructure.Graph;
import iu.sna.GraphCreator.FileGraph;
import iu.sna.GraphCreator.FileGraph.Vertex;
import iu.sna.GraphCreator.FileGraph.Edge;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


@ComponentScan(basePackages = {
    "iu.sna.GraphCreator",
    "com.domain.file_coupling",
    "com.infrastructure"
})
public class DependencyGraph extends Graph<ContextFile, Double> {
    private final Map<File, ContextFile> fileToContextFile;
    private final AnnotationConfigApplicationContext context;

    public DependencyGraph(Collection<File> files) {
        this.fileToContextFile = new HashMap<>();
        
        this.context = new AnnotationConfigApplicationContext(DependencyGraph.class);
        
        // Получаем FileGraph из контекста
        FileGraph fileGraph = context.getBean(FileGraph.class);
        
        // Создаем вершины
        for (File file : files) {
            int sizeToken  = Math.toIntExact((file.length() / 4));
            // пока так считаем tokenSIze
            ContextFile contextFile = new ContextFile(file, sizeToken);
            this.fileToContextFile.put(file, contextFile);
            super.addNode(contextFile);
        }

        // Копируем ребра из FileGraph
        for (iu.sna.GraphCreator.Graph.EdgeInfo<Vertex, Edge> edgeInfo : fileGraph.getEdges()) {
            Vertex sourceVertex = edgeInfo.source();
            Vertex targetVertex = edgeInfo.destination();
            Edge edge = edgeInfo.edgeData();

            File sourceFile = sourceVertex.getFilepath().toFile();
            File targetFile = targetVertex.getFilepath().toFile();

            if (fileToContextFile.containsKey(sourceFile) && fileToContextFile.containsKey(targetFile)) {
                ContextFile sourceContextFile = fileToContextFile.get(sourceFile);
                ContextFile targetContextFile = fileToContextFile.get(targetFile);

                super.addEdge(sourceContextFile, targetContextFile, edge.getCompoundWeight());
            }
        }
    }

    public ContextFile getContextFile(File file) {
        return fileToContextFile.get(file);
    }
    
    // Метод для закрытия контекста, когда он больше не нужен
    public void close() {
        if (context != null) {
            context.close();
        }
    }
}