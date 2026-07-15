# DSL Modules

This page is the quick map for Awake's authored DSL surface after the module split.

## Goal

Keep the pleasant builder syntax without hiding the real reusable contracts.

## Modules

| Module | Owns | Depends on |
|---|---|---|
| `awake:engine:game-dsl` | `game {}` / `gameSpec {}` / installer syntax | `awake:engine:game` |
| `awake:scene-dsl` | `sceneGame {}` and `game { ecs { ... } }` | `awake:scene`, `awake:engine:game-dsl`, `awake:engine:ui-dsl` |
| `awake:engine:ui-dsl` | `gameUi {}` and `game { ui { ... } }` | `awake:engine:game-dsl`, `ui-core`, `ui-widgets` |

## Recommended Authoring Shape

Prefer one `game {}` composition root and install reusable specs into it:

```kotlin
val game = game {
    window {
        title = "Hello Cube"
        size(1600, 900)
        backend.vulkan()
    }
    ecs(helloCubeSceneSpec(state))
    ui(helloCubeUiSpec(state))
    install(helloCubeDebugInstaller(state))
}
```

That keeps:

- `game-dsl` as the single top-level game shell
- `scene-dsl` focused on scene authoring
- `ui-dsl` focused on overlay authoring

## When To Use Specs Directly

Prefer spec values when you want reuse or testability:

```kotlin
val sceneSpec = sceneGame { ... }
val uiSpec = gameUi { ... }

val spec = gameSpec {
    ecs(sceneSpec)
    ui(uiSpec)
}
```

That is the preferred path for samples, demos, and future game modules that want small
composition roots and reusable authored pieces.

## What Stays Out Of DSL Modules

These modules should not own:

- sample-only HUD mapping
- debug server protocols
- backend-specific host loops for one concrete sample
- gameplay/session rules

Those belong in sample or game modules, or in backend/runtime modules when the behavior is
generic infrastructure rather than authored syntax.
