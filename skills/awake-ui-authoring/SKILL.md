---
name: awake-ui-authoring
description: Which UI layer to write in - ui-core vs ui-headless vs ui-designsystem - and the size/spacing rules that keep them separate. Read before adding or changing any UI widget, before adding a `.dp` or pixel constant to a widget, and before naming a new primitive. Trigger keywords - ui-core, ui-headless, ui-designsystem, shadcn, widget, primitive, component, padding, spacing, size, theme token, UiComponentStyles, Modifier, headless, headless.
---

# Authoring UI in Awake

Three layers, each with one job. Putting code in the wrong one is the single most common
defect in this stack, and it fails *silently* -- the UI still renders, it just renders someone
else's design language.

| Layer | Owns | Never contains |
|---|---|---|
| `ui-core` | Geometry, layout, slots, drawing, anchoring, clipping, input/state mechanics, neutral fallback resolution | Any widget recipe, variant, or brand name |
| `ui-headless` | Widget **behaviour**, structure, and neutral interaction visual states | Any named design-language vocabulary or branded visual policy |
| `ui-designsystem` | The shadcn **look** -- sizes, colours, radii, variants | Behaviour that another skin would also need |

Decide with one question: **"would a differently-skinned product still need this code?"** Yes ->
`ui-headless`. No -> `ui-designsystem`.

## The size rule (this is the one that bites)

> A `ui-headless` widget may only fall back to a size it can derive from **its own content**,
> **its own font/vector metrics**, or a **physical constraint** (1 device pixel). Any size it
> cannot derive must be supplied as neutral widget visual/layout data by the caller above it.

Not "no defaults". Defaults are mandatory here -- this is an immediate-mode UI with a single
measure pass, so a widget with no intrinsic size measures to **zero** and silently draws
nothing. The question is never *whether* to have a fallback, only whether it is derivable.

`textarea()` is the reference implementation: its height is
`fontHeight * minLines + lineGap + contentPadding` -- no magic numbers, scales with the font,
correct under any theme. Copy that shape.

### Why this matters more than it looks

A Material-flavoured `40.dp` button fallback sat in `ui-headless`. Nothing overrode it, so it
became the de-facto default -- and then it propagated **upward**: `ShadcnButtonSize.Md` was set
to `40f` to match, and shadcn's real button is `h-9` = **36px**. The comment in
`ShadcnAvatars.kt` says the quiet part out loud -- the avatar size was chosen to match "this
module's pre-existing 40dp default", not to match shadcn (`size-8` = 32px).

An headless default is not a neutral placeholder. It becomes the spec.

Do not add a `UiComponentStyles` registry to `ui-core`. A per-component size/recipe registry is
visual policy and inevitably turns Core into a hidden design system. Headless may accept a
generic visual-state contract; `ui-designsystem` supplies the actual branded sizes, colours, and
radii by mapping its named variant to that neutral contract. A Headless fallback must remain
content-derived, metric-derived, or a physical constraint.

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

## Fill the axis through `Modifier`, never `Dimension.FillMax`

`Dimension.FillMax` is a Core layout-resolution sentinel, not authored UI vocabulary. Public
Headless calls express intent with Compose-style modifiers:

```kotlin
Modifier.fillMaxWidth()
Modifier.fillMaxHeight()
Modifier.fillMaxSize()
```

Use `width(48.dp)` or `height(48.dp)` for a fixed authored dimension. Do not expose a public
`width(Dimension)` or `height(Dimension)` overload just to make `FillMax` available.

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
traversal -- that behaviour belongs in `ui-headless` even if `ui-designsystem` is its only
current caller. Several components (Tabs, Collapsible, Dialog, DropdownMenu) were built
styled-first and now own behaviour that cannot be reused or reskinned without dragging shadcn
in. That is a debt, not a pattern to copy.

The exception is genuine *composition*: a branded arrangement of existing primitives with no new
behaviour is correctly design-system-only.

## Headless consumes neutral visuals, not a theme recipe

Runtime-free theme value contracts live in `ui-api`; the Core runtime resolves them. Headless
may consume injected **neutral** values or generic visual states, but it must not choose a
semantic role for itself (for example, deciding `secondary` means "checked"), select a border
width, or name a visual variant. Existing direct `UiTheme`/Core-style reads are migration bridges
to remove from new APIs, not a model for new widget code.

The target is a generic state model such as
`SurfaceVisuals(rest, hovered, pressed, disabled)`. `ui-designsystem` maps `Primary`, `Ghost`,
or a shadcn preset to that model; Headless only applies the state it receives.

## One entry point per widget: `UiScope`, not one overload per scope

A widget gets ONE public function, on `UiScope`. Do not add a `ColumnScope`/`RowScope`/
`BoxScope`/`AbsoluteScope` overload of it.

`ui-core`'s `surface()` is the counter-example still in the tree -- six functions for one widget:

| overload | what it actually changes |
|---|---|
| `UiPrimitiveScope.surface` (public) | entry point |
| `UiPrimitiveScope.surface` (impl) | the real body |
| `ColumnScope.surface` | width defaults to `FillMax` |
| `RowScope.surface` | height defaults to `FillMax` |
| `AbsoluteScope.surface` | **nothing** -- `modifier = modifier` |
| `BoxScope.surface` | **nothing** -- `modifier = modifier` |

Two do literally nothing. The two that matter encode one rule -- "fill my cross axis by default"
-- which belongs on the scope (ask it for its main axis) rather than being spelled out once per
scope type. `ui-headless` and `ui-designsystem` already made this move; `ui-core` is the holdout.

Why this matters beyond tidiness: overload sets hide capability gaps. Resolution picks the most
specific receiver silently, so a scope that lacks the overload falls through to a base version
that quietly ignores what the caller asked for. That is the same failure mode as
`Modifier.verticalScroll` on `row()`/`box()` -- accepted, dropped, no error -- which cost a full
session to find. One function per widget makes an unsupported combination a compile error or an
explicit `error(...)`, not a silent no-op.

When a scope genuinely needs different behaviour, branch on data the scope exposes inside the one
function. Add an overload only when the signature itself must differ (different parameters, not
different defaults), and say why in a comment.

## Checklist

- [ ] Would another skin need this code? If yes it is not design-system code.
- [ ] Every size in a Headless widget is content-derived, metric-derived, a physical minimum,
      or provided through a generic Headless visual/layout contract -- never Core's component
      style registry.
- [ ] No raw-pixel `Float` constants; authored values are `Dp`, converted at use.
- [ ] No decoration choice (border width, chosen semantic colour) or named variant in
      `ui-headless`.
- [ ] Name matches the Radix concept; file name matches what the file exports.
- [ ] New behaviour landed in `ui-headless`, not in the `shadcn*` wrapper.
- [ ] One public function on `UiScope` -- no per-scope overload that only changes a default.
