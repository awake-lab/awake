# MTSDF atlas migration

Status: **ready to implement**. Approach validated, tooling installed, no open questions.

## Why

Every remaining text-quality defect traces to the `CoverageAlpha` atlas, not to metrics or
rounding. Both of those were fixed and neither closed the drift:

- `4beb60b3` gave `PackedUiFont` real vertical metrics off the atlas.
- `dd0e5504` replaced raster-bbox metrics (quantized to 1/64 em) with TTF outline geometry, and
  changed `BasicText` to snap a line's pen origin once with exact per-glyph sizes.

Measured after both: `FontBaselineFidelityTest`'s drift map still holds **three** entries, the
same count as before either fix — only the membership moved (`roundvsflat-12` closed,
`roundvsflat-16` opened). Straight stems (`i`, `l`, `I`) now measure exactly 1px at 12/14/16px,
but `t` still varies (1–2 at 12px, 2–3 at 16px).

That residue is sub-pixel **phase**, not scale. Glyphs land at different fractional pen
positions as advances accumulate, so a stem straddling two texels resolves to 2px in one
instance and 3px in another. A coverage atlas cannot fix this: snapping each glyph to kill
phase variance reintroduces the scale variance that caused the inconsistent stroke weight.

A distance field resolves the edge analytically per pixel, so phase stops changing apparent
weight. That is the fix.

## What already exists (do not rebuild)

- `ui_glyph.frag` (Vulkan) and `ui_glyph.wgsl` (WebGPU) already implement MSDF correctly —
  `median3(atlas.rgb)` with `screenPxRange` from `fwidth`, the canonical Chlumsky formula, plus
  stem darkening for gamma-space blending.
- Both `UiGlyphRenderPipeline`s already pass `isDistanceField` and `distanceFieldRangePx`.
- `UiFonts.msdf()` and `MsdfFont` exist in `ui-core`. **`UiFonts.msdf()` has zero callers** —
  the whole MSDF path is shipped, wired, and dead.

So the shader work is done. The missing piece is only the atlas data.

## Why MTSDF rather than MSDF

The atlas is already RGBA8 — `PackedUiFont.decodeAtlasPixels` builds `ByteArray(alpha.size * 4)`
and forces RGB to white. MSDF would use 3 channels and waste the 4th; MTSDF fills it with a true
signed distance at zero extra memory. That true distance is what enables outline, glow, drop
shadow, and dilate/erode (faux bold) later — `median3` is not a real distance and cannot drive
them. Same format-widening work either way, so take the superset.

## Tooling

`msdfgen 1.13` installed via Homebrew (`/opt/homebrew/bin/msdfgen`). Chosen over a hand-written
Kotlin implementation deliberately: the generated atlas is a **committed Kotlin source file**, so
day-to-day builds and CI never invoke the generator — only a deliberate regeneration needs the
binary, which happens rarely. Writing bezier distance + edge coloring by hand to avoid a
dependency on a file we commit anyway is the wrong trade, especially since subtly wrong edge
coloring produces exactly the hard-to-see rendering bugs that have already cost days here.

### Validated command

```bash
msdfgen mtsdf -font <ttf> <unicode> -emnormalize -size 32 32 -pxrange 4 -autoframe -printmetrics -o out.png
```

`-emnormalize` is required. Without it msdfgen emits legacy font units and warns that the
implicit scaling will change in a future version. With it, coordinates come out normalized to
1 em — the same space `PackedUiFontData` already uses.

Verified output for `A`:

```
bounds = 0.02099609375, 0, 0.61083984375, 0.7109375
advance = 0.63232421875
PNG 32 x 32 bitdepth 8 colortype 6   (6 = RGBA)
```

`0.7109375` matches the `capHeightEm = 0.710938` that the outline generator measured
independently. Two implementations agreeing to six decimals confirms the em convention lines up.

## Remaining work

1. **Per-glyph generation and packing** in
   `awake/engine/ui/font-atlas-generator/.../Main.kt` (289 lines today; it currently draws into a
   `TYPE_BYTE_GRAY` image and base64-encodes via `grayBytes`).
   Prefer passing explicit `-scale`/`-translate` derived from our own metrics over `-autoframe`,
   so every glyph shares one em→px mapping and the existing UV-rect derivation is unchanged. With
   `-autoframe` you must map back through the reported `scale`/`translate` per glyph.
2. **Widen the data format** — this is the bulk of the task. `encodedAlphaBase64` is
   single-channel by construction; it must carry RGBA. Touches the `PackedUiFontData` interface,
   `PackedUiFont.decodeAtlasPixels`, and the generator's emission. `BitmapFont` stays
   `CoverageAlpha`, so both paths must remain decodable.
3. **Flip the default** — `samplingMode = DistanceField`, set `distanceFieldRangePx` from the
   `-pxrange` used, point `UiFonts.default()` at the new data. Retire the coverage Roboto rather
   than maintaining two packed paths. No shader or backend changes.
4. **Re-baseline** `UiSnapshotSignatureTest` (run with `--console=plain -i`, grep
   `ui-snapshot-signature`, add a dated provenance comment matching the existing style).

## Pass/fail signal

Not "it compiles". Two measurements:

- `FontBaselineFidelityTest`'s `knownBaselineDrift` should shrink toward empty. Record what is
  actually measured; do not force-close it.
- The `t` stem should stop varying across sizes. Probe: render a repeated glyph at 12/14/16px,
  measure rendered stem width per instance, assert min == max. This is what caught the defect
  originally, and no existing gate covers it.

## Related follow-ups

- **Upgrade the font gate.** `docs/reference/font-previews/` holds Chromium-rendered Roboto PNGs
  and the test extracts a single integer (ink-bottom spread) from them. That is why inconsistent
  stroke weight and blur passed every check and were found by eye. Per-pixel comparison against
  those same references would have caught all three of this session's defects. Worth doing
  *before* MTSDF, so its bugs do not have to be found by looking at screenshots too.
- **`.claude/worktrees/agent-*`** holds stale full checkouts from earlier agent runs. Safe to
  delete; they are large and shadow real files in `find` results.
