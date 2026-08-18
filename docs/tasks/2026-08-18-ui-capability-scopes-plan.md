# UI capability-scoped receivers — simplified plan

Companion to the "Capability-scoped receivers" (P1) row in
`docs/audits/2026-08-17-ui-refactor-vs-recreate-audit.md`. That row is the full
evidence trail; this doc is the shape of the end state and the concrete steps to
get there, re-verified against real source on 2026-08-18.

## Why

`UiPrimitiveScope` mixes draw, layout, and input/state on one interface, plus a
public `context: UiContext` field its own doc comment says to never expose:

```kotlin
// awake/ui/ui-core/.../UiPrimitiveScope.kt — today
interface UiPrimitiveScope {
    val context: UiContext              // "Never expose this" — but it's public
    fun claimSlot(width, height, weight): UiBounds   // layout
    fun hitTest(slot): Boolean                        // input
    fun isActive(id): Boolean                         // input/state
    fun tryClaimActive(id, hovered)                   // input/state
    fun releaseActiveIfMatches(id)                    // input/state
    fun emit(primitive: UiDrawPrimitive)              // draw
    fun emitOverlay(primitive: UiDrawPrimitive)         // draw
    fun widgetState(id): WidgetState                   // state
}
```

Real Compose precedent (verified against Compose's own source, not assumed):
`DrawScope`, `MeasureScope`, and `@Composable` scope are each a narrow interface
carrying only their own phase's capability — nothing has a field that exposes the
layer below.

## What already exists (found 2026-08-18, corrects the audit's stale assumption)

A narrow draw scope already exists — `CanvasScope` (`ui-core/Canvas.kt`), handed
out only inside `canvas {}`:

```kotlin
class CanvasScope internal constructor(
    private val scope: UiPrimitiveScope,
    val bounds: UiBounds,
) {
    val context get() = scope.context   // ← same leak, one level down
    fun drawRect(x, y, width, height, color, overlay = false)
    fun drawRoundRect(...)
    fun drawShape(...)
    fun fillPath(...)
    fun strokePath(...)
    fun drawText(...)
    fun drawImage(...)
    fun drawGradientRect(...)
    fun clipRect(...)
    // …
}
```

Building a second, differently-named `UiDrawScope` would duplicate this — the
"twin nouns" pattern the audit itself bans elsewhere. **`CanvasScope` is the draw
scope.** No new type needed for that slice.

The real gap: `ShapePainter.kt`'s shared paint helpers (used by `scrollPanel`,
`Checkbox`, and everything routed through `paintSurface`) are extension functions
on `UiPrimitiveScope` directly, not on `CanvasScope`:

```kotlin
// awake/ui/ui-core/.../graphics/ShapePainter.kt — today
fun UiPrimitiveScope.emitFillAndBorder(...)   // bypasses CanvasScope entirely
fun UiPrimitiveScope.emitCheckmark(slot: UiBounds)
fun UiPrimitiveScope.emitRadioDot(slot: UiBounds, color: Color)
```

So `paintSurface` (the intended single widget-chrome path) still reaches
`UiPrimitiveScope`'s raw draw primitives directly, not through `CanvasScope`.

`emit*` is also the wrong verb — the audit's naming-lexicon decision (P2,
`docs/audits/2026-08-17-ui-refactor-vs-recreate-audit.md`) already settled
`draw*` as the reserved verb for painting members on a draw-capable scope, with
`emit*` banned outright. That decision was explicitly deferred with "decide
with P1" — this migration is that moment, since these functions are moving onto
`CanvasScope` (the draw scope) anyway.

## Final shape — before and after, together

Everything above lands as one coherent change, not three separate half-steps:

```kotlin
// BEFORE — awake/ui/ui-core/.../graphics/ShapePainter.kt, today
fun UiPrimitiveScope.emitFillAndBorder(fill: Color, border: Color, ...) {
    // draws directly against the raw scope; caller had no choice but
    // to already be holding a UiPrimitiveScope, not a CanvasScope
}
fun UiPrimitiveScope.emitCheckmark(slot: UiBounds) { ... }
fun UiPrimitiveScope.emitRadioDot(slot: UiBounds, color: Color) { ... }

// awake/ui/headless/.../internal/controls/Surface.kt, today
fun paintSurface(scope: UiPrimitiveScope, style: Style, slot: UiBounds) {
    val fill = style.foreground ?: scope.context.current(LocalTheme).colors.foreground
    scope.emitFillAndBorder(fill, border, ...)   // raw scope, emit* verb
}
```

```kotlin
// AFTER — awake/ui/ui-core/.../graphics/ShapePainter.kt
fun CanvasScope.drawFillAndBorder(fill: Color, border: Color, ...) {
    // draws through CanvasScope's own public API (drawRect/fillPath/etc);
    // fill/border are always caller-resolved, this function never touches theme
}
fun CanvasScope.drawCheckmark(slot: UiBounds) { ... }
fun CanvasScope.drawRadioDot(slot: UiBounds, color: Color) { ... }

// awake/ui/headless/.../internal/controls/Surface.kt
fun UiScope.paintSurface(style: Style, slot: UiBounds) {
    val fill = style.foreground ?: LocalTheme.current.colors.foreground   // resolved here, once
    canvas {                          // hands out CanvasScope
        drawFillAndBorder(fill, border, ...)   // draw* verb, no theme access needed
    }
}
```

Net result: `CanvasScope.context` has no remaining caller once this lands, so it
gets deleted — the leak this whole slice exists to close is actually closed, not
just relocated. `UiPrimitiveScope.emit`/`emitOverlay` (the lower-level interface
members `CanvasScope`'s own `drawRect`/etc. call internally) are a separate,
legitimate case and are NOT renamed — they're the raw primitive-emission path
underneath the draw scope, not a widget-facing verb the lexicon rule targets.

## The blocking decision

`ShapePainter`'s helpers read default colors off `UiPrimitiveScope.context` (theme
lookup). Rewiring them onto `CanvasScope` means picking one:

**Option A — `CanvasScope` grows theme-default params.**
```kotlin
fun CanvasScope.emitFillAndBorder(fill: Color? = null, border: Color? = null) {
    val resolvedFill = fill ?: context.current(LocalTheme).colors.foreground
    // ...
}
```
Keeps call sites simple; `CanvasScope` now needs to know about theme resolution,
which it currently doesn't.

**Option B — callers pre-resolve colors before calling `CanvasScope`.**
```kotlin
fun UiScope.checkbox(...) {
    val fill = style.foreground ?: LocalTheme.current.colors.foreground  // resolved here
    canvas { emitFillAndBorder(fill, border) }   // CanvasScope never touches theme
}
```
Keeps `CanvasScope` a pure draw surface (closer to real `DrawScope`, which also
never resolves theme values itself — the caller passes a `Color`). More call
sites touched, but each change is mechanical.

**Decided 2026-08-18: Option B.** Callers pre-resolve colors; `CanvasScope`
stays a pure draw surface, matching real `DrawScope`, and removes the reason
`CanvasScope.context` exists at all.

## Sequence

1. **Decide A vs B** (above) — blocks everything else in this doc.
2. **Migrate `ShapePainter`'s helpers onto `CanvasScope`**, `paintSurface` hands
   out `CanvasScope` instead of calling `UiPrimitiveScope` extensions. Small,
   contained: 9 files use raw `emit`/`emitOverlay` outside the shared helpers
   today (`TextureQuad.kt`, `layouts/Surface.kt`, `graphics/ClipScopes.kt`,
   `ResizablePanelGroup.kt`, `Overlay.kt`, `Dropdown.kt`, `Icon.kt`,
   `BasicText.kt`) — each already its own canonical function, not scattered
   duplication.
3. **Drop `CanvasScope.context`** once nothing inside `ui-core` needs it anymore
   (only possible after step 2, if Option B was chosen).
4. **Shrink `UiPrimitiveScope`** — once draw is fully behind `CanvasScope`, drop
   `emit`/`emitOverlay`/`context` from the interface. What remains:
   ```kotlin
   interface UiPrimitiveScope {
       fun claimSlot(width, height, weight): UiBounds
       fun hitTest(slot): Boolean
       fun isActive(id): Boolean
       fun tryClaimActive(id, hovered)
       fun releaseActiveIfMatches(id)
       fun widgetState(id): WidgetState
   }
   ```
5. **`UiScope`'s `.primitive` escape hatch** — once `UiPrimitiveScope` no longer
   exposes anything dangerous, decide whether `UiScope` still needs a wrapper
   field at all, or can just extend the (now-safe) `UiPrimitiveScope` directly:
   ```kotlin
   interface UiScope {
       val primitive: UiPrimitiveScope   // today: a real escape hatch
   }
   // →
   interface UiScope : UiPrimitiveScope   // tomorrow: nothing left to escape into
   ```
6. **Layout vs interaction split — investigated 2026-08-18, REJECTED. Not
   scoped for execution; nothing implemented.**

   Compose's `MeasureScope`/`PointerInputScope` split works because those really
   are separate phases in time: measure runs once per recomposition, pointer
   input reacts later, in its own coroutine, inside `Modifier.pointerInput{}`.
   Awake's immediate-mode model has no such separation — every widget claims its
   slot and evaluates `hitTest`/`isActive` against that exact slot in the same
   synchronous call, because there's no later phase to hand off to.

   Grepped every real call site of `claimSlot` vs `hitTest`/`isActive`/
   `tryClaimActive`/`releaseActiveIfMatches` across `ui-core`/`headless`. The
   claim-then-hit-test pattern is the norm at every real widget-authoring
   chokepoint, not an edge case:
   - `ui-core/layouts/Box.kt` (`box()`): `claimModifiedSlot` then `hitTest(slot)`
     the very next line.
   - `ui-core/layouts/Surface.kt` (`surfaceCore`, the shared chrome path every
     `shadcn*` surface widget funnels through): claims a slot, hit-tests it,
     resolves `isActive`, then *re-claims and re-hit-tests* once real bounds are
     known for wrap-content sizing — the file's own comment names this
     "claim-slot-then-hit-test order" and says it deliberately mirrors
     `interact()` in ui-headless.
   - `ui-core/layouts/Row.kt` / `Column.kt`: `hovered = modifier.forceHover ?:
     hitTest(slot)` / `active = ... isActive(id)` immediately after each
     `claimSlot`.
   - `ui-core/layouts/LazyList.kt`: `claimModifiedSlot` then `if (hitTest(slot))`
     for scroll-wheel consumption.
   - `ui-core/modifier/ClickableModifiers.kt` (`resolveClickable`): `hitTest` →
     `tryClaimActive` → `isActive` → `releaseActiveIfMatches` → `isActive` again,
     all against a slot the caller just claimed.
   - `headless/internal/layout/Interaction.kt` (`interact()` — the one function
     nearly every interactive headless widget calls): `claimModifiedSlot` then
     `hitTest`/`tryClaimActive`/`isActive`/`releaseActiveIfMatches`/`isActive`
     again, five calls in one function body:

     ```kotlin
     // headless/internal/layout/Interaction.kt — real code, the shared chokepoint
     internal fun UiPrimitiveScope.interact(
         id: String,
         modifier: UiModifier = Modifier,
         enabled: Boolean = true,
     ): UiInteraction {
         val slot = claimModifiedSlot(modifier)        // layout
         val hovered = hitTest(slot)                     // interaction — needs `slot` from above
         tryClaimActive(id, hovered && enabled)           // interaction
         val wasActiveBeforeRelease = isActive(id)        // interaction
         releaseActiveIfMatches(id)                        // interaction
         val active = isActive(id)                          // interaction
         // …
         return UiInteraction(slot, hovered, active, clicked = ...)
     }
     ```

     Splitting `claimSlot` onto a `UiLayoutScope` and the rest onto a
     `UiInteractionScope` means this one function needs both receivers held at
     once, and every interaction call directly depends on `slot`, the layout
     call's own return value — not a coincidental adjacency, a real data
     dependency.

   That's every layout composite (`box`/`row`/`column`/`surface`/
   `scrollPanel`-family) plus both shared interaction chokepoints
   (`resolveClickable`, `interact()`) needing both capabilities in the same
   function. Splitting `UiPrimitiveScope` here doesn't remove a leak — it forces
   every one of those ~9 files to juggle two receivers simultaneously (or reach
   for a combined wrapper type, which just rebuilds the god receiver one level
   up).

   `widgetState` reads (text cursor, skeleton/spinner animation, collapsible
   height, dropdown expanded flag) are NOT tangled with `claimSlot` in the same
   expression the way `hitTest`/`isActive` are — but that argues for leaving it
   ambient (no scope gate at all, matching Compose's own choice not to gate
   `remember{}`/`mutableStateOf`), not for giving it a third interface just for
   symmetry.

   **Verdict: do not split layout from input/state.** If this gets
   re-litigated later, re-run the grep above first — the interleaving is the
   load-bearing fact, not an impression.

Each numbered step is its own scoped, verified pass — land it, run
`verifyUiOwnership` + the three UI `desktopTest` suites, then decide whether the
next step is still worth it. No step here is "final" until it's actually landed
and re-measured, matching how B9a/B9b/B9c and this doc's own P1 re-measurement
went in practice today.
