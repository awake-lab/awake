# Render extensibility convention

Awake is a library/framework: a consumer must be able to build their own rendering
content (skybox, debug overlays, post-processing, custom UI) without forking a backend
module. Verified in source, not proposed -- both `Renderer` implementations
(`awake:backend:vulkan`, `awake:backend:webgpu`) already follow one consistent pattern
for this.

## The two shapes

| Shape | Example | Meaning |
|---|---|---|
| Nullable injected pipeline (`X? = null`) | `skyboxRenderPipeline: SkyboxRenderPipeline?`, `wireframeRenderPipeline: RenderPipeline?` | **Optional content.** Off unless the app's own bootstrap builds and passes one. The gated flag (e.g. `showEnvironment`) is a no-op without it. |
| Non-null capability | `lineRenderPipeline: LineRenderPipeline`, `drawDebugLines(...)` | **Always-available mechanism**, not baked-in content. Nobody is forced to call it with real geometry -- the actual gizmo/debug content (e.g. `StudioOrientationGizmo`) lives in `samples:*`, never in a backend module. |

## Rule for new rendering features

- **Authored content** (a specific visual: skybox, a debug overlay, a post-process look)
  -> nullable constructor param, opt-in, off by default.
- **Capability** (a draw primitive any consumer might call with their own content) ->
  can be non-null/always-present, but the backend module must never supply the content
  drawn through it -- that stays at the sample/app layer.

## Where this applies

- `awake:backend:vulkan`, `awake:backend:webgpu` -- `Renderer`'s own constructor, verified
  above.
- `awake:ui:*` -- the same principle, different mechanism: `ui-core`/`ui-headless` supply
  primitives and neutral `Style`, never branded/authored content; `ui-designsystem` owns
  opinionated recipes as an opt-in layer above them, never folded into core. See
  [ui-ownership.md](ui-ownership.md) for the full placement rules -- that doc predates
  this one and is the canonical source for the UI side specifically.
- Any future subsystem exposing a pluggable point (ECS systems, scene loaders) should
  default to the same test: is this *content* (opt-in, nullable/injected) or a
  *capability* (always-available, content-neutral)?

## Known gap

Neither `Renderer` doc-comments this convention explicitly today -- it holds because
every author who added a pipeline param happened to follow it, not because anything
enforces it. No mechanical check exists yet (unlike `verifyUiOwnership` on the UI side).
Worth a `verifyRenderExtensibility`-shaped check if a future addition breaks the pattern.
