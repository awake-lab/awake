# Vulkan god-class de-bloat + Vulkan/WebGPU shared-logic extraction — plan

Status: draft, not implemented. Follow-up to
[2026-08-19-render-feature-strategy-plan.md](2026-08-19-render-feature-strategy-plan.md), whose
implementation surfaced two real findings this doc scopes: `Renderer.kt` grew instead of
shrinking, and the two backends have already diverged with ~400+ lines of duplicated
pure-CPU logic between them (some already drifted). Findings below are read-verified with
real `file:line` references, not estimated.

## Part A — `Renderer.kt` actually got bigger (702→750 lines, 19→23 functions)

### Root cause

The `RenderFeature` refactor extracted 4 pass bodies (~214 lines) out of `RendererDraw3D.kt`,
not `Renderer.kt` — `Renderer.kt` was never where pass-recording code lived; it was already
702 lines of *state and lifecycle*. Meanwhile the refactor added 3 new members directly to
the `Renderer` class body instead of following the class's own documented convention
(`Renderer.kt:96-100`: "the rest of its behavior lives in sibling files as `internal`
extension functions on `Renderer`"):

| New member | Location | Why it landed as a member, not a sibling extension |
|---|---|---|
| `recordSharedPassFeatures` | `Renderer.kt:664-671` | Touches `renderFeatures`, a `private val` |
| `recordShadowPass` | `Renderer.kt:678-684` | Touches `shadowFeature`, a `private val` |
| `waitIdle()` | `Renderer.kt:686-688` | Interface override — must be a member, correctly |

The only thing forcing the first two to stay members is field privacy — `renderFeatures`/
`shadowFeature` are `private val`, while every other field a sibling extension touches is
already `internal` for exactly this reason (`Renderer.kt:97-100` documents that trade
explicitly). This is a one-line-per-field, behavior-free fix.

### Step 1 — move the 2 dispatch functions (mechanical, do first)

```kotlin
// Renderer.kt: change visibility only
private val renderFeatures: List<RenderFeature>,     // becomes: internal val renderFeatures
private val shadowFeature: ShadowFeature? = null,     // becomes: internal val shadowFeature

// RendererDraw3D.kt: move both functions here verbatim, as internal fun Renderer.xxx(...),
// matching every other function already in this file
internal fun Renderer.recordSharedPassFeatures(context: RenderFrameContext) {
    renderFeatures.forEach { it.recordCommands(context) }
}

internal fun Renderer.recordShadowPass(drawCalls: List<PreparedDrawCall>) {
    val feature = shadowFeature ?: return
    if (!shadowsEnabled) return
    runOffscreenCommands { commandBuffer ->
        feature.recordCommands(commandBuffer, drawCalls, renderPipeline.vertexFormat)
    }
}
```

Both call sites (`RendererDraw3D.kt:261`, `:283`, `:88`) are already in the same file, so
this is a pure cut-paste + visibility flip. Saves ~26 lines. `waitIdle()` stays — it's a
correct interface override, not a candidate for extraction.

### Step 2 — extract the offscreen-frame path (biggest single win)

`renderToTexture` (`Renderer.kt:509-585`, 77 lines) does its *own* inline command recording —
`vkCmdBeginRenderPass`/`vkCmdSetViewport`/`groupBy { it.pipeline }`/`recordDrawCalls`/
`vkCmdEndRenderPass` — the exact class of work the `RenderFeature` refactor claimed to move
out, sitting untouched in the class body. `readPixels` (`Renderer.kt:592-646`, 55 lines) is
staging-buffer + layout-transition plumbing with the same "this is frame-recording behavior,
not state" shape. Both, plus `runOffscreenCommands` (already an extension at
`RendererDraw3D.kt:621`, used only by these two plus `recordShadowPass`), belong together —
they only talk to each other.

```kotlin
// New file: RendererOffscreen.kt, package io.github.ronjunevaldoz.awake.vulkan.renderer
// Move verbatim: Renderer.renderToTexture, Renderer.readPixels, runOffscreenCommands
// (already an extension — just relocate). Same internal-field-access trade as every other
// sibling file; no field needs a new internal flip beyond what recordShadowPass already needs.
internal fun Renderer.renderToTexture(target: RenderTarget, camera: Camera, drawCalls: List<DrawCall>) {
    // body moved verbatim from Renderer.kt:509-585, including its own 16-line comment block
    // explaining why it doesn't run the shadow pass
}

internal fun Renderer.readPixels(target: RenderTarget): ByteArray {
    // body moved verbatim from Renderer.kt:592-646
}
```

Saves ~135 lines.

### Step 3 — extract the buffer/mesh pool factories (optional, only if still over threshold)

Seven near-identical `while (pool.size <= index) pool += X(...)` pool-growth functions:
`quadMeshForRun`, `roundedQuadMeshForRun`, `glyphMeshForRun`, `textureMeshForPrimitive`,
`instanceBufferForRun`, `skinnedInstanceBufferForRun`, `alphaInstanceBufferForRun`,
`frameInstanceBufferForRun` (`Renderer.kt:296-408`, ~113 lines) — same pooling pattern
repeated per resource type, no cross-talk with the rest of the class beyond the pools
themselves.

```kotlin
// New file: RendererBufferPools.kt
// Move the 8 pool-growth functions + their backing MutableList<T> fields verbatim.
```

Saves ~113 lines. Do this only if steps 1+2 (landing at ~615 lines) don't already clear the
audit threshold — a third extraction for its own sake isn't worth the churn if the class
already reads as "fields, constructor, `init`, the 3D resource API, and `destroy`" (what its
own class doc comment already claims it is).

### Expected result

Steps 1+2: ~589 lines, ~20 functions — genuinely just state/lifecycle/resource-API, matching
the class's own doc comment for the first time since this audit started tracking it.

## Part B — Vulkan/WebGPU: not just duplicated, already diverged

WebGPU has no `RenderFeature`, no `RenderPassSlot`, no `RenderFrameContext`, no shadow pass at
all (`webgpu/renderer/Renderer.kt:177-180`: `shadowsEnabled` is stored but never read). Its
whole frame is one 328-line function
(`awake/backend/webgpu/src/wasmJsMain/kotlin/io/github/ronjunevaldoz/awake/webgpu/renderer/RendererDraw3D.kt:41-369`).
Part A's refactor made the two backends *less* alike, and nothing would have caught it.

### What's genuinely backend-specific (don't share)

Command submission model, descriptor/bind-group model, render-pass declaration shape,
WebGPU's mandatory scissor-clamping (its validator rejects out-of-bounds scissors outright;
Vulkan doesn't need this), and `readPixels`' sync-vs-`suspend` split. All confirmed by reading
both backends' `RendererDraw3D.kt` side by side — real API differences, correctly handled
per-backend today.

### What's pure CPU logic, duplicated for no GPU reason — confirmed, not estimated

1. **`tessellateStrokedPath`** — `vulkan/RendererDrawUi.kt:442` vs
   `webgpu/RendererDrawUi.kt:403`. Diffed: **identical line-for-line** except the receiver
   type and a `Color`/`AwakeColor` import alias. Zero GPU types touched — path-clip geometry
   producing a `UiTriangleMesh`.
2. **`stageChunkedColoredTriangleMeshes`/`stageChunkedColoredVertexTriangleMeshes`** — same
   story; the WebGPU copy's own doc comment says "mirrors Vulkan's identical ...".
3. **`pbrTexturedMaterialFloats`, `fogFloats`, the 8-float light block** —
   `vulkan/RendererDraw3D.kt:369,572,585` vs `webgpu/RendererDraw3D.kt:77,396,410`. Pure
   `FloatArray` packing, dictated by the shared `.wgsl` layout, not by the API. **Already
   drifted**: Vulkan's light block writes `shadowTexelDepthScale()` into slot 3, WebGPU
   writes a hardcoded `0f` — a real bug hiding in duplicated code nobody's watching.
4. **The `UiRun` sealed class + primitive→run staging walk** — both backends independently
   declare the same 5-subclass sealed type and switch over the same 11 `UiDrawPrimitive`
   cases in the same order (`vulkan/RendererDrawUi.kt:105-333` vs
   `webgpu/RendererDrawUi.kt:80-309`). ~400 duplicated lines, the biggest single item here.
5. **`RendererVertexWriters.kt`** — 99 vs 98 lines, near-identical, writes floats into a
   `FloatArray`, no GPU type involved.

None of this is documented as an accepted tradeoff — checked `docs/architecture.md`,
`docs/reference/render-extensibility.md`, and this repo's own
`skills/awake-render-pipeline/SKILL.md`; none state WebGPU is expected to mirror
Vulkan's structure or that this duplication is intentional. It's held together by "mirrors
Vulkan's X" doc comments — prose, not a check.

### Recommendation: extract the pure-function tier, not a shared orchestration layer

**Don't** build a shared `RenderFeature`/pass-orchestration abstraction across backends — that
needs a command-buffer/encoder abstraction underneath it first (the genuinely-backend-specific
list above), and WebGPU doesn't even have shadows yet, so there'd be nothing real on one side
to abstract from. That's a much bigger, separate bet.

**Do** move pure functions into the shared render module — no new abstraction, just
relocating code that already doesn't touch backend types. Real precedent already exists and
is already tested: `SkyboxUniforms.kt`
(`awake/engine/render/contract/src/commonMain/kotlin/io/github/ronjunevaldoz/awake/render/renderer/SkyboxUniforms.kt`)
already lives in `render:contract` today, already shared by both backends
(`webgpu/RendererDraw3D.kt:11,72`), already has `SkyboxUniformsTest.kt`. **Naming update**
(see [the common-backend plan](2026-08-19-vulkan-webgpu-common-backend-plan.md)'s "Module
naming" section): `render:contract` is meant to stay pure interface/vocabulary types now that
a real behavior module exists — `SkyboxUniforms.kt` moves to the new
`awake:engine:render:passes` module alongside this section's new files, rather than staying
in `render:contract` where it's the one outlier. Follow its exact shape:

```kotlin
// SkyboxUniforms.kt's actual shape -- the template to copy:
val SkyboxUniformLayout = UniformLayout(/* ... field-for-field, no backend type ... */)

fun skyboxUniformFloats(
    viewProjection: Mat4,
    cameraEye: Vec3,
    sunDirection: Vec3,
    horizonColor: FloatArray,
    zenithColor: FloatArray,
): FloatArray? { /* pure math, returns null on a degenerate input, no GPU call */ }
```

**Sequenced, smallest/safest first:**

1. **Uniform-packing functions** (`pbrTexturedMaterialFloats`, `fogFloats`, the light block) →
   new file in `awake:engine:render:passes`, next to the relocated `SkyboxUniforms.kt`, e.g.
   `MaterialUniforms.kt`/`LightUniforms.kt`.
   `pbrTexturedMaterialFloats(drawCall: DrawCall)` moves completely unchanged — it already
   takes only the shared `DrawCall` type, zero backend coupling. `fogFloats`/the light block
   are currently `private fun Renderer.fogFloats()` extensions on each backend's *own*
   concrete `Renderer`, reading `fogColor`/`fogDensity`/etc. through the implicit receiver.
   **Not** carried over as an extension on the shared `RenderRenderer` interface — that would
   just relocate the same implicit-receiver coupling the render-feature plan's own "Design
   audit" section already rejected for `RenderFrameContext` (a function that quietly reads
   whatever it wants off a stateful interface, instead of declaring its real inputs). Same
   explicit-param shape as `skyboxUniformFloats` instead:
   `fun fogFloats(fogColor: FloatArray, fogDensity: Float): FloatArray`. Every function in
   `render:passes` takes plain explicit params, no receiver, no exceptions — keeps them
   trivially unit-testable and honest about their real inputs. Fixes the shadow-depth-scale
   drift as a side effect of having only one copy. Smallest, safest, fixes a real bug — do
   this first.
2. **`tessellateStrokedPath` + the UI mesh-chunking helpers** → move into `render:passes`
   verbatim (already backend-neutral, already proven identical by the diff). ~150 lines,
   removes a known crash-class risk from drifting silently (the Vulkan copy's own comment
   references a crash class it was fixed against; that history note doesn't exist on the
   WebGPU copy — a fix landing on one side today would not land on the other).
3. **`UiRun` + the primitive→run staging walk** → also `render:passes`, once it exists;
   defer until 1-2 land and prove the module boundary holds. Biggest win (~400 lines) but also
   the biggest job — staging touches each
   backend's own `DynamicMesh` type, so this needs a real
   `stageUiRuns(primitives): List<UiRunSpec>` returning backend-neutral vertex/index
   `FloatArray`s that each backend then uploads through its own mesh type. Real design work,
   not a cut-paste move like 1-2.

### Missing test coverage, either way

None of the duplicated UI-geometry functions have a unit test in either backend module today
— both live in backend-specific `commonMain`/`wasmJsMain` renderer files with no `commonTest`
coverage, which is exactly why the light-block drift went unnoticed. Moving them into
`render:passes` puts them somewhere `commonTest` can actually reach them — arguably a
stronger argument for doing this than the line-count reduction itself.

## Sequencing

Part A and Part B are independent — no ordering dependency, can ship in either order or in
parallel by different agents. Within Part B, step 1 (uniform packing) is the one worth
prioritizing regardless of the rest: it's small, safe, and fixes a real correctness bug
(shadow depth scale) that's live in production code right now, not just a cleanliness issue.
