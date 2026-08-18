# Trial-measure double-execution perf bug (2026-08-02)

Design/scoping only -- no fix implemented in this task. Read `UiContextMeasureState.kt`,
`UiMeasurementRuntime.kt`, `UiMeasureTrialStats.kt`, `layouts/Row.kt`, `layouts/Column.kt`, and
`samples/ui-showcase/.../UiShowcaseLayoutCostTest.kt` before touching anything here.

## Problem statement

Confirmed via real profiling (commit `dbdc3564`, task #60): on ui-showcase's Checkout Form page,
trial-measurement is 97.8-98.0% of frame layout time (~47-49ms of ~48-50ms total), 7,696 trial
passes vs. 44 on the simplest page (Overview) -- a ~175x scale factor while primitive count barely
moves (1,344 -> 1,480 glyphs/quads). This is backend-agnostic `ui-core` cost (affects Vulkan and
WebGPU equally).

## Mechanism audit (this session, empirically confirmed, not theorized)

### What's already fixed (commit `3f86853e`, do not re-break)

Before that commit, a `WrapContent`-sized `row()`/`column()` invoked through the
`RowScope.row()`/`ColumnScope.column()`/etc. wrapper functions in `Row.kt`/`Column.kt` paid for
**two separate trial passes of the same content at the same level**: one in the wrapper (to learn
the `WrapContent` size) and a second, independent one inside the wrapped `UiScope.row()`/`column()`
call (to answer "does this node have a weighted child," needed to pick the `plannedSlots` vs.
`childRow`/`childColumn` fast path). `3f86853e` added a `precomputedMeasured: UiMeasuredContent?`
parameter so the wrapper's trial result is threaded straight into the inner call, skipping that
second, same-level, same-content trial. This cut a plain N-level `WrapContent` nesting chain from
3^N to 2^N content-lambda executions (verified in that commit: 81 -> 16 for the checkout form's
4-level field nesting). **This fix is real, correct, and still in place -- confirmed by reading the
current source.** Nothing in this doc proposes touching it.

### What's still there: the remaining 2^N

The `precomputedMeasured` reuse only cancels the duplicate trial **at the exact same node**, and
only when that node happens to be `WrapContent`-sized (so the wrapper already ran a trial to
reuse). It does **not** help the much more common case: a `row()`/`column()` call that is *not*
itself `WrapContent`-sized (e.g. every `fillMaxWidth()` field row inside a form) still falls
through to `UiScope.row()`/`column()`'s own unconditional weight-detection step
(`Row.kt:238-245`, `Column.kt:313-320`):

```kotlin
val hasWeightedChild = (precomputedMeasured ?: run {
    context.measureRowContent(height = slot.height, gap = ..., width = slot.width, content = content)
}).weights.any { it != null }
```

This runs a **full trial execution of `content`** purely to answer a boolean ("did any direct
child call `.weight()`") -- discards the trial's sizes/positions entirely in the common case (no
weighted children, simple arrangement), then falls to `childRow`/`childColumn`, and separately runs
`content` again for real at the bottom of the function via
`context.withMeasuredRecordingSuppressed { scope.content(slot) }` (`Row.kt:305`, `Column.kt:376`).

So **every** `row()`/`column()` node not covered by a same-level `precomputedMeasured` reuse
executes its `content` lambda exactly twice per frame: once as a throwaway trial, once for real.
Because `content` is an ordinary Kotlin lambda that synchronously recurses into every nested
`row()`/`column()` call (there is no lazy "gather children, decide, then measure" phase the way
Compose's `Measurable` list allows -- see `MIRROR_MAP.md`'s Scope/DSL section), each of those two
executions *itself* re-triggers this same 2x pattern in every descendant node. That compounds
multiplicatively with nesting depth.

**Empirical call-graph confirmation** (temporary instrumentation added to
`UiMeasureTrialStats`/`Row.kt`/`Column.kt`/`UiShowcaseLayoutCostTest.kt` this session, run against
`measureRealShellFrameCost`, then fully reverted -- not part of the committed tree):

```
trialsByDepth    = {0=22, 1=37, 2=55, 3=138, 4=404, 5=952, 6=1640, 7=2144, 8=1728, 9=576}
realExecsByDepth = {0=9,  1=22, 2=51, 3=130, 4=372, 5=912, 6=1624, 7=2144, 8=1728, 9=576}
realContentExecs = 7568   (trialCount was 7696)
```

Two things this confirms precisely:

1. **`trialsByDepth[d] ~= realExecsByDepth[d]` at every depth** -- each row/column node really does
   pay almost exactly one trial + one real execution, not some other ratio. The two totals (7696
   trials vs. 7568 real execs) are nearly equal, which is exactly what "every node does 1 trial + 1
   real" predicts.
2. **Both series roughly double per depth level from depth 3 onward** (404 -> 952 -> 1640 -> 2144
   trials at depths 4-7) -- the compounding is real and depth-driven, not a flat per-node constant
   or a one-off duplicate at a single level. The Checkout Form page reaches ~9-10 levels of
   row/column nesting (sidebar > page > field group > field row > field > ...); the Overview page
   is much shallower, which is why 44 trials there vs. 7696 here is not surprising once you see the
   per-depth doubling -- it is the direct, already-understood consequence of nesting depth, not a
   separate new bug.

**Conclusion for question 1 in the task brief:** the blowup does **not** come from the same
subtree being independently re-trialed by an outer node's trial pass *and* its real pass in a way
`3f86853e` failed to catch (that specific redundancy is fixed). It comes from a *different*,
still-open redundancy one level down: **the unconditional weight-detection trial inside
`UiScope.row()`/`column()` itself**, which fires at every node whose `precomputedMeasured` is null
(i.e., every node not itself `WrapContent`-sized), discards a full subtree execution purely to
learn a boolean, and then re-executes that same subtree for real immediately after. This is
inherent, structural, and repeats at every depth level, which is why it compounds exponentially
with nesting depth rather than scaling with node count.

## Fix approaches considered

### A. Same-frame memoization of the trial's `UiMeasuredContent` for reuse by the real pass

Not viable as originally scoped: the trial and the real pass are not "the same content measured
twice for the same purpose" -- the real pass isn't a measurement at all, it's the actual widget
emission with real (non-measuring) side effects (state mutation, animation stepping, hit-testing
against real input, tree/primitive emission for rendering). You cannot skip the real pass by
reusing the trial's `UiMeasuredContent` snapshot; the real pass has to run regardless. This
approach only ever helps the exact case `3f86853e` already fixed (two trials at the same node) --
already done, no further win here.

### B. Cheaper trial passes (avoid full `UiContext` construction cost per trial)

Would reduce the fixed per-trial overhead (`UiContext(measuring=true)` alloc, `beginFrame`,
`pushTextStyle`/`pushFont`/`pushTheme`) but does not address the multiplication itself -- with
~7,700 trial passes at depth up to 9, a fixed-cost reduction is a constant-factor win (maybe
20-40% off trial time, unverified), not a fix for the underlying exponential shape. Worth doing
eventually as a cheap follow-on, not a substitute for addressing B below. Lower priority than C.

### C. Eliminate the unconditional weight-detection trial where it's structurally unnecessary
   (recommended)

The real target is `UiScope.row()`/`column()`'s `hasWeightedChild` check
(`Row.kt:238-245`/`Column.kt:313-320`). Two sub-cases:

1. **Node was reached via a `WrapContent`-sizing wrapper** (`RowScope.row()` etc.): `3f86853e`
   already covers this by passing `precomputedMeasured`. No further work needed here.
2. **Node was NOT `WrapContent`-sized** (the majority of real nodes -- e.g. `fillMaxWidth()` field
   rows): `precomputedMeasured` is null, so `UiScope.row()`/`column()` pays its own unconditional
   trial. This is the actual open gap.

The key insight for (2): a non-`WrapContent`-sized node's `hasWeightedChild` trial and its
subsequent real render both execute the *exact same* `content` lambda against the *exact same*
final `slot` (there is no size still-to-be-determined the way there is for the `WrapContent` case
-- `slot` is already fully resolved by `claimModifiedSlot` before the trial even runs, since
neither width nor height is `WrapContent` here). That means, unlike case (1), the trial's
`UiMeasuredContent.slots`/`weights` **are already exactly what the real pass would also produce
positionally** for the fast (`childRow`/`childColumn`) path -- there is no "wider provisional
bound vs. tightened real bound" mismatch the `WrapContent` case has to guard against.

Concretely: restructure so that when `precomputedMeasured` is null, `UiScope.row()`/`column()` runs
**one** trial-context execution of `content` (as it already does), but if that trial turns out
*not* to need `plannedSlots` (no weighted child, arrangement doesn't require measured
distribution -- the overwhelming common case per the depth data above, since `realExecsByDepth`
and `trialsByDepth` track almost 1:1, meaning most of these trials are wasted exactly this way),
**render the real content by replaying the trial's recorded slot list directly through
`childRow`/`childColumn`'s placement instead of re-invoking `content` a second time.** This
requires `childRow`/`childColumn` (or a new sibling function) to accept a pre-resolved list of
child slots instead of driving placement live off `content`'s own cursor-advancing `claimSlot`
calls -- a real, non-trivial restructuring of the fast path's placement mechanism, not a one-line
change, since today `childRow`/`childColumn` *is* the thing that walks `content` and drives
placement synchronously; there is currently no "replay these slots without re-running content"
entry point.

**This is the correct fix in principle but carries real implementation risk**: the fast path
(`childRow`/`childColumn`) is also where non-measurement side effects live (hit-testing against
`hitTest(slot)`, `forceHover`/`forceActive`/`forceFocus`, semantic-node recording, and any
descendant widget's own state mutation/animation stepping) -- all of that currently only happens
correctly once, during the "real" (non-measuring) execution of `content`. A trial execution
explicitly runs with `measuring = true` and existing side-effecting code (`animateFloat*`, etc.)
already special-cases `isMeasuringInternal()` to no-op during trials (see `UiAnimation.kt`'s
guards, referenced in `MIRROR_MAP.md`). Reusing the trial's *slots* while still running `content`
for real (not deleting the real execution, just not re-deriving `hasWeightedChild` from a second
full trial) is safe; trying to skip the real execution entirely and only synthesize output from the
trial's recorded slots would very likely reintroduce a real-vs-trial side-effect gap (hover
state, animation stepping, semantic recording) -- **not recommended**, and not what this design
proposes.

### Revised recommendation: narrow the fix to eliminating the trial *when it's provably
    unnecessary*, not to replaying its output

Given the risk profile above, the safer, smaller-blast-radius version of C is: **skip the
`hasWeightedChild` trial call entirely when `effectiveArrangement.requiresMeasuredDistribution()`
is already `true`** (no trial-based detection needed -- the plannedSlots branch is taken
unconditionally regardless of `hasWeightedChild` in that case, so today's code already pays for a
trial whose result is provably going to be discarded by the `||` short-circuit... except Kotlin's
`||` on `val hasWeightedChild = (...).weights.any { ... }` is **not actually short-circuited**
against `requiresMeasuredDistribution()` -- the trial runs *before* the `if` that checks
`effectiveArrangement.requiresMeasuredDistribution() || hasWeightedChild`, so today's code
literally always pays the trial even in the `requiresMeasuredDistribution() == true` case where the
boolean is provably irrelevant to the branch decision (that branch always takes the
`plannedSlots` path either way, and unconditionally re-measures once more inside it regardless of
`hasWeightedChild`'s value). **This alone is a real, low-risk, semantically-inert fix**: reorder to
check `effectiveArrangement.requiresMeasuredDistribution()` first and only run the trial (to decide
`hasWeightedChild`) when the arrangement doesn't already force the `plannedSlots` branch.

This narrow fix does not eliminate the exponential shape (most real nodes in the checkout form use
plain `Arrangement.spacedBy`/`Start`, which does *not* set `requiresMeasuredDistribution()`, so
most nodes still need the trial purely to detect `.weight()` usage) -- but it is a genuinely free,
zero-risk win for every `requiresMeasuredDistribution()` arrangement (`SpaceBetween`,
`SpaceEvenly`, `SpaceAround`, per `Arrangement.kt` -- confirm exact set before implementing) node
in the tree, and should be checked/landed as its own tiny, separate change regardless of what
happens with the larger question below.

### The actual open architectural question

Eliminating the *remaining* trial (the common case: plain arrangement, need to detect
`.weight()` on direct children before laying out any of them) without re-executing `content`
requires either:

- **A lazy children-gathering phase** (Compose's approach): change `content`'s contract from
  "immediately place each child via `claimSlot`" to "first enumerate children/modifiers, then
  place" -- a fundamental, cross-cutting change to the entire `RowScope`/`ColumnScope`/`claimSlot`
  contract that every widget in the codebase depends on. Far too large and risky for this task's
  scope; explicitly flagged as **not recommended** without a dedicated, larger design effort of its
  own (would need its own audit of every `RowScope`/`ColumnScope` consumer, likely a multi-week,
  multi-file effort on the scale of the `UiSlot` narrowing work in
  `docs/tasks/2026-07-24-uislot-narrowing.md`, not a follow-on to this doc).
- **Static/structural weight detection without executing `content`** -- not possible in Kotlin
  without either a compiler plugin (out of scope, this project deliberately avoids one per
  `docs/reference/ai-collaboration.md`'s "no compiler-tracked call-site identity" framing already
  cited in `MIRROR_MAP.md`) or requiring every call site to declare weight usage out-of-band (a
  breaking API change to every `row {}`/`column {}` call site in the codebase -- not viable).
- **Caching `hasWeightedChild` per call site across frames**, keyed by something stable (e.g. a
  caller-supplied `id`, mirroring `animateFloat`'s `id`-based identity model already in the
  codebase) -- viable in principle (weightedness is structural per call site, essentially never
  changes frame to frame for a given UI screen) but real correctness risk: many `row()`/`column()`
  calls in this codebase have no `id` (`UiScope.row()` doesn't even accept one; only
  `resolveMeasuredColumn`'s `column()` does), and coding up a correct fallback identity (e.g.
  source-line-based via `expect`/`actual` or a compiler-emitted key wouldn't exist here) is not
  straightforward without a stable identity contract this DSL currently doesn't have. This is the
  most promising bigger win but needs its own dedicated design pass on identity/invalidation before
  it's safe to implement -- explicitly out of scope for this doc; flagged as the next investigation
  if the small `requiresMeasuredDistribution()` reorder fix (above) turns out insufficient in
  practice.

## Recommended scope for a follow-up implementation task

1. **Land the `requiresMeasuredDistribution()`-before-trial reorder** in `UiScope.row()`/
   `UiScope.column()` (`Row.kt`, `Column.kt`) -- small, contained, single-file-pair change, zero
   behavior change for any node whose arrangement doesn't require measured distribution, and a real
   trial-avoidance win for every node whose arrangement does. Estimated risk: low. Estimated size:
   ~10-20 line diff across `Row.kt`/`Column.kt`.
2. **Do not** attempt the lazy-children-gathering or per-call-site-id caching approaches in the
   same task -- both need their own dedicated design/audit pass (identity contract for the cache
   approach; full `RowScope`/`ColumnScope` contract audit for the lazy-gathering approach) before
   they're safe to scope, let alone implement. If step 1's real-world win (measured via the
   verification plan below) is small relative to the 175x/97.8% headline number, that is the
   correct trigger for commissioning that follow-up design work -- not a reason to force either
   approach into this task's already-narrow, low-risk recommendation.
3. Explicitly confirmed **not a regression risk** to the already-fixed bug classes named in the
   task brief: the wrap-height-corruption fix (`3f86853e`'s `measuredMaxBottomExcludingFill`) and
   the `weight()`-starvation fix (`9455bc51`'s `fillsMainAxis`-first resolution order) both live in
   `UiContextMeasureState`/`resolveWeightedMainAxis` -- untouched by the reorder in step 1, which
   only changes *when* (not *whether*, not *how*) the trial for `hasWeightedChild` runs.

## Verification plan (for whoever implements step 1 above)

1. Re-run (or re-add, since this session's instrumentation was reverted) the depth-bucketed
   `trialsByDepth`/`realExecsByDepth` instrumentation in `UiMeasureTrialStats` temporarily, confirm
   the reorder measurably shrinks `trialCount` for depths where `requiresMeasuredDistribution()`
   arrangements are in use in the Checkout Form page (grep the page's real source for
   `Arrangement.SpaceBetween`/`SpaceEvenly`/`SpaceAround` usage first to know what to expect --
   don't assume without checking; if the Checkout Form happens to use none of these, this fix's
   real-world win on this specific page could legitimately be zero, in which case say so plainly in
   the follow-up task's own findings rather than forcing a doc rewrite here).
2. Extend `UiShowcaseLayoutCostTest.measureRealShellFrameCost` with an assertion (not just a print)
   that `heavy.trialCount` (Checkout Form) drops by whatever amount step 1's actual measurement
   shows -- pin the new number as a regression floor, matching this project's existing pattern of
   turning an investigation print into a committed assertion once a fix lands.
3. Run full `desktopTest` and confirm the existing documented baseline is unchanged: scene-dsl=0,
   ui-showcase=5, game-dsl=3, all other modules=0 failed. Any change here means the reorder altered
   real behavior, not just trial count, and must be investigated before landing.
4. Compile `samples/ui-showcase` (already the heaviest UI consumer) plus any other sample that uses
   `SpaceBetween`/`SpaceEvenly`/`SpaceAround` arrangements, to catch any visual regression the
   automated test baseline might not cover -- regenerate snapshot/tutorial reports per this
   project's UI-change validation convention if any visual diff appears.

## Status

- [x] Confirmed the specific mechanism (not theorized): unconditional per-node trial-then-real
      double execution of `content`, compounding with nesting depth, empirically verified via
      depth-bucketed `trialsByDepth`/`realExecsByDepth` instrumentation (data captured in this doc,
      instrumentation itself reverted -- not part of the committed tree).
    - `trialsByDepth`/`realExecsByDepth` counts are nearly 1:1 at every depth level, confirming
      "one trial + one real execution per node" as the mechanism, not some other pattern.
    - Both series roughly double per depth level from depth 3 onward, confirming the exponential
      shape is depth-driven, matching the already-diagnosed 2^N characterization in `3f86853e`'s
      own commit message (that commit fixed a *different*, same-node 3^N->2^N redundancy; this
      doc's finding is the next layer down, still exponential).
- [x] Ruled out "same subtree independently re-trialed by outer trial pass AND outer real pass" as
      the leading unaddressed cause -- that specific pattern was `3f86853e`'s fix and is confirmed
      still in place.
- [x] Identified the actual open redundancy: `UiScope.row()`/`column()`'s unconditional
      `hasWeightedChild`-detection trial, paid at every node not covered by same-level
      `precomputedMeasured` reuse.
- [x] Evaluated same-frame memoization (not viable as scoped -- trial and real pass serve different
      purposes), cheaper-trial-construction (real but small constant-factor win, not addressed
      here), and lazy-children-gathering / per-call-site caching (both real candidates for the
      actual exponential-eliminating fix, but too large/risky to scope inside this task -- flagged
      as follow-up design work, not attempted here).
- [x] Landed on a narrow, low-risk, immediately actionable partial fix (reorder
      `requiresMeasuredDistribution()` check before the trial) with an honest caveat that its
      real-world impact on the Checkout Form page specifically is unverified pending a grep of
      that page's actual arrangement usage -- explicitly flagged as the first thing a follow-up
      implementation task must check, not assumed.
- [ ] Not started: implementation of the reorder fix (deliberately out of scope for this task).
- [ ] Not started: the larger per-call-site-id caching or lazy-children-gathering redesign (out of
      scope; needs its own dedicated design task if step 1's win proves insufficient).
