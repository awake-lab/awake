### Design System (`awake:ui:designsystem`)

A styled component library for Awake's immediate-mode UI, built on top of `awake:ui:headless`
and modelled after the [shadcn/ui](https://ui.shadcn.com/) design language.

---

### What is a Design System?

A design system is the layer that turns unstyled, accessible primitives into components that
look and behave consistently across every screen in the app. It owns:

- **Visual tokens** — colors, spacing, radius, typography, and shadow defined as a theme
- **Component recipes** — functions like `shadcnButton`, `shadcnCard`, `shadcnInput` that
  apply those tokens to headless primitives via `SurfaceStyle` / `Style`
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

---

### Dependency rule

```
awake:ui:designsystem
    └── awake:ui:headless   (public API surface)
    └── awake:ui:tailwind   (spacing / text-scale tokens)
    └── awake:ui:heroicons  (icon set)
    └── awake:ui:ui-api     (Dp, UiBounds, UiThemeValues)
```

`awake:ui:ui-core` must **not** appear on the `designsystem` public compile classpath.
All primitive access goes through `headless`.

---

### Adding a new component

1. Add a recipe file under `components/` named `Shadcn<Family>Recipes.kt`.
2. If the component has visual variants, add a `Shadcn<Family>Variant` sealed class under
   `styles/` and write a `visuals()` extension that maps each variant to a `SurfaceStyle` or
   `Style`.
3. Call the matching headless primitive (`UiScope.button`, `UiScope.surface`, etc.) and pass
   the resolved style.
4. Add a preview fixture in `samples:ui-showcase` and verify with the parity CLI:
   ```
   scripts/awake ui parity <component>
   ```