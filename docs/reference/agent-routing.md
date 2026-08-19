# Agent Routing

This page shows how Awake routes real work between repo-local agents across the **Engine Framework Suite** and the **Game Studio Creative Suite**.

Use it when a task seems to span multiple domains and you want the smallest agent that still owns the deepest risk.

## Quick Rule

- For **Engine Library & Infrastructure**: Start with the engineer who owns the deepest architectural or native risk.
- For **Game Production & Content (`samples/`)**: Start with the creative lead who owns the feature's discipline (Design, Narrative, Camera, Art/VFX, Audio).

## Routing Matrix

### Suite A: Engine Framework Suite (Library & Core Systems)

| Task | Primary Agent | Why |
|---|---|---|
| Vector/Matrix math, ECS sparse sets, entity lifecycle, query performance, glTF format decoding, scene graph hierarchy | `awake-engine-core-engineer` | Foundation data layout, allocation-free loops, and core contracts dominate |
| Vulkan/WebGPU driver pipelines, GPU swapchain, JNI bindings codegen, Jolt physics bridge | `awake-render-backend-engineer` | Native driver correctness, memory symmetry, and GPU resource lifetimes dominate |
| Immediate-mode UI layout engine, headless behavioral widgets, Shadcn component recipes, visual snapshots & parity gates | `awake-ui-engineer` | End-to-end UI mechanics, token styling, and regression verification dominate |
| `GameApplication` shell bootstrap, frame lifecycle wiring, sample app composition, MVI state containers & effect draining | `awake-game-runtime-engineer` | Application assembly, mode transitions, and state flow dominate |
| Multiplatform launchers (Android/iOS/Desktop/Wasm), Gradle convention plugins, CI/CD workflows, Maven Central & SPM release | `awake-platform-release-engineer` | Build toolchain, platform glue, and package distribution dominate |
| Cross-module boundary reviews, KMP clean architecture audits, API leakage checks, legacy code extraction planning | `awake-architecture-auditor` | Multi-module architectural integrity and policy enforcement dominate |

### Suite B: Game Studio Creative Suite (Full-Stack Game Creation)

| Task | Primary Persona | Why |
|---|---|---|
| Milestone delivery, feature scope pruning, vertical slice prioritization, cross-discipline integration | `awake-game-producer` | Production pace and scope discipline dominate |
| Core gameplay loop, combat balance, player controls, economy, gameplay ECS components & systems (`gameplay/`) | `awake-game-designer` | Game feel, mechanics, and simulation rules dominate |
| World lore, narrative scripts, branching dialogue trees, quest progression state machines | `awake-narrative-director` | Storytelling, dialogue branching, and quest pacing dominate |
| Gameplay follow cameras, combat screen shake, cinematic cutscene spline tracks, dynamic framing | `awake-camera-director` | Visual direction, camera smoothing, and cinematic pacing dominate |
| Art style guides, 3D glTF specs, 2D texture/sprite prompts, particle VFX emitters, custom material shaders | `awake-art-vfx-director` | Visual aesthetics, lighting mood, and particle dynamics dominate |
| Sound effects (SFX), dynamic BGM stem triggers, ambient soundscapes, 3D spatial audio emitters | `awake-audio-designer` | Audio landscape, dynamic sound ducking, and spatial positioning dominate |

## Common Mixed Cases

### ECS Mechanics vs Core Engine Performance
If the task is authoring game-specific combat/movement rules in `samples/<game>/gameplay/`, route to `awake-game-designer`.
If the task is optimizing sparse-set storage, pooling, or query iteration in `awake:ecs`, route to `awake-engine-core-engineer`.

### Render Pipeline vs Game VFX / Shaders
If the task is low-level Vulkan/WebGPU pipeline setup, JNI bindings, or render pass architecture, route to `awake-render-backend-engineer`.
If the task is authoring custom particle emitter descriptors, material colors, or post-process bloom parameters for a game scene, route to `awake-art-vfx-director`.

### UI Component Authoring vs Game Dialogue UI
If the task is implementing a new reusable Shadcn component recipe or layout primitive in `awake:ui:*`, route to `awake-ui-engineer`.
If the task is authoring dialogue text and branching quest options for a game story, route to `awake-narrative-director`.
