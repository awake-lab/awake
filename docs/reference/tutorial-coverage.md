# Tutorial Coverage

This page tracks the rollout from "docs infrastructure exists" to "every meaningful Awake
API has both a reference surface and a learnable example."

## Coverage Rules

Every public-facing module should eventually have:

1. API reference through `Dokka`
2. At least one getting-started tutorial
3. At least one composition or integration example
4. Automatic proof where it matters:
   - screenshot or generated visual report for UI and rendering surfaces
   - deterministic test output, benchmark artifact, or serialized sample for non-visual APIs

## Coverage Matrix

| Module | Reference | Tutorial | Automatic Proof | Status | Next Step |
|---|---|---|---|---|---|
| `awake:base` | Dokka | Not yet | Sample/snippet tests | Planned | Add math/resources quickstart |
| `awake:ecs` | Dokka + `awake/ecs/README.md` | Partial | Benchmarks/tests | Partial | Add "first world/system/query" tutorial |
| `awake:engine` | Dokka | Not yet | Sample bootstrap | Planned | Add engine lifecycle walkthrough |
| `awake:engine:game` | Dokka | Not yet | `samples:hello-cube` bootstrap proof | Planned | Add backend-neutral game setup tutorial |
| `awake:engine:game-dsl` | Dokka + `awake/engine/game-dsl/README.md` + `docs/reference/game-dsl.md` | Partial | `game-dsl` tutorial docs + `samples:hello-cube` authored bootstrap proof | In progress | Expand `gameModule` cookbook with multi-feature samples |
| `awake:engine:ui-dsl` | Dokka + `awake/engine/ui-dsl/README.md` | Partial | UI DSL tutorial snapshots | In progress | Expand authored UI cookbook and custom component examples |
| `awake:engine:render-api` | Dokka | Not yet | Compileable sample | Planned | Add mesh/material/renderer usage guide |
| `awake:engine:ui-core` | Dokka | Partial | UI tutorial snapshots | In progress | Add path/style/shape guide |
| `awake:engine:ui-unstyled` | Dokka | Partial | UI tutorial snapshots | In progress | Expand widget cookbook |
| `awake:engine:ui-designsystem` | Dokka | Not yet | UI tutorial snapshots | Planned | Add design-system styling guide |
| `awake:scene` | Dokka + `awake/scene/README.md` | Partial | Sample scene runtime | Partial | Add scene JSON + runtime tutorial |
| `awake:scene-dsl` | Dokka + `awake/scene-dsl/README.md` | Partial | `gameModule { ecs(...) }` composition proof | In progress | Expand scene authoring cookbook |
| `awake:physics:api` | Dokka | Not yet | Deterministic sample/tests | Planned | Add body/world setup tutorial |
| `awake:backend:vulkan` | Dokka | Not yet | hello-cube + headless render tests | Planned | Add backend setup and shader pipeline guide |
| `awake:backend:webgpu` | Dokka | Not yet | hello-cube web sample | Planned | Add Wasm/WebGPU runtime guide |

## Evidence Types

### Visual APIs

Use generated artifacts for:

- UI widgets and layouts
- style composition
- renderer overlays
- sample scenes where composition and legibility matter

Current implementation:

- `./gradlew developerDocs`
- `awake/engine/ui-dsl/build/reports/ui-dsl-tutorials/index.html`
- `awake/engine/ui-unstyled/build/reports/ui-tutorials/index.html`
- `awake/engine/ui-unstyled/build/reports/ui-snapshots/index.html`

### Non-Visual APIs

Use deterministic proof for:

- ECS queries and system flow
- runtime bootstrap
- physics world setup
- render-api orchestration

Acceptable proof includes:

- unit tests with tutorial-style fixtures
- benchmark outputs
- serialized sample files
- compile-only tutorial samples under a test or sample module

## Rollout Order

1. `ui-core`, `ui-unstyled`, `ui-designsystem`
2. `engine:game`, `scene`, `render-api`
3. `ecs`, `base`, `physics:api`
4. backend-specific guides

That order matches current product pressure: the upcoming DSL needs the UI docs first, then
the scene/bootstrap layer that will consume it.

## Definition Of Done

A module is only "covered" when:

- its public API is documented enough that Dokka is useful
- there is a short tutorial page under `docs/reference/`
- the tutorial example is backed by something runnable or testable
- visual surfaces produce an automatic screenshot or report

Until then, treat it as partial even if we already have good KDoc.
