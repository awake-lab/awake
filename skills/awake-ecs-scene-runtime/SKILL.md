---
name: awake-ecs-scene-runtime
description: >
  How to consume Awake's real ECS (awake-ecs + awake-scene + awake-scene-authoring) from a
  game or sample -- World/Entity/component basics, the sceneGame{}/GameModuleDsl.scene{}
  DSL surface, when to reach for SceneGameRuntime instead of GameUiRuntime (and why they
  don't compose inside one demo), and the TransformSystem-overwrites-worldMatrix trap.
  This is a how-to reference for consuming the ECS, distinct from the awake-ecs-performance-engineer
  and awake-scene-runtime-engineer personas (which guide work ON the ECS/scene-authoring internals
  themselves).
license: Apache-2.0
metadata:
  author: awake
  last-updated: '2026-08-04'
  keywords:
    - Awake
    - ECS
    - World
    - Entity
    - SceneGameRuntime
    - GameUiRuntime
    - sceneGame
    - RenderSystem
    - TransformSystem
    - scene3d-playground
---

## When to Use This Skill

Use when a game or sample needs real 3D content -- entities with `Transform`/`MeshRenderer`/
`Camera` driven by the actual ECS `World` -- not just 2D UI chrome. Load this before wiring a
new demo/sample onto `SceneGameRuntime`, or before deciding whether a feature needs the ECS at
all.

**Trigger keywords:** ECS, World, Entity, spawn entity, SceneGameRuntime, sceneGame,
GameModuleDsl.scene, RenderSystem, TransformSystem, MeshRenderer, real 3D scene, awake-scene,
awake-scene-authoring, GameUiRuntime vs SceneGameRuntime.

---

## Two runtimes, two jobs -- pick one per demo, don't mix

Awake has two separate `Game`/`GameModule` implementations. **They don't compose inside one
demo** -- each owns its own `render()` call, so installing both side by side means two
independent per-frame passes, not one integrated one (see `SceneGameDslTest.gameModuleCanOwnSceneAndUiComposition`,
the only place they've ever coexisted, which installs `scene{}` and `ui{}` as two *sibling*
`GameModule`s, not one runtime doing both).

| | `GameUiRuntime` (`engine/game-authoring`) | `SceneGameRuntime` (`awake:scene` + `awake:scene:authoring`) |
|---|---|---|
| Owns | `UiContext` only, no `World` | `UiContext` **and** a real `World` |
| 3D content | Only via `provideDrawCalls` escape hatch (a lambda a demo sets to smuggle one `Camera`+`List<DrawCall>` into the runtime's one `renderer.draw()` call) | Real ECS entities -- `RenderSystem` walks `world.family<Transform, MeshRenderer>()` every frame and calls `renderer.draw()` itself |
| Install via | `gameModule { ui { ... } }` | `gameModule { scene("name") { ... } }` (or `GameSpecDsl.ecs { ... }`) |
| Use when | Pure 2D UI, dashboards, no real scene graph | Any demo/game with actual 3D entities, cameras, or a scene graph |

If a demo needs BOTH real ECS entities and rich UI chrome (sidebar, controls, HUD) in the same
frame, use `SceneGameRuntime` for both -- see "UI chrome on `SceneGameRuntime`" below. Don't
reach for `provideDrawCalls` for new work; it's the pre-ECS pattern this skill's own migration
(`samples/scene3d-playground`) replaced.

---

## `World`/`Entity`/component basics

```kotlin
// Bare entity + explicit component add
val entity: Entity = world.create()
world.add(entity, Transform(position = Vec3(0f, 1f, 0f)))
world.add(entity, MeshRenderer(mesh, material))

// Sugar: create + default-construct + configure in one call (component type must be
// default-constructible, e.g. Transform())
val camera: Entity = world.spawn<Camera> { it.isPrimary = true }
// (SceneCamera/scene.components.Camera's fields are all `val` -- spawn{} can't mutate them
// after construction; replace the whole component instead, see "Replacing a component" below)

// Read a component
val transform: Transform? = world.get(entity, Transform::class)

// Destroy -- removes every component too
world.destroy(entity)
```

**Replacing a component** (e.g. a component with `val` fields, or any per-frame value that
changes wholesale): `world.add(entity, newComponentInstance)` -- `add` replaces the existing
component of that type if one is already present, it's not add-only.

```kotlin
// scene.components.Camera is `data class Camera(val camera: CoreCamera, val isPrimary: Boolean)`
// -- both val, so a per-frame camera update replaces the whole component:
world.add(cameraEntity, Camera(computeCamera(), isPrimary = true))
```

**Querying two components together** (the exact pattern `RenderSystem` itself uses):

```kotlin
val family = world.family<Transform, MeshRenderer>()
val transforms = family.componentsA()
val meshRenderers = family.componentsB()
var i = 0
while (i < family.size) {
    val drawCall = DrawCall(meshRenderers[i].mesh, meshRenderers[i].material, transforms[i].worldMatrix)
    i += 1
}
```

Index-based, not a lambda callback -- `componentsA()`/`componentsB()` are parallel arrays, walk
them together by index rather than expecting a `family.forEach { entity, a, b -> }` shape.

---

## The `sceneGame{}` / `GameModuleDsl.scene(name){}` DSL

```kotlin
fun myGameModule(): GameModule = gameModule {
    scene("my-scene") {
        // Register gameplay/demo systems only. The scene DSL appends built-in
        // infrastructure systems (TransformSystem + RenderSystem) at build time.
        // Do not hand-register another RenderSystem here, or the scene submits twice
        // per frame and scene3d UI/viewport presentation can blink.
        frameSystem("demo-driver") { MyDemoDriverSystem(this) }

        // Suspend, runs once before the first frame -- the only place to do suspend
        // work (asset loads) before entities need it.
        onReady { /* preload suspend resources here */ }

        // Runs at the fixed simulation rate; `this: SceneGameRuntime`, has `world`/`renderer`.
        update { delta, inputSnapshot -> /* game logic, entity spawn/despawn */ }

        // Runs at render rate; `this: SceneGameRuntime`, receives real viewport size as params
        // (SceneGameRuntime doesn't store viewportWidth/viewportHeight in a field the way
        // GameUiRuntime does -- see "UI chrome" below for why that matters).
        overlay { viewportWidth, viewportHeight -> /* UI drawing */ }
    }
}
```

Other entry points: `entity(name) { transform { } ; meshRenderer(mesh = "x", material = "y") }`
for declaratively-authored scene documents, `assets { mesh("x") { renderer.createMesh(...) } }`
for a named mesh/material library resolved lazily by name, `cameraEntity(...)`/`meshEntity(...)`
sugar (`awake-scene-authoring`'s `EntityExtensions.kt`) for the common camera/mesh entity shapes. Real
signatures live in `awake/scene/authoring/.../runtime/SceneGameDsl.kt` -- read it before guessing a
DSL method exists; it's a small, closed surface, not an open-ended builder.

---

## Spawn-on-activate / destroy-on-deactivate (the per-demo-page pattern)

A multi-page sample (see `samples/scene3d-playground`) needs each page to own its own entities
without leaking a previous page's mesh when the user switches pages. The pattern:

```kotlin
data class MyDemo(
    val onActivate: SceneGameRuntime.() -> Unit = {},   // spawn this page's entities
    val onDeactivate: (World) -> Unit = {},             // destroy exactly those entities
    val onUpdate: SceneGameRuntime.(delta: Float) -> Unit = {}
)
```

The shell's `update { delta, _ -> }` block tracks which page was active last frame; on change,
calls the old page's `onDeactivate(world)` then the new page's `onActivate(this)`. Keep each
page's spawned `Entity` references in `private var` fields on the page's own object so
`onDeactivate` knows exactly what to destroy -- don't scan the whole `World` for "this page's
entities", there's no tag for that by default.

---

## UI chrome on `SceneGameRuntime`

`GameUiRuntime.frame { }` / `.frameStats()` (the root full-viewport box + fps/frame-time HUD
helpers) only exist on `GameUiRuntime`. `awake:scene` ships equivalents --
`awake/scene/.../runtime/SceneGameFrame.kt`'s `SceneGameRuntime.frame(viewportWidth,
viewportHeight) { }` and `SceneGameRuntime.frameStats(): SceneFrameStats` -- same shape, just
taking `viewportWidth`/`viewportHeight` as explicit params (from the `overlay{}` block's own
signature) instead of reading a stored field. Everything below the root `frame { }` call
(`row`, `column`, `shadcnSidebar`, `text`, `uiContext.pushTheme(...)`) is receiver-agnostic --
plain `UiContext`-produced scopes, not typed to either runtime -- so porting existing UI chrome
from a `GameUiRuntime.() -> Unit` overlay to a `SceneGameRuntime.() -> Unit` one only requires
changing the function's own receiver type and the `frame{}`/`frameStats()` call sites, not
anything inside the layout tree.

---

## Traps

**`TransformSystem` overwrites a directly-set `worldMatrix`.** `awake/scene/.../systems/
`TransformSystem.kt` recomputes every entity's `Transform.worldMatrix` from
`position`/`rotation`/`scale` (+ parent chain) every time it runs. In the `scene {}` DSL this
system is installed automatically as built-in infrastructure, before the built-in
`RenderSystem`. If you set `worldMatrix` directly (e.g. from a parsed glTF node transform, or a
hand-built `Mat4`), expect the infrastructure transform pass to recompute it from component
fields on the next frame unless the component model explicitly preserves that authored matrix.
This isn't hypothetical -- it is exactly what `samples/scene3d-playground`'s glTF viewer demo
has to account for (see `GltfViewerDemo.kt`'s own doc comment).

**Do not register `RenderSystem` manually inside `scene {}` DSL modules.**
`SceneGameDsl.build()` calls `installInfrastructureSystems()`, which appends a built-in
`TransformSystem` and `RenderSystem(renderer)` after user systems. Registering another
`frameSystem("render") { RenderSystem(renderer) }` in a sample such as `scene3DPlaygroundModule()`
causes two `renderer.draw()` calls per frame (`drawUi`, `draw`, `draw`) and can show up as
scene3d UI/viewport blinking even when UI-only samples remain stable.

**Components with `val` fields can't be mutated in place.** `scene.components.Camera` and
`MeshRenderer` are `data class`es with `val` fields -- `world.get(entity, Camera::class)!!
.camera = newCamera` doesn't compile. Replace the whole component instead: `world.add(entity,
Camera(newCamera, isPrimary = true))` (see "Replacing a component" above). `Transform`'s fields
ARE `var` (it's `Poolable`, reused across spawns), so `world.get(entity,
Transform::class)?.worldMatrix = newMatrix` works directly.

---

## Headless pixel-baseline regression testing

A visual bug in a demo's mesh/shading (e.g. wrong per-vertex normals producing a "warped"-looking
cube) is easy to miss with manual screenshot review -- slow, and this project's AppleScript-driven
screenshot automation is unreliable against its custom Vulkan UI (clicks don't register, captures
sometimes grab the wrong window). The fix is a real, deterministic, CI-runnable render comparison
instead of eyeballing screenshots.

**Pattern** (see `awake/backend/vulkan/src/desktopTest/.../RendererHeadlessPixelBaselineTest.kt`
for the original, and `samples/scene3d-playground/src/desktopTest/.../RotatingCubePixelBaselineTest.kt`
for a sample-level example built on it):

1. Construct a headless Vulkan pipeline -- `GraphicsDevice.createHeadless()` +
   `SwapchainManager.createHeadless(width, height)` -- no GLFW window, no real swapchain/surface.
2. Build the real mesh/camera the demo actually uses (reuse the demo's own `internal` geometry/
   camera code where module visibility allows it -- see `RotatingCubePixelBaselineTest`'s reuse of
   `rotatingCubeGeometry` and its `core.math.Camera` setup -- don't hand-copy vertex
   data a second time if the real source is reachable).
3. Freeze animation for a reproducible frame: `ManualTimeController(autoPlay = false, hours =
   <fixed value>)` instead of real elapsed time.
4. `renderer.renderToTexture(target, camera, drawCalls)` + `renderer.readPixels(target)`, then
   `awake:engine:testing`'s `comparePixels(actual, baseline)` against a committed `.rgba` golden
   resource (raw bytes matching `readPixels()`'s own output format, not a PNG).
5. On mismatch, dump both actual/baseline as PNGs under `build/test-failures/...` (see
   `writeRgbaPng` in either test file) so a developer can look at what actually rendered instead of
   only reading a diff-pixel count.

**Traps hit building the sample-level version:**
- A sample with no prior `desktopTest` source set needs one added explicitly, with a dependency on
  `awake:backend:vulkan` (for the headless device/renderer classes) and `awake:engine:testing`
  (for `comparePixels`), plus the same native-lib env wiring (`-Djava.library.path`,
  `VK_ICD_FILENAMES`, `DYLD_FALLBACK_LIBRARY_PATH`) `awake:backend:vulkan`'s own `desktopTest` task
  already sets -- copy that wiring, don't rediscover it.
- `RenderPipeline`'s default vertex/fragment entry point is `"main"`, but this project's
  naga-generated (WGSL -> SPIR-V) `.spv` shaders keep `vertexMain`/`fragmentMain` as the real
  entry point names -- using the default silently fails pipeline creation with
  `VK_ERROR_INITIALIZATION_FAILED`.
- A demo's live-viewport default camera framing (tuned for an interactive window) can be far too
  small/off-center for a fixed small offscreen target (e.g. 128x128) -- tune zoom/orbit/pitch
  specifically for the baseline frame, still through the demo's own real camera code, rather than
  reusing the live defaults verbatim.
- `SceneGameRuntime`'s only public construction path is the `game{}`/`scene{}` DSL, which always
  ends in a real `renderer.draw()` swapchain present -- there's no headless "run the DSL, read back
  a texture" path today (`SceneGameDslTest` only ever exercises it against a no-op recording
  `Renderer`). Driving the demo's own geometry/camera code directly against the raw `Renderer`
  API (skipping `SceneGameRuntime`/`World` entirely) is the pragmatic choice until that gap is
  closed -- note this tradeoff inline in the test if you take it, so a future reader doesn't assume
  full ECS coverage that isn't actually there.
- A second `GraphicsDevice.createHeadless()` call in the same JVM process (e.g. a second `@Test`
  method in the same class) can throw `VK_ERROR_EXTENSION_NOT_PRESENT` -- confirmed by isolating
  the second test alone (`--tests` filtered to just it), which got past device creation fine.
  Cross-instance native/global Vulkan loader state collides across sequential create/destroy
  within one process; nothing wrong with either test's own render logic. Fix: one shared headless
  device/renderer per test class, looping over every (angle, baseline) case inside a single
  `@Test`, rather than one device per case.
- A curved/warped-looking edge seen only in a screen recording, not in a live or `screencapture`d
  lossless screenshot at the same angle, is almost always H.264 ringing (codec artifact on a
  fast-moving, high-contrast diagonal edge at a low bitrate) -- not a real render bug. Verify with
  a lossless capture before trusting a compressed recording as evidence of a geometry defect;
  pixel-scan the suspect edge column-by-column (numpy over the raw screenshot) rather than
  eyeballing a zoomed, resampled crop, since resampling itself can manufacture the appearance of a
  curve.

---

## Related Skills

- `awake-ecs-performance-engineer` -- persona for working ON `awake-ecs`'s own storage/query
  internals (entity arena, component pools, family indexing), not for consuming it from a game
- `awake-scene-runtime-engineer` -- persona for working ON `awake-scene`/`awake-scene-authoring`
  themselves (new components, new systems, DSL surface changes)
- `awake-game-framework-engineer` -- persona for `GameUiRuntime`/`engine`/`game authoring` itself

---

## Changelog

| Date | Change |
|---|---|
| 2026-08-04 | Initial release -- written alongside `samples/scene3d-playground`'s full migration from `GameUiRuntime` to `SceneGameRuntime` (the first sample to actually use the ECS runtime end to end). |
| 2026-08-04 | Added "Headless pixel-baseline regression testing" section, written alongside `RotatingCubePixelBaselineTest.kt` -- the first sample-level (not just `awake:backend:vulkan`-level) pixel-regression test in this repo. |
| 2026-08-04 | Added two more traps to that section: two headless `GraphicsDevice`s in one JVM process collide (`VK_ERROR_EXTENSION_NOT_PRESENT`), and a screen recording's compression artifacts can look exactly like a real warp bug -- both hit while investigating a reported cube-warping regression that turned out to be a false alarm (video-codec ringing, not a render defect), which is also why `RotatingCubePixelBaselineTest` gained a second angle case. |
