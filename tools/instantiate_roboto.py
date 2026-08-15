#!/usr/bin/env python3
# Copyright (c) Ron June Valdoz
# SPDX-License-Identifier: Apache-2.0
"""Instantiates the static Roboto faces Awake ships from the variable source.

google/fonts ships Roboto as a single variable font (ofl/roboto/Roboto[wdth,wght].ttf), so the
static faces the atlas generator consumes are derived here rather than downloaded separately. One
source version means a weight change is only a weight change -- mixing releases would make every
metric difference unattributable, the same reason capture_font_reference.py pins its own file.

The variable source lives in tools/ because it is build input, not a shipped resource.

Usage:
    python3 tools/instantiate_roboto.py
"""

from __future__ import annotations

import copy
from pathlib import Path

from fontTools.ttLib import TTFont
from fontTools.varLib.instancer import instantiateVariableFont

REPO = Path(__file__).resolve().parent.parent
SOURCE = REPO / "tools/fonts/Roboto[wdth,wght].ttf"
OUT_DIR = REPO / "awake/ui/text/src/commonMain/resources/fonts"

# Only the weights with real call sites. Counted across awake/ui and samples: Medium 14, Normal 8,
# SemiBold 7, ExtraBold 1. Each face costs roughly 1.1 MB of generated Kotlin, so this list is
# deliberately short -- add a weight when something needs it, not in advance.
FACES = {
    "Roboto-Regular.ttf": 400,
    "Roboto-Medium.ttf": 500,
}


def main() -> int:
    source = TTFont(SOURCE)
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    for filename, weight in FACES.items():
        instance = instantiateVariableFont(
            copy.deepcopy(source), {"wght": weight, "wdth": 100}, inplace=False
        )
        out = OUT_DIR / filename
        instance.save(out)
        print(f"{filename:22} wght={weight}  {out.stat().st_size:>8,} bytes")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
