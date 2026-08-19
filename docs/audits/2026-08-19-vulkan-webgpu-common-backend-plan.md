# Vulkan/WebGPU common backend — phased plan

Status: draft, not implemented. Chosen over
[2026-08-19-vulkan-godclass-and-shared-render-logic-plan.md](2026-08-19-vulkan-godclass-and-shared-render-logic-plan.md)'s
Part B ("share the math, not the drawing") after discussing both options directly —
this is the bigger bet: one shared Kotlin implementation of render-feature logic across
both backends, not just shared pure functions. Part B's step 1 (uniform-packing extraction,
fixes the already-drifted shadow-depth-scale bug) is still worth doing regardless of this
plan's timeline — it's small, safe, and unrelated to whether this larger effort proceeds.

## Prior art and adjacent scope

[docs/tasks/2026-08-17-awake-core-module-split-proposal.md](../tasks/2026-08-17-awake-core-module-split-proposal.md)
already establishes real precedent for the hand-authored-vs-generated split this plan draws:
`awake:backend:vulkan:generator` (a hand-authored codegen tool) already produces
`awake:backend:vulkan:bindings` (generated Vulkan API bindings) — the same shape as this
plan's `SharedXRenderFeature` (hand-authored) vs `VulkanCommandRecorder`/`WebGpuCommandRecorder`
(thin, backend-specific), just one layer up. That doc's `Rendering + physics backend shape`
section is worth reading before Phase 0 lands, for the existing dependency-flow discipline any
new module here should match. One correction: that doc states backend selection happens via
`expect class Renderer(...)`; everything read directly from source this session shows a
concrete `Renderer` class per backend instead, selected by which `GameApplication` subclass
(`VulkanGameApplication`/`WebGpuGameApplication`) a consumer constructs — not `expect`/`actual`.
Worth fixing in that doc separately; not chased here.

**Platform separation is a different, orthogonal axis — not folded into this plan.** That same
proposal's `awake:core:platform` (window creation, app lifecycle, display settings) is about
`WindowApplication`/`GameApplication`'s own concerns, not rendering. This plan's
`CommandRecorder` abstraction only touches *per-frame draw-call recording* — it has no opinion
on window/lifecycle management and doesn't need `awake:core:platform` to exist first. If/when
that module gets built, it changes where `WindowApplication`'s implementation lives, not
anything in this plan's design. Keeping the two separate on purpose: conflating "how frames get
recorded" with "how a window gets created" was exactly the kind of scope-creep this plan's own
"why phased, not a rewrite" section argues against.

## What "common backend" means, concretely

Today: `Renderer` (the interface, `awake:engine:render:contract`) is shared. Each backend's
concrete `Renderer` class implements it separately, and — as of today's `RenderFeature`
refactor — each backend also has its own `RenderFeature`-shaped classes recording real GPU
commands (`VkCommandBuffer` calls for Vulkan, would-be `GPUCommandEncoder` calls for WebGPU).
That's the layer this plan unifies: one `OpaqueRenderFeature`/`SkyboxRenderFeature`/
`UiRenderFeature`/`ShadowFeature` implementation, written once, running on both backends via
a thin per-backend `CommandRecorder`.

## Membership vs. extension — one rule for both plans

`render:passes` holds two different shapes of thing, and they get two different treatments,
consistently:

- **Stateful behavior → a class implementing an interface, member functions.**
  `SharedOpaqueRenderFeature`/`SharedSkyboxRenderFeature`/etc. hold real state (a pipeline
  reference, a mesh) and implement `RenderFeature`, matching the already-implemented
  `OpaqueRenderFeature`/`SkyboxRenderFeature` classes' own shape today — proven to work, no
  reason to change it.
- **Pure math with no state → a plain top-level function, explicit params, never a receiver
  extension.** `skyboxUniformFloats`, `pbrTexturedMaterialFloats`, `fogFloats`, and every
  other function [the god-class plan](2026-08-19-vulkan-godclass-and-shared-render-logic-plan.md)
  moves into `render:passes`. No `fun RenderRenderer.xxx()` — an extension on a stateful
  interface just relocates the implicit-receiver coupling this plan's own "Design audit"
  section (in the render-feature plan) already rejected for `RenderFrameContext`. A function
  that reads `this.fogColor` instead of taking `fogColor: FloatArray` hides its real inputs
  and is harder to unit-test standalone — exactly the failure mode both plans are trying to
  design out of the render layer, not reintroduce in a new module.

The dividing line: does it own state across multiple calls (→ class), or is it one calculation
from explicit inputs to an output (→ plain function)? Not "does Kotlin let me write it as an
extension" — that's available for both shapes and answers a different question than which one
is actually correct here.

## Module naming

This plan's new types (`CommandRecorder`, `MaterialBinding`, `PipelineHandle`,
`SharedXRenderFeature` classes) do NOT land in `render:contract` — that module is already
mostly pure interface/vocabulary types (`Renderer`, `Material`, `DrawCall`, `VertexFormat`,
`UniformLayout`, etc.), and adding real behavior on top would make its own name stop being
accurate. New sibling module instead: **`awake:engine:render:passes`** — depends on
`render:contract` for the types, holds the shared behavior: this plan's abstraction plus
[the god-class plan](2026-08-19-vulkan-godclass-and-shared-render-logic-plan.md)'s
`MaterialUniforms.kt`/`LightUniforms.kt`, and `SkyboxUniforms.kt` (moved here from
`render:contract`, where it's the one existing real-logic outlier in an otherwise-pure-types
module). `passes` names what's actually in it (render-pass logic), not a filler word like
`shared`/`common` — matches this repo's own naming standard
(`docs/reference/agent-catalog.md`'s "avoid informal suffixes" rule, same discipline as the
`awake:scene:runtime`/`awake:scene:authoring` role-based split).

## The abstraction — simpler than a full RHI, using this codebase's existing idiom

The hard part in a naive design is unifying Vulkan descriptor sets and WebGPU bind groups —
genuinely different binding mechanics. **Sidestep it**: materials are already created once,
not per frame, and each backend's `Material` already produces an opaque binding artifact
internally (Vulkan: a `DescriptorSetHandle`; WebGPU: its bind-group equivalent). The shared
layer never needs to know that shape — it just needs an opaque `MaterialBinding` marker type
per backend, matching the `DescriptorSetLayoutHandle`/`BufferHandle`-style opaque-handle
pattern this codebase already uses everywhere (`awake/backend/vulkan/.../handles/`).

```kotlin
// awake:engine:render:passes — new module, package e.g. renderer.command

/** Opaque per-backend draw-call recorder. Shared RenderFeature code holds one of these per
 * frame and never touches the concrete Vulkan/WebGPU type underneath. */
interface CommandRecorder {
    fun bindPipeline(pipeline: PipelineHandle)
    fun bindMaterial(set: Int, binding: MaterialBinding)
    fun bindVertexBuffer(binding: Int, buffer: BufferHandle)
    fun bindIndexBuffer(buffer: BufferHandle)
    fun draw(vertexCount: Int, instanceCount: Int = 1)
    fun drawIndexed(indexCount: Int, instanceCount: Int = 1)
}

/** Opaque, backend-defined -- Vulkan's implementation IS a DescriptorSetHandle wrapper,
 * WebGPU's IS its bind-group wrapper. Shared code passes this through without inspecting it. */
interface MaterialBinding

interface PipelineHandle
```

`VulkanCommandRecorder` wraps a `VkCommandBuffer` plus enough bound-pipeline-layout tracking
to route `bindMaterial` to the right `vkCmdBindDescriptorSets` call.
`WebGpuCommandRecorder` wraps the active `GPURenderPassEncoder` and calls `setBindGroup`.

**Hard rule this plan draws explicitly**: `SharedXRenderFeature` classes (hand-authored draw
logic — order, grouping, what gets drawn when) never call `Vulkan.*` or a WebGPU-generated
type directly, and never will, at any phase. `VulkanCommandRecorder`/`WebGpuCommandRecorder`
are the *only* files allowed to make a real graphics-API call, and they contain no draw-order
logic of their own — each method is a one-line translation of one `CommandRecorder` call into
one real API call. If a shared feature class ever needs a Vulkan- or WebGPU-specific import to
compile, that's a signal the abstraction is leaking and the missing capability belongs on
`CommandRecorder` as a new method, not as a backend-specific branch inside the shared class.

Neither backend's actual resource-creation code (pipeline/material construction) changes —
only *recording a draw* moves behind the interface.

## Pass boundaries stay backend-owned (matches the render-feature plan's own precedent)

Same rule as `ShadowFeature` in the already-implemented refactor: a feature records draws
into a pass someone else opened. Beginning/ending a render pass — `VkRenderPassBeginInfo`+
framebuffer selection on Vulkan, `GPURenderPassDescriptor` on WebGPU — stays backend-specific,
orchestrated by each backend's own `recordCommandBuffer`-equivalent, which opens a pass, hands
the shared feature a `CommandRecorder`, and closes the pass. The shared layer never begins or
ends a pass, same boundary already established.

## Why phased, not a rewrite

A ground-up rewrite of both backends was considered and rejected for this plan: it would put
two renderers in play at once for the whole effort (old one still has to keep working, new
one isn't done yet), and renderer bugs are uniquely bad to introduce silently — a wrong
binding or a mis-recorded draw doesn't throw, it just renders slightly wrong. Each phase below
ships a working, verified state; nothing is "in progress and broken" between phases.

## Phase -1 — a real performance harness, before Phase 0 touches any renderer code

Today's fps investigation (earlier this session) was ad-hoc: a background agent running
`StudioFramePerfProbeTest`/`RendererHeadlessFrameTimingTest` by hand, reading numbers off
console output, no automated before/after comparison. Before this plan's phases start
touching the draw path, build a small reusable harness on top of those two existing tests:

- A named-span timer utility (start/stop by label, e.g. `"opaque-pass"`, `"ui-pass"`,
  `"shadow-pass"`) — the natural hook point is each `RenderFeature.recordCommands` call,
  already a single method per pass after today's refactor.
- A stored baseline (checked-in numbers from the pre-migration state) each phase's CI/test run
  compares against, failing loudly on regression past a tolerance, not just reporting a number
  a human has to eyeball.
- Reuses `StudioFramePerfProbeTest`'s CPU-side harness and `RendererHeadlessFrameTimingTest`'s
  real-GPU harness — this is packaging/generalizing what already exists, not building new
  measurement infrastructure from scratch.

**This becomes the acceptance gate for every phase below**: a phase isn't done until the
harness shows the migrated feature is within noise of its pre-migration baseline. Prevents
exactly the failure mode of a big rewrite — a subtle regression discovered only after
everything's already been torn up.

## Phased rollout — smallest real feature first, shadow last

**Phase 0 — define the interfaces, zero behavior change.** `CommandRecorder`/`MaterialBinding`/
`PipelineHandle` land in the new `awake:engine:render:passes` module. Nothing calls them yet. Reviewable in
isolation, no runtime risk.

**Phase 1 — migrate Opaque only.** Smallest real feature both backends already have. Write
`SharedOpaqueRenderFeature.recordCommands(recorder: CommandRecorder, groupedDrawCalls, ...)`
once; `VulkanCommandRecorder`/`WebGpuCommandRecorder` are the only new backend-specific code.
This is the spike that proves or disproves the abstraction — if `bindMaterial`'s opaque-handle
approach turns out to be awkward here, better to find out on the simplest feature before
committing further phases. **Stop and reassess after this phase**, don't pre-commit to 2-4.

**Phase 2 — migrate UI.** More complex: WebGPU's scissor-clamping requirement
(`webgpu/RendererDraw3D.kt:331-341`) is real per-backend behavior with no Vulkan equivalent —
this phase needs to prove the abstraction can accommodate one backend needing an extra step
the other doesn't (likely: a `CommandRecorder.setScissor(rect)` method that's a real Vulkan
call and a clamp-then-call on WebGPU, not a special case bypassing the interface).

**Phase 3 — migrate Skybox**, once 1-2 are proven.

**Phase 4 — Shadow, deliberately last.** WebGPU has no shadow pass today
(`webgpu/renderer/Renderer.kt:177-180`: `shadowsEnabled` stored, never read). This phase is
not a refactor — it's building WebGPU shadow support for the first time, informed by what
Vulkan's `ShadowFeature` already does, then unifying both under the shared abstraction. Don't
attempt to design a "shared shadow feature" before WebGPU has any shadow implementation to
unify with — that's designing against half a picture, the exact trap this plan's Q&A
discussion flagged for the original common-backend question.

## Honest cost

This is real multi-phase engineering, not a refactor pass — each phase touches the
correctness-critical draw path of both backends and needs real hardware/browser verification
(live render checks, not just compilation), same rigor as today's `RenderFeature` refactor's
own live-verification steps. Phase 4 additionally requires building a genuinely new WebGPU
feature, not just moving code. Treat phases 2-4 as provisional until Phase 1 proves the
`CommandRecorder`/`MaterialBinding` shape holds — don't treat this plan's phase list as
committed scope, only its ordering logic.

## Relationship to the other two follow-up docs

- [2026-08-19-vulkan-godclass-and-shared-render-logic-plan.md](2026-08-19-vulkan-godclass-and-shared-render-logic-plan.md)
  Part A (`Renderer.kt` de-bloat) is unrelated, can proceed independently, any order.
- That doc's Part B (pure-function extraction) is a strict subset of what this plan
  eventually needs anyway (`pbrTexturedMaterialFloats`/`fogFloats`/the light block computed
  once, called from the shared `RenderFeature` implementations regardless of which
  `CommandRecorder` records the resulting draw) — worth doing now rather than later,
  it's on the critical path for this plan too, not throwaway work if this plan stalls after
  Phase 1.
