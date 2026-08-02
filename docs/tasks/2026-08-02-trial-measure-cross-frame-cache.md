# Cross-frame trial-measure caching for `row()`/`column()` (2026-08-02)

Design/scoping only -- no fix implemented in this task. Follows on directly from
`docs/tasks/2026-08-02-trial-measure-double-execution.md` (the "narrow fix," commit `55dd0681`),
which cut trial count from 3^depth to 2^depth but **only** for `Center`/`End`/`SpaceBetween`/
`SpaceEvenly`/`SpaceAround` arrangements, and measurably did nothing for `Arrangement.spacedBy`
(still 7,696 trial passes / ~101ms on the real Checkout Form page -- unchanged, confirmed below).
Read that doc, `UiContextMeasureState.kt`'s `createMeasureContext()` doc comment,
`WrapContentMeasurementStateTest.kt` (the regression test referred to as `RepoBugTest` in
`UiContextMeasureState.kt`'s comment -- that class name appears not to exist verbatim in the
current tree; this is the actual test guarding the constraint), `UiMeasureTrialStats.kt`, and
`TrialMeasureScalingTest.kt` before touching anything here.

## Problem restated

`UiScope.row()`/`column()` (`Row.kt:238-245`/`Column.kt` equivalent) runs one throwaway trial
execution of `content` per node, purely to answer "does any direct child call `.weight()`"
(`hasWeightedChild`), unless `effectiveArrangement.requiresMeasuredDistribution()` is already true
(the landed fix) or a `precomputedMeasured` was already supplied by an outer `WrapContent` wrapper
(`3f86853e`, unrelated to arrangement). Every `spacedBy`/`Start` node without a weighted child pays
this trial and gets nothing for it except "no". The idea under evaluation: skip that trial on
frames where nothing relevant to the answer changed since last frame, by caching the boolean
(or more) across frames instead of just within a frame.

## Investigation: what's actually knowable as an "input" before running `content`

Went through each candidate input concretely, not abstractly:

| Candidate input | Knowable before running `content`? | Notes |
|---|---|---|
| Resolved `width`/`height`/`gap`/`insets` passed into `measureRowContent`/`measureColumnContent` | **Yes** -- already fully resolved from `slot`/`effectiveArrangement` before the trial call site. Cheap `==` comparison against last frame's stored values. | Real, comparable input. |
| `context.currentTheme`/`currentFont`/`currentTextStyle` | **Yes** -- read via `sourceContext` before the trial runs. | Real, comparable, but rarely changes frame-to-frame for a given subtree; low marginal value on its own. |
| The `content` lambda's own identity (same lambda instance reused across frames) | **No, not reliably.** Any capturing lambda (references an outer `val`/parameter -- e.g. `item.label`, a `ColumnScope.() -> Unit` built inside a `repeat(fields) { field -> ... }` loop, which is the exact shape of the Checkout Form page) is a **new lambda object every frame** in Kotlin; there is no positional/call-site memoization the way Compose's compiler plugin provides. A non-capturing lambda *can* be a JVM singleton instance, but Awake's `UiScope`/`RowScope`/`ColumnScope` DSL bodies routinely capture (state, loop variables, page data) -- this cannot be relied on as a general signal. |
| `rememberStateValue` reads inside `content` | **No.** There is no lazy "declare dependencies, then decide, then run" phase in this DSL (unlike Compose's snapshot-state read tracking during composition, which the framework observes automatically). The only way to know what state a piece of content reads is to run it -- which is exactly the cost being avoided. This is the same "no compiler-tracked call-site identity" gap already named in the prior doc, not a new finding, but worth confirming directly: `rememberStateValue`'s implementation (`widgetStateInternal`) is a plain map read/write with zero read-tracking/invalidation-list bookkeeping (unlike Compose's `MutableState`, which registers itself with the currently-composing scope on every read). Building that bookkeeping *is* essentially building Compose's snapshot system from scratch -- out of scope for a "follow-up perf fix," it's its own multi-week architecture project. |
| External captures the lambda closes over (outer `val`s from the call site, e.g. a list of form fields driving `repeat(...)`) | **No**, not enumerable in Kotlin without reflection, and JVM-reflection-based closure field inspection (a) only works on the JVM target, breaking multiplatform parity for `ui-core`/`ui-unstyled` (this project's commonMain-first architecture, confirmed by grepping this module -- no existing `expect`/`actual` split for anything like this), and (b) is fragile even there (captured-field names/order aren't a stable contract across Kotlin compiler versions). Not viable. |

**Conclusion on question 1:** there is no automatic, fully-general way to detect "this subtree's
inputs haven't changed" in this DSL today. The only two genuinely knowable, cheaply comparable
inputs are the resolved layout geometry (width/height/gap/insets) and theme/font/textStyle -- and
neither of those is *sufficient* on its own, because the actual thing that determines
`hasWeightedChild` (or any other trial-derived answer) is the **content lambda's own body**, which
is invisible without either running it or having the caller explicitly declare a stable identity
and a change signal for it.

**The only viable path is an explicit, caller-supplied key** -- the same conclusion the prior doc
already flagged as "the most promising bigger win but needs its own dedicated design pass," and
this section is that pass's answer: yes, a fully-automatic solution is not achievable in this DSL
without either (a) a compiler plugin (explicitly out of scope, no such plugin exists in this
project and adding one is a different, much larger undertaking than a perf follow-up), or (b) a
restructured "gather children lazily, then decide" contract (the prior doc's other named
alternative, already flagged as a multi-week `RowScope`/`ColumnScope` contract audit on the scale
of the `UiSlot` narrowing work -- not this task). Anything short of those two is necessarily
opt-in and manual, mirroring `animateFloat(id, ...)`'s existing id-based identity model already in
this codebase (`UiAnimation.kt`), not Compose's automatic one.

## Proposed shape (opt-in, additive, not a breaking change)

Add two new optional parameters to `UiScope.row()`/`UiScope.column()` only (not the
`ColumnScope`/`RowScope`/`AbsoluteScope`/`BoxScope` wrappers -- those already have their own
`precomputedMeasured` reuse path and don't need this):

```kotlin
fun UiScope.row(
    ...,
    id: String? = null,
    cacheKey: Any? = null,
    content: RowScope.(slot: UiBounds) -> Unit
): UiBounds
```

- Both default to `null` -- **every existing call site is byte-for-byte unaffected**, zero risk to
  the ~100% of call sites that don't opt in.
- When `id != null` and `cacheKey != null`: before running the `hasWeightedChild`-detection trial,
  look up a cache entry keyed by `id` (stored in `UiContext`'s existing state store, same lifetime
  as `rememberStateValue` -- reset on `UiContext` recreation, i.e. per app-window-lifetime, not
  per-frame). If the entry exists and its stored `cacheKey` is `==` to the one just supplied, reuse
  the stored `hasWeightedChild` boolean and skip the trial entirely. Otherwise (first time, or
  `cacheKey` changed), run the trial as today and store the fresh `(cacheKey, hasWeightedChild)`
  pair.
- **What is cached is deliberately narrow: only the `hasWeightedChild` boolean, not the trial's
  full `UiMeasuredContent` (sizes/slots/positions).** The prior doc already established why: the
  `plannedSlots` branch (taken when `hasWeightedChild` or `requiresMeasuredDistribution()` is true)
  needs a fresh trial measured against this frame's *actual resolved* `slot` regardless, since
  slot bounds can legitimately change frame to frame (e.g. window resize, a sibling's `weight()`
  distribution shifting) even when the content's *structure* hasn't. Caching sizes/positions across
  frames would reintroduce exactly the class of stale-geometry bug `RepoBugTest`
  (`WrapContentMeasurementStateTest.kt`) already exists to catch -- caching only the boolean avoids
  that risk category entirely, because a boolean answer ("does this content structurally contain a
  `.weight()` call") is far less likely to silently go stale than a pixel size is, and -- more
  importantly -- is verifiable cheaply (see below).

## Cache lifetime and location

- Lives in `UiContext`'s existing state store (same store `rememberStateValue` uses), keyed by the
  caller-supplied `id` string, namespaced (e.g. `"__weightcache__$id"`, mirroring
  `UiAnimation.kt`'s `"__animation__$id"` convention already in this codebase).
- Lifetime: per-`UiContext` instance, i.e. reset on app restart / `UiContext` recreation, same as
  every other `rememberStateValue`-backed piece of state. **Not** per-frame (that provides zero
  cross-frame benefit) and **not** a separate global cache with its own independent lifetime (that
  would risk outliving the content it describes, e.g. surviving a full page navigation that reuses
  the same `id` string for structurally different content -- a real risk, addressed below, not
  avoided by picking a different lifetime).
- **This directly reuses the `createMeasureContext()` constraint already documented and tested**:
  trial passes must read the *real* state store, not a fresh one, specifically so a
  `rememberStateValue`-driven branch inside `content` sees live state during a trial
  (`WrapContentMeasurementStateTest.kt`'s regression). Storing the weight-cache entry in that same
  real state store means a trial that *is* run (cache miss) still observes live state exactly as it
  does today -- this design doesn't touch `createMeasureContext()`'s contract at all, it only adds
  a lookup that may skip calling `measureRowContent`/`measureColumnContent` in the first place.

## The actual correctness risk, named plainly

The `RepoBugTest`-class bug was: a trial pass silently measured against *default* state instead of
*real* state, because the trial's own `UiContext` used a fresh state store. That bug is structural
and this design doesn't reintroduce it (see above).

This design's own risk is a **different, new class**: **caller-supplied `cacheKey` staleness.**
If a caller passes a stable `id` but a `cacheKey` that doesn't actually change when the content's
weight-usage structure changes (e.g. `cacheKey = someUnrelatedValue`, or forgetting to include a
dependency that flips whether a child calls `.weight()`), the cache silently returns the wrong
`hasWeightedChild` answer, picking the wrong measurement branch. This is a genuinely new failure
mode, not present in the current unconditional-trial code at all, and it's caller-introduced (opt-
in), which makes it easy to get subtly wrong at a call site -- the same shape of hazard
`animateFloat`'s `id` staleness already carries in this codebase (documented in `MIRROR_MAP.md`:
"forgetting to keep `id` stable across frames ... silently restarts the animation with no compile-
time signal"). This design does not solve that class of hazard, it inherits it -- and should say so
plainly rather than pretend `cacheKey` staleness is impossible.

Mitigation: a debug/test-only **consistency check** (opt-in via `UiMeasureTrialStats`-style flag,
zero cost in normal builds, mirroring that file's own `enabled: Boolean = false` pattern already
established this session): when enabled, on every cache *hit*, also run the real trial anyway and
assert the freshly computed `hasWeightedChild` equals the cached one, throwing/logging loudly on
mismatch. This turns a silent-wrong-layout bug into a loud, test-suite-catchable one -- but only
during tests that opt in, since running the trial anyway on every hit defeats the perf purpose in
production.

## Concrete conservative first step

Given the above, the actually-safe, narrow first step is:

1. Add the `id`/`cacheKey` parameters to `UiScope.row()`/`UiScope.column()` only, defaulting to
   `null` (no behavior change for any existing call site).
2. Cache only the `hasWeightedChild` boolean, never sizes/positions, for the reasons above.
3. Ship the debug-mode consistency check (`UiWeightCacheStats`-style flag, off by default) in the
   same change, not as a follow-up -- this is the thing that makes the opt-in safe to recommend to
   call sites at all, not an optional nice-to-have.
4. Do **not** attempt a static "provably pure" check as an alternative to the explicit key. Verified
   during this investigation (see table above) that Kotlin's lack of call-site identity/closure
   introspection in common code makes this infeasible without a compiler plugin or JVM-only
   reflection -- neither viable here. Say this plainly rather than attempting a fake-automatic
   heuristic (e.g. "no `rememberStateValue` calls textually visible in the lambda body" via source
   inspection) that would be unreliable and give false confidence.

This mirrors the discipline of the already-landed narrow fix: additive, opt-in, zero risk to
untouched call sites, one clearly named new risk class with a concrete mitigation, not a general
solution.

## Verification of correctness

- Extend `WrapContentMeasurementStateTest.kt`'s pattern (or a new sibling file,
  `RowColumnWeightCacheTest.kt`, since the failure mode is genuinely different from that file's
  stale-state-store bug) with:
  1. A cache-hit test: `id`/`cacheKey` supplied and held constant across two frames with content
     that doesn't change -- confirm the second frame's trial count (`UiMeasureTrialStats`) is lower
     than the first, and the resulting layout (slot positions) is identical to running without
     caching at all.
  2. A cache-invalidation test: same `id`, `cacheKey` changed between frames -- confirm a fresh
     trial runs and the (different) `hasWeightedChild` answer is picked up correctly.
  3. A **stale-key regression test**: same `id`, `cacheKey` deliberately held constant while content
     structurally changes (a `.weight()` call added/removed) between frames -- with the debug
     consistency check enabled, assert the mismatch is caught (exception/log), not silently
     swallowed into a wrong layout. This is the test that plays the same role for this design that
     `WrapContentMeasurementStateTest` plays for the trial/state-store bug: it exists specifically
     because this design has a real, named risk of shipping silently-wrong UI otherwise.
- Full `desktopTest` baseline unchanged (scene-dsl=0, ui-showcase=5, game-dsl=3, others=0 failed) --
  this design changes nothing for any call site that doesn't opt in, so a baseline shift would mean
  something else broke.

## Verification of the perf win, and an honest read on real-world impact

Extend `TrialMeasureScalingTest.kt` with a new fixture: the same nested chain shape but using
`Arrangement.spacedBy`/no `weight()` (the case the landed fix explicitly does not help), run for
**two frames** with `id`/`cacheKey` supplied and held constant, and assert frame 2's trial count is
`O(depth)` (linear -- each level pays one real execution but the cache prevents the extra trial),
not `O(2^depth)`. This is the fixture shape that's actually representative of what's failing today
(confirmed: `spacedBy` is unaffected by the landed fix per `MIRROR_MAP.md`'s own updated entry).

**Honest read on the real Checkout Form page specifically:** this conservative first step does
*not* automatically improve `UiShowcaseFieldDemoPage.kt` or its containing shell. Checked directly:
that file alone has 4 `row(`/`column(` calls (`grep -c`), and the page is reached through the
shared shell/sidebar chrome that contributes most of the ~9-10 levels of nesting depth measured in
the prior doc's `trialsByDepth` data -- realistically several dozen `row()`/`column()` call sites
across the shell and every field-demo page would each need `id`/`cacheKey` added by hand to see the
7,696-trial number move at all, since the opt-in cache only helps nodes that explicitly supply
both parameters. **This is real adoption cost, not a ui-core-internal change** -- every call site
needs a human to pick a stable `id` and correctly identify what its `cacheKey` should depend on
(get it wrong and you hit the stale-key risk named above). This is meaningfully bigger and slower
to land than the previous narrow reorder fix, which required touching zero call sites outside
`Row.kt`/`Column.kt` themselves.

Given that, the realistic sequencing if this is pursued: land the opt-in mechanism + consistency
check + `TrialMeasureScalingTest` fixture first (ui-core-only, zero call-site migration, provably
safe on its own), then migrate call sites incrementally starting with the highest-value ones (the
shell/sidebar chrome, since it's shared across every page and therefore the one place where the
`id`/`cacheKey` migration cost is paid once but the win compounds across every page) -- not as a
single big-bang page-by-page migration.

## Status

- [x] Investigated whether any input to `hasWeightedChild` is automatically, cheaply, and
      *sufficiently* knowable without running `content` -- confirmed geometry/theme are knowable
      but not sufficient on their own; `rememberStateValue` reads and lambda captures are not
      knowable without either running `content` or a compiler-level mechanism this project doesn't
      have.
- [x] Confirmed a fully-automatic solution is not achievable in this DSL without a compiler plugin
      or a lazy-children-gathering contract restructure -- both already flagged out of scope by the
      prior doc; this doc does not revisit that conclusion, it confirms it with a concrete
      knowability audit rather than restating it abstractly.
- [x] Landed on the only viable path being an explicit, opt-in, caller-supplied `id`/`cacheKey`
      pair, mirroring `animateFloat`'s existing id-based identity model.
- [x] Scoped the cache to the narrowest safe thing: the `hasWeightedChild` boolean only, never
      sizes/positions -- explicitly reasoned through why caching sizes would reintroduce the
      `RepoBugTest`/`WrapContentMeasurementStateTest` class of staleness bug and why the boolean
      does not carry the same risk, plus a debug-mode consistency check as the concrete mitigation
      for this design's own new risk class (caller `cacheKey` staleness).
- [x] Named the correctness verification plan: a new `RowColumnWeightCacheTest.kt` with cache-hit,
      cache-invalidation, and stale-key-regression cases, the last of which directly exercises the
      mitigation.
- [x] Named the perf verification plan: a `spacedBy`-specific two-frame fixture added to
      `TrialMeasureScalingTest.kt`.
- [x] Gave an honest read on real-world impact: this conservative first step requires per-call-site
      opt-in migration (`id`/`cacheKey` added by hand) to move the Checkout Form page's actual
      7,696-trial number at all -- it is not a drop-in fix the way the landed reorder was, and the
      real-world win is gated on adoption cost this doc does not hide.
- [ ] Not started: implementation of the `id`/`cacheKey` parameters, cache storage, or consistency
      check (deliberately out of scope for this design-only task).
- [ ] Not started: any call-site migration (shell/sidebar chrome or individual pages).

## Overall recommendation: is this worth pursuing?

**Worth building the opt-in mechanism itself -- low risk, real (if narrow) win, and it's the
correct target given the knowability audit above ruled out anything more automatic. Not worth
promising it will fix the Checkout Form page's headline number by itself**, because that requires
call-site migration this doc can't shrink away. Two things worth flagging as possibly
higher-leverage than continuing down this path immediately:

1. **Fix approach B from the prior doc first** ("cheaper trial passes" -- reducing the fixed
   `UiContext(measuring=true)` construction/`beginFrame`/`pushTextStyle`/`pushFont`/`pushTheme`
   cost per trial). It was deprioritized in the prior doc as "a constant-factor win, not a fix for
   the exponential shape," but it requires **zero call-site changes anywhere** (pure `ui-core`
   internal work) and stacks multiplicatively with whatever caching eventually lands -- every trial
   this design's cache *doesn't* manage to skip (which, realistically, is most of them until
   migration is well underway) still pays that fixed overhead 7,696 times. Given this design's own
   finding that call-site migration is the actual bottleneck to real-world impact, approach B is a
   better next unit of work: it improves the exact page in question today, with no adoption cost,
   while this caching design's call-site migration proceeds in parallel or afterward.
2. If the caching design is pursued, **start the call-site migration with the shared shell/sidebar
   chrome, not individual pages** -- confirmed above this is where the win compounds since it's
   common to every page, and it's the one place the migration cost is paid exactly once.

Recommended sequencing for whoever picks this up next: land approach B (cheap, immediate,
zero-risk, no adoption cost) before committing to this caching design's larger, adoption-cost-
bearing path.
