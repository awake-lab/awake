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

Not decided. Recommend **Option B** — it's what real `DrawScope` does, and it's
the one that actually removes the `context` leak (`CanvasScope.context` currently
exists ONLY because `ShapePainter`'s helpers need theme access; remove that need
and `context` has no reason to stay public).

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
6. **Layout vs interaction split** (**investigation in progress, 2026-08-18** —
   promoted from "optional" after re-checking real Compose source: Compose
   genuinely separates `MeasureScope` (layout, handed to `Layout{}`'s measure
   lambda) from `PointerInputScope` (input, handed inside
   `Modifier.pointerInput{}`) — two unrelated types, not one bundled interface.
   Compose does NOT scope state behind any capability type at all
   (`remember{}`/`mutableStateOf` are callable from anywhere), so `widgetState`
   has no direct Compose analog to split against and may not belong in either
   new scope). Separate `UiPrimitiveScope`'s remaining layout (`claimSlot`) from
   input (`hitTest`/`isActive`/`tryClaimActive`/`releaseActiveIfMatches`) into
   two interfaces; decide `widgetState`'s home once the real usage picture is
   in. An agent is investigating the real call-site picture now (interleaved
   vs cleanly separated usage) before any interface is designed or code moves —
   this section will be updated with the real finding once that lands.

Each numbered step is its own scoped, verified pass — land it, run
`verifyUiOwnership` + the three UI `desktopTest` suites, then decide whether the
next step is still worth it. No step here is "final" until it's actually landed
and re-measured, matching how B9a/B9b/B9c and this doc's own P1 re-measurement
went in practice today.
