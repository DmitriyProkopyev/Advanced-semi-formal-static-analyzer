# Python Wrapper README

A concise guide to installing and using `python-wrapper.py` — a unified interface for three functions:

* `token` — count characters and tokens using a Gemini tokenizer
* `cluster` — cluster files based on a graph of file connections
* `convert` — convert Markdown to PDF

---

## 1. Requirements

* Python 3.8 or higher
* Install dependencies:

  ```bash
  pip install -r requirements.txt
  ```

  Your `requirements.txt`:

  ```text
  google-cloud-aiplatform[tokenization]
  sentencepiece
  pydantic
  typeguard
  pypandoc
  ```

## 2. Setup

1. **Project structure**:

   ```
   project/
   ├── python-wrapper.py  # the CLI wrapper script
   ├── config.json        # tokenizer configuration
   ├── graph.json         # example config for clustering
   └── requirements.txt
   ```

2. **config.json** for tokenization (`python-wrapper.token`):

   ```json
   {
     "model_name": "gemini-1.5-pro-002"
   }
   ```

   *(Choose a model supported by your tokenizer.)*

3. **graph.json** for graph clustering (`python-wrapper.cluster`):

   ```json
   {
     "nodes": [
       {"path": "a.py", "size": 100},
       {"path": "b.py", "size": 200},
       {"path": "c.py", "size": 50}
     ],
     "edges": [
       {"src": "a.py", "dst": "b.py", "value": 0.9},
       {"src": "b.py", "dst": "c.py", "value": 0.8}
     ],
     "N": 250,
     "M": 2
   }
   ```

## 3. CLI Usage

```bash
# 1) Tokenization
  python wrapper.py token <file1> <file2> ...
# → outputs a JSON array of objects: {"path": str, "chars": int, "tokens": int}

# 2) Graph Clustering
 python wrapper.py cluster <config.json>
# → outputs a JSON array of clusters, e.g. [["a.py","b.py"], ["c.py"]]

# 3) Markdown → PDF
 python wrapper.py converting <input.md> <output.pdf>
# → generates the PDF and prints {"status":"ok"}
```

## 4. Python API

You can import and call the functions directly:

```python
from python_wrapper import token, graph_cluster, convert
import json

# 1) Tokenization
stats = token(["docs/readme.md", "file.txt"])

# 2) Clustering
graph_cfg = json.load(open("graph.json"))
clusters = graph_cluster(graph_cfg)

# 3) Conversion
convert("input.md", "output.pdf")
```

---
