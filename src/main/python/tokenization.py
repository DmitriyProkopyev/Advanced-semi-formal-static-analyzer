import sys
import json
from pathlib import Path
from typing import List, Tuple

from vertexai.preview import tokenization

_CONFIG_PATH = Path(__file__).parent / "config.json"
if not _CONFIG_PATH.exists():
    raise FileNotFoundError(f"Config not found: {_CONFIG_PATH}")
with _CONFIG_PATH.open(encoding="utf-8") as f:
    _CFG = json.load(f)
_MODEL_NAME = _CFG.get("model_name")


def _is_binary(path: Path, blocksize: int = 512) -> bool:
    with path.open("rb") as f:
        return b"\0" in f.read(blocksize)


def _read_text(path: Path) -> str:
    data = path.read_bytes()
    try:
        return data.decode("utf-8")
    except UnicodeDecodeError:
        return data.decode("utf-8", errors="replace")


def analyze_files(
    paths: List[Path]
) -> List[Tuple[Path, int, int]]:
    tokenizer = tokenization.get_tokenizer_for_model(_MODEL_NAME)
    result: List[Tuple[Path,int,int]] = []

    for path in paths:
        if not path.exists():
            print(f"File not found: {path}", file=sys.stderr)
            continue
        if _is_binary(path):
            print(f"Binary file: {path}", file=sys.stderr)
            continue

        text = _read_text(path)
        chars = len(text)
        tokens = tokenizer.count_tokens(text).total_tokens
        result.append((path, chars, tokens))

    return result
