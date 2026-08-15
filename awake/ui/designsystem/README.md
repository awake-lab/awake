### Design System (`awake:ui:designsystem`)

A styled component library for Awake's immediate-mode UI, built on top of `awake:ui:headless`
and modelled after the [shadcn/ui](https://ui.shadcn.com/) design language.

---

### What is a Design System?

A design system is the layer that turns unstyled, accessible primitives into components that
look and behave consistently across every screen in the app. It owns:

- **Visual tokens** — colors, spacing, radius, typography, and shadow defined as a theme
- **Component recipes** — functions like `shadcnButton`, `shadcnCard`, `shadcnInput` that
  apply those tokens to headless primitives via `Style`
- **Variant systems** — `ShadcnButtonVariant`, `ShadcnBadgeVariant`, etc. — sealed enums that
  resolve to the correct token set for each visual state (primary, secondary, outline, ghost,
  danger, …)

The design system **does not own layout logic or interaction behavior**. Those live in
`awake:ui:headless`. A recipe function calls a headless primitive and wraps it with the
correct style; it does not re-implement scrolling, focus, or hit-testing.

---

### Why shadcn?

shadcn/ui is not a component library you install — it is a reference design built from a set
of composable, copy-pasteable primitives. Two properties made it a good fit for Awake:

1. **Unstyled core, styled surface** — shadcn's own components sit on top of Radix UI
   headless primitives, which is exactly the `headless → designsystem` layering Awake already
   uses. Porting a shadcn component means mapping its Tailwind tokens to Awake's equivalent
   `ShadcnTokens` / `Tw.*` values, not re-architecting the component.

2. **Well-documented token contract** — Every shadcn component documents the exact CSS
   variables it reads (`--primary`, `--muted-foreground`, `--radius`, etc.). Awake's
   `ShadcnTheme` maps those variables to `OklchColor` values per preset, giving the same
   theming flexibility without a browser runtime.

---

### Module structure

```
designsystem/
  components/          # Public recipe functions (one file per component family)
    ShadcnButtonRecipes.kt
    ShadcnInputRecipes.kt
    ShadcnTypography.kt
    ShadcnNavigationRecipes.kt
    ShadcnOverlayRecipes.kt
    ShadcnSurfaceRecipes.kt
    ... (22 recipe files total)
  styles/              # Variant enums and Style resolvers
    ShadcnButtonStyles.kt
    ShadcnBadgeStyles.kt
    ...
  theme/               # Token definitions
    ShadcnTokens.kt    # Spacing, radius, text-scale tokens (backed by Tw.*)
  ShadcnTheme.kt       # Theme entry-point — color palette, preset, accent, dark mode
  OklchColor.kt        # OKLCH color value type used by the theme
  PresetUiThemes.kt    # Built-in presets (Default, Rose, Blue, …)
```

### Theme scope

Core owns the neutral runtime locals and fallback theme. This module owns the branded entry
point: use the lower-case `UiScope.shadcnTheme(...)` extension to provide a named shadcn theme.
It establishes the theme and default text style for recipes without passing theme or typography
through component parameters. Recipes may read the design-system-local `themeValues` accessor;
Headless primitives must not.

Use `shadcnThemeValues(...)` when constructing a plain Core theme value outside a composition
scope.

Use `ShadcnThemeValues` at the `shadcnTheme(...)` boundary when a subtree needs custom Shadcn
metrics. Public recipes use semantic parameters such as `variant`, `size`, `tone`, and
`emphasis`; their generic `Style` composition remains an internal recipe concern.

Put each component's visual factories in its dedicated `Shadcn<Family>Styles.kt` file. That file
owns its neutral, hover, active, disabled, and variant `Style` values; recipe files compose those
styles with Headless behavior.

---

### Dependency rule

```
awake:ui:designsystem
    └── awake:ui:headless   (public API surface)
    └── awake:ui:ui-core    (internal theme/style infrastructure only)
    └── awake:ui:tailwind   (spacing / text-scale tokens)
    └── awake:ui:heroicons  (icon set)
    └── awake:ui:ui-api     (Dp, UiBounds, UiThemeValues)
```

`awake:ui:ui-core` is an `implementation` dependency: it may support theme/text providers,
CompositionLocal mechanics, and `Style` resolution, but Core types must not appear in the Design
System's public API. Component recipes call Headless widgets for behavior and must not call Core
layout, drawing, hit-testing, slot-claiming, or semantic primitives directly. A need to do so
means Headless needs a generic API or slot.

---

### Adding a new component

1. Add a recipe file under `components/` named `Shadcn<Family>Recipes.kt`.
2. If the component has visual variants, add a `Shadcn<Family>Variant` sealed class under
   `styles/` and write a `Style` factory/extension that maps each variant and interaction state.
3. Call the matching headless primitive (`UiScope.button`, `UiScope.surface`, etc.) and pass
   the resolved style.
4. Add a preview fixture in `samples:ui-showcase` and verify with the parity CLI:
   ```
   scripts/awake ui parity <component>
   ```
