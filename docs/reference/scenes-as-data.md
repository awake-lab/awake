# Scenes as data

Goal: a scene is a file the app loads, not Kotlin code the app compiles. This is the
foundation for an in-app editor (nowhere to save to without it) and an asset browser
(nowhere to place assets without it).

## What already exists

More than expected. In `:awake:scene:runtime`:

- `SceneDocument` / `SceneNode` — the serializable model, with `kotlinx.serialization`
- `SceneLoader` — `encode`, `decode`, `loadFromResource`, `instantiate(document, world)`
- `SceneValidation` — `validate` / `requireValid`
- `SceneAssetLibrary` — asset handle resolution
- `awake/scene/runtime/src/commonTest/resources/scenes/mvp.scene.json` — a real round-tripped scene

So loading a scene from JSON into a `World` already works and is tested.

## Component shape (decided, done)

`SceneNode` used to carry one nullable field per component (`camera`, `light`,
`meshRenderer`). Adding a component meant four edits, one of them a breaking change to the
public `SceneInstantiationAdapter`, and forgetting the `instantiateNode` line dropped the
data silently at load with no error. `PbrMaterial` drifted this way within an hour of being
added.

Components are now a sealed `SceneComponent` list:

```kotlin
data class SceneNode(
    val name: String? = null,
    val transform: SceneTransform = SceneTransform(),
    val components: List<SceneComponent> = emptyList(),
    val children: List<SceneNode> = emptyList(),
)
```

The adapter has a single `attachComponent`, and its `when` is exhaustive — a new component
stops the build until it's mapped, instead of failing silently. Cost: "at most one camera
per node" is no longer a type guarantee, so `SceneValidator` checks it.

`transform` stays a named field. Every node has exactly one, and the parent link is
encoded by `children` nesting.

## Versioning (done)

`SceneDocument.version` defaults to `SCENE_SCHEMA_VERSION`. `decode` throws
`SceneSchemaVersionException` on a document from a newer build rather than degrading — a
future scene may use components this loader can't construct, and dropping them yields a
plausible-looking but wrong scene. A missing `version` key reads as 1, so pre-versioning
files still load.

Adding a new `SceneComponent` does not need a bump.

## Discriminator

`SceneComponent` sets `@JsonClassDiscriminator("component")`. The kotlinx default is
`type`, which `SceneLight.type` collides with — kotlinx refuses to serialize a subclass
whose property shadows the discriminator. Renaming that one field would only defer the
clash to the next component with a `type`. It's declared on the type, not in `SceneJson`,
so a caller-supplied `Json` still round-trips.

## The rotating cube loads from a file (done)

`assets/scenes/rotating-cube.scene.json` describes the camera, cube, ground, and light.
`RotatingCubeDemo.onActivate` calls `SceneLoader.instantiate`, resolves renderables through
`SceneAssetLibrary`, and looks its entities up by name. Procedural geometry works unchanged:
`assets { mesh("cube") { ... } }` registers a factory, so the file references `"cube"` the
same way it would reference a model.

The document is loaded in the scene DSL's `onReady` because `readResourceBytes` is suspend
and `onActivate` is not — the same reason `Duck.gltf` preloads there.

## What's still missing

**1. Camera mode.** `CameraComponent` lives in `:awake:scene:controls`, which
`:awake:scene:runtime` does not depend on. Serializing it would invert that layering, so it
needs a decision rather than a quick field.

**2. The other two demos** still build entities imperatively.

**3. No save path.** `encode` exists, but nothing writes a file — there's no
platform-neutral file-write in `awake:base`, only `readResourceBytes`. Deliberately not
built yet: nothing calls it until there's an editor to save from.

## Order to build the rest

1. **Migrate the remaining demos**, then make the demo registry a list of scene files.
2. **Decide camera mode's home** — either move `CameraComponent` down into
   `:awake:scene:rendering`, or let the sample own that component's serialization.
3. **Add a write path** when an editor needs one, not before.
