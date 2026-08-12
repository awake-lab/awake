### Awake Graphical User Interface (GUI)

Awake UI is an immediate-mode UI framework for the Awake engine.

### Folder structure

The following modules should be included in `settings.gradle.kts`:

```kotlin
include(":awake:ui:designsystem")
include(":awake:ui:font-atlas-generator")
include(":awake:ui:headless")
include(":awake:ui:heroicons")
include(":awake:ui:tailwind-generator")
include(":awake:ui:testing")
include(":awake:ui:ui-api")
include(":awake:ui:ui-core")
```

- `awake:engine:ui:`
    - `designsystem`:
        - Implementation of the design system components, following shadcn patterns
        - We must follow how components & styles approach
          from [shadcn-compose](https://github.com/ronjunevaldoz/shadcn-compose/blob/main/shadcn/core/src/commonMain/kotlin/io/github/ronjunevaldoz/shadcncompose),
          not yet discussed.
    - `font-atlas-generator`:
        - Tooling for generating signed distance field (SDF) font atlases
    - `headless`:
        - Unstyled, accessible UI components for building custom design systems
    - `heroicons`:
        - Integration for the Heroicons icon set
    - `tailwind-generator`:
        - Tooling to generate UI styles and themes based on Tailwind CSS patterns
    - `testing`:
        - Utilities and test harnesses for UI component testing, sugar-coating must use this
    - `ui-api`:
        - Experimental
        - Public interfaces and contracts exposed by the Awake UI framework.
        - TODO: Investigate the equivalent architectural boundary in Jetpack Compose and determine
          what should belong in this module.
    - `ui-core`:
        - Core primitives and internal implementation of the Awake UI system.
        - TODO: Review how Jetpack Compose separates its core modules and determine whether ui-core
          should be split further.

### Core

TODO: Study how Jetpack Compose divides runtime, ui, foundation, animation, and graphics-related
modules, then define equivalent boundaries for Awake without unnecessarily copying Compose's
structure.

- Graphics: Color, Vector (Path, ImageVector)
- Unit: Dp, Sp
- Animation: AnimatedVisibility, core.animateFloat
- Foundation: clickable, interaction, layout, shape, style, border, background, text (
  KeyboardOptions)
- Runtime: getValue, remember,
- Ui: Alignment, Modifier, draw, focus, graphics