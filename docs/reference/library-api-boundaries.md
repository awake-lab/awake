# Library API Boundaries

This document is the canonical rule for how Awake splits public library surface from DSL
syntax sugar and from local helpers.

## Goal

Keep Awake usable as a library even when a consumer does not want the DSL.

Rule: the DSL is optional authored syntax over reusable runtime contracts. It must not be
the only practical way to use the library.

## Layers

| Layer | Purpose | Lives in | Examples |
|---|---|---|---|
| API / contract | stable public types a consumer can depend on directly | engine or feature module public files | `AwakeGame`, `GameInstaller`, `GameWindowConfig`, `SceneGameSpec`, `SceneGameRuntime`, `GameUiSpec`, `GameUiRuntime` |
| DSL | thin builder syntax that produces or installs contract types | `*Dsl.kt` and builder entrypoints | `game {}`, `sceneGame {}`, `gameUi {}`, `GameDsl.ecs(...)`, `GameDsl.ui(...)` |
| Helpers | convenience wrappers around DSL or API for common authored cases | closest owning module, often sample/game module | `cameraEntity`, `meshEntity`, sample HUD mapping, debug overlay wiring |

## Placement Rules

### 1. API / contract layer

Owns the reusable library surface.

- configuration types
- installable specs
- runtime handles
- service lookup contracts
- direct integration points other modules compose against

This layer must not require DSL-only entrypoints to be useful.

### 2. DSL layer

Owns human-friendly authored syntax only.

- builder classes
- top-level DSL entrypoints
- small convenience wrappers that only expand to builder calls

The DSL should assemble contract types, not hide business/runtime behavior in surprising
ways.

### 3. Helper layer

Owns convenience for one specific authored workflow.

- sample/game-specific composition helpers
- debug bindings
- opinionated bootstrap wrappers
- formatting helpers for a concrete HUD or inspector

If a helper knows a specific sample, scene, gameplay rule, or debug protocol, it is not
core library API.

## Mechanical Rules

When adding a new type, answer these questions in order:

1. Can a consumer reuse this without our DSL?
2. Is this type a stable contract, or only convenience syntax?
3. Does it know runtime truth, or only how to author a common shape?
4. Does it know a specific sample or game?

Use the answers like this:

- reusable without DSL -> API / contract
- only builder syntax -> DSL
- only convenience for one authored flow -> helper
- tied to one game/sample -> sample or game module, not engine core

## Awake-Specific Guidance

- `awake:engine:game` should expose the neutral game contract first, then DSL sugar.
- `awake:scene` should expose runtime/spec/install surfaces first, then scene-authoring DSL.
- `awake:engine:ui-dsl` should expose runtime/spec/install surfaces first, then UI composition
  DSL.
- sample modules should own debug shells, demo overlays, inspector mappings, and bootstrap
  shortcuts.

## Anti-Patterns

- putting runtime classes, specs, and DSL builders in one monolithic file
- making a builder the only usable path to a feature
- moving sample-specific helpers into engine modules
- letting helper functions become hidden runtime registries
- treating the DSL as the architecture instead of authored syntax

## Done Right

Awake is in a healthy place when:

- the public contract reads cleanly without opening a DSL file
- the DSL files mostly read as builders and convenience wrappers
- sample/game helpers can be deleted or replaced without changing engine contracts
- a consumer can compose installable specs directly if they do not want authored DSL
