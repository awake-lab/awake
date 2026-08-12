---
name: awake-ui-verification
description: How to prove a UI change is correct in Awake - which tool answers which question, when a baseline may be re-recorded, and why a passing golden is not evidence of fidelity. Read before re-recording any snapshot/golden, before citing a parity number, and before claiming a visual change works. Trigger keywords - snapshot, golden, baseline, re-record, AWAKE_RECORD_SNAPSHOTS, parity, fidelity, pixel diff, mismatch, signature, drift, visual regression, shadcn reference, verify UI.
---

# Verifying UI in Awake

Policy for *what* must be proven lives in `docs/reference/ui-validation.md`. This skill is the
*how*: choosing the right tool, and the judgment calls that tooling cannot make for you.

## The one distinction everything depends on

**"Did this change?" and "Is this right?" are different questions, answered by different
tools. Never let one stand in for the other.**

| Question | Tool | What a pass means |
|---|---|---|
| Did this change? | Snapshot goldens (`snapshots/ui/*.png`), signature maps | Output matches what Awake produced *before*. Says nothing about correctness. |
| Is this right? | `ShadcnReferenceComparisonTest` vs `docs/reference/shadcn-previews/` | Output resembles a real shadcn screenshot. |
| Is this value right? | `ShadcnReferenceTokenExpandedTest` vs generated `ShadcnReferenceTokens.kt` | A token equals the pinned reference exactly. |
| Does the logic hold? | Unit tests, throwaway probes reading real `UiBounds`/pixels | The measured number is what you claim. |

This repo shipped the confusion twice. `shadcn-parity.md` described itself as machine-readable
ground truth while sourcing from `shadcn-compose`, a third-party port. The reference PNGs it
cited came from the same port — one rendered the text "Vega", a shadcn-compose preset name that
appears nowhere in real shadcn. Everything "verified against shadcn" was Awake compared to a
lookalike.

**Check provenance before trusting any reference.** If you cannot name where an artifact came
from and how to regenerate it, it is not ground truth.

## Re-recording a baseline

`-DAWAKE_RECORD_SNAPSHOTS=true` overwrites goldens. It is the single easiest way to convert a
real regression into a permanently green test.

Required sequence, no exceptions:

1. Run without recording. Let it fail.
2. **Open the diff PNG** (`build/ui-previews/<id>_diff.png`) and look at it.
3. Explain the drift in terms of the change you made. A uniform content shift means padding or
   size moved. Corner-only pixels mean radius moved. Ghost-doubled text means glyphs moved. If
   you cannot explain it, you have found a bug, not a baseline to refresh.
4. Only then record, and say why in the commit message or a dated comment next to the constants.

When several visual changes land together (this is normal in a multi-agent pass), do **one**
re-record at the end. Recording per-change bakes each intermediate state into the goldens and
destroys the ability to attribute a later regression.

Two independent mechanisms exist and both must be updated:

- **PNG goldens** — refreshed by the record flag.
- **Signature constant maps** (`UiSnapshotSignatureTest`, `UiShowcaseLayoutSignatureTest`) —
  hand-edited hex, never touched by the record flag. The failure message prints the replacement
  matrix.

Beware the failure message's shape: `assertSnapshotSignatures` throws on the *first* mismatch,
so it names one scene while many have drifted. `UiShowcaseLayoutSignatureTest` does the
opposite — it always prints the complete matrix, most of which is unchanged. Diff the printed
values against the recorded ones rather than trusting the headline.

## Reading a parity number

`ShadcnReferenceComparisonTest` writes `build/reports/shadcn-parity-metrics.json`. Each entry
carries `awakeSize`, `referenceSize` and `comparedSize`.

**`comparedSize` gates whether `mismatchPct` means anything.** The two images are framed
differently, so the harness compares their aligned intersection. When that intersection is a
sliver — a slider comparison collapsing to 300x12, a dialog comparing 320x150 of a 1280x800
capture — the percentage measures framing, not fidelity. `generate_parity_report.py` marks
these `poor` and they must be read as **unmeasured**, not as failures.

Demonstrated: the glyph-advance fix produced a large, plainly visible improvement in text
quality and moved these numbers by fractions of a percent, one of them upward. A harness
pointed at misaligned inputs cannot see a real fix. Do not use mismatch% to decide whether a
change helped until its row reads `good`.

## Proving a visual change actually renders

Reasoning from source about spacing, centering or smoothness is unreliable. Build the real
thing and read real output:

- **Numbers** — drive a real `UiContext`, call the real widget, assert on the actual `UiBounds`
  or mesh it produces. See `RowCrossAxisCenterProbeTest`, `UiPathFillTessellationTest`.
- **Pixels** — rasterize and write a PNG you open and look at:

```kotlin
val ui = UiContext()
ui.beginFrame(w.toFloat(), h.toFloat(), testSnapshot(), deltaSeconds = 1f / 60f)
ui.createAbsolute(x = 0f, y = 0f).yourWidget(...)
val pixels = ui.endFrame().rasterize(w, h, background = Color.Black)
// convert to BufferedImage, ImageIO.write to a path you then inspect
```

- **Through a real backend** — `UiAnimationFrameCapture` / `RendererHeadlessPixelBaselineTest`
  when the question involves the GPU path rather than the CPU rasterizer.

Delete the probe once it has answered the question; keep it only if it locks something worth
locking. Both defects found this session were caught this way and were invisible to every
existing test: a centroid fan overfilling concave glyphs, and a CPU-rasterizer bbox truncation
that dropped each triangle's last pixel row. The snapshot suite stayed green through both,
because the baselines were recorded from the same broken code.

**A test suite that renders through the defect it is meant to catch will never catch it.** When
a rendering primitive changes, verify with your eyes at least once before trusting any golden.

## What none of this covers

State it rather than implying coverage:

- Only the default theme (`Vega`, `Neutral`) and light mode have reference captures. Seven
  presets and all of dark mode are unverified against shadcn.
- Behavior — keyboard navigation, focus management, dismiss layers — has no parity coverage.
  These are still-image comparisons.
- Real-GPU output is only spot-checked; most suites run the CPU rasterizer in `ui-testing`,
  which is a separate implementation from the Vulkan/WebGPU pipelines.

## Commands

```bash
tools/fetch_shadcn_reference.sh                      # pin the reference (run first)
./gradlew :awake:ui:ui-core:desktopTest
./gradlew :awake:ui:headless:desktopTest
./gradlew :samples:ui-showcase:desktopTest
python3 tools/generate_parity_report.py              # after the comparison test
```

See `tools/README.md` for the generators and the full parity chain.

### CLI shortcut

Use `scripts/awake ui` (or add `scripts/` to `PATH` and use `awake ui`) when iterating on a
registered component fixture. It is a dispatcher over the same source-of-truth manifests, not a
new renderer:

```bash
awake ui reference --component checkbox --state rest --theme light
awake ui preview --component checkbox --state rest --theme light --debug-layout
awake ui validate --component checkbox --theme light
```

The command rejects states and visual configuration that lack a paired official reference and
Awake preview. Do not interpret a generated Awake-to-Awake golden as parity, and do not use any
record flag before reviewing the official crop heatmap.

## Component-level cropping

When a showcase page contains several widgets, do not manually crop before/after screenshots.
The shadcn reference side is already component-cropped by Playwright through
`tools/capture_shadcn_local.py`. For the Awake side, use
`tools/compare_component_crops.py`: it resolves a semantic node ID from the generated preview
JSON, applies the preview raster scale and optional logical padding, writes the crop and a
heatmap, and records JSON metrics. Use `tools/ui_component_parity_cases.json` for reviewed
pairings or start from `tools/ui_component_parity_cases.example.json` for new coverage. Cases
without a threshold are reported as `REVIEW`; this tool does not update baselines. Review the
crop and diff before adding a threshold or enabling `--fail-on-mismatch`.
