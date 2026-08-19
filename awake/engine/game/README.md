# Awake Engine Game

Core game application lifecycle, frame update coordination, and MVI store composition for [Awake](../../README.md). Mediates between windowing, rendering backends, input dispatching, and ECS game state without tight coupling.

## Installation

```kotlin
implementation(project(":awake:engine:game"))
```

## Key Primitives

- `GameApplication` — template-method game lifecycle managing backend resources and the main frame loop.
- `Game` — interface defining game initialization (`ready`), frame updates (`update`), rendering (`render`), and teardown (`dispose`).
- `GameSpec` / `GameSpecBuilder` — declarative builder specifying initial window size, render features, clear color, and scene configuration.
- `GameModule` — modular game feature registration.
- `FrameStats` — FPS tracking, frame delta times, and performance metrics.

## Usage Example

```kotlin
import io.github.ronjunevaldoz.awake.engine.game.Game
import io.github.ronjunevaldoz.awake.engine.game.GameSpec

val gameSpec = GameSpec.builder()
    .title("My Awake Game")
    .width(1280)
    .height(720)
    .build()
```

## Related Modules

- [`:awake:engine:app`](../app/README.md) — platform host applications (Desktop, Android, iOS, Wasm).
- [`:awake:engine:game-authoring`](../game-authoring/README.md) — high-level authoring DSLs (`gameApp { }`, `gameModule { }`).
- [`:awake:scene:runtime`](../../scene/runtime/README.md) — ECS scene runtime driver.
