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
component B," check whether A already exposes its own unstyled/primary form before adding B's
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

## Icon registry: `HeroIcons` (data) -> `UiIcons` (unstyled default) -> `ShadcnIcons` (overridable)

Three layers, each with one job:

- **`HeroIcons`** (`ui-unstyled`) -- the actual vector path data, hand-transcribed from
  Heroicons' published SVGs (no SVG-import pipeline exists in this engine). Nested one object per
  real Heroicons style+size tier (`Solid20Mini`, etc.) -- Heroicons ships genuinely different path
  data per size, not the same path rescaled, so there's no single correct glyph per name. No
  `Outline` tier exists yet: `UiImageVector`/`icon()` only fill paths, no stroke-width primitive.
- **`UiIcons`** (`ui-unstyled`) -- plain delegating pointers (`val chevronDown =
  HeroIcons.Solid20Mini.chevronDown`) for unstyled widgets (`dropdown`, etc.) that need a glyph
  without being shadcn-aware.
- **`ShadcnIcons`** (`ui-designsystem`) -- `var`, not `val`, fields defaulting to the same
  `HeroIcons` tier. Every `shadcn*` component should read from here, not inline a `UiImageVector`
  or reach past it into `UiIcons`/`HeroIcons` directly -- that's the one seam a consuming app
  reassigns to swap the whole icon set without touching component code.

Adding a new glyph: put the real vector data in `HeroIcons` (correct tier), add a delegating field
in `ShadcnIcons` if any `shadcn*` component needs it, and only add it to `UiIcons` too if an
unstyled widget also needs the same glyph. Don't build out `Solid24`/`Solid16Micro`/`Outline`
tiers ahead of an icon that actually needs them.

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
| 2026-08-04 | Initial release -- written alongside building `shadcnCollapsibleCard` (shadcn.io's "card-collapsible" reference example) and the `HeroIcons`/`ShadcnIcons` icon-registry split. |
