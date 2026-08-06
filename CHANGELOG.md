# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

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
- `:awake:scene:core` gained `SpinControl`/`SpinSystem` (generic entity rotation) and
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

### Removed

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
  `:awake:scene:core` now owns `Transform`/`Name`, and `:awake:scene:rendering` owns
  `Camera`/`Light`/`MeshRenderer` plus `RenderSystem`. `:awake:scene:physics` now owns
  `PhysicsBody`/`PhysicsSystem`, `:awake:scene:controls` now owns
  `OrbitControl`/`FreeFlyControl`/`FollowControl`/`MovementControl` plus their camera
  systems, and `:awake:scene:runtime` now owns `SceneGameRuntime`, `SceneGameSpec`, the
  scene document model, and `SceneAssetLibrary`. `TransformSystem` moved to
  `:awake:scene:core`; `PlayerControlSystem` moved into `:awake:scene-dsl` (it needs
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

## [1.0.0-SNAPSHOT] - YYYY-MM-DD

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

[unreleased]: https://github.com/ronjunevaldoz/awake/compare/v1.0.0...HEAD

[1.0.0-SNAPSHOT]: https://github.com/ronjunevaldoz/awake/compare/v0.0.1...v0.0.2
