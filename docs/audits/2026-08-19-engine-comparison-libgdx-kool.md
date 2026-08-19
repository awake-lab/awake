# Awake vs libGDX, Kool, Bevy, Godot, Unity, Unreal — architecture comparison

Status: research summary, not a design doc. Sourced from public docs/READMEs for every
non-Awake engine — treat as directionally accurate, not source-verified the way this
session's Awake-side findings are (those cite real `file:line`).

## Matrix

| Engine | Language | Shader authoring | Render pass architecture | App/lifecycle pattern | Threading | Scene/entity model | Vulkan |
|---|---|---|---|---|---|---|---|
| **Awake** | Kotlin (KMP) | Hand-written GLSL+WGSL per backend, per shader (`GameShaderSet.vulkan`/`.webGpu`) | Ordered `RenderFeature` list (planned), 3-4 passes | Constructor-injected Mediator (`GameApplication`/`Game`/`Renderer`) | Single-threaded frame loop (deliberate) | ECS (`awake:ecs`) | Yes |
| **libGDX** | Java | Hand-written GLSL per platform | None formalized — direct GL calls | Static globals (`Gdx.graphics`/`Gdx.input`/...) + `ApplicationListener` | Single-threaded | Scene2D (optional, thin) | **No** |
| **Kool** | Kotlin (KMP) | **KSL** — one Kotlin DSL generates GLSL+WGSL | Passes exist (deferred, SSR) but structure undocumented publicly | Undocumented publicly | Decoupled game-logic/render threads | Scene graph | Yes |
| **Bevy** | Rust | WGSL only (via wgpu) | **Explicit `RenderGraph`**, dependency-based, dual-World Extract stage | Plugin-based `App` builder | Pipelined — render world computed one frame behind main world | **ECS-native** (this *is* the engine) | Yes (via wgpu) |
| **Godot 4** | C++/GDScript/C# | Own shading language, transpiled internally | Backend-specific (`RendererCompositorRD` for Vulkan/D3D12/Metal) | `RenderingServer` — full client/server split, backend-agnostic API | Optional dedicated render thread | Node/scene tree (not ECS) | Yes |
| **Unity** | C# | HLSL (+ visual Shader Graph) | **`RenderGraph` API** (URP, default since 6000.3) — auto pass-merge via texture-usage analysis | `RenderPipeline`/`ScriptableRenderPass`, C# scripted | Engine-managed, not exposed | GameObject/Component (ECS/DOTS optional) | Yes |
| **Unreal** | C++ | HLSL, cross-compiled per platform via RHI | **RDG** (Render Dependency Graph) — setup/compile/execute, auto barriers+aliasing | `RHI` abstracts D3D/Vulkan/Metal; engine-managed app shell | Game thread + render thread + RHI thread | Actor/Component | Yes |

## Reading the matrix

- **Shader authoring:** Awake and libGDX are the only two hand-authoring per-backend source.
  Kool (KSL), Bevy (WGSL-only via wgpu), Godot (own language), Unity (HLSL), Unreal (HLSL)
  all use one source of truth per shader. **This is Awake's clearest, most isolated gap.**
- **Render pass architecture:** Bevy, Unity, Unreal all converged on an explicit dependency
  graph (`RenderGraph`/RDG) with automatic barrier/lifetime management — industry-standard
  since ~2021 per Unreal's own docs. Awake's planned ordered list (this session's
  `RenderFeature` draft) is the right size for today's pass count, not this shape yet.
- **App/lifecycle pattern:** Awake's constructor-injected Mediator is closest to Godot's
  server/client split in spirit (explicit, no hidden statics) and strictly better than
  libGDX's global-singleton `Gdx.*` approach.
- **ECS:** Bevy *is* an ECS with a renderer built on top, closest architectural sibling to
  Awake's own `awake:ecs` + scene runtime. Worth reading Bevy's Extract-stage pattern (copy
  only what the renderer needs from the main `World` into a separate render `World` each
  frame) if Awake's ECS-to-renderer boundary ever needs hardening — not proposed here, just
  noted as the one engine whose scene model rhymes with Awake's.
- **Vulkan:** Only libGDX lacks it. Not a differentiator among the rest.

## What we could improve (real, actionable)

1. **Shader duplication** — the one gap every modern comparison engine has already solved
   differently (KSL, WGSL-only, own language, HLSL). A Kotlin shader DSL generating
   SPIR-V/WGSL from one definition would close this. Compiler-shaped work, not a refactor —
   deserves its own scoped design pass with `awake-render-backend-engineer` (and
   `awake-asset-pipeline-engineer` for the shared uniform-layout contract side). Not started
   here.
2. **Render pass scaling ceiling** — today's planned ordered `RenderFeature` list won't scale
   to Bevy/Unity/Unreal's pass count without becoming a real dependency graph. Not needed at
   Awake's current scope (3-4 passes) — noted so it isn't a surprise later, not a call to
   build a render graph preemptively (same YAGNI reasoning as the `RenderFeature`
   sealed-hierarchy rollback earlier this session).

## What's already planned (this session's drafts — no new work implied here)

- **`RenderFeature` Strategy + `RenderFrameContext` port** —
  [2026-08-19-render-feature-strategy-plan.md](2026-08-19-render-feature-strategy-plan.md)
- **`GameShaderSetSpec` open registry** —
  [2026-08-19-application-layer-shape-options.md](2026-08-19-application-layer-shape-options.md)
- **`Game`/`GameApplication`/`GameShaderSet` → `AppBehavior`/`AppRuntime`/`ShaderSet` naming** —
  [2026-08-19-game-naming-generalization-plan.md](2026-08-19-game-naming-generalization-plan.md)
- **`awake-render-pipeline` skill** —
  [skills/awake-render-pipeline/SKILL.md](../../skills/awake-render-pipeline/SKILL.md)

## Caveat

Every non-Awake row is sourced from public docs/READMEs, not source-level review — treat as
directionally reliable, not load-bearing for an implementation decision without checking the
actual source first if any of this becomes real work.
