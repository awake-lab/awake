# UI Ownership

This document is the canonical source for Awake's reusable UI boundaries.

## Goal

Keep reusable UI building blocks separate from branded recipes and separate again from
sample-specific adapters.

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

Treat these as higher-level compositions:

- `InspectorPane`
- sample shells
- demo overlays
- app-specific control bars

Rule: a primitive should still make sense outside the current sample or demo.

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
