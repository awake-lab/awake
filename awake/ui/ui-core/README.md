# Awake UI Core

Low-level declarative UI engine for [Awake](../../README.md) (`compose-ui` equivalent). Provides layout measurement passes, draw scopes, shape rendering, styling primitives, theme token infrastructure, and scroll containers. Zero external framework dependencies.

## Installation

```kotlin
implementation(project(":awake:ui:ui-core"))
```

## Key Primitives

- `Canvas` — declarative 2D drawing surface and clip scopes.
- `Layout` — measure/layout pass engine evaluating child box constraints.
- `Surface` — background fills, borders, rounded corners, and click handling.
- `Column`, `Row`, `Box` — core layout container primitives with `Arrangement` and `Alignment`.
- `Style` — declarative styling rules, state variants (hover, active, focus), and property cascades.
- `UiTheme` / `UiColorTokens` / `UiShapeTokens` — semantic design token infrastructure.

## Usage Example

```kotlin
import io.github.ronjunevaldoz.awake.ui.layouts.Column
import io.github.ronjunevaldoz.awake.ui.layouts.Row
import io.github.ronjunevaldoz.awake.ui.layouts.Surface
import io.github.ronjunevaldoz.awake.ui.api.dp

Surface {
    Column {
        Row {
            // Primitive layout tree
        }
    }
}
```

## Architecture Layering Rule

- `ui-core` sits at the lowest layer of the UI subsystem.
- **Consumer code must never import `ui-core` directly.** Applications and games render visible UI exclusively through `:awake:ui:designsystem` (`shadcn*` recipes).

## Related Modules

- [`:awake:ui:headless`](../headless/README.md) — unstyled interactive behavior primitives (`Button`, `Dialog`, `Dropdown`).
- [`:awake:ui:designsystem`](../designsystem/README.md) — complete Shadcn component suite and themes.
- [`:awake:ui:text`](../text/README.md) — font atlas rasterization and text layout engine.
