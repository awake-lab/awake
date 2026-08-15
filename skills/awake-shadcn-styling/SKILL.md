---
name: awake-shadcn-styling
description: >
  Maintainer-facing how-to for BUILDING/EXTENDING shadcn-flavored components inside Awake's
  ui-designsystem itself -- not for an app/sample that just calls an existing shadcn* component.
  Style.then's per-state-rule merge semantics (and why a variant's hover fill bleeds through a
  caller's own style), shadcnCard's default contentPadding coupling with composed triggers,
  shadcnCard's header-slot auto-divider, decoupling a behavior primitive (shadcnCollapsible) from
  a visual one (shadcnCard) instead of baking styling into behavior, animateFloat vs
  animateFloatTween for anything that must reach an exact terminal value, and the
  HeroIcons/UiIcons/ShadcnIcons icon-registry layering. Written from a real card-collapsible
  build (shadcn.io's own reference example), not theoretical.
license: Apache-2.0
metadata:
  author: awake
  last-updated: '2026-08-04'
  keywords:
    - Awake
    - shadcn
    - tailwind
    - Tw.Spacing
    - h-9
    - parity
    - ui-designsystem
    - Style
    - animatedHeight
    - animateFloatTween
    - shadcnCard
    - shadcnCollapsible
    - HeroIcons
    - ShadcnIcons
---

## When to Use This Skill

For whoever is working ON `ui-designsystem` itself -- adding a new `shadcn*` component, composing
one out of existing ones (a card that's also a collapsible, a button that's also a trigger),
adding a `hovered`/`active` style override, or reaching for `animateFloat`/`animatedHeight` for
something that needs to visibly finish (collapse, dismiss, fade out). NOT for a sample/app that
only *consumes* an already-built `shadcn*` component -- that caller needs the public API
(`shadcnCollapsibleCard(id, expanded, header) { content }`), not `Style.then`'s merge order or
`shadcnCard`'s private divider logic. If the task is "use `shadcnCard` in my screen," this skill
has nothing that call site needs; if it's "add a new `shadcn*` component" or "fix how an existing
one looks/behaves," load this first.

**Trigger keywords:** shadcnCard, shadcnCollapsible, shadcnCollapsibleCard, Style.then, hover
bleed, button hover looks wrong, animatedHeight, animateFloat, animateFloatTween, snap, jump,
panelPadding, contentPadding, HeroIcons, ShadcnIcons, icon registry, chevron, new shadcn
component, extend ui-designsystem.

---

## `Style.then` merges by concatenating rules, not replacing whole state blocks

```kotlin
infix fun then(other: Style): Style = when {
    this === Empty -> other
    other === Empty -> this
    else -> Style(rules + other.rules)   // <-- concatenation, not replacement
}
```

`resolve()` walks every rule in list order and applies it; a later rule's `hovered { }` block
runs its own property-setters on top of an earlier `hovered { }` block's -- only the specific
properties the later rule sets get overridden, not the whole state.

**The trap this caused:** `shadcnButton(variant = Ghost, style = Style { ... })` composes as
`ShadcnStyles.button(theme, Ghost) then style` -- Ghost's own `hovered { background(accent);
foreground(accentForeground) }` runs first, so wrapping a `shadcnButton` as a card's clickable
header trigger painted a full rounded accent-color fill on hover, reading as "a button", not "part
of the card" (real shadcn's card-collapsible header has no such fill). The fix is NOT avoiding
`shadcnButton` -- it's explicitly re-pinning both properties the base variant's hover rule set,
in the caller's own `style` block, which composes *after* the variant and therefore wins:

```kotlin
style = Style {
    foreground(shadcnTheme.colors.foreground)
    hovered { background(ShadcnTransparent); foreground(shadcnTheme.colors.foreground) }
    active { background(ShadcnTransparent); foreground(shadcnTheme.colors.foreground) }
}
```

Setting only `background` in the override and expecting `foreground` to fall back to the
non-hover value does NOT happen -- once Ghost's `hovered` rule sets `foreground`, it stays set for
that state unless a *later* `hovered` rule also sets it.

---

## `shadcnCard`'s default style bakes in `contentPadding` -- zero it when composing your own inset

`theme.components.surface` (what `shadcnCard`/`shadcnSurface` both resolve to by default) sets
`contentPadding(metrics.panelPadding)`. Fine for a plain card with a body slot. Wrong when you're
composing a full-bleed trigger row *inside* that body and adding your *own* horizontal inset to
the trigger and to the animated content below it -- the result is doubly inset (card's own
padding + component's own padding), and worse, the trigger's chevron sits nowhere near the card's
actual edge the way real shadcn's card-collapsible reference has it. Zero the card's own padding
and let the composing component own 100% of the spacing:

```kotlin
shadcnCard(id = "$id.card", modifier = modifier.fillMaxWidth(), style = Style { contentPadding(0f.dp) }) { _ ->
    // trigger row and content both apply their own shadcnTheme.spacing.sm inset here
}
```

---

## `shadcnCard`'s `header` slot always inserts a divider -- don't use it if you don't want one

```kotlin
private fun ColumnScope.shadcnCardContent(...) {
    if (header != null) {
        header()
        shadcnCardDivider(gap)   // <-- unconditional
    }
    body(slot)
    ...
}
```

A card-collapsible's header (the clickable trigger) has no divider line in real shadcn's own
reference -- just spacing, no rule. Passing your trigger as `shadcnCard`'s `header` parameter gets
you a divider you didn't ask for. Put the trigger row and the animated content both inside `body`
instead, and own the whole layout yourself -- see `ShadcnCollapsibleCard.kt`.

---

## Decouple behavior from styling -- compose, don't teach one primitive about the other

`shadcnCollapsible` is a trigger + `animatedHeight`, with a documented `bordered` convenience that
already wraps it in a `shadcnSurface`. Reaching for that same pattern to build a *card*-flavored
collapsible (adding more surface-styling logic directly inside `ShadcnCollapsible.kt`) recouples a
behavior primitive to a specific visual choice -- the next caller who wants a collapsible with
*any other* visual treatment is stuck either accepting `shadcnSurface`'s look or forking the whole
trigger-building logic.

The fix that actually stayed decoupled: leave `shadcnCollapsible` exactly as documented (trigger +
animated content, no opinion on background/border), and build the card-flavored version as its own
function (`shadcnCollapsibleCard`) that composes `shadcnCard` (visual) with `animatedHeight`
(behavior, the same low-level primitive `shadcnCollapsible` itself uses) directly -- neither
primitive ends up knowing the other exists. When a request sounds like "component A styled like
component B," check whether A already exposes its own headless/primary form before adding B's
styling logic into A's own file.

---

## `animateFloat` (spring/exponential decay) never truly reaches its target -- don't use it for anything that must visibly finish

```kotlin
val t = 1f - exp(-responsiveness * deltaSeconds)
val next = current + (target - current) * t
return if (abs(target - next) <= snapDistance) target else next
```

Convergence time scales with `ln(startValue / snapDistance)` -- decay is fast early and slows to
an imperceptible crawl near the target, so a real collapse (say 120px of content) looks fully
collapsed within ~10 frames while the container keeps an invisible sub-pixel sliver of leftover
height for dozens more frames, then hard-snaps to exactly 0 once it finally crosses the epsilon.
Live-reported as **"slowly hidden, then snap."** Worse: a wrap-sized parent (a card measuring a
collapsible child every frame) has no equivalent snap of its own, so its own shrink desyncs from
the child's right at that final epsilon-crossing frame -- reported separately as **"the parent
didn't have the same animation."**

`animateFloatTween` (fixed duration, `Easing`-shaped, ships in this same module) has neither
problem: it reaches the *exact* target in a known, bounded number of frames, so a wrap-sized
parent re-measuring every frame tracks the true value the whole way, with no separate snap step to
fall out of sync with:

```kotlin
val animatedHeight = animateFloatTween(id, targetHeight, durationMs = 250f, easing = EaseOut)
```

Reach for `animateFloat` only for continuous, never-terminating chase behavior (a cursor following
a pointer, a camera easing toward a live target that keeps moving) where there's no real "done"
state to hit exactly. Anything that collapses, dismisses, fades to zero, or otherwise needs to
visibly *finish* wants `animateFloatTween`.

---

## Icon registry: `HeroIcons` (data) -> `UiIcons` (headless default) -> `ShadcnIcons` (overridable)

Three layers, each with one job:

- **`HeroIcons`** (`ui-headless`) -- the actual vector path data, hand-transcribed from
  Heroicons' published SVGs (no SVG-import pipeline exists in this engine). Nested one object per
  real Heroicons style+size tier (`Solid20Mini`, etc.) -- Heroicons ships genuinely different path
  data per size, not the same path rescaled, so there's no single correct glyph per name. No
  `Outline` tier exists yet: `UiImageVector`/`icon()` only fill paths, no stroke-width primitive.
- **`UiIcons`** (`ui-headless`) -- plain delegating pointers (`val chevronDown =
  HeroIcons.Solid20Mini.chevronDown`) for headless widgets (`dropdown`, etc.) that need a glyph
  without being shadcn-aware.
- **`ShadcnIcons`** (`ui-designsystem`) -- `var`, not `val`, fields defaulting to the same
  `HeroIcons` tier. Every `shadcn*` component should read from here, not inline a `UiImageVector`
  or reach past it into `UiIcons`/`HeroIcons` directly -- that's the one seam a consuming app
  reassigns to swap the whole icon set without touching component code.

Adding a new glyph: put the real vector data in `HeroIcons` (correct tier), add a delegating field
in `ShadcnIcons` if any `shadcn*` component needs it, and only add it to `UiIcons` too if an
headless widget also needs the same glyph. Don't build out `Solid24`/`Solid16Micro`/`Outline`
tiers ahead of an icon that actually needs them.

---

## Reading Tailwind: shadcn's real source is a class string, and it is the spec

Every value in this module traces back to a Tailwind utility class in shadcn's own `.tsx`. You
cannot fix a parity bug without reading one, so read them correctly.

**The numbered scale is the whole game.** `h-9`, `w-9`, `p-9`, `gap-9`, `m-9`, `size-9` all resolve
to the *same* number -- Tailwind has ONE spacing scale, applied to different CSS properties. The
step number is not pixels: `step * 4 = px` (`1` = 4px, `2` = 8px, `9` = 36px, `64` = 256px). Two
steps break the pattern and are the ones people get wrong: `px` = exactly 1px (a hairline, not
step 0.25), and `0.5`/`1.5`/`2.5`/`3.5` are half-steps (2/6/10/14px). `Tw.Spacing` already encodes
all of this -- `Tw.Spacing.s9`, `Tw.Spacing.px`, `Tw.Spacing.s1_5` -- so never do the arithmetic by
hand, just read the step number off the class and use the matching constant.

| Tailwind class | Means | This codebase |
|---|---|---|
| `h-9` / `w-9` | height/width, scale step 9 | `.height(Tw.Spacing.s9)` / `.width(...)` |
| `size-9` | height AND width both | both modifiers, same constant |
| `p-4` / `px-3` / `py-2` | padding all / horizontal / vertical | `contentPadding(...)` or `.padding(...)` |
| `gap-2` | flex/grid gap between children | `Arrangement.spacedBy(Tw.Spacing.s2)` |
| `rounded-md` | border radius, *named* not numbered | `theme.radii.md` -- NOT `Tw`, radius is theme-relative here |
| `w-[100px]` | arbitrary value, escapes the scale | plain `100f.dp` literal, `Tw` has no step for it |
| `w-3/4` | fraction of parent | no direct equivalent -- see below |
| `sm:max-w-sm` | responsive breakpoint cap | this engine has no breakpoints; the cap value is what to use |

**Named scales are not the numbered scale.** `rounded-md`, `text-sm`, `shadow-lg` use word tiers,
not step numbers, and in this codebase they are theme-driven (`theme.radii.md`,
`theme.typography.label`) precisely so a preset swap changes them. Reaching for a `Tw` constant or a
literal for a radius/type value is the bug, not the fix -- `Tw` deliberately ships spacing only (see
`tailwindSpacingScalePx`'s own doc comment for why).

**Responsive and fractional classes need a judgment call, not a translation.** Real
`SheetContent` is `w-3/4 sm:max-w-sm`: three-quarters of the viewport, capped at 384px on
small-and-up. This engine has no breakpoints, so the honest port is the cap
(`Tw.Spacing.s96` = 384dp) with a comment saying the `w-3/4` floor isn't modelled -- which is
exactly what `shadcnSheet`'s `sizeDp` default does. Don't silently pick one and pretend it's a full
port.

**`max-w-*` is a CONSTRAINT, not a width -- use `widthIn`, never `Dimension.Fixed`.** This is a
real trap: `Dimension` only offers `Fixed`/`FillMax`/`WrapContent`, so `max-w-lg` looks
unportable and the tempting move is `Dimension.Fixed(512f.dp)`. That is wrong in both
directions -- it forces 512dp in a viewport narrower than 512 (which overflowed a 320px test
frame outright, pushing a dialog's confirm button out of bounds), and it drops the `w-full`
floor. `UiModifier` already carries the right primitive:

```kotlin
// UiModifier.kt -- these exist, use them
val minWidth: Dp? = null
val maxWidth: Dp? = null
val minHeight: Dp? = null
val maxHeight: Dp? = null
```

```kotlin
// WRONG -- `w-full sm:max-w-lg` is not a fixed 512dp width
width: Dimension = Dimension.Fixed(512f.dp),

// RIGHT -- fill the parent (w-full), capped at max-w-lg
modifier = Modifier
    .width(Dimension.FillMax)
    .widthIn(max = 512f.dp)
```

Rule of thumb: `w-N`/`h-N` (a size) map to `Dimension.Fixed`; `max-w-*`/`min-w-*`/`max-h-*`
(a bound) map to `widthIn`/`heightIn`. Mapping a bound onto a size is how a component ends up
correct on one viewport and broken on another.

**Where to read the real classes:** `github.com/shadcn-ui/ui/blob/main/apps/v4/registry/new-york-v4/ui/<component>.tsx`
(the registry `ShadcnStylePreset.Vega` targets). Fetch the raw file, find the `className` on the
element whose role you're fixing -- `cva()` base string, a variant entry, or an inline JSX
`className` -- and read the step off it. Guessing from how the component looks in this engine's own
preview is how all six bugs in the next section got there.

---

## Every `shadcn*` component needs Variant + Size + Style -- a bare `.dp` literal is how spacing/size bugs slip through

```
button
 -> shadcnButton()              // public, the widget
 -> ShadcnButtonVariant         // enum (Primary/Ghost/Outline/...), shared file ShadcnVariants.kt
 -> ShadcnButtonSize            // enum (Sm/Md/Lg), same file -- heightDp lives ON the enum
 -> shadcnButtonStyle(...)      // private function, resolves Style from variant+size+theme
```

`Style` (the resolver function) and `Variant` (the enum) are consistently modeled across the module.
`Size` is not -- some components have a real enum (`ShadcnButtonSize`, `ShadcnCardSize`), most just
have loose file-scope constants (`MENU_MIN_WIDTH_DP`, `COMBOBOX_OPTION_HEIGHT`) or a bare inline
literal with no name at all. That inconsistency is a real, live bug source, not a style nit: a 2026
parity audit against real shadcn/ui found `ShadcnFieldTextField`/`ShadcnFieldDropdown` hand-typing
`40f.dp` for a control height that `ShadcnButtonSize.Md` (36dp, `h-9`) already defines correctly one
file over -- the wrapper never referenced the token, so it silently drifted. Same root cause hit
`ShadcnInputOTP` (non-square 36×40 slots, should be 36×36 = `h-9 w-9`), `ShadcnSidebar` (240dp
instead of `16rem`=256dp), `ShadcnTabs` (32dp instead of `h-9`=36dp), and `ShadcnDrawer`'s drag
handle (48×6dp instead of the real `h-2 w-[100px]`=8×100dp) -- six separate bugs, same shape: a
number typed by hand instead of a name pointed at.

**The rule:** any height/width/padding/radius that appears in more than one component -- or even
one component with more than one call site -- gets a named constant (an enum like `ShadcnButtonSize`
when there are real size variants, a `private val`/`const` when there's just one size). A bare `.dp`
literal on a `height()`/`width()`/`padding()` call is the thing to grep for when auditing this
module; genuinely one-off values (this repo's Sheet's `384f.dp` width cap, the drawer handle's
`100f.dp`) can stay literal the same way real shadcn's own Tailwind source has arbitrary values
(`w-[100px]`, `sm:max-w-sm`) that aren't part of its spacing scale either -- the rule is "reused
values get a name," not "no literals ever."

**Every size/spacing value's source of truth is real shadcn/ui, not a guess or "what looked
right."** Every one of the six bugs above was a number someone typed by hand instead of checking
the actual spec -- confirm against the real component source
(`github.com/shadcn-ui/ui/blob/main/apps/v4/registry/new-york-v4/ui/<component>.tsx`, the
new-york-v4 registry this module's `ShadcnStylePreset.Vega` preset targets) before adding or
changing a size constant, not against how it looks in this engine's own preview. A value that
"looks about right" and one that's actually `h-9` both compile and both render -- only one of them
stays correct when a sibling component is built next to it using the real number.

**`Tw.Spacing` (`ui-core`, generated by `:awake:ui:tailwind-generator`) has the numbers --
mapping which shadcn class applies to which component role is still a manual step, not a tool.**
`Tw.Spacing.s9` is Tailwind's `9` step (36px/dp) -- correct by construction, no arithmetic to get
wrong. What `Tw` does NOT do is read shadcn's real source for you: no scraper exists that reads
`sheet.tsx`'s `className` and tells you `sizeDp`'s default should be `Tw.Spacing.s96`. That
requires the same manual/researched step every fix in this section used -- fetch the real
component source, read which class applies to which role, then write `Tw.Spacing.sN` (not a
literal) at that call site:

```kotlin
// 1. Fetch the real component source (WebFetch or equivalent), e.g.:
//    https://raw.githubusercontent.com/shadcn-ui/ui/main/apps/v4/registry/new-york-v4/ui/sheet.tsx
// 2. Read the class that governs the role you're fixing:
//    right/left SheetContent: "... w-3/4 ... sm:max-w-sm" -- the fixed cap is sm:max-w-sm = 384px
// 3. Match 384px to its Tailwind scale step (96 -> 2.25rem*... = 384px) and write the constant,
//    not the literal:
sizeDp: Dp = Tw.Spacing.s96,  // NOT 384f.dp -- same value, but traceable back to the real class
```

A value with no matching `Tw.Spacing` step (the drawer handle's `w-[100px]`, an arbitrary value
in shadcn's own source too) stays a plain literal -- `Tw` only covers the standard scale, per its
own doc comment.

---

## There is already parity tooling -- use it before eyeballing a diff

As of 2026-08-15, "does this look right vs shadcn" splits into two oracles that answer different
parts of it. Read `docs/reference/ui-validation.md`'s component coverage matrix before trusting
either in isolation -- most components (16 of 23) have neither.

- **Layout (size, position, spacing) -- `ShadcnGeometryParityTest`.** Compares Awake's semantic
  bounds against the reference app's own `getBoundingClientRect`, both sides exact numbers, no
  rasterizer or font dependency. This is the first stop for a padding/width/spacing question, not
  the pixel diff below -- three real bugs (a mis-framed reference, an unset reference font, a
  reference rendering every weight as 400) were found *in* the pixel instrument before this
  existed, against one real component bug found *by* it. `tools/shadcn-reference-app/src/cases.tsx`
  tags each reference element with `data-parity-id`; `tools/capture_shadcn_local.py` exports the
  rects alongside the PNG.
- **Colour, border, shadow -- `ShadcnReferenceComparisonTest` (demoted, not removed).** Still the
  only oracle for this dimension. `compare_parity.py` / `shadcn_parity_baseline.json` /
  `shadcn_parity_thresholds.json` are its supporting tooling, same as before -- what changed is
  that its mismatch% is no longer where a layout question gets decided, and it will never reach 0%
  even for a pixel-perfect layout (different rasterizer, different font hinting).
- **Behavior, motion -- no oracle.** Not built yet.
- `ShadcnParityScreenshotTest` -- the separate golden-image regression lock (Awake vs Awake's own
  prior render), re-recorded with `-DAWAKE_RECORD_SNAPSHOTS=true`. Unrelated to either oracle
  above; it proves nothing changed by accident, not that anything is correct.

For "did my change move something," the layout-signature and golden-image tests answer it. Reach
for a manual source-reading audit (the previous two sections) for *numeric* spec questions --
`h-9` vs `h-10` -- which neither oracle names on its own; geometry will tell you the number is
wrong, not which Tailwind class it should have matched.

---

## Fidelity tests run a different font than the real app -- vertical-centering bugs can survive them

`UiFonts.default()` resolves to `PackedUiFont(RobotoRegularUiFontData)` -- a **generated** atlas
(`:awake:ui:font-atlas-generator`'s `generateFontAtlas` task, ~8.8k lines of packed
per-glyph quad metrics, instantiated from `tools/fonts/Roboto[wdth,wght].ttf` -- Roboto 3.015,
not the 2011 static this atlas shipped with before 2026-08-15) that overrides
`visibleTopEm`/`visibleBottomEm` with values computed from real glyph ink, and reports
`lineHeightEm = 1.1875`.

Every fidelity/screenshot test in the repo constructs `BitmapFont()` instead, which overrides
neither -- it inherits `UiFont`'s defaults of `visibleTopEm = 0f` / `visibleBottomEm = 1f` and
`lineHeightEm = 1f`.

Vertical centering is computed from exactly those values (`BasicText.kt`'s
`penY = slot.y + (slot.height - blockHeight) / 2f - blockTopPx`, fed by `measureTextBlock` ->
`measureVisibleLineBandEm` -> `font.visibleTopEm/visibleBottomEm`). A clean 0..1 band and Roboto's
real asymmetric band do not center identically, so **a test asserting text is centered proves it
for `BitmapFont`, not for what ships**. When a text-alignment bug is reported against the running
app but every test is green, this divergence is the first thing to check -- and when the metrics
themselves look wrong, remember they are generated: fix
`:awake:ui:font-atlas-generator`'s `Main.kt` and re-run its `generateFontAtlas` task,
never hand-edit `RobotoRegularUiFontData.kt`.

---

## Related Skills

- `awake-design-system-engineer` (agent persona) -- the broader role guidance for working on
  `ui-designsystem` (visual language, theme tokens, showcase polish); this skill is the narrower,
  trap-focused companion for the specific mechanics that persona will hit while composing or
  extending `shadcn*` components. Load both when the task is "add/fix a shadcn component."
- `awake-ecs-scene-runtime` -- unrelated layer (ECS/scene runtime consumption), same
  consumer-vs-maintainer split (that skill is explicitly the *consuming* how-to, with its own
  `awake-scene-runtime-engineer` persona covering the maintainer side) and the same "how-to
  reference written from real traps" shape this skill follows.
- `kmp-compose-design-system` -- generic Compose Multiplatform design-system scaffolding; this
  skill is Awake's own hand-rolled `ui-core`/`ui-designsystem` stack, not Compose, so patterns
  don't transfer directly (no `@Composable`, no recomposition, immediate-mode instead).

---

## Changelog

| Date | Change |
|---|---|
| 2026-08-10 | Added the Tailwind-reading primer (numbered vs named scales, arbitrary/responsive/fractional classes, the class-to-`Tw.Spacing` table), the Variant+Size+Style component convention, and the shadcn-source-of-truth rule -- written after a parity audit found six components (Field wrappers, InputOTP, Sidebar, Tabs, Sheet, Drawer) with hand-typed dp values that drifted from real shadcn/ui. |
| 2026-08-04 | Initial release -- written alongside building `shadcnCollapsibleCard` (shadcn.io's "card-collapsible" reference example) and the `HeroIcons`/`ShadcnIcons` icon-registry split. |
