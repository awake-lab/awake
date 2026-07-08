# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Removed

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