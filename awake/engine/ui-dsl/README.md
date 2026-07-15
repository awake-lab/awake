# Awake UI DSL

`awake:engine:ui-dsl` is the authored overlay/composition layer on top of `ui-core`,
`ui-widgets`, and the neutral UI contracts in this module.

Use it when you want:

- `gameUi { ... }`
- `game { ui { ... } }`
- `gameModule { ui { ... } }`
- `overlayShell(...) { topLeft { ... } topRight { ... } }`
- authored overlay composition over reusable widgets and primitives

Minimal example:

```kotlin
val spec = gameUi {
    overlay { _, _ ->
        column(x = 16f, y = 16f, width = 180f) {
            text("Inspector")
        }
    }
}
```

Overlay shell example:

```kotlin
val spec = gameUi {
    overlay { viewportWidth, viewportHeight ->
        overlayShell(viewportWidth, viewportHeight) {
            topRight(width = 280f, height = 120f) { slot ->
                shellPane(slot = slot, id = "status") {
                    text("Ready")
                }
            }
        }
    }
}
```

Composed through the higher-level game facade:

```kotlin
val game = game {
    module {
        ui(spec)
    }
}
```

Rule of thumb: this module owns authored UI syntax. Low-level layout/drawing primitives
stay in `ui-core`, reusable widgets stay in `ui-widgets`, and branded recipes stay in
`ui-designsystem`.

Defaults in this module fall back to `CoreUiTheme` so the API stays dependency-light.
Real authored apps and samples should pass a named theme from `awake:engine:ui-designsystem`.
