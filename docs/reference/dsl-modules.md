# DSL Modules

This page is the quick map for Awake's authored DSL surface after the module split.

## Goal

Keep the pleasant builder syntax without hiding the real reusable contracts.

## Modules

| Module | Owns | Depends on |
|---|---|---|
| `awake:engine:game-dsl` | `game {}` / `gameSpec {}` / `gameModule {}` / `feature.createGame {}` / installer syntax | `awake:engine:game` |
| `awake:scene-dsl` | `sceneGame {}`, `flow {}`, `sceneFlow {}`, and `game/module { ecs { ... } }` | `awake:scene`, `awake:engine:game-dsl`, `awake:engine:ui-dsl` |
| `awake:engine:ui-dsl` | `gameUi {}`, `game/module { ui { ... } }`, responsive overlay authoring through `overlayBox(...)` + `UiBoxConstraints`, shell helpers like `shellPane(...)` / `overlayShell(...)`, and neutral property-form composition | `awake:engine:game-dsl`, `ui-core`, `ui-headless` |

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
- `ui-dsl` focused on overlay authoring, responsive box-style composition, and reusable shell composition

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

## Theme Rule

`ui-dsl` stays neutral. Its defaults should use `CoreUiTheme`, while authored samples and
games should opt into a named theme from `awake:engine:ui:ui-designsystem`.

## Overlay Rule

Treat `overlayBox(...)` as the primary authored overlay surface:

- use `UiModifier.align(...)` and `UiModifier.padding(...)` for placement
- use `UiBoxConstraints.widthSizeClass` for `Compact` / `Medium` / `Expanded` decisions
- keep `overlayShell(...)` as convenience sugar for simple corner HUDs, not the base mental model
