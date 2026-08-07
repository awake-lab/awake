---
name: awake-ui-authoring
description: Which UI layer to write in - ui-core vs ui-unstyled vs ui-designsystem - and the size/spacing rules that keep them separate. Read before adding or changing any UI widget, before adding a `.dp` or pixel constant to a widget, and before naming a new primitive. Trigger keywords - ui-core, ui-unstyled, ui-designsystem, shadcn, widget, primitive, component, padding, spacing, size, theme token, UiComponentStyles, Modifier, headless, unstyled.
---

# Authoring UI in Awake

Three layers, each with one job. Putting code in the wrong one is the single most common
defect in this stack, and it fails *silently* -- the UI still renders, it just renders someone
else's design language.

| Layer | Owns | Never contains |
|---|---|---|
| `ui-core` | Geometry, layout, slots, drawing, anchoring, clipping, modifiers, the theme *contract* | Any widget; any brand name |
| `ui-unstyled` | Widget **behaviour** and structure -- what a checkbox *is* and does | Any size or colour it cannot derive; any design language's vocabulary |
| `ui-designsystem` | The shadcn **look** -- sizes, colours, radii, variants | Behaviour that another skin would also need |

Decide with one question: **"would a differently-skinned product still need this code?"** Yes ->
`ui-unstyled`. No -> `ui-designsystem`.

## The size rule (this is the one that bites)

> A `ui-unstyled` widget may only fall back to a size it can derive from **its own content**,
> **its own font/vector metrics**, or a **physical constraint** (1 device pixel). Any size it
> cannot derive must come from `UiTheme`.

Not "no defaults". Defaults are mandatory here -- this is an immediate-mode UI with a single
measure pass, so a widget with no intrinsic size measures to **zero** and silently draws
nothing. The question is never *whether* to have a fallback, only whether it is derivable.

`textarea()` is the reference implementation: its height is
`fontHeight * minLines + lineGap + contentPadding` -- no magic numbers, scales with the font,
correct under any theme. Copy that shape.

### Why this matters more than it looks

A Material-flavoured `40.dp` button fallback sat in `ui-unstyled`. Nothing overrode it, so it
became the de-facto default -- and then it propagated **upward**: `ShadcnButtonSize.Md` was set
to `40f` to match, and shadcn's real button is `h-9` = **36px**. The comment in
`ShadcnAvatars.kt` says the quiet part out loud -- the avatar size was chosen to match "this
module's pre-existing 40dp default", not to match shadcn (`size-8` = 32px).

An unstyled default is not a neutral placeholder. It becomes the spec.

Sizes belong in `UiComponentStyles` (`ui-core`), which already carries a per-component slot for
button, toggle, checkbox, slider, dropdown, surface, textField and avatar. `CoreUiComponentStyles`
supplies neutral values; `ShadcnTheme` overrides with the real shadcn figures. Add a field there
rather than a constant in a widget.

## Units: authored values are `Dp`, never raw pixels

Widget code lives in density-independent units. A raw `Float` added to a pixel-space coordinate
renders half-size on a 2x display and looks fine on the machine that wrote it.

```kotlin
// Wrong -- physical pixels, does not scale
private const val LABEL_GAP = 8f
x += LABEL_GAP

// Right -- authored in Dp, converted at the point of use
private val LABEL_GAP = 8f.dp
x += LABEL_GAP.toPx()
```

If surrounding arithmetic is genuinely px-based, still source the value from a `Dp` and convert
at use. `awake.ui-authored-units-convention` is meant to police this; do not rely on it alone.

## Naming: Radix is canonical

Adopt the Compose name only where Compose and Radix agree, or where Radix has no concept.

Reasoning: there is no Compose here -- no `@Composable`, no recomposition, no androidx
`Modifier`. A Compose name promises a runtime this engine does not have. The behaviour-parity
target is Radix, so names should track the contract they are held to. In practice the two
conventions agree on nearly everything (Checkbox, Switch, Slider, Separator, Button, Card,
Dialog, Tabs), and where they differ the Compose name has been the one that was *wrong about
the behaviour*.

- `select()`, not `dropdown()` -- it returns a chosen value, it is not a menu.
- `progress()`, not `progressBar()` -- the latter is Android View-era.
- A file's name must match what it exports. A file called `BasicText.kt` that exports no
  `basicText()` is lying.
- Styled counterparts are `shadcn<Widget>` over the same base name, so the base names must
  *be* the shadcn names for the pairing to read.

For anything Radix has no name for, the Awake name wins -- `*Slot` overloads are an Awake
invention and a good one; keep them consistent.

## Behaviour belongs underneath, even when only one skin exists today

If a component owns real structure -- measurement, selection resolution, open/close state,
traversal -- that behaviour belongs in `ui-unstyled` even if `ui-designsystem` is its only
current caller. Several components (Tabs, Collapsible, Dialog, DropdownMenu) were built
styled-first and now own behaviour that cannot be reused or reskinned without dragging shadcn
in. That is a debt, not a pattern to copy.

The exception is genuine *composition*: a branded arrangement of existing primitives with no new
behaviour is correctly design-system-only.

## Theme access from `ui-unstyled` is correct, and not a layering violation

`UiTheme` lives in `ui-core`, one layer *below* unstyled -- reading it is depending downward on
an injected abstraction. `?: theme.colors.foreground` fallbacks are sanctioned and there are
~45 of them; leave them alone.

The violation is different: unstyled deciding *which* semantic token carries meaning (choosing
`secondary` to mean "checked"), or baking in decoration such as a border width. That is a design
decision wearing a theme lookup's clothing.

## Checklist

- [ ] Would another skin need this code? If yes it is not design-system code.
- [ ] Every size in an unstyled widget is content-derived, metric-derived, a physical minimum,
      or read from `UiTheme`.
- [ ] No raw-pixel `Float` constants; authored values are `Dp`, converted at use.
- [ ] No decoration (border width, chosen semantic colour) in `ui-unstyled`.
- [ ] Name matches the Radix concept; file name matches what the file exports.
- [ ] New behaviour landed in `ui-unstyled`, not in the `shadcn*` wrapper.
