import json
from typing import List, Dict, Any
from pydantic import BaseModel, field_validator, conint, confloat
from typeguard import typechecked
from collections import defaultdict, deque

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
        self.N = config.N
        self.M = config.M
        self.nodes = {n.path: n for n in config.nodes}

        edges = config.edges or []
        avg = sum(e.value for e in edges) / len(edges) if edges else 0
        thr = 0.2 * avg
        self.edges = [e for e in edges if e.value >= thr]

        self.adj: Dict[str, List[tuple[str, float]]] = defaultdict(list)
        for e in self.edges:
            self.adj[e.src].append((e.dst, e.value))
            self.adj[e.dst].append((e.src, e.value))

        self.components = self.find_components()

    def find_components(self) -> List[set[str]]:
        visited = set()
        comps: List[set[str]] = []
        for start in self.nodes:
            if start in visited:
                continue
            queue = deque([start])
            comp = set()
            while queue:
                u = queue.popleft()
                if u in comp:
                    continue
                comp.add(u); visited.add(u)
                for v, _ in self.adj[u]:
                    if v not in comp:
                        queue.append(v)
                for e in self.edges:
                    if e.dst == u and e.src not in comp:
                        queue.append(e.src)
            comps.append(comp)
        return comps

    def build_clusters(self, comp: set[str]) -> List[set[str]]:
        comp_edges = [e for e in self.edges if e.src in comp and e.dst in comp]
        comp_edges.sort(key=lambda e: -e.value)
        clusters, covered = [], set()
        while comp_edges:
            seed = comp_edges.pop(0)
            cset = {seed.src, seed.dst}
            size = self.nodes[seed.src].size + self.nodes[seed.dst].size

            improved = True
            while improved:
                improved = False
                best_gain, best_node = 0.0, None
                for cand in comp - cset:
                    sz = self.nodes[cand].size
                    if size + sz > self.N:
                        continue
                    gain = sum(w for nbr, w in self.adj[cand] if nbr in cset)
                    if gain > best_gain:
                        best_gain, best_node = gain, cand
                if best_node:
                    cset.add(best_node)
                    size += self.nodes[best_node].size
                    improved = True

            clusters.append(cset)
            covered |= cset
            comp_edges = [e for e in comp_edges if not (e.src in cset and e.dst in cset)]

        for node in comp - covered:
            clusters.append({node})
        return clusters

    def merge_clusters(self, clusters: List[set[str]]) -> List[set[str]]:
        def gain(a: set[str], b: set[str]) -> float:
            return sum(w for u in a for v, w in self.adj[u] if v in b)

        while len(clusters) > self.M:
            best_i = best_j = -1
            best_g = 0.0
            L = len(clusters)
            for i in range(L):
                for j in range(i+1, L):
                    si = sum(self.nodes[n].size for n in clusters[i])
                    sj = sum(self.nodes[n].size for n in clusters[j])
                    if si + sj > self.N:
                        continue
                    g = gain(clusters[i], clusters[j])
                    if g > best_g:
                        best_i, best_j, best_g = i, j, g
            if best_i < 0:
                break
            clusters[best_i] |= clusters[best_j]
            clusters.pop(best_j)
        return clusters

    def get_clusters(self) -> List[List[str]]:
        out: List[set[str]] = []
        for comp in self.components:
            out.extend(self.build_clusters(comp))
        if len(out) > self.M:
            out = self.merge_clusters(out)
        return [list(c) for c in out]


def cluster_from_dict(cfg: Dict[str, Any]) -> List[List[str]]:
    config = ClusteringConfig.model_validate(cfg)
    clustering = ContextClustering(config)
    return clustering.get_clusters()


if __name__ == "__main__":
    import sys
    data = json.load(sys.stdin)
    res = cluster_from_dict(data)
    print(json.dumps(res, ensure_ascii=False))
