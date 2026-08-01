# Render Backend Audit: Vulkan vs. WebGPU

Audits `awake/backend/vulkan`'s `Renderer.kt` against `awake/backend/webgpu`'s `Renderer.kt`
for correctness and cross-backend consistency, using the same direct-source-read discipline
as `docs/reference/MIRROR_MAP.md` (that doc audits `ui-core`'s DSL against Jetpack Compose --
a different concern; this doc is scoped to the two render backends' own behavior and is
intentionally kept separate from it, per explicit instruction). Every row below is backed by
a direct read of:

- `awake/backend/vulkan/src/commonMain/kotlin/io/github/ronjunevaldoz/awake/vulkan/renderer/Renderer.kt`
- `awake/backend/webgpu/src/wasmJsMain/kotlin/io/github/ronjunevaldoz/awake/webgpu/renderer/Renderer.kt`
- `awake/backend/vulkan/src/commonMain/kotlin/io/github/ronjunevaldoz/awake/vulkan/ui/UiTextureRenderPipeline.kt`
- `awake/backend/webgpu/src/wasmJsMain/kotlin/io/github/ronjunevaldoz/awake/webgpu/ui/UiTextureRenderPipeline.kt`
- `awake/backend/webgpu/src/wasmJsMain/kotlin/io/github/ronjunevaldoz/awake/webgpu/material/Material.kt`
- `awake/backend/vulkan/src/commonMain/kotlin/io/github/ronjunevaldoz/awake/vulkan/material/Material.kt`
- `awake/engine/ui/ui-core/src/commonMain/kotlin/io/github/ronjunevaldoz/awake/ui/UiDrawPrimitive.kt`
- `awake/engine/ui/ui-core/src/commonMain/kotlin/io/github/ronjunevaldoz/awake/ui/layout/UiBounds.kt`

not memory of either graphics API or of this codebase from an earlier session.

## Real finding worth flagging (not a hidden bug -- but a real, common-case rendering gap)

**WebGPU silently drops corner rounding on `RoundedQuad` primitives outside an active
convex-path clip.** In WebGPU's `Renderer.drawUi()` (`renderer/Renderer.kt:383-398`), the
`UiDrawPrimitive.RoundedQuad` branch only produces an actually-rounded shape
(`stageRoundedQuadFillRun`, which tessellates a real `RoundedRectangle` path) when
`canExactClip(activePathClips)` is true -- i.e. only when the widget happens to be nested
inside an active `ClipPathPush`-established convex path clip. In the far more common case (no
active path clip, which is the normal state for e.g. a plain `shadcnButton`/`shadcnCard` not
nested inside a scroll/mask region), it falls through to:

```kotlin
stageQuadRun(mesh, roundedSlice.map { UiDrawPrimitive.Quad(it.x, it.y, it.w, it.h, it.color) })
```

which drops `radius` entirely and draws a flat square. This is a real, in-code-acknowledged
limitation (comment at that call site: `"No rounded-corner shader support yet ... fall back
to drawing it as a flat Quad, dropping radius"`), not something masked or silently introduced
by the audited commit -- but it means **the large majority of rounded-corner widgets in the
WebGPU backend currently render as square corners**, since most rounded UI elements are not
inside a path-clip region. Vulkan has no such gap: it has a dedicated
`UiRoundedQuadRenderPipeline` (`ui/UiRoundedQuadRenderPipeline.kt`, an SDF-based
rounded-corner fragment shader) for the non-clipped case, and the same
`stageRoundedQuadFillRun` exact-tessellation path for the clipped case, so Vulkan renders
correct rounded corners in both branches. This is a cross-backend visual-parity gap, not a
crash or leak -- flagging per this audit's scope, not fixing.

## Commit `22d72f33`'s `Renderer.kt` edits: verified mechanical rename, not a masked change

`git show 22d72f33 -- <path>` for both files shows an identical 3-hunk diff per file:

1. Import: `io.github.ronjunevaldoz.awake.ui.scope.UiSlot` -> `io.github.ronjunevaldoz.awake.ui.layout.UiBounds`
2. `UiRun.ClipRun(val rect: UiSlot)` -> `UiRun.ClipRun(val rect: UiBounds)`
3. `UiShapeSpec.RoundedRectangle(quad.radius.px).toPath(UiSlot(quad.x, quad.y, quad.w, quad.h))` -> `...toPath(UiBounds(quad.x, quad.y, quad.w, quad.h))`

Confirmed genuinely behavior-neutral:

- `UiBounds` (`ui-core/layout/UiBounds.kt`) is `data class UiBounds(val x: Float, val y: Float, val width: Float, val height: Float)` -- same 4-field shape every `ClipRun.rect`/`RoundedRectangle.toPath(...)` call site in both `Renderer.kt` files already reads (`.x`, `.y`, `.width`, `.height` in the scissor-rect and rounded-quad-path construction code), unchanged before/after.
- `grep -rn "UiSlot"` across the whole repo (excluding `build/`) turns up zero remaining real type references anywhere, including both `Renderer.kt` files -- the 4 remaining hits (`AbsoluteScope.kt`, `UiAnchor.kt`, `UiAnchor.kt` x2 inside a commented-out block, `GameUiRuntime.kt`) are all stale comments/doc strings, not live code, confirming the type merge is complete and this isn't a case of two parallel types now silently diverging.
- Both files' `import` lists show no other changed lines in this commit -- the diff is exactly the 3 hunks above, nothing else moved.

Verdict: **genuinely a mechanical rename**, safe as characterized in the task prompt.

## Cross-backend `UiDrawPrimitive` handling

`UiDrawPrimitive` (`ui-core/UiDrawPrimitive.kt`) is a `sealed class` with 10 concrete
subtypes: `Quad`, `GradientQuad`, `RoundedQuad`, `Glyph`, `FilledPath`, `StrokedPath`,
`Texture`, `ClipPathPush`, `ClipPush`, `ClipPop`. Both `Renderer.drawUi()`'s `when (first)`
blocks are non-`else` exhaustive `when`s over this sealed class (Kotlin's compiler enforces
completeness for a `when` over a sealed type with no `else` branch), so neither backend can
have a silently-unhandled variant fall through undetected -- a new subtype added to
`UiDrawPrimitive` without updating both `Renderer.kt`s would fail to compile, not silently
no-op. That said, "compiles" and "renders the same way" are different guarantees:

| Primitive | Vulkan | WebGPU | Status | Detail |
|---|---|---|---|---|
| `Quad` | `stageQuadRun` | `stageQuadRun` | Faithful | Byte-for-byte identical implementation in both files (same vertex/index layout, same exact-clip branch). |
| `GradientQuad` | `stageGradientQuadRun` | `stageGradientQuadRun` | Faithful | Identical implementation; per-corner gradient colors written the same way in both. |
| `RoundedQuad` | Exact-clip fill (`stageRoundedQuadFillRun`, real tessellated rounded path) when clipped, dedicated SDF `UiRoundedQuadRenderPipeline` (real rounded corners) when not | Exact-clip fill (`stageRoundedQuadFillRun`, real tessellated rounded path) when clipped, flat `Quad` fallback (radius dropped, square corners) when not | **Diverges (real gap)** | See "Real finding" section above -- WebGPU has no equivalent of Vulkan's SDF rounded-quad pipeline for the non-clipped case, so most rounded corners render square on WebGPU today. |
| `FilledPath` | `stageFilledPathRun` (tessellate + exact clip) | `stageFilledPathRun` | Faithful | Identical. |
| `StrokedPath` | `stageStrokedPathRun` | `stageStrokedPathRun` | Faithful | Identical. |
| `Glyph` | `stageGlyphRun`, chunked into `MAX_UI_QUADS`-sized sub-runs (`while (chunkStart < glyphSlice.size)` loop in `drawUi()`) | `stageGlyphRun`, NOT chunked -- the whole same-type glyph slice is staged into a single run/mesh in one call | Diverges (capacity edge case) | Vulkan explicitly splits a same-type glyph run larger than `MAX_UI_QUADS` (256) into multiple pooled meshes so a long text label list doesn't hit the `require(glyphs.size <= MAX_UI_QUADS)` guard inside `stageGlyphRun`. WebGPU's `drawUi()` glyph branch (`renderer/Renderer.kt:413-419`) calls `stageGlyphRun` once on the full slice with no chunking -- if a single contiguous glyph run in one frame exceeds 256 glyphs, WebGPU's `require()` throws where Vulkan would have silently split and kept rendering. This is a real latent crash-on-large-text-run gap on WebGPU that Vulkan doesn't have, worth a follow-up (not fixed here per this audit's scope). |
| `Texture` | `stageTextureRun`, one draw call per primitive, rewrites one shared descriptor set per material (`bindMaterial`) | `stageTextureRun`, one draw call per primitive, per-material cached `GPUBindGroup` (`bindGroupFor`) | Faithful (different mechanism, same result) | Both backends' comments explicitly document *why* the mechanism differs (Vulkan's single rewritten descriptor set is safe under this codebase's full per-frame serialization; WebGPU's `GPUBindGroup`s are immutable once created, hence the per-material cache) -- not an oversight, a real API-shape difference with equivalent visual output. |
| `ClipPathPush` | Pushes `ClipKind.Path`, records path in `activePathClips`, emits `ClipRun(it.boundsRect)` | Same | Faithful | Identical logic, including the same-shape `clipKindStack`/`activePathClips` bookkeeping. |
| `ClipPush` | Pushes `ClipKind.Rect`, emits `ClipRun(it.rect)` | Same | Faithful | Identical. |
| `ClipPop` | Pops `clipKindStack`, conditionally pops `activePathClips`, emits `ClipRun(it.restoreRect)` | Same | Faithful | Identical, including the `ClipKind.Rect, null -> Unit` no-op branch for an already-empty stack. |

## GPU resource lifetime

No new leak pattern found in either `Renderer.kt`. Specifically checked:

- **Per-frame allocation without a release path**: neither backend allocates a *new* buffer,
  texture, or pipeline per frame inside `draw()`/`drawUi()`. Both use pre-sized pooled
  `DynamicMesh`es (`uiQuadMeshPool`/`uiGlyphMeshPool`/[Vulkan only] `uiRoundedQuadMeshPool`),
  grown once (never shrunk) as needed and reused every frame -- `mesh.update(vertices,
  indices)` rewrites an existing GPU buffer's contents rather than allocating a new one.
- **`destroy()` symmetry**: Vulkan's `Renderer.destroy()` (`renderer/Renderer.kt:1551-1578`)
  tears down every pooled mesh (`uiQuadMeshPool`, `uiGlyphMeshPool`, `uiRoundedQuadMeshPool`,
  `lineMesh`), every lazily-built pipeline (`uiRenderPipeline`, `uiGlyphRenderPipeline`,
  `offscreenGlyphRenderPipeline`, `uiTextureRenderPipeline`, `uiRoundedQuadRenderPipeline`),
  `textureQuadMesh`, `fontTexture`, every `createdTextures`/`createdRenderTargets` entry, the
  offscreen fence, framebuffers, present-transition resources, and the depth image/view/memory
  triad -- matches every corresponding `create*`/lazy-build call site 1:1. WebGPU's
  `Renderer.destroy()` (`renderer/Renderer.kt:924-936`) similarly tears down `uniformBuffer`,
  every lazily-built pipeline, `createdRenderTargets`, both mesh pools, `textureQuadMesh`, and
  `lineMesh`.
- **`Material` lifetime is deliberately NOT the `Renderer`'s job on either backend** (confirmed
  via `RendererHeadlessPixelBaselineTest.kt`'s own comment: `"demo/game owns
  createMesh()/createMaterial()'s destroy() calls (Renderer doesn't...)"`, and
  `SceneAssetLibrary.dispose()` being the actual caller of `material.destroy()` across all
  cached materials) -- this is a documented, symmetric ownership split, not an asymmetry:
  `Renderer.createMaterial()` returns an owned handle to the caller, who is responsible for
  its `destroy()`, same as `createMesh()`. Vulkan's `Material.destroy()` does correctly free
  its uniform buffer, its memory, its descriptor pool, and its descriptor set layout when the
  caller does call it.
- **Minor, non-leak note**: WebGPU's `UiTextureRenderPipeline.bindGroups` (a
  `HashMap<Material, GPUBindGroup>`, `ui/UiTextureRenderPipeline.kt:52`) is never cleared, even
  in `destroy()`, and grows one entry per distinct `Material` ever passed through a `Texture`
  primitive over the pipeline's lifetime. Not a real leak in WebGPU terms (`GPUBindGroup` has
  no native handle to explicitly release; the WebGPU spec relies on GC once unreferenced), but
  if a game creates many short-lived render-target materials for `Texture` primitives (e.g. a
  churn of temporary preview materials), this map holds a `GPUBindGroup` (and therefore keeps
  the entry's `Material`/its `previewTextureView` reachable) for the pipeline's entire
  lifetime rather than the material's. Worth a follow-up if that usage pattern becomes real;
  not urgent today since the documented use case ("a minimap/portal material is typically
  created once and reused") doesn't hit this.

## Other notes (real correctness signal, not style)

- **`flipYForClipSpace` correctly differs and is correctly consumed elsewhere**: Vulkan's
  `Renderer` sets `override val flipYForClipSpace: Boolean = true` (Vulkan's NDC is +Y down);
  WebGPU's sets it to `false`, with an inline comment citing the exact convention difference.
  Both are read by `RenderRenderer`-consuming code outside these two files, not by the two
  `Renderer.kt`s themselves -- correctly scoped as a per-backend fact the abstraction exposes,
  not something either `Renderer` acts on internally.
- **Debug-line depth testing genuinely differs, and is documented as such, not accidental**:
  Vulkan's debug lines share the same render pass/depth attachment as the 3D draw calls
  (`recordCommandBuffer`'s `lineRenderPipeline.bind()` call happens *before*
  `vkCmdEndRenderPass`, inside the depth-tested pass). WebGPU's debug lines are drawn in the
  same render pass as the 3D draw calls too, but that pass has no depth attachment at all on
  this backend (`draw()`'s `RenderPassDescriptor` for the 3D pass has no
  `depthStencilAttachment` field set at all, unlike `renderToTexture()`'s offscreen pass, which
  does) -- the code comment at that exact call site (`renderer/Renderer.kt:821-825`) explicitly
  flags this as a pre-existing, not-new gap ("this doesn't depth-test against scene geometry
  ... matches this pass's existing (pre-existing, not new) lack of depth testing"). Confirmed
  accurate as of this read; not something this audit is the first to notice, but worth keeping
  on record here since it's a real render-correctness divergence between backends (a
  frustum-wireframe debug line drawn behind opaque geometry would incorrectly show through on
  WebGPU's swapchain pass, not on Vulkan's).
- **WebGPU's single shared uniform buffer for MVP matrices is a documented single-draw-call
  limitation, not a bug missed by this audit**: the class doc comment at the top of WebGPU's
  `Renderer` explicitly states multiple draw calls sharing the one uniform buffer within a
  render pass "would clobber each other's MVP matrix" since `queue.writeBuffer` doesn't
  interleave mid-encoder, and marks this as scoped-out until `Material` becomes real. Confirmed
  still true as of this read (`draw()`'s inner draw-call loop writes to the same
  `uniformBuffer`/`uniformBindGroup` pair every iteration, no per-draw-call buffer). Not new,
  not masked -- just re-confirmed live and worth keeping visible here since it's the kind of
  thing a reader skimming only the 3D draw-call path (not this comment) could miss.

## Tally

- **Commit `22d72f33`**: mechanical rename, verified behavior-neutral in both files.
- **`UiDrawPrimitive` cross-backend coverage**: 8 of 10 variants faithful/equivalent, 1 real
  visual-parity gap (`RoundedQuad` corner rounding missing outside path-clip on WebGPU), 1 real
  latent-crash gap (`Glyph` run chunking missing on WebGPU, `>256` glyphs in one contiguous run
  throws instead of splitting).
- **GPU resource lifetime**: no leaks found; create/destroy symmetry holds on both backends;
  one minor non-leak note (WebGPU's per-material bind-group cache is never evicted).
- **Other**: two backend behavior differences (debug-line depth testing, single-draw-call MVP
  uniform buffer limit) that are pre-existing and already documented in-code, re-confirmed
  accurate as of this read, not newly discovered bugs.

`docs/reference/MIRROR_MAP.md` was read for tone/structure reference only and was not
modified by this audit.
