package iu.sna.domain.file_coupling;

import iu.sna.application.Config;
import iu.sna.infrastructure.Wrapper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class DependencyGraphOperator {
    private final DependencyGraph graph;
    private final Wrapper wrapper;
    private final JSONObject graphConfig;

    public DependencyGraphOperator(DependencyGraph graph) throws IOException, InterruptedException {
        this.graph = graph;
        this.wrapper = new Wrapper();
        this.graphConfig = constructConfig();
    }

    private JSONObject constructConfig() throws IOException, InterruptedException {
        var result = new JSONObject();
        var nodes = this.graph.getNodes();

        var paths = nodes.stream().
                map((node) -> node.file().getPath()).toList();
        var tokenizedFiles = wrapper.token(paths);
        var nodesArray = new JSONArray();

        for (var nodeInfo : tokenizedFiles) {
            var node = new JSONObject();
            node.put("path", nodeInfo.get("path"));
            node.put("size", nodeInfo.get("tokens"));
            nodesArray.put(node);
        }

        var edgesArray = new JSONArray();
        for (var edgeInfo : this.graph.getEdges()) {
            double coupling = edgeInfo.edgeData();
            var source = edgeInfo.source().file().getAbsolutePath();
            var destination = edgeInfo.destination().file().getAbsolutePath();

            var edge = new JSONObject();
            edge.put("src", source);
            edge.put("dst", destination);
            edge.put("value", coupling);
            edgesArray.put(edge);
        }

        result.put("nodes", nodesArray);
        result.put("edges", edgesArray);
        return result;
    }

    public List<List<File>> extractClusters(int contextSize, int maxClusters) throws IOException, InterruptedException {
        var config = new JSONObject(this.graphConfig.toString());
        config.put("N", contextSize);
        config.put("M", maxClusters);

        var content = config.toString().getBytes(StandardCharsets.UTF_8);
        Files.write(Config.graphConfig.toPath(), content);

        var result = wrapper.cluster(Config.graphConfig.getAbsolutePath());
        return result.stream().map(
                (collection) -> collection.stream().
                        map(File::new).toList()
        ).toList();
    }
}
