### Codex Project Profile

### Load skills context on initialization
--system-prompt-file=".claude/AGENTS.md"

### Default flags
--compact
--verbose=false

### Ignore generated and vendor directories
--ignore="**/build/**"
--ignore="**/.gradle/**"
--ignore="**/vendor/**"
--ignore="**/third_party/**"

### Read first
- `docs/architecture.md`
- `docs/reference/ai-collaboration.md`
- `docs/reference/agent-catalog.md`
- `docs/reference/ui-ownership.md`
- `docs/reference/ui-validation.md`
- `docs/reference/game-structure.md`
- `docs/MVP_PLAN.md`

### Read before writing engine code
Mandatory for the domain you are touching — each encodes a bug this repo actually shipped:
- `skills/awake-core-math/SKILL.md` — before any `Vec3`/`Mat4`/camera math, or any code inside
  a `System.update`. Covers the mutating-vs-allocating naming contract (`normalize()` mutates,
  `normalized()` allocates), per-frame allocation rules, and the shared camera-basis rule.
- `skills/awake-ecs-authoring/SKILL.md` — before adding a component, writing a `System`, or
  building entities with the `scene { }` DSL. Covers `Poolable.reset()` completeness, why
  reflective component construction breaks on iOS/wasmJs, structural-change churn, entity
  ownership on teardown, and `@DslMarker` on nested builders.
- `skills/awake-ecs-scene-runtime/SKILL.md` — consuming the scene runtime from a sample/demo.
- `skills/awake-render-pipeline/SKILL.md` — before adding a render feature/pass, wiring a new
  `RenderPipeline`, touching draw-call sorting in `RendererDraw3D`, or changing `GameApplication`/`Game` wiring.
- `skills/awake-render-vulkan/SKILL.md` — before modifying Vulkan swapchain creation/resizing,
  GPU resource allocations, command recording, JNI bindings, or Android Vulkan verification.
- `skills/awake-render-webgpu/SKILL.md` — before modifying WebGPU pipelines, wgpu4k/Dawn integration,
  WASM browser canvas resizing, or buffer upload paths.
- `skills/awake-physics-jolt/SKILL.md` — before modifying physics simulation steps, rigid bodies,
  colliders, contact listeners, raycasting, or ECS physics synchronization.
- `skills/awake-ui-authoring/SKILL.md` — before adding or changing any UI widget, adding a
  size/spacing constant, or naming a primitive. Covers which of `ui-core`/`ui-headless`/
  `ui-designsystem` owns what, the derivable-size rule (an headless default becomes the spec),
  Dp-not-pixels, and the Radix-canonical naming policy.
- `skills/awake-ui-shadcn-consuming/SKILL.md` — before adding or changing any screen in a
  sample, game, or tool that renders UI (not limited to `samples:studio`). Consumer code
  renders visible UI only through `shadcn*` recipes, never imports `ui-core`, never authors
  its own `Style{}`; `ui-headless` layout/state (`column`/`row`/`Modifier`/`remember*`) stays
  fine to import for structure. Mirrors Jetpack Compose's real layering (`ui-core` ≈
  `compose-ui`, `ui-headless` ≈ `compose-foundation`, `ui-designsystem` ≈ Material).
- `skills/awake-ui-shadcn-styling/SKILL.md` — maintainer guide for building or extending Shadcn
  components in `ui-designsystem`. Covers `Style.then` state-rule merges, card trigger padding,
  collapsible decoupling, and animation tweening.
- `skills/awake-ui-icons/SKILL.md` — before adding or editing any `UiImageVector`/icon
  path data. One hard rule: icon vectors are generated from SVG sources via
  `tools/svg_to_ui_image_vector.py`, never hand-transcribed or derived by rotating another
  glyph's coordinates.
- `skills/awake-ui-verification/SKILL.md` — before claiming UI fidelity or modifying snapshot tests.
