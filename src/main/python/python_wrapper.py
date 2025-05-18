import json
from pathlib import Path
from typing import List, Dict, Any

from tokenization import analyze_files
from graph_clustering import cluster_from_dict
from parser import convert_md_to_pdf

def token(files: List[str]) -> List[Dict[str, Any]]:
    """
    return:

      { "path": str, "chars": int, "tokens": int }
    """
    paths = [Path(f) for f in files]
    stats = analyze_files(paths)
    return [
        {"path": str(p), "chars": c, "tokens": t}
        for p, c, t in stats
    ]

def graph_cluster(cfg: Dict[str, Any]) -> List[List[str]]:
    return cluster_from_dict(cfg)

def convert(input_md: str, output_pdf: str) -> None:
    convert_md_to_pdf(input_md=input_md, output_pdf=output_pdf)


if __name__ == "__main__":
    import argparse

    parser = argparse.ArgumentParser(prog="wrapper.py")
    sub = parser.add_subparsers(dest="cmd", required=True)

    p1 = sub.add_parser("token", help="Count tokens")
    p1.add_argument("files", nargs="+", help="Paths to files")

    p2 = sub.add_parser("cluster", help="Graph clustering")
    p2.add_argument("config", help="Path to JSON config file")

    p3 = sub.add_parser("convert", help="MD→PDF with emoji")
    p3.add_argument("input_md", help="Path to .md")
    p3.add_argument("output_pdf", help="Path to .pdf")

    args = parser.parse_args()

    if args.cmd == "token":
        res = token(args.files)
        print(json.dumps(res, ensure_ascii=False))
    elif args.cmd == "cluster":
        cfg = json.loads(Path(args.config).read_text(encoding="utf-8"))
        res = graph_cluster(cfg)
        print(json.dumps(res, ensure_ascii=False))
    elif args.cmd == "convert":
        convert(args.input_md, args.output_pdf)
        print(json.dumps({"status":"ok"}, ensure_ascii=False))
    else:
        print("No such method")
