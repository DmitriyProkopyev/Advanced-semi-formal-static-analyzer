from typing import List, Dict, Set, Tuple, Optional
from collections import defaultdict, deque

from pydantic import BaseModel, field_validator, conint, confloat
from typeguard import typechecked


class FileNodeModel(BaseModel):
    path: str
    size: conint(ge=0)


class FileEdgeModel(BaseModel):
    src: str
    dst: str
    value: confloat(ge=0.0)

    @field_validator("src", "dst")
    def _paths_must_not_be_empty(cls, v: str) -> str:
        if not v.strip():
            raise ValueError("path must be non-empty")
        return v


class ClusteringConfig(BaseModel):
    nodes: List[FileNodeModel]
    edges: List[FileEdgeModel]
    N: conint(gt=0)
    M: conint(gt=0)


@typechecked
class ContextClustering:
    def __init__(self, config: ClusteringConfig):
        self.N: int = config.N
        self.M: int = config.M

        self.nodes: Dict[str, FileNodeModel] = {n.path: n for n in config.nodes}

        edges = config.edges
        if edges:
            avg: float = sum(e.value for e in edges) / len(edges)
            threshold: float = 0.2 * avg
            self.edges: List[FileEdgeModel] = [e for e in edges if e.value >= threshold]
        else:
            self.edges = []

        self.adj: Dict[str, List[Tuple[str, float]]] = defaultdict(list)
        for e in self.edges:
            self.adj[e.src].append((e.dst, e.value))
            self.adj[e.dst].append((e.src, e.value))

        self.components: List[Set[str]] = self.find_components()


    def find_components(self) -> List[Set[str]]:
        visited: Set[str] = set()
        comps: List[Set[str]] = []
        all_paths: Set[str] = set(self.nodes.keys())

        for start in all_paths:
            if start in visited:
                continue
            queue = deque([start])
            comp: Set[str] = set()
            while queue:
                u = queue.popleft()
                if u in comp:
                    continue
                comp.add(u)
                visited.add(u)
                for v, _ in self.adj[u]:
                    if v not in comp:
                        queue.append(v)
                for e in self.edges:
                    if e.dst == u and e.src not in comp:
                        queue.append(e.src)
            comps.append(comp)
        return comps


    def build_clusters(self, comp: Set[str]) -> List[Set[str]]:
        comp_edges: List[FileEdgeModel] = [
            e for e in self.edges
            if e.src in comp and e.dst in comp
        ]
        comp_edges.sort(key=lambda e: -e.value)

        clusters: List[Set[str]] = []
        covered: Set[str] = set()

        while comp_edges:
            seed: FileEdgeModel = comp_edges.pop(0)
            cluster: Set[str] = {seed.src, seed.dst}
            size: int = self.nodes[seed.src].size + self.nodes[seed.dst].size

            improved = True
            while improved:
                improved = False
                best_gain: float = 0.0
                best_node: Optional[str] = None
                for cand in comp - cluster:
                    cand_size = self.nodes[cand].size
                    if size + cand_size > self.N:
                        continue
                    gain = sum(w for nbr, w in self.adj[cand] if nbr in cluster)
                    if gain > best_gain:
                        best_gain = gain
                        best_node = cand
                if best_node is not None:
                    cluster.add(best_node)
                    size += self.nodes[best_node].size
                    improved = True

            clusters.append(cluster)
            covered |= cluster

            comp_edges = [
                e for e in comp_edges
                if not (e.src in cluster and e.dst in cluster)
            ]

        for node in comp:
            if node not in covered:
                clusters.append({node})

        return clusters


    def merge_clusters(self, clusters: List[Set[str]]) -> List[Set[str]]:
        def merge_gain(a: Set[str], b: Set[str]) -> float:
            total: float = 0.0
            for u in a:
                for v, w in self.adj[u]:
                    if v in b:
                        total += w
            return total

        while len(clusters) > self.M:
            best_i, best_j, best_gain = -1, -1, 0.0
            L = len(clusters)
            for i in range(L):
                for j in range(i + 1, L):
                    size_i = sum(self.nodes[n].size for n in clusters[i])
                    size_j = sum(self.nodes[n].size for n in clusters[j])
                    if size_i + size_j > self.N:
                        continue
                    gain = merge_gain(clusters[i], clusters[j])
                    if gain > best_gain:
                        best_i, best_j, best_gain = i, j, gain
            if best_i < 0:
                break
            clusters[best_i] |= clusters[best_j]
            clusters.pop(best_j)

        return clusters


    def get_clusters(self) -> List[List[str]]:
        all_cls: List[Set[str]] = []
        for comp in self.components:
            all_cls.extend(self.build_clusters(comp))

        if len(all_cls) > self.M:
            all_cls = self.merge_clusters(all_cls)

        return [list(c) for c in all_cls]


if __name__ == "__main__":
    example = {
        "nodes": [
            {"path": "a.py", "size": 100},
            {"path": "b.py", "size": 200},
            {"path": "c.py", "size":  50},
            {"path": "d.py", "size": 120},
        ],
        "edges": [
            {"src": "a.py", "dst": "b.py", "value": 0.9},
            {"src": "b.py", "dst": "c.py", "value": 0.8},
            {"src": "a.py", "dst": "c.py", "value": 0.3},
            {"src": "c.py", "dst": "d.py", "value": 0.7},
        ],
        "N": 250,
        "M": 2,
    }

    cfg = ClusteringConfig.model_validate(example)

    clustering = ContextClustering(cfg)
    clusters = clustering.get_clusters()
    for idx, cl in enumerate(clusters, start=1):
        print(f"Cluster {idx}: {cl}")
