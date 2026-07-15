# Game Structure

This document is the canonical source for how Awake organizes state and folders when
authoring a game or sample.

## Goal

Keep simulation, session/runtime state, UI view state, and widget-local state separate so
games stay understandable as they grow.

## State Categories

| State category | Owner | Examples | Keep it out of |
|---|---|---|---|
| Simulation state | ECS / scene runtime | transforms, mesh bindings, gameplay components, physics state | UI widgets, game-shell state holders |
| Session/runtime state | game module `state/` | active scene, selected mode, paused/running, current tool, selected entity id | reusable UI modules, ECS components unless gameplay truly owns it |
| UI view state | `ui/presenter/` or equivalent mapping layer | HUD lines, inspector rows, toolbar models, labels derived from world/session state | ECS components, leaf widgets |
| Ephemeral widget state | UI runtime / widget internals | hover, expanded dropdown, drag progress, cursor position | game session state and ECS unless another layer really needs it |

Rule: keep state at the lowest layer that genuinely owns it.

- if gameplay rules need it, it is simulation state
- if the app shell or mode flow needs it, it is session/runtime state
- if the UI only renders it, it is UI view state
- if only one widget cares, it stays widget-local

## Folder Structure For A New Game

Start with folders inside one module. Split into multiple modules only when the game earns
the extra boundary.

```text
samples/<game-name>/
  src/commonMain/kotlin/<package>/
    app/
      GameEntry.kt
      GameModule.kt
    gameplay/
      components/
      systems/
      events/
      rules/
    scene/
      scenes/
      prefabs/
      assets/
    state/
      GameSessionState.kt
      GameAction.kt
      GameSnapshot.kt
    ui/
      components/
      overlays/
      screens/
      presenter/
    debug/
      tools/
      inspector/
      telemetry/
```

## Folder Responsibilities

### `app/`

Owns the composition root only.

- bootstraps `game {}` or `ecsGame {}`
- preferably uses one `game {}` root and installs reusable `ecs(...)` / `ui(...)` specs
- wires scene/runtime/services
- does not own gameplay rules
- does not own widget behavior

### `gameplay/`

Owns simulation logic.

- ECS components
- ECS systems
- gameplay rules
- gameplay events

Do not put UI formatting or shell/session glue here.

### `scene/`

Owns authored scene structure and reusable prefabs.

- scene declarations
- prefab builders
- asset bindings

Do not put HUD layout or inspector formatting here.

### `state/`

Owns non-ECS runtime/session state.

- camera mode
- active tool
- selected entity id
- debug toggles
- paused/running state

Keep this layer free of renderer primitives and widget internals.

### `ui/`

Owns rendering-facing UI and presenter mapping.

- stateless components
- overlays and screens
- presenter/mapping code that turns session/world state into UI view state

Leaf UI components should consume plain values and callbacks. They should not reach into
`World`, `SceneGameRuntime`, or ECS systems directly.

### `debug/`

Owns optional tooling.

- inspectors
- debug commands
- telemetry overlays
- snapshot/export helpers

Keep debug workflows out of reusable engine UI modules.

## Creation Checklist

Before adding a new type, answer these questions:

1. Is this gameplay truth, session/runtime truth, UI view data, or widget-local state?
2. Does this type need ECS access, or only a mapped snapshot of ECS state?
3. Is this reusable engine API, reusable game composition, or authored game usage?

Use the answers like this:

- gameplay truth -> `gameplay/`
- session/runtime truth -> `state/`
- UI rendering data -> `ui/presenter/` or `ui/components/`
- authored overlay/screen composition -> `ui/overlays/` or `ui/screens/`
- reusable engine primitive -> engine module, following [ui-ownership.md](/Users/ronvaldoz/StudioProjects/awaken/docs/reference/ui-ownership.md)

## Split To Modules Only When Needed

If the game grows beyond one sample module, split along the same ownership lines:

```text
:game:<name>:app
:game:<name>:gameplay
:game:<name>:scene
:game:<name>:ui
:game:<name>:debug
```

Do not split just because something feels important. Split when:

- compile times or ownership boundaries are getting painful
- multiple consumers need the same game-specific layer
- a folder has become large enough that build-time protection adds real value
