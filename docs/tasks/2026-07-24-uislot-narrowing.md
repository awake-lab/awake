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
ui-headless, ui-dsl, ui-designsystem, engine/ui/ui-testing, game-dsl, samples, and backends.

- Overwhelming majority: `.x` / `.y` / `.width` / `.height` reads only.
- `.place(...)` -- exactly 1 downstream call site (`game-dsl/GameUiRuntime.kt:113`).
- `.inset(...)` -- used only inside `ui-headless` (`Surface.kt`, `Text.kt`, `Textarea.kt`).
- No downstream code reads a `gap` field off `UiSlot` -- the "gaps on components" symptom is local
  spacing constants (`CHECKBOX_LABEL_GAP`, `TOGGLE_LABEL_GAP`, line-height gap in `Textarea`)
  combined with `.width`/`.height`/`.x` reads, e.g. `Checkbox.kt:65`, `Switch.kt:67,72`,
  `Textarea.kt:293`, `PropertyCheckbox.kt:43-70`. `UiBounds` doesn't need a gap field; these sites
  keep working unchanged since they only need x/y/width/height.
- A few sites reconstruct/`.copy()` a slot-shaped value outside ui-core (`UiLayoutSignatureTest.kt`,
  `ReusableCompositionTest.kt`, `ProgressBar.kt`, `Switch.kt`, `TextField.kt`) -- these need to
  migrate onto `UiBounds(...)`/`.copy(...)` too.

**Conclusion:** `UiBounds(x, y, width, height)` plus a `place(...)` extension covers every
downstream module except ui-headless, which also needs `.inset(...)`. Since ui-headless is a
separate module from ui-core, `.inset(...)` must ship on `UiBounds`, not stay ui-core-internal.

## Implementation Order (batched, 2026-07-24)

Given the 186-site blast radius, migrating in one pass is too large a single change. Splitting
into batches, each independently compilable/testable/committable:

**Batch 1 -- `anchorSlot` params (in progress).** The narrowest, most self-contained slice:
`popup(anchorSlot: UiSlot, ...)` (`ui-core/UiPopup.kt`) and its downstream `anchorSlot: UiSlot`
params on `shadcnTooltip`, `shadcnTooltipText`, `shadcnDropdownMenu`
(`ui-designsystem/components/popup/`). ~20 call sites across ui-headless, ui-designsystem,
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

**Batch 2 -- widget return types (not started, re-scoped 2026-07-24).** Change public-facing
return types (`surface{}`, `row{}`, `claimSlot()`, `UiSemanticNode.contentBounds`/`bounds`,
`UiButtonResult.slot`, and other widget-result `.slot` fields), plus every
`content: XScope.(slot: UiSlot) -> Unit` lambda param (the Slot-API content-lambda pattern
carries measured output through nearly every widget), from `UiSlot` to `UiBounds`. This is what
lets Batch 1's callers drop their explicit `.toBounds()` calls (the value is already `UiBounds`
by the time it reaches them).

**Full audit (2026-07-24) found this is much larger than the original ~166-site estimate
implied:** in `ui-headless`/`ui-designsystem` `commonMain` alone -- not counting ui-core-internal
call sites, game-dsl, samples, or test files -- there are 29 public `UiSlot` return types, 79
public `UiSlot` param types (mostly the content-lambda pattern above), and 5 data classes with a
`UiSlot` field (`Buttons.kt`, `Interaction.kt`, `Surface.kt`, `DropdownMenu.kt`,
`ShadcnPropertyRow.kt`). Realistic total is 150-200+ edits, effectively the whole widget API
surface, not a contained slice. Treat this as its own dedicated session, not a continuation of
Batch 1 -- do a fresh, file-by-file inventory of all ~113+ commonMain sites before starting (the
counts above are from grep, not yet triaged into "must convert" vs "internal, leave as UiSlot"),
then apply the same compile-iterate-to-convergence technique, heaviest-consumer-first module
order (ui-headless, then ui-designsystem, engine/ui/ui-testing, game-dsl, samples, backends).

**Batch 3 -- lock it down (not started).**
1. Mark `UiSlot`'s constructor/class `internal` to `ui-core` once no downstream file references it.
2. Enforce mechanically: add `"UiSlot"` to `forbiddenUiTypeReferences` for
   `:awake:engine:ui:ui-headless` in
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
- [x] Batch 1 (`anchorSlot` params)
- [x] Batch 2 (widget return types, content-lambda params, semantic node bounds)
- [x] Resolved differently than planned Batch 3 (see below)

## Resolution (superseded plan)

`UiSlot` and `UiBounds` turned out structurally identical (`x`/`y`/`width`/`height`, both
immutable `val`) -- the only real distinction was the intended public/internal split, which the
two-type-plus-converter design enforced by convention, not by the compiler. Once Batch 2 finished
converting the public widget surface to `UiBounds`, the remaining `UiSlot` was renamed into
`UiBounds` directly (merged into a single `ui-core` public type in the `layout` package) instead
of proceeding with the originally planned Batch 3 (`internal`-lock `UiSlot` +
`forbiddenUiTypeReferences` Gradle enforcement). This eliminates the `toBounds()`/`toSlot()`
conversion boundary and the dual-type maintenance cost entirely, at the cost of no longer having a
compiler-enforced internal/public split -- accepted as the better tradeoff since the split wasn't
buying meaningful safety once the public surface was already narrowed. The Hard Rule above (rule
6 in `docs/reference/ui-ownership.md`) is now moot and should be removed/updated to reflect the
merged type.
