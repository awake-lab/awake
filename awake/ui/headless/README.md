### Headless UI (`awake:ui:headless`)

The unstyled, behavior-only primitive layer for Awake's immediate-mode UI. `headless` owns
interaction, layout, focus, semantics, and accessibility — but deliberately owns **no visual
opinions** (no colors, no fonts, no radii). Styling is applied by the layer above it:
`awake:ui:designsystem`.

---

### What is Headless UI?

A headless component provides structure and behavior without any built-in appearance. You can
think of it as the logic you would write yourself for a button (click area, focus ring,
disabled state, keyboard activation) without any specific look attached.

This separation means:

- The same `button` primitive can be a pill badge, a sidebar menu item, an icon-only action,
  or a full-width CTA — because the caller provides the `SurfaceStyle` / `Style`, not the
  primitive itself.
- Tests can assert interaction and semantic correctness without depending on visual output.
- A new design theme can be built entirely in `designsystem` without touching any primitive
  behavior.

Awake's headless layer is inspired by [Base UI](https://base-ui.com/) (the successor to
MUI Base / Radix UI Primitives), which follows the same philosophy: ship the unstyled
interaction model, let the design system own the visual contract.

---

### What headless owns

| Category | File(s) | Description |
|---|---|---|
| **Layout** | `Layout.kt`, `LayoutScopes.kt` | `column`, `row`, `box`, `absolute`, `UiScope`, `ColumnScope`, `RowScope` |
| **Interactive** | `Button.kt`, `Selection.kt`, `Radio.kt`, `Tabs.kt` | Click, toggle, radio group, tab strip |
| **Input** | `Input.kt`, `NumberField.kt`, `Field.kt` | Text fields, number fields, labeled field wrappers |
| **Disclosure** | `Accordion.kt`, `Collapsible.kt` | Expand/collapse with animation hooks |
| **Overlay** | `Dialog.kt`, `Popup.kt`, `Tooltip.kt`, `Overlay.kt` | Modal dialogs, positioned pop-overs |
| **Navigation** | `Dropdown.kt`, `Menu.kt` | Menus, context menus, dropdown triggers |
| **Surface** | `Surface.kt` | Styled container that resolves `SurfaceStyle` against the active theme |
| **Scroll** | `ScrollState.kt` | `rememberScrollState`, `verticalScroll`, `horizontalScroll` |
| **Status** | `Status.kt` | Progress, slider, spinner |
| **Media** | `Avatar.kt`, `Icon.kt`, `Canvas.kt` | Image fallbacks, vector icons, raw canvas |
| **Utility** | `Separator.kt`, `Text.kt`, `Animation.kt`, `State.kt` | Lines, text primitives, animation values, state hooks |
| **Metrics** | `UiScopeMetrics.kt` | Frame-level size queries |

---

### What headless does NOT own

- Color values, font faces, radii, or any design token → those belong in `designsystem`
- Padding or size constants specific to a component family (e.g. button minimum height)
  → those are set by the `SurfaceStyle` / `Style` passed in by the recipe
- Application-level layout (sidebars, page chrome, split views) → those belong in the
  consuming app or `samples`

---

### Dependency rule

```
awake:ui:headless
    └── awake:ui:ui-core    (implementation only — not re-exported)
    └── awake:ui:ui-api     (Dp, UiBounds, UiThemeValues — part of public API)
```

`ui-core` types (`UiModifier`, `UiPrimitiveScope`) must **not** leak into `headless` public
signatures. `HeadlessModifier` is the internal adapter that wraps `UiModifier`; it is not a
second public modifier type.

---

### Adding a new primitive

1. Add a file under `headless/` named after the primitive concept (e.g. `Slider.kt`).
2. Write the function as a `UiScope` extension returning `UiBounds`.
3. Accept a `Modifier` and an optional `style: SurfaceStyle` / `Style` parameter — **do not
   hard-code any visual value** inside the primitive itself.
4. Record a semantic node with the appropriate `UiSemanticRole` so tests and the overlay
   debugger can identify it.
5. The matching `ShadcnXxxRecipes.kt` in `designsystem` is written separately and calls this
   primitive with a token-resolved style.