# Awake Scene DSL

`awake:scene-dsl` is the authored scene layer that sits on top of `awake:scene`.

Use it when you want:

- `sceneGame { ... }`
- `game { ecs { ... } }`
- authored entities, assets, systems, and scene update blocks

Minimal example:

```kotlin
val spec = sceneGame {
    name("example")
    cameraEntity("camera") {
        primary(true)
    }
    meshEntity(
        name = "cube",
        mesh = "cube",
        material = "default"
    )
}
```

Composed through the higher-level game facade:

```kotlin
val game = game {
    window { title = "Scene Example" }
    ecs(spec)
}
```

Rule of thumb: this module owns authored scene syntax. Runtime/spec/install surfaces such
as `SceneGameSpec` and `SceneGameRuntime` stay in `awake:scene`.
