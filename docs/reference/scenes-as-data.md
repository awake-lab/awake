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

## What's missing

**1. `SceneNode` can't express what the demos use.** It covers `transform`, `camera`,
`light`, `meshRenderer`, `children`. The playground's entities also carry `SpinControl`,
`PbrMaterial`, and `CameraComponent`'s mode. A demo serialized today would lose them.

**2. No demo loads from a document.** Every one builds entities imperatively in
`onActivate`. `SceneLoader.instantiate` has no production caller.

**3. No save path.** `encode` exists, but nothing writes a file — there's no
platform-neutral file-write in `awake:base`, only `readResourceBytes`.

## Order to build it

1. **Extend `SceneNode`** with the missing components. Add them as nullable fields, same
   shape as `meshRenderer`. Round-trip test per component.
2. **Migrate one demo** — the rotating cube is the right pick: it uses `Transform`,
   `MeshRenderer`, `SpinControl`, `PbrMaterial`, `Camera`, and `Light`, so it exercises
   every field at once. Keep the imperative path until the loaded scene matches it
   visually, then delete it.
3. **Add a write path** to `awake:base` (`expect fun writeFile`) so `encode` has somewhere
   to go. Desktop first; web needs a download or origin-private-filesystem shim.
4. **Migrate the rest** and make the demo registry a list of scene files.

Step 1 is the only one that touches published API. Steps 2-4 are sample-local.

## Open question

Whether a component's serialized form belongs on `SceneNode` (one growing struct, simple,
but every new component edits a published type) or a keyed map of component payloads
(open-ended, no API churn, but loses schema validation). Worth deciding before step 1
rather than after.
