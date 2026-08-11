# Studio layout -- proposed design

Companion to `2026-08-11-studio-layout-audit.md`, which found three inert controls and a left
dock occupied by a demo picker. This proposes the target layout and maps every region to
components that already exist in `ui-designsystem`.

## Target

    +------------------------------------------------------------------------------+
    | Awake Studio | Scene v |          > Play || >|          | Wire  Shadows      |
    +--------------------+-------------------------------------+-------------------+
    | HIERARCHY          |[S]|                                 | INSPECTOR         |
    | [Scene] [Assets]   |[M]|                                 | Transform      v  |
    |                    |[R]|                                 |   Position x y z  |
    |  v Scene           |[S]|                                 |   Rotation x y z  |
    |    > Camera        +---+         VIEWPORT                |   Scale    x y z  |
    |    > Light         |                                     | Mesh           v  |
    |    v Cube          |     (gizmo on selection)            |   Material ...    |
    |        Transform   |                                     | Light          >  |
    |    > Ground        |                                     |                   |
    |                    |                                     |                   |
    +--------------------+-------------------------------------+-------------------+
    | Console                                                          [x] [!] [i] |
    | 12:47 scene loaded (4 entities)                                              |
    +------------------------------------------------------------------------------+
    | Edit mode | 4 entities | 60 fps                                    [Vulkan]  |
    +------------------------------------------------------------------------------+

Four docks around a viewport, which is the arrangement Unity, Godot, Unreal and Blender all
converge on. The differences from today are: the left dock holds the scene, not the demo list;
the tool rail keeps its place at the viewport edge but gains real transform tools; and a console
dock appears at the bottom.

## Region by region

### Top bar -- `drawStudioTopBar`

| Element | Component | Notes |
|---|---|---|
| Title | `shadcnText` | unchanged |
| Scene picker | `shadcnSelect` or `shadcnDropdownMenu` | this is where the demo list goes, freeing the left dock |
| Play / Pause / Step | `shadcnButton` x3 | today's single Play is really "reload example"; separate the concepts |

Nothing viewport-scoped goes here. Transform tools, camera mode, projection, wireframe and
shadows all move to the viewport -- see below. Wireframe and shadows sat in the top bar in an
earlier draft, which was the same scoping mistake as the tools: they govern how ONE VIEWPORT
displays, not the document.

### Left dock -- new `HierarchyPanel`

`shadcnTabs` with two tabs:

- **Scene** -- the entity tree, driven by `world.queryEach<Name>`
- **Assets** -- placeholder now; the eventual content browser

Rows are `shadcnContextMenu` triggers (rename, delete, focus). Clicking dispatches the
`SelectEntity` intent that already exists and is currently discarded.

Nesting: there is no tree component in `ui-designsystem`. Two options, in preference order:

1. Flat list first. Scenes here are small, `Transform` has no parent link today, so a tree would
   be decorative. Ship flat, add nesting when parenting exists.
2. `shadcnCollapsible` per node if nesting is wanted sooner -- it composes, but indentation and
   keyboard traversal would be hand-rolled.

### Viewport

Gains a header strip, and keeps its left pill. The split follows Blender (the `T` toolbar holds
only tools; the header along the top holds shading, overlays and view) and Unity (tools top-left,
display toolbar beside them).

| Control | Home | Component | Interaction |
|---|---|---|---|
| Select / Move / Rotate / Scale | left pill | `shadcnToggleGroup` | modal, mutually exclusive |
| Camera mode | header | `shadcnSelect` | 4 options, needs labels |
| Perspective / Ortho | header | `shadcnToggleGroup` | 2-way modal |
| Wireframe, Shadows | header | `shadcnToggle` | independent booleans |

They are deliberately NOT all in the pill. The pill is a single-select control; wireframe and
shadows are independent booleans and camera mode is a picker. Three interaction models in one
strip muddles all three, and `shadcnToggleGroup`'s single-select semantics only fit the first.

This also fixes a discoverability problem in the current build: camera mode lives in a
right-click menu (`viewportCameraMenu`), so it is invisible until you know it exists. A header
puts it on screen.

Two further additions once selection works:

- click-to-select, hit-testing against entity bounds
- a transform gizmo reflecting the active tool

The floating vertical pill **stays**, with its contents replaced by real transform tools --
Select / Move / Rotate / Scale, backed by `shadcnToggleGroup` for single-select and a pressed
state, which is the semantics the current rail hand-rolls and never uses.

An earlier draft of this document moved them to the top bar. That was wrong, and contradicted
this design's own audit, which had already found the placement conventional and only the wiring
broken. Reasons the pill is the right home:

- It is the convention. Blender's toolbar is a vertical strip at viewport left; Unity puts
  transform tools at the viewport's top-left.
- Scope. Transform mode governs how you interact with A VIEWPORT, not the document. A top-bar
  control implies global state.
- It survives a second viewport. A split view or a second camera pane makes a global tool
  selector immediately wrong; a per-viewport pill stays correct.
- Proximity. You are already looking at the viewport when you switch tools.

The pill's problem was never where it sits -- it was five buttons wired to nothing. Deleting the
container would have been fixing the wrong thing.

### Right dock -- `InspectorPanel`

Becomes selection-driven and editable:

- reads `inspector.selectedEntityId` instead of listing every entity
- one `shadcnCollapsibleCard` per component (Transform, MeshRenderer, Light)
- fields become `shadcnFieldTextField` / `shadcnFieldSlider` / `shadcnFieldSwitch` rather than
  `shadcnText` pairs
- `shadcnEmpty` when nothing is selected

The `Field*` wrappers already pair a label with a control, which is the exact shape an inspector
row needs.

### Bottom dock -- new `ConsolePanel`

Vertical `shadcnResizablePanelGroup` wrapping the horizontal one, so the console is draggable.
`shadcnScrollArea` for the log, `shadcnBadge` counters for error/warning/info, `shadcnTabs` if
an asset browser lands beside it later.

Lowest priority of the four docks: it is additive, where the others fix things that currently
mislead.

### Status bar

Replace the hard-coded `Vulkan` badge with the real backend, and add entity count and frame
rate. The badge is currently a literal because the `ui` package cannot reach the running window
-- that needs the same plumbing as the cursor gap (`SceneGameRuntime` exposing what it already
computes).

## Missing from the design system

| Need | Status |
|---|---|
| Tree / outliner | absent -- flat list first, see above |
| Numeric drag field (scrub to change) | absent -- `shadcnFieldSlider` is the nearest, needs bounds |
| Splitter for nested groups | `shadcnResizablePanelGroup` nests already |
| Gizmo | not a UI component; viewport-space rendering |
| Menu bar | `shadcnDropdownMenu` composes into one |

Only the tree and the numeric drag field are genuinely new UI work, and both have workable
substitutes for a first pass.

## Sequencing

Each phase is independently shippable and leaves studio working.

**Phase 1 -- camera.** Adopt `CameraMode` + `CameraInputSystem`, delete `CameraPresetMath`. Four
modes, per-mode drag and scroll. No layout change. Highest result-to-risk ratio, and independent
of everything below.

**Phase 2 -- hierarchy.** Move the demo picker to the top bar, add `HierarchyPanel` in the left
dock as a flat list, wire click -> `SelectEntity`.

**Phase 3 -- selection.** Inspector reads `selectedEntityId`, shows one entity, `shadcnEmpty`
otherwise. Phases 2 and 3 together are what make the existing inert plumbing mean something.

**Phase 4 -- tools.** Replace the icon rail's CONTENTS with a `shadcnToggleGroup` of real
transform tools, keeping it where it is. Until a gizmo exists the tools only set state -- so either land a gizmo in the same phase, or ship Select alone
and add the rest with the gizmo. Do not ship four more buttons that look pressed.

**Phase 5 -- editable inspector.** `Field*` controls writing back to components.

**Phase 6 -- console dock.** Vertical resizable group, log panel.

Phases 1-3 address everything the audit found actually broken. 4-6 are the editor-shaped
features on top.

## Open questions

- Should the demo picker stay at all once real scene loading exists, or is it a samples-only
  affordance?
- Is studio meant to edit scenes (write back to a scene document) or only inspect a running
  world? Phase 5 and the gizmo only make sense under the first reading.
