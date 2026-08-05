# Awake Scene

Scene-graph components and systems built on top of [`awake-ecs`](../awake-ecs/README.md):
`Transform` (with parent/child hierarchy), `MeshRenderer`, `Camera`, `Light`, plus the
`Name`, `TransformSystem`/`RenderSystem`, and the scene runtime that turns `scene.json`
into live ECS entities. This is the layer between the bare ECS and a real game — read
`awake-ecs`'s README first if you haven't.

## Installation

```kotlin
implementation("io.github.ronjunevaldoz:awake-scene:1.0.0-SNAPSHOT")

repositories {
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
}
```

## Components

- **`Transform`** — `position`/`rotation`/`scale` (each a `Vec3`), an optional `parent:
  Entity?` for hierarchy, and a `worldMatrix: Mat4` that `TransformSystem` fills in every
  frame. `localMatrix()` builds the local transform from position/rotation/scale.
- **`Name`** — optional label for scene hierarchy/editor views. It is a small runtime
  component, not part of the serialized document itself.
- **`MeshRenderer`** — wraps a `Mesh` + `Material` (from `awake-vulkan`) for `RenderSystem`
  to turn into a `DrawCall`.
- **`Camera`** — wraps `awake-core`'s math `Camera` (eye/center/up/fov/near/far) plus
  `isPrimary: Boolean`; `RenderSystem` renders through whichever `Camera` has
  `isPrimary = true`.
- **`Light`** — `color`/`intensity`/`type` (`Directional` or `Point`). Not yet consumed by
  `RenderSystem` — it's a data holder for a future lighting pass.

## Systems

- **`TransformSystem`** — walks every entity with a `Transform`, resolves `worldMatrix`
  parent-before-child via a memoized depth-first traversal (with cycle detection: a
  `Transform.parent` cycle throws `IllegalStateException`), and writes the result back onto
  each `Transform`. Safe to call every frame — it re-derives from scratch each time (so
  reparenting an entity by mutating `Transform.parent` directly is picked up next frame)
  while reusing its own scratch arrays instead of allocating fresh state per call.
- **`RenderSystem`** — finds the primary `Camera`, builds a `DrawCall` for every
  `Transform`+`MeshRenderer` pair, and hands them to a `Renderer` (from `awake-core`) in one
  `draw()` call per frame.

## Runtime

The scene runtime keeps the document format separate from the renderer-specific asset
binding step. A `scene.json` file becomes a live `World` first; actual `MeshRenderer`
components can be attached later once the app resolves meshes and materials.

```mermaid
flowchart LR
    "scene.json" --> "SceneLoader"
    "SceneLoader" --> "SceneInstance"
    "SceneInstance" --> "World"
    "SceneInstance" --> "renderableRequests"
    "renderableRequests" --> "MeshRenderer binder"
```

- `SceneDocument`, `SceneNode`, `SceneTransform`, `SceneCamera`, `SceneLight`, and
  `SceneMeshRenderer` define the serializable scene contract.
- `SceneLoader.loadFromResource(...)` parses bundled JSON, `SceneDocument.instantiate(...)`
  builds entities and hierarchy, and `SceneInstance.attachRenderableComponents(...)` is the
  handoff point for actual GPU-backed mesh/material construction.
- `Name` keeps scene hierarchy labels available at runtime for editors and debug views.
- `SceneGameRuntime` is the canonical game-loop integration path. The older manual
  `SceneRuntime` bootstrap is kept only as a compatibility bridge while scene modules are
  being split.

## Quick start

```kotlin
import io.github.ronjunevaldoz.awake.core.math.Camera as MathCamera
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.core.renderer.Renderer
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.components.Camera
import io.github.ronjunevaldoz.awake.scene.components.MeshRenderer
import io.github.ronjunevaldoz.awake.scene.components.Transform
import io.github.ronjunevaldoz.awake.scene.systems.RenderSystem
import io.github.ronjunevaldoz.awake.scene.systems.TransformSystem

fun buildScene(world: World, renderer: Renderer, mesh: Mesh, material: Material) {
    // Camera
    val cameraEntity = world.create()
    world.add(
        cameraEntity,
        Camera(
            camera = MathCamera(
                eye = Vec3(0f, 0f, 5f),
                center = Vec3(0f, 0f, 0f),
                fovYRadians = 60f * (kotlin.math.PI.toFloat() / 180f),
                near = 0.1f,
                far = 100f
            ),
            isPrimary = true
        )
    )

    // A parent/child pair -- the child's world matrix will include the parent's transform
    val parent = world.create()
    world.add(parent, Transform(position = Vec3(0f, 1f, 0f)))
    world.add(parent, MeshRenderer(mesh, material))

    val child = world.create()
    world.add(child, Transform(position = Vec3(1f, 0f, 0f), parent = parent))
    world.add(child, MeshRenderer(mesh, material))
}

val transformSystem = TransformSystem()
lateinit var renderSystem: RenderSystem   // constructed once you have a Renderer

fun onFrame(world: World, delta: Float) {
    transformSystem.update(world, delta)   // must run before RenderSystem each frame
    renderSystem.update(world, delta)
}
```

`TransformSystem` must run before `RenderSystem` in your per-frame system order —
`RenderSystem` reads `Transform.worldMatrix`, which only `TransformSystem` writes.
When using `awake:scene-dsl`, those built-in systems are registered as frame systems for
you and appended after user systems.

## Building your own systems on top

Scene systems are plain `awake-ecs` `System`s — there's nothing special about the two
above beyond what components they read/write. A typical addition follows the same shape:

```kotlin
class SpinSystem(private val radiansPerSecond: Float) : System {
    override fun update(world: World, delta: Float) {
        world.queryEach<Transform> { _, transform ->
            transform.rotation.y += radiansPerSecond * delta
        }
    }
}
```

If your system needs per-frame scratch state (a traversal buffer, a visited-set), keep it
as reused instance fields rather than allocating fresh collections in `update()` — and if
that state would otherwise be keyed by `Entity`, prefer indexing by `entity.id` into a
plain array instead of a `Map<Entity, _>`/`Set<Entity>`. `Entity` is a value class, so
using it as a hash-based collection key forces a box allocation and `hashCode()`/`equals()`
per access; `TransformSystem` itself was rewritten this way after profiling showed it
costing real CPU (see [docs/ecs-benchmark-scorecard.md](../docs/ecs-benchmark-scorecard.md)).
