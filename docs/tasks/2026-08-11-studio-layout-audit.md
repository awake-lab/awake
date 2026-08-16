# Studio layout audit

Audited 2026-08-11 against `samples/studio/src/commonMain/kotlin/.../ui/`. Findings are from
reading the wiring, not from opinion about styling.

## Status as of 2026-08-16

Resolved: engine camera adopted (rec. 1); left dock is the hierarchy and selection is wired end
to end (2, 3); the tool rail's five inert buttons are deleted, leaving reset and camera (4); the
status bar reports the configured backend and the scene's entity count; `SceneLoader.fromWorld`
supplies the `World -> SceneDocument` direction the design doc listed as missing.

Open: gizmo and transform tools, editable inspector (needs a numeric drag field), edit/play mode
split, save/load UI on top of `fromWorld`, timeline and asset dock tabs, multi-scene tabs.

The demo picker moved to the top bar rather than a tab strip -- a strip over one openable scene
is the mistake the design doc's own tab-strip note warns about.

## Current layout

    +--------------------------------------------------------------+
    | top bar: "Awake Studio" | Play | Wireframe sw | Shadows sw    |
    +--------------------------------------------------------------+
    | example  |[icon]|                              |              |
    | rail     |[rail]|         viewport             |  inspector   |
    | (demos)  | pill |                              |  (entities)  |
    +--------------------------------------------------------------+
    | status bar: "Edit mode"                            [Vulkan]   |
    +--------------------------------------------------------------+

Three resizable panels, two handles, via `shadcnResizablePanelGroup`. That part is sound: no
hand-rolled layout, the widget records its own semantics.

## Dead controls (state changes, nothing happens)

**The whole tool rail.** `StudioContract.Tool` has five entries -- Layers, Grid, Environment,
History, Panels. Selecting one dispatches `SelectTool`, the store updates `toolRail.activeTool`,
and `IconRail` highlights the pressed button. `activeTool` is then read in exactly one place:
`IconRail` itself, to decide which button looks active. Nothing else in the codebase reads it.
Five buttons whose only effect is to look pressed.

**Entity selection.** `InspectorState.selectedEntityId` is written by `StudioStore` on
`SelectEntity` and read by nothing. `InspectorPanel` lists every named entity via
`world.queryEach<Name>` regardless of selection, so there is no selected-entity concept in the UI
at all -- the field exists, the intent exists, the reducer exists, and the result is discarded.

**Status bar.** "Edit mode" is a literal, and the `Vulkan` badge is hard-coded rather than read
from the running backend (its own comment says the `ui` package cannot reach that without
depending on `app`).

## Structural gaps vs a modern editor

Comparing against the layout conventions shared by Unity, Godot, Unreal and Blender:

| Region | They put | Studio has |
|---|---|---|
| Left dock | Scene hierarchy / outliner | Example (demo) list |
| Viewport edge | Transform tools: select, move, rotate, scale | 5 non-functional icons |
| Right dock | Inspector, editable | Inspector, read-only |
| Bottom dock | Console / log / asset browser | Single-line status bar |
| Top | Menu bar, transform-space and snap toggles | Title, Play, 2 render switches |

The big one is the left dock. A scene editor's primary navigation is the object tree; studio uses
that slot for a demo picker, so there is nowhere to see or select what is actually in the scene.
That is also why entity selection has no UI to hang off.

Second is the viewport itself: no selection, no gizmo, no camera drag outside Orbit (the drag
handler returns early for other modes). The viewport is currently a render target, not a
manipulable scene.

## The camera problem, restated

`awake:scene:controls` already ships `CameraMode` (FirstPerson, ThirdPerson, Cinematic, TopDown)
with `CameraComponent`, `CameraSystem` and `CameraInputSystem` handling drag and scroll per mode
via `usesYaw`/`usesPitch`/`usesZoom`. `samples:scene3d-playground` uses it.

Studio instead defines `StudioContract.CameraPresetMode` (Orbit, Front, Top) with hand-rolled
math in `CameraPresetMath`, and drag wired only for Orbit. So Cinematic and TopDown are
unreachable from studio, the viewport is undraggable in Front/Top, and there are two camera
implementations to maintain.

## Recommendation

Ordered by leverage. Each is independently shippable.

**1. Adopt the engine camera system.** Replace `CameraPresetMode`/`CameraPresetMath` with
`CameraMode` + `CameraInputSystem`. One change gets four working modes, per-mode drag and scroll,
and deletes a parallel implementation. `scene3d-playground` is the reference. This is the highest
ratio of result to risk in the list.

**2. Make the left dock a scene hierarchy.** Move the demo picker somewhere it belongs (top-bar
dropdown, or a tab) and put the entity tree in the left panel. This is the change that makes
`selectedEntityId` mean something and unblocks everything selection-shaped.

**3. Wire selection end to end.** Hierarchy click -> `SelectEntity` -> inspector shows only that
entity. The plumbing already exists and is inert; this connects it.

**4. Either wire the tool rail or delete it.** Five buttons that only look pressed are worse than
no buttons. If the intent is transform tools (select/move/rotate/scale) that is a real feature
behind a gizmo; if the intent was layout toggles, wire them to panel visibility. Deleting is a
legitimate answer until one of those exists.

**5. Make the inspector editable.** Read-only property rows are a viewer, not an inspector.
Transform fields are the obvious first target.

Not recommended yet: bottom console/asset browser, menu bar. They are real gaps but they are
additive, and the items above fix things that are currently misleading rather than merely absent.

## Smaller items

- Viewport padding: `drawStudioViewportPanel` puts `.padding(8f.dp)` on the viewport column, so
  the 3D render inset does not align with the panel edges. Reported as wrong; confirm the intended
  inset before changing, since the icon rail sits inside the same row.
- The vertical pill toolbar's placement is conventional (Blender/Unity both float tools at the
  viewport edge). The problem is not where it is, it is that it does nothing.
- No custom cursor in studio: `SceneGameRuntime` has the cursor in its frame effects and discards
  it, unlike `GameUiRuntime`, and no service registration exposes the runtime to an entry point.
