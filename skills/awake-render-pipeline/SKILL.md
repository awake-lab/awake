---
name: awake-render-pipeline
description: Rules for structuring render features, pipelines, materials and the application bootstrap in Awake's backend renderers (Vulkan today, WebGPU later). Read before adding a new render feature (shadow/opaque/skybox/UI-style pass), before wiring a new RenderPipeline into Renderer, before touching draw-call sorting/batching in RendererDraw3D, or before changing GameApplication/Game wiring. Trigger keywords - RenderFeature, RenderPipeline, Material, PipelineTable, vkCmdBindPipeline, vkCmdBindDescriptorSets, draw call sorting, state batching, recordCommandBuffer, groupBy pipeline, GameApplication, Game.ready, Game.render, mediator.
---

# Render feature / pipeline / material architecture in Awake

Pairs with [render-extensibility.md](../../docs/reference/render-extensibility.md), which
governs *whether* a pipeline is opt-in content vs an always-available capability. This skill
governs *how* render features, pipelines and materials are structured and composed once that
call is made.

Three patterns apply here. Only the first is a gap today — the other two already hold and
must not regress.

## 1. Strategy pattern for render features (target state, not yet in place)

`Renderer` today wires each pass as its own nullable field (`skyboxRenderPipeline`,
`shadowRenderPipeline`, lazily-built UI pipelines) and `RendererDraw3D.recordCommandBuffer`
hardcodes the pass order: skybox, then 3D, then debug lines, then a `when` over UI runs.
Adding a pass means editing the frame-loop function itself.

Target: a `RenderFeature` interface every pass implements, held as an ordered
`List<RenderFeature>` on `Renderer`. Adding a feature (e.g. post-process blur) means adding
a class to that list, not touching `recordCommandBuffer`.

**`RenderFeature` depends on a `RenderFrameContext` port, never on `Renderer` directly.**
An extension function shaped `fun Renderer.recordCommands(...)` (matching the sibling-file
`internal fun Renderer.xxx(...)` convention `RendererDraw3D.kt`/`RendererUiPipelines.kt`
already use) looks natural but isn't real Strategy — every feature would still see the whole
`Renderer` surface, so the god class doesn't actually shrink. Use a narrow interface instead:

```kotlin
interface RenderFrameContext {
    val commandBuffer: Long
    val frameIndex: Int
    val groupedDrawCalls: Map<RenderPipeline, List<PreparedDrawCall>>
    val primaryPipeline: RenderPipeline
    fun recordDrawCalls(drawCalls: List<PreparedDrawCall>)
    // + narrow, purpose-built accessors for anything else a feature needs (e.g. lazily
    // built UI pipelines, a pooled mesh allocator) -- never the whole Renderer.
}

interface RenderFeature {
    fun recordCommands(context: RenderFrameContext)
    fun destroy()
}
```

Only one small adapter class implements `RenderFrameContext` by delegating into `Renderer`'s
real internals; every feature depends on the interface, not that adapter or `Renderer`
itself.

**Shadow is not a `RenderFeature`.** It already owns its own render pass today
(`performShadowPass` is a wholly separate function from `recordCommandBuffer`, since the
shadow map's render pass is not the scene pass), so `RenderFeature` only covers the 3
features that genuinely share a pass (`Opaque`, `Skybox`, `UI`). `ShadowFeature` stays its
own class with its own signature. Don't generalize this into a shared pass-ownership
interface until a second standalone-pass feature (e.g. a future post-process blur) actually
exists — building that abstraction for a hypothetical is speculative generality. See
[docs/audits/2026-08-19-render-feature-strategy-plan.md](../../docs/audits/2026-08-19-render-feature-strategy-plan.md)
for the full worked design, including why the receiver-on-`Renderer` shortcut and an earlier
3-way sealed hierarchy were both rejected.

Rules when doing this refactor:

- Wrap existing pipelines (`ShadowRenderPipeline`, `SkyboxRenderPipeline`, `LineRenderPipeline`,
  the UI pipelines) behind `RenderFeature` implementations — do not merge their internals.
  Each keeps its own `bind()`/`destroy()`; the feature wrapper is what standardizes the
  outward-facing contract.
- The opt-in-content-vs-capability rule from `render-extensibility.md` still applies to each
  feature. A `RenderFeature` for authored content (skybox) is still constructed only when the
  consumer supplies one; it does not become non-null just because it is now list-managed.
- Order in the list is behavior — shadow must run before opaque (opaque samples the shadow
  map), opaque before UI. If a feature depends on another's output, say so at the
  registration site, same convention as ECS system registration order.
- `PipelineTable` (`RendererResources.kt`) stays as the per-`VertexFormat` pipeline registry
  *inside* the opaque/3D `RenderFeature` — it is not itself a list of features, don't conflate
  the two.

## 2. Material is data, Pipeline is shader execution — already correct, keep it that way

`Material` (`material/Material.kt`) owns the descriptor set, uniform buffer and per-frame
uniform slots. It implements the cross-backend `RenderMaterial` interface and knows nothing
about which `RenderPipeline` it will be bound into.

The join key between mesh geometry and pipeline is **`VertexFormat`**, not `Material`.
`DrawCall.mesh.format` resolves which `RenderPipeline` runs; `Material` only supplies the
descriptor set bound into whatever pipeline layout was already chosen. Do not add a
`material.pipeline` back-reference or let `Material` pick its own pipeline — that recouples
what this split deliberately keeps apart, and breaks the case where two materials with
different textures share one pipeline.

## 3. Draw-call batching — pipeline-level sorting exists, descriptor-set churn does not (yet)

`recordCommandBuffer` and `renderToTexture` both do `drawCalls.groupBy { it.pipeline }` before
recording, so `vkCmdBindPipeline` is called once per pipeline per frame, not once per draw
call. Preserve this grouping in any new 3D `RenderFeature` — don't flatten back to
per-draw-call pipeline binds.

Within a pipeline group, `Material` (i.e. descriptor set) is rebound on every draw call —
`vkCmdBindDescriptorSets` churn is not currently minimized. If a future change sorts draw
calls by `Material` inside a pipeline group to cut that churn, it must stay 3D-only:

- **UI draw order is intentionally NOT pipeline/material-sorted.** `RendererDraw3D.kt`
  documents this at the UI dispatch site — UI elements can overlap, and pipeline batching
  would silently reorder paint order. Never apply material/pipeline sorting to the UI
  `RenderFeature`.

## 4. Mediator pattern for the application bootstrap — already correct, keep it that way

`GameApplication` (`engine/game/GameApplication.kt`) is the mediator between three parties
that must never reference each other directly: the platform window
(`WindowApplication`/`create`/`resize`/`dispose` callbacks), the backend `Renderer`
(constructed by each subclass's `createBackendResources`), and the injected `Game` (the
actual scene/gameplay logic, via `Game.ready(renderer)` / `Game.render(delta, w, h)` /
`resize` / `pause` / `resume` / `dispose`).

- `Game` never touches the window or backend GPU types directly — it only ever sees the
  backend-neutral `Renderer` interface handed to it in `ready(renderer)`.
- `VulkanGameApplication`/`WebGpuGameApplication` never know what the game draws — they only
  build GPU resources (`createBackendResources`) and forward lifecycle calls
  (`update`/`resize`/`pause`/`resume`/`dispose`) to `game`. This is the same boundary
  `render-extensibility.md` enforces from the content side: backend subclasses supply
  capabilities/resources, never authored scene content.
- Do not let a backend subclass reach into `Game`'s internals, and do not let `Game`
  construct or hold backend-concrete types (`GraphicsDevice`, `SwapchainManager`,
  `RenderPipeline`) — only the `Renderer` interface crosses that boundary. If a new backend
  capability needs exposing to games, add it to the shared `Renderer` interface
  (`render/renderer/Renderer.kt`), not as a backend-specific escape hatch reached through
  casting.
- One `GameApplication` instance owns exactly one `Game` and one `Renderer` for its whole
  lifecycle — it is a per-session mediator, not a registry. A game that needs multiple
  scenes swaps `Game` implementations or manages that internally; it doesn't ask
  `GameApplication` to hold a list.

## Subsystem / pattern map (naming differs from generic examples — mapped to Awake's real types)

| Layer | Awake class | Pattern | Note |
|---|---|---|---|
| Top orchestrator | `Game` (interface, injected into `GameApplication`) | Strategy | Not Template Method — `Game` is a swapped-in behavior object, not a base class a game subclasses. |
| System lifecycle | `GameApplication` (abstract, `engine/game`) | Template Method + Mediator | `create`/`update`/`resize`/`dispose` are `final`, calling the abstract `createBackendResources`/`destroyBackend` hooks — that's Template Method. It's *also* the Mediator described in §4: the same class keeps window, `Renderer` and `Game` from referencing each other. Both readings are correct, different axes of the same class. |
| Backend construction | `VulkanGameApplication` / `WebGpuGameApplication` | Facade | Each hides `GraphicsDevice`/`SwapchainManager`/pipeline-table construction behind one `createBackendResources` call — this is the Template Method *hook implementation*, not a separate top-level class. |
| Window & OS | `WindowApplication` (`core/graphics`), platform `expect`/`actual` window glue | Bridge | Matches — abstraction (`WindowApplication`) decoupled from per-platform implementation. |
| Engine logic | **Mismatch — no `Scene`/`SceneNode` composite exists.** Awake is ECS-based (`World`, `Entity`, `System`, the `scene { }` DSL from `awake-ecs-authoring`), not a retained scene graph. | N/A | Do not introduce a `SceneNode` Composite/Command layer to match a generic diagram — it would duplicate what `World`/`queryEach`/`System` already do. If scene-graph-shaped structure (parenting, hierarchical transforms) is genuinely needed, that is `Transform.parent` + `TransformSystem`, still queried, not a Command-pattern object. |
| Graphics | `Renderer` (backend-neutral interface) + per-pass `RenderFeature` (target state, §1) | Strategy | Matches — this is the pattern §1 above is closing the gap on. |

## Checklist

- [ ] New render pass implements `RenderFeature`, registered in `Renderer`'s ordered list —
      no new hardcoded call site in `recordCommandBuffer`.
- [ ] Feature ordering documented at the registration site if it depends on another feature's
      output (e.g. reads a texture another feature wrote).
- [ ] Authored-content features stay nullable/opt-in per `render-extensibility.md`; only
      capability features may be non-null.
- [ ] `Material` gains no pipeline back-reference; `VertexFormat` stays the mesh→pipeline key.
- [ ] Any new draw-call sort/batch step preserves the existing `groupBy { pipeline }` and is
      never applied to the UI pass.
