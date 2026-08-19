---
name: awake-ui-css-modifier
description: Translates CSS/Tailwind utility classes and inline JS styles to Awake's UiModifier/Style calls. Read when porting a web reference (shadcn/ui, a Figma-to-CSS export, a Tailwind class string) into Awake UI code, or when unsure which UiModifier/Style function a given CSS property maps to. Trigger keywords - w-auto, w-min, p-x, px-, py-, m-, margin, Tailwind class, CSS property, flexbox, justify-content, align-items, tailwind to kotlin, css to UiModifier.
---

# CSS / Tailwind → Awake `UiModifier`/`Style`

Awake's UI is not CSS -- there is no cascade, no `auto` sizing solved by a layout algorithm
guessing intent, and no box-model margin. Every mapping below is checked against the real
`UiModifier`/`Style`/`Dimension` source in `ui-core`, not assumed by CSS-name similarity.
Where there is no equivalent, this doc says so instead of inventing one.

## Sizing (`w-*`, `h-*`, `min-w-*`, `max-w-*`)

| CSS / Tailwind | Awake | Notes |
|---|---|---|
| `width: 200px` / `w-[200px]` | `.width(200f.dp)` | |
| `height: 200px` / `h-[200px]` | `.height(200f.dp)` | |
| `width: 100%` / `w-full` | `.fillMaxWidth()` or `.width(Dimension.FillMax)` | |
| `w-4`, `w-8`, ... (Tailwind spacing scale) | `.width(Tw.Spacing.s4)` | Use the vendored scale (`Tw.Spacing`, `ui-core`'s `tailwind/Tw.kt`) instead of hand-converting px, so a class and its Awake call stay numerically pinned to the same source. |
| `min-width: 100px` | `.widthIn(min = 100f.dp)` | `widthIn`/`heightIn` take nullable `min`/`max`, same shape as Compose's `Modifier.widthIn`. |
| `max-width: 400px` | `.widthIn(max = 400f.dp)` | |
| `width: auto` (`w-auto`) | **No 1:1 equivalent.** Omit the width modifier (leaf widgets resolve their own intrinsic size), or `.width(Dimension.WrapContent)` if the widget is a composite container that supports it (`WrapContent` is reserved for containers that can measure their own children -- see `Dimension`'s own doc comment, most leaf widgets don't implement it). |
| `width: min-content` (`w-min`) | **No equivalent exists.** `Dimension` is `Fixed` / `FillMax` / `WrapContent` only -- there is no min-content/max-content sizing mode. `WrapContent` is the closest available behavior but is not the same algorithm; don't claim parity. |
| `width: max-content` (`w-max`) | Same gap as `w-min` -- no equivalent. |

## Spacing (`p-*`, `m-*`)

| CSS / Tailwind | Awake | Notes |
|---|---|---|
| `padding: 16px` (`p-4`) | `.padding(Tw.Spacing.s4)` | Single-arg `padding(all: Dp)` sets all four sides. |
| `padding: 8px 16px` (`px-4 py-2`) | `.padding(horizontal = Tw.Spacing.s4, vertical = Tw.Spacing.s2)` | The two-arg `padding(horizontal, vertical)` overload is the direct Tailwind `px-*`/`py-*` match. |
| `padding-top: 8px` (`pt-2`) | `.paddingTop(Tw.Spacing.s2)` | Also `paddingBottom`/`paddingStart`/`paddingEnd` (logical start/end, not left/right -- matches RTL-aware Compose naming, not CSS's physical `pl-`/`pr-`). |
| `padding: 4px 8px 12px 16px` (`p-1 px-2 pb-3`-style explicit box) | `.padding(start, top, end, bottom)` | Four-arg overload, same order as CSS shorthand's *top/right/bottom/left* is NOT this order -- Awake's is `start, top, end, bottom`. Don't transcribe CSS shorthand order directly. |
| `margin: *` (`m-*`, `mx-*`, `my-*`) | **No equivalent. `UiModifier` has no margin concept at all.** | This is deliberate, not a gap to fill: Awake follows the Compose/immediate-mode convention where inter-sibling spacing is the *parent's* job (`row`/`column`'s `Arrangement.spacedBy(dp)`), not each child's own margin. A CSS `margin` between siblings translates to `Arrangement.spacedBy(...)` on the enclosing `row`/`column`, not a per-child modifier. A CSS margin that pushes a single child away from a container edge translates to that container's own `padding`, or `.align(...)` + `.offset(...)` for an isolated push -- see `awake-ui-authoring/SKILL.md` before reaching for `.offset()` on anything that isn't a one-off visual nudge. |

## Flexbox (`flex`, `justify-*`, `items-*`)

| CSS / Tailwind | Awake | Notes |
|---|---|---|
| `display: flex; flex-direction: row` | `row(id = "...") { }` | Container choice, not a modifier. |
| `display: flex; flex-direction: column` | `column(id = "...") { }` | |
| `flex: 1` (`flex-1`) | `.weight(1f)` | Only meaningful inside `row`/`column`; a no-op in `Box`/`Absolute` scopes (see `LayoutWeight`'s own doc comment). |
| `flex-grow: 0` (fixed-size sibling) | omit `.weight(...)` | |
| `justify-content: flex-start/center/flex-end/space-between/space-around` | `row(horizontalArrangement = Arrangement.Start / .Center / .End / .SpaceBetween / .SpaceAround)` | Passed to the container, not a per-child modifier -- matches CSS's own "justify-content lives on the flex container" semantics. |
| `justify-content: space-evenly` | **No equivalent.** `Arrangement` has `Start`/`Center`/`End`/`SpaceBetween`/`SpaceAround`/`spacedBy(dp)` -- no `SpaceEvenly` variant. Closest available is `SpaceAround`, not identical. |
| `gap: 8px` (flex `gap`) | `Arrangement.spacedBy(Tw.Spacing.s2)` on the container | Same primitive as `justify-content`'s row/column param -- gap and justify-content share one `Arrangement` value, they can't be set independently the way CSS allows both `gap` and `justify-content: space-between` simultaneously. Pick one. |
| `align-items: center` (cross-axis) | `row(verticalAlignment = ...)` / `column(horizontalAlignment = ...)`, or per-child `.align(UiAlignment...)` | Container-level default plus a per-child override, same split Compose uses. |

## Visual (border, radius, background, opacity)

These live on `Style`, not `UiModifier` -- `UiModifier` is layout-only, `Style` is paint/decoration.
Confirmed against `Style.kt`'s real builder surface:

| CSS | Awake (`Style { }` block) | Notes |
|---|---|---|
| `background-color: #fff` | `background(Color(0xFFFFFFFF))` | |
| `color: #000` (text color) | `foreground(Color(0xFF000000))` | |
| `border: 1px solid #ccc` | `borderWidth(1f.dp); borderColor(Color(0xFFCCCCCC))` | Two separate calls -- no single-line CSS-shorthand equivalent. |
| `border-radius: 8px` | `shape(8f.dp)` | Also `shape(UiShapeSpec.Pill)` etc. for named shapes, not just a radius `Dp`. |
| `padding` (as decoration box, not layout) | `contentPadding(all/horizontal-vertical/start-top-end-bottom)` | Distinct from `UiModifier.padding` above -- `Style.contentPadding` is what a *component's own* default inset resolves to (e.g. a card's internal padding), `UiModifier.padding` is what a *caller* adds around a widget instance. Don't conflate them; see `awake-shadcn-styling/SKILL.md`'s note on `shadcnCard`'s default `contentPadding`. |
| `font-size: 14px` | `textSize(14f.sp)` | |
| `opacity: 0.5` | `UiScope.withGraphicsLayerAlpha(0.5f) { ... }` | Not a `Style` field -- a scope wrapper around the draw calls it applies to (group alpha over an entire painted region), see `ShadcnButtons.kt`'s `buttonSlotInternal` doc for why it's structured this way instead of a per-color alpha multiply. |
| `display: none` (hide from layout) | **No equivalent — check before assuming one exists.** Nothing in this pass found a documented conditional-render primitive; the practical answer today is not calling the widget at all inside the `UiScope` block (immediate-mode has no persistent node tree to hide/show). If a future caller needs a fade-out-then-remove, `graphicsLayer`'s alpha compositing (`awake-shadcn-styling/SKILL.md`'s `AnimatedVisibility` note) is the closest primitive, not `display: none`. |

## Checklist before porting a CSS/Tailwind reference

- [ ] Reach for `Tw.Spacing.sN` for any numeric spacing/sizing value with a Tailwind-scale
      match, instead of hand-typing a `Dp` literal that can drift from the scale.
- [ ] `margin` → `Arrangement.spacedBy` on the parent, or the parent's own `padding`. Never
      invent a per-child margin modifier -- it doesn't exist and shouldn't be added ad hoc
      (see `awake-ui-authoring/SKILL.md`'s size-rule discipline before adding new modifier
      surface).
- [ ] `w-auto`/`w-min`/`w-max` have no faithful equivalent -- say so to whoever's reading the
      port, don't silently substitute `WrapContent` and call it equivalent.
- [ ] Layout (`UiModifier`) and paint (`Style`) are separate blocks passed to different
      parameters -- a CSS `padding` maps to one or the other depending on whether it's the
      *caller* spacing a widget out, or the widget's *own* internal inset.

## Related Skills

- `awake-ui-authoring` -- which layer (`ui-core`/`ui-headless`/`ui-designsystem`) owns a
  given size/spacing decision, and the size-derivation rule that governs adding any new
  modifier here.
- `awake-shadcn-styling` -- `Style.then`'s per-state-rule merge semantics, needed once a
  ported style has to compose with an existing `shadcn*` component's own variant style.
