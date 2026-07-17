# UI Ownership

This document is the canonical source for Awake's reusable UI boundaries.

## Goal

Keep reusable UI building blocks separate from branded recipes and separate again from
sample-specific adapters.

## Hard Rules

These are placement rules, not style preferences.

1. `awake:engine:ui-core` may expose only geometry, drawing, anchoring, clipping, slot,
   style primitives, theme contracts, and at most a neutral fallback theme.
2. `awake:engine:ui-widgets` may expose only generic leaf widgets built on `ui-core`. It
   must not own property-form, inspector, or tooling-shell composition.
3. `awake:engine:ui-dsl` may expose generic composition templates and DSL surfaces, and it
   owns neutral property forms and reusable tooling composition, but it
   must not introduce helper APIs that collapse a low-level primitive and a specific
   container into one convenience function when a slot/rect primitive can express it.
4. `awake:engine:ui-designsystem` owns branded or strongly opinionated recipes and every
   named authored theme intended for direct app/sample use.
5. `samples:*` and future game modules own runtime-bound adapters, authored overlays,
   debug HUD wiring, and sample-specific compositions.

## Module Responsibilities

| Module | Responsibility | Examples |
|---|---|---|
| `awake:engine:ui-core` | Foundational drawing, layout, and surface primitives | low-level layout, drawing, clipping, slots, style plumbing, `UiTheme`, `CoreUiTheme` |
| `awake:engine:ui-widgets` | Reusable widget primitives built on `ui-core` | button, checkbox, text field, slider, primitive panels |
| `awake:engine:ui-dsl` | Style-agnostic composition templates and UI DSL surfaces | shells, sections, property forms, reusable inspector layouts |
| `awake:engine:ui-designsystem` | Branded or strongly opinionated recipes | shadcn-style skins, `DefaultUiTheme`, `DarkUiTheme`, `LightUiTheme`, branded presets |
| `samples:*` or game modules | Sample/game adapters and authored usage | scene inspector bindings, demo-specific overlays, debug HUD wiring |

## Primitive Vs Composition

Treat these as reusable primitives:

- `Panel`
- `Section`
- `PropertyList`
- `PropertyRow`
- `UiSlot.anchored(...)`

Treat these as higher-level compositions:

- `InspectorPane`
- sample shells
- demo overlays
- app-specific control bars

Rule: a primitive should still make sense outside the current sample or demo.

Text ownership rule:

- leaf widgets may expose simple text params such as `label`, `title`, or `supportingText`
- structural or multi-region compositions should expose slots or restricted-scope regions as
  the primary API, with string overloads kept only as convenience wrappers
- if a reusable API owns both the container structure and all displayed text, it is usually too
  coupled for long-term reuse

## Concrete Placement Examples

| API shape | Correct home | Why |
|---|---|---|
| `UiSlot.anchored(anchor, width, height, margin)` | `ui-core` | pure placement math returning a slot |
| `button`, `checkbox`, `slider` | `ui-widgets` | generic reusable leaf widgets |
| `CoreUiTheme`, `UiTheme`, `UiColorTokens` | `ui-core` | theme contract and neutral fallback only |
| `PropertyList`, `PropertyRow`, `propertyCheckbox`, generic inspector scaffolds | `ui-dsl` | reusable compositions of primitives/widgets |
| `DefaultUiTheme`, `DarkUiTheme`, `LightUiTheme` | `ui-designsystem` | named authored themes belong above engine core |
| `AwakeShadcnPanelStyle`, branded variants | `ui-designsystem` | visual opinion, not engine primitive |
| `HelloCubeHud`, `SceneInspectorBindings`, demo overlays | sample/game module | runtime-bound authored usage |

These API shapes are specifically discouraged in reusable UI modules:

- `anchoredColumn(...)`
- `anchoredRow(...)`
- `anchoredPanel(...)`
- `propertyRow(...)` in `ui-widgets`
- `propertyCheckbox(...)` in `ui-widgets`
- `DefaultUiTheme` in `ui-core`
- `DarkUiTheme` in `ui-core`
- `LightUiTheme` in `ui-core`
- `HelloCube*`
- helpers that know `SceneGameRuntime`, ECS `World`, demo modes, or sample debug state

Rule of thumb: if the API name bakes in both a placement concern and a concrete container,
it is usually too high-level for `ui-core`, and usually too convenience-shaped for long-term
reuse.

## What Must Stay Out Of Reusable UI Modules

If a UI component knows about any of these directly, it likely belongs in a sample or game
adapter layer instead of `ui-core`, `ui-widgets`, or `ui`:

- `SceneGameRuntime`
- ECS `World` or direct system access
- entity names or entity selection state owned by a sample
- demo modes
- sample-only debug toggles

The reusable module should accept generic state, callbacks, and content slots instead.

Theme rule:

- `ui-core` stays neutral. If the API needs a no-dependency fallback theme, keep it generic
  and do not treat it as the authored app theme.
- named shipped themes for apps, demos, docs, and samples belong in `ui-designsystem`
- property-form rows, checkbox rows, and inspector-style control groupings belong in `ui-dsl`,
  even when they are visually conservative

## Naming Guidance

- use general names for real reusable pieces
- avoid making app-specific shells sound foundational
- if a piece is mostly a concrete composition of primitives, name it like a composition
  rather than a primitive

Examples:

- good primitive: `Panel`
- good composition: `InspectorPane`
- suspicious primitive name: `InspectorPanel` if it is tightly tied to one sample workflow

## Placement Checks

Before adding a new UI type, ask:

1. Can this be reused without ECS or sample state?
2. Is it a foundational primitive or a composition?
3. Does it define brand/look opinion, or only structure?

Use the answers like this:

- foundational + generic -> `ui-core` or `ui-widgets`
- compositional + generic -> `ui-dsl`
- branded/opinionated -> `ui-designsystem`
- sample/runtime-bound -> sample or game module

## Mechanical Enforcement

This policy is build-enforced in Awake's reusable UI modules.

- `:awake:engine:ui-core:check`
- `:awake:engine:ui-widgets:check`
- `:awake:engine:ui-dsl:check`

run a `verifyUiOwnership` task that rejects:

- container-bound anchored helper names such as `anchoredColumn`
- `propertyRow` and `propertyCheckbox` in `ui-core`/`ui-widgets`
- `DefaultUiTheme`, `DarkUiTheme`, and `LightUiTheme` in `ui-core`
- direct sample/runtime-bound references such as `SceneGameRuntime` or `HelloCube*`

The check is intentionally lightweight and curated. It is not a theorem prover. When the
policy grows, expand the canonical doc first, then update the check.
