package iu.sna.domain.file_coupling;

import iu.sna.domain.repository_scanner.FileTechnologyStack;
import iu.sna.infrastructure.Graph;
import iu.sna.infrastructure.GraphCreator.FileGraph;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;

import java.io.IOException;
import java.util.Collection;

public class DependencyGraph extends Graph<FileTechnologyStack, Double> {
    public DependencyGraph(
            Collection<FileTechnologyStack> files) throws IOException {
        // ищем среди списка файлов .git
        FileTechnologyStack repoPath = files.stream().filter(f -> f.file().getName().equals(".git")).findFirst().orElse(null);


        Collection<FileTechnologyStack> filesWithoutGit = files.stream().filter(f -> !f.file().getName().equals(".git")).toList();
        if (repoPath == null) {
            throw new IllegalStateException("Git directory not presented in " + "the " + "input");
        }
        Repository repo = new RepositoryBuilder().setGitDir(repoPath.file()).build();
        // Create FileGraph and calculate dependencies
        FileGraph fileGraph = new FileGraph(filesWithoutGit, "src/main/resources/graphConfig.txt", repo);

        // Copy nodes (files)
        for (FileGraph.Vertex vertex : fileGraph.getNodes()) {

            // Find corresponding FileTechnologyStack for this vertex
            FileTechnologyStack fileStack = files.stream().filter(f -> f.file().equals(vertex.getFilepath().toFile())).findFirst().orElse(null);

            if (fileStack != null) {
                addNode(fileStack);


                // Copy edges with weights
                for (Graph.EdgeInfo<FileGraph.Vertex, FileGraph.Edge> edgeInfo : fileGraph.getEdges()) {
                    FileTechnologyStack source = files.stream().filter(f -> f.file().equals(edgeInfo.source().getFilepath().toFile())).findFirst().orElse(null);

                    FileTechnologyStack destination = files.stream().filter(f -> f.file().equals(edgeInfo.destination().getFilepath().toFile())).findFirst().orElse(
                            null);

                    if (source != null && destination != null) {
                        addEdge(source, destination, edgeInfo.edgeData().getCompoundWeight());
                    }
                }
            }
        }
    }
}