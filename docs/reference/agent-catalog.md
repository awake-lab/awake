# Agent Catalog

This document is the canonical source for Awake's repo-local agent roster, naming convention,
and responsibility map.

For copyable starter structure, see
[docs/reference/agent-starter-pack.md](/Users/ronvaldoz/StudioProjects/awaken/docs/reference/agent-starter-pack.md).
For real routing examples, see
[docs/reference/agent-routing.md](/Users/ronvaldoz/StudioProjects/awaken/docs/reference/agent-routing.md).

## Purpose

Awake is now large enough that "one engine agent" is too broad. The repo uses named
role-based agents so ECS work, rendering work, UI work, platform work, and documentation work
can evolve independently without drifting into overlapping responsibilities.

## Naming Standard

All repo-local agent files under `skills/awake/agents/` must follow this pattern:

`awake-<domain>-<role>.md`

Rules:

- use `kebab-case`
- always start with `awake-`
- use a concrete domain noun such as `ecs`, `ui`, `scene`, `platform`, `developer-experience`
- end with a professional role noun such as `engineer` or `auditor`
- avoid informal suffixes such as `-dev`, `-helper`, or `-guy`

Examples:

- `awake-ecs-performance-engineer.md`
- `awake-render-backend-engineer.md`
- `awake-game-framework-engineer.md`
- `awake-ui-systems-engineer.md`
- `awake-design-system-engineer.md`
- `awake-ui-quality-engineer.md`
- `awake-app-state-engineer.md`

## Model Tiers

Agent frontmatter's `model:` field must be a real provider model ID, not a capability tier —
tooling (Claude Code agent dispatch, kmp-audit) reads this field to actually select a model,
and a tier name like `flagship-coding` isn't resolvable at runtime. Tiers below are a
human-facing planning vocabulary only, for picking which real ID to assign.

| Tier | Use For |
|---|---|
| `flagship-coding` | hard refactors, architecture changes, renderer backends, ECS performance work |
| `balanced-coding` | everyday implementation, maintenance, validation, docs-linked changes |
| `fast-utility` | bulk scans, rote edits, inventory work, low-risk formatting and reporting |

## Mapping Rule

Verified 2026-08-08 against active provider docs. When assigning a tier's real ID in an
agent's `model:` field, use this table — and update it (not the tier name) when a provider
ships a new model generation.

| Tier | Anthropic (Claude Code) |
|---|---|
| `flagship-coding` | `claude-opus-5` |
| `balanced-coding` | `claude-sonnet-5` |
| `fast-utility` | `claude-haiku-4-5-20251001` |

Awake's agent files only run under Claude Code today, so only the Anthropic column is
tracked. Add OpenAI/Google columns back if/when this repo runs agents under those tools.

## Active Agents

| Agent | Status | Scope | Preferred Tier |
|---|---|---|---|
| `awake-ecs-performance-engineer` | Active | ECS internals, component storage, family/query behavior, benchmarks, churn and transform propagation performance | `flagship-coding` |
| `awake-render-backend-engineer` | Active | Vulkan/WebGPU backend work, renderer extraction, GPU resource lifetime, native graphics bridges, render correctness validation, physics contract (`awake:physics:api`) and native bridge | `flagship-coding` |
| `awake-core-foundations-engineer` | Active | `awake:core` math/input/application-loop, `awake:core:geometry` mesh simplification, `awake:core:animation` skeletal runtime — the dependency-free foundation layer | `balanced-coding` |
| `awake-asset-pipeline-engineer` | Active | `awake:asset:gltf` import, `awake:asset:mesh-optimizer` offline decimation, `awake:asset:shaders` shared uniform-layout contracts | `balanced-coding` |
| `awake-game-framework-engineer` | Active | game/application runtime shell, frame lifecycle, shared engine bootstrap, sample runtime structure, non-backend engine composition | `balanced-coding` |
| `awake-ui-systems-engineer` | Active | `ui-core`, `ui-headless`, `ui-dsl`, low-level layout/text/input behavior, immediate-mode UI mechanics | `balanced-coding` |
| `awake-design-system-engineer` | Active | `ui-designsystem`, theme tokens, component recipes, shadcn-style visual language, showcase and tutorial presentation quality | `balanced-coding` |
| `awake-ui-quality-engineer` | Active | UI verification strategy, snapshot/pixel baselines, overlap/text-fit/layout inspections, density/theme/platform parity checks | `balanced-coding` |
| `awake-scene-runtime-engineer` | Active | scene graph runtime, scene DSL composition, scene serialization boundaries, demo scene structure | `balanced-coding` |
| `awake-platform-integration-engineer` | Active | Android/iOS/Desktop/Web integration, expect/actual edges, device validation, packaging and launcher behavior | `balanced-coding` |
| `awake-developer-experience-engineer` | Active | build logic, docs pipelines, agent guidance, release plumbing, benchmark/snapshot workflows | `balanced-coding` |
| `awake-architecture-auditor` | Active | cross-module ownership checks, split recommendations, review/audit passes, policy drift detection | `flagship-coding` |
| `awake-app-state-engineer` | Active | MVI-style Contract/State/Intent/Effect definitions, Store implementations, wiring store effects into the ECS frame loop | `balanced-coding` |

## Responsibility Boundaries

Use the smallest agent that fully owns the task.

| Task Shape | Primary Agent |
|---|---|
| World storage, pooling, churn benchmarks | `awake-ecs-performance-engineer` |
| Vulkan/WebGPU backend, renderer extraction, GPU/native bridge, physics contract/native bridge | `awake-render-backend-engineer` |
| Foundation math/geometry/animation, dependency-free primitives | `awake-core-foundations-engineer` |
| glTF import, offline mesh baking, shared uniform-layout contracts | `awake-asset-pipeline-engineer` |
| game shell bootstrap, runtime wiring, sample application structure | `awake-game-framework-engineer` |
| Text overflow, layout engine behavior, UI input and animation plumbing | `awake-ui-systems-engineer` |
| Theme tokens, component skins, showcase polish, tutorial presentation styling | `awake-design-system-engineer` |
| UI regression gates, overlap/text-fit checks, pixel baselines, theme/density/platform parity validation | `awake-ui-quality-engineer` |
| Scene composition, runtime scene organization, scene-facing DSL | `awake-scene-runtime-engineer` |
| Android/iOS/Desktop/Web runtime validation and platform glue | `awake-platform-integration-engineer` |
| Build logic, docs generators, agent/skill upkeep, CI-adjacent structure | `awake-developer-experience-engineer` |
| Review, split planning, architectural drift, ownership audits | `awake-architecture-auditor` |
| Sample/game MVI Contract/Store, dispatching intents, draining effects into a frame system | `awake-app-state-engineer` |

If a task spans multiple domains, start with the agent that owns the deepest risk and link to
the other agent docs as needed.

## Agent File Contract

Each repo-local agent file should contain:

1. frontmatter with `name`, `description`, `tools`, and `model`
2. a one-paragraph mission statement
3. read-first canonical docs
4. boundaries: what the agent owns and what it must not absorb
5. validation expectations for its domain

Agent files should not duplicate long-form architecture policy from `docs/*`. Their
`model:` value should be one of Awake's capability tiers unless a specific tool requires a
temporary provider override.

## Current File Map

- [awake-ecs-performance-engineer.md](/Users/ronvaldoz/StudioProjects/awaken/skills/awake/agents/awake-ecs-performance-engineer.md)
- [awake-render-backend-engineer.md](/Users/ronvaldoz/StudioProjects/awaken/skills/awake/agents/awake-render-backend-engineer.md)
- [awake-core-foundations-engineer.md](/Users/ronvaldoz/StudioProjects/awaken/skills/awake/agents/awake-core-foundations-engineer.md)
- [awake-asset-pipeline-engineer.md](/Users/ronvaldoz/StudioProjects/awaken/skills/awake/agents/awake-asset-pipeline-engineer.md)
- [awake-game-framework-engineer.md](/Users/ronvaldoz/StudioProjects/awaken/skills/awake/agents/awake-game-framework-engineer.md)
- [awake-ui-systems-engineer.md](/Users/ronvaldoz/StudioProjects/awaken/skills/awake/agents/awake-ui-systems-engineer.md)
- [awake-design-system-engineer.md](/Users/ronvaldoz/StudioProjects/awaken/skills/awake/agents/awake-design-system-engineer.md)
- [awake-ui-quality-engineer.md](/Users/ronvaldoz/StudioProjects/awaken/skills/awake/agents/awake-ui-quality-engineer.md)
- [awake-scene-runtime-engineer.md](/Users/ronvaldoz/StudioProjects/awaken/skills/awake/agents/awake-scene-runtime-engineer.md)
- [awake-platform-integration-engineer.md](/Users/ronvaldoz/StudioProjects/awaken/skills/awake/agents/awake-platform-integration-engineer.md)
- [awake-developer-experience-engineer.md](/Users/ronvaldoz/StudioProjects/awaken/skills/awake/agents/awake-developer-experience-engineer.md)
- [awake-architecture-auditor.md](/Users/ronvaldoz/StudioProjects/awaken/skills/awake/agents/awake-architecture-auditor.md)

## Split Guidance

The catalog comes before module or API splits on purpose:

- formalize ownership first
- split modules second
- move code only after the destination agent boundary is clear

That order keeps the split from becoming a naming and ownership rewrite at the same time.
