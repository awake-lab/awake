---
name: awake-shadcn-consuming
description: >
  Consume Awake's in-repository shadcn design system from a sample, game, or application.
  Use whenever a task adds or changes a screen that calls shadcn* components, installs a
  Shadcn theme, or asks how an Awake app should style UI. Do not use for the external
  io.github.ronjunevaldoz:shadcn-compose Maven library, or for maintaining ui-designsystem
  recipes themselves.
---

# Consuming Awake Shadcn UI

Use this skill at call sites: games, samples, tools, and feature UI that consume Awake's
in-repository `awake:engine:ui:ui-designsystem` module. For implementation inside the design
system, use `awake-shadcn-styling` instead; for choosing the ownership layer of a new behavior,
use `awake-ui-authoring`.

## Start with the named design-system theme

Install a named design-system theme at the application root. Do not rely on Core's neutral
fallback theme in a sample or game.

```kotlin
uiScope.shadcnTheme(values = ShadcnDefaultTheme) {
    shadcnButton(id = "save") { text("Save") }
}
```

Use `shadcnThemeValues(...)` only when a pure `UiThemeValues` value is required before a
`UiScope` exists, such as application state or a host configuration. It is not the scoped
provider:

```kotlin
val appTheme = shadcnThemeValues(dark = isDark)
```

Use a complete [ShadcnThemeValues] value at that same scoped boundary when the product needs
custom Shadcn metrics. It never changes one component ad hoc:

```kotlin
uiScope.shadcnTheme(
    theme = ShadcnThemeValues(
        core = appTheme,
        metrics = ShadcnTheme.metrics,
    ),
) {
    shadcnButton(id = "save", label = "Save")
}
```

## Consume recipes; do not restyle Headless in app code

Use `shadcnButton`, `shadcnSurface`, `shadcnInput`, and other `shadcn*` recipes for visible UI.
Choose their named variant and size. An app or sample must not build its own `Style { ... }`
for a component or call a Headless widget merely to establish a custom look. If the needed
variant does not exist, add it in `ui-designsystem` with the official shadcn reference and
parity coverage.

Shadcn recipes do not expose a public `Style` override. Express local intent with their named
parameters (`variant`, `size`, `tone`, `emphasis`); put a product-wide visual change in a complete
`ShadcnThemeValues` value.

Headless remains appropriate for generic structure and behavior when a design-system recipe
already supplies the visible component. Never make an application theme provider or a local
visual-token registry to compensate for a missing recipe.

## Keep imports and dependencies one-way

Depend on `ui-designsystem` to access named themes and recipes. It brings the required Headless
behavior boundary with it. Application code may use public Headless layout/interaction APIs when
needed, but should not import Core primitives or style its own component surfaces.

## Before finishing

- Use a named design-system theme at the app or sample root.
- Use `UiScope.shadcnTheme(...)` for scoped provision and `shadcnThemeValues(...)` only for a
  pure value.
- Use named `shadcn*` variants instead of app-authored component `Style` blocks.
- Use `ShadcnThemeValues` at the root for Shadcn metric customization, never as a
  per-component visual escape hatch.
- Keep behavior gaps in `ui-headless`; keep new look/variants in `ui-designsystem`.
- Compile the affected sample or game target and run its focused UI tests.

Read [UI ownership](../../docs/reference/ui-ownership.md) for the canonical module boundaries.
