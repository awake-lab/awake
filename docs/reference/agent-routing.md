# Agent Routing

This page shows how Awake routes real work between repo-local agents.

Use it when a task seems to span multiple domains and you want the smallest agent that still
owns the deepest risk.

## Quick Rule

Start with the agent that owns the hardest-to-reverse design or correctness risk.

Examples:

- if the risk is storage/query correctness, start with ECS
- if the risk is GPU/backend correctness, start with render backend
- if the risk is layout/text/input behavior, start with UI systems
- if the risk is visual language and component skinning, start with design system

## Awake Routing Matrix

| Task | Primary Agent | Why |
|---|---|---|
| sparse-set storage, pooling, query invalidation, churn benchmark | `awake-ecs-performance-engineer` | data layout and hot-path correctness dominate |
| Vulkan/OpenGL/WebGPU backend extraction, swapchain, GPU lifetime | `awake-render-backend-engineer` | native/backend correctness dominates |
| game bootstrap, runtime shell, `game {}` / `ecsGame {}` composition | `awake-game-framework-engineer` | assembly and lifecycle ownership dominate |
| scene graph runtime, scene DSL, scene serialization | `awake-scene-runtime-engineer` | scene-facing contracts dominate |
| text wrapping, clipping, layout sizing, input dispatch, UI animation plumbing | `awake-ui-systems-engineer` | low-level UI mechanics dominate |
| theme tokens, shadcn-style recipes, showcase appearance, palette tuning | `awake-design-system-engineer` | visual-language consistency dominates |
| Android/iOS/Desktop/Web launcher behavior and platform glue | `awake-platform-integration-engineer` | platform runtime behavior dominates |
| build logic, docs pipelines, tutorial generation, benchmark workflows | `awake-developer-experience-engineer` | contributor tooling dominates |
| split planning, ownership audit, reusable extraction review | `awake-architecture-auditor` | cross-module boundary judgment dominates |

## Common Mixed Cases

### ECS plus rendering

If the task is:

- `MeshRenderer` storage shape
- family iteration
- transform propagation

start with `awake-ecs-performance-engineer`.

If the task is:

- renderer extraction
- GPU resource lifetime
- draw submission correctness

start with `awake-render-backend-engineer`.

### UI mechanics plus design-system work

If the bug is:

- overlapping text
- wrong clipping
- box sizing
- pointer/input behavior

start with `awake-ui-systems-engineer`.

If the bug is:

- muted palette
- wrong border radius
- inconsistent component recipe
- showcase visual polish

start with `awake-design-system-engineer`.

### Game shell plus scene DSL

If the problem is:

- how an app starts
- how runtime state is wired
- where reusable bootstrap helpers belong

start with `awake-game-framework-engineer`.

If the problem is:

- how authored scene structure is expressed
- what belongs in `scene {}` vs runtime helpers
- serialization boundaries

start with `awake-scene-runtime-engineer`.

## Coordination Rule

When a task crosses boundaries:

1. pick one primary agent
2. link to the secondary agent doc in the implementation notes or task doc
3. keep the owning module responsible for the final API shape

Do not flatten two domains into one helper just because one change touches both.
