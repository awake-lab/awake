# UiSlot narrowing (2026-07-24)

Follow-up to `docs/tasks/2026-07-17-ui-api-simplification.md`'s deferred item: `UiSlot` is
currently a public type constructed and read across 9 modules (186 raw `UiSlot(...)` call sites).
It should be measured-output-only, internal to `ui-core`'s layout engine, so future layout-engine
changes don't ripple into every downstream module again.

## Plan

Split into two types:

- `UiSlot` (existing, `ui-core` internal) -- stays the mutable/measurement-facing type the layout
  engine composes internally. Made `internal` to `ui-core`.
- `UiBounds` (new, public) -- frozen value type crossing the module boundary. Return type of
  `surface{}`/`row{}`/`claimSlot()`, type of `UiSemanticNode.contentBounds` and any lambda param
  handed to downstream widget code.

## Contract audit (completed 2026-07-24)

Audited every downstream read (not construction) of `UiSlot` outside `ui-core`, across
ui-unstyled, ui-dsl, ui-designsystem, engine/ui/ui-testing, game-dsl, samples, and backends.

- Overwhelming majority: `.x` / `.y` / `.width` / `.height` reads only.
- `.place(...)` -- exactly 1 downstream call site (`game-dsl/GameUiRuntime.kt:113`).
- `.inset(...)` -- used only inside `ui-unstyled` (`Surface.kt`, `Text.kt`, `Textarea.kt`).
- No downstream code reads a `gap` field off `UiSlot` -- the "gaps on components" symptom is local
  spacing constants (`CHECKBOX_LABEL_GAP`, `TOGGLE_LABEL_GAP`, line-height gap in `Textarea`)
  combined with `.width`/`.height`/`.x` reads, e.g. `Checkbox.kt:65`, `Switch.kt:67,72`,
  `Textarea.kt:293`, `PropertyCheckbox.kt:43-70`. `UiBounds` doesn't need a gap field; these sites
  keep working unchanged since they only need x/y/width/height.
- A few sites reconstruct/`.copy()` a slot-shaped value outside ui-core (`UiLayoutSignatureTest.kt`,
  `ReusableCompositionTest.kt`, `ProgressBar.kt`, `Switch.kt`, `TextField.kt`) -- these need to
  migrate onto `UiBounds(...)`/`.copy(...)` too.

**Conclusion:** `UiBounds(x, y, width, height)` plus a `place(...)` extension covers every
downstream module except ui-unstyled, which also needs `.inset(...)`. Since ui-unstyled is a
separate module from ui-core, `.inset(...)` must ship on `UiBounds`, not stay ui-core-internal.

## Implementation Order (batched, 2026-07-24)

Given the 186-site blast radius, migrating in one pass is too large a single change. Splitting
into batches, each independently compilable/testable/committable:

**Batch 1 -- `anchorSlot` params (in progress).** The narrowest, most self-contained slice:
`popup(anchorSlot: UiSlot, ...)` (`ui-core/UiPopup.kt`) and its downstream `anchorSlot: UiSlot`
params on `shadcnTooltip`, `shadcnTooltipText`, `shadcnDropdownMenu`
(`ui-designsystem/components/popup/`). ~20 call sites across ui-unstyled, ui-designsystem,
samples, and tests (several tests construct `UiSlot(...)` directly as the arg).
1. Add `UiBounds` (`ui-core`, `layout` package) with `x`/`y`/`width`/`height` -- start minimal
   (just the fields), add `.place(...)`/`.inset(...)` ports only if this batch's call sites
   actually need them (`anchorSlot` itself is read-only positioning data, may not need either).
2. Add `UiSlot.toBounds()` and `UiBounds.toSlot()` conversions at the ui-core boundary --
   `popup()`'s internal math (`calculatePosition`, `hitTest`) stays `UiSlot`-based, converts at
   the function boundary.
3. Change `anchorSlot`'s type to `UiBounds` on `popup()`, `shadcnTooltip()`,
   `shadcnTooltipText()`, `shadcnDropdownMenu()`.
4. Fix the ~20 call sites: real widget results' `.slot` field is still `UiSlot`-typed (Batch 2's
   job, not this batch's), so callers add an explicit `.toBounds()` at the call site for now;
   test files that construct `UiSlot(...)` directly switch to constructing `UiBounds(...)`.
5. Compile whole tree (0 errors), run `desktopTest`, confirm baseline: scene-dsl=1,
   ui-showcase=6, ui-dsl=3 (now under game-dsl), all other modules 0.

**Batch 2 -- widget return types (not started).** Change public-facing return types
(`surface{}`, `row{}`, `claimSlot()`, `UiSemanticNode.contentBounds`/`bounds`, `UiButtonResult
.slot`, and other widget-result `.slot` fields) from `UiSlot` to `UiBounds`. This is what lets
Batch 1's callers drop their explicit `.toBounds()` calls (the value is already `UiBounds` by
the time it reaches them). Heaviest-consumer-first module order (ui-unstyled, then
ui-designsystem, engine/ui/ui-testing, game-dsl, samples, backends), same
compile-iterate-to-convergence technique used for the layout/style package move.

**Batch 3 -- lock it down (not started).**
1. Mark `UiSlot`'s constructor/class `internal` to `ui-core` once no downstream file references it.
2. Enforce mechanically: add `"UiSlot"` to `forbiddenUiTypeReferences` for
   `:awake:engine:ui:ui-unstyled` in
   `build-logic/src/main/kotlin/awake.ui-ownership-convention.gradle.kts` (the existing
   `verifyUiOwnership` check already supports this via `forbiddenTypeReferences`). Do this only
   once Batch 2 has zero remaining downstream `UiSlot` references, or `check` fails immediately.

## Hard Rule (in effect now, enforced once implementation lands)

`UiSlot` is `ui-core`-internal. No module outside `ui-core` may construct, read, or `.copy()` a
`UiSlot`. Anything crossing the `ui-core` boundary must use `UiBounds` instead. Recorded in
`docs/reference/ui-ownership.md`'s Hard Rules list (rule 6).

## Status

- [x] Contract audit
- [x] Rule recorded in `docs/reference/ui-ownership.md` (rule 5, with concrete `anchorSlot` example)
- [ ] Batch 1 (`anchorSlot` params) -- in progress
- [ ] Batch 2 (widget return types)
- [ ] Batch 3 (lock down + mechanical enforcement)
