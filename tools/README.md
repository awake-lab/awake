# Tools

Build-time and verification tooling. None of these run as part of a normal build — they
generate committed artifacts or produce reports, and you invoke them by hand.

## Asset generation

Both of these generate committed Kotlin source. Never hand-edit their output, and never
hand-author the data they produce — that is how the icon set ended up with every corner arc
flattened to line segments, and how the font atlas ended up with mismatched glyph metrics.

| Script | Generates | Notes |
|---|---|---|
| `svg_to_ui_image_vector.py` | `UiImageVector` glyph data (e.g. `HeroIcons.kt`) | Preserves curves as real cubic Beziers, converts SVG arcs exactly, keeps nested `evenodd` subpaths as holes. Rejects what the engine cannot render (strokes, transforms, crossing subpaths). Run `--self-test` after editing. See `skills/awake-icon-authoring/SKILL.md`. |
| `generate_ui_font_atlas.py` | `RobotoRegularUiFontData.kt` (packed glyph atlas + metrics) | Rasterizes a TTF into a coverage atlas via PIL. Glyph offsets and advances must stay in the same coordinate space — mixing cell-relative offsets with pen-relative advances produces uneven letter spacing. |

```bash
python3 tools/svg_to_ui_image_vector.py icon.svg --name chevronDown --dp 16 --source "Heroicons chevron-down (20/solid)"
python3 tools/svg_to_ui_image_vector.py --self-test
```

## Shadcn parity

The chain that answers "does this actually look like shadcn?" rather than "did this change?".
Read `docs/reference/shadcn-reference-pipeline.md` first.

| Script | Purpose |
|---|---|
| `fetch_shadcn_reference.sh` | Clones `shadcn-ui/ui` at a pinned SHA into `third_party/` (gitignored). Everything below depends on it. |
| `extract_shadcn_tokens.py` | Parses the pinned registry's new-york/neutral theme into `ShadcnReferenceTokens.kt`, the numeric ground truth for token tests. |
| `capture_shadcn_reference.py` | Screenshots real component demos from `ui.shadcn.com` into `docs/reference/shadcn-previews/` (playwright, fixed viewport, animations disabled). |
| `compare_parity.py` | Diffs an Awake render against a reference capture: aligned crop, heatmap, mismatch metrics. Pairing lives in `shadcn_parity_pairs.json`. |
| `generate_parity_report.py` | Regenerates `docs/reference/shadcn-parity.md` from the component inventory, token test state, and comparison metrics. |
| `generate_ui_status.py` | Regenerates `docs/reference/ui-fidelity-status.md`, the per-area status matrix. Each row's status comes from a probe against the source, so it cannot claim done for unwired work. |

```bash
tools/fetch_shadcn_reference.sh
./gradlew :samples:ui-showcase:desktopTest --tests "*ShadcnReferenceComparisonTest*"
python3 tools/generate_parity_report.py
python3 tools/generate_ui_status.py
```

Comparison metrics are only as good as the alignment between the two captures. The generated
report carries a `crop` column for exactly this reason — a `poor` row means the framing
differs too much to conclude anything, not that the component is wrong.

## Icon fidelity

Proves each shipped `HeroIcons` `UiImageVector` renders the same shape as the official
Heroicons SVG it was generated from, automatically -- see `skills/awake-icon-authoring/SKILL.md`.

| Script | Purpose |
|---|---|
| `capture_heroicons_reference.py` | Downloads each icon's official SVG and rasterizes it to a fixed 128x128 reference PNG (white fill on black, playwright/chromium) under `docs/reference/icon-previews/`. |
| `heroicons_manifest.json` | The (name, tier, Kotlin symbol) list both this script and `IconFidelityTest` read -- one source of truth, keyed by (name, tier) since Heroicons ships different path data per tier for the same name (e.g. `square-3-stack-3d` in both 20/solid and 24/solid). |

```bash
python3 tools/capture_heroicons_reference.py                    # re-capture every reference
python3 tools/capture_heroicons_reference.py --only chevron-down,camera
./gradlew :awake:engine:ui:ui-headless:desktopTest --tests "*IconFidelityTest*"
```

`IconFidelityTest` (`awake/engine/ui/ui-headless/src/desktopTest/`) renders each icon through
the real CPU rasterizer at the same 128x128 size and compares coverage-mask IoU against the
reference (threshold and measured correct-vs-corrupted separation are documented on
`passThreshold` in the test). It writes a reference/ours/diff PNG per icon to
`build/reports/icon-fidelity/` and a `metrics.tsv` — always regenerate references after adding
or regenerating an icon in `HeroIcons.kt`, and add the new entry to `heroicons_manifest.json`.

Catches gross shape errors (wrong path data, a rotated/mirrored derivation, a flattened curve).
Does not catch sub-pixel drift -- both pipelines' antialiasing differs enough that IoU alone
can't distinguish a 1px edge nudge from noise; that is a known ceiling, not a gap this guard
silently papers over.

## Preview serving

`ui_preview_server.py` and `ui_preview_watch.sh` serve rendered preview PNGs for manual
review. They are a convenience for eyeballing, not a verification gate — see
`docs/reference/ui-validation.md` for what actually counts as proof.

## Other

`jni-binding-generator/` generates JNI bindings for the native backends and has its own
documentation.
