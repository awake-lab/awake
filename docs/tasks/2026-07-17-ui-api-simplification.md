# 2026-07-17: UI API Simplification

## Goal

Reduce public UI API sprawl so Awake's reusable UI stack reads more like a real library and
less like a pile of convenience wrappers.

## Why This Matters

The current UI foundation is stronger than it was a week ago, but the authored API surface
is still wider than it needs to be:

- public DSL and design-system entry points still expose too many raw `Float` sizing helpers
- `Dp` and `Sp` exist, but authored code can still drift back to `.px` too easily
- design-system wrappers repeat the same `UiScope` and `UiDslScope` logic with only minor
  receiver differences
- `UiShellDsl` and `UiDslLayout` still carry too many overlapping convenience shapes
- the testing layer is still mostly structural, not semantic

This makes the code harder to teach, harder to evolve, and too easy to use in the wrong way.

## Current State

- `ui-core` already owns the right primitives: `Dp`, `Sp`, `Dimension`, `UiModifier`,
  `Style`, `UiTheme`, drawing primitives, popup contracts, and font/runtime pieces.
- `ui-unstyled` is mostly in the right place as a generic leaf-widget layer.
- `ui-dsl` owns the right category of APIs, but its surface is too broad.
- `ui-designsystem` owns the right category of APIs, but it still duplicates too many
  wrapper entry points and still tolerates raw-float authored sizing.
- `engine/ui/ui-testing` can catch some overlap and drift, but it still cannot reason about
  semantic roles, text-fit expectations, or layout intent strongly enough.

## Policy: Slot API vs `UiSlot` vs Modifier-First Input (2026-07-24)

Three different things, easy to conflate under "get rid of slots":

- **Slot API (keep, everywhere, including base components) -- a composition pattern, not a
  type.** Components take their content as lambda parameters -- a trailing `content: Scope.()
  -> Unit`, or named slots like `shadcnSectionHeader(title = { ... }, description = { ...
  })`. This is unrelated to the `UiSlot` class; it's about *how content is composed into a
  component*, the same shape as Compose's slot API. `ui-unstyled`/`ui-designsystem` base
  components must **not** be decoupled from this -- don't replace slot-lambda composition with
  something else (e.g. don't collapse a `title`/`description` slot pair into a single
  `label: String` param just to shrink the signature -- see the button-label bug, which is
  exactly this mistake in the other direction: a param that should have stayed a slot).
- **`UiSlot` (keep as measured output/internal layout data).** The `x`/`y`/`width`/`height`
  data class a container hands back after measuring -- via a slot-API lambda parameter
  (`surface(...) { slot -> ... }`), a return value (`claimSlot(...): UiSlot`), or a semantic
  field (`UiSemanticNode.contentBounds`). A slot lambda receiving a `UiSlot` is incidental --
  it's still Slot API even when the lambda receives nothing, or receives something else.
- **Modifier-first input (the actual cleanup target).** Raw `x`, `y`, `width`, `height`,
  `insets`, `gap` as *authored* parameters -- values a caller hand-types to place/size
  something -- must go through `UiModifier` (`.offset()`, `.width()`, `.height()`, `.padding()`,
  arrangement instead of `gap: Float`) or `Style` (`shape()`, `borderWidth()` instead of a
  parallel `radius`/`borderWidth` param). Applies at every layer: root `UiContext.column/row/
  box/absolute` calls, base-component widget signatures in `ui-unstyled`/`ui-designsystem`, and
  `ui-core`'s own public surface (`surface()`'s `radius`/`borderWidth` were the same mistake one
  layer down -- see the Core-UI audit finding below).

**Checklist before adding any new widget param (found via `avatarFallback`'s `diameter: Dp`,
2026-07-24):** the anti-pattern isn't limited to params literally named `width`/`height`/`gap`.
Any authored param that's really a size, position, or spacing value under a different name
(`diameter`, `radius` used as a dimension rather than `Style.shape()`, `length`, `thickness`,
`spacing`, ...) is the same violation just spelled differently. Before adding a `Dp`/`Float`
param to a widget signature, ask: *"could `Modifier.size()`/`.width()`/`.height()`/`.padding()`
or `Style.shape()`/`.borderWidth()` already express this?"* If yes, use that instead of a new
param -- don't rely on the param's literal name matching a denylist, since names vary and no
generic grep/lint can catch every synonym. This is a manual gate (code review / self-check when
authoring a widget), not a mechanical one.

In short: content composition stays Slot API; measured/composed output stays `UiSlot`; authored
input goes through `UiModifier`/`Style`. The root-level sweep below finished the *root* half of
the modifier-first half; the base-component widget-signature half is done too (see Core-UI
audit finding).

## UiSlot As Root Authored Input (2026-07-24)

Same problem, one layer down: modifier-first sizing was covered above, but root-level
`UiContext.column/row/box/absolute/create*` calls still accepted a hand-authored `UiSlot(x, y,
w, h)` as the normal way to place a page. `UiSlot` should be measured output/internal layout
data, not something a caller constructs by hand at the root.

- [x] Migrate every root-level `column(slot = UiSlot(...))` / `createColumn(x, y, width)` /
  `createAbsolute(x, y)` / `createBox(x, y, w, h)` call site onto `modifier = Modifier
  .offset(x.dp, y.dp).width(w.dp).height(h.dp)` across `ui-dsl`, `ui-designsystem`,
  `ui-unstyled`, and `samples:ui-showcase` (~80 call sites, ~20 files). Verified pixel/geometry
  parity per site (offset+TopStart reproduces the old `UiSlot` exactly) and confirmed zero
  regressions against `main` via `git stash` diffing at every batch.
- [x] Fixed `ui-designsystem`, which did not compile on `main` at all — 3 test files called a
  `column(x=, y=, width=)` signature that didn't exist anywhere in the codebase (stale from an
  earlier refactor). First time this module has ever run its test suite.
- [x] Deleted the now-dead deprecated overloads once every caller was confirmed migrated:
  `UiLegacyCompat.kt`'s `createAbsolute(x,y,...)` / `createColumn(x,y,width,...)` /
  `column(x,y,width,...)` / `createBox(x,y,w,h,...)`, and `UiContextCompat.kt` /
  `RootLayouts.kt`'s slot-based `createColumn(slot=)` / `createAbsolute(slot=)` / `column(slot=)`
  / `row(slot=)` / `box(slot=)` / `absolute(slot=)`.
- [ ] `UiSlot` itself is still a public type (return type of `surface{}`/`row{}`/`claimSlot()`,
  field on `UiSemanticNode.contentBounds`, etc.) — cannot be made `internal` without first
  giving measured-output consumers a narrower read-only view type. Not attempted this pass;
  needs its own scoped task if wanted.
- [ ] Nested/computed `UiSlot` construction (`box.column(slot = UiSlot(...))` in `LayoutTest.kt`,
  `AnimatedLayoutScopes.kt`'s `createColumn(slot, gap = ...)`) intentionally left alone — these
  consume a slot measured by a parent, not hand-authored magic numbers, matching the
  measured-output carve-out above.
- [x] **Core-UI audit (2026-07-24), found by manual review after the first pass** — the
  base-component sweep above (step 8) only covered `ui-unstyled`/`ui-designsystem` call sites,
  not `ui-core` itself, where `surface()` actually lives. `layouts/ext/Surface.kt`'s 5
  `surface()` overloads (plus their `UiLegacyCompat.kt` mirror) took `radius: Dp` and
  `borderWidth: Dp` as separate authored params *alongside* `style: Style`, even though `Style`
  already has `shape()`/`borderWidth()` builders for exactly this — two ways to author the same
  value. `rawSurface()` had `gap: Float` instead of `verticalArrangement: Arrangement`. Fixed:
  removed `radius`/`borderWidth` from all 10 signatures, hardcoded the old
  `UiShape.md`/`UiShape.none` defaults into the merged style so unstyled callers are unaffected;
  migrated `rawSurface`'s `gap` to `verticalArrangement`. Migrated the 8 real external
  overriding call sites (`PanelTest.kt`, `UiSnapshotFixtures.kt`, `UiDslTutorialDocsTest.kt`,
  `ShadcnFields.kt`, `tooltip.kt`, `dropdownMenu.kt`, `Column.kt`) onto
  `style = Style { shape(...); borderWidth(...) }` / `Arrangement.spacedBy(...)`. Verified zero
  regressions (identical desktopTest failure counts to baseline).

## Known Constraint: `Canvas`/`CanvasScope` Is Not For Base Components

`Canvas.kt`'s `canvas { }` / `CanvasScope` (raw `drawRect`/`nested`/path drawing) is an escape
hatch for app-level custom graphics (see `UiDslTest.canvasExposesResponsiveWidthClassesAndAlignment`
for the intended shape: width-class-aware custom layout, not a widget). It must not be used to
implement base/reusable components in `ui-unstyled`/`ui-designsystem` — those go through the
normal widget/modifier pipeline (`Style`, `UiModifier`, the shared draw-primitive emitters) so
they get shape/border/clip/scroll/semantics for free and stay theme-able. `Canvas` bypasses all
of that. Track the proper fix as a future feature: a real graphics-layer modifier (Jetpack
Compose's `graphicsLayer` equivalent) so effects like the shimmer modifier stop being coupled
directly into `ui-core` and instead compose the same way `Canvas` content does today, without
`Canvas`'s bypass of the widget pipeline.

## Other Known Issues

- [x] Button label not displayed on dialog and dropdown menu. Root cause: `buttonSlotInternal`
  (`ui-unstyled/Buttons.kt`) resolved a themed `Style` but never pushed its foreground into the
  ambient text-style stack before composing Slot-API content, so labels rendered inside slot
  buttons (dialog/dropdown-menu actions) fell back to the surrounding page's ambient color. Fixed
  by pushing `resolved.textStyle then TextStyle(color = resolved.foreground)` before
  `drawContent`, matching how `surface`/`column`/`row`/`box` already propagate themed text style.
- [x] Shimmer modifier is directly coupled into `ui-core` instead of living as a shadcn-compose-style
  extension on top of a real modifier/graphics-layer primitive. Fixed: added
  `UiGraphicsLayer`/`UiGraphicsEffect` (`modifier/GraphicsLayer.kt`) as a real graphics-layer
  modifier primitive; `UiModifier.shimmer: Boolean` is now a computed extension property over
  `graphicsLayer?.has<UiShimmerEffect>()` instead of a hardcoded field, and `shadcnShimmer()`
  attaches/removes `UiShimmerEffect` through `graphicsLayer(...)`.

## Module Checklist

### `awake:engine:ui:ui-core`

- [ ] Keep `Dp` / `Sp` / `Dimension` / `UiModifier` / `Style` / `UiTheme` as the stable public base.
- [ ] Keep raw pixel math in layout and renderer internals only.
- [x] Add semantic debug metadata on widget output so tests and debug tooling can identify intent.
- [x] Add a first-class wireframe/layout debug overlay for bounds, padding, clip rects, and baselines. `UiDebugOverlay.kt`: `UiSemanticNode.debugOverlayPrimitives()`/`UiContext.debugOverlayPrimitives()` render bounds (blue), contentBounds (green), and clippedBounds (red) as stroked outlines from recorded semantic nodes -- append after a frame's own primitives to paint the wireframe on top.
- [ ] Keep `CoreUiTheme` only as the neutral fallback theme.
- [ ] Do not move named authored themes into `ui-core`.

### `awake:engine:ui:ui-unstyled`

- [ ] Keep only generic leaf widgets and generic containers.
- [x] Deprecate authored convenience overloads that take width/height as raw `Float`. Deleted outright (button/buttonSlot/toggle/checkbox/dropdown/slider/textureQuad in ui-unstyled, plus the mirrored copies in ui-dsl's `UiDslControls.kt`) rather than deprecated, since this API isn't published yet -- migrated all 51 real call sites onto modifier-based sizing.
- [x] Prefer modifier-first sizing on public entry points. Byproduct of the Float-overload deletion above -- every remaining public widget entry point is modifier-based only.
- [ ] Add slot-based variants only where content structure genuinely matters.
- [ ] Keep simple `label`/`title` params on leaf widgets when that is still the cleanest API.

### `awake:engine:ui-dsl`

- [ ] Keep generic composition templates, shells, property forms, and popup compositions.
- [x] Add `Dp`-first overloads where public height/width helpers still speak raw `Float`.
- [x] Deprecate float-based authored spacing/sizing helpers in favor of `Dp` or modifier-based sizing. Added `spacer(modifier: UiModifier)` as the single entry point for column and row spacing; deprecated the axis-named `spacer(height: Dp)`/`spacer(width: Dp)` overloads.
- [x] Shrink `UiShellDsl` to fewer canonical entry points. Deprecated `OverlayShellScope`'s corner-specific named helpers (`topLeftSlot`/`topRight`/`bottomLeftPane`/etc.) in favor of the generic `slot`/`place`/`pane(UiAnchor, ...)` entry points.
- [x] Keep `overlayBox(...)` as the preferred responsive overlay surface. All real production samples (hello-cube, starter-game, ui-showcase) already use `overlayBox` exclusively; `overlayShell` is now doc-commented as narrower corner-HUD sugar.
- [ ] Avoid adding new named placement helpers when `align(...)` or an existing slot primitive can express the layout.

### `awake:engine:ui:ui-designsystem`

- [ ] Keep themes, presets, tokens, and branded recipes here.
- [x] Deprecate float width/height convenience overloads on branded components. Deleted outright (badges/buttons/fields/property controls) rather than deprecated, since this API isn't published yet.
- [x] Collapse duplicated `UiScope` and `UiDslScope` wrapper logic behind shared internals where possible. The real duplication was the 16+ Float/Dp width-height overload pairs, already deleted. What remains (`shadcnToggle`/`Checkbox`/`Slider`, 3 pairs of identical 5-line bodies differing only by receiver type) has no shared interface between `UiScope`/`UiDslScope` to hang a collapse on; not worth inventing one for three 5-line functions. `shadcnDropdown`'s two variants are genuinely different implementations, not duplication.
- [x] Keep modifier-first and slot-based APIs as the canonical public surface. All branded components are modifier-based only after the Float-overload deletion pass.
- [ ] Keep named themes here, not in `ui-core`.

### `awake:engine:ui:ui-testing`

- [ ] Keep primitive-count and bounds drift checks.
- [x] Add semantic widget-role inspection.
- [x] Add text-fit, truncation, and alignment assertions.
- [x] Add sample-level golden layout verification. `UiLayoutSignature.kt`: `layoutSignature(nodes)` fingerprints a full page's semantic-node roles/ids/bounds independent of color/style; wired into `samples:ui-showcase`'s `UiShowcaseLayoutSignatureTest` across all 9 catalog pages. Catches layout drift (a widget moving/resizing/vanishing) without pixel-screenshot flakiness -- a pure recolor leaves the signature untouched.
- [ ] Stop treating screenshot eyeballing as the main UI correctness gate.

### `samples:ui-showcase`

- [ ] Keep this as the proof app for the intended public API.
- [x] Remove unnecessary `.px` authored sizing from sample UI code. Converted all 23 `Nf.px` modifier sizes in `UiShowcaseCatalog.kt` to `Nf.dp` -- `.px` divides by density scale (raw-pixel match), `.dp` is the correct density-independent authored unit; zero `.px` remain in the sample.
- [x] Add dedicated proof pages for layout, typography, slot APIs, and theme switching. Typography and theme switching were already covered (`fonts`/`theming` pages); added new `layout` (row/column primitives) and `slot-apis` (`buttonSlot(...)`'s content-lambda form) pages, each wired into the sidebar, the preview catalog, and the golden-layout signature test.
- [x] Make sample code demonstrate the preferred modifier-first usage path. Byproduct of the raw-Float overload deletion and `.px` -> `.dp` conversion above -- no remaining call site in `samples:ui-showcase` uses raw-`Float`/`.px` authored sizing.

## Delete / Deprecate / Keep

### Delete

- [x] Duplicate convenience wrappers whose only work is `Float -> .px -> modifier`. Deleted across ui-unstyled, ui-dsl, and ui-designsystem (badges/buttons/fields/property controls) rather than deprecated, since this API isn't published yet.
- [x] Extra placement helpers that only rename corner anchoring patterns already expressible by existing primitives. `OverlayShellScope`'s `topLeftSlot`/`topRight`/`bottomLeftPane`/etc. deprecated in favor of the generic `slot`/`place`/`pane(UiAnchor, ...)`.

### Deprecate

- [x] Raw `Float` authored sizing in public DSL and design-system APIs. Deleted outright rather than deprecated (unpublished API) across ui-unstyled/ui-dsl/ui-designsystem; zero raw-`Float` sizing overloads remain.
- [ ] Composition APIs that tightly own both structure and all displayed text when slots are the better contract.
- [x] Sample usage patterns that teach `.px` first instead of `Dp` or `UiModifier`. All 23 `.px` authored-sizing call sites in `samples:ui-showcase` converted to `.dp`.

### Keep

- [ ] Raw pixels for layout/runtime internals.
- [ ] `UiModifier`, `Dimension`, `Dp`, `Sp`, popup primitives, and scroll primitives.
- [ ] Neutral engine contracts in `ui-core`, generic widgets in `ui-unstyled`, generic compositions in `ui-dsl`, branded recipes in `ui-designsystem`.
- [ ] `Canvas`/`CanvasScope` scoped to app-level custom graphics only -- never used to implement base/reusable components (see "Known Constraint" above).

## Implementation Order

1. Public API normalization: `Dp` / `Sp` / modifier-first sizing. **(done)**
2. Design-system overload reduction and duplicate wrapper collapse. **(done)**
3. `UiShellDsl` and `UiDslLayout` surface reduction. **(done)**
4. Semantic UI debug metadata in `ui-core`. **(done)**
5. Stronger UI assertions in `engine/ui/ui-testing`. **(done)**
6. Showcase rewrite so the sample proves the intended usage path. **(done)**
7. Root-level `UiSlot`-as-authored-input cleanup (see "UiSlot As Root Authored Input" above).
   **(done)**
8. Base-component `x`/`y`/`width`/`height`/`insets`/`gap`-as-authored-input cleanup in
   `ui-unstyled`/`ui-designsystem` widget signatures, per the Slot-API-vs-Modifier-first policy
   above. **(done)** -- audit found the surface was already nearly clean; only remaining
   offender was `UiLegacyCompat.kt`'s deprecated `ColumnScope.row(height, width, gap: Float,
   modifier, content)`, migrated its 9 call sites onto `horizontalArrangement =
   Arrangement.spacedBy(...)` and deleted the dead overload.
9. Graphics-layer modifier (Jetpack Compose `graphicsLayer` equivalent) so `shimmer` decouples
   from `ui-core` into a real modifier-driven effect, matching the shadcn-compose extension
   pattern instead of being hardcoded into core. **(done)**
10. Button-label bug fix on dialog and dropdown menu -- decouple label the same way (modifier/
    slot content, not a hardcoded param), not just patch the symptom. **(done)**

## First Slice

This task starts with the lowest-risk cleanup that creates immediate pressure toward the
right API shape:

- add `Dp`-first entry points where the DSL still speaks raw `Float`
- deprecate float convenience overloads in the design-system layer
- keep existing behavior intact so downstream call sites can migrate incrementally

## Validation

- `:awake:engine:ui:ui-core:commonTest`
- `:awake:engine:ui-dsl:desktopTest`
- `:awake:engine:ui:ui-designsystem:commonTest`
- `:samples:ui-showcase:desktopTest`

## Done When

- Public authored APIs stop teaching raw pixels by default.
- The showcase code reads like the intended library API, not like internal engine scaffolding.
- The biggest duplicate wrapper families are either gone or formally deprecated.
- UI tests can fail on overlap, text-fit, and alignment regressions without relying on manual screenshots alone.
