# graphicsLayer rotation/scale tier (2026-08-02)

Design/scoping only -- no fix implemented in this task. Read
`docs/reference/MIRROR_MAP.md`'s `graphicsLayer` section, `modifier/GraphicsLayer.kt`,
`UiDrawPrimitive.kt`'s `scaledByAlpha`, and `UiContextFrameState.kt`'s `pushClip`/`popClip`
before touching anything here. This doc covers the tier alpha compositing deliberately left
out this session (commits `76f10b99`/`35ad0f0d`/`770e78c1`/`10cee8c9`/`e836ed91`).

## Problem statement

`UiModifier.graphicsLayer(effect)` and `UiAlphaEffect` are real (a per-primitive color
pre-multiply applied once at `UiContext`'s emission choke point). Rotation and scale are
`Not implemented` -- no `UiGraphicsEffect` carries a transform, no shader anywhere applies
one. This doc scopes what it would take to add them, and specifically the harder question
alpha never had to answer: what happens to an active clip region when the content under it
is also rotated/scaled.

## Mechanism audit (grounding this session, not theorized)

**Where alpha's cheap path came from.** `UiAlphaEffect(alpha)` works as a CPU-side
post-hoc mutation because alpha is a pure per-channel multiply on data every
`UiDrawPrimitive` already carries (`color.a`) -- `scaledByAlpha(factor)` just returns
`copy(color = ...)`. No vertex position is touched, no shader changed, no backend touched.
That is *why* it was a "no backend changes" win, not because CPU-side effects are cheap in
general.

**Why rotation/scale is not the same shape.** A `UiDrawPrimitive.Quad`/`RoundedQuad`/
`Glyph`/`Texture` stores `x, y, w, h` -- an axis-aligned rect in screen-pixel space, plus
(for `Glyph`) `u0/v0/u1/v1` UVs and (for `RoundedQuad`) a scalar `radius`. There is no
rotation field an axis-aligned rect struct can represent: a rotated quad is not an
axis-aligned `x/y/w/h` rect anymore, full stop. Any implementation, CPU- or GPU-side, must
either (a) turn these primitives into real 4-vertex (or N-vertex) quads with independently
positioned corners, or (b) keep the rect representation and add a separate transform
applied at draw time. This is a strictly bigger structural change than alpha's "multiply an
existing field," which is the root of why this tier is harder in kind, not just in degree.

**What the vertex-shader source actually looks like today** (`ui_quad.vert`, confirmed by
reading the current Vulkan GLSL source): the vertex buffer already carries final
pixel-space positions (`inPosition`), and the shader's only job is `ndc = (inPosition /
screenSize) * 2 - 1`. There is no per-draw transform uniform anywhere in the UI pipelines
today -- the one UBO binding is a shared `screenSize`. This matters for the GPU-side option
below: it is not "add a field to an existing transform uniform," it is "introduce a
transform uniform where none exists," in 4 pipeline types (quad, rounded-quad, glyph,
texture) times 2 backends (Vulkan GLSL/SPIR-V, WebGPU WGSL) = 8 shader/pipeline touch
points, each needing a new uniform/push-constant binding, host-side buffer-write code to
populate it per draw call (or per primitive, depending on batching granularity), and
pipeline-layout changes. Confirmed by listing the actual shader files: `ui_quad.{vert,frag}`,
`ui_rounded_quad.{vert,frag}`, `ui_glyph.{vert,frag}`, `ui_texture.{vert,frag}` exist for
Vulkan (`.spv` compiled alongside), and `ui_quad.wgsl`, `ui_rounded_quad.wgsl`,
`ui_glyph.wgsl`, `ui_texture.wgsl` for WebGPU -- exactly the 8 files this doc's brief
predicted, verified by directory listing, not assumed.

**The clip stack's real shape** (`UiContextFrameState.kt`, confirmed by reading): `pushClip`/
`popClip` maintain a flat `ArrayList<UiBounds>` of already-intersected axis-aligned rects.
`pushClip(rect)` intersects `rect` against `clipStack.lastOrNull() ?: fullFrameRect` and
pushes the intersection; `popClip()` pops and returns the new top (or `fullFrameRect` if
empty). `UiDrawPrimitive.ClipPush(rect)`/`ClipPop(restoreRect)` carry these resolved,
already-intersected rects straight through to the backend, which does nothing but "set
scissor to this rect" -- no backend-side stack awareness, no rotation awareness, nothing
but a literal `vkCmdSetScissor`/`setScissorRect` call. Commit `d30160b2` (this session)
added defensive clamping of that rect to the render-target bounds after nested-scroll
floating-point drift pushed it a few px out of bounds and WebGPU's strict scissor validation
silently dropped the entire frame's command buffer. **This is entirely a rectangle-in,
rectangle-out, scissor-rect model.** There is no code path anywhere in this stack that
represents or draws a non-axis-aligned clip region.

## Question 1: where should the transform be applied

### Option A: CPU-side, at primitive emission (rotate/scale vertex positions before staging)

Mechanically: each `UiDrawPrimitive` would need to grow from an axis-aligned `x/y/w/h` rect
into either (a) 4 explicit corner points, or (b) keep `x/y/w/h` as the "local" rect plus a
separate `rotation: Float, scale: Float, pivot: UiOffset` field that the *backend* still has
to interpret at draw time (not avoiding backend changes after all -- see below). Only
representation (a) actually keeps backends untouched, since a backend that only knows how to
draw an axis-aligned rect from `x/y/w/h` cannot draw a rotated one no matter what extra
scalar fields ride along unless it's taught to read them. So a real "zero backend changes"
CPU-side design requires *every* primitive type to carry per-corner geometry instead of a
rect -- a materially bigger primitive-model change than alpha's single-field multiply, and
it means every existing renderer's vertex-buffer-packing code (which today assumes 4
corners of an axis-aligned rect derivable from `x, y, w, h`) has to change anyway to consume
arbitrary corners. **This mostly cancels out Option A's headline "no backend changes"
selling point** -- alpha got that win because `Color` was already backend-agnostic data every
primitive carried; geometry is not that, the backend's vertex-packing code is intimately
tied to the rect shape today.

Perf: rotating/scaling 4 corners per primitive is ~8 multiply-adds, genuinely cheap in
isolation. But this session's trial-measure investigation
(`docs/tasks/2026-08-02-trial-measure-double-execution.md`) is a live demonstration that
"cheap per-node work" in this codebase can still blow up badly when it repeats at every
primitive, every frame, especially under nested nodes (the Checkout Form page alone emits
1,344-1,480 glyphs/quads per frame `per that doc's own numbers`). A `graphicsLayer` rotation
is typically applied at a subtree root (rotate a card, rotate an icon), not per-leaf-glyph,
so the actual multiplier is "primitives in the rotated subtree," which for something like a
rotating icon is small (a handful of quads/glyphs) but for "fade the whole page transition"
could be the whole tree. Unverified without a real benchmark, but the order of magnitude is
plausible: WORST case is comparable to alpha's own cost (which already touches every
primitive under an active `withGraphicsLayerAlpha` block, and that was accepted as cheap) --
so CPU-side rotation is not obviously worse cost-wise than alpha *for primitives already
under an active graphics layer*, the difference is alpha didn't need new geometry
representation and CPU-side rotation does.

### Option B: GPU-side, a real transform uniform in both backends' vertex shaders

Mechanically: add a per-draw (or per-primitive, if batching allows instance data) transform
uniform -- minimally `(cos θ, sin θ, scaleX, scaleY, pivotX, pivotY)` or a packed 2x3 affine
matrix -- to each of the 8 shader files, thread it through pipeline-layout/descriptor-set
changes in both backends' renderer code, and populate it host-side per draw call. This keeps
`UiDrawPrimitive`'s data shape untouched (no corner-geometry migration) and keeps the CPU
cost at "write a few floats to a uniform buffer per draw," not "recompute 4 corners per
primitive." But it is real, non-trivial backend work in 8 files across two completely
different shader languages and two completely different pipeline/descriptor APIs (Vulkan
descriptor sets + SPIR-V recompilation, WebGPU bind groups + WGSL), and per the shader
audit above there is currently no per-draw transform uniform infrastructure to extend --
this is new plumbing, not a field addition to something that exists.

### Recommendation

**GPU-side (Option B)**, with reservations, for these concrete reasons:

1. CPU-side's "no backend changes" appeal evaporates once you account for the primitive
   geometry migration every backend's vertex-packing code would still need (corners vs.
   rect) -- it is not actually the free lunch alpha was.
2. GPU-side keeps `UiDrawPrimitive` semantically simple (still an axis-aligned rect *in its
   own local space*, transform applied at draw time) which is a much smaller, more
   contained data-model change, and matches how every real GPU UI renderer (including
   Compose's Skia backend, which uses a real transform matrix per layer) actually does this.
3. GPU-side is the only option that can also solve the clip-interaction question cleanly (see
   below) without inventing a second, CPU-side clip-mask mechanism from scratch.

The honest cost of recommendation B is the 8-touch-point shader/pipeline work -- this is the
dominant cost driver of the whole task, not a minor detail. It should not be undersold.

## Question 2: the clip-region interaction

**Confirmed target behavior**: real UI frameworks (Compose's `graphicsLayer` +
`Modifier.clip`, CSS `transform` + `overflow: hidden`/`clip-path`) do transform the clip
region along with rotated/scaled content -- clipping happens in the transformed coordinate
space, i.e. "clip to this rounded rect, then rotate the whole clipped-and-filled result,"
not "clip to this axis-aligned rect in un-rotated screen space, then draw rotated content
into it." Matching this is the right target for Awake too, since it's what a Compose
developer reading `graphicsLayer` docs would expect (this codebase's own stated goal per
`MIRROR_MAP.md`'s intro).

**Why Awake's current clip model cannot represent that today, concretely**: `pushClip`/
`popClip`/`ClipPush`/`ClipPop` are a pure axis-aligned-rectangle scissor-rect stack --
`UiBounds` has no rotation field, `ClipPush(rect: UiBounds)` is drawn via a literal
`vkCmdSetScissor`/WebGPU `setScissorRect` call in both backends, and scissor rects are a
hardware feature that is *inherently* axis-aligned on both Vulkan and WebGPU (and most GPU
APIs) -- there is no "rotated scissor rect" primitive to fall back to. Making a rotated clip
region real requires abandoning scissor-rect clipping for the rotated-subtree case and using
a real stencil buffer or mask (stencil-then-draw, or render-to-texture-then-composite) --
a materially different clipping mechanism, not an extension of the existing one. This is a
second large, separate piece of GPU work on top of the transform-matrix plumbing from
question 1, and it directly touches the exact scissor-rect code path commit `d30160b2` just
patched this session for an unrelated bounds-clamping bug -- any change here must not
reintroduce that class of bug (out-of-bounds/invalid scissor state silently dropping whole
frames on WebGPU's strict validator).

**Recommendation for this tier: explicitly defer the clip-interaction question.** Ship
rotation/scale with clip staying axis-aligned in the pre-transform coordinate space (i.e.
current behavior, unchanged) -- document this plainly as a known, intentional gap rather
than attempting stencil-based rotated clipping in the same pass. This is a defensible
staged scope specifically because: (a) it's the harder of the two problems in this doc by a
wide margin (new clipping mechanism vs. new transform uniform), (b) the common real use
cases for `graphicsLayer` rotation/scale in this codebase's current widget set (icon
spinners, small rotated badges, hover-scale card effects) rarely nest inside an active clip
region in the first place, so the mismatch is low-frequency in practice even though it's a
real semantic gap, and (c) shipping a documented "clip stays axis-aligned for now, matching
Compose here is future work" caveat is honest and matches this project's own established
convention for exactly this kind of staged scoping (see `MIRROR_MAP.md`'s own tone for
partially-real features).

## Recommended overall approach and honest size/risk estimate

**Recommended approach**: GPU-side transform uniform (Option B), rotation and scale only
(no shadow/elevation, no clip transform), threaded through all 8 shader/pipeline touch
points, with clip staying axis-aligned (explicitly documented gap, not attempted this pass).

**This is a large task, not a small one -- say so plainly.** Concrete size drivers:

- 8 shader files need a new uniform/push-constant/bind-group binding added and their vertex
  math changed to apply it (4 pipeline types x 2 backends, each backend using a completely
  different shader language and resource-binding API).
- Both backends' host-side renderer code (`RendererDraw3D.kt`-equivalent for Vulkan,
  `Renderer.kt`-equivalent for WebGPU -- the same files `d30160b2` touched for scissor
  clamping) need new code to populate and bind the transform data per UI draw call, which
  interacts with however UI primitives are currently batched (if primitives of the same
  type are batched into one draw call today, per-primitive transforms may force
  un-batching or a move to instance data -- this needs its own investigation before
  implementation, not assumed away here).
- `ui-core`'s `UiGraphicsEffect`/`UiDrawPrimitive` model needs a new
  `UiTransformEffect(rotationDegrees, scaleX, scaleY, pivot)` (or similar) threaded through
  the same `UiContext` emission choke point `UiAlphaEffect` uses, but unlike alpha (a color
  multiply resolvable entirely in `ui-core`), this data has to survive all the way into the
  backend's draw call -- meaning `UiDrawPrimitive` itself likely needs a new optional
  transform field on every geometry-carrying variant, a real, if mechanical, per-case change
  across `Quad`/`RoundedQuad`/`Glyph`/`Texture`/`FilledPath`/`StrokedPath`.
- Correctness verification needs new snapshot/visual coverage (rotated/scaled primitives are
  inherently visual, per this repo's own "prove UI changes through snapshot docs" rule) in
  both backends, since Vulkan and WebGPU shader math is independently hand-written and can
  independently drift (this session's own "Vulkan's Y-down NDC needs no flip, WebGPU's does"
  comment in `ui_quad.vert` is a concrete precedent for backend-specific sign/axis bugs in
  exactly this kind of shader math).

Estimated risk: medium-high. Not because the math is hard (2D rotation/scale is standard),
but because of the touch-point count, the two-independent-shader-language duplication risk,
and the batching-interaction unknown flagged above that needs its own investigation pass
before implementation can even be estimated precisely.

## Smaller first step, if the full scope above is too large for one pass

Two independently-shippable narrowings, either is defensible:

1. **Scale-only, no rotation.** A uniform scale (or non-uniform scaleX/scaleY) around a
   pivot is representable as a single `vec2 scale` (+ pivot) uniform and is simpler shader
   math than a full rotation matrix (no trig, no `cos`/`sin` uniform pair) -- still touches
   all 8 files, still needs the same `UiDrawPrimitive` field threading, so it does not
   reduce the touch-point count, but it does reduce the shader math risk and gives a real,
   shippable "hover-scale a card/button" effect (a common real UI need) without rotation's
   additional axis-alignment questions (e.g. does a rotated glyph's UV sampling still need
   anything special -- it doesn't, but that's one fewer thing to verify).
2. **Rotation-only, no scale.** Slightly more shader math (rotation matrix) but conceptually
   simpler for the clip-interaction deferral argument above, since "the icon still occupies
   roughly the same footprint, just spun" is a more obviously-safe case to leave
   clip-unaware than a scale that could make content visually overflow its original,
   still-axis-aligned clip rect in an obviously-wrong-looking way.

Recommend **scale-only** as the first step if forced to pick one: it is the more commonly
requested real effect (hover/press affordances) in typical UI kits, and its clip-overflow
failure mode (scaled content visibly poking outside an unrotated clip rect) is easier for a
developer to reason about and work around at call sites than rotation's less intuitive
axis-aligned-clip-of-rotated-content mismatch. Either narrowing still requires all 8
shader/pipeline touch points -- there is no meaningfully smaller slice than "touch every UI
pipeline in both backends," since every pipeline type can, in principle, appear inside a
`graphicsLayer`-wrapped subtree. The only real scope reduction available is *which*
transform components apply, not *how many backend files* need touching.

## Status

- [x] Confirmed alpha's CPU-side cheapness came from a property (`Color`) every primitive
      already carried in backend-neutral form, not from CPU-side effects being cheap in
      general -- rotation/scale changes geometry, which is structurally different.
- [x] Read the actual current vertex shader source (`ui_quad.vert`) and confirmed there is
      no existing per-draw transform uniform to extend -- this is new plumbing.
- [x] Enumerated the real 8 shader/pipeline touch points by listing actual files, not
      assuming the brief's count.
- [x] Read `UiContextFrameState.kt`'s `pushClip`/`popClip` and confirmed the clip stack is a
      pure axis-aligned scissor-rect model with no rotation representation, and that
      `d30160b2` (this session) already had to patch this exact mechanism for an unrelated
      bounds-clamping bug -- any rotation-aware clip design is a second, separate
      stencil/mask mechanism, not an extension of scissor rects.
- [x] Confirmed real frameworks (Compose, CSS) transform clip with content, and recommended
      matching that eventually while explicitly deferring it for this tier's first
      implementation pass, with the axis-aligned-clip-stays-as-is caveat documented as
      intentional, not an oversight.
- [x] Landed on GPU-side transform uniform as the recommended approach over CPU-side
      vertex mutation, with the specific reasoning for why CPU-side's apparent "no backend
      changes" win does not actually hold for geometry (unlike alpha).
- [x] Gave an honest medium-high risk / large size estimate, explicitly not undersold, with
      concrete size drivers (8 touch points, 2 shader languages, batching-interaction
      unknown, per-backend visual-drift risk, new snapshot coverage needed).
- [x] Proposed two independently-shippable smaller-first-step narrowings (scale-only,
      rotation-only) with a concrete recommendation (scale-only) and an honest caveat that
      neither reduces the touch-point count, only the shader-math/clip-reasoning risk.
- [ ] Not started: any implementation. This task is design/scoping only.
- [ ] Not started: the batching-interaction investigation flagged above (whether UI
      primitives are currently batched per-type across multiple primitives in one draw
      call, which would force either un-batching or instance-data design for a per-primitive
      transform) -- needed before an implementation task can size the backend work
      precisely, deliberately not resolved in this doc.
