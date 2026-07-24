# 2026-07-22: UiContext Refactor Plan

## Goal

Continue the `UiContext` cleanup as a staged architecture refactor instead of a second
overlapping implementation pass.

This note exists because the UI runtime is already being split under
`awake:engine:ui:ui-core`'s `ui/context` package, and a fresh one-shot rewrite would likely
conflict with that in-flight work.

## Status Update

Current progress is roughly **85% complete**.

Completed:

- Phase 2 is done in practice: persisted widget state now lives in a dedicated
  `UiStateStore` instead of interaction state.
- Phase 3 is done in practice: `UiFrameOutput` and `finishFrame()` exist, and the runtime
  path now consumes the unified frame result.
- Phase 4 is mostly done in practice: the `UiContextServiceRegistry` seam was removed from
  `ui-core`, and runtime-facing service access now stays in higher-level layers.

Still open:

- compatibility cleanup around older shims and call sites
- measurement isolation follow-through
- input payload rename/split, which is intentionally deferred until the frame contract
  settles

Validation snapshot:

- `:awake:engine:ui:ui-core:compileKotlinDesktop` passes
- `:awake:engine:ui:ui-unstyled:compileKotlinDesktop` passes
- `:awake:engine:game-dsl:compileKotlinDesktop` passes
- `:awake:scene:compileKotlinDesktop` passes
- `:awake:scene-dsl:compileTestKotlinDesktop` still fails, but the remaining failures are
  in older `SceneDslTest` API drift rather than in the `UiContext` split itself

## Why This Needs A Plan

`UiContext` started as a compact immediate-mode runtime and that foundation is still good.
But as Awake's UI stack has grown, the context has accumulated too many responsibilities:

- frame lifecycle
- theme, font, and text-style stacks
- pointer capture and focus ownership
- widget-local persisted state
- primitive and overlay staging
- semantic collection
- clip-stack management
- measurement-only execution
- runtime service lookup

That is still workable for an MVP, but it is no longer a clean long-term boundary for
`ui-core`.

## Current State

The split has already started in `awake/engine/ui/ui-core/src/commonMain/kotlin/io/github/ronjunevaldoz/awake/ui/context/`.

Current extracted pieces already include:

- `UiContextStacks`
- `UiRuntimeCoordinator`
- `UiContextFrameState`
- `UiContextInteractionState`
- `UiMeasurementRuntime`
- `UiContextServiceRegistry`
- `UiLayoutFactory`

That is a strong direction. The next step should not be "replace it again." The next step
should be "finish the split and tighten the remaining boundaries."

## Main Architectural Reading

### What is already better

- interaction logic is no longer mixed directly with primitive collection
- frame state and semantic/render collection now have separate homes
- measuring has its own runtime objects instead of living only as ad hoc code inside one
  large file

### What is still not clean enough

- `UiContext` is still the facade for too many concerns, even if some internals moved out
- `inputResult()` and `endFrame()` are still separate, order-sensitive concepts
- widget-local state still lives under interaction state, even though persistence is a
  different concern from pointer/focus ownership
- service lookup still lives in `ui-core`, which is too high-level for the foundational UI
  runtime
- measuring is still modeled as a boolean flavor of the same public context instead of a
  fully separate execution shape

## Recommended Boundary

Keep `UiContext` as a public facade, but make it intentionally small.

Recommended public role:

- frame entry point
- layout factory access
- style/theme/font scope access
- frame output retrieval

Recommended internal ownership:

- `UiEnvironment`
  theme stack, font stack, text-style stack
- `UiInteractionTracker`
  active id, focused id, pointer edge, pointer capture, scroll claims
- `UiStateStore`
  persisted per-widget state
- `UiFrameCollector`
  primitives, overlay primitives, semantic nodes, clip stack, frame bounds
- `UiMeasurementEngine`
  measurement-only execution and measured-slot accumulation
- `UiPlatformBridge` or runtime-owned equivalent
  service lookup and platform effects such as soft-keyboard requests

## Input Model Recommendation

Awake should keep **input state** as the primary shared-UI abstraction.

### Recommendation

- platform adapters use listeners/callbacks/events
- those events feed the session-local `Input` accumulator
- each frame produces an immutable snapshot
- UI consumes the snapshot
- UI returns ownership/effect results for the platform and gameplay layers

### Why

This fits Awake's immediate-mode UI better than listener-first widgets:

- widgets are reevaluated every frame already
- snapshot input keeps custom widgets deterministic
- tests can replay input easily
- layout and interaction stay renderer-neutral
- UI can remain decoupled from Android/Desktop/Web event models

### Listener rule

Listeners belong at the platform edge only.

Shared UI modules should not expose widget-level listener plumbing for pointer or text
events as their primary interaction model.

### Naming improvement

`UiInputState` is carrying both state-like data and transient event-like data.

Recommended future direction:

- rename to `UiInputFrame`, or
- split into:
  - `UiPointerSnapshot`
  - `UiScrollDelta`
  - `UiTextInputFrame`

That would make the API read closer to what it actually represents: one frame's UI-facing
input payload.

## Proposed Frame Contract

The biggest cleanup win is to replace the split end-of-frame contract with one explicit
result object.

Current shape:

- `beginFrame(...)`
- widgets run
- `inputResult()`
- `endFrame()`

Recommended shape:

- `beginFrame(...)`
- widgets run
- `finishFrame(): UiFrameOutput`

Recommended output:

```kotlin
data class UiFrameOutput(
    val primitives: List<UiDrawPrimitive>,
    val semantics: List<UiSemanticNode>,
    val ownership: UiInputOwnership,
    val effects: UiPlatformEffects
)
```

Benefits:

- removes order sensitivity between `inputResult()` and `endFrame()`
- makes UI-to-platform effects explicit
- gives scene/game runtime one object to pass onward
- makes future output growth easier without widening `UiContext` again

## Service Lookup Recommendation

`UiScope.service()` is convenient, but the service-registry seam does not belong in
`ui-core` long-term.

Recommended direction:

- keep service lookup in `game-dsl`, `scene`, or another runtime-facing layer
- keep `ui-core` focused on renderer-neutral UI runtime concerns
- custom widgets in shared modules should accept explicit values/callbacks rather than pull
  runtime services from the context

This aligns with Awake's ownership rules: reusable UI modules should stay generic and not
learn more runtime wiring than they need.

## Phased Plan

### Phase 1: Stabilize the current split

- keep the new `ui/context` package as the active refactor direction
- do not restart the split in parallel under different type names
- finish moving remaining internal responsibilities behind the smaller collaborator types
- keep public behavior unchanged

### Phase 2: Separate persisted widget state from interaction state

- move `widgetStates` out of `UiContextInteractionState`
- introduce a dedicated `UiStateStore`
- keep `rememberStateValue(...)` behavior unchanged

### Phase 3: Collapse frame completion into one result

- add `UiFrameOutput`
- add `finishFrame()`
- internally compute:
  - draw primitives
  - semantic nodes
  - input ownership
  - platform effects
- keep `inputResult()` and `endFrame()` temporarily as compatibility shims if needed

### Phase 4: Move runtime/platform seams out of `ui-core`

- deprecate `bindServiceResolver(...)`
- remove `UiContextServiceRegistry` from the long-term public design
- move `UiScope.service()` helpers to runtime-facing layers only
- replace direct `Input.textInputFocused` mutation with explicit UI output effects

### Phase 5: Finish measurement isolation

- keep measurement execution separate from the live frame coordinator
- minimize boolean `measuring` branching in the shared runtime path
- treat measuring as its own engine, not as "normal runtime but muted"

### Phase 6: Rename input payloads

- evaluate renaming `UiInputState` to `UiInputFrame`
- or split the snapshot into pointer/text/scroll submodels
- do this after frame-output cleanup so the migration only happens once

## Non-Goals

This plan does **not** recommend:

- replacing immediate-mode UI with retained-mode UI
- introducing platform-native event models into shared widgets
- moving sample/runtime concerns into `ui-core`
- mixing style-system work into the `UiContext` cleanup itself
- inventing a new second public context type unless the existing facade proves insufficient

## Conflict Guidance

Because the workspace already contains an in-progress `UiContext` split, any further work in
this area should follow these rules:

- prefer continuing the current `ui/context` extraction over starting a separate refactor
- avoid large API renames until the internal split stabilizes
- avoid touching scene/game runtime call sites unless a frame-contract migration is ready
- keep compatibility shims during the transition so tests and samples can move gradually

## Suggested First Implementation Slice

The safest next slice is:

1. extract `UiStateStore`
2. add `UiFrameOutput`
3. implement `finishFrame()` internally
4. keep `inputResult()` and `endFrame()` delegating to the new output during migration

That gives a real architecture improvement without forcing a broad rename or a second
competing split.

## Validation

- `:awake:engine:ui:ui-core:commonTest`
- `:awake:engine:ui:ui-unstyled:commonTest`
- `:awake:engine:ui-dsl:commonTest`
- `:awake:engine:ui:ui-designsystem:commonTest`
- `:awake:engine:game-dsl:commonTest`
- `:samples:ui-showcase:commonTest`
- targeted `desktopTest` where frame output or text-input focus behavior changes

## Done When

- `UiContext` is a thin facade instead of the place every new runtime feature lands
- widget persistence is separated from pointer/focus routing
- frame completion is represented by one explicit output object
- platform/runtime service seams are no longer owned by `ui-core`
- the input model remains snapshot-based in shared UI, with listeners confined to platform
  bridges
- the migration can proceed incrementally without colliding with parallel refactor work
