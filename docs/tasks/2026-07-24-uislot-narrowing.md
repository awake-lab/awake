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
ui-unstyled, ui-dsl, ui-designsystem, engine/testing, game-dsl, samples, and backends.

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

## Implementation Order

1. Add `UiBounds` (`ui-core`, `layout` package) with `x`/`y`/`width`/`height`, `.place(...)`, and
   `.inset(...)` -- ports of the existing `UiSlot` extensions, same logic.
2. Change public-facing return types (`surface{}`, `row{}`, `claimSlot()`,
   `UiSemanticNode.contentBounds`/`bounds`) from `UiSlot` to `UiBounds`.
3. Add `UiSlot.toBounds()` / `UiBounds` conversion at the ui-core measurement/output boundary.
4. Fix downstream call sites module by module (ui-unstyled first -- heaviest consumer -- then
   ui-dsl, ui-designsystem, engine/testing, game-dsl, samples, backends), same
   compile-iterate-to-convergence technique used for the layout/style package move.
5. Mark `UiSlot`'s constructor/class `internal` to `ui-core` once no downstream file references it.
6. Compile whole tree (0 errors), run `desktopTest`, confirm baseline: scene-dsl=1, ui-showcase=6,
   ui-dsl=3, all other modules 0 (ui-designsystem baseline is now 0 per the button-label fix).
7. Enforce the rule mechanically so it can't regress: add `"UiSlot"` to
   `forbiddenUiTypeReferences` for `:awake:engine:ui-unstyled` and `:awake:engine:ui-dsl` in
   `build-logic/src/main/kotlin/awake.ui-ownership-convention.gradle.kts` (the existing
   `verifyUiOwnership` check already supports this via `forbiddenTypeReferences` -- just needs
   `UiSlot` added to those two modules' lists, kept separate from `ui-core`'s list since `UiSlot`
   is legitimately defined there). Do this last, only once step 4 has zero remaining downstream
   `UiSlot` references, or `check` will fail immediately.

## Hard Rule (in effect now, enforced once implementation lands)

`UiSlot` is `ui-core`-internal. No module outside `ui-core` may construct, read, or `.copy()` a
`UiSlot`. Anything crossing the `ui-core` boundary must use `UiBounds` instead. Recorded in
`docs/reference/ui-ownership.md`'s Hard Rules list (rule 6).

## Status

- [x] Contract audit
- [x] Rule recorded in `docs/reference/ui-ownership.md`
- [ ] Implementation (not started)
- [ ] Mechanical enforcement (step 7, blocked on implementation)
