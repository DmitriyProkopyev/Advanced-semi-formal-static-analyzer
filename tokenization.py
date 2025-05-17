import sys
from pathlib import Path
from typing import List, Tuple
import json

from vertexai.preview import tokenization

CONFIG_PATH = Path(__file__).parent / "config.json"
if not CONFIG_PATH.exists():
    raise FileNotFoundError(f"Config not found: {CONFIG_PATH}")
with CONFIG_PATH.open(encoding="utf-8") as cfgf:
    cfg = json.load(cfgf)


def is_binary(path: Path, blocksize: int = 512) -> bool:
    with path.open("rb") as f:
        chunk = f.read(blocksize)
    return b"\0" in chunk


def read_text(path: Path) -> str:
    data = path.read_bytes()
    try:
        return data.decode("utf-8")
    except UnicodeDecodeError:
        return data.decode("utf-8", errors="replace")


def analyze_files(
    paths: List[Path],
    model_name: str = cfg.get("model_name")

) -> List[Tuple[Path, int, int]]:
    tokenizer = tokenization.get_tokenizer_for_model(model_name)
    results = []

    for path in paths:
        if not path.exists():
            print(f"Файл не найден: {path}", file=sys.stderr)
            continue

        if is_binary(path):
            print(f"Бинарный файл: {path}", file=sys.stderr)
            continue

        text = read_text(path)
        char_count = len(text)
        token_count = tokenizer.count_tokens(text).total_tokens

        results.append((path, char_count, token_count))

    return results


if __name__ == "__main__":
    import argparse

    print("Использование: python tokenization.py <file path>")

    parser = argparse.ArgumentParser()
    parser.add_argument(
        "files", metavar="FILE", type=Path, nargs="+",
    )
    args = parser.parse_args()

    stats = analyze_files(args.files)
    for path, chars, toks in stats:
        print(f"{path}: {chars:,} chars → {toks:,} tokens")
