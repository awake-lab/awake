# Glyphs render ~0.6x their own metrics

Status: **measured and localized, not fixed**. This is the cause of the long-running "text is
thin" report, and it is not a font-weight problem.

## Evidence

Rendered `H` through `UiRasterizer` (which now matches the real shaders, `5e7c063c`) and compared
its ink against what the font's own metrics predict:

| size | rendered ink height | expected (`capHeightEm x size`) | ratio |
|---|---|---|---|
| 12px | 5px | 8.53px | 0.59 |
| 14px | 7px | 9.95px | 0.70 |
| 16px | 7px | 11.38px | 0.62 |

An `H` at 14px also measures only 3px wide; it should be ~9px.

Against the Chromium ground truth in `docs/reference/font-previews/` (`iliaeco`, same TTF, same
canvas, `device_scale_factor=1` -- confirmed in `tools/capture_font_reference.py`, so there is NO
DPR difference):

| size | ours | Chromium |
|---|---|---|
| 14px | stems 1px, bowls 3px | stems 2px, bowls 6px |
| 16px | stems 1px, bowls 3-4px | stems 2px, bowls 6-7px |

Same ~0.6 factor. Half-size text reads as thin, spindly and washed out, which is exactly the
reported symptom.

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
