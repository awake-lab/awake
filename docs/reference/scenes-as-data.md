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

## What's still missing

**1. Component coverage.** `SpinControl` and `CameraComponent`'s mode still have no
`SceneComponent`. `PbrMaterial` now does.

**2. No demo loads from a document.** Every one builds entities imperatively in
`onActivate`. `SceneLoader.instantiate` has no production caller.

**3. No save path.** `encode` exists, but nothing writes a file — there's no
platform-neutral file-write in `awake:base`, only `readResourceBytes`.

## Order to build the rest

1. **Add the remaining components** as `SceneComponent` subtypes. Each is now additive: a
   new subtype plus an adapter branch the compiler demands.
2. **Migrate one demo** — the rotating cube uses `Transform`, `MeshRenderer`,
   `SpinControl`, `PbrMaterial`, `Camera`, and `Light`, so it exercises everything at once.
   Keep the imperative path until the loaded scene matches it visually, then delete it.
3. **Add a write path** to `awake:base` (`expect fun writeFile`). Desktop first; web needs
   a download or origin-private-filesystem shim.
4. **Migrate the rest** and make the demo registry a list of scene files.
