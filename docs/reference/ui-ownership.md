# UI Ownership

This document is the canonical source for Awake's reusable UI boundaries.

## Goal

Keep reusable UI building blocks separate from branded recipes and separate again from
sample-specific adapters.

## Hard Rules

These are placement rules, not style preferences.

1. `awake:engine:ui-core` may expose only geometry, drawing, anchoring, clipping, slot,
   and style primitives.
2. `awake:engine:ui-widgets` may expose only generic leaf widgets built on `ui-core`.
3. `awake:engine:ui` may expose generic composition templates and DSL surfaces, but it
   must not introduce helper APIs that collapse a low-level primitive and a specific
   container into one convenience function when a slot/rect primitive can express it.
4. `awake:engine:ui-designsystem` owns branded or strongly opinionated recipes.
5. `samples:*` and future game modules own runtime-bound adapters, authored overlays,
   debug HUD wiring, and sample-specific compositions.

## Module Responsibilities

| Module | Responsibility | Examples |
|---|---|---|
| `awake:engine:ui-core` | Foundational drawing, layout, and surface primitives | low-level layout, drawing, clipping, slots, style plumbing |
| `awake:engine:ui-widgets` | Reusable widget primitives built on `ui-core` | button, checkbox, text field, slider, primitive panels |
| `awake:engine:ui` | Style-agnostic composition templates and UI DSL surfaces | shells, sections, property forms, reusable inspector layouts |
| `awake:engine:ui-designsystem` | Branded or strongly opinionated recipes | shadcn-style skins, app-specific themes, branded presets |
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

## Concrete Placement Examples

| API shape | Correct home | Why |
|---|---|---|
| `UiSlot.anchored(anchor, width, height, margin)` | `ui-core` | pure placement math returning a slot |
| `button`, `checkbox`, `slider` | `ui-widgets` | generic reusable leaf widgets |
| `PropertyList`, `PropertyRow`, generic inspector scaffolds | `ui` | reusable compositions of primitives/widgets |
| `AwakeShadcnPanelStyle`, branded variants | `ui-designsystem` | visual opinion, not engine primitive |
| `HelloCubeHud`, `SceneInspectorBindings`, demo overlays | sample/game module | runtime-bound authored usage |

These API shapes are specifically discouraged in reusable UI modules:

- `anchoredColumn(...)`
- `anchoredRow(...)`
- `anchoredPanel(...)`
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
- compositional + generic -> `ui`
- branded/opinionated -> `ui-designsystem`
- sample/runtime-bound -> sample or game module

## Mechanical Enforcement

This policy is build-enforced in Awake's reusable UI modules.

- `:awake:engine:ui-core:check`
- `:awake:engine:ui-widgets:check`
- `:awake:engine:ui:check`

run a `verifyUiOwnership` task that rejects:

- container-bound anchored helper names such as `anchoredColumn`
- direct sample/runtime-bound references such as `SceneGameRuntime` or `HelloCube*`

The check is intentionally lightweight and curated. It is not a theorem prover. When the
policy grows, expand the canonical doc first, then update the check.
