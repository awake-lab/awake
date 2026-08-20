# AGENTS.md — Awake

Awake is a Kotlin Multiplatform game engine library and game studio. Use this file as a startup index, not
as the long-form home for project policy.

## Read First

- [docs/architecture.md](/Users/ronvaldoz/StudioProjects/awaken/docs/architecture.md)
- [docs/reference/ai-collaboration.md](/Users/ronvaldoz/StudioProjects/awaken/docs/reference/ai-collaboration.md)
- [docs/reference/agent-catalog.md](/Users/ronvaldoz/StudioProjects/awaken/docs/reference/agent-catalog.md)
- [docs/reference/ui-ownership.md](/Users/ronvaldoz/StudioProjects/awaken/docs/reference/ui-ownership.md)
- [docs/reference/ui-validation.md](/Users/ronvaldoz/StudioProjects/awaken/docs/reference/ui-validation.md)
- [docs/reference/game-structure.md](/Users/ronvaldoz/StudioProjects/awaken/docs/reference/game-structure.md)
- [docs/reference/framework-game-boundary.md](/Users/ronvaldoz/StudioProjects/awaken/docs/reference/framework-game-boundary.md)
- [docs/MVP_PLAN.md](/Users/ronvaldoz/StudioProjects/awaken/docs/MVP_PLAN.md)
- [docs/tasks.md](/Users/ronvaldoz/StudioProjects/awaken/docs/tasks.md)

## Critical Guardrails

- The Android Vulkan sample remains the regression gate for backend work.
- Do not hand-edit generated JNI Accessor/Mutator C++ files; regenerate them.
- Engine modules do not follow the app-style 6-layer clean-architecture split.
- Reusable UI boundaries are canonical in [docs/reference/ui-ownership.md](/Users/ronvaldoz/StudioProjects/awaken/docs/reference/ui-ownership.md).
- Shared UI verification rules are canonical in [docs/reference/ui-validation.md](/Users/ronvaldoz/StudioProjects/awaken/docs/reference/ui-validation.md).

## Mandatory Repo-Local Domain Skills

- `skills/awake-core-math/SKILL.md` — Vector/Matrix mutating-vs-allocating math & camera basis
- `skills/awake-ecs-authoring/SKILL.md` — Component/system creation, pooling, and query rules
- `skills/awake-ecs-scene-runtime/SKILL.md` — Scene runtime and entity hierarchy patterns
- `skills/awake-render-pipeline/SKILL.md` — Strategy RenderFeature & Pipeline/Material separation
- `skills/awake-render-vulkan/SKILL.md` — Vulkan swapchain, resource lifecycle & Android gate
- `skills/awake-render-webgpu/SKILL.md` — WebGPU wgpu4k/Dawn, WASM canvas resize & buffer binding
- `skills/awake-physics-jolt/SKILL.md` — Jolt C++ native bridge, physics contract & collision loop
- `skills/awake-ui-authoring/SKILL.md` — UI 3-layer architecture (`ui-core` vs `ui-headless` vs `ui-designsystem`)
- `skills/awake-ui-shadcn-consuming/SKILL.md` — Consuming Shadcn component recipes
- `skills/awake-ui-shadcn-styling/SKILL.md` — Building & extending Shadcn component recipes
- `skills/awake-ui-icons/SKILL.md` — SVG-to-UiImageVector generation pipeline
- `skills/awake-ui-verification/SKILL.md` — UI visual snapshots and parity test baselines
- `skills/awake-framework-boundary/SKILL.md` — framework-versus-game ownership before promoting sample code or adding MMO-oriented abstractions

## Upstream KMP Skill Routing

| Topic | Skill |
|---|---|
| Publishing to Maven Central | `kotlin-multiplatform-library-publishing` |
| iOS / SPM distribution, MoltenVK framework | `kotlin-multiplatform-xcframework-spm` |
| JNI bridge / C++ marshalling / memory safety | `kotlin-multiplatform-jni-pro` |
| Platform-specific implementations (`expect/actual`) | `kotlin-multiplatform-expect-actual` |
| Toolchain upgrade / KMP migration | `kotlin-multiplatform-migration` |
| Unit / integration tests | `kotlin-multiplatform-unit-testing` |
| Code quality (Detekt, Ktlint) | `kotlin-multiplatform-code-quality` |
| CI automation | `kotlin-multiplatform-ci-github-actions` |
| Desktop windowing / packaging (editor, GLFW host) | `kotlin-multiplatform-desktop-app` |
| Custom drawing / graphics layers (editor viewport) | `kotlin-multiplatform-graphics-modifiers` |
| Release / versioning / changelog | `kotlin-multiplatform-release` |
| Architecture audit | `kotlin-multiplatform-audit` |
| Capture lessons learned | `kotlin-multiplatform-lessons` |
| Harvest consumer lessons | `/kmm-harvest-lessons` |

## Repo-Local Skill Sources

- Canonical repo-local skill and agent files live under `skills/awake/`.
- `.claude/agents` and `.claude/commands/awake` are symlinks into `skills/awake/`.
- Edit the tracked files under `skills/awake/`, not the symlinked `.claude/` paths.
