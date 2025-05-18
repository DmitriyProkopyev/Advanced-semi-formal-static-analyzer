package iu.sna.GraphCreator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Graph<TNode, TEdge> {

    private final Set<TNode> nodes = new HashSet<>();
    private final Map<TNode, List<Edge<TNode, TEdge>>> adjacencyList = new HashMap<>();

    private record Edge<D, E>(D destination, E edgeData) { }

    public record EdgeInfo<S, E>(S source, S destination, E edgeData) { }

    public void addNode(TNode node) {
        nodes.add(node);
    }

    public void addEdge(TNode source, TNode destination, TEdge edgeData) {
        if (!nodes.contains(source)) {
            addNode(source);
        }
        if (!nodes.contains(destination)) {
            addNode(destination);
        }

        List<Edge<TNode, TEdge>> edges = adjacencyList.computeIfAbsent(source, k -> new ArrayList<>());
        edges.add(new Edge<>(destination, edgeData));
    }

    public Set<TNode> getNodes() {
        return Collections.unmodifiableSet(nodes);
    }

    public List<EdgeInfo<TNode, TEdge>> getEdges() {
        List<EdgeInfo<TNode, TEdge>> edges = new ArrayList<>();
        for (Map.Entry<TNode, List<Edge<TNode, TEdge>>> entry : adjacencyList.entrySet()) {
            TNode source = entry.getKey();
            for (Edge<TNode, TEdge> edge : entry.getValue()) {
                edges.add(new EdgeInfo<>(source, edge.destination(), edge.edgeData()));
            }
        }
        return edges;
    }

    public List<EdgeInfo<TNode, TEdge>> getOutgoingEdges(TNode source) {
        List<Edge<TNode, TEdge>> edges = adjacencyList.getOrDefault(source, Collections.emptyList());
        List<EdgeInfo<TNode, TEdge>> result = new ArrayList<>();
        for (Edge<TNode, TEdge> edge : edges) {
            result.add(new EdgeInfo<>(source, edge.destination(), edge.edgeData()));
        }
        return result;
    }

    public boolean containsNode(TNode node) {
        return nodes.contains(node);
    }

    public boolean containsEdge(TNode source, TNode destination, TEdge edgeData) {
        if (!adjacencyList.containsKey(source)) {
            return false;
        }
        for (Edge<TNode, TEdge> edge : adjacencyList.get(source)) {
            if (edge.destination().equals(destination) && edge.edgeData().equals(edgeData)) {
                return true;
            }
        }
        return false;
    }
}
