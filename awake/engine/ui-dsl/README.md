# Awake UI DSL

`awake:engine:ui-dsl` is the authored overlay/composition layer on top of `ui-core`,
`ui-widgets`, and the neutral UI contracts in this module.

Use it when you want:

- `gameUi { ... }`
- `game { ui { ... } }`
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

Composed through the higher-level game facade:

```kotlin
val game = game {
    ui(spec)
}
```

Rule of thumb: this module owns authored UI syntax. Low-level layout/drawing primitives
stay in `ui-core`, reusable widgets stay in `ui-widgets`, and branded recipes stay in
`ui-designsystem`.
