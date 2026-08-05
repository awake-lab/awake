# Awake Scene DSL

`awake:scene-dsl` is the authored scene layer that sits on top of `awake:scene`.

Use it when you want:

- `sceneGame { ... }`
- `game { ecs { ... } }`
- `gameModule { ecs { ... } }`
- authored entities, assets, fixed-step systems, frame systems, and scene update blocks

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
    module {
        ecs(spec)
    }
}
```

Rule of thumb: this module owns authored scene syntax. Runtime/spec/install surfaces such
as `SceneGameSpec` and `SceneGameRuntime` stay in `awake:scene`.

## System scheduling

`awake-ecs` systems do not carry their own timing policy. Scene scheduling is explicit at
registration:

```kotlin
fixedSystem("gameplay") { GameplaySystem() } // deterministic simulation/physics step
frameSystem("camera") { CameraSystem() }     // once per rendered frame
```

Use `fixedSystem` for deterministic gameplay or physics work. Use `frameSystem` for camera
controllers, transform propagation, rendering, and demo drivers that must run exactly once
for every presented frame.
