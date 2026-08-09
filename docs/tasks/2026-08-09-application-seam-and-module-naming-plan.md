# Application seam and module naming plan

Date: 2026-08-09

## Objective

Give Awake a real public application entry point (`AwakeApplication`) so consumers stop
importing backend modules directly, and fix the module-path naming inconsistencies this work
touches along the way. This is the precondition for shipping a public API — today a consumer
must import `awake:backend:vulkan` (or `webgpu`) directly and hand-write per-platform bootstrap
code, which is exactly the coupling that blocked treating this as a public library.

Separate, unrelated concern noted but deliberately **not** covered here: `ui-designsystem`'s
~600 imports of `ui-core` (should route through `ui-headless` only). That is a distinct axis —
different modules, different dependency direction, no shared file with this plan — and gets its
own task doc so this one stays reviewable.

## Why This Exists

Three findings, from an architecture review of what's blocking a public API cut:

1. **No backend-neutral entry point exists.** `VulkanGameApplication` and `WebGpuGameApplication`
   have different constructor shapes (Vulkan carries `additionalPipelines`/`wireframeSupport`/
   `shadowShaderSet`; WebGpu doesn't), and every sample hand-writes a per-platform
   `*Bootstrap.kt` + `Main.kt`/`main.ios.kt` to construct the right one. Three samples, three
   times each, six-plus files total, all duplicating the same wiring.
2. **The layers already exist, just unnamed as a hierarchy.** `Application`
   (`awake:base`, `core.graphics`) is the platform-loop contract
   (`create/update/resize/pause/resume/dispose`). `GenericGameApplication` (`awake:engine:game`)
   implements it and owns the `Game` lifecycle, abstract over `createBackendResources`. Both are
   real, working, and correctly layered — they're just not named as the `WindowApplication` →
   `GameApplication` → concrete-backend stack they actually are, and there's no top layer a
   consumer is meant to construct.
3. **Module path naming has no single rule.** Four different segmenting habits coexist today
   (see the table below), discovered while deciding where the new module should live — placing
   it revealed the inconsistency, not the other way around.

## Part 1 — Application layers

### Current shape (verified in source, not assumed)

```
WindowApplication‑shaped contract   awake:base                → core.graphics.Application
        │ implements
GameApplication‑shaped base         awake:engine:game          → GenericGameApplication (abstract)
        │ extended by
   ┌────┴────┐
Vulkan actual   WebGpu actual        awake:backend:vulkan / awake:backend:webgpu
   VulkanGameApplication             WebGpuGameApplication
   (desktop/android/ios)             (wasmJs)
        │ constructed directly by
   samples:*/app/*Bootstrap.kt  (duplicated per sample, per platform)
```

`GenericGameApplication.createBackendResources(window): BackendResources` is the seam each
backend fills in; `BackendResources(renderer, viewportSize)` is already the neutral return
type. The abstraction is sound — the problem is entirely that step 4 exists at all and that
step 2/3's constructors don't unify.

### Target shape

```
WindowApplication      awake:base                — renamed from Application
        │
GameApplication        awake:engine:game          — renamed from GenericGameApplication
        │
   ┌────┴────┐
VulkanGameApplication   WebGpuGameApplication      — UNCHANGED, become internal
   │                        │
   └───────────┬────────────┘
        AwakeApplication    NEW — expect/actual, top of the graph
        │
samples:*/app/*.kt   — one call site: AwakeApplication(config) { game }
```

`AwakeApplication` cannot live inside `engine:game` next to its own parent: it must depend on
**both** `backend:vulkan` and `backend:webgpu` to pick between their actuals, and those backends
already depend on `engine:game` (upward). Nesting it under `engine:` would be circular. It has
to sit structurally above the backends — a new top-level module, not a peer.

### `AwakeApplication` design

- `expect class AwakeApplication` in the new module's `commonMain`.
- Constructor carries the **union** of both backends' current parameters, all but `game` and
  the shader set defaulted: `vertexFormat`, `additionalPipelines`, `wireframeSupport`,
  `shadowShaderSet`. This is a deliberate choice over a shared-base-class param subset — a
  consumer writing one `commonMain` call site needs one signature that compiles on every
  target, and expect/actual is what lets each `actual` honor only the params its backend
  supports without a runtime "ignored on this platform" surprise.
- `actual` in `backend:vulkan` (covers desktop/android/ios via that module's existing target
  set) forwards every parameter to `VulkanGameApplication` unchanged.
- `actual` in `backend:webgpu` (wasmJs) constructs `WebGpuGameApplication`, and its `actual`
  constructor simply does not declare `additionalPipelines`/`wireframeSupport`/
  `shadowShaderSet` as meaningful — either the expect's defaults are silently accepted (Kotlin
  allows narrower actual signatures behind default params) or, if that proves awkward in
  practice, they resolve to a no-op with nothing surprising happening at runtime. Confirm the
  exact mechanism during implementation; do not guess it into the plan.
- `VulkanGameApplication`/`WebGpuGameApplication` stay exactly as they are today — same file,
  same tests, same constructors. They become **internal implementation detail** of their
  module (or gain `internal` visibility) once `AwakeApplication` is the only sanctioned
  construction path; existing tests that construct them directly (see
  `RotatingCubePixelBaselineTest` etc. in `samples:scene3d-playground`) keep working since
  those are same-module/test-scope call sites, not public-API consumers.

### Before / after (real code, `samples:scene3d-playground`)

**Before — two backend-specific bootstrap files, plus a third platform-entry file per target.**

`app/Scene3DPlaygroundVulkanBootstrap.kt` (appMain — desktop/android/ios):

```kotlin
private val Scene3DPlaygroundShaders = gameShaderSet("lit_shadow")
private val Scene3DPlaygroundSkinnedShaders = gameShaderSet("skinned")
private val Scene3DPlaygroundTexturedShaders = gameShaderSet("textured")
private val Scene3DPlaygroundShadowShaders = gameShaderSet("shadow_depth")

fun createScene3DPlaygroundVulkanApplication(
    game: AwakeGame = scene3DPlayground(),
): VulkanGameApplication = VulkanGameApplication(
    shaderSet = Scene3DPlaygroundShaders,
    vertexFormat = VertexFormat.PositionNormalColor,
    game = game,
    additionalPipelines = mapOf(
        VertexFormat.PositionNormalColorSkin to Scene3DPlaygroundSkinnedShaders,
        VertexFormat.PositionNormalColorUv to Scene3DPlaygroundTexturedShaders,
    ),
    wireframeSupport = true,
    shadowShaderSet = Scene3DPlaygroundShadowShaders,
)
```

`app/Scene3DPlaygroundWebGpuBootstrap.kt` (wasmJsMain) — a *second*, differently-shaped
constructor call for the same game, missing `shadowShaderSet` and `additionalPipelines`
entirely because `WebGpuGameApplication` never grew them:

```kotlin
private val Scene3DPlaygroundShaders = gameShaderSet("triangle")

fun createScene3DPlaygroundWebGpuApplication(): WebGpuGameApplication = WebGpuGameApplication(
    shaderSet = Scene3DPlaygroundShaders,
    vertexFormat = VertexFormat.PositionNormalColor,
    game = scene3DPlayground(),
    wireframeSupport = true,
)
```

Then each platform's real entry point calls the matching factory — `Main.kt` (desktopMain):
`applicationFactory = ::createScene3DPlaygroundVulkanApplication`; `main.ios.kt`:
`makeVulkanGameViewController(createScene3DPlaygroundVulkanApplication())`; `main.kt`
(wasmJsMain): `launchWebGpuGame(applicationFactory = ::createScene3DPlaygroundWebGpuApplication)`.
Backend selection is baked into the sample's own source tree, once per platform.

**After — one `commonMain` factory, same shape on every target** (illustrative: exact
`AwakeApplication` API TBD during Part 1 implementation, shown here to make the collapse
concrete, not as a final signature):

```kotlin
// commonMain — no backend import, compiles and means the same thing on every target
private val Scene3DPlaygroundShaders = gameShaderSet("lit_shadow")
private val Scene3DPlaygroundSkinnedShaders = gameShaderSet("skinned")
private val Scene3DPlaygroundTexturedShaders = gameShaderSet("textured")
private val Scene3DPlaygroundShadowShaders = gameShaderSet("shadow_depth")

fun createScene3DPlaygroundApplication(
    game: AwakeGame = scene3DPlayground(),
): AwakeApplication = AwakeApplication(
    shaderSet = Scene3DPlaygroundShaders,
    vertexFormat = VertexFormat.PositionNormalColor,
    game = game,
    additionalPipelines = mapOf(
        VertexFormat.PositionNormalColorSkin to Scene3DPlaygroundSkinnedShaders,
        VertexFormat.PositionNormalColorUv to Scene3DPlaygroundTexturedShaders,
    ),
    wireframeSupport = true,
    shadowShaderSet = Scene3DPlaygroundShadowShaders,
)
```

wasmJs's `actual` receives the same call, same params; it simply doesn't build the shadow
pass or the extra pipelines internally, since WebGPU has no SPIR-V-shaped equivalent to wire
them into. One file replaces two; the per-platform `Main.kt`/`main.ios.kt`/`main.kt` entry
points still exist (that's `WindowApplication`'s job, unchanged by this plan) but each now
calls one shared factory instead of a backend-specific one.

### Migration per sample

Each of `samples:scene3d-playground`, `samples:studio`, `samples:ui-showcase` currently has:
- `app/*VulkanBootstrap.kt` (appMain)
- `app/*WebGpuBootstrap.kt` (wasmJsMain)
- `app/Main.kt` (desktopMain), `app/main.ios.kt` (iosMain), `app/main.kt` (wasmJsMain)

All of these collapse into one `commonMain` file calling `AwakeApplication(config) { game }`.
The per-platform `Main.kt`/`main.ios.kt`/`main.kt` files may still need to exist as thin
platform entry points (window creation, GLFW loop start) — that is a platform-loop concern
`WindowApplication` already owns, not something this plan changes. What disappears is the
backend **selection and configuration** duplication, not the platform bootstrap itself.

### Tests to add

- `AwakeApplication` construction test per platform target confirming it delegates to the
  correct concrete backend with the parameters preserved (unit-testable without a real GPU —
  assert on the `BackendResources`/constructor-forwarding shape, not a live render).
- A regression test asserting no sample's `commonMain` imports `io.github.ronjunevaldoz.awake.vulkan.*`
  or `io.github.ronjunevaldoz.awake.webgpu.*` directly — this is the architectural property the
  whole plan exists to establish, so it should be a real, permanently-enforced check (a Konsist-
  style test or a grep-based CI step), not just a one-time cleanup.

## Part 2 — Module path naming

### The four inconsistent patterns found

| Pattern in use | Correct examples | Where it breaks |
|---|---|---|
| Colon-nest by domain | `scene:core`, `scene:controls`, `scene:physics`, `scene:rendering`, `scene:runtime` | `scene-dsl` is hyphenated and a *sibling* of `scene`, not nested under it, despite being the same domain |
| Leaf states what's different | `backend:vulkan`, `backend:webgpu`, `backend:jolt` | `engine:ui:ui-core` repeats the parent segment's name in the leaf — says "ui" twice |
| Colon-nest a sub-boundary | `backend:vulkan:android-native` | `backend:vulkan-generator` hyphenates instead of `backend:vulkan:generator` |
| `domain:api` for a neutral contract + backend implementations | `physics:api` (+ `backend:jolt` implementing it) | `engine:render-api` hyphenates the same shape instead of `engine:render:api` — confirmed via `backend:vulkan`/`backend:webgpu` both declaring `api(project(":awake:engine:render-api"))`, i.e. it is genuinely the same `domain:api` relationship as physics, just spelled differently |

One rule replaces all four: **colon-nest by domain, never hyphenate a domain boundary; a leaf
segment names what's different about it, never repeats an ancestor's name.**

Not a naming bug, confirmed during this review and intentionally left alone: `render-api` /
`scene:rendering` / `backend:vulkan`'s `Renderer.kt` are three different jobs at three
different layers (neutral contract → ECS-facing wiring → concrete GPU implementation), the
same relationship `physics:api` has with `backend:jolt`. The word "rendering" appearing three
times is the pattern working correctly, not duplication.

### Renames (Gradle module paths only — no Kotlin package changes here)

| Current path | New path |
|---|---|
| `awake:scene-dsl` | `awake:scene:dsl` |
| `awake:engine:ui:ui-core` | `awake:engine:ui:core` |
| `awake:engine:ui:ui-headless` | `awake:engine:ui:headless` |
| `awake:engine:ui:ui-designsystem` | `awake:engine:ui:designsystem` |
| `awake:engine:ui:ui-testing` | `awake:engine:ui:testing` |
| `awake:backend:vulkan-generator` | `awake:backend:vulkan:generator` |
| `awake:engine:render-api` | `awake:engine:render:api` |
| *(new)* | `awake:engine:app` — the `AwakeApplication` module; not `awake:X` since "awake" already names the whole project, and `app` states its actual job (assembles a runnable at the top of the graph) |

Scope decision: apply the **full table**, not a partial subset limited to what `AwakeApplication`
touches. Reasoning: this is a Gradle path rename only (every `include(...)` line in
`settings.gradle.kts`, every `project(":...")` reference), mechanically identical work whether 3
paths change or 8, and leaving `ui-core`/`ui-designsystem` inconsistent while fixing
`render-api` next to them would recreate exactly the "why is this one different" confusion that
started this conversation.

This table is the *rename* list — existing modules getting a new path. Part 3 below adds one
more new module (`awake:backend:vulkan:bindings`) that isn't a rename of anything, so it isn't
listed here; see Part 3 for it.

### Timing decision: land the module-path rename first, standalone

Two rename passes are coming — this Gradle-path rename, and the separate Kotlin-package rename
(`io.github.ronjunevaldoz` → `io.github.awakelab`, plus the `Shadcn*` preset renames, both
already tracked in memory as pre-publish work). They are mechanically independent: one touches
`settings.gradle.kts` and `build.gradle.kts` `project(...)` references, the other touches
`package`/`import` lines inside `.kt` files. Bundling them would double the size of a single
commit's diff for no shared risk reduction — a Gradle-path rename cannot conflict with a package
rename since they edit different kinds of lines. Land this one first, on its own, since it's
lower-risk (the build either resolves module paths or it doesn't — no partial-drift state is
possible the way a partially-renamed package can leave stale imports).

## Part 3 — Split `backend:vulkan` into bindings + engine

### The finding

`backend:vulkan` today is one module holding two genuinely different things fused together:
raw generated Vulkan API bindings, and Awake's own opinionated renderer built on top of them.
There is no way to depend on only the former — a consumer who wants raw Vulkan access to build
a *different* renderer (skip `GraphicsDevice`/`SwapchainManager`/`RenderPipeline`/
`VulkanGameApplication` entirely) gets the whole engine-opinionated stack or nothing. This
surfaced from asking "can we ship a pure Vulkan wrapper, no engine dependency?" — no, not today.

Sixteen top-level packages under `vulkan/`, classified by what they actually contain (verified,
not assumed — sizes and content checked directly):

| Package | Files | What it is |
|---|---|---|
| `gen/` | 4 | Generated `vkCreate*`/`vkDestroy*` binding calls |
| `handles/` | 1 (51 lines) | Raw Vulkan handle wrapper types |
| `models/` | 65 | Generated `Vk*CreateInfo`/`Vk*` struct models |
| `enums/` | 52 | Generated `Vk*` enum types |
| `vulkan/` (root) | — | `Vulkan` object, `VkArray`, `Version` — the raw API surface itself |
| `renderer/` | 7 | `Renderer`, `RendererDraw3D`, swapchain draw loop — Awake's opinionated renderer |
| `application/` | 1 | `VulkanGameApplication` — Awake's `GameApplication` actual |
| `pipeline/` | — | `RenderPipeline`, `ShadowRenderPipeline` — Awake's pipeline abstraction |
| `swapchain/` | — | `SwapchainManager` — Awake's swapchain lifecycle policy |
| `device/` | 1 (229 lines) | `GraphicsDevice` — Awake's own physical/logical device selection, validation-layer and extension choices. A decision, not a binding. |
| `commands/` | 1 | `TransferContext` — Awake's staging-buffer transfer abstraction |
| `material/`, `mesh/`, `texture/`, `debug/`, `ui/` | — | Awake's `render-api` contract implementations and UI glyph/quad pipelines |

The boundary is exactly `gen/` + `handles/` + `models/` + `enums/` + the raw `vulkan/`-root API
surface (what `vulkan-generator` produces or directly wraps) versus everything else (what Awake
built with those bindings). One direction only — the engine layer depends on the bindings
layer, never the reverse, so the split has no circularity risk.

### Target module structure

```
awake:backend:vulkan:bindings   NEW — raw generated API only
    gen/, handles/, models/, enums/, vulkan/ (root: Vulkan, VkArray, Version)
    depends on: nothing but Kotlin/Native cinterop + awake:base (Buffer/memory types)
    publishable standalone: a consumer wanting raw Vulkan access, no Awake renderer opinions

        │ depended on by
        ▼
awake:backend:vulkan            renamed target of today's module — Awake's renderer
    renderer/, application/, pipeline/, swapchain/, device/, commands/,
    material/, mesh/, texture/, debug/, ui/
    depends on: bindings (above), engine:render:api, engine:app (game/application layer)
    (the awake:scene dependency found in Part 1's investigation is dead -- removed here,
    not carried into either new module)

awake:backend:vulkan:android-native   UNCHANGED in content, moves to depend on bindings only
    Pure CMake/NDK Android library (bundled Vulkan validation-layer binaries + generated JNI
    stubs, no Kotlin plugin, no engine logic at all -- confirmed by reading its build.gradle.kts
    and content). Raw-binding-shaped, same category as gen/handles/models/enums. Today
    :backend:vulkan's androidMain depends on it directly; after the split that dependency edge
    moves to :backend:vulkan:bindings' androidMain, and the engine module reaches it only
    transitively through bindings, same as every other platform's raw-API access.
```

Under the Part 2 naming rule this reads correctly without a special case: `vulkan:bindings` is
colon-nested, states what's different about the leaf, and `vulkan-generator` becomes the
natural producer of `vulkan:bindings`' contents (worth revisiting whether `vulkan-generator`
should itself move under `vulkan:generator` and generate directly into `vulkan:bindings` — flag
for implementation time, not a decision this plan needs to force now).

Confirmed, not assumed: `vulkan-generator`'s `FileWriter.rootDir` (`awake/backend/vulkan-generator/.../tool/WriteFile.kt`)
is a caller-set variable, not a path hardcoded into `backend:vulkan`'s tree. Repointing
generation at `backend:vulkan:bindings/src/commonMain/...` needs zero generator-logic changes —
only the `rootDir` argument the build script passes it. The split's mechanical cost is exactly
what this plan estimates, not larger.

### The C++ side is a hybrid, and one naming claim in it is false

The Kotlin split has a mirror on the native side, verified the same way (read the generator's
actual entry point and import list, not assumed from file names):

- `src/main/cpp/vulkan-kotlin/*Accessor.cpp` / `*Mutator.cpp` (one pair per `Vk*` struct,
  matching `models/` 1:1) — **genuinely generator-produced.** `vulkan-generator/main.kt` imports
  every `models.*` type and runs `CppClassBuilder` against them. These move to
  `vulkan:bindings` alongside their Kotlin counterparts, and stay generator-owned there.
- `src/main/cpp/generated/*_jni.gen.cpp` (`VulkanWindow`, `VulkanBuffers`, `VulkanDescriptors`,
  `VulkanImages` — the `gen/` package's JNI glue) — **hand-written despite the `.gen.`
  filename.** `vulkan-generator/main.kt`'s import list never references any of these four
  types, and this session hand-edited `VulkanWindow_jni.gen.cpp` directly (adding
  `glfwSetCursorShape`) rather than running a generator task — there is no generator task that
  produces them. The `.gen.` suffix is a leftover from however the file was originally
  scaffolded and is actively misleading today.

Consequence for Part 3: these four hand-written JNI files move to `vulkan:bindings` **by hand**
(a file move + updated `#include` paths), not by re-running any generator — budget that as real
work, not a generator invocation. Separately, and not blocking the split: the `.gen.` naming
should eventually either come off (they're not generated) or become true (wire them into
`vulkan-generator` for real) — flagged here as a known-false name, not fixed by this plan.

## Sequencing

The bindings split (Part 3) lands *before* `AwakeApplication` is built: `AwakeApplication`'s
`actual` in `backend:vulkan` should depend on the split shape from the start rather than being
retrofitted onto it.

1. Module path renames (Part 2) — mechanical, `settings.gradle.kts` + every `project(":...")`
   reference, verified by `./gradlew build` staying green. Do this first: every module created
   or split in later steps gets its correct name from creation instead of being renamed again
   immediately after.
2. Split `backend:vulkan` into `backend:vulkan:bindings` + `backend:vulkan` (Part 3). Remove
   the dead `awake:scene` dependency in the same pass — it's touching the same
   `build.gradle.kts` regardless.
3. `WindowApplication`/`GameApplication` renames — two `class`/`interface` renames plus their
   call sites; small, mechanical, same-commit-safe with the earlier steps since all are
   non-behavioral.
4. `AwakeApplication` itself — new module, expect/actual, the design in Part 1. This is the one
   step with real design risk (the expect/actual parameter story); budget the most review time
   here.
5. Migrate all three samples off their `*Bootstrap.kt` files onto `AwakeApplication`.
6. Make `VulkanGameApplication`/`WebGpuGameApplication` internal; add the "no sample imports a
   backend directly" regression check from Part 1.

Each step should land as its own commit/PR, verified with a full `./gradlew build` before the
next starts — this plan does not assume background-agent parallelism the way the recent UI
component wave did, since every step here touches shared build configuration (`settings.gradle.kts`,
module `build.gradle.kts` files) that doesn't parallelize safely across concurrent agents.

## Explicitly out of scope for this plan

- The `ui-designsystem` → `ui-headless`-only import boundary (separate task doc).
- The Kotlin package rename (`io.github.ronjunevaldoz` → `io.github.awakelab`) and the
  `Shadcn*` preset renames — already decided, tracked separately, land after this.
- Any change to `WindowApplication`'s actual per-platform driving loop (GLFW/Activity/
  UIViewController/`requestAnimationFrame`) — this plan only renames and layers the existing
  contract, it does not touch how each platform drives it.
