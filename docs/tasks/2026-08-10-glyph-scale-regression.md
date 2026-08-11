# Glyphs render ~0.6x their own metrics -- RETRACTED; real ~0.9x residual FIXED

Status: **the 0.6x report was a measurement artifact; behind it sat a real, smaller defect,
now fixed (2026-08-11).**

Every number in the original investigation came from probes that called
`rasterize(width, height, background = ...)` WITHOUT passing `font =`. With no font the
rasterizer draws a null-font placeholder rect per glyph, so the probes measured placeholder
geometry, not glyph sampling. The placeholder is `fillRect` inset by `min(w, h) * 0.25`, which
reproduces every original number exactly: 3x7 ink for H at 14px, the "asymmetric" 0.40/0.70
ratios (width loses 50% of a narrow quad, height only 38%), the 5/7/7 height table, and
"stems 1px, bowls 3px" (every glyph's run is half its quad width). The Chromium comparison
drawn from those probes is equally invalid.

## The real defect behind it (found by the honest re-measurement, fixed 2026-08-11)

With the font passed, ink measured ~0.90x of the quad horizontally and ~0.92x vertically at
EVERY size -- sub-pixel at 12-14px, past a pixel from 16px up:

| size | quad h | ink rows (thr=1) | ratio |
|---|---|---|---|
| 14px | 9.95 | 9 | 0.90 |
| 16px | 11.38 | 11 | 0.97 |
| 20px | 14.22 | 13 | 0.91 |

Mechanism: the generator sized the render quad (`quadMetricsEm`) to the glyph's OUTLINE rect,
but the UV rect (`sampleRect`) to outline + `CROP_BLEED` + an outward integer-texel snap. The
padded atlas region was squeezed into an outline-sized quad, shrinking ink by
`outline / (outline + bleed + snap)` per glyph -- for H, 18/20 wide and 24/26 tall, matching
the measured ratios exactly. The per-glyph snap slack also scattered baselines by a subpixel,
which is where the baseline-fidelity drift came from. This is the "quad-vs-UV padding
mismatch" the original doc listed as disproved: the mechanism was real all along -- it was
only disproved as the cause of the 0.6x number, and its predicted ~0.92 is what the honest
measurement found.

Fix: the generator now derives `quadMetricsEm` from the snapped sample rect itself (quad and
UV describe the same texels 1:1), and ships outline-true `inkMetricsEm` separately so metrics
(`capHeightEm`, baseline, visible band, advance clamping) stay ink-exact -- the earlier failed
attempt inflated `capHeightEm` precisely because quad metrics and ink metrics were one array.
After the fix, H's ink at half-coverage threshold lands within 0.05-0.78px of
`capHeightEm * size` at 12/14/16/18/20px, and `GlyphAbsoluteSizeTest` gates it.

## What is worth keeping from this

- `rasterize()` silently substitutes a placeholder when `font` is null. It should either require
  the font for frames containing glyphs, or make the placeholder obviously not a glyph, because
  it cost a full investigation and produced a confident, wrong "top open issue".
- `GlyphStemWeightTest` had the same omission and was therefore asserting uniform placeholder
  widths. It now passes the font.
- The absolute-size check proposed here is still worth adding, precisely because it would have
  caught the bad probe: `H` at 14px must render `capHeightEm * 14 = 9.95px` of ink.

## Original investigation

Retained below only as a record of what was eliminated while chasing a measurement artifact.
None of it should be treated as an open lead.

## Why no gate caught it

`FontBaselineFidelityTest` measures baseline SPREAD, which is scale-invariant -- both renders put
every glyph flush on one baseline, ours just smaller. `GlyphStemWeightTest` asserts one glyph
renders at a CONSISTENT weight, not a correct one. Snapshot signatures only assert pixels match
what was last recorded. Nothing asserted absolute size against an external truth.

## LOCALIZED (2026-08-10, later)

One measurement splits it. At 14px, for `H`:

    metrics:  widthEm=0.538086  heightEm=0.710938
    emitted:  quad 7.53 x 9.95   <- exactly heightEm * 14, CORRECT
    rendered: ink  3 x 7         <- 0.40 of quad width, 0.70 of quad height

So `BasicText` emits the right quad and the packed metrics are right. The INK inside the quad is
wrong, and asymmetrically so. That rules out metrics, resolveGlyphPx, density, quad math and font
weight -- all of which were investigated and are correct.

Everything upstream of sampling is VERIFIED CORRECT, by measurement rather than reasoning:

- packed metrics -- `capHeightEm` 0.710938 matches Roboto's published value
- `BasicText` quad emission -- 7.53 x 9.95 at 14px, exactly `heightEm * size`
- atlas glyph placement -- dumped the atlas and looked: `H` sits inside its UV rect and fills it,
  with the expected ~1px bleed. An msdfgen `-translate`/`-scale` mistake was suspected here and
  is DISPROVED.
- the UV rect itself -- 20x26 texels, matching the ink
- the rasterizer's UV mapping -- `u` spans 0..1 across the quad and maps to `u0..u1` correctly

So every input is right and the output is wrong. The defect is in the sampling/coverage
resolution between them.

Note `screenPxRange` evaluates to `max(distanceFieldRangePx * (glyph.w / texelWidth), 1.0)` =
`max(2 * 7.53/20, 1.0)` = `max(0.75, 1.0)` = **1.0** at every real UI size, in both the rasterizer
and the shaders. The clamp makes the edge-sharpening term inert, so alpha collapses to the raw
signed distance. Whether that alone explains a 0.4x-wide glyph is NOT yet established -- it is
where to look first, not a conclusion.

Next step, and keep it to one thing: instrument `sampleGlyphAlpha` for a single `H` at 14px and
log `u`, `v`, `signedDistance`, `screenPxRange` and the resulting alpha across the quad. That
shows directly where the coverage ramp lands relative to where the ink should be. Do not change
code before that log exists -- four hypotheses have now been disproved by measurement after
looking plausible on paper.

## Disproved suspects (kept so they are not re-tried)

**Quad-vs-UV padding mismatch.** Tried: deriving the quad from the sampled rect instead of the
ink bbox. It did NOT fix the scale (ink stayed 7px at 14px) and it CORRUPTED the metrics --
`capHeightEm` is derived from the glyph rects, so padding them inflated the metric itself from
9.95 to 11.375 expected, meaning the bug would begin hiding itself. Reverted. The original
reasoning follows.

The quad-vs-UV mismatch that MTSDF made significant. The glyph quad is sized to the INK
(`widthEm * glyphPx` in `BasicText`), while `uvBoundsPx` (see `sampleRect` in the generator)
covers the ink PLUS `CROP_BLEED` padding. Stretching a padded atlas region into an ink-sized quad
renders the ink smaller than the quad by roughly `ink / (ink + 2 * bleed)`.

Note that alone does not account for the full gap: at `OVERSAMPLE = 2` an em is 32 texels, so
`H`'s 0.710938em is ~22.75 texels, and 22.75/24.75 is 0.92, not 0.6. So either `CROP_BLEED` is
being applied more than once, or the em-to-texel relationship is off as well -- `OVERSAMPLE`
changed 4 -> 2 during the MTSDF work (`e65c9eed`), so that relationship should be re-derived from
scratch rather than trusted.

Worth checking first, cheapest to most involved:

1. Does the ratio change with `OVERSAMPLE`? Regenerate at 4 and re-measure `H`. If the ratio
   moves, the em-to-texel mapping is the bug.
2. Does it change with `CROP_BLEED` 1 -> 0? That isolates the padding contribution.
3. Compare `uvBoundsPx` for `H` against `quadMetricsEm` for `H` by hand: the UV rect's
   texel size divided by 32 should equal `widthEm`/`heightEm` plus exactly the bleed.

## The check to add with the fix

An absolute-size assertion, which is the gate this repo has never had:

    H at 14px must render capHeightEm * 14 = 9.95px of ink, within a pixel.

Cheap, external-truth-based, and it fails today. Add it alongside `GlyphStemWeightTest`.

## Do NOT swap the font first

Shipping modern Roboto (2014+) or Inter is easy and was considered, but doing it before this is
fixed would change the control while the bug is live: slightly heavier text, still 0.6x too small,
and the Chromium reference no longer comparable. Fix scale, verify against the reference, then
decide on the font as a separate question.
