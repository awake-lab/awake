# DSL convenience sugar — mesh generation + camera/light one-liners

Status: landed (`6ed450ac5`). Scoped from comparing Awake's real DSL (`game { }`/`GameDsl`,
`scene { }`/`SceneGameDsl`, `entity { }`/`SceneBuilder`) against Kool's `KoolApplication { addScene { } }`
example. The DSL *mechanism* is already equivalent (nested trailing-lambda builders,
`@DslMarker`-scoped) — these are the two real, narrow gaps found, not a redesign. A third gap
(inline shader assignment, Kool's `shader = KslPbrShader { }`) is the already-separately-scoped
ASL project ([2026-08-19-asl-single-source-shader-sketch.md](2026-08-19-asl-single-source-shader-sketch.md)) — not part of this doc.

## Gap 1 — procedural mesh generation

Today, `entity()` resolves meshes by name through an asset library (`assets { }`/
`renderables { }`) — there's no inline primitive-geometry builder. `MeshGeometry`
(`engine/render/contract/.../mesh/MeshGeometry.kt`) is already the right target type:
plain data (`vertices: FloatArray`, `indices: IntArray`, `format: VertexFormat`), no backend
coupling — a generator just needs to produce one.

```kotlin
// New file, e.g. awake:engine:render:contract, package render.mesh.generate
class MeshGenerateScope {
    private var geometry: MeshGeometry? = null
    internal fun build(): MeshGeometry = requireNotNull(geometry) { "generate { } produced no geometry -- call cube()/... inside the block." }

    fun cube(size: Float = 1f, colored: Boolean = false) {
        geometry = buildCubeGeometry(size, colored)
    }
    // sphere(), plane(), etc. -- same shape, added as real primitives are needed.
}

fun generate(block: MeshGenerateScope.() -> Unit): MeshGeometry =
    MeshGenerateScope().apply(block).build()
```

Consumed from the scene DSL as a `mesh { }` trailing-lambda slot inside `entity()` — not
`mesh(generate { })`, which mixes idioms (a DSL-builder result passed as a positional value
into a second call, instead of one consistent nested-scope read). `mesh` takes a lambda
matching `generate`'s own shape:

```kotlin
fun SceneBuilder.mesh(block: MeshGenerateScope.() -> Unit) {
    mesh(generate(block))   // delegates to the existing asset-path mesh(MeshGeometry) setter
}
```

```kotlin
entity("cube") {
    mesh { generate { cube(colored = true) } }
    // ... material, transform, etc. same as today
}
```

Doesn't replace the asset-library path (`mesh("name")` by string still works) — adds a second
one for inline-generated geometry, consistent with the DSL's existing trailing-lambda idiom
throughout instead of introducing a value-passing exception.

## Gap 2 — camera/light convenience one-liners

Both already have real underlying primitives (`Modifier().camera(...)`, a light-bearing
entity) — this is pure sugar, no new capability, extension functions on `SceneGameDsl`/
`SceneBuilder`:

```kotlin
// SceneGameDsl or SceneBuilder extension
fun SceneBuilder.defaultOrbitCamera(target: Entity? = null) {
    entity("camera", Modifier().camera(CameraMode.ThirdPerson, target = target))
}

class LightingScope internal constructor(private val builder: SceneBuilder) {
    fun singleDirectionalLight(
        direction: Vec3 = Vec3(-1f, -1f, -1f),
        color: Color = Color.WHITE,
        intensity: Float = 1f,
    ) {
        builder.entity("light", Modifier().with(Light(type = Light.Type.Directional /* + direction/color/intensity */)))
    }
}

val SceneBuilder.lighting get() = LightingScope(this)
```

## Gap 3 — the nested `scene { scene { } }` this doc originally shipped with

Earlier draft of this doc's "Resulting shape" had `scene { scene { } }` -- not a typo, but
not fixed either. Two real `scene` overloads exist at two different levels:
`AppSpecDsl.scene(name, block: AppSpecDsl.() -> Unit)` installs a scene *module* into the app
spec (spec-build time, no `World` yet); the DSL-internal `scene(name, block: SceneBuilder.() -> Unit)`
declares the actual entity layout, deferred until a real `World` exists at runtime
(`SceneGameDsl.kt`'s own doc comment explains why: entity-spawning code needs a live `World`,
which only exists once the runtime starts). Legitimate reason, but the common case (no
`systems { }`/`assets { }`/`onReady { }`, just entities) shouldn't have to write both levels.

Fix: a convenience overload on `AppSpecDsl` collapsing both into one call for the simple case:

```kotlin
fun AppSpecDsl.scene(name: String? = null, block: SceneBuilder.() -> Unit) {
    scene(name) { scene(block) }   // delegates to the existing two-level DSL, hidden from the caller
}
```

## Resulting shape

```kotlin
fun main() = runVulkanDesktopGame(
    game {
        window { title = "My Game"; size(1280, 720) }
        scene {
            defaultOrbitCamera()
            entity("cube", Modifier().with(SpinControl())) {
                mesh(generate { cube(colored = true) })
            }
            lighting.singleDirectionalLight(color = Color.WHITE)
        }
    },
)
```

One `scene { }` in the common case, using the Gap 3 overload above -- the two-level DSL still
exists underneath for when `systems { }`/`assets { }`/`onReady { }` are actually needed.

Same shape as the Kool example this was compared against, using only real, verified Awake
primitives underneath.

## Scope check

- No new module needed — `generate { }` lives in `engine/render/contract` (already has
  `MeshGeometry`), the camera/light sugar lives in `scene/authoring` (already has
  `SceneBuilder`/`SceneGameDsl`).
- `Light`'s actual constructor shape (direction/color/intensity fields) needs checking against
  real source before the `singleDirectionalLight` signature above is final — sketched from
  memory of `Light.Type.Directional` seen earlier this session, not re-verified for this doc.
- Not addressed here, intentionally: inline shader assignment (ASL, separate plan), any
  `onUpdate { }`-style inline per-entity behavior shorthand (deliberately not proposed — that
  would bypass the ECS component+system discipline this codebase consistently uses elsewhere,
  not a gap worth closing the same way as 1 and 2).
