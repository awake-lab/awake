# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Known issues

- **`UiContext.measuring` is declared but never toggled true during a trial measurement
  pass.** `UiContext.kt`'s hit-test/click-claim suppression guards (`!measuring` on
  `tryClaimActive`/`releaseActiveIfMatches`/`hitTest`) and `isMeasuringInternal()` all
  assume this flag goes true during a wrap-content trial pass; it's hardcoded `false` and
  never set anywhere. Confirmed real by instrumentation: a wrap-content trigger's content
  lambda genuinely runs 30 times per frame during a click/release (`ShadcnPopoverCheckboxClickProbeTest`,
  `awake/ui/designsystem/src/commonTest/.../ShadcnPopoverCheckboxClickProbeTest.kt`), and
  every one of those 30 invocations processes the click as real, unsuppressed. Despite
  that, the probe's exact shape (one interactive control alone in a wrap-content trigger's
  popover) has NOT reproduced a swallowed click across 3 independent runs, including after
  package 5's wrap-content measure-pass changes. The originally reported symptom
  (`samples/studio/.../StudioPills.kt`'s worked-around "Debug" menu) has *multiple* sibling
  interactive controls sharing one button group's `activeId` — that contention is the
  likely missing ingredient this probe doesn't reproduce alone. Fix direction if
  revisited: toggle `measuring = true` for the duration of a trial-pass content
  invocation in `measureColumnContentInternal`/`measureRowContentInternal`, restoring the
  suppression the existing guards already assume.

- **A wrap-height `shadcnToggleGroup` creates a 100,000px hit rectangle.** Its internal
  `fillMaxHeight()` resolves against the trial-measurement sentinel, so a viewport header pushes
  the rail offscreen and then intercepts the popup's clamped-on-screen menu clicks. It is not an
  active/focus claim failure or the nesting defect fixed in `d3f270c7`. **Blocks the viewport
  header**, and matters anywhere a wrap-height toggle group can overlap a popup. Diagnosis and
  the required regression shape are in `docs/tasks/2026-08-11-studio-layout-design.md`.

- **Glyph stem weight varies with sub-pixel phase.** The same character repeated on one line
  renders 1px and 2px stems in alternation (`'i' @14px: [1,1,1,2,1,2,1,2,...]`), which reads as
  "some characters thin, some not". MTSDF was expected to close this and did not: the field
  resolves an edge analytically, but each quad still lands at a different sub-pixel phase as
  fractional advances accumulate. Recorded as `knownStemWidthSpread = 1` in
  `GlyphStemWeightTest` -- which is currently `@Ignore`d, because its probe cannot isolate
  individual stems (a repeated 'i' at 12px collapses into one run at every threshold tried).
  Disabled deliberately rather than left green and lying. **Top open issue.**
- **Studio shows no custom cursor.** `SceneGameRuntime` has the cursor in its frame effects
  and discards it, unlike `GameUiRuntime`, and no service registration exposes the runtime to
  an entry point. `runVulkanDesktopGame`'s `cursor` defaults to null, so every request is
  dropped.
- **Studio does not use the engine's camera system.** `awake:scene:controls` provides
  `CameraMode` (FirstPerson, ThirdPerson, Cinematic, TopDown) with `CameraInputSystem`
  handling drag and scroll per mode, and `scene3d-playground` uses it. Studio defines a
  parallel `CameraPresetMode` (Orbit, Front, Top) with hand-rolled preset math and drag that
  only responds in Orbit, so Cinematic and TopDown are unreachable from studio and the
  viewport is not draggable in the other modes.
- **The resizable handle's geometry does not match upstream shadcn.** Real shadcn is a `w-px`
  in-flow Separator whose grab area is an absolutely positioned pseudo-element, plus
  react-resizable-panels' `hitAreaMargins`. Splitting ours into a 1dp layout cost and a
  separate grab margin was tried and reverted: it re-proportioned every panel, and because it
  required hit-testing outside `interact()` it killed the hover state the resize cursor reads.
  Closing this needs `interact()` to accept a hit rect distinct from its layout rect.
- **`docs/reference/ui-status.md` is stale** — it predates the MTSDF and resizable work.
- **Studio viewport canvas padding is wrong.** Reported, not yet diagnosed.
- **Studio's tool rail is inert.** All five buttons (Layers, Grid, Environment, History, Panels)
  dispatch `SelectTool` and update the store, and `activeTool` is then read only by `IconRail`
  itself to decide which button looks pressed. Nothing else reads it. Its PLACEMENT is fine --
  Blender and Unity both float tools at the viewport edge -- the wiring is what is missing. See
  `docs/tasks/2026-08-11-studio-layout-audit.md`.
- **Studio entity selection is inert.** `InspectorState.selectedEntityId` is written by the store
  and read by nothing; `InspectorPanel` lists every named entity regardless. There is no UI to
  select from, which is what the hierarchy dock is for.
- **`StudioShellLayoutTest.panelsDockFlushToEveryFrameEdge` fails.** Pre-existing, confirmed
  against clean `main` with all local changes stashed. Never diagnosed.

### Added

- GPU-based 3D Camera System: a single `CameraComponent` carrying a `CameraMode` enum
  (`FirstPerson`, `ThirdPerson`, `Cinematic`, `TopDown`) plus an `ActiveCamera` tag
  component. One `CameraSystem` drives every mode and only processes entities tagged with
  `ActiveCamera`, preventing input drift in inactive modes.
- `CameraInputSystem`: Handles hotkey switching (F1-F4) between camera modes.
- `MatrixRelativeMovementSystem`: Dynamically transforms movement intents from
  `MovementControl` based on the active camera's orientation, ensuring view-relative WASD.
- `PlayerInputSystem`: Simplified hardware-to-ECS intent mapping for kinematic movement.
- `Vec3` math helpers: `set`, `lerp`, and `add` for easier mutation.
- `awake-ecs` and `awake-scene` as publishable artifacts. `awake-ecs` is now the pure
  sparse-set ECS runtime; Awake-specific scene components and systems moved to
  `awake-scene`.
- Maintained ECS family handles for one- and two-component queries, including
  component-only iteration for systems that do not need entity handles.
- GPU skinning: `Quat` (`awake-base` `core.math`), `GltfSkinning.kt` (skin/animation data
  types) and `SkinnedAnimationPlayer` (a generic glTF animation sampler) in `awake-base`
  `core.mesh.gltf`, `VertexFormat.PositionNormalColorSkin`, and
  `Renderer.drawSkinnedMesh(mesh, material, model, jointPalette)` — a separate staged-draw
  path from `draw()`/`DrawCall` because a skinned mesh's vertex layout doesn't match the
  one shared main 3D pipeline. `Renderer.createMaterial(...)` gained a `uniformFloatCount`
  parameter so a skinned material's joint-palette uniform buffer can be sized correctly.
- Texture sampling: `Bitmap.toRgba8Bytes()` (`awake-base` `core.graphics.BitmapRgba8.kt`),
  materials/textures/images parsing on `GltfDocument`/`GltfParser`/`GltfMesh`
  (`GltfMesh.baseColorImageBytes`, `toInterleavedPositionNormalColorUv()`),
  `VertexFormat.PositionNormalColorUv`, and `Renderer.drawTexturedMesh(mesh, material,
  model)` — same separate staged-draw pattern as GPU skinning.
- `:awake:scene:scene-core` gained `SpinControl`/`SpinSystem` (generic entity rotation) and
  `:awake:scene:controls` gained `LookAtControl`/`LookAtCameraSystem` (rotation-only
  tracking) plus `PrimaryOrbitCamera` (a plain lifecycle helper, not an ECS `System`, for
  a UI-driven debug camera entity) — extracted from boilerplate duplicated across
  `samples:scene3d-playground` demos.
- `Mesh.format` and `DrawCall.extraUniformFloats` — a `MeshRenderer` entity's mesh now
  determines which GPU pipeline draws it (`Renderer.pipelinesByFormat` on the Vulkan
  backend), instead of `RenderSystem`/`draw()` only ever supporting one fixed vertex
  format. `:awake:scene:rendering` gained `SkinnedPose`, an optional `MeshRenderer`
  add-on component carrying a GPU-skinned mesh's joint palette. `OrbitCameraDemoRig`
  gained a public `entity` accessor so a demo can attach these to its placement entity.

- Real-time shadow mapping for the directional scene light: a depth pre-pass renders
  shadow casters from the light's point of view into a `ShadowMap`, and the lit pipeline
  compares against it with manual PCF (this repo's `VkSamplerCreateInfo` binding has no
  `compareEnable`/`compareOp`, so hardware Dref sampling isn't available). Runtime-gated by
  `Renderer.shadowsEnabled`.
- `Renderer.wireframe` mesh view mode. Vulkan uses a `VK_POLYGON_MODE_LINE` companion
  pipeline sharing the fill pipeline's loaded shaders; WebGPU has no polygon-mode
  equivalent, so it derives a line-index buffer from the triangle indices and draws
  `LineList` topology instead.
- Web (wasmJs) bitmap decoding, via `createImageBitmap()` plus an offscreen-canvas
  readback. `Bitmap` creation became `suspend` across all four platform actuals as a
  result — decoding in the browser is inherently asynchronous.
- A build gate (`verifyShaderBinaries`) that fails `check` when a GLSL source drifts from
  its checked-in `.spv`. Vulkan loads the compiled binaries at runtime, never the adjacent
  sources, so a stale binary silently ignores shader edits — this was found the hard way
  after 7 of 10 checked-in binaries turned out to already be stale.

### Removed

- Legacy camera systems and controllers: `OrbitCameraController`, `OrbitCameraSystem`,
  `FollowCameraSystem`, `LookAtCameraSystem`, and `FreeFlyCameraSystem`.
- `PlayerControlSystem` and legacy control components (`OrbitControl`, `FreeFlyControl`,
  `FollowControl`, `LookAtControl`).
- `Renderer.drawSkinnedMesh`/`drawTexturedMesh` — the separate staged-draw bypass a skinned
  or textured mesh needed before `RenderSystem`/`draw()` supported more than one vertex
  format. `GltfViewerDemo`/`SkinnedMeshDemo` (`samples:scene3d-playground`) now spawn a real
  `MeshRenderer` entity (plus `SkinnedPose` for the skinned case) instead.
- `DemoApplication`'s per-second `Random.nextInt` drawable switching on desktop. Leftover
  debug code that silently overrode `DemoDrawer`'s real click-to-select mechanism — selecting
  an item in the drawer never stuck for more than a second. Not a feature; just noise.
- `VulkanApplication.glfwWindowHandle` field. Replaced by a real `expect fun createSurface`/
  `destroySurfaceWindow` pair in `awake-vulkan` (see `awake-vulkan/src/*/kotlin/io/github/
  ronjunevaldoz/awake/vulkan/VulkanSurface.kt`) — the field existed only to let `destroy()`
  know whether to call GLFW teardown; now `destroySurfaceWindow()` is called unconditionally
  and is simply a no-op on Android, so there's nothing left to track.
- `buildSrc`'s hand-rolled `signing-publication-conventions.gradle.kts` (`maven-publish` +
  `signing` applied and configured by hand, including a manual sign-task-dependency
  workaround). Replaced by the vanniktech `maven-publish` plugin applied directly in
  `awake-core/build.gradle.kts` — see "Changed" below for why it isn't a shared buildSrc
  convention plugin anymore.

### Changed

- `awake-ecs` `System` is now scheduler-free: the stale `frequency` property and
  `SystemFrequency` enum were removed from the public ECS core API. Scene scheduling now
  belongs to `awake-scene`/`awake-scene-dsl` registration via `SceneSystemPhase` plus
  explicit fixed-step and per-frame system helpers.
- `awake-scene` now keeps only the small `NavMesh` contract; the demo navmesh bootstrap,
  hardcoded demo geometry, recast4j dependencies, and proof tests moved to
  `samples:scene3d-playground`. `SceneRuntime` is deprecated in favor of
  `SceneGameRuntime`/`sceneGame {}`.
- Authored gameplay systems `ChaseAiSystem` and `PlayerMovementSystem` moved out of
  published `awake-scene` and into `samples:scene3d-playground`. Engine-owned systems should
  be reusable behavior; sample/game-specific systems belong with the authored gameplay that
  owns their rules.
- Scene internals started splitting behind the published `awake-scene` facade:
  `:awake:scene:scene-core` now owns `Transform`/`Name`, and `:awake:scene:rendering` owns
  `Camera`/`Light`/`MeshRenderer` plus `RenderSystem`. `:awake:scene:physics` now owns
  `PhysicsBody`/`PhysicsSystem`, `:awake:scene:controls` now owns
  `OrbitControl`/`FreeFlyControl`/`FollowControl`/`MovementControl` plus their camera
  systems, and `:awake:scene:runtime` now owns `SceneGameRuntime`, `SceneGameSpec`, the
  scene document model, and `SceneAssetLibrary`. `TransformSystem` moved to
  `:awake:scene:scene-core`; `PlayerControlSystem` moved into `:awake:scene-dsl` (it needs
  `ui-core`). `:awake:scene-dsl` now depends on the specific scene leaf modules it uses
  instead of the whole `:awake:scene` facade. Public package names stay stable.
- **Maven Central publishing migrated off Sonatype's legacy OSSRH staging API**
  (`s01.oss.sonatype.org`), which Sonatype sunset in June 2025 — publishing would have failed
  outright had it been run. Now uses the vanniktech `maven-publish` plugin targeting the
  current Central Portal (`./gradlew :awake-core:publishToMavenCentral -PisMainHost=true`).
  Credential property names changed accordingly: `ossrhUsername`/`ossrhPassword` →
  `mavenCentralUsername`/`mavenCentralPassword` (now a Central Portal user token, not a
  Sonatype JIRA account), `signing.keyId`/`signing.secretKey`/`signing.password` →
  `signingInMemoryKeyId`/`signingInMemoryKey`/`signingInMemoryKeyPassword`. CI's
  `.github/workflows/build-and-publish.yml` JDK bumped 11 → 17 to match `jvmToolchain(17)`.
- `awake-demo/desktopApp/src/jvmMain/kotlin/main.kt`'s `fun main()` now launches the real
  Compose UI (`application { Window { MainView() } }`) again — it had been commented out in
  favor of a raw OpenGL/AWT `createFrame` loop, which meant `:awake-demo:desktopApp:run`
  never actually showed the Compose demo (buttons, Enable-Vulkan switch, FPS text) on any
  renderer. That OpenGL loop is preserved as `runOpenGlFrameDemo()` — a plain function, not
  currently wired to a Gradle task — since it's still a legitimate manual smoke test, just no
  longer what `run` should launch by default.
- `VulkanApplication.createSurface()` no longer branches on `window is Long` to distinguish
  Android's `Surface` from desktop's GLFW window handle — it delegates to the new
  `expect fun createSurface` in `awake-vulkan` instead. Behavior is unchanged; this is a
  structural cleanup (see Phase 1c in `docs/MVP_PLAN.md` for the full rationale).
- `awake-ecs` component stores now use primitive sparse/dense arrays instead of
  `MutableList` storage, reducing structural add/remove overhead in the benchmark harness.
- Glyph coverage-alpha now gets stem darkening (`pow(alpha, 1/1.45)`) on both backends.
  `SwapchainManager` deliberately picks a `_UNORM` (not `_SRGB`) format because authored
  colors are already sRGB-encoded bytes, which means all alpha blending happens on
  gamma-encoded values and text rendered visibly too thin. Darkening the stems is what
  FreeType/Skia do when linear blending isn't available.
- Icon centering offsets snap to whole pixels, matching what text already did via
  `resolveGlyphPx`. An odd width difference otherwise landed a whole glyph on a
  half-pixel, blurring every edge.
- Shadow lookups use a slope-scaled bias (`max(0.0090 * (1 - dot(N,L)), 0.0015)`) instead
  of one constant. A single constant can't serve both face-on and grazing surfaces: large
  enough to stop grazing-angle acne means detaching face-on contact shadows.
- `VulkanGameApplication` takes one `additionalPipelines: Map<VertexFormat, GameShaderSet>`
  instead of paired `skinnedShaderSet`/`skinnedVertexFormat`/`texturedShaderSet`/
  `texturedVertexFormat` parameters, so adding a fourth vertex format no longer means
  adding a fifth and sixth constructor parameter.
- `VulkanGameApplication` no longer builds a whole `Material` purely to borrow its
  descriptor-set layout — a half-constructed instance that `createResources` had to never
  be called on, and that teardown had to partially destroy. `Material.createDescriptorSetLayout`
  is now a companion function and only the layout handle is held.
- Dark-theme `card` and `sidebar` colors corrected to oklch lightness 0.205, matching the
  published shadcn spec (they were 0.168 and 0.158).

## [0.1.0-dev.4] - 2026-08-11

UI correctness and the first piece of the studio layout redesign. Ships the general fix for a
bug class that had produced three separate shipped defects.

### Fixed

- **Widget-state writes made during a measuring pass are dropped.** `column()` re-executes its
  content against a scratch context sharing the real, persisted `WidgetState` but with blank
  input, so anything writing state from that pass corrupted what the real pass read moments
  later in the same frame. It had shipped three times: the resizable handle's drag anchor was
  deleted every frame so dragging did nothing, `animatedHeight` kept a stale height across a
  collapse, and a popup nested one container deeper would not open. The first two were fixed
  per-widget, which left every other stateful widget exposed. Guarding `UiStateValue`'s setter
  covers every hook at once, so a new widget cannot reintroduce it by forgetting to guard itself.
- **Studio's display toggles moved to a viewport-edge pill.** Wireframe and shadows sat in the
  top bar, which was a scoping error -- they govern how one viewport draws, not the document.
  Kept out of the tool rail deliberately: that rail is modal, these are independent booleans.
- **`popup()` honours `Modifier.widthIn()`/`heightIn()`**, so `max-w-*` is expressible for
  popup-based components instead of hard-coded. `maxWidth` applies before measurement as well as
  after, so wrapped content reflows within the cap rather than being clipped.

### Added

- Studio layout audit, target design and an SVG wireframe --
  `docs/tasks/2026-08-11-studio-layout-audit.md`, `-design.md` and `-layout.svg`. The audit found
  three inert controls; the design maps every region to components that already exist and
  sequences the work in independently shippable phases.

## [0.1.0-dev.3] - 2026-08-11

Font rendering: the atlas moved to MTSDF and the verification gaps that let font bugs ship were
closed. Text rendering is NOT finished at this tag -- stem weight still varies with sub-pixel
phase, tracked under Unreleased / Known issues.

### Fixed

- **`rasterize()`'s missing-font placeholder is no longer glyph-shaped.** It drew a filled rect
  in the glyph's own colour, inset 25%, which reads as a blob of text and -- worse -- measures as
  one: probes scanning for ink found placeholder geometry and reported it as glyph metrics. That
  produced a confident but wrong "glyphs render at 0.6x their metrics" investigation and left two
  font gates green while they measured placeholders. Now a solid magenta box over the glyph's
  full bounds, so neither a reader nor a pixel measurement can mistake it for text, and the
  `font` parameter documents that it is required whenever a frame contains glyphs.

- **Glyph ink rendered at ~0.90x of its own metrics** (sub-pixel at 12-14px, past a pixel from
  16px up): the font-atlas generator sized render quads to the glyph outline but UV rects to
  outline + crop bleed + a texel snap, squeezing the padded atlas region into an outline-sized
  quad. Quads are now derived from the snapped sample rect (quad and UV cover the same texels
  1:1) and outline-true `inkMetricsEm` ships separately so `capHeightEm`/baseline/advance
  metrics stay ink-exact. The per-glyph snap slack was also what scattered baselines; the
  Chromium baseline-fidelity drift map is re-measured with an honest probe (transparent
  background, alpha-channel coverage, degenerate-run guard) and every text-bearing snapshot
  signature is re-recorded. `GlyphAbsoluteSizeTest` now gates absolute ink size against
  `capHeightEm * size` -- the external-truth check this repo never had.

  Verified on screen by Ron June Valdoz, 2026-08-11. That confirms this fix specifically, not
  text rendering overall -- stem weight still varies with sub-pixel phase, see Known issues.

## [1.0.0-SNAPSHOT] - not released

Placeholder retained from the Keep a Changelog template. It was never filled in, which is why
every entry above accumulated in Unreleased instead of being cut into a section at tag time --
`v0.1.0-dev.1` through `dev.3` all shipped without one. Sections are cut at tag time from here
on.

### TODO

- [ ] Render triangle via Vulkan (Android, iOS, Desktop)
- [ ] To improve game loop
- [ ] Implement Fixed & Variable timestep
- [ ] Create ECS (Entity, Component, System)
- [ ] Create renderer that can draw single and batch instance
- [ ] Model instancing to provide faster rendering for batch models
- [ ] Animation instancing to provide faster rendering for batch model with animations
- [ ] UI Text, etc. (similar to compose-ui??)
- [ ] Physics Networking (low priority)

### Changes

- Improve vulkan struct to java vice-versa converter

### Added

- Vulkan Android support.
- Initial support for Android, iOS, and desktop OpenGL.
- Implemented using Compose Multiplatform for increased flexibility.
- Experimental high-quality text rendering (TTF).
- GlslValidator plugin for pre-compiling glsl to spv in the demo project.

### Known Issues

- Possible memory leak when using Vulkan.
- Incomplete text rendering.
- Desktop OpenGL texture rendering is leaking.

[unreleased]: https://github.com/awake-lab/awake/compare/v1.0.0...HEAD

[1.0.0-SNAPSHOT]: https://github.com/awake-lab/awake/compare/v0.0.1...v0.0.2
