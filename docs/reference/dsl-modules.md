# DSL Modules

This page is the quick map for Awake's authored DSL surface after the module split.

## Goal

Keep the pleasant builder syntax without hiding the real reusable contracts.

## Modules

| Module | Owns | Depends on |
|---|---|---|
| `awake:engine:game-dsl` | `game {}` / `gameSpec {}` / `gameModule {}` / `feature.createGame {}` / installer syntax | `awake:engine:game` |
| `awake:scene-dsl` | `sceneGame {}`, `flow {}`, `sceneFlow {}`, and `game/module { ecs { ... } }` | `awake:scene`, `awake:engine:game-dsl`, `awake:engine:ui-dsl` |
| `awake:engine:ui-dsl` | `gameUi {}`, `game/module { ui { ... } }`, and generic shell helpers like `shellPane(...)` | `awake:engine:game-dsl`, `ui-core`, `ui-widgets` |

## Recommended Authoring Shape

Prefer one `game {}` composition root and install reusable specs into it:

```kotlin
val game = game {
    window {
        title = "Hello Cube"
        size(1600, 900)
        backend.vulkan()
    }
    module(
        gameModule {
            ecs(helloCubeSceneSpec(state))
            ui(helloCubeUiSpec(state))
            install(helloCubeDebugInstaller(state))
        }
    )
}
```

Or wrap one reusable module directly when the sample/game root is only window and backend
selection:

```kotlin
val feature = gameModule {
    ecs(helloCubeSceneSpec(state))
    ui(helloCubeUiSpec(state))
    install(helloCubeDebugInstaller(state))
}

val game = feature.createGame {
    title = "Hello Cube"
    size(1600, 900)
    backend.vulkan()
}
```

That keeps:

- `game-dsl` as the single top-level game shell
- `gameModule {}` as the reusable authored feature layer
- `scene-dsl` focused on scene authoring
- `ui-dsl` focused on overlay authoring and reusable shell composition

## When To Use Specs Directly

Prefer spec values when you want reuse or testability:

```kotlin
val sceneSpec = sceneGame { ... }
val uiSpec = gameUi { ... }
val feature = gameModule {
    ecs(sceneSpec)
    ui(uiSpec)
}

val spec = gameSpec {
    module(feature)
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
