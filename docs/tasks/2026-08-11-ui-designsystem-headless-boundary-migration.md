# Final Plan: UI API / Core / Headless Boundary

## Outcome

`ui-designsystem` compiles only against `ui-headless` and `ui-api`. It has no `ui-core`
dependency and cannot import raw UI runtime capabilities. Public call sites remain Compose-like:

```kotlin
uiScope.shadcnDialog(...)
uiScope.shadcnButton(...)
```

## Final Module Model

```text
ui-api
  Stable, runtime-free value contracts only: UiBounds, Dimension, Dp, Sp, and
  color/shape/typography value contracts. Move structural contracts here only
  when they have no UiContext, renderer, or runtime dependency.
  May depend only on genuine lower-level contracts such as awake-core Color.

ui-core
  UiContext, UiPrimitiveScope, frame lifecycle, layout, draw collection,
  hit testing, input/focus/state, renderer-facing mechanics, and a neutral
  fallback resolver. No public UiComponentStyles-style component recipe registry.
  Depends on ui-api.

ui-headless
  Public UiScope, ColumnScope, RowScope, BoxScope, and AbsoluteScope facades;
  generic leaf widgets, interaction behavior, and neutral visual-state
  contracts such as SurfaceVisuals(rest, hovered, pressed, disabled). These
  scopes hold internal raw primitive-scope references. No named variants.
  Depends on ui-api and implementation(ui-core).

ui-designsystem
  UiScope.shadcn* recipes, named themes, token instances, branded variants,
  icons, and branded composition. It maps branded variants to Headless neutral
  visual states. Depends on ui-api and ui-headless. Never depends on ui-core.

ui-dsl (when production composition warrants the module)
  Neutral multi-widget/tooling composition such as property rows, inspector
  scaffolds, and tooling shells. Depends on Headless; it is not a Headless export.

apps/samples
  May assemble ui-core, ui-headless, and designsystem. Root integration creates
  UiScope from UiContext; component call sites use only UiScope.
```

`UiScope` is the public headless facade. Rename the current raw scope to
`UiPrimitiveScope`. Do not add a fourth facade module, use friend modules, or add a
`headless { ... }` call-site block.

## Guardrails

- `ui-headless` must not expose `UiContext`, `UiPrimitiveScope`, or any core-only type in a
  public signature; otherwise its implementation dependency leaks.
- Public layout scopes (`ColumnScope`, `RowScope`, `BoxScope`, `AbsoluteScope`) belong in
  `ui-headless` alongside public `UiScope`. They are behavioral DSL receivers, not immutable
  API contracts. Core keeps separate raw `UiPrimitive*Scope` implementations.
- Advanced custom primitive authoring remains deliberate: provide a named primitive-authoring
  dependency/API for consumers that genuinely need `UiPrimitiveScope`; do not expose it through
  designsystem or ordinary headless APIs.
- `awake.ui-ownership-convention` remains defense in depth, not the primary boundary.
- Named variants (`Primary`, `Ghost`, `Outline`, and brand vocabulary) are forbidden in
  `ui-headless`; a public Core component-style registry is forbidden in `ui-core`.

## Implementation Sequence

### Phase 0 — Freeze and inventory

1. Add this document to the work lane and avoid unrelated UI ownership moves.
2. Inventory all `ui-designsystem` imports from core and every public signature containing a core
   type.
3. Classify every item as `ui-api` contract, reusable headless behavior, branded composition, or
   deliberate advanced primitive authoring.

Gate: the inventory has a target module and test owner for every item.

### Phase 1 — Classify contracts and introduce `ui-api`

1. Create `:awake:engine:ui:ui-api`.
2. Move pure values first: units, bounds/dimensions, and color/shape/typography contracts. Keep
   `UiContext`, drawing/layout machinery, and any type with runtime behavior in Core. Do not
   move behavioral scope receivers to `ui-api` merely to make a dependency graph compile.
3. Replace Core's `UiComponentStyles`/`CoreUiComponentStyles` direction with a Headless generic
   visual-state model. Do not move the registry wholesale: its component-specific fields are the
   visual-policy leak this migration is eliminating.
4. Make `ui-core` depend on `ui-api`; update imports with no behavior changes.

Gate: all moved types compile on every existing KMP target and `ui-api` has no dependency on
`ui-core`.

### Phase 2 — Split raw scope from public scope

1. Rename the current `UiScope` to `UiPrimitiveScope` within core.
2. Move the author-facing `UiScope` FQN into `ui-headless`; it contains an internal primitive
   backing reference.
3. Add `UiContext.createUiScope()` in headless for root/app integration.
4. Adapt generic headless widgets to the facade without changing their behavior.

Gate: app roots can create the new `UiScope`; existing headless desktop tests remain green.

### Phase 3 — Enforce the classpath boundary

1. Change headless to `implementation(project(":awake:engine:ui:ui-core"))`.
2. Remove every `ui-core` dependency from designsystem.
3. Ensure every designsystem public signature uses only `ui-api` or headless types.
4. Add a compile-only consumer fixture that depends on designsystem and proves `UiContext` and
   `UiPrimitiveScope` cannot be imported.

Gate: designsystem's `commonMain` compile classpath has no ui-core and the consumer fixture
compiles.

### Phase 4 — Vertical behavior and visual-policy migration

Migrate one behavior at a time, always retaining `UiScope.shadcn*` syntax:

1. **Complete:** Button plus its generic visual states and `shadcnButton` variant mapping
   (proof slice).
2. **Complete:** Dialog plus `overlayScrim`, including runtime-free `card`/`popover` theme
   roles consumed through `UiThemeValues`.
3. Sheet and drawer mechanics.
4. Popup/dropdown/menu position, selection, and scrolling.
5. Tabs and collapsible state/measurement.
6. OTP focus traversal, resize drag handling, and remaining reusable drawing/layout helpers.

For each slice: move generic behavior and neutral state contracts to Headless; leave Shadcn
tokens, named variants, and composition in Design System; and add a Headless behavior test plus
Design System desktop regression coverage.

### Phase 5 — Defense-in-depth enforcement

1. Expand `ui-ownership-convention` to reject core dependencies, `UiPrimitiveScope`, `UiContext`,
   core-package imports, and known implementation escape hatches in designsystem.
2. Add negative Gradle fixture tests proving each prohibited reference fails.
3. Require designsystem checks and the consumer fixture in CI.

Gate: a direct core reference from designsystem fails locally and in CI.

### Phase 6 — Document and stabilize

1. Update `docs/reference/ui-ownership.md` with the final module graph and allowed `ui-api`
   contract list.
2. Document the advanced primitive-authoring path separately from ordinary widget authoring.
3. Update sample/root integration examples and generated API docs.

## Definition of Done

- Designs system has no ui-core compile dependency or public core type leakage.
- UI core runtime is reachable only through intended integration or advanced primitive-authoring
  paths.
- Reusable behavior lives in headless and branded policy lives in designsystem.
- Compose-style `UiScope.shadcn*` call sites stay intact.
- Compile fixtures, targeted tests, and CI prevent boundary regressions.
