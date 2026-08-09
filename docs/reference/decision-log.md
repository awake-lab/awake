# Decision Log

Historical "why we chose X" rationale for Awake's engine architecture, extracted from
`docs/MVP_PLAN.md` (2026-08-06) to keep that document focused on live planning --
vision, current state, phases, timeline, risks. Entries here are append-only; a
decision that gets revisited gets a dated addendum in its own entry, not a rewrite.

### D1 — ECS: Fleks vs. custom
**Decided: custom sparse-set ECS for MVP** (revised 2026-07-09). Fleks was considered and
kept only as a benchmark dependency in `:awake:ecs:benchmark`; the engine runtime owns its
ECS layer for the same reason it owns Vulkan bindings, JNI generation, and math. The chosen
architecture is one sparse-set per component type, not archetype tables. If future tasks
need library-grade features such as complex boolean family queries, archetype migration, or
multi-threaded scheduling, treat that as scope growth and revisit the decision instead of
silently adding a general-purpose ECS library.

### D2 — Compose-style scene API
**Correction (2026-08-03):** this entry was left marked "OPEN — under discussion" after the
decision had already shipped in code. **Decided: option 2, custom Kotlin DSL.** Real,
tested modules exist: `awake:scene:authoring` (`AwakeSceneDsl.kt`, `SceneNodeDsl.kt`,
`SceneTransformDsl.kt`, `SceneCameraDsl.kt`, `SceneLightDsl.kt`, `SceneDocumentDsl.kt` —
`sceneGame { ... }` builder syntax over `awake:scene`) and `awake:engine:game-authoring`
(`game { ... }`/`gameSpec { ... }` entrypoint over `awake:engine:game`). No Compose runtime
dependency, no incremental recomposition — matches option 2's known tradeoff exactly
(rebuild/manual diff, not diffing). See Phase 7 above for the checklist reconciliation.

**OPEN — under discussion.** Options:
1. **Reuse the Compose runtime** (`compose.runtime` only, no UI): custom `Applier` that
   materializes the composition into ECS entities. True `@Composable` scenes, `remember`/
   state/recomposition for free. Precedent: Compose for Web/Canvas, JetBrains' non-UI appliers.
2. **Custom Kotlin DSL** (builder-style, no Compose dependency): simpler, no runtime magic,
   but no incremental recomposition — rebuild or manual diff.
3. **Defer entirely**: ECS-first API for MVP, declarative layer as post-MVP sugar.

### D3 — kmm-skills project/agent setup
**OPEN — under discussion.** This is an existing 2023-era repo, not a fresh scaffold, so
`kmm-new-project`/feature-scaffold don't apply wholesale. Candidate selective adoption:
`kmm-setup-agents` + `kmm-setup-hooks` (repo agent config), code-quality skill (Ktlint/Detekt
gates), CI skill (workflow YAML), docs-maintainer + lessons capture.

### D6 — Fate of the OpenGL backend
**OPEN.** `awake-core`'s OpenGL wrapper is the only stable, shipped functionality today.
Options: (a) freeze it — bugfixes only, Vulkan is the future; (b) keep it as a fallback
backend behind the Phase 2 renderer abstraction (real cost: every renderer feature ×2);
(c) delete post-MVP. Recommendation: **(a) freeze now, decide (b) vs (c) after MVP.**
Maintaining two backends during the rewrite would roughly double Phase 2.

### D7 — Web target (Wasm/JS)
**DECIDED (2026-07-08): in scope — see Phase 2.5.** Vulkan does not exist in browsers, so
this means a real **WebGPU** backend behind the Phase 2 renderer abstraction seam, plus a
`wasmJs` Kotlin target (Fleks and the kotlinx libraries already used elsewhere in this
project already support Wasm). Deliberately sequenced *after* Phase 2's core abstraction
(`GraphicsDevice`/`SwapchainManager`/`Mesh`/`Material`) exists, not in parallel with it —
writing a WebGPU backend against an abstraction that's still being designed would mean
writing it twice.

### D8 — Audio
**OPEN (post-MVP phase, needs a slot).** No audio anywhere in the plan. Candidate:
**OpenAL Soft** (LGPL, industry standard) — LWJGL already ships bindings for desktop;
Android/iOS build it via the same native pipeline. Alternative: platform-native
(AAudio/AVAudioEngine) behind expect/actual — more work, less consistency.
Recommendation: OpenAL Soft as Phase 9, after physics.

### D9 — Versioning during the rewrite
**OPEN.** `awake-core`/`awake-vulkan` are published artifacts with users (snapshots).
Recommendation: current `main`/1.x goes maintenance-only; engine rework publishes as
**2.0.0-alpha** snapshots from day one (Phase 0), so breaking changes are free until 2.0.0.

### D10 — Codegen path for Vulkan structs
**DECIDED (2026-07-08): option (a) — jni-binding-generator.** Round 1 de-risk (2026-07-07)
found the tool had no struct/data-class marshalling concept and silently miscompiled bare
struct params via a naive enum heuristic. Rather than fall back to keeping
`awake-vulkan-generator` (option b) or a hybrid split (option c), the gaps were fixed
directly in jni-binding-generator itself, generically (not Vulkan-specific), across two
more rounds — round 2 found three further gaps after the initial fix landed, round 3 fixed
all three and re-verified clean against the real Awake source (all fields/functions parse
correctly; generated C++ compiles against real JNI headers; tool's own 259-test suite,
compile-check, and drift checks all pass). Full history:
[decisions/D10-codegen-derisk-findings.md](decisions/D10-codegen-derisk-findings.md).
`awake-vulkan-generator` retirement (originally a Phase 1a checklist item) is back on the
table now that jni-binding-generator covers real struct marshalling end-to-end.

### D11 — Split `awake-core` into a dependency-free `awake-base` module
**DECIDED (2026-07-10): done.** `awake-core`'s `commonMain` forced every consumer to pull in
Compose UI and the entire `:awake-vulkan` native-binding graph just to reach portable math/
input/glTF-parsing code, which would have blocked any headless consumer (e.g. a future
physics-server module) from depending on the engine's math layer alone. Confirmed via a full
read of every file in `awake-core` and a grep of every consumer that no circular dependency
existed, only 3 files pulled in `vulkan.*` (`Renderer.kt`, `DrawCall.kt`,
`VulkanTextureLoader.kt`), and only 1 file pulled in Compose (`glExt.kt`, deep in the legacy
OpenGL path). New module `:awake-base` (modeled on `:awake-ecs`'s already-lean template) now
holds math, `Input`, `FixedTimestepLoop`, glTF parsing, and bitmap/resource I/O — moved with
package names unchanged (`io.github.ronjunevaldoz.awake.core.*`), so `awake-core`'s
`api(project(":awake-base"))` re-export needed zero downstream import changes. The 3
Vulkan-coupled files moved (and were repackaged) into `:awake-vulkan` itself, which now
depends on `:awake-base` for `Camera`/`Mat4`; `awake-core` dropped its `:awake-vulkan`
dependency entirely. `Context`/`Config`/`GameLoop` deliberately stayed untouched (still
OpenGL-FPS-coupled) — out of scope for this pass, since a headless consumer can drive
`FixedTimestepLoop.advance(...)` from its own tick source without needing a render loop at
all.

**Follow-up (2026-07-10): the OpenGL-FPS coupling above is now resolved too.** A second
pass split `awake-core` again — this time pulling the entire legacy OpenGL stack
(`graphics/opengl/*`, `rendering/*`, `shader/*`, `fonts/*`, `geometry/Attribute.kt`,
`utils/AssetUtils.kt`/`BufferUtils.kt`/`BitmapUtils.kt`, `Context.kt`, `graphics/Config.kt`,
the desktop `createFrame`) into a new peer module, `awake-opengl` (same "keep package names
unchanged" trick as `awake-base`, so zero consumer import changes). What's left in
`awake-core` is genuinely backend-agnostic app-lifecycle glue: `Application`, `GameLoop` +
its 3 platform actuals, `VulkanView` (confirmed via read to import nothing Vulkan-specific
despite the name — just a generic Android surface/render-thread helper), `Greeting`.
Compose and lwjgl are no longer `awake-core` dependencies at all.

### D12 — Extract `awake-engine-render-api`: a real backend-neutral module

**DECIDED (2026-07-10): slice 1 done.** User proposed a target module shape (`core/ecs`,
`core/scene`, `core/engine`, `api/engine-render-api`, `backend/{vulkan,opengl,webgpu}`,
flat directory names per this repo's `awake-*` convention, not physically nested folders).
Usage analysis (grep across `awake-scene`/`awake-demo`) found `RenderSystem` only ever
touches `DrawCall`/`Renderer` from `awake-vulkan`'s Phase 2.5 `expect` seam — never the raw
Vulkan bindings, and never `GraphicsDevice`/`SwapchainManager`/`RenderPipeline`/`Texture`/
`TransferContext` (those are only constructed by `VulkanApplication.kt`'s own backend-
specific bootstrap). `awake-scene` was depending on all of `awake-vulkan` via `api()` just
to reach two types.

**Real architectural constraint found mid-implementation**: `VulkanApplication.kt` is one
*common* Kotlin file compiling for desktop/Android/iOS/wasmJs simultaneously, constructing
`RenderPipeline(...)`/`Mesh(...)`/etc. as plain constructor calls — this only works because
`expect class Foo(...)` + per-platform `actual class Foo(...)` let one shared call resolve
polymorphically, and **`expect`/`actual` only resolves within a single Gradle module's own
target graph** (you cannot declare `expect` in one module and `actual` in a different one).
Converting these to plain interfaces in a new module would have broken that construction
pattern — plain interfaces have no constructors, so there's no "same call, different
implementation per platform" left; fixing that properly would mean splitting
`VulkanApplication.kt` per-platform, a much larger change than what was asked for.

**Resolution**: `expect class Foo(...) : io.github.ronjunevaldoz.awake.render.foo.Foo { }`
is fully valid Kotlin — an `expect` class can implement an interface declared in a
*different* module, and Kotlin requires only the `actual` implementations (already in
`awake-vulkan`'s `vulkanMain`/`wasmJsMain`) to also declare the same supertype. This means
`VulkanApplication.kt`'s exact existing construction pattern needed **zero changes**.

**Scope, deliberately narrow** — only `Mesh`, `Material`, `Renderer` (+ the `DrawCall` data
class) moved into the new `awake-engine-render-api` module, as interfaces exposing only
what's read across the module boundary (`bind()`/`draw()`/`destroy()`/
`updateUniformBuffer()` — confirmed via grep, not `vertexBuffer`/`indexBuffer`/`indexCount`/
etc., which only same-backend code ever touches). `GraphicsDevice`/`SwapchainManager`/
`RenderPipeline`/`Texture`/`TransferContext` stay exactly as they are in `awake-vulkan`,
untouched — nothing outside `awake-vulkan` needs them. Also deliberately NOT moved:
`VkFormat` (a 356-entry enum mirroring the entire Vulkan spec, which `SwapchainManager`'s
now-untouched `imageFormat` property uses) — dragging Vulkan's entire format enum into a
"neutral" API module to satisfy one field with zero external readers would have been a
real mistake, not just extra work.

`awake-vulkan` now `api()`-depends on `awake-engine-render-api`; `awake-scene` now depends
on `awake-engine-render-api` directly instead of all of `awake-vulkan`. Verified zero
behavior change: all 5 `awake-vulkan` targets + `awake-engine-render-api`'s 5 targets +
`awake-scene` compile clean, `desktopTest` (lavapipe Vulkan suite) passes, and a real iOS
Simulator run renders the demo cube identically to before this change.

**Explicitly deferred** (noted here so a future session doesn't lose them): physically
splitting `awake-vulkan` into `awake-backend-vulkan` (raw bindings + Vulkan actuals) and
`awake-backend-webgpu` (wasmJs actuals) as separate Gradle modules — real work, since
`VulkanApplication.kt`'s per-platform construction pattern would need to split too (see the
constraint above); renaming `awake-opengl` → `awake-backend-opengl`; renaming `awake-core`
→ `awake-engine` (note: `awake-scene` currently sits *above*, not below, the
`awake-core`/`awake-base` loop/config layer in the real dependency graph — the opposite of
what the user's original diagram assumed — worth confirming before this rename).

The one real blocker the first pass explicitly deferred — `GameLoop` actuals reading
`AwakeContext.config.fps`, which would have meant `awake-core` depending on the
OpenGL-specific `Context`/`Config` it no longer contains — is fixed by a new
`EngineConfig`/`EngineConfigHolder` (`awake-core/.../application/EngineConfig.kt`): a
backend-agnostic fps/ups holder. `awake-opengl`'s `AwakeContext.init()` mirrors its
resolved `Config.fps`/`Config.ups` into this holder, so every existing
`AwakeContext.init { fps = X }` call site keeps working with zero changes, while
`awake-core`'s `GameLoop` actuals now read `EngineConfigHolder.config.fps` and have no
OpenGL dependency whatsoever. `awake-scene` and `awake-ecs-benchmark` — which only ever
needed `awake-core` for math — now depend on `awake-base` directly instead, dropping
Compose/OpenGL from their transitive graph entirely. Verified: all modules compile
(desktop + Android), `awake-base`/`awake-scene` test suites pass, and the real GLFW+Vulkan
desktop demo (`runVulkanDesktop`) still runs correctly end-to-end.

### D13 — Module restructuring slice 2: physically split `awake-vulkan` into `awake-backend-vulkan` + `awake-backend-webgpu`

**DECIDED (2026-07-11): done.** D12 slice 1 deferred the physical split because
`VulkanApplication.kt` is one common Kotlin file targeting desktop/Android/iOS
simultaneously — the concern was that splitting the backend would force splitting that
file per-platform too. Investigation found the concern didn't apply: `awake-demo:shared`
never declares a `wasmJs` target at all (only android/desktop/iosArm64/
iosSimulatorArm64), so `VulkanApplication.kt` never needs to compile against the webgpu
backend and needed zero structural change — just a `project(":awake-vulkan")` →
`project(":awake-backend-vulkan")` dependency-coordinate update.

**A second finding simplified the code itself**: the 8 renderer-type `expect class`
declarations (`GraphicsDevice`/`SwapchainManager`/`RenderPipeline`/`Mesh`/`Material`/
`Texture`/`TransferContext`/`Renderer`) only needed `expect`/`actual` because wasmJs was a
*sibling target in the same module* as desktop/Android/iOS. Their `vulkanMain` body was
already a single implementation shared identically across desktop/Android/iOS. Once wasmJs
moved to its own module, all 8 became plain classes directly in each new module's
`commonMain` — no more `actual constructor` ceremony, no more custom `vulkanMain` source
set, no more explicit `applyDefaultHierarchyTemplate()` workaround (that call existed
solely to counteract the custom `vulkanMain` `dependsOn()` edge, which no longer exists).

**Dead code confirmed and dropped, not carried forward**: `awake-vulkan/src/wasmJsMain`'s
`gen/`, `Vulkan.kt`, `VulkanSurface.kt` (raw Vulkan-binding `TODO()` stubs, never called by
the real WebGPU code) and `webgpu/WebGpuSpike.kt` (the milestone-2 spike, unreferenced
anywhere per repo-wide grep). `SwapchainManager`'s `imageFormat: VkFormat` field (dead —
grep confirmed nothing reads it, `imageFormatWebGpu` was always the real one) and `extent:
VkExtent2D` (also confirmed dead on closer inspection — despite its own doc comment
claiming `RenderPipeline`/`Renderer` read it, they don't) were both dropped from the new
`awake-backend-webgpu` module.

**Package naming**: `awake-backend-vulkan` keeps the `io.github.ronjunevaldoz.awake.vulkan`
package unchanged (only the Gradle module id changed) — matches `awake-engine-render-api`'s
existing precedent (module id ≠ package root) and meant zero import changes anywhere.
`awake-backend-webgpu`'s moved files were repackaged to `io.github.ronjunevaldoz.awake.webgpu`
— safe since grep confirmed no file outside the old `awake-vulkan` module ever imported a
wasmJs-specific symbol. Its own tiny `handles/Handles.kt` (9 `@JvmInline value class`
wrappers) is a local copy, not a dependency on `awake-backend-vulkan`, to keep the two
backends genuinely independent (the whole point of the split). Note: `@JvmInline` value
classes must live in `commonMain`, not a platform-specific source set, even for a
single-target module — first attempt placed the copy in `wasmJsMain` and got
`Declaration annotated with '@OptionalExpectation' can only be used in common module
sources`.

Verified: `awake-backend-vulkan` compiles on desktop/Android/iosArm64/iosSimulatorArm64,
`desktopTest` (real lavapipe Vulkan smoke suite) passes, `awake-backend-webgpu` compiles on
wasmJs, `awake-ecs-benchmark` and `awake-vulkan-generator` (its hardcoded C++ output path
and `project()` coordinate updated) both compile against the renamed module. A real iOS
Simulator run (after also fixing a hardcoded MoltenVK library search path in
`iosApp.xcodeproj/project.pbxproj`, which `git mv` doesn't touch) renders the demo cube
identically to before the split. The WebGPU backend's code path was verified indirectly —
compiles clean, runs a full draw loop with zero exceptions and zero WebGPU validation
errors, and produces a valid non-degenerate MVP matrix — but a temporary browser
screenshot check was inconclusive: even a raw hand-written JS WebGPU triangle (bypassing
Kotlin entirely) drawn on the same canvas in the same sandboxed browser tool failed to show
a visible triangle, only its clear color, indicating an environment/screenshot-pipeline
quirk in that tool rather than a defect in the ported code.

**Still deferred** (unchanged from D12): renaming `awake-opengl` → `awake-backend-opengl`
(OpenGL doesn't implement `awake-engine-render-api` at all yet, so this rename would imply
parity it doesn't have); renaming `awake-core` → `awake-engine` (blocked on resolving the
`awake-scene` vs `awake-core` dependency-direction mismatch vs. the original diagram); real
`Material`/`Texture` WebGPU implementation (still `TODO()`); a real `wasmJs` target on
`awake-demo:shared` to run the actual demo scene on web.

### D14 — Web demo: async resource loading + a real `wasmJs` target on `awake-demo:shared`

**DECIDED (2026-07-11): done.** D13's last deferred item. Four real forks surfaced during
planning, each resolved with the user before implementation:

1. **Compose Multiplatform's `wasmJs` target has no first-class API to embed a native
   `<canvas>` inside the Compose layout tree** (confirmed via web search — only community
   CSS-overlay/"punch-hole" hacks exist). Resolution: the web demo is a bare-canvas
   `requestAnimationFrame` loop (`awake-demo/shared/src/wasmJsMain/kotlin/main.kt`) that
   does not go through `App()`/`DemoScene()`/`AwakeCanvas` at all — no FPS counter/
   Vulkan-toggle chrome on web.
2. **`readResourceBytes`** (used by `SceneLoader.loadFromResource` to load
   `scenes/mvp.scene.json`) **was a synchronous-signature `TODO()` stub on wasmJs** —
   browser resource loading is inherently async (`fetch()`). Fixed properly: the `expect`
   declaration and all 5 platform actuals became `suspend` (4 of 5 needed no body change;
   wasmJs got a real `kotlinx-browser`-based `window.fetch()` implementation — new
   dependency, plus `kotlinx-coroutines-core`, neither previously in
   `gradle/libs.versions.toml`).
3. **The `suspend` conversion cascades to every caller** — confirmed via grep this reaches
   `awake-opengl`'s `TextureLoader.load`/`SimpleShader` and all 6 legacy OpenGL demo scene
   classes (`TransformTriangle`, `DemoTexture`, `DemoColoredTriangle`, `DemoTriangle`,
   `FontBitmapSample`, `CubeSample`), none of which relate to WebGPU. Did the full cascade
   rather than a parallel web-only API. `SimpleShader` gained a `suspend fun create(...)`
   factory that pre-loads source text into `private val` fields — `Shader`/`BaseShader
   .compile()`'s own contract stayed fully synchronous and untouched. Each demo scene
   class's `private val shader = SimpleShader(...)` + `init {}` (shader compile + texture
   load) became `private lateinit var shader` + a new `suspend fun load()`.
4. **`Application.create(surface: Any?)` stayed synchronous everywhere** rather than
   becoming `suspend` — it's called from platform lifecycle callbacks that can't
   (`VulkanView.surfaceCreated` on Android, `VulkanMetalView.layoutSubviews` on iOS, a
   plain `fun main()` on desktop). Every implementation that needs `suspend` work
   (`VulkanApplication`, the new `WebGpuApplication`, `DemoApplication`) launches it
   internally via `MainScope().launch { }` instead. Real correctness bug caught during
   this: since `create()` now returns before setup finishes, the platform render loop can
   call `update()` before the `lateinit` scene-host field is initialized — fixed with an
   `isReady` guard in each implementation's `update()`/`dispose()`.

`SceneRuntimeHost` (used by every platform, including the new wasmJs one) needed two
changes to become genuinely shared: its `Renderer` import switched from the concrete
`io.github.ronjunevaldoz.awake.vulkan.renderer.Renderer` to the backend-neutral
`io.github.ronjunevaldoz.awake.render.renderer.Renderer` interface (a pre-existing bug —
`RenderSystem` already expected the interface), and its constructor became `private` +
a `companion object suspend fun create(...)` factory, since `SceneLoader.loadFromResource`
is now `suspend` and Kotlin forbids `suspend` calls inside `init {}`/property initializers.

**New `appMain` intermediate source set** in `awake-demo/shared` (mirrors the exact
`vulkanMain`-style pattern D13 just removed from `awake-backend-vulkan`, needed here one
layer up for a different reason): `App.kt`, `AwakeCanvas.kt` (the `expect fun`),
`demo/DemoScene.kt`, `demo/DemoApplication.kt`, `demo/DemoDrawer.kt`,
`demo/VulkanApplication.kt`, and `scene/*.kt` (the 6 OpenGL demo classes) all depend on
`awake-backend-vulkan` and/or `awake-opengl`, neither of which publishes a wasmJs variant
by design — a `commonMain.dependencies` entry on either fails Gradle dependency resolution
the moment wasmJs becomes a declared target, not just compilation. These moved into
`appMain` (`dependsOn(commonMain)`, desktop/Android/iOS `dependsOn(appMain)`);
`demo/SceneRuntimeHost.kt` is the one file that stayed in true `commonMain`.

**A wrong assumption surfaced mid-implementation**: `awake-scene`/`awake-core`/`awake-ecs`
were assumed to already support wasmJs (since `awake-engine-render-api`/`awake-base` do) —
they didn't; none had declared a `wasmJs` target at all. All three needed one added.
`awake-ecs`'s `Platform.kt` (`newComponentArray`/`createComponentInstance`/
`componentTypeKey`/`componentTypeKeyOf`) needed a real wasmJs actual — mirrors the iOS
actual exactly (no `java.lang.Class` on Kotlin/Wasm either, so there's nothing to optimize,
same as Kotlin/Native). `awake-core`/`awake-scene` had zero `expect` declarations, so
adding the target was a pure Gradle change, no new Kotlin files.

**`WebGpuApplication.kt`** (`awake-demo/shared/src/wasmJsMain/kotlin/demo/`) mirrors
`VulkanApplication.kt`'s structure (same cube geometry, `MAX_FRAMES_IN_FLIGHT`,
`resolveRenderable` mesh="cube"/material="textured-default" contract) on
`awake-backend-webgpu`'s types. `Material`/`Texture` are not meaningfully used — `Material`
is constructed only as a placeholder to satisfy `MeshRenderer`'s type (its real methods are
still `TODO()` on this backend, and `Renderer.draw()` never touches `DrawCall.material`,
confirmed this session's browser verification); `Texture` isn't constructed at all.
`main.kt` resolves the `WGPUContext` (`canvasContextRenderer()` + `surface.configure()`,
both `suspend`) before calling `WebGpuApplication.create()`, matching `GraphicsDevice`'s
existing doc-commented expectation that its `window` parameter is a pre-resolved context
on this backend, then drives a plain `window.requestAnimationFrame` loop.

**Verified end-to-end, not just compiled**: all 5 `awake-demo:shared` targets compile;
`awake-scene`/`awake-base` test suites pass (including the now-`runTest`-wrapped
`SceneLoaderTest`); a real iOS Simulator run renders the demo cube identically to before
(confirming the suspend restructuring changed no runtime behavior on the 4 existing
platforms); and — the actual payoff — a real browser run of the wasmJs bundle
(`wasmJsBrowserDevelopmentRun`) renders the real RGB cube loaded from `mvp.scene.json` via
genuine async `fetch()`, through the same `SceneRuntimeHost`/`RenderSystem`/ECS pipeline
every other platform uses, animating frame-to-frame via `requestAnimationFrame` — screenshot-
confirmed rotating between two captures two seconds apart. Unlike D13's WebGPU check, this
one's screenshot pipeline worked cleanly (via a different browser tool than the sandboxed
one that failed on even a raw-JS triangle in D13) — no unresolved concerns here.

**Deferred, not this slice**: real `Material`/`Texture` WebGPU implementation (still
`TODO()`, so no texture sampling/multi-material support on web yet); `awake-opengl` →
`awake-backend-opengl` and `awake-core` → `awake-engine` renames (unchanged blockers from
D12/D13); Compose UI chrome on web (would require the CSS-overlay canvas-embedding hack
explicitly declined this slice).

### D15 — `awake-core` → `awake-engine` rename

**DECIDED (2026-07-11): done.** D12/D13/D14 all deferred this rename on the same stated
blocker: the user's original diagram had `core/engine` depending on `core/scene` (engine
sits above scene, orchestrating it), but the real graph never matched — confirmed via
`Explore`: `awake-core` only ever depended on `awake-base`, never `awake-scene`, and
`awake-scene` never depended on `awake-core` either. They're siblings, not layered.

**Why this turned out not to be a real blocker**: `awake-core` is nearly empty today —
just the `Application`/`GameLoop` interfaces and `EngineConfig` (fps/ups holder). Nearly
everything substantial that used to live there (math, `Input`/`Key`, `FixedTimestepLoop`,
`Bitmap`, glTF parsing) already migrated to `awake-base` back in D11. The orchestration
logic that *would* create a real engine→scene dependency — driving a fixed-timestep loop
that reads input and steps an ECS scene graph — is `demo/SceneRuntimeHost.kt`, and it
lives in `awake-demo` (the app layer), not in `awake-core`. So the diagram's assumed
dependency was aspirational for a "real orchestrating engine" module that was never
actually built here; what got built instead is a thin, independent lifecycle-interface
layer that doesn't need `awake-scene` for anything.

**Resolution**: renamed `awake-core` → `awake-engine` as a plain Gradle module id +
directory rename (`git mv`), no dependency changes — matches the precedent already set by
`awake-engine-render-api` (module id ≠ Kotlin package root is fine) and `awake-backend-
vulkan` (D13): package name `io.github.ronjunevaldoz.awake.core` and the iOS framework
`baseName = "awake-core"` were left unchanged, since neither is tied to any native
toolchain or external consumer that would break, and touching them isn't necessary to fix
the actual issue (the module id/name). Updated all 3 consumers'
`project(":awake-core")` → `project(":awake-engine")`: `awake-opengl`, `awake-demo:shared`,
`awake-demo:desktopApp`.

**Explicitly not done, and not planned**: building out `awake-engine` into a real
scene-orchestrating layer (moving `SceneRuntimeHost`-style logic out of `awake-demo` into
it, so it would actually depend on `awake-scene` and match the original diagram literally)
— this would be a real, separate feature (generalizing app-specific demo orchestration
into a reusable engine API), not a naming fix, and wasn't asked for.

**Verified**: `awake-engine` compiles on all 5 targets (desktop/Android/iosArm64/
iosSimulatorArm64/wasmJs); all 3 consumers (`awake-opengl`, `awake-demo:shared` including
its wasmJs target, `awake-demo:desktopApp`) compile clean.

### D16 — Reusable-Application gap: `VulkanGameApplication`/`WebGpuGameApplication`

**DECIDED (2026-07-11): done.** While scoping a "sample project" to show a new consumer how
to build a game on Awake, confirmed a real gap: `awake-demo`'s `VulkanApplication.kt`/
`WebGpuApplication.kt` each hand-rolled ~150–230 lines of near-identical engine bootstrap
(`GraphicsDevice` → `SwapchainManager` → `RenderPipeline` → `TransferContext`/`Texture`/
`Material` → `Mesh`(es) → scene loading → `Renderer`), differing only in concrete backend
types and a few real omissions (WebGPU skips `TransferContext`/`Texture`/
`Material.createResources` entirely). A sample built against that starting point would just
be another copy of the same boilerplate — not simpler, just smaller in scope.

**Resolution**: extracted `VulkanGameApplication` (`awake-backend-vulkan`) and
`WebGpuGameApplication` (`awake-backend-webgpu`), each owning the full generic bootstrap
plus scene loading/`TransformSystem`/`RenderSystem` wiring. A game supplies mesh geometry
(new `MeshGeometry`/`TextureAsset` data types in `awake-engine-render-api`, same neutral
role `DrawCall` already plays), an optional texture (`null` binds a built-in 1x1 white
placeholder, so the shader/descriptor-set contract doesn't need a second no-texture
variant), and a scene path via the constructor. Game-specific logic hooks in via three
`open` methods: `onSceneReady()` (resolve entities once the scene loads),
`onFixedUpdate()`/`onRender()` (per-frame, call `super` first to keep the generic systems
running). `awake-demo`'s own `SceneRuntimeHost` was trimmed to only the parts genuinely
specific to that demo (player/camera/NPC resolution, their systems) — generic scene
loading/`TransformSystem`/`RenderSystem` moved into the base classes.

**Relationship to D15**: D15 explicitly deferred "building `awake-engine` out into a real
scene-orchestrating layer" as a separate, unrequested feature. This is that feature, now
requested — but it landed in `awake-backend-vulkan`/`awake-backend-webgpu`, not
`awake-engine` itself, since the bootstrap is inherently backend-specific (concrete
`GraphicsDevice`/`RenderPipeline`/etc. types differ per backend); `awake-engine` still only
holds the backend-neutral `Application` interface and fixed-timestep loop.

**Verified**: retrofitting `awake-demo`'s two `Application` classes onto the new base
classes shrinks `VulkanApplication.kt` from 229 lines to ~90 (mostly geometry data, no
bootstrap code) — real proof, not just a claim. All 5 targets compile clean; a real wasmJs
browser screenshot is pixel-identical to the pre-refactor render (confirming this was a
pure extraction); `awake-scene`'s test suite is unaffected. New
[`sample-hello-cube`](../sample-hello-cube) module (a single static cube, no texture)
compiles and its desktop process boots without crashing, demonstrating the base class is
genuinely reusable outside `awake-demo` — visual screenshot confirmation of that specific
module wasn't done (no reliable tool for its unlisted native GLFW window in this session).

**D16 follow-up (2026-07-11): `sample-hello-cube` extended to all 4 platforms.** Proves the
base-class extraction above is reusable beyond desktop/wasmJs specifically:

- **Android**: new `samples:hello-cube:androidApp` module — plain `com.android.application`
  (not KMP), a bare `Activity` calling
  `setContentView(VulkanView(this, SampleApplication()))`, no Compose at all (simpler than
  `awake-demo:androidApp`'s own Activity, since `VulkanView` already accepts an
  `Application` instance directly). Required promoting `sample-hello-cube`'s
  `awake-engine`/`awake-backend-vulkan` deps from `implementation` to `api`, scoped to
  `androidMain` only (not the shared `appMain` source set) — see the iOS note below for why
  scoping mattered.
- **iOS**: first plain-`UIViewController`-hosting-`VulkanMetalView` pattern in this repo
  (`sample-hello-cube/src/iosMain/kotlin/main.ios.kt`'s `makeSampleViewController()`) —
  `awake-demo` always goes through Compose (`ComposeUIViewController` wrapping
  `AwakeCanvas.kt`'s `UIKitView`); this sample has no UI chrome, so it wires the same five
  `VulkanMetalView` lifecycle lambdas directly. `sample-hello-cube/iosApp` was `cp -R`'d
  from `awake-demo/iosApp` (per this doc's own standing rule against hand-authoring
  `project.pbxproj`), then only file *contents* were repointed: `Package.swift`'s
  `binaryTarget` path/framework name, and the two resource-folder `PBXFileReference` paths
  (`../shared/src/commonMain/resources/...` → `../src/.../resources/...`, since
  `sample-hello-cube` has no nested `shared/` module). `ContentView.swift` imports the
  XCFramework's own compiled module name (`Sample`, from `baseName = "Sample"`) even though
  the wrapping SPM package/product is still named `Shared` (cosmetic) — the binaryTarget
  just wraps the binary as-is, so Swift resolves the framework's actual module name on
  import, not the package's declared name.
- **Bug found + fixed while verifying iOS**: promoting `sample-hello-cube`'s deps to `api`
  project-wide (not scoped to `androidMain`) caused `awake-ecs`'s `World` class to leak into
  the iOS XCFramework's generated Objective-C header, which exposed a **pre-existing,
  previously-latent bug**: `World`'s two `inline reified` `family()` overloads (1-type and
  2-type) both erase to the same Objective-C selector (`- (id)family`), causing
  "duplicate declaration of method 'family'" — a genuine compile blocker, confirmed to
  affect `awake-demo`'s own `iosApp` identically once its XCFramework is rebuilt fresh
  (stale cached builds had been masking it). Fixed in `awake-ecs/World.kt` with
  `@ObjCName("family1")`/`@ObjCName("family2")` on the two overloads — a 4-line fix,
  unrelated in scope to this platform-extension work but blocking it entirely, so it
  landed here rather than as a separate deferred item.
- **Verified**: `androidApp:assembleDebug` succeeds (no device/emulator connected, so
  build-only, not visual — same limitation already flagged for task #85 elsewhere in this
  doc). iOS: `assembleSampleDebugXCFramework` + `xcodebuild build` against a concrete
  simulator device (`generic/platform=iOS Simulator` hits an unrelated missing-x86_64-slice
  issue that also pre-exists for `awake-demo`, since the XCFramework only registers an
  arm64 simulator slice — a concrete-device destination sidesteps it) both succeed, for
  `sample-hello-cube` and, after the `awake-ecs` fix, `awake-demo` too. `awake-scene`'s
  desktop test suite is unaffected by the `awake-ecs` change.

### D17 — Custom immediate-mode UI (Phase A: colored quads), not ImGui/Nuklear

**Decided (2026-07-11): build a small custom immediate-mode UI in `commonMain` rather than
bind ImGui or Nuklear.** The next planned tool (a model-viewer + camera/frustum "catalog"
debug overlay) needs some UI — buttons, toggles, a dropdown. ImGui/Nuklear are mature but
require real per-platform cinterop/JNI binding work to reach Vulkan (desktop/Android/iOS)
and would never reach wasmJs without a separate Emscripten story — the same cost class as
the Jolt physics binding already deferred to MVP1b. Matches this repo's established pattern
of avoiding native-binding cost when possible (own ECS instead of Fleks, `kbox2d` over a
native 2D physics binding for cross-platform reach). Not ECS-backed (widgets have no
persistent gameplay state — forcing them through `World`/family queries would mean
rebuilding a second Transform-hierarchy system just for 2D screen space) and not
declarative/Compose-style (no composer/recomposer/slot-table runtime needed for ~5 widget
types) — plain immediate-mode, matching ImGui's own actual architecture.

**Phase A scope (this slice): colored quads only, no text.** Both Vulkan and WebGPU
backends. Bitmap-font text (Phase B) and the actual model-viewer/camera-catalog feature
this infra exists for are explicit, separate follow-ups.

- New `awake-engine-ui` module (mirrors `awake-engine-render-api`'s backend-neutral-facade
  role): `UiContext` (the immediate-mode API — `beginFrame`/`button`/`toggle`/`dropdown`/
  `endFrame`, ImGui's classic hot/active-id click model), `UiDrawPrimitive` (currently just
  `Quad`). Depends only on `awake-base` (for `Input`).
- `Renderer` (`awake-engine-render-api`) gains a second interface method,
  `drawUi(primitives: List<UiDrawPrimitive>)`, alongside the existing `draw(camera,
  drawCalls)` — a real interface-widening change, but there are exactly two implementers,
  both in this repo, both updated in this slice.
- **Vulkan**: new `DynamicMesh` (HOST_VISIBLE, rewritten every frame via
  `writeBufferMemoryFloats`/`Bytes` — a different lifecycle contract than `Mesh`'s
  DEVICE_LOCAL one-time upload, so a separate class, not a bolted-on `update()`) and
  `UiRenderPipeline` (its own render pass — single color attachment, `loadOp = LOAD` so it
  composites on top of the 3D pass's output, `blendEnable = true`, no depth). The 3D pass's
  `finalLayout` changed from `PRESENT_SRC_KHR` to `COLOR_ATTACHMENT_OPTIMAL` so the UI pass
  can pick up from there and do the final present-layout transition itself.
  `Renderer.recordCommandBuffer` appends the UI pass to the *same* command buffer, after the
  3D pass — `drawUi()` only stages this frame's primitives into `DynamicMesh` (no GPU
  commands), so it must be called *before* `draw()` (see `onRender()`'s ordering below) for
  the staged content to reach the same frame's submission rather than lagging behind.
  Screen size reaches the shader via a tiny uniform buffer (not a push constant — this
  repo's Vulkan bindings don't expose `vkCmdPushConstants` yet, confirmed by checking; only
  the `VkPipelineLayoutCreateInfo`/`VkPushConstantRange` struct fields exist).
- **WebGPU**: mirrors the above — `DynamicMesh` is thinner (`queue.writeBuffer` is already
  safe per-frame, no staging/mapping needed), `UiRenderPipeline` adds a second render pass
  in the same command encoder with `loadOp = Load` (WebGPU has no framebuffer object, so no
  Vulkan-style parallel-framebuffer-list needed), and its own screen-size uniform + bind
  group (separate from the 3D pass's shared MVP buffer, so it doesn't touch that class's
  already-documented "unsafe for >1 draw call" constraint).
- Both `*GameApplication` base classes gain `protected val ui = UiContext()` and
  `protected open fun onDrawUi(ui: UiContext) {}`, called from `onRender()` *before*
  `renderSystem.update(...)` (the call that actually triggers GPU submission) — same
  "subclass declares behavior, base class drives the call" shape as `onSceneReady`/
  `onFixedUpdate`.
- **Bug found and fixed along the way**: a genuine `devicePixelRatio` scale mismatch on
  wasmJs — `UiRenderPipeline`'s screen-size uniform reads the WebGPU canvas's physical
  backing-buffer size (e.g. 3024×1598 on a 2x-DPI display), while mouse events report CSS
  pixels (e.g. 1512×799) — meaning a click where the button visually appears didn't line up
  with where hit-testing expected it, and the button itself rendered at roughly half the
  intended size, shifted toward the canvas origin. Fixed by scaling `offsetX`/`offsetY` by
  `window.devicePixelRatio` before feeding `Input.setPointer` in `sample-hello-cube`'s
  wasmJs `main.kt`, so pointer coordinates stay in the same physical-pixel space the shader
  already assumes.
- **Verified**: new `UiContextTest` (`awake-engine-ui`) unit-tests the press-while-hovered
  → release-while-hovered → click-fires sequence and confirms out-of-bounds clicks don't
  register — deterministic proof the core click-detection logic is correct, decoupled from
  any platform rendering pipeline. All `sample-hello-cube` targets (desktop/android/iOS/
  wasmJs) plus `awake-backend-vulkan`/`awake-backend-webgpu`/`awake-engine-ui` compile
  clean. `sample-hello-cube`'s `androidApp:assembleDebug` still succeeds. Real desktop runs
  of both `sample-hello-cube` and the full `awake-demo` gameplay scene (cube/ground/NPC/
  player) confirm no crash/regression from the new second-render-pass wiring. In-browser
  (wasmJs) verification confirmed the overlay quad renders and its hover-color state
  visibly changes in response to real pointer events reaching `Input` — full click-to-
  toggle visual confirmation in-browser wasn't independently reconfirmed via screenshot in
  this session (a `devicePixelRatio`-driven rendering/debugging detour consumed the
  available verification budget), relying instead on the passing unit test as the
  authoritative proof of the click-detection logic itself.
- **Explicitly out of scope for this slice**: Phase B bitmap-font/text rendering; the
  actual model-viewer/camera-mode/frustum-wireframe catalog feature (this slice is
  infrastructure it will be built on top of); per-widget clip/scissor rects; auto-
  disambiguated widget ids (caller must supply unique string ids manually).

**Phase B (2026-07-11): minimal bitmap-font text rendering, both backends.** Considered
`msdf-bmfont-xml`/MSDF signed-distance-field rendering (real font quality/scaling) against a
hand-authored fixed-grid bitmap font; picked the latter since this UI is a debug/catalog
overlay, not user-facing typography, and MSDF's shader + toolchain cost isn't justified yet
(revisit if the UI grows more prominent — see project memory `decision-bitmap-font-over-
msdf`).

- `awake-engine-ui`: new `font.BitmapFont` — hand-authored 8×8 glyph bitmaps for exactly the
  ~10 characters `"DEBUG: ON"`/`"DEBUG: OFF"` need (space, `:`, B, D, E, F, G, N, O, U), not
  full ASCII; generates its RGBA8 atlas at runtime from those bitmaps rather than shipping an
  image asset. `UiDrawPrimitive` gained a `Glyph` variant (quad + UV rect); `UiContext.text()`
  walks a string, looks up each character's UV rect, and emits one `Glyph` per character,
  advancing by `font.cellSize`.
- **Vulkan**: new `UiGlyphRenderPipeline` — a second pipeline sharing `UiRenderPipeline`'s
  already-created render pass (both draw within the same subpass sequentially: colored quads
  first, glyphs on top), with its own descriptor set (binding 0 = screen-size uniform,
  binding 1 = combined-image-sampler for the font atlas, mirroring `Material`'s existing
  texture-binding pattern for the 3D pass). `DynamicMesh` gained a constructor
  `floatsPerVertex` parameter so one class serves both the 6-float colored-quad layout and
  the 8-float (pos+uv+color) glyph layout.
- **WebGPU**: mirrors the Vulkan shape, but WebGPU's own `Texture` class
  (`awake-backend-webgpu/.../texture/Texture.kt`) is still an unimplemented `TODO()` stub (no
  upload path exists yet for either this or the 3D pass's `Material`) — so the font-atlas
  `GPUTexture` is created and uploaded directly inside the new WebGPU `UiGlyphRenderPipeline`
  (`device.createTexture(TextureDescriptor(...))` + `device.queue.writeTexture(...)`), not
  routed through that stub. Bind group has three entries (screen-size uniform, texture view,
  sampler) since WGSL requires the texture and sampler as separate bindings, unlike Vulkan's
  combined-image-sampler.
- Both `*GameApplication` base classes construct `protected val font = BitmapFont()`, the
  glyph pipeline, and (Vulkan only) the font-atlas `Texture`, alongside their existing
  `UiRenderPipeline` setup/teardown.
- **Verified**: `awake-backend-vulkan`/`awake-backend-webgpu`/`sample-hello-cube` (all
  targets) compile clean. `sample-hello-cube` now renders a live `"DEBUG: ON"`/`"DEBUG:
  OFF"` label next to its toggle in both `SampleApplication.kt` (Vulkan) and
  `WebGpuSampleApplication.kt` (WebGPU); confirmed in-browser for WebGPU via screenshot
  (label renders and is legible). Toggle click-to-flip itself was already proven by Phase
  A's `UiContextTest`; a synthetic mousedown/mouseup dispatch in this headless-browser
  session didn't reliably reproduce the full press-release cycle (hover-state color change
  did register, confirming hit-testing/coordinate math is correct) — not re-litigated here
  since it isn't Phase B's concern.

### D18 — Camera/frustum catalog debug tool

**Decided (2026-07-11): Orbit + Free-fly camera modes, a world-space debug-line renderer,
and a frustum-wireframe visualization, all wired into `awake-demo` (not `sample-hello-cube`,
which has nothing meaningful to catalog).** This is the feature the custom UI (D17, Phase
A+B) was built as infrastructure for.

- `awake-base`: new `Frustum` object — `corners(camera, aspect)` computes the 8 frustum
  corner points analytically (no matrix inverse; this codebase's `Mat4` has none), `EDGES`
  gives the 12 corner-index pairs a line renderer needs. Unit-tested (`FrustumTest`) against
  a hand-verifiable identity-view camera setup (same trick `CameraTest` already uses).
- `awake-engine-render-api`: new `LineSegment` (world-space, unlike screen-space
  `UiDrawPrimitive`) + `Renderer.drawDebugLines(lines)` — a third interface method,
  staged before `draw()` the same way `drawUi` already is.
- Both backends drawn **inside the existing main 3D render pass** (not a new pass), reusing
  that frame's already-computed view-projection matrix — new `debug.LineMesh`
  (`LINE_LIST`/no-index dynamic vertex buffer, mirrors `ui.DynamicMesh`'s rewrite-every-frame
  lifecycle) + `debug.LineRenderPipeline` per backend, `debug_line.vert/.frag`(`.spv`)/
  `.wgsl` shaders. **Discovered along the way**: `awake-backend-webgpu`'s 3D pass has no
  depth attachment at all today (confirmed by reading `pipeline/RenderPipeline.kt`/
  `renderer/Renderer.kt` — no `depthStencil` field anywhere) — so WebGPU debug lines don't
  depth-test against scene geometry, matching that backend's existing (pre-existing, not
  newly introduced) lack of depth testing for every other draw call too. Vulkan's lines DO
  depth-test correctly, sharing the 3D pass's real depth buffer.
- `awake-scene`: new `OrbitCameraSystem` (drag rotates yaw/pitch around a mutable `target`
  Transform, `W`/`S` zoom — no scroll-wheel axis exists in `Input` yet) and
  `FreeFlyCameraSystem` (`WASD` + drag, spectator-style) — same shape as the existing
  `CameraFollowSystem`, unit-tested the same way (`OrbitCameraSystemTest`/
  `FreeFlyCameraSystemTest`).
- `awake-demo`'s `SceneRuntimeHost` gained a `CameraMode` enum (`FOLLOW` default/`ORBIT`/
  `FREE_FLY`) driving which system's `update()` runs each fixed step, a `catalogTargets` map
  (`player`/`cube`/`ground`/`npc` — reusing meshes the demo already loads, not a new
  arbitrary-glTF-loading feature; `awake-base`'s glTF parser is still explicitly single-mesh
  only), and `followCameraSnapshot()` — a synthetic `Camera` computed from
  `CameraFollowSystem`'s own offset formula, *not* the live shared camera (which IS the
  active render camera regardless of mode) — used to visualize "what Follow would see" while
  actually looking from Orbit/Free-fly.
- `VulkanApplication`/`WebGpuApplication` (`awake-demo`) `onDrawUi`: a camera-mode dropdown,
  a catalog-target dropdown, and a show-frustum toggle calling `drawDebugLines(Frustum.EDGES
  .map { ... })`. **Two real bugs found and fixed during verification, not anticipated by
  the plan**:
  1. `BitmapFont`'s glyph set (D17 Phase B) is deliberately minimal (space/`:`/B/D/E/F/G/N/
     O/U, scoped to "DEBUG: ON"/"DEBUG: OFF") — none of "FOLLOW"/"ORBIT"/"FREE_FLY"/
     "FRUSTUM" fit it (missing L/W/R/I/T/C/A/Y), so the panel's dropdown/toggle widgets have
     no text captions this slice; their own hover/active/checked coloring is the feedback.
  2. `awake-demo` (unlike `sample-hello-cube`) uses the Compose Multiplatform plugin, and its
     wasmJs resource-aggregation pipeline did not reliably pick up `awake-backend-webgpu`'s
     bundled UI/debug-line `.wgsl` shaders (a stale `wasmJsDevelopmentExecutableCompileSync`
     cache was the proximate cause here — forcing that specific task, not just the resource
     tasks, fixed it) — but as a durable fix (not dependent on cache behavior), the three
     WebGPU UI shaders are now also copied into `awake-demo/shared`'s own
     `wasmJsMain/resources`, alongside its per-app `triangle.wgsl`. This is a deliberate,
     documented exception to D17/AGENTS.md's "bundle once in the backend module" rule,
     scoped to this one Compose-based consumer.
- **Verified**: `awake-base`/`awake-scene`/`awake-backend-vulkan`/`awake-backend-webgpu`/
  `awake-demo` (all 5 targets: desktop/android*/iOS arm64+simulator/wasmJs) compile clean.
  `FrustumTest`/`OrbitCameraSystemTest`/`FreeFlyCameraSystemTest` pass alongside the existing
  `CameraFollowSystemTest`/`CameraTest`. Real desktop launch (`awake-demo:desktopApp:run`)
  starts clean, Vulkan native libs load, no crash. wasmJs browser verification: the catalog
  panel renders correctly (no garbled text after the font-glyph fix), the ground plane
  renders, `debug_line.wgsl` resolves with a 200 (after the resource-bundling fix) — the
  frustum-toggle click itself wasn't independently re-confirmed via a successful synthetic
  browser click (a known, previously-documented headless-click-timing limitation, not a new
  regression), relying on `FrustumTest`'s corner-math proof and the absence of any runtime
  exception as the evidence the code path is sound. (*Android's `androidApp` module wasn't
  separately `assembleDebug`-checked this slice — out of scope, same as most prior slices
  that only spot-checked Android periodically.)
- **Explicitly out of scope**: loading arbitrary external glTF models; scroll-wheel zoom;
  resolving the UI-vs-camera-drag input-consumption conflict (dragging over a UI panel
  button also rotates the camera underneath it, since both read the same `Input.pointerDown`
  /`X`/`Y` with no arbitration); expanding `BitmapFont`'s glyph set for real mode-name
  captions (revisit if this UI grows enough to need it).

### D19 — Deduplicate `VulkanGameApplication`/`WebGpuGameApplication` into `GenericGameApplication`

**Decided (2026-07-11): extract the ~90% of `VulkanGameApplication` (`awake-backend-vulkan`,
298 lines) and `WebGpuGameApplication` (`awake-backend-webgpu`, 224 lines) that was
identical — same fields, same `Application` lifecycle, same `onFixedUpdate`/`onRender`/
`onDrawUi`/`onSceneReady`/`resolveRenderable`/`aspectRatio`/`drawDebugLines` hooks — into one
new shared base class, rather than continuing to hand-copy every catalog-tool feature (this
session's `drawDebugLines`, `OrbitCameraSystem` wiring) into both files.** They'd already
drifted once: only `VulkanGameApplication`'s `create()` had its `-XstartOnFirstThread`/
`MainScope()` desktop deadlock fixed a few commits earlier in this same session;
`WebGpuGameApplication` still had the identical latent bug (harmless only because wasmJs is
single-threaded).

- New module `awake-engine-game` (same 5-target shape as `awake-scene`/
  `awake-engine-render-api`) holds `GenericGameApplication` — an abstract class with all the
  shared state/lifecycle/hooks, operating only through `awake-engine-render-api`'s existing
  `Renderer`/`Mesh`/`Material` interfaces (no new interfaces needed). Each backend implements
  exactly two abstract members: `createBackendResources(window): BackendResources`
  (construct concrete `GraphicsDevice`/`SwapchainManager`/`RenderPipeline`/etc., upload
  meshes/texture, return the handful of interface-typed objects the shared bootstrap needs)
  and `destroyBackend()` (GPU teardown, reading the base class's now-`protected` `renderer`/
  `meshInstances`/`material` fields).
- `viewportSize: () -> Pair<Float, Float>` in `BackendResources` replaces each backend's
  separate "compute width/height for `ui.beginFrame`" and "compute `aspectRatio`" logic
  (previously duplicated 2x each) with one live-queried lambda.
- `awake-backend-vulkan`/`awake-backend-webgpu` depend on `awake-engine-game` via `api`, not
  `implementation` — it's a supertype of `VulkanGameApplication`/`WebGpuGameApplication`, so
  Gradle requires it resolvable on every downstream consumer's own classpath too (confirmed
  the hard way: `implementation` compiled fine within the backend module itself, but broke
  `sample-hello-cube`/`awake-demo` with "Cannot access X which is a supertype" the moment
  they tried to compile against the now-slimmer `VulkanGameApplication`).
- **Zero changes needed** to the four actual consumer classes (`demo.VulkanApplication`,
  `demo.WebGpuApplication`, `SampleApplication`, `WebGpuSampleApplication`) or their modules'
  `build.gradle.kts` files — they only ever touched the protected surface this refactor kept
  byte-for-byte identical.
- **Two pre-existing, unrelated bugs found during verification** (confirmed via `git stash`
  to predate this refactor, not caused by it): (1) `awake-demo`'s Vulkan companion window
  spams `VK_ERROR_MEMORY_MAP_FAILED` every frame and exits early instead of showing a window
  — reproduces identically on the pre-refactor commit; (2) `sample-hello-cube`'s wasmJs
  target is missing `awake-backend-webgpu`'s bundled UI/debug-line `.wgsl` shaders in its
  aggregated browser package (only its own per-app `triangle.wgsl` makes it through) — same
  gap as D18's Compose-specific exception, except `sample-hello-cube` doesn't apply the
  Compose plugin, so the assumption in AGENTS.md's resource-bundling rule that plain KMP
  consumers bundle transitively fine needs re-examining. Both flagged as separate follow-up
  work, not fixed in this slice.
- **Verified**: all 5 targets (desktop/android/iOS arm64+simulator/wasmJs) compile clean for
  `awake-engine-game`, `awake-backend-vulkan`, `awake-backend-webgpu`, `sample-hello-cube`,
  and `awake-demo`. `awake:scene:desktopTest` regression passes. Real desktop run of
  `sample-hello-cube` confirms the full refactored bootstrap chain (`create` →
  `createBackendResources` → scene loading → `onSceneReady` → `OrbitCameraSystem` →
  `onRender`) still works end to end — the cube renders at Orbit's default distance/pitch/
  auto-rotate framing, visually distinct from the pre-Orbit straight-on view.

### D20 — Retire `awake-demo`; port camera/frustum catalog into `sample-hello-cube`

**Decided (2026-07-11): delete `awake-demo` entirely; `sample-hello-cube` becomes the
project's one demo going forward.** `awake-demo`'s Compose-based two-window desktop split
(a Compose placeholder window spawning a separate GLFW/Vulkan companion process) and the
legacy OpenGL gallery it never fully escaped were the root cause of three separate pieces of
confusion this same session: the `FontBitmapSample` OpenGL-context crash, a round of
questions about why the Vulkan cube rendered in a companion window instead of the Compose
canvas, and the `VK_ERROR_MEMORY_MAP_FAILED` bug (D19's note above, fixed the same session
in a separate commit). `sample-hello-cube` — bare GLFW/Vulkan window, no Compose, no
subprocess — had already proven simpler and more reliable to verify throughout.

- Before deleting `awake-demo`, its camera/frustum catalog tool (Orbit/Free-fly modes,
  frustum-wireframe toggle — the feature D17's custom UI was built as infrastructure for)
  was ported into `SampleApplication`/`WebGpuSampleApplication`, scoped down for a
  single-entity scene: no catalog-target dropdown (nothing to switch between) and no
  `FOLLOW` camera mode (nothing to follow). A `homeCameraSnapshot` (copied from the scene's
  authored `Camera` before `OrbitCameraSystem`/`FreeFlyCameraSystem` start mutating it in
  place) plays the role `demo.SceneRuntimeHost.followCameraSnapshot()` played, minus the
  moving-player part.
- **Explicitly not ported**: NPC chase AI, player movement, NavMesh — separate MVP1a
  gameplay systems tied to entities (`player`, `npc`, `ground`) `sample-hello-cube`
  deliberately doesn't have. Porting them would turn `sample-hello-cube` into another
  `awake-demo`, contradicting the "minimal single cube" purpose it was scaffolded for.
- `awake-demo/` (shared, androidApp, desktopApp, iosApp) removed entirely; its three
  `include(...)` lines dropped from `settings.gradle.kts`; the `wasmjs-demo` entry dropped
  from `.claude/launch.json` (local-only, not git-tracked).
- **A second, separate pre-existing bug surfaced while verifying the ported catalog panel
  on desktop**: clicking a button/toggle at its visually-correct location never registers.
  Root cause (confirmed by precisely pixel-measuring a screenshot): `Input.pointerX/Y`
  (from `glfwGetCursorPos`, logical points) is compared directly against widget coordinates
  authored in the same units as the UI shader's `screenSize` uniform — which is populated
  from the swapchain's actual framebuffer-pixel extent (2x the logical point size on a
  Retina display). The two coordinate spaces silently disagree by exactly the Retina scale
  factor. Unrelated to D19's Y-flip fix (confirmed separately: text position/orientation is
  now correct, only click hit-testing is off) — flagged as its own follow-up, not fixed in
  this slice, since it's a pre-existing gap this session's UI system already had and is out
  of scope for a retirement/port task.
- Docs updated: `README.md`'s "Running the Demo"/"Building a New Game" sections now describe
  `sample-hello-cube` as the primary demo; `docs/MMORPG_ROADMAP.md`'s catalog-tool row now
  points at `sample-hello-cube`. Historical decision-log entries referencing `awake-demo`
  (D14/D16/D18/D19/etc.) are left unedited — accurate record of what was true when written.
- **Verified**: all 5 `sample-hello-cube` targets compile clean after the port,
  `awake:scene:desktopTest` regression passes, `./gradlew projects` confirms `awake-demo` no
  longer appears anywhere in the build. Real desktop run confirms the ported catalog panel
  renders correctly (right-side-up, correctly positioned per D19's fix) with Orbit
  auto-rotating as expected; the wasmJs equivalent couldn't be visually verified this slice
  due to the separate pre-existing `sample-hello-cube` wasmJs shader-bundling gap (D19's
  other flagged follow-up).

### D21 — `GenericGameApplication` becomes a standalone render bootstrap; `Game` injected, not inherited

**Decided (2026-07-12): `GenericGameApplication` no longer knows about ECS, UI, or game
assets at all — a game is a plain `Game` implementation, constructor-injected into
`VulkanGameApplication`/`WebGpuGameApplication`, not a subclass overriding lifecycle hooks.**
D19's dedup correctly identified that `VulkanGameApplication`/`WebGpuGameApplication`
duplicated ~90% of their bootstrap fields/lifecycle, but the resulting
`GenericGameApplication` still hard-baked in `awake-scene` (`World`/`SceneInstance`/
`SceneLoader`/`TransformSystem`/`RenderSystem`) and `awake-engine-ui` (`UiContext`,
`BitmapFont`), forcing every game onto one specific ECS and one specific immediate-mode UI
system whether it wanted them or not — the same problem the earlier deferred
`GameApplication { scene { }; ui { } }` DSL discussion was pointing at (see the
`decision_defer_gameapplication_dsl` memory note: a DSL's `scene { }`/`ui { }` blocks would
need to be optional/composable under the hood; they weren't).

- **`Renderer`'s UI-glyph pipeline is now lazy/opt-in.** Both backends previously built a
  font texture + glyph render pipeline unconditionally inside `createBackendResources()`,
  passed into `Renderer`'s constructor as required params — so no game could avoid the
  UI/text machinery even if it never called `drawUi`. `Renderer.drawUi(primitives, font)`
  now lazily builds its (colored-quad) UI pipeline on the first call of any kind, and its
  glyph pipeline on the first call that passes a non-null `font`, caching both after that.
  Vulkan's `recordCommandBuffer()` gained the same "skip the UI pass when nothing's ever
  been drawn" guard WebGPU's `draw()` already had.
- **`Renderer` gained `createMesh(geometry)`/`createMaterial(texture)` as on-demand
  methods**, replacing `GenericGameApplication`'s old `meshes: Map<String, MeshGeometry>`/
  `texture: TextureAsset?` constructor params (and the `meshInstances`/`material` fields
  built from them at bootstrap-construction time). Uploading a mesh isn't a
  Vulkan-vs-WebGPU backend concern — it's a "does this game want this asset, and when"
  concern, so it moved to the game.
- **New `SceneRuntime` class in `awake-scene`** — a plain class encapsulating exactly what
  `GenericGameApplication` used to do for scene handling (`World`/`SceneInstance`/
  `TransformSystem`/`RenderSystem`, driven via `load(scenePath, resolveRenderable)` +
  `render(delta)`), constructed and driven by a game that wants it, not baked into the
  bootstrap.
- **New `Game` interface in `awake-engine-game`**, injected via `GenericGameApplication`'s
  constructor: `suspend fun ready(renderer)`, `fun render(delta, viewportWidth,
  viewportHeight)`, plus `resize`/`pause`/`resume`/`dispose` (all default no-op) — mirroring
  libGDX's `Game`→`Screen` delegation (its `Game` class forwards every
  `ApplicationListener` callback to the active `Screen`). No `fixedUpdate`: forcing every
  `Game` to reason about a fixed-vs-variable timestep split would itself be a form of
  coupling; `FixedTimestepLoop` (already in `awake-engine`) becomes an optional tool a
  `Game` opts into internally if it wants deterministic stepping (`SampleGame` does, for its
  camera systems), not something the bootstrap imposes on every game.
- **`GenericGameApplication` is now `final` with respect to lifecycle wiring** — every
  `Application` callback either builds/tears down backend GPU resources
  (`createBackendResources`/`destroyBackend`, still abstract, still backend-specific) or
  forwards verbatim to the injected `game`. Zero game-specific logic lives in this class.
  `VulkanGameApplication`/`WebGpuGameApplication` are now concrete (not abstract) classes —
  with `Game` injected rather than overridden, they have no reason to stay open for
  subclassing.
- **`SampleApplication.kt`/`WebGpuSampleApplication.kt` deleted entirely** — once
  `VulkanGameApplication`/`WebGpuGameApplication` take `game: Game` as a plain constructor
  parameter, these two files did nothing but forward constructor args (confirmed: no
  overrides, no body). `sample-hello-cube`'s camera/UI/catalog logic (previously
  copy-pasted between the two files, including a byte-for-byte duplicated cube mesh) now
  lives once, in a new commonMain `SampleGame : Game`, constructed directly at each of the
  4 platform entry points (`Main.kt`/`main.ios.kt`/`MainActivity.kt`/wasmJs `main.kt`) via
  `VulkanGameApplication(..., game = SampleGame())` / `WebGpuGameApplication(...)`.
- **Explicitly out of scope**: unifying the SPIR-V (Vulkan) vs. WGSL (WebGPU) shader
  sources so a game author writes one shader instead of two — a real KMP-boilerplate gap,
  but a build-tooling problem (shader cross-compilation or a small Kotlin shader DSL),
  orthogonal to the application-layer coupling this entry fixes. Tracked as a follow-up
  decision, not bundled here.
- **A real bug was caught during real-run verification, not compile-checking**: after the
  refactor, `samples:hello-cube:run` crashed on exit with `vkDestroyBuffer: buffer not
  initialized`. Cause: `VulkanGameApplication` still constructs a template `Material` purely
  to get its `descriptorSetLayout` before `RenderPipeline` exists (needed before any real
  material can be built) — `createResources(texture)` is deliberately never called on this
  instance (real materials now come from `Renderer.createMaterial()`), so its
  `uniformBuffer`/`descriptorPool` are never allocated. `destroyBackend()` was still calling
  this template's full `Material.destroy()`, which tried to destroy those never-allocated
  handles. Fixed by renaming the field to `pipelineLayoutMaterial` (documenting why it's
  special) and destroying only its `descriptorSetLayout` directly in `destroyBackend()`,
  not the whole `Material`.
- **Verified**: `awake-engine-render-api`/`awake-scene`/`awake-engine-game`/
  `awake-backend-vulkan`/`awake-backend-webgpu` compile clean; all 5 `sample-hello-cube`
  targets (desktop, `androidApp`, iOS, wasmJs) compile clean; `awake:scene:desktopTest`
  passes. Real desktop run: the app stays up and renders continuously (previously crashed
  on close before the `pipelineLayoutMaterial` fix, confirmed fixed after). wasmJs
  console-checked via `preview_start` — clean apart from the same pre-existing
  `debug_line.wgsl` 404 flagged in D20 (unrelated to this change; the file exists on disk,
  the dev server just doesn't serve it — separate follow-up, not fixed here).

### D22 — Offscreen render-to-texture (`RenderTarget`): CPU readback + on-screen compositing

**Decided (2026-07-12): `Renderer` gains `createRenderTarget`/`renderToTexture`/`readPixels`
plus a `renderTarget` param on `createMaterial`, so a game can render a scene into an
offscreen GPU texture instead of the swapchain/canvas — for future golden-image testing
(CPU pixel readback) and on-screen compositing (minimap/portal-camera quads).**

Landed in 3 slices:

1. **`RenderTarget` interface + Vulkan implementation.** `RenderTarget` (narrow, like
   `Mesh`/`Material` — no raw GPU handle crosses the module boundary) lives in
   `awake-engine-render-api` alongside the extended `Renderer` signatures. Vulkan's
   `OffscreenRenderTarget` reuses the existing swapchain `renderPipeline`/render pass
   unmodified by giving the offscreen color image the SAME format as the swapchain
   (`swapchainManager.imageFormat`), rather than forcing `R8G8B8A8_UNORM` — Vulkan requires
   a framebuffer's attachment format to exactly match the render pass it's used with, so a
   different format would have forced building an entire second render pass + graphics
   pipeline. CPU readback needed two new native Vulkan bindings
   (`VulkanBuffers.readBufferMemoryBytes`, `VulkanImages.vkCmdCopyImageToBuffer`, both
   hand-written JNI C++ mirroring the existing upload-direction bindings) plus three new
   `vkTransitionImageLayout` cases (`COLOR_ATTACHMENT_OPTIMAL` ↔ `SHADER_READ_ONLY_OPTIMAL`
   ↔ `TRANSFER_SRC_OPTIMAL`). Verified via a real desktop run: `createRenderTarget` →
   `renderToTexture` (clear only) → `readPixels` returned the exact clear color.
2. **iOS + WebGPU real implementations.** iOS: the same two native operations via direct
   MoltenVK cinterop (mirrors `writeBufferMemoryBytes`/`vkCmdCopyBufferToImage`'s existing
   pattern), plus the same three transition cases. WebGPU: a real `GPUTexture`-based
   `OffscreenRenderTarget`, `mapAsync`/`getMappedRange` readback with WebGPU's mandatory
   256-byte `bytesPerRow` padding stripped back out before returning a tightly-packed
   `TextureAsset`. Verified end-to-end on desktop: rendering `CubeDemo`'s actual cube (not
   just a clear color) into a 128×128 target and saving the readback via a new
   `saveDebugPng` helper (desktop-only, `javax.imageio`) produced a real, visually-confirmed
   image of the cube from a second camera angle.
3. **On-screen compositing (`UiDrawPrimitive.Texture`/`UiContext.textureQuad`).**
   `awake-engine-render-api` already depends on `awake-engine-ui` (`Renderer.drawUi` takes
   `List<UiDrawPrimitive>`), so `UiDrawPrimitive.Texture` carries its material as `Any`
   rather than the `Material` interface, to avoid a module dependency cycle — each backend
   casts it back to its own concrete `Material`, the same "opaque handle" pattern
   `DrawCall.mesh`/`.material` already use across that boundary. Vulkan: a fourth UI
   pipeline (`UiTextureRenderPipeline`) with ONE shared descriptor set, rewritten via
   `vkUpdateDescriptorSetImage` immediately before each quad's draw call — safe because
   this codebase already fully serializes frames. WebGPU: bind groups are immutable once
   created, so that backend instead caches one bind group per distinct `Material`; this
   also required the minimum `Material`/`Texture` completion needed to expose a render
   target's view+sampler for this path specifically (the general 3D `DrawCall.material`
   texture-binding case remains out of scope — it'd need the shared cube shader to declare
   a second bind group, real scope creep beyond offscreen rendering itself).

**Bug found + fixed during this work**: `TransferContext.runOneTimeCommands` allocates a
fresh command buffer from the shared pool on every call and never frees it — fine for its
existing callers (a handful of one-time uploads at startup), but `renderToTexture` calling
it every frame (as a live minimap would) leaked a command buffer every frame and
destabilized the Vulkan instance within under a minute of real testing (confirmed by a
real desktop run crashing with `VK_SUBOPTIMAL_KHR`). Fixed by giving `Renderer` its own
`runOffscreenCommands`, which allocates its command buffer/fence once and reuses both
every call, instead of going through `TransferContext`.

**Known remaining issue, NOT fixed**: even after that leak fix, calling
`renderToTexture`/`readPixels` every single frame (e.g. a live-updating minimap) still
reproducibly crashes the Vulkan backend within ~30-60 seconds with the same
`VK_SUBOPTIMAL_KHR` error — confirmed via real desktop testing to be specific to the
per-frame offscreen render (5+ minutes stable with it off, crashes consistently with it on
every frame). Root cause not yet found. `sample-hello-cube`'s `CubeDemo` has a `MINIMAP`
toggle wired to reproduce this on demand, defaulting off so the shipped demo is stable.
Infrequent/one-shot `RenderTarget` usage (the golden-image-testing use case, and the PNG
sample-test proof above) is fully verified stable — this issue is specific to the
"render every frame" on-screen-compositing use case.

- **Verified**: all 5 `sample-hello-cube` targets (desktop, `androidApp`, iOS, wasmJs)
  compile clean across all 3 slices; `awake:scene:desktopTest` passes. Real desktop runs
  confirmed the readback pipeline (exact clear color, then a real rendered cube image) and
  no regression to the existing demo (HUD/toggle/dropdown/frustum) — see the known-issue
  note above for the one limitation surfaced by testing that remains open.

### D23 — Fixed: every 3D scene rendered via WebGPU was vertically mirrored

**Found (2026-07-12) via the Jolt Physics falling-cube demo**: the user reported the cube
looked like it rose from the ground to rest height instead of falling onto it. Root cause:
`Camera.viewProjectionMatrix()`'s `flipYForClipSpace` correction (`awake/base/.../Camera.kt`)
exists specifically because Vulkan's NDC has +Y down, needing a sign flip against
`Mat4.perspective()`'s native OpenGL-convention (+Y up) matrix. WebGPU's NDC is *also* +Y up
(confirmed by this repo's own `ui_quad.wgsl` comment: "pixel-space is Y-down, NDC is Y-up")
— i.e. the opposite of Vulkan, needing no flip at all. But every scene JSON hardcoded
`"flipYForClipSpace": true` (the `SceneCamera` schema default), applied uniformly to
whichever backend loaded it, so WebGPU always got an extra, incorrect Y-flip. Invisible
until now because the only prior WebGPU content (`CubeDemo`'s spinning cube) is rotationally
symmetric — a falling/rising cube is the first content with an unambiguous vertical motion
cue, which is why this surfaced only now, not during any earlier WebGPU work.

**Fix**: `flipYForClipSpace` is a backend clip-space convention, not scene-authored content,
so it no longer lives in scene JSON at all. Added `val flipYForClipSpace: Boolean` to the
`Renderer` interface (`awake:engine:render-api`) — `true` for Vulkan, `false` for WebGPU.
`SceneLoader.instantiate`/`SceneRuntime.load` now take this from the active `Renderer`
instead of trusting the document. Removed the field from `SceneCamera`'s schema and from
`sample.scene.json`/`physics.scene.json`/the `awake:scene` test fixture. Also fixed the two
manually-constructed `Camera` instances in `sample-hello-cube` (`CubeDemo`'s minimap camera
and `DemoCatalog`'s offscreen-readback verify camera) to pass `renderer.flipYForClipSpace`
explicitly instead of relying on `Camera`'s own `true` default.

- **Verified**: `awake:scene:desktopTest` (including the updated `SceneLoaderTest`) passes;
  `awake:backend:vulkan`/`awake:backend:webgpu`/`sample-hello-cube` compile clean across
  desktop, Android, iOS-simulator, and wasmJs. Confirmed visually in a real browser
  (`wasmJsBrowserDevelopmentRun`): `CubeDemo`'s rendered cube face colors are now vertically
  mirrored relative to pre-fix screenshots, as expected for a real Y-axis correction (a
  symmetric spinning cube gives no independent "which way is up" signal on its own, but the
  two screenshots directly show the flip took effect). Vulkan's `flipYForClipSpace` value is
  unchanged (`true`), so desktop/Android rendering has zero behavioral change from this fix.
  A live mid-fall screenshot of the physics demo on WebGPU was not obtained (this sandbox's
  browser automation throttles `requestAnimationFrame` heavily when not actively driven,
  making the ~1-2 second fall too fast to reliably catch — confirmed by repeated attempts
  showing `FPS: 0` between polls) — the fix's correctness rests on the two independent,
  pre-existing doc-comment sources above (`Camera.kt`'s Vulkan-specific rationale,
  `ui_quad.wgsl`'s Y-up-NDC comment) plus the confirmed visual mirroring, not a live capture
  of the physics scene specifically.

### D5 — Physics engine
**Decided (2026-07-07): Jolt Physics for 3D, post-MVP (Phase 8).**
- Jolt (MIT, C++) over Bullet (aging), PhysX (heavyweight), Rapier (Rust toolchain cost).
- Same binding split as Vulkan: JNI on Android/Desktop JVM, cinterop on iOS.
- **Binding must be coarse-grained**: physics is called per-body per-frame — a generated
  1:1 API mirror would mean thousands of JNI crossings/frame. Instead: one `step(dt)` call
  + batched transform read-back via shared direct buffer. Small hand-designed facade
  (~20 fns); jni-binding-generator used for marshalling, not API mirroring.
- 2D (when needed): **kbox2d** pure-Kotlin port in `commonMain` — zero native cost,
  covers future Wasm/JS targets.

**D5 follow-up (2026-07-11): binding-layer facade + per-target library choice.**

To keep any future backend swap (different Jolt binding, or a different engine entirely)
from rippling into `awake-scene`/gameplay code, physics is fronted by a new
**`awake-physics-api`** module — plain `commonMain` interfaces only (`PhysicsWorld`,
`PhysicsShape`, `BodyHandle`, `MotionType`, batched `syncTransforms`/`raycast`), no
`expect`/`actual`, same architectural role `awake-engine-render-api` already plays for
`Renderer`/`Mesh`/`Material` between the ECS layer and the Vulkan/WebGPU backends. Gameplay
code depends only on this module; each backend below implements it in isolation.

Per-target binding choice, evaluated on maintainer track record over star count (low stars
on a JVM-physics-binding repo mostly reflects a small market, not neglect):

- **Desktop + Android (JVM)**: [`stephengold/jolt-jni`](https://github.com/stephengold/jolt-jni).
  Chosen over hand-rolled JNI and over `xpenatan/xJolt`: xJolt claims broader coverage
  (JNI + Emscripten/TeaVM) via a custom in-house codegen tool (`jParser`) — more surface for
  a small maintainer to sustain, and its web claim doesn't apply here anyway (TeaVM ≠
  Kotlin/Wasm). `jolt-jni`'s author (stephengold) has a multi-year track record maintaining
  JVM physics bindings (`libbulldeme`, `Minie`), a narrower JVM-only scope, and no iOS
  claim to begin with (irrelevant — iOS isn't JVM in this project regardless). Fallback if
  it goes stale: fork/patch the existing working JNI layer, not a from-scratch rewrite.
- **iOS (Kotlin/Native)**: no off-the-shelf option exists (neither `jolt-jni` nor `xJolt`
  targets Kotlin/Native cinterop). **Custom cinterop against `JoltC`**
  ([`SecondHalfGames/JoltC`](https://github.com/SecondHalfGames/JoltC), a C-compatible
  wrapper over Jolt's C++ core — cinterop only understands C, same constraint MoltenVK's
  vendoring already solved for Vulkan), vendored as a git submodule the same way MoltenVK
  is. Scoped small per the coarse-grained rule above — a dozen or so entry points, not a
  full mirror.
- **wasmJs**: [`jrouwe/JoltPhysics.js`](https://github.com/jrouwe/JoltPhysics.js) — the
  official Emscripten/WASM port, maintained by Jolt's own author, published on npm as
  `jolt-physics`. Consumed via Kotlin/Wasm JS interop, the same role `wgpu4k` plays for the
  WebGPU backend today. No custom work needed for this leg.
- Core engine itself: [`jrouwe/JoltPhysics`](https://github.com/jrouwe/JoltPhysics) —
  vendored as a submodule regardless of binding choice, since it's the upstream C++ library
  every option above ultimately wraps.

**D5 slice 1 (2026-07-12): `awake:physics:api` + real desktop/Android `awake:backend:jolt`.**

- `awake:physics:api` (`awake/physics/api/`) landed exactly as scoped above: `PhysicsWorld`,
  `PhysicsShape` (`BoxShape`/`SphereShape`), `MotionType`, `BodyHandle` (a `value class`,
  not a raw `Long` — new public API surface), `BodyTransform`, `RaycastHit`. Same 4-target
  shape as `awake:engine:render-api` even though only desktop+Android have a real backend
  yet — iOS/wasmJs just need to compile.
- `awake:backend:jolt` (`awake/backend/jolt/`) implements `JoltPhysicsWorld` for real on
  desktop + Android via `stephengold/jolt-jni` 5.2.0 (confirmed current via
  `repo1.maven.org`'s `maven-metadata.xml`, not guessed). Desktop uses the JVM library
  (`jolt-jni-MacOSX_ARM64`, no classifier) plus the matching native classifier jar
  (`ReleaseSp`, loaded at runtime via `electrostatic4j:snaploader` per jolt-jni's own
  onboarding doc). Android uses the self-contained `jolt-jni-Android` AAR.
  - **Confirmed the hard way**: the published 5.2.0 `jolt-jni-Android:SpRelease` AAR's
    `classes.jar` has been R8-shrunk to zero `.class` files (only its bundled Metal/Vulkan
    shader resources for an unrelated soft-body demo survive) — presumably built with no
    consumer keep-rules, so R8 treated the whole public API as unreachable. Switched to the
    `SpDebug` AAR, which jolt-jni's own doc suggests as the starting point anyway.
  - **Confirmed the hard way**: Kotlin executes property initializers and instance `init {}`
    blocks in textual declaration order. An early version put `JoltNative.ensureLoaded()`
    (native library load + `Jolt.newFactory()`/`registerTypes()`) inside the instance
    `init {}` block, textually *below* the `tempAllocator: TempAllocator = TempAllocatorMalloc()`
    property — so `TempAllocatorMalloc()`'s own native constructor ran first and threw
    `UnsatisfiedLinkError` before the library was ever loaded. Fixed by moving the load call
    into a `private companion object { init { ... } }`, which Kotlin always runs before any
    instance member.
  - iOS/wasmJs get an explicit `TODO()`-throwing `JoltPhysicsWorld` stub (per the binding
    plan above, JoltC/JoltPhysics.js are separate, deferred slices) — just enough for those
    targets to compile.
  - `QuatEuler.kt`'s quaternion-to-Euler conversion (for `BodyTransform.rotation`) is a pure
    function in `commonMain` (no jolt-jni type in its signature) so it's usable/derivable
    without native bindings; its rotation order was derived to match the exact inverse of
    jolt-jni's own `Quat.sEulerAngles(x, y, z)` construction (confirmed by symbolically
    expanding jolt-jni's quaternion-multiply operator against `sEulerAngles`'s formula, then
    numerically round-tripping arbitrary angles).
- **Confirmed on real hardware (desktop, this dev machine, macOS Apple Silicon)**: a new
  `awake:backend:jolt:desktopTest` (`JoltPhysicsWorldTest`) creates a real `JoltPhysicsWorld`,
  drops a dynamic sphere from y=10 with no ground, steps it 60 times at a fixed 1/60s
  timestep, and asserts the resulting fall distance is within 20% of the closed-form
  semi-implicit-Euler estimate (`g·dt²·steps·(steps+1)/2` ≈ 4.987 m). Actual measured fall:
  4.902 m — passes for real, not just compiles; this is the real jolt-jni step/readback loop
  running end to end. All 4 targets of both new modules compile
  (`compileKotlinDesktop`/`compileAndroidMain`/`compileKotlinIosArm64`/
  `compileKotlinIosSimulatorArm64`/`compileKotlinWasmJs`), and a full `./gradlew build`
  (excluding this repo's pre-existing, unrelated JDK-25-vs-toolchain-17 failures in
  `awake:base`'s iOS/wasmJs test compilation and `samples:hello-cube`'s spotless check —
  none of which touch physics code) has no new failures from this slice.

**D5 slice 2 (2026-07-12): real iOS `JoltPhysicsWorld` via JoltC cinterop.**

- **Vendored `SecondHalfGames/JoltC`** as a git submodule at
  `awake/backend/jolt/ios-native/JoltC` (same convention as `awake:backend:vulkan`'s
  MoltenVK submodule). JoltC itself nests `jrouwe/JoltPhysics` as its own submodule
  (`ios-native/JoltC/JoltPhysics`) and its own `CMakeLists.txt` builds both `joltc` (the C
  wrapper) and `Jolt` (the engine itself, via `add_subdirectory(JoltPhysics/Build)`) as
  separate static libs in one configure — no separate JoltPhysics build step needed.
- **Built for both iOS target slices via plain CMake iOS cross-compilation**
  (`-DCMAKE_SYSTEM_NAME=iOS -DCMAKE_OSX_SYSROOT=iphoneos|iphonesimulator
  -DCMAKE_OSX_ARCHITECTURES=arm64`), no separate toolchain file needed (Apple's CMake
  supports this directly) — new `configureJoltC<Target>`/`buildJoltC<Target>` Gradle tasks
  in `awake:backend:jolt/build.gradle.kts` (manual/on-demand, same convention as
  `awake:backend:vulkan`'s `configureDesktopNative`/`buildDesktopNative`), output under
  `build/joltc-native/<target>/{libjoltc.a, JoltPhysics/Build/libJolt.a}`. Confirmed both
  slices build clean (Xcode 26.5 / CMake 4.3.2 on this machine).
- **Cinterop `.def`** (`awake/backend/jolt/src/nativeInterop/cinterop/JoltC.def`) exposes
  just `JoltC/JoltC.h` (pulls in `Enums.h`/`Functions.h`) — a pure C API surface, no C++
  name-mangling concerns unlike a raw JoltPhysics header would have. Same static-base-def +
  per-target-generated-def-with-linkerOpts pattern as MoltenVK (`-ljoltc -lJolt -lc++`,
  paths pointing at each target's own `build/joltc-native/<target>` dir).
- **`JoltPhysicsWorld.kt` (iosMain)** mirrors the desktop/Android `jolt-jni` backend's exact
  semantics: 2 object layers (moving/non-moving) mapped to 1 broadphase layer, tracked
  body-ID list for batched `syncTransforms`/`destroy`, same gravity default. JoltC's C API
  differs from jolt-jni's OO one in two structural ways that shaped the Kotlin code:
  - **Opaque C structs** (`JPC_PhysicsSystem`, `JPC_BodyInterface`, `JPC_Shape`, `JPC_String`,
    `JPC_JobSystem`, ...) map to `cnames.structs.*`, not `platform.joltc.*` — same as
    `awake:backend:vulkan`'s `VkBuffer_T`-style opaque Vulkan handles.
  - **Struct fields that are themselves structs** (e.g. `JPC_BodyCreationSettings.Position`)
    expose a read-only `val` returning a *live view* into the parent's memory, not a
    settable `CValue` property — writing one means setting the view's own scalar fields
    (`.Position.x = ...`), whereas the *same* vector type used as a plain **function
    parameter** (e.g. `JPC_PhysicsSystem_SetGravity`) *does* take a `CValue<JPC_Vec3>`.
    `JPC_Vec3.write(Vec3)`/`JPC_Quat.write(...)` extension functions handle the former;
    `vec3Value(Vec3)`/`quatValue(...)` (via `cValue { }`) handle the latter.
  - JoltC's `JPC_MotionType` is a real Kotlin `enum class` (entries `JPC_MotionType.
    JPC_MOTION_TYPE_STATIC` etc., dot-accessed) despite looking identical in the C header to
    `JPC_Activation`, which cinterop instead represents as a plain `UInt` typealias with
    top-level `const val`s — an inconsistency discovered only by inspecting the generated
    klib metadata (`klib dump-metadata`), not documented anywhere obvious in advance.
  - JoltC's callback-based `BroadPhaseLayerInterfaceFns`/`ObjectVsBroadPhaseLayerFilterFns`/
    `ObjectLayerPairFilterFns` (C function-pointer tables, no vtable) are filled via
    top-level `staticCFunction(::fn)` references — this class's 2-object-layer/1-broadphase-
    layer scheme is a compile-time constant, so no captured state was ever needed.
  - Added `eulerVec3ToQuatWxyz` to `QuatEuler.kt` (pure function, inverse of the existing
    `quatToEulerVec3`) since JoltC exposes no `Quat.sEulerAngles`-style helper the way
    jolt-jni does.
- **`samples/hello-cube`'s iOS `PhysicsWorldFactory` actual** now constructs a real
  `JoltPhysicsWorld()` instead of returning `null`; `build.gradle.kts` wires
  `awake:backend:jolt` into `iosMain` and repeats JoltC's linker flags on the app's own
  framework link step (same linker-opts-repetition requirement as MoltenVK — flags embedded
  in a module's own binary don't propagate through a `project()` dependency to a downstream
  consumer's link step).
- **Verified:** `:awake:backend:jolt:compileKotlinIosArm64` /
  `compileKotlinIosSimulatorArm64` and `:samples:hello-cube:compileKotlinIosArm64` /
  `compileKotlinIosSimulatorArm64` all pass clean. `:samples:hello-cube:
  assembleSampleDebugXCFramework` (Xcode 26.5, iOS Simulator SDK 26.5) links successfully —
  JoltC's static libs resolve at the framework's own link step, not just at Kotlin
  compile time. `awake:backend:jolt:desktopTest` (the existing free-fall regression test)
  still passes, confirming no regression to the desktop/Android backend.
  - **Found and fixed, while trying to get an actual simulator run, a real pre-existing bug
    unrelated to this slice**: `samples/hello-cube/iosApp/iosApp.xcodeproj`'s
    `LIBRARY_SEARCH_PATHS` for MoltenVK pointed at `$(SRCROOT)/../../awake-backend-vulkan/...`
    — stale from *two* separate renames (the flat-to-nested `awake-backend-vulkan` →
    `awake/backend/vulkan` module rename, and `sample-hello-cube` moving under `samples/`),
    silently broken (Xcode's own live-preview "debug dylib" re-link step needs this path
    independently of the XCFramework's own already-correct embedded linker flags). Fixed to
    `$(SRCROOT)/../../../awake/backend/vulkan/...` (3 levels up, matching the current
    directory depth) for both Debug/Release configs, and added JoltC's equivalent
    `LIBRARY_SEARCH_PATHS`/`OTHER_LDFLAGS` entries alongside it.
  - **Real simulator run attempted, got partway, blocked by an unrelated, pre-existing iOS
    resource-bundling gap — not a JoltC/physics issue**: booted an iPhone 17 Pro simulator,
    installed and launched the app. Console log shows MoltenVK successfully creating a real
    `VkInstance`/`VkDevice`/swapchain (3 images, real GPU-backed Metal device) — proof the
    JoltC-linked binary runs at all — but the app then crashes with `IllegalStateException:
    Resource not found: .../assets/shader/vulkan/debug_line.vert.spv` inside
    `VulkanGameApplication.createBackendResources`, before any demo's `render()` loop (and
    therefore before `PhysicsDemo`'s physics stepping) ever runs. Root cause: `debug_line.
    {vert,frag}.spv` live under `awake:backend:vulkan`'s own `src/commonMain/resources/`,
    but `iosApp.xcodeproj`'s Resources build phase only bundles `samples/hello-cube/src/
    appMain/resources/assets` (confirmed: the built `.app`'s `assets/shader/vulkan/`
    contains only `triangle.{vert,frag}.spv`) — the exact same class of gap already
    documented for wasmJs in this doc (D19–D21: a module's own resources don't automatically
    merge into a downstream consumer's bundled output) and in Phase 0's still-unresolved iOS
    compile-target backlog, affecting every demo on iOS, not something introduced by this
    slice. Fixing it (adding another Xcode folder reference, or moving debug-line drawing
    behind a capability check) is out of scope for this Jolt slice.
  - **Honest bottom line**: JoltC's cinterop/native-linking integration is verified as far as
    is possible without that unrelated resource-bundling fix — compiles, links (both into
    the XCFramework and into the app's own re-link step), and the binary runs far enough to
    prove the linked JoltC symbols don't themselves crash anything. The specific "cube Y
    settles" runtime proof (the same bar the desktop free-fall test and this doc's other
    entries hold to) was **not** obtained this round, and is blocked on the resource-bundling
    gap above, not on anything Jolt/JoltC-specific.

**D5 slice 2 (2026-07-12): ECS wiring (`PhysicsBody`/`PhysicsSystem`) + `sample-hello-cube`
falling-cube demo.**

- `awake:scene` gained `PhysicsBody` (shape/motionType/nullable `BodyHandle`, same
  "declared-before-the-live-resource-exists" shape as `MeshRenderer`) and `PhysicsSystem`
  (externally driven `update(world, delta)` — constructed and called by game code, never
  wired into `SceneRuntime` itself, since only the caller knows which concrete
  `PhysicsWorld` to construct). Body
  creation is lazy/one-shot per entity (first `update` that sees a `null` handle creates the
  body); `step`/`syncTransforms` are each called exactly once per `update`, matching
  `PhysicsWorld.syncTransforms`'s batched-readback contract. Covered by
  `PhysicsSystemTest` using a fake `PhysicsWorld` (pure ECS-wiring logic, no jolt-jni needed).
  There's still no scene-JSON authoring for `PhysicsBody` (no `"physicsBody"` key in
  `SceneDocument`/`SceneLoader` yet) — the demo below attaches it in code via `world.add`
  after `SceneRuntime.load`, once the entity's authored `Transform` is available to derive a
  matching `BoxShape`. Scene-JSON authoring is left for whenever the editor needs it.
- `sample-hello-cube` gained a new `PhysicsDemo` catalog entry (`DemoCatalog`'s "PHYSICS"
  dropdown option) rather than bolting physics onto the existing `CubeDemo` — `CubeDemo`
  already juggles two camera modes, a frustum toggle, and a minimap, and this demo wanted its
  own fixed (non-orbiting) camera so settling is easy to observe without a moving viewpoint
  fighting it. Scene: `physics.scene.json` — a static "ground" node (unit cube mesh scaled to
  10×0.5×10, `BoxShape` half-extents derived from that same authored scale, `MotionType.STATIC`)
  and a dynamic "cube" node (unit `BoxShape`, `MotionType.DYNAMIC`) dropped from y=5.
  `PhysicsSystem.update` runs inside `FixedTimestepLoop.advance`'s `fixedUpdate` step, before
  `sceneRuntime.render(delta)` in the `render` step — same ordering `CubeDemo`'s own camera
  systems already use. The falling cube's Y position is logged to console once a second (a
  plain per-frame accumulator, not tied to `DemoCatalog`'s own once-a-second HUD log) so a
  real run's settling behavior is verifiable from console output alone.
- **iOS/wasmJs handling**: a small `expect fun createPhysicsWorld(): PhysicsWorld?` in
  `sample-hello-cube`'s own `commonMain` (same "return `null` on an unsupported platform"
  shape used by sample-owned navmesh bootstrap) — `desktopMain`/`androidMain` actuals
  construct a real `JoltPhysicsWorld`, `iosMain`/`wasmJsMain` actuals return `null` since
  `awake:backend:jolt`'s backends there are still `TODO()`-throwing stubs (per D5's binding
  plan). `PhysicsDemo` degrades gracefully on a `null` `PhysicsWorld`: it still loads/renders
  the ground+cube scene, just never steps physics, instead of crashing. **iOS/wasmJs real
  Jolt bindings (`JoltC` cinterop, `JoltPhysics.js`) remain fully deferred** — nothing in this
  slice or slice 1 implements either, matching how RenderTarget's iOS/WebGPU slices were
  flagged previously.
- **Confirmed on real hardware (desktop, this dev machine, macOS Apple Silicon)**: ran
  `:samples:hello-cube:run` with `PhysicsDemo` selected and watched the console log. The cube
  starts at y=5, falls, and settles: `cube Y = 0.45309728` on the first ~1s sample, then
  stable at `cube Y = 0.47999978` for the remainder of a 20-second observation window (FPS
  54-55, frame time 16-19ms throughout) — expected rest height is 0.5 (ground top at y=0 plus
  the cube's own half-height), so this settles right at the ground surface within Jolt's
  normal contact-penetration tolerance, not through the floor and not floating. Both
  `:samples:hello-cube:compileKotlinDesktop` and `:compileAndroidMain` (plus
  `:compileKotlinIosArm64`/`:compileKotlinWasmJs`, to confirm the `null`-returning stub
  actuals compile) pass clean.

**D5 slice 3 (2026-07-12): real wasmJs `JoltPhysicsWorld` via `jolt-physics` (JoltPhysics.js).**

- **npm wiring**: `awake:backend:jolt/build.gradle.kts`'s `wasmJsMain` source set adds
  `implementation(npm("jolt-physics", "1.1.0"))`. Default entrypoint (not `/wasm`) resolves to
  the `wasm-compat` flavour, whose `.wasm` binary is base64-embedded in the JS bundle —
  avoids a `locateFile`/separate-`.wasm`-fetch dance under Kotlin/Wasm's webpack dev server,
  at the cost of bundle size. Single-threaded, not `-multithread` (that flavour needs
  SharedArrayBuffer + COOP/COEP headers the default webpack-dev-server task doesn't set).
- **Interop shape — no `dynamic`, so no typed per-class `external class` bindings**:
  `jolt-physics`'s default export is `declare function Jolt<T>(target?: T): Promise<T &
  typeof Jolt>` — calling it resolves one namespace object holding every `Jolt.XXX`
  class/constant as a runtime property, not separate ES module exports Kotlin/Wasm's
  `@JsModule` could bind per-declaration at compile time. Kotlin/Wasm has no `dynamic` type
  (unlike Kotlin/JS), so the resolved object's shape can't be described the way
  `kotlinx-browser`'s DOM bindings are. Every actual Jolt call instead goes through an
  `@JsFun("...")`-annotated `external fun` (the Kotlin/Wasm-idiomatic substitute for
  `dynamic`/`js()`, neither of which exist for this target) — each one inline JS mirroring
  `jrouwe/JoltPhysics.js`'s own `Examples/js/example.js` boilerplate
  (`setupCollisionFiltering`/`initPhysics`/`createBox`/`updatePhysics`), taking the resolved
  namespace object as an opaque `JsAny` parameter and indexing into it by property name at
  runtime. `@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)` silences the (currently
  experimental) opt-in this whole approach requires.
- **Async→suspend bridge**: `expect fun createPhysicsWorld(): PhysicsWorld?` became `expect
  suspend fun createPhysicsWorld(): PhysicsWorld?` across **all four** platforms (not just
  wasmJs) — `JoltPhysicsWorld`'s wasmJs `companion object` factory (`JoltPhysicsWorld.create()`)
  awaits `jolt-physics`'s own `Promise`-returning module bootstrap
  (`kotlinx.coroutines.await()`) before constructing; the other three platforms' actuals
  stay plain synchronous constructor calls wrapped in a no-op `suspend`. `PhysicsDemo.ready()`
  was already `suspend`, so the ripple stopped at that one call site as anticipated.
- **`JoltPhysicsWorld` (wasmJs)** mirrors the desktop/iOS backends' exact semantics
  (2 object layers → 2 broadphase layers here, copied verbatim from JoltPhysics.js's own
  example rather than collapsed to 1 broadphase layer like desktop/iOS, specifically to stay
  faithful to the upstream reference boilerplate), Kotlin-side `trackedBodyIds: MutableList<Int>`
  (same "no cheap bulk body query" reasoning as the other two backends), raycast via
  `NarrowPhaseQuery.CastRay` with accept-everything base-class filters (`new
  jolt.BroadPhaseLayerFilter()` etc. — the JS-side equivalent of passing `null` filters on
  desktop/iOS).
- **Build-time bundling gap found and fixed, unrelated to the physics API itself**:
  `jolt-physics`'s bundled Emscripten output contains a dead `if (isNode) { await
  import("node:module") ... }` fallback branch (never taken in a browser) that webpack's
  browser target still statically resolves and fails on (`UnhandledSchemeError`) since it has
  no loader for the `node:` URI scheme — `resolve.fallback: { "node:module": false, ... }`
  compiles but does **not** fix this specific error class (confirmed by trying it first; the
  same error persisted), because `UnhandledSchemeError` is raised by webpack's scheme-handling
  step, before normal resolution/fallback is even consulted. Fixed with `new
  webpack.IgnorePlugin({ resourceRegExp: /^node:/ })` in a new
  `samples/hello-cube/webpack.config.d/jolt-physics-node-fallback.js` (Kotlin/Wasm's `browser()`
  webpack task auto-merges any `*.js` under a module's own `webpack.config.d/`, same
  convention Kotlin/JS's webpack DSL already documents — confirmed by inspecting the
  generated `webpack.config.js` and the `KotlinWebpack`/`KotlinWebpackConfig` Gradle-plugin
  classes directly).
- **Found and fixed a second, pre-existing, unrelated bug while setting up browser
  verification**: `samples/hello-cube/src/wasmJsMain/resources/index.html` referenced
  `<script src="sample-hello-cube.js">`, stale from the `sample-hello-cube` → `samples/hello-cube`
  directory rename (the actual webpack output/module name is `hello-cube.js`) — silently
  404'd, so the wasmJs sample has not actually been loadable in a browser since that rename.
  Fixed to `hello-cube.js`.
- **Confirmed in a real browser (headless Chromium via Playwright, `wasmJsBrowserDevelopmentRun`'s
  webpack-dev-server at `localhost:8080`, this dev machine)**: `PhysicsDemo`'s console log
  shows the cube falling and settling for real, not just compiling —
  `PHYSICS DEMO: cube Y = 3.98` (early, mid-fall) → `... Y = 1.0156326` → `... Y = 0.47999975`
  (settled, stable across many subsequent samples) — same ~0.48 rest height (ground top at
  y=0 plus the cube's own 0.5 half-height, within Jolt's normal contact-penetration
  tolerance) the desktop backend's own real-hardware run in slice 2's log entry settled at.
  Screenshots taken ~6s apart show the cube high up mid-air vs. resting on the ground plane
  at the same on-screen position, with the on-screen `PHYSICS: Y` HUD readout matching the
  console log at each point. FPS 100-120 throughout, no console errors from the physics path
  itself (a pre-existing, unrelated WebGPU attachment-format warning — `RenderPipeline`
  configured for `RGBA8Unorm` vs. the swapchain's actual `BGRA8Unorm` — appears in the same
  log and predates this slice; out of scope here).
  `:awake:backend:jolt:compileKotlinWasmJs`, `:samples:hello-cube:compileKotlinWasmJs`, and
  the desktop/Android/iOS-simulator compiles of both modules all pass clean; `awake:backend:
  jolt:desktopTest` (the existing free-fall regression test) still passes, confirming no
  regression to the desktop/Android backend from the `suspend` signature change. New
  `kotlin-js-store/wasm/yarn.lock` committed alongside for reproducible npm dependency
  resolution (Kotlin/Wasm's npm-dependency mechanism generates this the same way Kotlin/JS's
  does).

### D24 — Desktop-only Ktor WebSocket debug-control channel for `sample-hello-cube`
**Decided and implemented (2026-07-12).** Verifying UI interactions on the desktop (Vulkan/
GLFW) build has no automation hook at all — no browser-automation-style tool can click,
screenshot, or read state from a real GLFW window the way the wasmJs build can be driven via
arbitrary JS in the running page. Added a small Ktor WebSocket server so an agent can send a
JSON command and get a JSON state snapshot back, deterministically, with no pixels or
frame-timing races involved.

- **Ktor 3.5.1** (`ktor-server-core`/`ktor-server-cio`/`ktor-server-websockets`/
  `ktor-serialization-kotlinx-json`, new `gradle/libs.versions.toml` entries), CIO engine —
  fewer transitive deps than Netty, fine for a single dev-tooling endpoint. 3.5.1 is the
  latest stable release compatible with this repo's Kotlin 2.4.0 (Ktor 3.x requires Kotlin
  2.0+; confirmed via Maven Central's `ktor-server-cio` metadata). All four dependencies
  scoped to `samples/hello-cube/build.gradle.kts`'s `desktopMain` source set only — this is
  desktop-only, wasmJs/Android don't need it (wasmJs already has a JS-execution hook via
  browser automation; Android has no equivalent network-reachable dev loop today either).
- **`kotlin.serialization` plugin applied module-wide**, not scoped to desktopMain, since a
  Gradle KMP plugin applies per-module, not per-source-set. `DebugSnapshot`/`DebugCommand`
  (see below) live in `commonMain` even though only the desktop debug server currently uses
  them, for two reasons: (a) parsing a `DebugCommand` from JSON is pure logic with no GPU/
  WebSocket dependency, so it's unit-testable per this project's "no app-layer test doubles"
  rule (push logic into small pure functions specifically so it's testable); (b) it keeps the
  door open for another platform's debug hook to reuse the same wire shape later.
- **New common seam, `DebugCameraTarget`** (`samples/hello-cube/src/commonMain/kotlin/
  DebugCameraTarget.kt`) — `getCameraEye`/`setCameraEye`/`getCameraCenter`/`setCameraCenter`,
  same "optional capability a demo can implement" pattern `DebugReadout`/
  `OffscreenPreviewSource` already use (`(current as? DebugCameraTarget)?...` in
  `DemoCatalog`). `CubeDemo` already held its `cameraComponent: Camera` field privately;
  exposed it via this interface instead of widening its visibility for no other reason.
  `PhysicsDemo` didn't hold a camera reference at all before this — added a `cameraComponent`
  field, looked up the same way `CubeDemo.ready()` already does
  (`scene.roots.firstOrNull { it.name == "camera" }?.entity` → `world.get<Camera>(...)`);
  `physics.scene.json` already had a `camera` root node shaped exactly like
  `sample.scene.json`'s, so no scene-JSON change was needed.
- **`DemoCatalog` debug API**: `debugSwitchDemo(index)` (thin wrapper reusing the existing
  private `switchTo`'s suspend/coroutine swap logic, not a duplicate), `debugSetCameraEye`/
  `debugSetCameraCenter` (no-ops if `current` doesn't implement `DebugCameraTarget` — neither
  shipped demo hits this today), and `debugSnapshot(): DebugSnapshot` (demo name, the same
  `debugLines()` the on-screen/console HUD already produces, and the current demo's camera
  eye/center via a small `@Serializable` `DebugVec3` DTO — `Vec3` itself isn't `@Serializable`
  since it's an `awake:base` engine type this sample-only feature has no business annotating).
- **`DebugCommand` parsing** (`samples/hello-cube/src/commonMain/kotlin/DebugCommand.kt`): a
  hand-rolled `type`-discriminator `when`, not kotlinx.serialization's polymorphic/sealed-class
  JSON support — for a 4-command protocol this small, a manual `when` is less machinery than a
  `SerializersModule` + per-subclass registration, and makes "unknown/malformed command"
  (returns `null`, caller skips the frame) an explicit branch rather than a caught exception.
- **`DebugControlServer`** (`samples/hello-cube/src/desktopMain/kotlin/
  DebugControlServer.kt`): `embeddedServer(CIO, port = 8090) { ... }.start(wait = false)` —
  non-blocking, runs on Ktor's own engine thread/coroutines, per `docs/architecture.md`'s
  "one thread owns every Vulkan call" rule. The WebSocket handler never
  touches `DemoCatalog`/ECS state directly: each incoming command is enqueued as a
  `(DebugCommand, CompletableDeferred<DebugSnapshot>)` pair onto a `ConcurrentLinkedQueue`,
  and the handler `await()`s the deferred before sending a response. `Main.kt`'s existing
  per-frame `while (!glfwWindowShouldClose(window))` loop — the one real render-thread owner —
  calls `drainCommands()` once per frame (right before `DesktopGameLoop.startLoop { app.update
  (...) }`), applies each command's effect (`applyDebugCommand`), and completes the paired
  deferred with a fresh `demoCatalog.debugSnapshot()` — the only place any mutation or state
  read actually happens. Completing a `CompletableDeferred` from one thread and awaiting it
  from a coroutine on another is a normal cross-thread synchronization primitive; it never
  touches a Vulkan handle itself, so it doesn't violate the threading rule. Server started
  right after `app.create(window)` (so the first `getState` already reflects a fully-
  initialized demo) and stopped after the render loop exits, before `app.dispose()`.
- **Confirmed with a real WebSocket round trip** (Python 3, `websockets` 16.0 — installed and
  available in this sandbox, no fallback needed), against the actual `./gradlew :samples:
  hello-cube:run` process (`MainKt`, backgrounded):
  - `{"type":"getState"}` → `{"demoName":"CUBE","debugLines":["FPS: 1  FRAME: 17MS","MODE:
    ORBIT","X:-7.3 Y:3.1 Z:0.6"],"cameraEye":{"x":-7.346...,"y":3.115...,"z":0.571...},
    "cameraCenter":{"x":0.0,"y":0.0,"z":0.0}}` — real, non-zero orbiting-camera values, not
    placeholders.
  - `{"type":"switchDemo","index":1}` → `demoName":"PHYSICS"` in the response itself, with
    `cameraEye`/`cameraCenter` immediately reflecting `physics.scene.json`'s authored camera
    (`4,3,8` / `0,0.5,0`) rather than CUBE's leftover values — proves the switch actually
    completed (not just accepted) by the time this same command's response was sent, and
    `debugLines` shows the real Jolt-driven falling cube: `"PHYSICS: cube Y=5.0"` →
    (a follow-up `getState`) `"cube Y=4.96"` — the cube is really falling under physics, not a
    static echo.
  - `{"type":"setCameraEye","x":10,"y":10,"z":10}` → response's `cameraEye` is exactly
    `{"x":10.0,"y":10.0,"z":10.0}`, and `PHYSICS: cube Y=4.85` in the same response confirms
    the physics simulation kept running normally throughout (server traffic didn't stall the
    render loop).
  - `{"type":"switchDemo","index":0}` → back to `"demoName":"CUBE"`, camera reset to a fresh
    `CubeDemo` instance's scene-authored home position (`0,0,5`), then a follow-up `getState`
    shows the orbit auto-rotate already moved it (`0.2, 3.1, 7.4`) — confirms `CubeDemo` was
    genuinely re-constructed/re-`ready()`'d (old camera mutation from the first CUBE session
    didn't leak across the switch-away-and-back).
  - Desktop process's own console log (`DEBUG HUD [...]`/`PHYSICS DEMO: cube Y = ...` lines)
    kept flowing throughout, and the `MainKt` process (confirmed via `jps -l`) was still alive
    and cleanly killable after the test — WebSocket traffic didn't stall or crash the render
    loop.
- Compiles clean: `:samples:hello-cube:compileKotlinDesktop` and `:compileAndroidMain` both
  `BUILD SUCCESSFUL`, no new warnings attributable to this change. `spotlessCheck` passes.
  (`detekt` fails on this module, but identically on a stashed pre-change tree too — a
  pre-existing, unrelated environment issue, not a regression from this feature.)

#### D24 follow-up (2026-07-12): minimap-crash re-investigation using the debug channel

Re-opened `CubeDemo`'s known, unresolved `showMinimap` crash (see the field's own doc comment
history: toggling it on calls `renderToTexture` every frame and previously crashed within
~30-60s with `VK_SUBOPTIMAL_KHR` out of `vkAcquireNextImageKHR` in the main swapchain — a
prior session ruled out a command-buffer leak as the cause but never found the real one).
Used the new `DebugControlServer` to drive the toggle live instead of a GUI click, and
enabled Vulkan validation layers for the first time on this repro.

- **Extended the debug protocol**: `{"type":"setMinimap","enabled":true}` (`DebugCommand.
  SetMinimap`, `DemoCatalog.debugSetMinimap`, a new `DebugMinimapTarget` seam `CubeDemo`
  implements, `minimapEnabled` added to `DebugSnapshot`) — same pattern as `DebugCameraTarget`.
- **Enabling validation layers surfaced a real, unrelated, pre-existing bug first**:
  `GraphicsDevice`'s device-creation path queried `vkEnumerateDeviceExtensionProperties`
  per-instance-layer (a deprecated Vulkan 1.0 pattern the loader/ICD already ignore in
  practice). As soon as any instance layer became discoverable on the host (validation layers
  installed via `brew install vulkan-validationlayers`, scanned by the loader on macOS even
  without `VK_LAYER_PATH` set), that per-layer query failed loader-side ("pLayerName is too
  long or is badly formed"), cascading into `vkCreateDevice` itself failing with
  `VK_ERROR_EXTENSION_NOT_PRESENT` — the app couldn't even start. Fixed by dropping the
  per-layer query entirely (the plain, no-layer `deviceExtensions` query already returns the
  physical device's real extension list; device-level layers are a legacy concept modern
  loaders don't need this for).
- **Found a second, real, independently-confirmed bug**: `GenericGameApplication.dispose()`
  called `destroyBackend()` (destroying the `VkDevice`) *before* `game.dispose()`. But
  `CubeDemo.dispose()`'s own teardown (`cubeMesh.destroy()`/`minimapMaterial.destroy()`/etc.,
  all `vkDestroyBuffer`/`vkDestroyImage` calls) needs that device to still be alive — calling
  them after it's destroyed is undefined behavior. Confirmed as a **real, reproducible
  SIGSEGV** (not theoretical) via a validation-layer-instrumented run: the crash happened
  natively inside `libvulkan.dylib`'s own `vkDestroyBuffer`, with the validation layer's log
  immediately prior showing 14 objects the loader "couldn't find" — i.e. already-destroyed
  device state. **Fixed by reversing the order** (`game.dispose()` first, `destroyBackend()`
  second) in `GenericGameApplication.dispose()`.
- **This dispose-order bug is very likely the actual cause of this session's earlier,
  separately-reported "still crash on close" issue** (a prior `vkDeviceWaitIdle`-in-
  `destroyBackend()` fix did not fully resolve that report) — the ordering bug reproduces on
  *any* app close where `game.dispose()` owns live GPU resources, not specifically the
  minimap. Not yet confirmed against that original report (would need the user to re-test),
  but it is the first concretely-reproduced, root-caused close-time crash found this session.
- **Minimap-toggle verification after both fixes**: driving `setMinimap: true` live via the
  WebSocket channel and leaving it on ran **130+ seconds with no crash, no
  `VK_SUBOPTIMAL_KHR`** — well past the originally-reported 30-60s window. However, the
  validation-layer-enabled run's window closed unexpectedly early on repeated attempts (tens
  of seconds in, cause not conclusively identified — a later run *without* validation layers
  did not exhibit this early-closing behavior at all, suggesting validation-layer log-spam
  overhead stalling the render thread rather than a real bug). **Because of that, the original
  `VK_SUBOPTIMAL_KHR` trigger is not conclusively confirmed fixed** — only ruled out as *not*
  being (solely) the dispose-order bug above. `showMinimap` stays `false` by default; flip to
  `true` to keep exercising this path, and re-open the investigation if `VK_SUBOPTIMAL_KHR`
  resurfaces now that the dispose-order crash it may have been masquerading as is gone.

#### D24 follow-up (2026-07-12): `DebugControlServer` moved into its own `:samples:server` module

Pulled `DebugControlServer` out of `samples:hello-cube`'s `desktopMain` into a new, small,
plain-JVM module (`kotlin.jvm` plugin, same shape as `awake:ecs:benchmark`) so it's reusable
by any future desktop sample without depending on `hello-cube`'s own demo/command types.
Genericized over `TCommand`/`TResponse` (`DebugControlServer<TCommand, TResponse>(port,
parseCommand, encodeResponse)`) — the module now knows nothing about `DebugCommand`/
`DebugSnapshot`'s shape; those stay in `hello-cube`'s `commonMain` as before (still
cross-platform-safe pure logic, still unit-testable without a GPU/WebSocket), and `Main.kt`
supplies the parse/encode functions when constructing the server. This also drops the Ktor
`content-negotiation`/`kotlinx.serialization` dependency this module never actually needed
(the server sends/receives raw text frames only; JSON encode/decode is the caller's job).
Dependency direction: `hello-cube` → `samples:server` (desktop source set only), never the
reverse — same shape as `hello-cube:androidApp` → `hello-cube`.

Verified with the same real WebSocket round trip as D24's original verification, against the
module boundary this time: `getState`/`setMinimap` both round-tripped correctly through
`:samples:server`'s generic server + `hello-cube`'s own `DebugCommand`/`DebugSnapshot`
parse/encode functions. `awake:scene:desktopTest`, `:samples:hello-cube:androidApp:
assembleDebug`, and `spotlessCheck` on both modules all pass.

### D25 — `UiContext.slider`, and public `OrbitCameraSystem.yaw`/`pitch`/`distance`

Added a `slider` widget to `awake:engine:ui`'s custom immediate-mode UI (`UiContext.kt`),
same idiom as `toggle`/`button` — caller-owned value in, updated value returned, built
entirely out of `UiDrawPrimitive.Quad` (track background + proportional filled handle, no
new GPU pipeline work). Drag interaction reuses `button`'s existing `activeId` field rather
than adding a second parallel piece of state — the press-latch/release-clear semantics are
identical for a continuous drag and a press-release click, only the per-frame value
computation differs. The pointer-position-to-value math (`sliderValueFromPointerX`) is a
top-level pure function, not a method on `UiContext`, specifically so it's unit-testable
without an `Input`/GPU-backed instance (see this project's "push logic into pure functions"
convention) — covered by `UiContextTest`: track-edge-to-min/max mapping, midpoint, and
clamping (not extrapolating) for pointer positions outside the track.

`OrbitCameraSystem`'s previously-private `yaw`/`pitch`/`distance` became public `var`s so a
UI slider can read the current value (to draw the handle) and write a new one (when
dragged), without changing `update()`'s own drag/auto-rotate/zoom logic at all. `pitch` and
`distance` keep the same `MIN_PITCH`/`MAX_PITCH`/`MIN_DISTANCE` clamps enforced via a custom
property setter now (rather than only inline in `update()`), so a slider-driven write can't
push the camera past the same limits `update()` itself already enforces. The distance
constructor parameter was renamed `initialDistance` (was `distance`) to avoid shadowing the
new class-body property of the same name — call sites (`OrbitCameraSystemTest`) updated
accordingly, no behavior change. `MIN_PITCH`/`MAX_PITCH`/`MIN_DISTANCE`/`DEFAULT_DISTANCE`
moved from a `private companion object` to a plain (non-private) one, specifically so
`CubeDemo`'s elevation/zoom sliders can size their range off the system's own constants
instead of inventing a separate range that could fight its clamping.

Wired 3 sliders (azimuth `[-PI, PI]`, elevation `[MIN_PITCH, MAX_PITCH]`, zoom/distance
`[MIN_DISTANCE, 20f]`) into `CubeDemo.drawCatalogUi`, below the existing toggle row, visible
only when `cameraMode == CameraMode.ORBIT` (`FREE_FLY` drives the same live `Camera` via its
own independent WASD/mouse-look controls, so these sliders would fight it there).

**Verified:** `:awake:engine:ui:compileKotlinDesktop`, `:awake:scene:compileKotlinDesktop`,
`:samples:hello-cube:compileKotlinDesktop` all compile clean. New `UiContextTest` slider
cases (edge/midpoint mapping, clamping, drag-then-release) pass (5/5 tests in that class, 0
failures). `:awake:scene:desktopTest`'s `OrbitCameraSystemTest` (4/4) and the rest of that
module's suite pass unchanged after the visibility change. `spotlessCheck` passes on both
`awake:engine:ui` and `samples:hello-cube`.

Confirmed on real hardware (desktop, `:samples:hello-cube:run`, real Vulkan window) via the
D24 WebSocket debug channel — **this only proves the `OrbitCameraSystem` property-visibility
change didn't regress the render-camera wiring, not that the slider widget itself
renders/drags correctly** (no GUI-interaction tool available in this sandbox to actually drag
a slider with a real mouse). Repeated `{"type":"getState"}` calls while the CUBE demo's
auto-rotate ran showed `cameraEye` changing frame to frame (e.g. `x:1.18, z:-1.55` →
`x:1.04, z:-1.65` three seconds later, with `y` held exactly constant at `-5.70` across that
same window) — yaw continuing to auto-rotate while pitch/distance stayed put is exactly the
expected behavior post-change, confirming `yaw` is still being written and read correctly as
a public `var`. (Early in this same run, before the window had focus, `cameraEye` swung
erratically across polls with FPS dropping to 1 — consistent with the host's real cursor
transiently interacting with the unfocused GLFW window, not a regression in this change;
behavior stabilized once left alone, as the numbers above show.)

### D4 — Editor base
**Decided: build on [graphyn-editor](https://github.com/ronjunevaldoz/graphyn-editor)**
(Compose Desktop shell + design system) rather than building from scratch.
