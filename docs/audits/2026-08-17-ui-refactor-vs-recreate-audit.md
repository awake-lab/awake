# UI refactor plan — 2026-08-17

**Verdict: refactor in place. Do not recreate.** Recreate only 3 small units
(`shadcnEmpty`, `shadcnTabs`, `ShadcnComponentStyles`, ~170 lines total) plus ~800 lines
of pure dead-code deletion.

Source: four parallel audits (ui-core, headless, designsystem, cross-cutting) over
`awake/ui/*`. Full evidence matrix in the [appendix](#appendix--full-findings-matrix);
each package below references its rows (A1, B2, …).

**Freshness (verified against HEAD `1cc85482`, 2026-08-17):** that commit ("retire
UiComponentStyles, kill headless's ambient theme fallback") pre-completed part of
package 4 — the `UiComponentStyles`/`CoreUiComponentStyles` defaults tier and
`theme.components.*` reads are already deleted (survivors are comments), and F5 +
the E5 scratch test are already gone. Some audit line numbers for that file family
came from stale `build/` mirrors; re-verify file:line before starting any row, and
exclude `**/build/**` from audit greps.

## TL;DR

- **The dirt accumulated because the guardrails were off, not because the architecture
  is wrong.** `verifyUiOwnership` has been a green no-op for headless + designsystem
  since a module rename (path typo in build-logic). ~54 violations piled up unseen.
- **Nothing scored complexity 5.** Every finding is a deletion, rename, Gradle string,
  or bounded sweep. The measure pass, `Style` resolver, and `UiLocal` machinery are
  sound and carry the project's best test coverage.
- **Recreate burns 524 tests + 89 goldens + the real-browser CSS oracle + the OKLCH
  token pipeline** — none of it transfers — then rebuilds the same workarounds against
  the same missing primitives.
- Effort: **~3–4 focused weeks across 6 packages, in order.** Each unblocks the next.
  Packages 1–3 ≈ one week, all low risk.

## Expected end state

What the stack looks like when all 6 packages land:

1. **One style channel.** Every visual property on every widget is set through
   `style: Style` and nowhere else. A variant is one `Style` value with state blocks
   (`hovered {}`, `selected {}`, `disabled {}`). The merge-order bug class (the
   2026-08-15 P0) is structurally impossible, not patched.
2. **One widget contract.** Every leaf widget:
   `(id, <state>, modifier, style, enabled, <callbacks>, content): UiBounds`.
   Learn one widget, know all of them. No sizing/spacing params — `Modifier` and
   `Style` own that, always.
3. **Single-responsibility leaves.** headless = leaf behavior only (dead composites
   deleted, live ones either moved or justified); designsystem wrapper = variant→Style
   mapping only, zero structural logic. Exactly one implementation per component.
4. **Layout without hacks.** Wrap-content parents size correctly around fillMax
   children; button group / toggle group are plain `row`/`column` with per-corner
   radii; the 4 workaround species are deleted.
5. **Guardrails that fire.** `verifyUiOwnership` and the designsystem audits actually
   run and fail on violations; unclassified modules break the build; the ownership doc
   describes the real module set. Dirt of every class found here cannot silently regrow.
6. **Tests guard the public API.** Facade-level widget tests + a style-precedence test
   gate every future sweep; goldens stop being Awake-vs-Awake tautologies.
7. **Smaller codebase.** ~800 lines dead code out, plus dedupe (Row ×5, 14 style
   re-impls, sidebar pair, icon registry) — net commonMain shrinks roughly 10–15%
   while behavior is preserved or fixed.
8. **Bug classes retired structurally:** merge-order colors, widget-id collisions,
   half-px seams (one `pixelPerfectPixel`), tooltip shared-state instability,
   trial-pass state corruption.

Not in scope of the 6 packages (parking lot): facade-mirror deletion, package-root
rename, typed px space, density globals, parity features (focus ring, popover panel,
checkmark, arrow). Those start from clean ground afterwards.

Snippet convention: `before` = in the tree today (file:line real). `after` = proposed
shape, not applied code.

---

## Package 1 — guardrails on (1 day) · rows A1 A2 A3

Everything else is only safe once the checks actually run.

- [x] Fix the 3 module-path literals in `build-logic/awake.ui-ownership-convention.gradle.kts`
- [x] Make unclassified modules fail instead of falling to `emptyList()` (`classifiedUiModules` guard)
- [x] ~~Wire `auditUiDesignsystemHeadlessBoundary` into `check`~~ — deleted instead: its rules
      moved into `verifyUiOwnership` (which is in `check`); one rule source, not two
- [x] Key `auditUiDesignsystemRecipeDuplicates` by file, not package — immediately caught the
      `shadcnEmpty` twin (B8), which was merged on the spot: `ShadcnEmptyRecipes.kt` +
      `ShadcnEmptyStyles.kt` deleted, `InspectorPanel.kt` migrated to the id-form; compile
      verified across designsystem/studio/ui-showcase
- [x] Delete `reportUiDesignsystemMigrationProgress` and the inverted `verifyUiDesignsystemClasspath`;
      also deleted the fragile commonMain include-filter (component naming audit already owns that rule)
- [x] Triage the surfaced violations — outcome: contract imports (`ui.style`, `ui.theme`, `ui.font`)
      legalized per the ownership doc's own licensing; runtime packages + `primitive.context` banned;
      `UiLocal`/`uiLocalOf` allowed via lookahead; 2 known offenders on the shrink-only
      `exemptUiSourcePatternFiles` ledger (`ShadcnButtonGroupRecipes.kt`, `ShadcnThemeLocals.kt`);
      task gained `exemptSourcePatternFiles` support. All 5 UI modules green with rules live
- [x] Update `docs/reference/ui-ownership.md` (real task paths, exemption ledger, ui-api de-facto
      note, stale `.copy()` note corrected, root-file count) + `awake/ui/README.md` (2 stale lines)

```kotlin
// before — awake.ui-ownership-convention.gradle.kts:16
when (project.path) {
    ":awake:ui:ui-core"         -> listOf(coreRules)
    ":awake:ui:ui-headless"     -> listOf(headlessRules)   // real path is :awake:ui:headless — never matches
    ":awake:ui:ui-designsystem" -> listOf(dsRules)         // never matches either
    else -> emptyList()                                    // both land here: green no-op
}

// after
when (project.path) {
    ":awake:ui:ui-core"      -> listOf(coreRules)
    ":awake:ui:headless"     -> listOf(headlessRules)
    ":awake:ui:designsystem" -> listOf(dsRules)
    else -> error("unclassified UI module: ${project.path}")   // rename can't disarm it again
}
```

**Done when:** `verifyUiOwnership` fails on a deliberately-planted violation in headless
and designsystem; violation list triaged into follow-up items.

---

## Package 2 — safety net (1–2 days) · row F10

Headless tests drive `UiPrimitiveScope`; the `UiScope` facade that designsystem actually
calls has near-zero coverage, and nothing pins style-merge precedence — the exact
mechanism of the 2026-08-15 P0. Without this, every later sweep stays green even when
wrong.

- [x] `StyleResolutionOrderTest` — 5 tests pinning the REAL semantics (see correction below)
- [x] Facade-level smoke tests — 17 new test methods: 11 new `*FacadeTest.kt` files +
      extensions to `ButtonEnabledTest`/`AvatarFallbackTest`; all drive public
      `headless.*` imports only; `separator` already had facade coverage
- [x] Cycle broken — headless test source sets no longer depend on designsystem: branded
      snapshot fixtures/tests moved to designsystem's test sources (all 16 pinned signature
      hashes matched byte-for-byte, zero re-record), neutral `UiSnapshotWriter` moved to
      `:awake:ui:testing` desktopMain, `UiCrossPlatformQualityTest` given a local neutral
      theme (inert — `button()` reads no ambient theme)
- [x] Fallout fix: `ShadcnAdoptionRecipeTest` missed by package 1's `shadcnEmpty` caller
      sweep (test sources weren't compiled then) — `id` added

**Semantics correction (matters for package 4):** the original sketch here asserted the
variant's unconditional `background(red)` wins at hovered. Reality (pinned by the test,
per `537d13c5`'s own doc + regression test): `resolve()` runs two passes over the whole
`then`-chain — all unconditional rules first, then all matching state rules — so **a state
rule outranks any unconditional, regardless of chain order**. `(defaults{hovered{gray}}
then variant{background(red)})` at hovered = **gray**. The 2026-08-15 P0 was fixed by
making defaults state-neutral, not by flipping precedence. Consequence: package 4's
defaults MUST stay state-neutral (state rules in a base style will always bleed through
variants' unconditional fills); `UiThemeTest`'s inverted invariants enforce this.

```kotlin
// pinned — StyleResolutionOrderTest.kt (actual shipping semantics)
@Test fun stateRuleOutranksLaterUnconditionalOverrideRegardlessOfThenOrder() {
    val defaults = Style { hovered { background(gray) } }
    val variant  = Style { background(red) }
    val composed = defaults then variant
    assertEquals(red, composed.resolve(state(hovered = false)).background)
    assertEquals(gray, composed.resolve(state(hovered = true)).background)  // state pass runs last
}
```

**Done when (met):** precedence pinned (5/5), 165/165 headless suite green without the
designsystem dependency, designsystem 131/132 (the 1 failure is the intentionally-red
package-5 wrap+fill spec), wasmJs test compiles green.

---

## Package 3 — delete + point fixes (2–3 days) · rows E1–E5, F1–F9, B4, B7

Pure subtraction, ~800 lines, zero design decisions.

Deletions (executed 2026-08-17, ~-708 net lines / 93 files / 13 files deleted):
- [x] Dead headless files/symbols — E1. Also took `MenuItem.kt` whole (incl.
      `intrinsicMenuWidthPx` — its only "user" was a KDoc link). SKIP: `provideTextStyle`
      — package 2's fixture migration gave it 6 live designsystem-test callers
- [x] ui-core zero-caller publics incl. `shadcnShimmer` + `UiApiCompatibility.kt` — E2.
      SKIP: `neutralSurfaceDefaults` (audit mischaracterized it — `internal` with 4 real
      callers). `textureQuad` KEPT — headless now delegates to it (user's camera-preview
      feature); stale TODO replaced with real doc. `UiApiCompatibility` fallout: 24 files
      (not 1) silently bound to the deprecated same-package aliases — all given real
      `ui.api` imports
- [x] `ShadcnIcons` registry + test initializer deleted; `ShadcnComponentContracts.kt`
      split by owner and deleted; `ShadcnAvatarSize` no longer freezes typography at
      enum-init; tautological `ShadcnCheckboxRadiusTest` deleted — E3
- [ ] ~~`UiButtonVariant` + `resolveFill`~~ **RE-PARKED into package 4**: package 2's
      fixture migration made `UiSnapshotFixtures` render 3 variant scenes through the enum
      (hash-pinned). Rewrite those scenes Style-based first, then delete — E4
- [ ] ~~`FigmaModeMatrix`~~ **RE-PARKED**: 3 live fidelity tests call it
      (Drawer/Select/Tooltip) — contradicts the memory-recorded deletion decision; delete
      with the Figma-tooling removal pass, after those tests get a neutral mode matrix — E5
- [x] `pixelPerfectPixel` 4→1 (graphics `UiBounds.kt`, `roundToInt` semantics; all imports
      fixed) — B4
- [x] Duplicate `@DslMarker` deleted — the marker mechanism now actually shadows across
      scopes for the first time; 3 latent implicit-receiver leaks surfaced and fixed — B7

Point fixes:
- [x] `shadcnAlertDialog`: `actions` slot now returns `UiAlertDialogAction?`, convenience
      buttons report Dismiss/Confirm through `shadcnButton` Outline/Primary; orphaned
      `shadcnDialogActionButtonStyle` deleted; sheet/drawer X wired to `onDismissRequest` — F1
- [x] `shadcnTooltip`/`shadcnTooltipText` `id` required-first; 2 call sites fixed — F2
- [x] Ids threaded: avatar badge/group, field separator, table cells now `"$id."`-prefixed
      (table scope was `internal` — zero external impact) — F3
- [x] Unguarded `WidgetState.rememberStateValue` overload deleted; test callers moved to
      the guarded `UiContext` overload — F4
- [x] ~~`Skeleton`/`ProgressBar` styled from `theme.components.slider`~~ — gone with
      the defaults tier in `1cc85482` — F5
- [x] Grip radius `2f` → `2f.dp.toPx()` — F6
- [x] All 8 `shadcnField*` helpers wrap in `shadcnField {}`; `FieldSet`/`FieldGroup` share
      one container helper (gap literals stayed at call sites — new per-preset `ShadcnMetrics`
      fields would be a design decision, not a dedup); file-wide suppressions → per-declaration
      (sized against real detekt output) — F7
- [x] `shadow`/`overlay` roles added to `ShadcnPalette`; scrim consolidated to one path at
      0.5 — **fixed a real bug**: drawer/dialog set `showScrim=true` but never `scrimColor`,
      so their scrim silently never drew. 7/10 `Sp` literals → typography tokens; 4 left
      commented (no matching token / needs signature change) — F8
- [x] `UiIcon.asVector()` unchecked cast → checked `when` with clear error — F9

Verification: ui-core/headless/testing suites green; headless wasmJs test-compile green;
studio + ui-showcase compile green; designsystem 131 run / 2 failed — the known package-5
spec + `ui-panel-controls` signature drift from F7's intentional container fix
(re-recorded after render review + user approval, dated note in the test file). F1's
alert-dialog drift under review at commit time.

```kotlin
// before — ShadcnPopupRecipes.kt:139: id defaulted on a STATE key; two tooltips share one bucket
fun UiScope.shadcnTooltip(..., id: String = "tooltip")
// after
fun UiScope.shadcnTooltip(id: String, ...)   // required, like every stateful widget
```

**Done when:** grep finds no symbol from the deletion list; F1–F9 each locked by the
package-2 tests or a one-line assertion.

---

## Package 4 — one style channel (~1 week) · rows B1 B2 B3

The core cleanup. One visual property gets exactly one place to live: `Style`.

- [x] ~~Delete the `UiComponentStyles`/`theme.components.*` defaults tier~~ — done by
      HEAD `1cc85482` before this plan started; survivors are comments — B1
- [x] Swept the unconditional theme-token picks in headless — most sites (Toggle,
      Dropdown, Textarea, ProgressBar) were already compliant from packages 1–3; real
      fixes landed in Checkbox/Switch/Slider/RangeSlider (resolved-first, token as
      fallback), TextField's focused-border, and `ResizablePanelGroup.handle()` (had
      no `style` param at all — added one) — B2, commit `451d2254`
- [ ] ~~Delete `resolveFill` + `UiButtonVariant`~~ — still re-parked: `UiSnapshotFixtures`
      renders 3 variant scenes through the enum, hash-pinned; unblocks once those
      scenes render Style-based — E4
- [x] `ShadcnComponentStyles` — confirmed already deleted by an earlier package. Its
      one real unmet goal, a canonical focus ring, didn't exist anywhere (zero
      `focused{}` rules in designsystem); added `shadcnFocusRing()` to
      `ShadcnInputStyles.kt`, composed into `shadcnTextFieldStyle`/`shadcnTextareaStyle`
      via `then` — B3, commit `a48be373`
- [x] Collapsed to `style =` alone: `button(content=)` already had it; deleted
      `DialogProperties.surface`, gave `dialog()` its own `style` param, threaded
      through `shadcnDrawer`/`shadcnDialog`/`shadcnAlertDialog` (both overloads);
      deleted `Radio.kt`'s dead implicit `Style{shape(9999f.dp)}` default (primitive
      already hardcodes the circle unconditionally) — B1, commit `a48be373`

```kotlin
// before — headless/Switch.kt:117 (same shape ×30 across 12 files)
val trackColor = if (checked) theme.colors.primary else theme.colors.muted
// unconditional token pick — no Style can ever re-color a checked track

// after — widget resolves a selected-state pass; token is fallback only
val resolved = style.resolve(state)               // state carries selected = checked
val trackColor = resolved.background ?: theme.colors.muted
// designsystem owns the color, in the one existing channel:
val shadcnSwitchStyle = Style { selected { background(colors.primary) } }
```

**Done when:** every visual property on every widget is reachable through `style =`;
`ShadcnStyleParityTest` + package-2 precedence tests green; no widget reads
`theme.colors.*` as an unconditional override.

---

## Package 5 — layout unlock (2–3 days) · rows D1 D2 B5

- [ ] Land the intrinsic wrap+fill fix at `ColumnScope.kt:94` / `RowScope.kt:81`
      (failing spec `ShadcnButtonGroupTest` already in tree) — D1
- [ ] Delete the 4 workaround species: `withIntrinsicLabelWidth` call sites where now
      redundant, `wrapContentWidthOrDefault()`, `minWidth` param, `withSizeFallback(40dp)` — D1
- [ ] Consume `UiShapeSpec.RoundedCorners` in button group (zero consumers today; stale
      "impossible" comment at `ShadcnButtonGroupRecipes.kt:95-103`); second consumer:
      toggle-group segments — D2
- [ ] Delete `LocalShadcnButtonGroup` context + triplicated `shape(0.dp)` mutations once
      the group is a plain row/column — D2
- [ ] Dedupe `Row.kt`'s 5 measure-block copies into one helper (Column's `smartColumn`
      pattern); fixes the `LocalCacheKey` drift bug — B5

```kotlin
// before — ui-core/layouts/ColumnScope.kt:94 (mirrored RowScope.kt:81)
context.recordMeasuredSlot(slot, contributesToWrapWidth = width != Dimension.FillMax)
// every fillMaxWidth() child excluded from wrap measure → vertical button group
// measures 0 wide, falls back to the 600px frame

// after — FillMax child still reports intrinsic size during a wrap-content pass
val effective = if (context.isWrapContentPass && width == Dimension.FillMax)
    Dimension.WrapContent else width
context.recordMeasuredSlot(slot, contributesToWrapWidth = effective != Dimension.FillMax)
```

**Done when:** `ShadcnButtonGroupTest` spec green; button-group capture PNG shows
labels, wrapped width, filled members, per-corner radii; regression surface
(`LayoutSizingMatrixTest` + 27 ui-core test files) green.

---

## Package 6 — uniform signatures (~1 week, mechanical) · rows C1–C8, C4, B8, E6, B12

Compiler-driven sweeps. Boring by design.

- [ ] Eliminate click/change callbacks from discrete-interaction widgets — return-value
      idiom (`if (button(id))`, `checked = checkbox(id, checked)`,
      `value = slider(id, value)`); one event contract, immediate-mode native — C9
- [ ] Gesture contract (C9 companion) — **immediate-mode core, Compose-like sugar**:
      - headless: every interactive widget returns one `UiInteraction` result
        (`clicked`, `doubleClicked`, `longPressed`, `hovered`, `pressed`, `focused`,
        `bounds`) from a single recognizer in ui-core's `interactiveSurface` — never
        per-widget timing code, never callbacks. Return values are trial-measure-safe
        by construction (a callback could double-fire during trial passes — the
        `isMeasuring` bug class that shipped 3×).
      - designsystem: thin Compose-familiar sugar allowed —
        `shadcnButton(onClick = {...})` = `if (button(...).clicked) onClick()`,
        documented synchronous-same-frame, wrapper adds zero behavior.
      - visuals: hover/press/focus reactions only via `Style` state blocks.
      - continuous gestures (drag, text) return the new value each frame from
        `WidgetState`-held gesture state. Long-press/double-click need the input-latch
        fix (F11) first.
- [ ] One canonical order everywhere:
      `(id, <state>, modifier, style, enabled, content): UiBounds` (callbacks gone per
      C9; continuous-gesture exceptions documented individually) — C3 C4
- [ ] `id: String` required on every stateful widget (kill `canvas` default, `separator`
      nullable-last, tooltip default); fold `semanticId` into `testTag` — C3
- [ ] Remove the 16 rule-6 params (`size`, `textSize`, `boxSize`, `thickness`, `radius`,
      `minWidth`, `gap`, …); delete `Modifier.margin()` (silently drops end/bottom) — C5
- [ ] `modifier` on all 8 overlay components; kill `Dimension` params + `FillMax` leak;
      replace hand-rolled text measurement with `withIntrinsicLabelSize` — C6
- [ ] Rename raw-slot `column`/`row` overloads to `columnAt`/`rowAt`; single 21-param
      factory, `UiContext.create*` become internal forwards — C1
- [ ] `button()` label form becomes a wrapper over the slot form (after package 4 — the
      prior attempt's regression was a B2 symptom) — C2
- [ ] One style-fn shape: `internal fun shadcnXStyle(values: ShadcnThemeValues, …): Style`;
      merge the 14 `foreground+textSize` re-implementations and twin surface styles — B8
- [ ] Merge the two `shadcnEmpty` implementations *(recreate unit)* — B8
- [ ] Recreate `shadcnTabs` with a content slot *(recreate unit, below)* — D5
- [ ] Split `scrollPanel` god function; enum axis instead of `"width"`/`"height"` strings — C7
- [ ] Repackage `internal/controls/Buttons.kt` (declares the public package from `internal/`) — C8
- [ ] Park or delete the 7 speculative presets (49 unverifiable positional Dp args) — E6
- [ ] Spacing-vocab sweep per the recorded decision (`Tw` in designsystem, `UiSpacing` in
      core/headless, delete `ShadcnSpacing`) — per-site, no bulk rename — B12

```kotlin
// before — shadcnTabs models only the track; content panel not expressible;
// index overload breaks on duplicate labels
fun UiScope.shadcnTabs(tabs: List<String>, selected: Int, ...): Int

// after — signature change makes the current body disposable
fun UiScope.shadcnTabs(
    id: String,
    items: List<UiTabItem>,
    selected: String,
    modifier: Modifier = Modifier,
    content: ColumnScope.(String) -> Unit,   // the missing panel
): String
```

**Done when:** signature lint (or the package-1 check, extended) passes; no rule-6
params remain; designsystem call sites compile against the canonical shapes.

---

## Parking lot — decide separately, not blockers

| Item | Row | What | Why parked |
|---|---|---|---|
| Delete headless facade mirror | B9 | `HeadlessModifier` (16 forwards) + second `UiScope`; `primitive` escape already public, 48 imports bypass the wall | Touches every widget signature; boundary is better enforced by package-1's live check + `implementation(ui-core)` |
| Package-root rename | B11 | 6 packages split across modules break `internal` + import checks | Fold into the planned `io.github.awakelab.*` rename — one breaking pass, not two |
| Typed pixel space | D7 | `value class Px` at pointer/bounds boundary; the dp-vs-px 2× drag class is unguarded | Wide, mechanical, best after signatures settle |
| Density/global state | B10 | `UiDensity.scale` + `UiShape.base` mutable globals | Multi-window blocker, not a today problem |
| Parity features | D3 D4 D6 | Focus ring, popover container panel, checkmark, tooltip arrow, indeterminate visual, input-group weight | Additive; same cost whenever done — cleanest after packages 4–5 |
| Naming lexicon | P2 — **enforcement landed 2026-08-17**: 3 lexicon patterns live in `verifyUiOwnership` (emit/paint/render scope-extensions frozen to exempted legacy files, PascalCase providers banned — the 3 live ones renamed to camelCase, new `*Slot` twins banned with `claim*` excluded); bulk renames remain package-6/B11 work | Render path speaks 4 verb dialects (`emit*` dead, `paint*`, `draw*`, `render*`) with no layer rule; twin nouns (`DrawPrimitive`/`UiDrawPrimitive`, `Bounds`/`UiBounds`, `Alignment`/`Insets` aliases); shape drift (`ProvideCacheKey` PascalCase fn vs `provideTextStyle`, `visuals()` vs 47 `*Style()`, `with*` meaning both lambda-scoped and value-returning). Lexicon: `draw*` = UiDrawScope painting members only; `render*` = backends only, banned in ui modules; `emit*` banned; `measure*`/`resolve*`/`claim*` = ui-core pass verbs; `remember*` = state hooks; `with*` = lambda-scoped only, value-returning transforms get participle/noun names (the core-math `normalize()`/`normalized()` contract); one name per concept — twins die with B11's package rename; enforcement via name-pattern bans in the live ownership check | Decide with P1; mechanical renames land inside package 6's sweeps (C-rows already cover `visuals`→`shadcnButtonStyle`, `*Slot` deletion) |
| Capability-scoped receivers | P1 | "Extension hell" root cause: `UiPrimitiveScope` is one god receiver (frame+layout+draw+input+state+theme), so every capability accretes as a floating extension — painting helpers scatter (`scrollPanel` inlines scrollbars, `Checkbox` inlines its mark), completion shows the whole engine, re-export layers multiply (B6/B9), same-name receivers mis-resolve (C8). Fix: Compose's model — a small member-based `UiDrawScope` (fillRect/roundRect/path/texture/textRun) handed out only by `draw {}`/`paintSurface`, `resolveStyle→paintSurface` as the ONLY widget-chrome path (D3's ring lands there), `UiPrimitiveScope` shrunk to composition (slots/state/locals), and an ownership-check ban on new `UiPrimitiveScope.draw*`/`emit*` extensions outside the draw layer | Architectural; design after B6/B9 delete the re-export layers — most of the "hell" is those plus B7's marker split, so measure again post-package-6 before committing to the split |

```kotlin
// before — headless/Layout.kt:34: mirror type whose wall is half-built
interface Modifier
internal data class HeadlessModifier(val primitive: PrimitiveModifier)
fun Modifier.width(w: Dp): Modifier = HeadlessModifier(asPrimitiveModifier().width(w))  // ×16 forwards
// meanwhile UiScope.primitive is public (designsystem uses it 6×) and Style/TextStyle/
// UiLocal/Dimension cross unwrapped via 48 direct imports

// after — mirror deleted; boundary enforced by the live check + dependency scope
fun UiScope.button(id: String, modifier: UiModifier = UiModifier, style: Style = Style.Empty, ...)
```

---

## Appendix — full findings matrix

Four audits, one row per consolidated finding. Complexity: 1 = mechanical, hours ·
2 = a day · 3 = days, cross-file · 4 = week+, cross-module · 5 = architectural (none
found). Verdict `recreate` = that unit is cheaper rewritten than patched.

### Why refactor wins (evidence summary)

1. The rot is peripheral; the load-bearing engine (measure pass, `Style` resolver,
   `UiLocal` stacks, glyph layout, drag conservation) is coherent and carries
   named-after-the-bug regression tests.
2. Enforcement was silently off (A1/A2); dirt regrowth is preventable, not inherent.
3. The 2026-08-15 five style channels are already three — the consolidation path is
   proven on this codebase, locked by tests.
4. 524 tests + 89 goldens + 26-case CSS oracle + OKLCH pipeline are non-transferable
   sunk value; a recreate re-earns those bugs at full price and hits the same missing
   primitives.

### A. Dead guardrails

| # | Dirt | Where | Suggestion | Verdict | Cx |
|---|---|---|---|---|---|
| A1 | `verifyUiOwnership` disarmed for headless + designsystem by module-path typo; ~54 violations hidden | `build-logic/awake.ui-ownership-convention.gradle.kts:16,31,47` vs `settings.gradle.kts:41-42` | Fix 3 path strings; fail-on-unclassified; triage | refactor | 1 (+3 triage) |
| A2 | Designsystem audit tasks dead: boundary task not in `check`; classpath check asserts opposite of description; migration report reads deleted path (always 100%); duplicates check keyed by package = no-op; source filter silent no-op | `designsystem/build.gradle.kts:20-26,78-91,162,216-218,249` | Rewire, delete stale, key by file | refactor | 1 |
| A3 | Ownership doc names nonexistent `ui-api`/`ui-dsl` modules, stale task paths, stale `.copy()` sweep note | `docs/reference/ui-ownership.md:79,82,313-334,46-48,234` | Doc update; declare `awake:ui:graphics` the de-facto contract module | refactor | 1 |

### B. Redundancy

| # | Dirt | Where | Suggestion | Verdict | Cx |
|---|---|---|---|---|---|
| B1 | Live style channels beyond `Style`: `UiComponentStyles` defaults tier (16 headless sites), `resolveFill`, designsystem's 7 delivery paths (`style=`, theme default, `styleable`, `then`, `DialogProperties.surface`, omission, non-Style params) | `ui-core/theme/UiComponentStyles.kt`, `headless/internal/controls/Buttons.kt:67,248`, designsystem-wide | Collapse to `style =` alone | refactor | 3 |
| B2 | 6th undocumented channel: 30 unconditional theme-token picks no `Style` can override | `Checkbox.kt:94-95,127,190`, `Switch.kt:117,148`, `Slider.kt:112,131-132`, `RangeSlider.kt:183,203-204`, `Toggle.kt:53-57`, `Dropdown.kt:61-62,119-120,177`, `TextField.kt:96,98,273`, `Textarea.kt:97,280`, `ResizablePanelGroup.kt:222,232,255`, `ProgressBar.kt:57` | Selected-state slots in `Style`; token = fallback | refactor | 3 |
| B3 | `ShadcnComponentStyles` competes with recipe styles on 9 widgets; owns the module's only `focused {}` rule — focus ring existence depends on merge order | `designsystem/ShadcnComponentStyles.kt:18-76` | Rewrite token-only + `shadcnFocusRing()` fragment | **recreate** | 3 |
| B4 | `pixelPerfectPixel` ×4, 2 tie-rounding rules — border and icon snap .5px opposite ways | `ui-core/scope/UiScopeMetrics.kt:25`, `ui-core/api/layout/LayoutValues.kt:80`, `graphics/UiDensity.kt:31`, `graphics/api/layout/UiBounds.kt:27` | Keep one (`roundToInt` semantics), delete 3 | refactor | 1 |
| B5 | `Row.kt` 50-line measure block ×5, drifted: only `AbsoluteScope.row` reads `LocalCacheKey` — and passes the wrong key downstream | `ui-core/layouts/Row.kt:25-332` | One helper + 4 thin wrappers (Column's pattern) | refactor | 2 |
| B6 | `UiContext` 821 lines: 31 `@Deprecated` one-line forwards re-exposed 3–4×; ui-core calls its own deprecated layer from 15 sites | `ui-core/context/UiContext.kt`; callers in `Row.kt`, `Column.kt`, `Surface.kt` | Re-point 15 callers, delete the mirror in one commit | refactor | 3 |
| B7 | Two `@DslMarker AwakeUiDsl` — headless `UiScope{}` does not shadow enclosing `UiPrimitiveScope` | `ui-core/UiDslMarker.kt:9` vs `headless/UiScope.kt:19` | Delete headless copy | refactor | 1 |
| B8 | Designsystem dupes: 2 shipping `shadcnEmpty`; 14 `foreground+textSize` re-impls; byte-identical dialog surface styles; 45-line sidebar pair; `FieldSet`/`FieldGroup` identical modulo one Dp; 8 style-fn signature shapes | `ShadcnEmptyRecipes.kt:25` vs `ShadcnStatusRecipes.kt:141`; `ShadcnFieldStyles.kt`, `ShadcnPopupStyles.kt:45,52-53`, `ShadcnSidebarRecipes.kt:61-151` | Merge/delete/extract; one style-fn shape | recreate (empty) / refactor | 2 |
| B9 | Headless facade wall half-built: `HeadlessModifier` mirror + second `UiScope`; `primitive` escape public (used 6× by designsystem); 48 unwrapped imports bypass it | `headless/Layout.kt:34-40`, `headless/UiScope.kt:24-29` | Delete mirror; enforce via check + dependency scope | refactor | 4 |
| B10 | Mutable process globals: `UiShape.base` (second shape system beside `UiTheme.shapes`), `UiDensity.scale`/`fontScale` | `ui-core/style/UiShape.kt:24`, `graphics/UiDensity.kt:13,18` | `val`; density into context long-term | refactor | 2/4 |
| B11 | 6 packages split across module pairs — `internal` and import checks structurally can't work; `heroicons` package missing `.awake` | `ui`, `ui.api`, `ui.api.layout`, `ui.graphics`, `ui.scope`, `ui.theme` | Unique roots; fold into awakelab rename | refactor | 3 |
| B12 | 3 spacing vocabularies (decision recorded, sweep pending — must be per-site: bulk rename would launder 4 known-wrong values) + 5 unit vocabularies in designsystem | `ui-status.md` risk 5; `ShadcnInputOtpRecipes.kt:40-49`, `ShadcnVariants.kt:26,64` | Per-site sweep; `Dp` on all metric fields | refactor | 2 |

### C. Overloading / non-uniform API

| # | Dirt | Where | Suggestion | Verdict | Cx |
|---|---|---|---|---|---|
| C1 | `column` ×12 entry points incl. 20/21-param twins; silent-wrong-overload trap documented in-tree ("infinite trial / OOM") | `UiContext.kt:125,148,257`, `UiLayoutFactory.kt:33`, `NestedLayouts.kt:21`, `Column.kt:282-395`, `UiScopeNesting.kt:19` | `columnAt`/`rowAt` renames; internal factory forwards | refactor | 2 |
| C2 | `button()` label vs slot form: opposite sizing (wrap vs fill) and opposite theming (reads theme vs reads nothing), one name | `headless/Button.kt:25-42` vs `:85-114` | Label form wraps slot form (after B2) | refactor | 2 |
| C3 | Headless signatures: `style` position ×8 variants; `id` ×5 conventions (canvas default, separator nullable-last with in-code collision note); `semanticId` = 4th identity param; `disabled` polarity inverted on `field`; disabled alpha open-coded ×6 | headless-wide; `Text.kt:36`, `Canvas.kt:111`, `Separator.kt:21-23` | Canonical order; id required; one `disabledAlpha{}` | refactor | 3 |
| C4 | Designsystem signatures: `id` absent ~20 / optional 7 / defaulted 2; 9 `Unit` returns (can't anchor popups); 9 rule-6 params; FQNs in signatures; 8 unexplained `public` style fns | designsystem-wide | One mechanical pass | refactor | 2 |
| C5 | Headless param bloat: 16 sizing/spacing params duplicating Modifier/Style; `Modifier.margin()` silently discards `end`+`bottom`, zero callers | `Avatar.kt:15-30`, `Selection.kt:17`, `Separator.kt:18`, `Buttons.kt:43-216`, `Layout.kt:113-124` | Delete/move to modifier (~20 call sites move) | refactor | 2 |
| C6 | 8 overlays take `Dimension`/`Dp`, none has `modifier`; `Dimension.FillMax` in public API (banned); hand-rolled text measurement with magic 40/80/128 | `ShadcnPopupRecipes.kt:63-64,131-132,182-185`, `ShadcnOverlayRecipes.kt:82,128,133-134` | `modifier` on all 8; `withIntrinsicLabelSize`; also fix `popup()` min/max bounds (ui-status risk 7) | refactor | 3 |
| C7 | `scrollPanel` 285-line god function; stringly axis `"width"`/`"height"` beside real enums | `ui-core/ScrollContainers.kt:49-333` | Split axis fns; extract scrollbar paint + viewport geometry | refactor | 2 |
| C8 | `internal/controls/Buttons.kt` declares the public package from `internal/`; `internal.*` packages not Kotlin-`internal` | `headless/internal/controls/Buttons.kt:3` | Repackage; real `internal` after F10 | refactor | 1 |
| C9 | Mixed event idioms: `button` returns a result (immediate-mode native), `toggle`/`checkbox`/`switch`/`slider` take `onCheckedChange`/`onValueChange` callbacks (retained-mode idiom), some expose both — three contracts for "did the user interact" | headless `Selection.kt`, `Switch.kt`, `Slider.kt` vs `Button.kt` | Eliminate discrete-event callbacks: interaction returns the new state (`if (button(id))`, `checked = checkbox(id, checked)`, `value = slider(id, value)`); callbacks only if a continuous gesture genuinely needs one | refactor | 3 |
| C10 | Redundant overloading beyond C1/C2: parallel `*Slot` entry points (`buttonSlot`, `toggleSlot`) shadow the widget's own slot form — overloading by name; designsystem twin overloads of one recipe use different style mechanisms (`shadcnButton` `styleable` at :64 vs `then` at :91); avatar/toggleGroup dual forms | headless entry-point list (`ui-component-coverage.md`), `ShadcnButtonRecipes.kt:64,91` | One primary slot form + at most one string-convenience wrapper per widget (wrapper delegates, never re-implements); delete `*Slot` names; twin overloads share one mechanism | refactor | 2 |

### D. Missing primitives

| # | Dirt | Where | Suggestion | Verdict | Cx |
|---|---|---|---|---|---|
| D1 | Intrinsic wrap+fill fix not landed; failing spec in tree; 4 workaround species downstream | `ui-core/layouts/ColumnScope.kt:94`, `RowScope.kt:81`; spec `ShadcnButtonGroupTest.kt:42` | Two-line fix, delete workarounds | refactor | 3 |
| D2 | `UiShapeSpec.RoundedCorners` landed (fe90a848), zero consumers; stale "impossible" comment steering work wrong | `graphics/UiPath.kt:231` vs `ShadcnButtonGroupRecipes.kt:95-103` | Consume in button group (after D1), toggle group next | refactor | 2 |
| D3 | No focus ring anywhere; `ring` token exists and is tested, zero draw sites | token `api/theme/UiThemeValues.kt:42` | One `ResolvedStyle` ring field + one draw in `paintSurface` | refactor | 2 |
| D4 | Select/combobox popover has no container panel style hook ("page shows through gaps") | `headless/Dropdown.kt:19-39` | `menuStyle: Style` + surface behind options | refactor | 2 |
| D5 | `shadcnTabs` no content slot; lossy index overload; no real headless `tabs()` | `ShadcnNavigationRecipes.kt:138-180` | New content-slot signature | **recreate** | 3 |
| D6 | Parity gaps: selected checkmark, tooltip arrow, indeterminate visual (renders as checked), input-group affix starvation, string-only table cells, dialog X unwired | designsystem agent finding 19 | Additive; several need headless hooks first | refactor | 3 |
| D7 | Untyped px space (2× drag class unguarded); magic `4096f` ×9; `verifyUiAuthoredUnits` only catches literal `.px` | `UiContext.kt:810-815`, `UiSliderMath.kt:6` | `value class Px`; named `UNBOUNDED_AXIS_PX` | refactor | 4/1 |

### E. Dead code / stranded

| # | Dirt | Where | Suggestion | Verdict | Cx |
|---|---|---|---|---|---|
| E1 | 5 dead headless files (~330 lines) + 6 symbols; pattern: every composite bypassed by its `shadcn*` counterpart (tooltip's dropped hover timer = the instability symptom) | `Accordion.kt`, `Field.kt`, `NumberField.kt`, `Tooltip.kt`, `UiEdge.kt`, `menu()`, `tabs()`, `ActionRow.kt` | Delete | recreate (delete) | 1 |
| E2 | 12 zero-caller ui-core publics incl. `shadcnShimmer` (brand in core); `UiApiCompatibility.kt` alive only via same-package implicit resolution | `StyleModifiers.kt:13`, `TextureQuad.kt:14`, `Surface.kt:49`, `UiApiCompatibility.kt` | Delete ~130 lines; add `\bshadcn[A-Z]` pattern to core/headless check | refactor | 1 |
| E3 | `ShadcnIcons`: 32 `lateinit` globals + public re-runnable init for a dependency cycle deleted in `a0b71c93` | `ShadcnComponentContracts.kt:15-94` | Delete registry; direct `HeroIcons.*`; split the rest of the audit-exempt contracts god-file; fix `ShadcnAvatarSize` freezing typography at class-load | refactor | 1 |
| E4 | `UiButtonVariant.Filled/Outline/Ghost` + `resolveFill`: branded vocab in headless, zero production callers | `headless/internal/controls/Buttons.kt:231-256` | Delete; behavior moves to `Style` | refactor | 1 |
| E5 | `FigmaModeMatrix` (deletion already decided in memory) + uncommitted scratch probe test | `testing/ui/FigmaModeMatrix.kt`, `designsystem/desktopTest/.../ScratchAlertDialogProbeTest.kt` | Delete both | refactor | 1 |
| E6 | 7 of 8 theme presets speculative: 49 unverifiable positional `Dp` args, 7.4K test pinning numbers with no oracle | `ShadcnTheme.kt:28-83` | Keep Vega; park or delete rest; named args if kept | refactor | 2 |

### F. Correctness bugs found in passing

| # | Dirt | Where | Cx |
|---|---|---|---|
| F1 | `shadcnAlertDialog` result `action` never assigned; both button returns discarded; dialog X drawn but not wired to dismiss | `ShadcnPopupRecipes.kt:187-222`, `ShadcnOverlayRecipes.kt:103,143` | 2 |
| F2 | Tooltip `id = "tooltip"` default — two tooltips share one `WidgetState` bucket (frame-instability root cause) | `ShadcnPopupRecipes.kt:139,167` | 1 |
| F3 | Hardcoded colliding widget ids | `ShadcnAvatarRecipes.kt:37,49`, `ShadcnFieldRecipes.kt:118,124`, `ShadcnTableRecipes.kt:39,43` | 1 |
| F4 | `WidgetState.rememberStateValue` omits `isMeasuring` guard the same file documents as shipping 3× | `ui-core/state/UiStateHooks.kt:82-85` | 1 |
| F5 | `Skeleton` + `ProgressBar` styled from `theme.components.slider` | `Skeleton.kt:44`, `ProgressBar.kt:43` | 1 |
| F6 | Grip radius `2f` raw px — half-size at dpr 2; `handle()` has no style param | `ResizablePanelGroup.kt:255-256` | 1 |
| F7 | 8 `shadcnField*` helpers emit label+widget as siblings, no container — bug class already fixed+documented in `shadcnRadioGroup` | `ShadcnFieldRecipes.kt:131-257` | 2 |
| F8 | Color/type literals outside theme files: shadows, scrim 0.48 (shadcn 0.5) + second scrim mechanism, 9 `Sp` literals bypassing typography | `ShadcnCardStyles.kt:21`, `ShadcnOverlayRecipes.kt:87`, `ShadcnSidebarStyles.kt` | 1 |
| F9 | `UiIcon.asVector()` unchecked cast | `headless/Icon.kt:11` | 1 |
| F10 | Test suite tests the layer below the public API; no merge-precedence test — the safety-net gap gating all sweeps | headless commonTest | 2 |
| F11 | ~~Pointer pipeline stores latest state only~~ — **done**, commit `42a3180e`. Root cause was the shared `awake:core` `Input` class, not `bindWindowPointerInput` alone — fixed there so every platform bridge (wasm/GLFW/Android/iOS) inherited the fix at once. Added `pointerPressed`/`pointerReleased` edges to `InputSnapshot`, mirroring the existing `keysPressed`/`keysReleased` pattern | `awake:core/input/Input.kt` | 2 |
