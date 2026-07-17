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
- `ui-widgets` is mostly in the right place as a generic leaf-widget layer.
- `ui-dsl` owns the right category of APIs, but its surface is too broad.
- `ui-designsystem` owns the right category of APIs, but it still duplicates too many
  wrapper entry points and still tolerates raw-float authored sizing.
- `engine/testing` can catch some overlap and drift, but it still cannot reason about
  semantic roles, text-fit expectations, or layout intent strongly enough.

## Module Checklist

### `awake:engine:ui-core`

- [ ] Keep `Dp` / `Sp` / `Dimension` / `UiModifier` / `Style` / `UiTheme` as the stable public base.
- [ ] Keep raw pixel math in layout and renderer internals only.
- [x] Add semantic debug metadata on widget output so tests and debug tooling can identify intent.
- [ ] Add a first-class wireframe/layout debug overlay for bounds, padding, clip rects, and baselines.
- [ ] Keep `CoreUiTheme` only as the neutral fallback theme.
- [ ] Do not move named authored themes into `ui-core`.

### `awake:engine:ui-widgets`

- [ ] Keep only generic leaf widgets and generic containers.
- [ ] Deprecate authored convenience overloads that take width/height as raw `Float`.
- [ ] Prefer modifier-first sizing on public entry points.
- [ ] Add slot-based variants only where content structure genuinely matters.
- [ ] Keep simple `label`/`title` params on leaf widgets when that is still the cleanest API.

### `awake:engine:ui-dsl`

- [ ] Keep generic composition templates, shells, property forms, and popup compositions.
- [x] Add `Dp`-first overloads where public height/width helpers still speak raw `Float`.
- [x] Deprecate float-based authored spacing/sizing helpers in favor of `Dp` or modifier-based sizing. Added `spacer(modifier: UiModifier)` as the single entry point for column and row spacing; deprecated the axis-named `spacer(height: Dp)`/`spacer(width: Dp)` overloads.
- [x] Shrink `UiShellDsl` to fewer canonical entry points. Deprecated `OverlayShellScope`'s corner-specific named helpers (`topLeftSlot`/`topRight`/`bottomLeftPane`/etc.) in favor of the generic `slot`/`place`/`pane(UiAnchor, ...)` entry points.
- [x] Keep `overlayBox(...)` as the preferred responsive overlay surface. All real production samples (hello-cube, starter-game, ui-showcase) already use `overlayBox` exclusively; `overlayShell` is now doc-commented as narrower corner-HUD sugar.
- [ ] Avoid adding new named placement helpers when `align(...)` or an existing slot primitive can express the layout.

### `awake:engine:ui-designsystem`

- [ ] Keep themes, presets, tokens, and branded recipes here.
- [ ] Deprecate float width/height convenience overloads on branded components.
- [ ] Collapse duplicated `UiScope` and `UiDslScope` wrapper logic behind shared internals where possible.
- [ ] Keep modifier-first and slot-based APIs as the canonical public surface.
- [ ] Keep named themes here, not in `ui-core`.

### `awake:engine:testing`

- [ ] Keep primitive-count and bounds drift checks.
- [x] Add semantic widget-role inspection.
- [x] Add text-fit, truncation, and alignment assertions.
- [ ] Add sample-level golden layout verification.
- [ ] Stop treating screenshot eyeballing as the main UI correctness gate.

### `samples:ui-showcase`

- [ ] Keep this as the proof app for the intended public API.
- [ ] Remove unnecessary `.px` authored sizing from sample UI code.
- [ ] Add dedicated proof pages for layout, typography, slot APIs, and theme switching.
- [ ] Make sample code demonstrate the preferred modifier-first usage path.

## Delete / Deprecate / Keep

### Delete

- [ ] Duplicate convenience wrappers whose only work is `Float -> .px -> modifier`.
- [ ] Extra placement helpers that only rename corner anchoring patterns already expressible by existing primitives.

### Deprecate

- [ ] Raw `Float` authored sizing in public DSL and design-system APIs.
- [ ] Composition APIs that tightly own both structure and all displayed text when slots are the better contract.
- [ ] Sample usage patterns that teach `.px` first instead of `Dp` or `UiModifier`.

### Keep

- [ ] Raw pixels for layout/runtime internals.
- [ ] `UiModifier`, `Dimension`, `Dp`, `Sp`, popup primitives, and scroll primitives.
- [ ] Neutral engine contracts in `ui-core`, generic widgets in `ui-widgets`, generic compositions in `ui-dsl`, branded recipes in `ui-designsystem`.

## Implementation Order

1. Public API normalization: `Dp` / `Sp` / modifier-first sizing.
2. Design-system overload reduction and duplicate wrapper collapse.
3. `UiShellDsl` and `UiDslLayout` surface reduction.
4. Semantic UI debug metadata in `ui-core`.
5. Stronger UI assertions in `engine/testing`.
6. Showcase rewrite so the sample proves the intended usage path.

## First Slice

This task starts with the lowest-risk cleanup that creates immediate pressure toward the
right API shape:

- add `Dp`-first entry points where the DSL still speaks raw `Float`
- deprecate float convenience overloads in the design-system layer
- keep existing behavior intact so downstream call sites can migrate incrementally

## Validation

- `:awake:engine:ui-core:commonTest`
- `:awake:engine:ui-dsl:desktopTest`
- `:awake:engine:ui-designsystem:commonTest`
- `:samples:ui-showcase:desktopTest`

## Done When

- Public authored APIs stop teaching raw pixels by default.
- The showcase code reads like the intended library API, not like internal engine scaffolding.
- The biggest duplicate wrapper families are either gone or formally deprecated.
- UI tests can fail on overlap, text-fit, and alignment regressions without relying on manual screenshots alone.
