# Awake UI DSL

`awake:engine:ui-dsl` is the authored overlay/composition layer on top of `ui-core`,
`ui-unstyled`, and the neutral UI contracts in this module.

Use it when you want:

- `gameUi { ... }`
- `game { ui { ... } }`
- `gameModule { ui { ... } }`
- `overlayBox(...) { constraints -> ... }` for Box-style, responsive overlays
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

Responsive overlay example:

```kotlin
val spec = gameUi {
    overlay { viewportWidth, viewportHeight ->
        overlayBox(viewportWidth, viewportHeight) { constraints ->
            val compact = constraints.isCompact

            panel(
                id = "hud",
                width = if (compact) Dimension.FillMax else 280f.toDimension(),
                height = Dimension.WrapContent,
                modifier = UiModifier()
                    .align(if (compact) UiAlignment.TopStart else UiAlignment.TopEnd)
                    .padding(16f.dp)
            ) {
                text("Status")
                metaText("Mode: Orbit")
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
stay in `ui-core`, reusable widgets stay in `ui-unstyled`, and branded recipes stay in
`ui-designsystem`.

## Overlay guidance

- Prefer `overlayBox(...)` when layout depends on available space, alignment, or a
  `Compact` / `Medium` / `Expanded` breakpoint.
- Treat `overlayBox(...)` as the Compose-style root `Box`, with `UiModifier.align(...)`
  and `UiModifier.padding(...)` driving placement.
- Use `overlayShell(...)` only as optional sugar for simple corner-anchored HUDs.
- `UiBoxConstraints.widthSizeClass` is the intended entry point for responsive overlay
  layout decisions.
- `UiBoxConstraints.maxWidth` / `maxHeight` are density-independent logical sizes; use
  `maxWidthPx` / `maxHeightPx` only when you truly need framebuffer-pixel values.

Defaults in this module fall back to `CoreUiTheme` so the API stays dependency-light.
Real authored apps and samples should pass a named theme from `awake:engine:ui-designsystem`.
