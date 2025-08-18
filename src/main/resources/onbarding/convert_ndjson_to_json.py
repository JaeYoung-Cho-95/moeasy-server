#!/usr/bin/env python3
"""
ndjson_to_json.py

Convert an NDJSON (newline-delimited JSON) file to a single JSON file (array).
- Supports large files via streaming (default).
- Optional pretty-print mode (loads everything into memory).
- Handles .gz compressed input/output transparently.
"""

import argparse
import gzip
import io
import json
import os
import sys
from typing import TextIO, Optional

def smart_open_read(path: str, encoding: str = "utf-8") -> TextIO:
    """Open a text file for reading; supports .gz compressed files."""
    if path.endswith(".gz"):
        return gzip.open(path, mode="rt", encoding=encoding, newline="")
    return open(path, mode="rt", encoding=encoding, newline="")

def smart_open_write(path: str, encoding: str = "utf-8") -> TextIO:
    """Open a text file for writing; supports .gz compressed files."""
    if path.endswith(".gz"):
        # For gzip, do not pass encoding to gzip directly;
        # wrap the binary stream with TextIOWrapper for encoding support.
        gz = gzip.open(path, mode="wb")
        return io.TextIOWrapper(gz, encoding=encoding, newline="")
    return open(path, mode="w", encoding=encoding, newline="")

def convert_streaming(
    ndjson_path: str,
    json_path: str,
    encoding: str = "utf-8",
    ensure_ascii: bool = False,
    skip_bad_lines: bool = False,
) -> None:
    """
    Convert NDJSON -> JSON array using constant memory (streaming).
    Note: output is compact (no pretty indent) by design.
    """
    with smart_open_read(ndjson_path, encoding=encoding) as fin, smart_open_write(json_path, encoding=encoding) as fout:
        fout.write("[")
        first = True
        line_no = 0
        for raw_line in fin:
            line_no += 1
            line = raw_line.strip()
            if not line:
                continue  # skip empty lines
            try:
                obj = json.loads(line)
            except json.JSONDecodeError as e:
                if skip_bad_lines:
                    continue
                # Show a shortened preview of the offending line
                preview = (line[:200] + "...") if len(line) > 200 else line
                raise SystemExit(
                    f"JSON parse error on line {line_no}: {e}. Offending line preview: {preview}"
                )
            dumped = json.dumps(obj, ensure_ascii=ensure_ascii, separators=(",", ":"))
            if first:
                fout.write(dumped)
                first = False
            else:
                fout.write("," + dumped)
        fout.write("]\n")

def convert_pretty(
    ndjson_path: str,
    json_path: str,
    encoding: str = "utf-8",
    ensure_ascii: bool = False,
    indent: int = 2,
    skip_bad_lines: bool = False,
) -> None:
    """
    Convert NDJSON -> JSON array with pretty printing.
    WARNING: Loads the entire input into memory.
    """
    data = []
    line_no = 0
    with smart_open_read(ndjson_path, encoding=encoding) as fin:
        for raw_line in fin:
            line_no += 1
            line = raw_line.strip()
            if not line:
                continue
            try:
                data.append(json.loads(line))
            except json.JSONDecodeError as e:
                if skip_bad_lines:
                    continue
                preview = (line[:200] + "...") if len(line) > 200 else line
                raise SystemExit(
                    f"JSON parse error on line {line_no}: {e}. Offending line preview: {preview}"
                )
    with smart_open_write(json_path, encoding=encoding) as fout:
        json.dump(data, fout, ensure_ascii=ensure_ascii, indent=indent)
        fout.write("\n")

def main(argv=None):
    parser = argparse.ArgumentParser(
        description="Convert an NDJSON file to a single JSON array file."
    )
    parser.add_argument("input", help="Path to input NDJSON file (supports .gz)")
    parser.add_argument("output", help="Path to output JSON file (supports .gz)")
    parser.add_argument(
        "--encoding",
        default="utf-8",
        help='Text encoding for input/output (default: "utf-8")',
    )
    parser.add_argument(
        "--ensure-ascii",
        action="store_true",
        help="Escape non-ASCII characters in JSON output (default: False)",
    )
    parser.add_argument(
        "--skip-bad-lines",
        action="store_true",
        help="Skip lines that fail to parse instead of stopping with an error",
    )
    parser.add_argument(
        "--pretty",
        action="store_true",
        help="Pretty-print JSON output (loads entire file into memory)",
    )
    parser.add_argument(
        "--indent",
        type=int,
        default=2,
        help="Indent size when using --pretty (default: 2)",
    )

    args = parser.parse_args(argv)

    # Validate input existence
    if not os.path.exists(args.input):
        sys.exit(f"Input file not found: {args.input}")

    if args.pretty:
        convert_pretty(
            args.input,
            args.output,
            encoding=args.encoding,
            ensure_ascii=args.ensure_ascii,
            indent=args.indent,
            skip_bad_lines=args.skip_bad_lines,
        )
    else:
        convert_streaming(
            args.input,
            args.output,
            encoding=args.encoding,
            ensure_ascii=args.ensure_ascii,
            skip_bad_lines=args.skip_bad_lines,
        )

    print(f"Done. Wrote JSON to: {args.output}")

if __name__ == "__main__":
    main()
