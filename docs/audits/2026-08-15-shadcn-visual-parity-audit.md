# Shadcn visual parity audit — 2026-08-15

Live audit of `samples:ui-showcase` (wasmJs, WebGPU, dpr=2) against official shadcn/ui
(new-york, light). Method: drove the running app in a browser, exercised hover/press/drag/
focus per component, confirmed root causes in source. Severity: **P0** breaks usage,
**P1** visibly wrong, **P2** parity drift.

## Buttons — interaction-state colors (P0) — FIXED 2026-08-15

Fixed same day (see the fix-direction paragraph below, applied verbatim): `neutralStyle()`
is now state-neutral, variants own hover styles (`primary/90`, `secondary/80`,
`destructive/90`, outline/ghost → accent), `withDisabledDim` deleted, `shadcnToggle` owns its
muted hover. Locked by `ShadcnButtonStateColorTest` + inverted `UiThemeTest` invariants +
re-recorded toggle/switch state goldens. Verified live on wasm. Link-underline and
focus-ring gaps remain open.

Original finding — rest state was correct for all six variants; every non-rest state on
filled variants was wrong:

| Variant | State | Observed | shadcn expected |
|---|---|---|---|
| Primary | hover | muted-gray fill, white label → **label invisible** | `primary/90` (still dark) |
| Primary | pressed | accent-gray fill, label invisible | same as hover |
| Danger | hover/pressed | red fill replaced by muted gray | `destructive/90` (still red) |
| Secondary | hover | muted fill (token drift, readable) | `secondary/80` |
| Link | hover | **gray fill appears**; no underline | no bg ever, underline on hover |
| any | disabled | fg swapped to mutedForeground *and* 50% alpha (double-dim, gray mush) | variant colors at 50% opacity only |

Root cause chain:
- `UiColorTokens.neutralStyle()` (`ui-core/theme/UiColorTokens.kt:13`) bakes
  `hovered{background(muted)}, active{background(accent)}` into `theme.components.button`
  defaults for **every** button.
- Designsystem variants (`ShadcnButtonStyles.kt`) set only an *unconditional* background;
  `SurfaceVisuals.hovered/pressed = null` for Primary/Secondary/Danger/Link.
- Since 537d13c5 ("resolve state-conditional style rules after unconditional overrides"),
  the defaults' state rules outrank the variant's unconditional fill → default gray wins on
  hover/press. Foreground rule untouched → white-on-light-gray.
- `withDisabledDim` (`ShadcnDecorators.kt:12`) sets mutedForeground on top of the
  primitive's whole-surface 0.5 alpha → double treatment.

Fix direction: strip state rules from `neutralStyle()` (defaults should be state-neutral),
make each `ShadcnButtonVariant` own explicit hover/pressed styles (`primary/90` etc.), and
delete the foreground override in `withDisabledDim`. Sibling risk: `toggle`, `checkbox`,
`dropdown` defaults share `neutralStyle()` — same bleed applies to any recipe passing
rest-only styles over them.

Also: no focus-visible ring anywhere (shadcn: 3px `ring/50`). The only focused rule in the
stack is textField `borderColor(primary)`.

## Button Group (P1 — coverage)

`blocks/ButtonGroup` page is an explicit NOT IMPLEMENTED placeholder. No horizontal, no
vertical variant, no component. Named blocker in the placeholder: shadcnButton owns its own
corners; a grouped-border recipe (first/last radii, collapsed inner borders) doesn't exist.
Reference: `registry/new-york-v4/examples/button-group-demo.tsx`.

## Resizable (P0) — core drag bug FIXED 2026-08-15

Fixed (commit 189bd0f6): the "2x drag" and "group grows" symptoms were one bug — the
group's interior claims (panel/handle at `fraction x resolved bound`) leaked into an
enclosing WrapContent surface's measure trial as phantom intrinsic width, re-sizing the
card and re-basing the panel budget mid-gesture. Not dpr-related. `resizablePanelGroup`
now wraps its walk in `boundDerivedContent` (public wrapper over
`withMeasuredSubtreeIsolated`). Exact-value tests: delta-exact redistribution,
conservation, 1:1 tracking, density 1 and 2, full showcase composition. Still open below:
cursor plumbing on wasm, divider line, centered labels, nested demo.

Original findings:

- **Drag delta ≈ 2× pointer movement.** Measured: 100 CSS px drag → ~182 CSS px handle
  travel. wasm host publishes pointer in physical px (`offsetX * density`,
  `WebGpuCanvasHost.kt:106-112`); fraction math in `ResizablePanelGroup.handle()` divides by
  a logical-px axis size. Dp-vs-pixels violation; same 2× reproduces on Slider drags.
- **Group total width not conserved.** Dragging grows panel One and the whole preview card
  (measured 477→692 units); panel Two never shrinks. The WrapContent surface re-measures to
  children's sum each frame, so the "redistribute" contract is broken end to end.
- **Press-jump.** Pressing the handle without moving displaces it when `lastPointerMain`
  survives from a prior drag (cleanup lives in the not-dragging else-branch; a gesture that
  starts before an idle frame reuses the stale value). Reset it on press transition instead.
- **Cursor never changes.** `requestCursor(ResizeHorizontal)` has no wasm plumbing; canvas
  CSS cursor stays `auto` even mid-drag (shadcn: `col-resize`).
- Visuals: no divider line between panels (shadcn draws a full-height border line; Awake
  draws only the grip), panel labels top-left instead of centered, nested-group demo the
  Notes text advertises isn't in the page.

## Input Group (P1)

`shadcnInputGroup` (`ShadcnInputRecipes.kt:203`): row = [prefix surface | ghost input |
suffix surface] with the input greedy-filling the row.

- `prefixText = "https://"` renders clipped to ~"http".
- Second demo: prefix `$` and suffix `USD` don't render at all — input consumes the row,
  affixes starve to zero width.
- Divider line renders between affix and input; shadcn inline addons have none.
- Focus draws a border around the inner ghost input (double-box); shadcn puts the focus
  ring on the *group* container and keeps the inner input borderless.

## Select (P1)

- Option rows carry their own popover-colored fills but there is **no container panel**:
  no popover background surface, no border, no shadow, no 4px padding — page content shows
  through the gaps between rows and row widths are ragged (`selectOptionVisuals` styles rows;
  nothing styles the menu).
- No checkmark on the selected item (options are plain `String`s; no selected-item slot).
- Trigger is content-width (~105px); shadcn demo trigger is w-[180px] class of width.
- Selection behavior itself works (click item → trigger label updates, closes).

## Tabs (P1 — API gap)

`shadcnTabs(tabs = listOf(...)): Int` models only the trigger list. There is no
TabsContent equivalent, so the page shows a track with no panel — official tabs-demo is
track + content card. Track/active-trigger styling itself is close.

## Tooltip (P1)

- With `visible = true` hardcoded on both page tooltips, rendered output is unstable
  frame-to-frame: both bubbles visible in some frames (stacked overlapping near the *first*
  anchor, second one clipping through the card edge), gone in others while an unrelated
  pointer moves — overlay anchor/persistence bug, not just page authoring.
- No arrow (new-york tooltip has one).
- Page authoring: triggers are raw unstyled `buttonSlot`s (render as bare text), and
  always-on tooltips can't demo hover behavior at all.

## Dialog (P2)

- No scrim — shadcn overlays `bg-black/50` behind the panel; Awake dims nothing.
- No drop shadow on the panel; no top-right X close button.
- Centering and outside-press dismiss are correct.

## Slider (P1)

- Track collapses to ~35 logical px (thumb-width); the whole preview card wraps down with
  it. shadcn slider fills its container. Default/fill sizing for the headless slider is
  broken in WrapContent parents.
- Drag: same 2× physical-px delta as Resizable (thumb slammed to max from a 25px drag).
- "Low" state thumb overhangs the track start; label rows sit tight enough to collide.

## Showcase shell (P1)

- **Sidebar doesn't scroll.** At 720px viewport height only Getting Started + Inputs are
  reachable; Layout/Overlays/Status/Typography/Blocks pages are unreachable by mouse.
- Footer user card overlaps the last sidebar item (Button Group) — no bottom inset.
- Checkbox page Notes promise indeterminate + disabled states; States row shows neither.

## Input pipeline (P2, robustness)

`bindWindowPointerInput` stores latest pointer state; a mousedown+mouseup pair arriving
within one frame interval nets to "nothing happened" — sub-frame taps (fast trackpad taps,
any synthetic/automation click) are physically droppable. Latch per-frame press/release
events instead of overwriting one state.

## Why the tooling didn't catch this

- `ShadcnStyleParityTest` (23 cases, real getComputedStyle oracle) compares **rest state
  only**. `tools/capture_shadcn_local.py` never captures hover/pressed/focus, so the entire
  bug class the user reported was structurally invisible. Extend capture to force
  `:hover`/`:active`/`:focus-visible` (CDP `Input.dispatchMouseEvent` or forced pseudo-class
  states) and add state rows to the oracle.
- `ShadcnGeometryParityTest` covers 7/23 components, layout only. Resizable/Select/
  InputGroup aren't among them.
- Behavior (drag, open/close, dismiss) has no oracle at all (known: ui-validation.md says
  behavior coverage is 0/23). The Resizable dpr bug needs one drag-delta assertion against a
  real dpr=2 frame to lock.
- Snapshot goldens are Awake-vs-Awake and were recorded from the same broken code — green
  through every bug above.

## Architecture note: style-channel redundancy (relates to all of the above)

Same question the state-color bug answers: one visual property (button hover fill) can
currently be set in **five** competing places:

1. `Style` + state blocks (`ui-core/style/Style.kt`) — the real system, mirrors
   shadcn-compose's single-Style organization.
2. `UiComponentVisuals`/`UiThemeComponents` (`api/theme/UiThemeValues.kt:71`) — static
   per-component defaults, converted to Style in `CoreUiComponentStyles`.
3. `UiColorTokens.neutralStyle()/destructiveStyle()` — token-derived default *state rules*.
4. Headless `SurfaceStyle`/`SurfaceVisuals` (+ `SurfaceBorder`/`SurfaceShadow`) — a parallel
   struct family whose only job is `asPrimitiveStyle()` conversion back into (1).
5. `UiButtonVariant.resolveFill()` — programmatic per-state fill mutation inside the
   button primitive, independent of any Style rule.

shadcn-compose (the reference the user pointed at) expresses everything with (1) alone:
variant = one Style with hovered/pressed/disabled blocks. Consolidation direction: variants
build `Style` values directly; headless takes `style: Style`; delete `SurfaceVisuals`
conversion layer and `resolveFill`; reduce theme component defaults to state-neutral base
values. That removes the merge-order class of bug (this audit's P0) structurally instead of
patching precedence.
