![Build And Publish](https://github.com/ronjunevaldoz/awake/actions/workflows/build-and-publish.yml/badge.svg)
[![Kotlin](https://img.shields.io/badge/kotlin-2.4.0-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

# Awake

A Kotlin Multiplatform game engine. Write your game once in `commonMain` and run it on
Android, iOS, Desktop, and the Web — Awake picks the right graphics backend for each
(Vulkan, or WebGPU on the web).

> **Not published yet.** Build from source. Nothing has shipped to Maven Central.

## What's actually here

**Rendering** — Vulkan on desktop/Android/iOS (MoltenVK), WebGPU on the web. Shaders are
authored once as WGSL and compiled per backend at build time (naga → SPIR-V). Directional
light with shadow mapping (texel-calibrated depth bias), PBR metallic-roughness materials,
glTF loading with GPU skinning, headless rendering with pixel-readback for tests.

**ECS and scenes** — a sparse-set ECS (benchmarked against Fleks), a `scene { }` DSL, and
scenes-as-data: JSON scene documents a runtime instantiates, so switching examples is a
loader call, not hand-rolled demo objects.

**UI** — an immediate-mode UI stack built in three owned layers:

- `ui-core` — layout, text, styling, animation primitives. Packed Roboto glyph atlas
  verified against Chromium rendering the same TTF.
- `ui-headless` — Radix-style unstyled widgets, plus Heroicons as generated vector data
  (every icon gated at ≥0.75 IoU against the official SVG render; the shipped set measures
  0.98–1.0).
- `ui-designsystem` — ~80 shadcn-inspired components: buttons, fields, dialogs, drawer,
  sheet, tabs, table, combobox, resizable panel groups, sidebar, context menus, and the
  rest, all themed through tokens extracted from a pinned `shadcn-ui/ui` checkout.

**Physics** — backend-neutral API with a Jolt Physics implementation (desktop/Android/iOS).

**Tooling** — generators, not hand-authoring: SVG → `UiImageVector`, TTF → glyph atlas,
JNI bindings, and the UI verification harnesses below.

## Try it

The demo suite is [`samples/scene3d-playground`](samples/scene3d-playground) — a rotating
cube with camera modes, a glTF model viewer, GPU skinning, and texture sampling.

```bash
./gradlew :samples:scene3d-playground:run
```

For the web version (needs Chrome/Edge 113+ for WebGPU), run
`:samples:scene3d-playground:wasmJsBrowserDevelopmentRun` and open the URL it prints.

Also runnable:

- [`samples/ui-showcase`](samples/ui-showcase) — a docs-style gallery of every UI
  component, and the home of the visual regression suites.
- [`samples/studio`](samples/studio) — an MVI-structured editor shell: example browser,
  inspector, viewport tool rail, camera modes via right-click context menu.

Desktop and Web are the targets with runnable apps today. iOS and Android build the shared
framework and AAR, but these samples have no app wrapper for them yet.

## How a game fits together

1. **A `Game`** — your code. Implement `ready(renderer)` and `render(delta, width, height)`
   (plus optional `resize`/`pause`/`resume`/`dispose`).
2. **An application** — `VulkanGameApplication` or `WebGpuGameApplication`. Hand it your
   `Game` and your shader paths; it builds the device, swapchain, and pipelines for you.
3. **A scene, if you want one** — `sceneGame { }` gives you an ECS world with `Transform`,
   `MeshRenderer`, `Camera`, and `Light` components, plus the systems that drive them.

Meshes and materials come from `renderer.createMesh(...)` / `renderer.createMaterial(...)`.
A mesh's vertex format decides which pipeline draws it, so skinned and textured meshes go
through the same `MeshRenderer` path as everything else.

## Modules

All modules are `0.1.0-SNAPSHOT` and target Android, iOS, Desktop, and Web unless noted.

| Module | Status | What it does |
| :--- | :--- | :--- |
| [`awake:base`](awake/base) | Alpha | Math, assets, input |
| [`awake:ecs`](awake/ecs/README.md) | Alpha | Sparse-set ECS runtime |
| [`awake:engine`](awake/engine) | Alpha | Bootstrap and app lifecycle |
| [`awake:engine:ui`](awake/engine/ui) | Alpha | Immediate-mode UI stack |
| [`awake:scene`](awake/scene/README.md) | Alpha | Scene graph and components |
| [`awake:physics:api`](awake/physics/api) | Alpha | Backend-neutral physics contract |
| [`awake:backend:vulkan`](awake/backend/vulkan) | Alpha | Main renderer (no Web) |
| [`awake:backend:webgpu`](awake/backend/webgpu) | Experimental | Web renderer (Web only) |
| [`awake:backend:jolt`](awake/backend/jolt) | Alpha | Jolt Physics implementation |
| [`awake:backend:opengl`](awake/backend/opengl) | Frozen | The old engine's renderer. Compiles, not wired in. |

## Shaders

Shaders are authored once as WGSL and compiled to each backend's format. After editing one:

```bash
./gradlew :samples:scene3d-playground:syncAwakeShaders
```

Vulkan loads the compiled `.spv` binaries at runtime, **not** the `.frag`/`.vert` sources
next to them — so editing a source without recompiling silently changes nothing. The
`verifyShaderBinaries` check catches that and fails the build.

## Verifying UI and icons

The UI is verified against real external references, not just its own previous output.
Four different questions, four different gates — a passing golden says a render did not
change, never that it is correct.

| Question | Gate | Where |
|---|---|---|
| Did this change? | snapshot goldens + layout signature maps | `:samples:ui-showcase:desktopTest` |
| Does it look like shadcn? | per-pair mismatch baselines vs a local build of real shadcn components — drift beyond tolerance **fails the build** | `ShadcnReferenceComparisonTest` |
| Is this token value right? | numeric comparison against a pinned `shadcn-ui/ui` checkout | `ShadcnReferenceTokenExpandedTest` |
| Does this icon match Heroicons? | coverage-mask IoU against the rasterized official SVG | `IconFidelityTest` |

```bash
tools/fetch_shadcn_reference.sh                 # pin the reference (run first)
./gradlew :samples:ui-showcase:desktopTest
python3 tools/generate_parity_report.py         # refresh the parity report
python3 tools/generate_ui_status.py             # refresh the status matrix
```

Two rules before touching a baseline: never re-record without opening the diff first, and
treat a parity number from a badly cropped pair as *unmeasured* rather than passing.
`skills/awake-ui-verification/SKILL.md` carries the reasoning,
`docs/reference/ui-validation.md` the policy, `tools/README.md` the toolchain.

## Docs

- [`CHANGELOG.md`](CHANGELOG.md) — what changed
- [`docs/MVP_PLAN.md`](docs/MVP_PLAN.md) — near-term status
- [`docs/MMORPG_ROADMAP.md`](docs/MMORPG_ROADMAP.md) — long-horizon plan
- [`docs/reference/developer-docs.md`](docs/reference/developer-docs.md) — docs workflow
- [`docs/reference/ui-fidelity-status.md`](docs/reference/ui-fidelity-status.md) — per-area UI status
- [`docs/reference/shadcn-parity.md`](docs/reference/shadcn-parity.md) — generated shadcn parity report
- [`tools/README.md`](tools/README.md) — asset generators and the parity toolchain

`./gradlew developerDocs` builds the API reference and UI tutorial pages.

## License

[Apache License, Version 2.0](LICENSE).
