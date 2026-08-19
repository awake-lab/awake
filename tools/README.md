# UI tooling

This folder contains the implementation details behind Awake's UI workflow. Start with the
single front door instead of choosing scripts yourself:

```bash
scripts/awake ui <reference|preview|validate> ...
```

Use the lower-level tools only when you are maintaining the reference pipeline, adding a new
fixture, or investigating a renderer/font/icon problem.

## Choose the right lane

| Need | Use | A pass means |
|---|---|---|
| Check a component while iterating | `awake ui reference`, `preview`, or `validate` | You produced the relevant reference, Awake preview, or semantic crop. Review the generated image. |
| Prove a layout matches shadcn | `ShadcnGeometryParityTest` | The measured bounds match the pinned reference. |
| Detect an unintended visual change | snapshot tests or `ShadcnReferenceComparisonTest` | Awake did not drift from its accepted output. This is not proof of fidelity. |
| Maintain upstream shadcn inputs | fetch, extract, and capture tools below | The pinned source, tokens, and captures are refreshed. |
| Diagnose rendering assets | font or icon fidelity tools below | Awake is compared with the original asset rendered externally. |

Live preview serving is optional convenience tooling. It is never a verification gate.

Read `docs/reference/ui-validation.md` for the required proof for a UI change, and
`docs/reference/ui-testing-dictionary.md` for plain-English test terms.

The staged cleanup and retirement criteria live in
[`docs/tasks/2026-08-16-ui-tooling-simplification.md`](../docs/tasks/2026-08-16-ui-tooling-simplification.md).

## Normal component workflow

For a component that already has registered fixtures:

```bash
# Render the official pinned reference in Chromium.
scripts/awake ui reference --component button --state rest --theme light

# Render Awake's matching fixture. Add --debug-layout when bounds need inspection.
scripts/awake ui preview --component button --state rest --theme light

# Crop Awake by semantic node and compare it with the reference. This never records a baseline.
scripts/awake ui validate --component button --theme light
```

The command fails for an unregistered state or pairing rather than guessing. Add the reference
case, Awake fixture, and manifest entry together when expanding support.

## Maintainer workflow

Run this only when changing the pinned shadcn source, its token extraction, or the full
reference-report pipeline:

```bash
tools/fetch_shadcn_reference.sh
python3 tools/extract_shadcn_tokens.py
./gradlew :samples:ui-showcase:desktopTest --tests "*ShadcnReferenceComparisonTest*"
python3 tools/generate_parity_report.py
python3 tools/generate_ui_status.py
```

The generated reports are status aids, not independent sources of truth. If they disagree with
the code or the tests, repair the generator before relying on the report.

## Asset generation

Both of these generate committed Kotlin source. Never hand-edit their output, and never
hand-author the data they produce — that is how the icon set ended up with every corner arc
flattened to line segments, and how the font atlas ended up with mismatched glyph metrics.

| Script | Generates | Notes |
|---|---|---|
| `svg_to_ui_image_vector.py` | `UiImageVector` glyph data (e.g. `HeroIcons.kt`) | Preserves curves as real cubic Beziers, converts SVG arcs exactly, keeps nested `evenodd` subpaths as holes. Rejects what the engine cannot render (strokes, transforms, crossing subpaths). Run `--self-test` after editing. See `skills/awake-ui-icons/SKILL.md`. |
| `:awake:ui:font-atlas-generator` (`generateFontAtlas` task, Kotlin/JVM, not a `tools/*.py` script) | `RobotoRegularUiFontData.kt` (packed glyph atlas + metrics) | Reads glyph metrics from the TTF's own outline geometry (`Font.createGlyphVector`) and rasterizes a separate antialiased atlas bitmap via `Graphics2D`. Glyph offsets and advances must stay in the same coordinate space — mixing cell-relative offsets with pen-relative advances produces uneven letter spacing. Replaced the former `generate_ui_font_atlas.py`, which derived metrics from the antialiased raster ink bbox and quantized them to 1/64 em. |

```bash
python3 tools/svg_to_ui_image_vector.py icon.svg --name chevronDown --dp 16 --source "Heroicons chevron-down (20/solid)"
python3 tools/svg_to_ui_image_vector.py --self-test
```

## Font fidelity

`capture_font_reference.py` renders each sample in `font_samples_manifest.json` with the exact
TTF baked into `RobotoRegularUiFontData`, using Chromium as the control, and saves it to
`docs/reference/font-previews/`. `FontBaselineFidelityTest` renders the same sample through
Awake's atlas pipeline and compares per-glyph ink baselines.

It exists because atlas metrics alone could not say whether a glyph sitting a pixel low was
faithful to the typeface or introduced by us. The control answered it: real Roboto puts every
glyph on one baseline at 12, 14 and 16px, while our atlas splits round and flat glyphs by a
pixel at 12 and 14. That drift is recorded in the test's `knownBaselineDrift` map, so it cannot
widen unnoticed — the goal is an empty map.

```bash
python3 tools/capture_font_reference.py
./gradlew :awake:ui:headless:desktopTest --tests "*FontBaselineFidelityTest*"
```

## Shadcn reference implementation details

The chain that answers "does this actually look like shadcn?" rather than "did this change?".
Read `docs/reference/shadcn-reference-pipeline.md` first.

| Script | Purpose |
|---|---|
| `fetch_shadcn_reference.sh` | Clones `shadcn-ui/ui` at a pinned SHA into `third_party/` (gitignored). Everything below depends on it. |
| `extract_shadcn_tokens.py` | Parses the pinned registry's new-york/neutral theme into `ShadcnReferenceTokens.kt`, the numeric ground truth for token tests. |
| `capture_shadcn_local.py` | Builds and serves `shadcn-reference-app/`, then screenshots each case from `shadcn_reference_cases.json` into `docs/reference/shadcn-previews-local/`. Components come verbatim from the pinned checkout, so the reference is shadcn's own source. Captures states a docs page cannot show (focus, disabled, hover, open overlays) and any theme or radius. A case may name its own `selector` when Radix portals its content outside `#case`. |
| `compare_parity.py` | Diffs an Awake render against a reference capture: aligned crop, heatmap, mismatch metrics. Pairing lives in `shadcn_parity_pairs.json`. |
| `compare_component_crops.py` | Resolves an Awake semantic node ID to a raster crop, compares that crop with a component-hugging shadcn reference PNG, and writes the crop, heatmap, and JSON metrics. Supports a batch manifest. |
| `generate_parity_report.py` | Regenerates `docs/reference/shadcn-parity.md` from the component inventory, token test state, and comparison metrics. |
| `generate_ui_status.py` | Regenerates `docs/reference/ui-fidelity-status.md`, the per-area status matrix. Each row's status comes from a probe against the source, so it cannot claim done for unwired work. |
| `shadcn_parity_baseline.json` | Committed regression baseline consumed by `ShadcnReferenceComparisonTest.kt` (not a script) -- each pair's last-accepted mismatch%, an absolute-percentage-point tolerance, and an `excluded` map for pairs whose crop alignment can't be trusted yet. See `docs/reference/ui-validation.md`'s "Shadcn Parity Regression Gate" section. |

```bash
tools/fetch_shadcn_reference.sh
./gradlew :samples:ui-showcase:desktopTest --tests "*ShadcnReferenceComparisonTest*"
python3 tools/generate_parity_report.py
python3 tools/generate_ui_status.py
```

### Semantic component crops

`capture_shadcn_local.py` already crops the reference side: each case is rendered in
`tools/shadcn-reference-app` inside a `w-fit` `#case` wrapper (or an explicit portal selector)
and Playwright screenshots that target. `compare_component_crops.py` supplies the missing
Awake-side crop. It reads the semantic JSON emitted beside an Awake preview PNG, converts the
node's logical `UiBounds` to the preview's raster scale, and compares the resulting crop without
manual image editing.

Use the same state/content on both sides; for a grouped case, repeat `--node-id` to union the
semantic bounds before diffing:

```bash
python3 tools/compare_component_crops.py \
  --awake-png samples/ui-showcase/build/ui-previews/<preview-id>.png \
  --semantic-json samples/ui-showcase/build/ui-previews/<preview-id>.json \
  --node-id <component-node-id> \
  --reference-png docs/reference/shadcn-previews-local/<matching-case>_light.png \
  --name <case-name> \
  --padding 4 \
  --out-dir build/reports/ui-component-parity
```

For reviewed pairings, use `tools/ui_component_parity_cases.json`; copy
`tools/ui_component_parity_cases.example.json` when adding a new case. Run either manifest with
`--manifest`. The tool never updates a baseline. A case without `maxMismatchPct` is reported as
`REVIEW`, not pass/fail. Use
`--fail-on-mismatch` only after reviewing the generated crop and heatmap and choosing an
explicit per-case `maxMismatchPct`; a before/after crop diff proves drift, while the pinned
shadcn reference is the correctness comparison.

Comparison metrics are only as good as the alignment between the two captures. The generated
report carries a `crop` column for exactly this reason — a `poor` row means the framing
differs too much to conclude anything, not that the component is wrong.

`ShadcnReferenceComparisonTest` fails the build on regression (a pair's mismatch% drifting worse
than its `tools/shadcn_parity_baseline.json` entry by more than the baseline's tolerance), not on
absolute distance from shadcn/ui -- that distance is real and stays untargeted. Re-record the
baseline the same way as any other golden here, `-DAWAKE_RECORD_SNAPSHOTS=true`, only after
reading the diff PNG under `build/reports/shadcn-parity/` and confirming the drift is intended
(see `skills/awake-ui-verification/SKILL.md`).

### `awake ui` command line

`scripts/awake` is the discoverable command-line front door for this pipeline. It does not own
a second renderer or invent reference images: it resolves a component fixture from
`tools/shadcn_reference_cases.json` and `tools/ui_component_parity_cases.json`, then dispatches
to the existing official capture, Awake preview, and semantic-crop comparison tools.

Use it directly from the repository, or add `scripts/` to `PATH` to use `awake` without a path:

```bash
export PATH="$PWD/scripts:$PATH"

# Pinned upstream source rendered in Chromium.
awake ui reference --component button --state rest --theme light

# Existing Awake fixture. This writes the preview PNG and design-report JSON.
awake ui preview --component button --state rest --theme light

# Same Awake preview plus the semantic layout overlay:
# blue = node bounds, green = content bounds, red = clip bounds.
awake ui preview --component button --state rest --theme light --debug-layout

# Writes an Awake crop, heatmap, and JSON metric; it never updates a baseline.
awake ui validate --component button --theme light
```

The current Kotlin preview registry contains fixed, committed fixtures. The CLI rejects a state,
variant, style, base color, or accent that does not have a matching fixture rather than silently
rendering a different state. Add the reference-app case, Awake preview entry, and parity-manifest
row together before expanding the command's supported combinations.

## Icon fidelity

Proves each shipped `HeroIcons` `UiImageVector` renders the same shape as the official
Heroicons SVG it was generated from, automatically -- see `skills/awake-ui-icons/SKILL.md`.

| Script | Purpose |
|---|---|
| `capture_heroicons_reference.py` | Downloads each icon's official SVG and rasterizes it to a fixed 128x128 reference PNG (white fill on black, playwright/chromium) under `docs/reference/icon-previews/`. |
| `heroicons_manifest.json` | The (name, tier, Kotlin symbol) list both this script and `IconFidelityTest` read -- one source of truth, keyed by (name, tier) since Heroicons ships different path data per tier for the same name (e.g. `square-3-stack-3d` in both 20/solid and 24/solid). |

```bash
python3 tools/capture_heroicons_reference.py                    # re-capture every reference
python3 tools/capture_heroicons_reference.py --only chevron-down,camera
./gradlew :awake:ui:headless:desktopTest --tests "*IconFidelityTest*"
```

`IconFidelityTest` (`awake/ui/headless/src/desktopTest/`) renders each icon through
the real CPU rasterizer at the same 128x128 size and compares coverage-mask IoU against the
reference (threshold and measured correct-vs-corrupted separation are documented on
`passThreshold` in the test). It writes a reference/ours/diff PNG per icon to
`build/reports/icon-fidelity/` and a `metrics.tsv` — always regenerate references after adding
or regenerating an icon in `HeroIcons.kt`, and add the new entry to `heroicons_manifest.json`.

Catches gross shape errors (wrong path data, a rotated/mirrored derivation, a flattened curve).
Does not catch sub-pixel drift -- both pipelines' antialiasing differs enough that IoU alone
can't distinguish a 1px edge nudge from noise; that is a known ceiling, not a gap this guard
silently papers over.

## Optional live preview

`ui_preview_server.py` and `ui_preview_watch.sh` serve rendered preview PNGs for manual
review. They are a convenience for eyeballing, not a verification gate — see
`docs/reference/ui-validation.md` for what actually counts as proof.

## Other

`jni-binding-generator/` generates JNI bindings for the native backends and has its own
documentation.

## Detekt baselines

Every module's `detekt-baseline.xml` was regenerated on 2026-08-09 after the parallel
component wave (Resizable, Sheet, ScrollArea, Empty, Combobox, Table, studio camera mode)
and the accordion/slider/checkbox fixes. Everything absorbed is structural — MagicNumber on
graphics/geometry constants, LongMethod, LongParameterList, CyclomaticComplexMethod — and was
reviewed before absorbing: findings that pointed at real defects (dead code, unused
parameters, an unapplied modifier) were fixed in source, not baselined. detekt's baseline
parser rejects XML comments, so this record lives here rather than in the files themselves.

A new entry appearing in a baseline diff means new structural debt: prefer fixing it or a
commented `@Suppress` at the site; `./gradlew detektBaseline` is the deliberate second choice.
