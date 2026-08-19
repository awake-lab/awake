# Agent Catalog

This document is the canonical source for Awake's repo-local agent roster, naming convention,
responsibility map, and expansion governance.

For real routing examples, see
[docs/reference/agent-routing.md](/Users/ronvaldoz/StudioProjects/awaken/docs/reference/agent-routing.md).

## Purpose

Awake operates as both a Kotlin Multiplatform game engine library and a full-stack game development studio.
To maintain high cohesion and prevent context fragmentation, the repository organizes agents into a **Dual-Suite Architecture**:
1. **Engine Framework Suite**: 6 engineering agents responsible for core engine algorithms, graphics backends, UI subsystems, platform glue, and architecture governance.
2. **Game Studio Creative Suite**: 6 creative personas responsible for full-stack game production in `samples/` (production, mechanics design, narrative, camera direction, art/VFX, and audio).

## Naming Standard

All repo-local agent files under `skills/awake/agents/` must follow this pattern:

`awake-<domain>-<role>.md`

Rules:
- use `kebab-case`
- always start with `awake-`
- use a concrete domain noun such as `engine-core`, `render-backend`, `ui`, `game-runtime`, `platform-release`, `game`, `narrative`, `camera`, `art-vfx`, `audio`
- end with a professional role noun such as `engineer`, `auditor`, `director`, `designer`, `producer`
- avoid informal suffixes such as `-dev`, `-helper`, or `-guy`

## Model Tiers & Tooling Mapping

Agent frontmatter's `model:` field must contain an active provider model ID (e.g. `claude-opus-5`, `claude-sonnet-5`), as runner tooling (Claude Code agent dispatch, kmp-audit) reads this field to select an executable model at runtime.
The capability tiers below serve as a design taxonomy for picking which provider model ID to assign.

| Tier | Use For | Anthropic (Claude Code) |
|---|---|---|
| `flagship-coding` | Deep refactors, native backends, ECS performance hot-paths, cross-module architecture audits | `claude-opus-5` |
| `balanced-coding` | Everyday feature implementation, creative game authoring, UI recipes, snapshot verification | `claude-sonnet-5` |
| `fast-utility` | Bulk scans, rote edits, inventory checks, low-risk formatting | `claude-haiku-4-5-20251001` |

---

## Active Agents

### Suite A: Engine Framework Suite (Library & Native Infrastructure)

| Agent | Status | Primary Scope | Preferred Tier | Assigned Model |
|---|---|---|---|---|
| `awake-engine-core-engineer` | Active | `:awake:core` (math/geom/anim), `:awake:ecs` (+ benchmark), `:awake:scene` (graph/runtime/DSL), `:awake:asset` (glTF/shaders/optimizer) | `flagship-coding` | `claude-opus-5` |
| `awake-render-backend-engineer` | Active | `:awake:backend:vulkan` (+ bindings), `:awake:backend:webgpu`, `:awake:backend:jolt`, `:awake:physics:api`, JNI bridges, pixel baselines | `flagship-coding` | `claude-opus-5` |
| `awake-ui-engineer` | Active | `:awake:ui:ui-core`, `:awake:ui:headless`, `:awake:ui:designsystem`, `:awake:ui:testing` (geometry parity, visual regression) | `balanced-coding` | `claude-sonnet-5` |
| `awake-game-runtime-engineer` | Active | `:awake:engine:game`, `:awake:engine:app`, `:samples:studio`, `:samples:ui-showcase`, MVI Store & effect draining | `balanced-coding` | `claude-sonnet-5` |
| `awake-platform-release-engineer` | Active | `androidMain`, `iosMain`, `desktopMain`, `wasmJs`, `build-logic`, CI workflows, Maven Central publishing | `balanced-coding` | `claude-sonnet-5` |
| `awake-architecture-auditor` | Active | Cross-module boundaries, KMP clean architecture, Detekt rules, API leakage audits | `flagship-coding` | `claude-opus-5` |

### Suite B: Game Studio Creative Suite (Full-Stack Game Creation)

| Persona | Status | Primary Scope | Preferred Tier | Assigned Model |
|---|---|---|---|---|
| `awake-game-producer` | Active | Game milestone planning, scope control, task prioritization, integration checklists | `balanced-coding` | `claude-sonnet-5` |
| `awake-game-designer` | Active | Game Design Documents (GDD), core loops, combat balance, player controls, ECS gameplay systems | `balanced-coding` | `claude-sonnet-5` |
| `awake-narrative-director` | Active | Story lore, character sheets, branching dialogue graphs, quest progression state | `balanced-coding` | `claude-sonnet-5` |
| `awake-camera-director` | Active | Virtual camera choreography, cinematic cutscenes, easing spline tracks, screen shake | `balanced-coding` | `claude-sonnet-5` |
| `awake-art-vfx-director` | Active | Art style guides, 3D glTF specs, 2D sprite prompts, particle VFX emitters, custom material shaders | `balanced-coding` | `claude-sonnet-5` |
| `awake-audio-designer` | Active | Sound effects (SFX), dynamic BGM stem triggers, ambient soundscapes, 3D spatial audio emitters | `balanced-coding` | `claude-sonnet-5` |

---

## Domain Skill Alignment Matrix

Every agent directly references and owns its matching domain skills:

| Agent | Matching Domain Skills |
|---|---|
| `awake-engine-core-engineer` | [skills/awake-core-math/SKILL.md](/Users/ronvaldoz/StudioProjects/awaken/skills/awake-core-math/SKILL.md), [skills/awake-ecs-authoring/SKILL.md](/Users/ronvaldoz/StudioProjects/awaken/skills/awake-ecs-authoring/SKILL.md), [skills/awake-ecs-scene-runtime/SKILL.md](/Users/ronvaldoz/StudioProjects/awaken/skills/awake-ecs-scene-runtime/SKILL.md) |
| `awake-render-backend-engineer` | [skills/awake-render-pipeline/SKILL.md](/Users/ronvaldoz/StudioProjects/awaken/skills/awake-render-pipeline/SKILL.md), [skills/awake-render-vulkan/SKILL.md](/Users/ronvaldoz/StudioProjects/awaken/skills/awake-render-vulkan/SKILL.md), [skills/awake-render-webgpu/SKILL.md](/Users/ronvaldoz/StudioProjects/awaken/skills/awake-render-webgpu/SKILL.md), [skills/awake-physics-jolt/SKILL.md](/Users/ronvaldoz/StudioProjects/awaken/skills/awake-physics-jolt/SKILL.md) |
| `awake-ui-engineer` | [skills/awake-ui-authoring/SKILL.md](/Users/ronvaldoz/StudioProjects/awaken/skills/awake-ui-authoring/SKILL.md), [skills/awake-ui-shadcn-consuming/SKILL.md](/Users/ronvaldoz/StudioProjects/awaken/skills/awake-ui-shadcn-consuming/SKILL.md), [skills/awake-ui-shadcn-styling/SKILL.md](/Users/ronvaldoz/StudioProjects/awaken/skills/awake-ui-shadcn-styling/SKILL.md), [skills/awake-ui-css-modifier/SKILL.md](/Users/ronvaldoz/StudioProjects/awaken/skills/awake-ui-css-modifier/SKILL.md), [skills/awake-ui-icons/SKILL.md](/Users/ronvaldoz/StudioProjects/awaken/skills/awake-ui-icons/SKILL.md), [skills/awake-ui-verification/SKILL.md](/Users/ronvaldoz/StudioProjects/awaken/skills/awake-ui-verification/SKILL.md) |
| `awake-game-runtime-engineer` | [skills/awake-ecs-scene-runtime/SKILL.md](/Users/ronvaldoz/StudioProjects/awaken/skills/awake-ecs-scene-runtime/SKILL.md) |
| `awake-game-designer` | [skills/awake-ecs-authoring/SKILL.md](/Users/ronvaldoz/StudioProjects/awaken/skills/awake-ecs-authoring/SKILL.md) |
| `awake-camera-director` | [skills/awake-core-math/SKILL.md](/Users/ronvaldoz/StudioProjects/awaken/skills/awake-core-math/SKILL.md) |
| `awake-art-vfx-director` | [skills/awake-render-pipeline/SKILL.md](/Users/ronvaldoz/StudioProjects/awaken/skills/awake-render-pipeline/SKILL.md) |

---

## Expansion Governance (The 3-Point Gatekeeper)

To prevent agent sprawl when new capabilities (e.g. AI bots, level generation, audio DSP) arrive:

### 1. The Skill-First Rule
**Default to creating a Domain Skill (`skills/awake-<topic>/SKILL.md`) first.**
An existing agent executes that skill. Only promote a discipline to a standalone Agent when it passes the 3-Point Gatekeeper.

### 2. The 3-Point Gatekeeper Test
A new agent must pass all three criteria:
1. **Module Ownership Gate**: Does it own a distinct Gradle module or subsystem?
2. **Verification Autonomy Gate**: Does it have an independent automated test/benchmark harness?
3. **Vertical Handoff Gate**: Can a developer complete an end-to-end task in this domain without requiring 3+ agent handoffs for a single PR?

---

## Current File Map

- [awake-engine-core-engineer.md](/Users/ronvaldoz/StudioProjects/awaken/skills/awake/agents/awake-engine-core-engineer.md)
- [awake-render-backend-engineer.md](/Users/ronvaldoz/StudioProjects/awaken/skills/awake/agents/awake-render-backend-engineer.md)
- [awake-ui-engineer.md](/Users/ronvaldoz/StudioProjects/awaken/skills/awake/agents/awake-ui-engineer.md)
- [awake-game-runtime-engineer.md](/Users/ronvaldoz/StudioProjects/awaken/skills/awake/agents/awake-game-runtime-engineer.md)
- [awake-platform-release-engineer.md](/Users/ronvaldoz/StudioProjects/awaken/skills/awake/agents/awake-platform-release-engineer.md)
- [awake-architecture-auditor.md](/Users/ronvaldoz/StudioProjects/awaken/skills/awake/agents/awake-architecture-auditor.md)
- [awake-game-producer.md](/Users/ronvaldoz/StudioProjects/awaken/skills/awake/agents/awake-game-producer.md)
- [awake-game-designer.md](/Users/ronvaldoz/StudioProjects/awaken/skills/awake/agents/awake-game-designer.md)
- [awake-narrative-director.md](/Users/ronvaldoz/StudioProjects/awaken/skills/awake/agents/awake-narrative-director.md)
- [awake-camera-director.md](/Users/ronvaldoz/StudioProjects/awaken/skills/awake/agents/awake-camera-director.md)
- [awake-art-vfx-director.md](/Users/ronvaldoz/StudioProjects/awaken/skills/awake/agents/awake-art-vfx-director.md)
- [awake-audio-designer.md](/Users/ronvaldoz/StudioProjects/awaken/skills/awake/agents/awake-audio-designer.md)
