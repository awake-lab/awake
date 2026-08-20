# Game DSL

This page is the quick guide for Awake's root authored game shell.

## Goal

Keep the application shell small while letting reusable game content live in modules instead
of private sample helpers.

## The Two Main Shapes

### One-off root

Use `game { ... }` when everything belongs in one local composition root:

```kotlin
val game = game {
    window {
        title = "Example"
        size(1280, 720)
        backend.vulkan()
    }
}
```

### Reusable authored content

Use `gameModule { ... }` when the authored content should be reused, tested, or moved across
samples and future game modules:

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

That keeps:

- the root shell responsible for window and platform concerns
- the reusable module responsible for authored content and runtime wiring

### Stateful authored game

Use `gameDefinition(...) { ... }` when the reusable content also owns a runtime state
factory and a default window shell:

```kotlin
val definition = gameDefinition(createState = ::HelloCubeRuntimeState) {
    window {
        title = "Hello Cube"
        size(1600, 900)
        backend.vulkan()
    }
    module { state ->
        helloCubeGameModule(state)
    }
}

val game = definition.createGame()
```

That keeps:

- the library responsible for the repeated `state -> module -> windowed game` pattern
- the sample responsible only for its scene/UI/debug composition

## Routed Scene Flow

When a game owns multiple authored scenes, prefer `flow { ... }` or `sceneFlow { ... }`
instead of a sample-local router:

```kotlin
val spec = gameSpec {
    window {
        title = "Starter"
        size(1600, 900)
        backend.vulkan()
    }
    flow {
        start("overview")
        scene("overview", label = "Overview") { ... }
        scene("editor", label = "Editor") { ... }
    }
    ui { ... }
}
```

That keeps scene switching as a reusable engine contract instead of demo glue.

## Compose Modules

Modules can stack other modules:

```kotlin
val debugModule = gameModule { ... }
val hudModule = gameModule { ... }

val feature = gameModule {
    module(debugModule)
    module(hudModule)
}

val spec = feature.createGameSpec {
    title = "Composable"
    size(960, 540)
    backend.webGpu()
}
```

### Multi-feature sample shape

When a sample grows beyond one authored concern, keep the composition slices separate and
compose them at the app layer:

```kotlin
fun starterGameModule(state: StarterGameRuntimeState): GameModule = gameModule {
    module(starterSceneModule())
    module(starterUiModule(state))
    module(starterDebugModule(state))
}
```

That keeps:

- scene flow in a scene-owned feature module
- overlays in a UI-owned feature module
- debug wiring in a debug-owned feature module
- the app shell responsible only for choosing how those pieces are assembled

## Proof

The cookbook examples above are backed by executable tests and a generated report:

- [awake/engine/game-authoring/src/desktopTest/kotlin/io/github/ronjunevaldoz/awake/engine/application/GameDslTutorialDocsTest.kt](/Users/ronvaldoz/StudioProjects/awaken/awake/engine/game-authoring/src/desktopTest/kotlin/io/github/ronjunevaldoz/awake/engine/application/GameDslTutorialDocsTest.kt:1)
- `awake/engine/game-authoring/build/reports/game-dsl-tutorials/index.html`
- [samples/starter-game/src/commonTest/kotlin/io/github/ronjunevaldoz/awake/sample/startergame/app/StarterGameTest.kt](/Users/ronvaldoz/StudioProjects/awaken/samples/starter-game/src/commonTest/kotlin/io/github/ronjunevaldoz/awake/sample/startergame/app/StarterGameTest.kt:1)

Regenerate with:

```bash
./gradlew :awake:engine:bootstrap:desktopTest :awake:engine:bootstrap:gameDslTutorialDocsReport
```
