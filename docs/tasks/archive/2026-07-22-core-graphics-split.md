## Core Graphics Split

Date: 2026-07-22
Status: Draft

### Goal

Decide whether Awake should introduce a non-UI graphics foundation module, and if so,
move only truly shared graphics concepts there while keeping UI-specific authored APIs in
`awake:engine:ui:ui-core`.

### Current State

Today `awake:engine:ui:ui-core` owns both:

- UI-authored surfaces:
  - `UiScope`
  - `CanvasScope`
  - `UiDrawPrimitive`
  - `UiTheme`
  - `UiModifier`
  - layout, slot, semantics, clipping entrypoints

- Lower-level graphics-ish data:
  - `UiPath`
  - `UiStroke`
  - `UiLinearGradient`
  - `UiImageVector`
  - shape/path conversion helpers

This is acceptable while those types are used only by UI, but it becomes a coupling point
if editor overlays, vector assets, debug rendering, or non-UI authored drawing want the
same path/paint vocabulary.

### Recommendation

Do not rush a new module just because `CanvasScope` now exists.

Add a dedicated shared graphics module only when at least one non-UI consumer is real.

Recommended future shape:

- `awake:engine:ui:ui-core`
  - owns UI-authored APIs
  - keeps `CanvasScope`
  - keeps `UiDrawPrimitive`
  - keeps UI clipping/layout/semantics/theme

- future `awake:graphics` or `awake:graphics-core`
  - owns renderer-agnostic graphics data only
  - no theme, no layout, no widget semantics, no UI scope

### Candidates To Extract Later

These are the strongest candidates for a future shared graphics module:

- `UiPath`
- `UiPathBuilder`
- `UiFillRule`
- `UiStroke`
- `UiStrokeCap`
- `UiStrokeJoin`
- `UiPoint`
- `UiTriangleMesh`
- `UiTexturedTriangleMesh`
- `UiLinearGradient`
- `UiImageVector`
- shape-to-path helpers that do not depend on UI theme or layout state

These should stay in `ui-core`:

- `CanvasScope`
- `UiDrawPrimitive`
- `UiScope`
- `UiSlot`
- `UiModifier`
- `UiTheme`
- semantic recording
- UI clip stack entrypoints
- text/layout helpers tied to `UiContext`

### Extraction Rule

Promote a type out of `ui-core` only if all are true:

1. It makes sense without widgets, layout, or theme
2. It has at least one non-UI consumer
3. It does not require `UiContext`
4. Its naming can drop the `Ui` prefix without becoming misleading

If any of those are false, keep it in `ui-core`.

### Risks

- Premature split creates naming churn without architectural gain
- Moving `UiDrawPrimitive` too early can blur renderer/UI ownership
- Extracting text too early is risky because current text emission still depends on UI
  font/style/context rules

### Good Next Steps

1. Wire `canvas {}` into at least one real sample or preview
2. Track which canvas/data types get reused outside shared UI
3. Once a second consumer exists, extract only the path/gradient/vector layer first
4. Leave draw command emission and UI context in `ui-core`
