# RenderFeature Strategy refactor — plan draft

Status: **implemented** (commits e26c7ca/466e1b3/d5e738f0 on `dev/improvement`). Kept as the
design record — the implementation followed this shape with the deviations noted inline where
the real source disagreed with the sketch. Follow-up work this implementation surfaced (the
`Renderer.kt` god-class regrowth, Vulkan/WebGPU shared-logic extraction) is scoped separately in
[2026-08-19-vulkan-godclass-and-shared-render-logic-plan.md](2026-08-19-vulkan-godclass-and-shared-render-logic-plan.md).
Pairs with [skills/awake-render-pipeline/SKILL.md](../../skills/awake-render-pipeline/SKILL.md)
(§1) and the `Renderer.kt` god-class finding (702 lines / 19 functions,
`awake/backend/vulkan/src/commonMain/kotlin/io/github/ronjunevaldoz/awake/vulkan/renderer/Renderer.kt:91`)
from the 2026-08-19 vulkan-backend audit. Independent of, but eventually consumed by, the ASL
shader-DSL sketch — see "Relationship to ASL" near the end of this doc.

## Current shape (verbatim from source, as of the pre-refactor baseline)

`Renderer` held each pass as its own nullable/lateinit field:

```kotlin
// Renderer.kt:114-135 (pre-refactor)
class Renderer(
    graphicsDevice: GraphicsDevice,
    swapchainManager: SwapchainManager,
    pipelines: PipelineTable,
    internal val lineRenderPipeline: LineRenderPipeline,
    internal val transferContext: TransferContext,
    uiShaderPairs: UiShaderPairs,
    internal val maxFramesInFlight: Int,
    shadow: ShadowResources? = null,
    internal val skyboxRenderPipeline: SkyboxRenderPipeline? = null,
) : RenderRenderer {
    internal val shadowMap: ShadowMap? = shadow?.map
    internal val shadowRenderPipeline: ShadowRenderPipeline? = shadow?.pipeline
    // ...
}
```

`RendererDraw3D.recordCommandBuffer` hardcoded the pass order inline
(`RendererDraw3D.kt:194-382`): begin 3D render pass → bind primary pipeline → draw skybox
(`if (showEnvironment && skybox != null)`) → `recordDrawCalls` for the primary group →
bind+draw debug lines → loop remaining pipeline groups → end 3D pass → begin UI render pass
→ walk `uiRuns` in a `when` block → end UI pass. Adding a pass (e.g. post-process blur) meant
editing this ~190-line function directly. Separately, `performShadowPass`
(`RendererDraw3D.kt:713`) was already its own function, called outside
`recordCommandBuffer` entirely, since the shadow map's render pass is not the scene pass.

## Design audit — two rejected shapes, and why

**Rejected #1 — extension function on `Renderer`.** `fun Renderer.recordCommands(...)`
matches the sibling-file convention (`RendererDraw3D.kt` etc. are all
`internal fun Renderer.xxx(...)`), but it's not actually Strategy — every feature would still
see all of `Renderer`'s internals, so the god class doesn't shrink in the way that matters.

**Rejected #2 — a 3-way sealed `RenderFeature` hierarchy** (`SharedScenePass`/`SharedUiPass`/
`StandalonePass`), built to generalize pass ownership so a *future* feature (post-process
blur) would already have a home. That's speculative generality: at the time there was exactly
one feature that doesn't fit the shared-pass shape (shadow), and building a 3-interface
hierarchy plus 3 dispatch methods plus `filterIsInstance` calls to accommodate a feature that
doesn't exist yet was solving a problem that isn't real yet. Reverted before implementation.

**What's actually true:** three features (`Opaque`, `Skybox`, `UI`) share a pass and fit one
interface. One feature (`Shadow`) owns its own pass entirely and always has —
`performShadowPass` was already separate from `recordCommandBuffer`. The plan below keeps
that one real asymmetry as a special case, not a generalized abstraction. If a second
standalone-pass feature is ever actually built, that's the moment to extract a shared shape
for it — not before (YAGNI).

**Target design: a port/adapter for the 3 shared-pass features.** `RenderFeature` depends
only on a narrow `RenderFrameContext` interface — never on the concrete `Renderer` class.
`Renderer` (via a tiny per-frame adapter) implements that interface. This is a genuine
Strategy + Dependency Inversion combination:

- A feature file can be read and understood with zero knowledge of `Renderer`'s 700 lines —
  it only needs `RenderFrameContext`'s handful of members.
- `Renderer` can add/remove/rename internal fields freely as long as it keeps producing a
  valid `RenderFrameContext` — features can't reach past the port to grab something else.
- Future-proofs open question #1 (WebGPU parity, deferred until Vulkan is proven): a WebGPU
  `Renderer` implementing the same `RenderFrameContext` port could reuse `OpaqueRenderFeature`
  et al. verbatim.
- Each feature becomes independently testable against a fake `RenderFrameContext`, no Vulkan
  device/swapchain construction required.

`ShadowFeature` does not implement `RenderFeature` — it stays its own class with its own
`recordCommands(commandBuffer, frameIndex)` signature, called directly by `Renderer` before
the shared-pass features run, same as `performShadowPass` ran before `recordCommandBuffer`
before this refactor. No shared interface pretends it belongs with the other three.

## Target shape

### 1. `RenderFrameContext` and `RenderFeature` — for the 3 shared-pass features only

```kotlin
// pipeline/RenderFrameContext.kt (new file — zero import of Renderer)
internal interface RenderFrameContext {
    val commandBuffer: Long
    val frameIndex: Int

    /** Computed once per frame by whoever builds this context — every 3D feature needs the
     * same grouping, so it is not recomputed per feature. */
    val groupedDrawCalls: Map<RenderPipeline, List<PreparedDrawCall>>
    val primaryPipeline: RenderPipeline
    val showEnvironment: Boolean
    val uiRuns: List<UiRun>

    fun recordDrawCalls(drawCalls: List<PreparedDrawCall>)

    /** Lazily builds (first call) or returns the cached UI pipeline set -- mirrors today's
     * `RendererUiPipelines.kt` lazy-build-on-first-`drawUi()` behavior. Exposed as a method,
     * not a field, because the underlying `Renderer.uiRenderPipeline` is `internal var` and
     * resize-rebuilt; the port hides that mutability behind one call. */
    fun ensureUiPipelines(): UiPipelineSet

    /** Stateful pooled `DynamicMesh` allocator -- see `Renderer.textureMeshForPrimitive`'s
     * existing doc comment for the pooling contract this must preserve. */
    fun textureMeshForPrimitive(index: Int): DynamicMesh
}

internal data class UiPipelineSet(
    val quad: UiRenderPipeline,
    val glyph: UiGlyphRenderPipeline?,
    val texture: UiTextureRenderPipeline?,
    val roundedQuad: UiRoundedQuadRenderPipeline?,
)

/** Only for features that share a render pass someone else begins/ends -- currently the 3D
 * scene pass (Opaque, Skybox) and the UI pass (UI). Shadow is NOT a `RenderFeature` -- see
 * `ShadowFeature` below and the design audit above for why. */
internal interface RenderFeature {
    fun recordCommands(context: RenderFrameContext)
    fun destroy()
}
```

### 2. Existing pipelines wrapped, not merged

```kotlin
// pipeline/ShadowFeature.kt (new file — does NOT implement RenderFeature; owns its own pass)
internal class ShadowFeature(
    private val shadowMap: ShadowMap,
    private val shadowRenderPipeline: ShadowRenderPipeline,
) {
    fun recordCommands(commandBuffer: Long, frameIndex: Int) {
        // Body is today's performShadowPass (RendererDraw3D.kt:713) moved here verbatim --
        // begins/ends shadowMap.renderPass itself, entirely separate from the scene pass.
    }

    fun destroy() {
        shadowRenderPipeline.destroy()
        shadowMap.destroy()
    }
}

// pipeline/OpaqueRenderFeature.kt (new file — wraps existing RenderPipeline/PipelineTable)
internal class OpaqueRenderFeature(
    private val lineRenderPipeline: LineRenderPipeline,
    private val lineMesh: LineMesh,
) : RenderFeature {
    override fun recordCommands(context: RenderFrameContext) = with(context) {
        primaryPipeline.bind(commandBuffer)
        recordDrawCalls(groupedDrawCalls[primaryPipeline] ?: emptyList())

        lineRenderPipeline.bind(commandBuffer, frameIndex)
        lineMesh.bind(frameIndex, commandBuffer)
        lineMesh.draw(frameIndex, commandBuffer)

        groupedDrawCalls.forEach { (pipeline, group) ->
            if (pipeline === primaryPipeline) return@forEach
            pipeline.bind(commandBuffer)
            recordDrawCalls(group)
        }
    }

    override fun destroy() {
        lineRenderPipeline.destroy()
    }
}

// pipeline/SkyboxRenderFeature.kt (new file — thin wrapper, SkyboxRenderPipeline unchanged)
internal class SkyboxRenderFeature(
    private val skyboxRenderPipeline: SkyboxRenderPipeline?,
) : RenderFeature {
    override fun recordCommands(context: RenderFrameContext) = with(context) {
        val skybox = skyboxRenderPipeline ?: return
        if (!showEnvironment) return
        // Sky first, depth test/write off -- SkyboxRenderPipeline's own contract, unchanged.
        skybox.draw(commandBuffer, frameIndex)
        primaryPipeline.bind(commandBuffer)
    }

    override fun destroy() {
        skyboxRenderPipeline?.destroy()
    }
}

// pipeline/UiRenderFeature.kt (new file)
internal class UiRenderFeature : RenderFeature {
    override fun recordCommands(context: RenderFrameContext) = with(context) {
        val pipelines = ensureUiPipelines()
        // Walk this frame's runs in original paint order, switching pipeline at each run
        // boundary -- identical semantics to RendererDraw3D.kt:292-364 today, just reached
        // through the port instead of direct Renderer field access. TextureRun still calls
        // textureMeshForPrimitive(...) through the context, not a captured Renderer field.
        uiRuns.forEach { run -> /* same when(run) block as RendererDraw3D.kt:293-361 */ }
    }

    override fun destroy() { /* nothing owned directly -- lazy pipelines destroyed by Renderer,
                                 same lifetime split as today */ }
}
```

### 3. `Renderer` holds the ordered shared-pass list, plus `ShadowFeature` as its own field

```kotlin
// Renderer.kt, target shape
class Renderer(
    graphicsDevice: GraphicsDevice,
    swapchainManager: SwapchainManager,
    pipelines: PipelineTable,
    /** Registration order is behavior: opaque before UI (UI draws on top). See
     * skills/awake-render-pipeline's §1. Shadow always runs first regardless of
     * this list's order -- see [shadowFeature]. */
    private val renderFeatures: List<RenderFeature>,
    /** Not part of [renderFeatures] -- owns its own render pass, runs before the scene pass
     * unconditionally when non-null. See the design audit above for why this stays a special
     * case instead of a shared interface. */
    private val shadowFeature: ShadowFeature? = null,
    internal val transferContext: TransferContext,
    internal val maxFramesInFlight: Int,
) : RenderRenderer {
    // no more skyboxRenderPipeline/lineRenderPipeline fields here -- each now lives inside
    // its owning RenderFeature. uiRenderPipeline/uiFramebuffers stay (RendererUiPipelines.kt's
    // lazy-build machinery is unchanged), reached only through the RenderFrameContext port,
    // never accessed by a feature directly.

    internal fun recordSharedPassFeatures(context: RenderFrameContext) {
        renderFeatures.forEach { it.recordCommands(context) }
    }

    internal fun recordShadowPass(commandBuffer: Long, frameIndex: Int) {
        shadowFeature?.recordCommands(commandBuffer, frameIndex)
    }

    fun destroy() {
        renderFeatures.forEach { it.destroy() }
        shadowFeature?.destroy()
        // ... existing depth/framebuffer/etc teardown unchanged
    }
}

/** The only class that bridges [RenderFrameContext] to real `Renderer` internals -- every
 * method here is a one-line delegation, and this is the sole file allowed to reference both
 * [RenderFrameContext] and [Renderer]'s internal fields together. Cheap to allocate once per
 * `recordSharedPassFeatures` call (holds only references, no copying). */
private class RendererFrameContext(
    private val renderer: Renderer,
    override val commandBuffer: Long,
    override val frameIndex: Int,
    override val groupedDrawCalls: Map<RenderPipeline, List<PreparedDrawCall>>,
    override val primaryPipeline: RenderPipeline,
) : RenderFrameContext {
    override val showEnvironment get() = renderer.showEnvironment
    override val uiRuns get() = renderer.uiRuns
    override fun recordDrawCalls(drawCalls: List<PreparedDrawCall>) =
        renderer.run { recordDrawCalls(commandBuffer, drawCalls) }
    override fun ensureUiPipelines(): UiPipelineSet = renderer.ensureUiPipelineSet()
    override fun textureMeshForPrimitive(index: Int) = renderer.textureMeshForPrimitive(index)
}
```

`RendererDraw3D`'s frame sequence becomes: `recordShadowPass(...)` first (unchanged position
from `performShadowPass`'s original call) → begin 3D pass → compute
`groupedDrawCalls`/`primaryPipeline` (unchanged from before) → `recordSharedPassFeatures(...)`
for `Opaque`/`Skybox` → end 3D pass → decide UI pass vs. present-transition pass (unchanged
`uiRenderPipeline != null` check — pass *selection* stays `recordCommandBuffer`'s job, not a
feature's) → begin whichever pass → `recordSharedPassFeatures(...)` for `UI` when applicable
→ end pass.

### 4. `VulkanGameApplication` builds the list once

```kotlin
// VulkanGameApplication.kt, createBackendResources — replaces the pre-refactor
// shadowRenderPipeline = ...; lineRenderPipeline = ...; skyboxRenderPipeline = ...
// sequence of separate field assignments
val shadowFeature = shadowMap?.let { map -> ShadowFeature(map, shadowRenderPipelineFor(map)) }
val renderFeatures: List<RenderFeature> = buildList {
    add(OpaqueRenderFeature(lineRenderPipeline, lineMesh))
    skyboxShaderSet?.let { add(SkyboxRenderFeature(buildSkyboxPipeline(it))) }
    add(UiRenderFeature())
}
val renderer = Renderer(
    graphicsDevice = graphicsDevice,
    swapchainManager = swapchainManager,
    pipelines = pipelineTable,
    renderFeatures = renderFeatures,
    shadowFeature = shadowFeature,
    transferContext = transferContext,
    maxFramesInFlight = MAX_FRAMES_IN_FLIGHT,
)
```

Opt-in-content-vs-capability (`docs/reference/render-extensibility.md`) is preserved exactly:
`shadowFeature`/`SkyboxRenderFeature` only exist when their shader set was supplied;
`OpaqueRenderFeature`/`UiRenderFeature` are always present (capabilities), same as the
pre-refactor non-null `lineRenderPipeline`.

## What DOES change beyond the god-class split

- **Destroy ownership of `lineRenderPipeline`/`skyboxRenderPipeline`/`shadowRenderPipeline`/
  `shadowMap` moves from `VulkanGameApplication.destroyBackend()` into `Renderer.destroy()`**
  — `Renderer` now owns destruction of the list it was handed, since it's the one thing that
  actually knows the list's full membership and order. This was a real, intentional ownership
  change, not preserved behavior. Verified live: a real window-close (not `pkill`) with
  validation layers on showed zero double-destroy errors after implementation.

## What does NOT change

- `PipelineTable`'s per-`VertexFormat` registry stays inside `OpaqueRenderFeature` — it is
  not itself list-managed (skill §1, last bullet).
- `Material`/`RenderPipeline` split (join key = `VertexFormat`) is untouched — no feature
  gains a `material.pipeline` back-reference (skill §2).
- `drawCalls.groupBy { it.pipeline }` batching stays exactly as-is, computed once in
  `recordCommandBuffer` before `recordSharedPassFeatures` runs; UI pass keeps its deliberate
  non-batched paint order (skill §3).
- `GameApplication`/`Game` mediator wiring (skill §4) is untouched — this refactor is fully
  inside `awake:backend:vulkan`.
- Shadow's frame position (runs before the scene pass, unconditionally) is unchanged from
  before — only its code location and destroy ownership moved, not its position or behavior.

## If a second standalone-pass feature is ever built

Don't generalize `ShadowFeature`'s shape into a shared interface preemptively. When (not if
speculatively) a second feature needs to own its own pass — e.g. a post-process blur reading
the scene's color output into an offscreen target before compositing — that's the point to
look at whether `ShadowFeature` and the new feature share enough real structure to justify
one interface. Until then, two special cases are cheaper to read than one abstraction built
for a shape neither of them actually shares yet.

## Implementation deviations (what actually landed differs from this sketch)

The implementing agent found several places this design sketch didn't survive contact with
the real source. Recorded here rather than silently reconciled, since they're the reason the
follow-up plan doc exists:

1. `ShadowFeature.recordCommands` doesn't take `frameIndex` in practice — the real
   `performShadowPass` took `List<PreparedDrawCall>` and never used a frame index (each draw
   call carries its own). Real signature: `recordCommands(commandBuffer, drawCalls, castFormat)`.
2. Feature registration order is **Skybox → Opaque, not Opaque → Skybox** as sketched above —
   sky must draw before opaque geometry (depth test/write off) or it paints over everything
   already drawn. The sketch's order would have been a real visual regression; caught and
   fixed during implementation, verified live by temporarily forcing `showEnvironment` on.
3. `RenderFeature` gained a third member in the real implementation, `val pass: RenderPassSlot`
   — an enum discriminator, cheaper than the rejected sealed hierarchy, needed because one
   list now serves two invocation points (scene pass, UI pass).
4. Per-frame uniform writes (line/skybox `writeMvp`) moved into each feature's
   `recordCommands`, so `RenderFrameContext` grew a few more read-only fields
   (`viewProjection`, `cameraEye`, `light`, `horizonColor`, `zenithColor`) beyond this
   sketch's set.
5. `Renderer`'s constructor became `internal` — a public constructor can't expose the
   internal `RenderFeature`/`ShadowFeature` types now required to build it.

None of these change the design's core claims (Strategy for the 3 shared-pass features,
Shadow as a deliberate special case, port/adapter over receiver-on-`Renderer`) — they're
real-source corrections to the sketch's mechanics.

## Relationship to ASL

[2026-08-19-asl-single-source-shader-sketch.md](2026-08-19-asl-single-source-shader-sketch.md)
sketches a Kotlin DSL that generates WGSL, feeding the existing naga-based
`syncAwakeShaders`/`validateAwakeShaders` pipeline unchanged. No dependency either direction
between that and this `RenderFeature` refactor — they operate at different layers (ASL: how
shader *source* is authored; this doc: how already-built `RenderPipeline`/`Material` objects
are organized into passes) and can ship in either order or independently.

The one place they'll eventually meet: each `RenderFeature` (`OpaqueRenderFeature`,
`SkyboxRenderFeature`, `ShadowFeature`, `UiRenderFeature`) is constructed from a
`GameShaderSet`/`ShaderPair` today — a resource-path pointer to hand-written or
naga-transpiled shader binaries, not the shader source itself. If ASL ships, it changes
*where the `.wgsl`/`.spv` files a `RenderFeature` points to come from* (generated vs.
hand-written) — it does not change how `RenderFeature` consumes them (`ShaderPair`,
`RenderPipeline` construction, `Material` binding all stay identical either way). Nothing in
this refactor needs to anticipate ASL landing; nothing in ASL needs this refactor to land
first.

## Open questions

1. **Deferred.** Does `awake:backend:webgpu`'s `Renderer` need to implement the same
   `RenderFrameContext` port for parity/reuse? Revisit only after the Vulkan refactor is
   proven (compiles, renders correctly, headless pixel-baselines pass) — not blocking this
   pass. Superseded in practice by the follow-up plan doc's Q2 finding: WebGPU has already
   diverged further (no `RenderFeature`, no shadow pass at all) — the parity question is
   larger than originally scoped.

2. **Resolved.** Checked against the real fields `RendererDraw3D.kt:194-382` reads.
   `uiRenderPipeline`/`uiFramebuffers` are lazily built on the first `drawUi()` call
   (`RendererUiPipelines.kt:25`, `internal var`, not `val`) and rebuilt on swapchain resize
   (`RendererSwapchain.kt:217`); `textureMeshForPrimitive(index)` (`Renderer.kt:331`) is a
   stateful pooled `DynamicMesh` allocator. Neither is a pure per-frame value, so neither can
   live in an immutable snapshot — both are hidden behind `RenderFrameContext` methods
   (`ensureUiPipelines()`, `textureMeshForPrimitive(...)`) instead, backed by the private
   `RendererFrameContext` adapter that is the only file allowed to touch both sides.

3. **Resolved — clean split, no shared surface.** Checked `Material.kt`'s function list
   (`createResources`, `createResourcesFromRenderTarget`, `updateUniformBuffer`, `bind`,
   `asList`) — none depend on `RenderFeature`/`RenderFrameContext`/`Renderer`; every feature
   calls `material.bind(commandBuffer, pipelineLayout, frameIndex, drawSlotIndex)` the same
   way regardless of how features are organized. `Material.kt`'s god-class split is a fully
   independent follow-up — not bundled into this pass, no ordering dependency either way.
