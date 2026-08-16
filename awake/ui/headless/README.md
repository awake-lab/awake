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
  or a full-width CTA — because the caller provides `Style`, not the
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
| **Surface** | `Surface.kt` | Generic container and interaction primitive; appearance comes from caller-supplied `Style` |
| **Scroll** | `ScrollState.kt` | `rememberScrollState`, `verticalScroll`, `horizontalScroll` |
| **Status** | `Status.kt` | Progress, slider, spinner |
| **Media** | `Avatar.kt`, `Icon.kt`, `Canvas.kt` | Image fallbacks, vector icons, raw canvas |
| **Utility** | `Separator.kt`, `Text.kt`, `Animation.kt`, `State.kt` | Lines, text primitives, animation values, state hooks |
| **Metrics** | `UiScopeMetrics.kt` | Frame-level size queries |

---

### What headless does NOT own

- Color values, font faces, radii, or any design token → those belong in `designsystem`
- Padding or size constants specific to a component family (e.g. button minimum height)
  → those are set by `Style` passed in by the recipe
- Theme provisioning or ambient theme, typography, and text-style access → Core owns the
  neutral local machinery; a design system exposes its branded provider
- Application-level layout (sidebars, page chrome, split views) → those belong in the
  consuming app or `samples`

### Testing Headless components

Use `renderUiComponent(...)` from `awake:ui:testing` for single-frame behavior, semantic, and
snapshot fixtures. Use `uiTestSession(...)` for persistent input across frames. A test installs
`shadcnTheme { ... }` through the helper's `rootProvider` when it exercises a Shadcn recipe.
Do not repeat the `UiContext`/frame/font/theme setup in ordinary component tests; direct context
construction is reserved for Core layout, rasterizer, and real-backend probes.
For ordinary pointer interactions use the session's `hover`, `click`, `doubleClick`,
`longPress`, `rightClick`, and `drag` gestures; use exact input frames only for wheel or keyboard
behavior.

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

### Base UI component parity

Every component in [Base UI's directory](https://base-ui.com/react/components), checked
against **this module's own primitives only** (function-level, not filename guessing — see
`docs/reference/ui-validation.md`'s "proof over eyeballing" rule). `designsystem` depends on
`headless`, never the reverse — see the dependency rule above and
[`docs/reference/ui-ownership.md`](/Users/ronvaldoz/StudioProjects/awaken/docs/reference/ui-ownership.md)
— so this table stays scoped to what exists at this layer; it says nothing about `shadcn`
skins, which belong to `designsystem`'s own doc.

| Base UI component | Backed by | Status | How it maps |
|---|---|---|---|
| Accordion | `accordion` | present | 1:1 |
| Alert Dialog | `dialog` | present | 1:1 |
| Autocomplete | `combobox` | present | same filter+select mechanic, one name |
| Avatar | `avatar` | present | 1:1 |
| Button | `button` | present | 1:1 |
| Checkbox | `checkbox` | present | 1:1 |
| Checkbox Group | `toggleGroup(options, selectedIndices: Set<Int>)` | present | a set of togglable options, already what this returns |
| Collapsible | `collapsible` | present | 1:1 |
| Combobox | `combobox` | present | 1:1 |
| Context Menu | `contextMenuTrigger` | present | 1:1 |
| Dialog | `dialog` | present | 1:1 |
| Drawer | `popup` | present | slide-in is a placement variant of the same popup mechanic, not its own primitive |
| Field | `field` | present | 1:1 |
| Fieldset | `column` | present | grouping fields is layout, not new behavior |
| Form | `field` | present | validation composes over repeated `field` calls, no dedicated form runtime |
| Input | `textField` | present | 1:1 |
| Menu | `menu` | present | 1:1 |
| Menubar | `menubar` (`ActionRow.kt`) | present | thin `surface`+`row` composition, no roving-focus/keyboard-nav semantics yet |
| Number Field | `numberField` | present | 1:1 |
| OTP Field | `otpInput` (`OtpInput.kt`) | present | owns focus/value/separator-placement; caller supplies each slot's/separator's visuals via lambda |
| Radio | `radio` | present | 1:1 |
| Select | `select` | present | 1:1 |
| Slider | `slider` | present | 1:1 |
| Switch | `switch` | present | 1:1 |
| Toggle | `toggle` | present | 1:1 |
| Toggle Group | `toggleGroup` | present | 1:1 |
| Meter | `progress` | present | same value/range primitive, Meter just drops the indeterminate state |
| Navigation Menu | `menu`, `tabs` | present | breadcrumb/tab/sidebar navigation in this project compose from these two; nothing here needs a mega-menu |
| Popover | `popup` | present | 1:1 |
| Preview Card | `popup` | present | anchored content-on-demand is the same mechanic; only the hover-intent delay is missing, and nothing asks for it |
| Progress | `progress` | present | 1:1 |
| Scroll Area | `rememberScrollState` | present | 1:1 |
| Separator | `separator` | present | 1:1 |
| Tabs | `tabs` | present | 1:1 |
| Toast | `toast` | present | 1:1 |
| Toolbar | `toolbar` (`ActionRow.kt`) | present | thin `surface`+`row` composition, no ARIA toolbar semantics yet |
| Tooltip | `tooltip` | present | 1:1 |
| CSP Provider, Direction Provider, mergeProps, useRender | — | n/a | React-only plumbing, nothing to map |

**37 real Base UI components (the React-only utils don't count). 37/37 present.**

Menubar and Toolbar (`ActionRow.kt`) reduce to the same shape at this layer — a
`Panel`-semantic `surface` wrapping a `row` — so one private `actionRow` backs both public
names rather than duplicating the composition. Neither adds keyboard roving-focus/ARIA menu
semantics Base UI's real versions have; extend `actionRow` if a consumer's UX needs that later,
per
[`skills/awake-ui-authoring/SKILL.md`](/Users/ronvaldoz/StudioProjects/awaken/skills/awake-ui-authoring/SKILL.md)'s
no-speculative-behavior rule. Test coverage: `ActionRowWidgetsTest.kt`. A `designsystem` skin
(`shadcnMenubar`/`shadcnToolbar`) is written separately when a real caller needs one, same as
every other primitive in this file — see
[`docs/reference/ui-validation.md`](/Users/ronvaldoz/StudioProjects/awaken/docs/reference/ui-validation.md)
for the required proof any new skin ships with.

---

### Adding a new primitive

1. Add a file under `headless/` named after the primitive concept (e.g. `Slider.kt`).
2. Write the function as a `UiScope` extension returning `UiBounds`.
3. Accept a `Modifier` and an optional `style: Style` parameter — **do not
   hard-code any visual value** inside the primitive itself.
4. Record a semantic node with the appropriate `UiSemanticRole` so tests and the overlay
   debugger can identify it.
5. The matching `ShadcnXxxRecipes.kt` in `designsystem` is written separately and calls this
   primitive with a token-resolved style.
