# Cross-frame text-layout measurement caching (2026-08-03)

Design/scoping only -- no fix implemented in this task. Follow-on from the real WebGPU web-lag
investigation (commit `3f51404a`, which fixed two actual bugs: per-element buffer-conversion
overhead and font-atlas-pipeline thrashing). After both were fixed, a fresh Chrome DevTools trace
on `samples/ui-showcase` showed the frame-time top no longer dominated by interop/upload junk, but
by real Awake text-layout code: `PackedUiFont.advanceFor` (21.4% self / 24.1% total),
`measureLineWidth` (16.9% total), `renderTextBlock$emitLinesInternal` (8.5% total),
`measureVisibleLineBandEm`, `measureTextWidth`. This doc scopes whether/how to cache that work
across frames.

## Problem restated

`renderTextBlock` (`BasicText.kt:33`) is called once per text widget per frame it's drawn. For
every call it re-runs, from scratch, on every frame regardless of whether anything changed:

1. `layoutBitmapText` (`BasicText.kt:277`) -- word-wraps/truncates `label` into lines, walking
   every character via `advanceOf` (`font.advanceFor`) to measure fit.
2. `measureTextBlock` (`BasicText.kt:234`) -- walks every line's every character again via
   `measureVisibleLineBandEm` to compute the block's vertical extent.
3. `emitLinesInternal` (`BasicText.kt:83`) -- walks every character a *third* time to actually
   emit glyph quads (this pass is unavoidable, it's the actual draw and must run every frame the
   text is visible -- not a caching target).

Steps 1 and 2 are pure computation over `(label, glyphPx, maxWidthPx, wrap, overflow, maxLines,
font)` and produce the same `UiBitmapTextLayout`/block-metrics every time those inputs are
unchanged. For static catalog content (headings, body copy, code samples -- most of
`samples/ui-showcase`'s Introduction/Typography/pattern pages), those inputs never change
frame-to-frame, so steps 1-2 are pure waste after the first frame.

## Why this is a fundamentally easier/safer cache than the row/column weight cache

`docs/tasks/2026-08-02-trial-measure-cross-frame-cache.md` ruled out automatic caching for
`row()`/`column()` because the thing that determines the cached answer is the **opaque `content`
lambda's body** -- not enumerable without running it (no compiler-tracked call-site identity, no
read-tracking, closures aren't introspectable in common code).

Text layout does not have that problem. `layoutBitmapText`'s and `measureTextBlock`'s entire
behavior is a **pure function of their explicit, typed, value parameters** -- `label: String`,
`glyphPx: Float`, `maxWidthPx: Float`, `wrap: UiTextWrap`, `overflow: UiTextOverflow`,
`maxLines: Int`, and `font: UiFont` (now a *stable, memoized* reference per `UiFonts.default()`,
per commit `3f51404a` -- previously two different `UiFont` instances existed for "the same" font,
which would have made this cache's key partially unreliable too; that's now fixed and this design
depends on it staying fixed). There is no closure, no hidden state read, no `rememberStateValue`
call anywhere in either function -- confirmed by reading both in full (`BasicText.kt:234-275`,
`BasicText.kt:277-351`). **This can be a fully automatic, non-opt-in cache**, unlike the row/column
design. That is the key structural difference and the reason this is worth a separate, much
smaller design rather than folding into the existing opt-in mechanism.

## Proposed shape

Add an internal memoization cache inside `ui-headless`'s text module, transparent to every call
site (no API change, no new parameters on `renderTextBlock`/`Text`/`shadcnLabel`/etc.):

```kotlin
private data class TextLayoutCacheKey(
    val label: String,
    val glyphPx: Float,
    val maxWidthPx: Float,
    val wrap: UiTextWrap,
    val overflow: UiTextOverflow,
    val maxLines: Int,
    val font: UiFont // reference-stable per commit 3f51404a; data class equals() falls back to
                     // referential equality unless UiFont/PackedUiFont overrides equals(), which
                     // it does not (see "Correctness risk" below)
)

private val layoutCache = LruCache<TextLayoutCacheKey, UiBitmapTextLayout>(maxSize = 256)
```

- `layoutBitmapText` becomes a thin wrapper: build the key, `layoutCache.getOrPut(key) { <existing
  body> }`. Zero behavior change on a cache miss (identical code path). On a cache hit, skips the
  entire wrap/truncate/measure walk.
- `measureTextBlock`'s result (`UiMeasuredTextBlock`) can be folded into the *same* cache entry
  (it's derived from the same inputs plus `lineGap`, itself derived from `glyphPx`) rather than a
  separate cache -- avoids a second lookup and keeps one cache to reason about, not two.
- `advanceFor`/`uvFor` themselves (already O(1) map lookups inside `PackedUiFont`) are **not**
  cache targets -- they're already cheap; their trace cost is call *volume* from the wrap/measure
  walk, which this cache eliminates at the source by skipping the walk entirely on a hit.

## Cache lifetime and location

- **Not** per-frame (zero benefit) and **not** per-`UiContext` state store (`rememberStateValue`'s
  store is keyed by caller-supplied `id`, which text widgets don't currently have/require -- adding
  that requirement would turn this into another opt-in migration, defeating the point). Instead: a
  **module-level, size-bounded LRU** living in `ui-headless`'s text file, scoped to process
  lifetime (survives across `UiContext` recreations, which is fine and desirable -- the cached
  value only depends on the pure inputs in the key, not on which `UiContext` asked for it).
- Bounded (LRU, not unbounded `HashMap`) because `label` is an open string -- a chat log, a search
  results page, or user-generated content could otherwise grow the cache unboundedly. A cap in the
  low hundreds covers "static catalog page" workloads (this investigation's actual motivating case)
  without meaningfully re-measuring on every keystroke of a text field (those keys naturally evict
  as `label` changes every frame anyway -- they were never going to hit this cache).

## Correctness risk, named plainly

The `TextLayoutCacheKey` data class's generated `equals()` calls `UiFont.equals()` on the `font`
field. `PackedUiFont` (and `UiFont` generally) does not override `equals()`/`hashCode()`, so this
falls back to `Any.equals()` (referential identity) -- which is exactly what's wanted here (two
`UiFont` instances with identical glyph data but different object identity should *not* silently
match, since nothing guarantees their `advanceFor`/`uvFor` outputs are actually identical). This is
safe **only because** `UiFonts.default()` now memoizes per `cellSize` (commit `3f51404a`) --
before that fix, `GameUiRuntime` and `SceneGameRuntime` each held a *different* `UiFont` instance
for "the default font," which would have made every cache lookup a guaranteed miss between the two
draw paths (not wrong, just useless -- a silent no-op cache, the worst kind of "looks like it
works" bug to debug later). Any future `UiFont` implementation that constructs a fresh instance per
call (a custom font provider, a hot-reloadable font, etc.) would silently defeat this cache the
same way -- worth a one-line doc comment on `UiFont`'s call sites, not a runtime guard (guarding
against it would require `UiFont` to have structural `equals()`, a bigger and unrelated design
question about font identity that's out of scope here).

Unlike the row/column weight cache, there is **no stale-key risk category** here: every input to
the cached computation is an explicit key field, not an opaque caller-supplied token standing in
for "trust me, this hasn't changed." If any key field changes, the key changes, and it's
automatically a fresh computation. This is the structural reason this design doesn't need a
debug-mode consistency check the way the row/column cache does.

## Verification of correctness

- New `TextLayoutCacheTest.kt` (`ui-headless/commonTest`, sibling to `RowColumnWeightCacheTest.kt`
  in spirit but a different risk shape per above):
  1. Cache-hit test: call `layoutBitmapText` twice with identical arguments, confirm the second
     call's result is `==` the first (trivial, since it's a pure function) **and** that a call
     counter/spy on the character-walk path shows it did not re-walk (the actual thing being
     verified -- correctness of the *value* was never in doubt for a pure function, correctness of
     the *skip* is).
  2. Cache-miss-on-each-key-field test: vary each field of `TextLayoutCacheKey` independently
     (label, glyphPx, maxWidthPx, wrap, overflow, maxLines, font instance) and confirm each
     produces a fresh computation, not a stale hit.
  3. Eviction test: fill the cache past `maxSize`, confirm oldest entries are evicted and a
     re-request for an evicted key re-computes correctly (not corrupted/stale).
- Existing text tests (`UiFontMeasureTextWidthTest.kt`, any `BasicText`/`shadcnLabel` snapshot
  tests) must stay green unchanged -- this design changes zero observable output, only skips
  redundant computation.

## Verification of the perf win

- Extend `UiShowcaseLayoutCostTest.kt` (already the home for `UiMeasureTrialStats`-style
  measurement per the row/column cache doc) with a text-specific fixture: render a static
  text-heavy page (e.g. the Introduction/Typography catalog pages that motivated this
  investigation) for two frames, assert frame 2's character-walk count (a new counter, mirroring
  `UiMeasureTrialStats`'s existing pattern) drops to ~0 for unchanged text, non-zero only for the
  glyph-emit pass (which must still run).
- Re-run the same Chrome DevTools trace workflow used in this investigation (or, once
  `chrome-devtools-mcp` is available next session, the automated
  `performance_start_trace`/`stop_trace` path) on `samples/ui-showcase` before/after, confirm
  `advanceFor`/`measureLineWidth`/`measureVisibleLineBandEm` self/total time drops materially for
  the catalog content pages, while `emitLinesInternal` (the draw pass, expected to be unaffected)
  stays roughly flat.

## Estimated win and honest limits

- **Static text** (headings, body copy, most catalog/docs content, disabled/read-only labels):
  full win -- steps 1-2 collapse to a single `HashMap` lookup after the first frame.
- **Dynamic text** (a text field's live input, a counter, a timestamp): **no win** -- `label`
  changes every relevant frame, so the key changes and it's a cache miss every time, correctly
  falling back to today's behavior with only the (cheap) key-construction/lookup overhead added.
  This is expected and acceptable -- dynamic text was never the source of the 21-24% cost profile
  motivating this doc; static catalog content was.
- Does **not** reduce `emitLinesInternal`'s per-character glyph-emit cost (unavoidable, it's the
  actual draw), so this will not fully eliminate text-related frame cost -- only the redundant
  wrap/measure work layered on top of it every frame for content that never changes.

## Status

- [x] Read `renderTextBlock`/`layoutBitmapText`/`measureTextBlock`/`measureVisibleLineBandEm`
      (`BasicText.kt`) in full; confirmed both cache-target functions are pure over explicit,
      typed parameters with no closures or hidden state reads.
- [x] Confirmed this can be a fully automatic (non-opt-in) cache, unlike the row/column weight
      cache, and named the structural reason why (explicit key vs. opaque lambda).
- [x] Named the one real correctness dependency: `UiFont` reference stability, satisfied today by
      commit `3f51404a`'s `UiFonts.default()` memoization fix, and flagged what would silently
      defeat it in the future.
- [x] Scoped cache shape (single LRU keyed by all layout-determining inputs, bounded size),
      lifetime (process-scoped, not per-`UiContext`/per-frame), and what's explicitly *not* a
      cache target (`advanceFor`/`uvFor` themselves, `emitLinesInternal`'s draw pass).
- [x] Named the verification plan for both correctness (`TextLayoutCacheTest.kt`) and perf
      (`UiShowcaseLayoutCostTest.kt` extension + before/after trace).
- [x] Gave an honest read: full win for static content (the actual motivating case), no win for
      dynamic text (expected, not a gap), and an explicit non-goal (glyph-emit cost).
- [ ] Not started: implementation of `TextLayoutCacheKey`, the LRU, or the wrapper around
      `layoutBitmapText` (deliberately out of scope for this design-only task).
- [ ] Not started: `TextLayoutCacheTest.kt` or the `UiShowcaseLayoutCostTest.kt` extension.

## Recommendation

Worth building. Lower risk than the row/column cache (no opt-in migration cost, no stale-key
hazard class, fully automatic) and targets exactly the cost category the post-fix trace surfaced.
Suggested next step if picked up: implement the cache + `TextLayoutCacheTest.kt` in one change
(mirrors this session's discipline of shipping the safety net with the mechanism, not after),
verify via `UiShowcaseLayoutCostTest.kt`, then re-trace `samples/ui-showcase` to confirm the real
win before calling it done.
