# Awake Game DSL

`awake:engine:game-dsl` is the authored entrypoint for assembling a game from reusable
contracts in `awake:engine:game`.

Use it when you want:

- `game { ... }` and `gameSpec { ... }`
- `gameModule { ... }` for reusable authored game content
- `feature.createGame { ... }` and `feature.createGameSpec { ... }` for wrapping a reusable module in a root shell
- install-time service wiring
- window configuration without touching `GameSpecBuilder` directly

Minimal example:

```kotlin
val game = game {
    window {
        title = "Example"
        size(1280, 720)
        backend.vulkan()
    }
}
```

Reusable module example:

```kotlin
val feature = gameModule {
    service(String::class, "hello-cube")
}

val game = feature.createGame {
    title = "Hello Cube"
    size(1600, 900)
    backend.vulkan()
}
```

When scene and UI DSL modules are on the classpath, the same `game {}` block can compose
higher-level authored specs:

```kotlin
val spec = gameSpec {
    window { title = "Example" }
    module(
        gameModule {
            ecs(exampleSceneSpec())
            ui(exampleUiSpec())
        }
    )
}
```

Rule of thumb: this module owns builder syntax only. Stable contracts such as
`AwakeGame`, `GameSpec`, `GameInstaller`, and `GameWindowConfig` stay in
`awake:engine:game`.
