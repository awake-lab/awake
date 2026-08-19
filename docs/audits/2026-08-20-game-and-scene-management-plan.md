# Game and Scene management — clean shape

Status: draft, not implemented (except where noted as already landed). Answers "don't we have
a dedicated scene management?" — no, and this is the shape to build one. Builds directly on
today's teardown fix (`Scene.destroy()`, `awake:scene:runtime`) rather than replacing it.

## Two layers, already mostly correct, one real gap

**Game management** — who runs the frame loop, already exists, already clean:

- `Game` interface (`engine/game`) — per-frame behavior contract. Planned rename →
  `AppBehavior` ([2026-08-19-game-naming-generalization-plan.md](2026-08-19-game-naming-generalization-plan.md)), not yet done, orthogonal to this doc.
- `GameApplication` (`engine/game`) — Mediator wiring window/renderer/behavior. Planned rename
  → `AppRuntime`, same doc, same "not yet done, orthogonal" note.
- `SceneGameRuntime` (`scene/runtime`) — a concrete `Game` implementation that *also* owns
  `World`/`Renderer`/ECS systems/`UiContext`. Not a separate concept from "Game management" —
  it's the scene-runtime-flavored way of being one.

**Scene management** — who owns "what's currently loaded" — is the real gap. Today, landed
just now:

- `Scene` (`scene/runtime`, renamed from `SceneInstance`) — a live loaded scene:
  `world` + `roots: List<SceneNodeInstance>` + `renderableRequests`.
- `Scene.destroy()` — recursive teardown, destroys every entity a scene's node tree created,
  not just top-level roots (today's fix — was the actual correctness bug).

What's still missing: a place that *owns* "the current scene" and exposes switching as one
safe operation. Right now that logic is hand-rolled inside Studio's `ExampleLoader`
(`samples/studio/examples/ExampleLoader.kt`) — tied to `StudioExamples`, not reusable by any
other game built on `SceneGameRuntime`.

## The clean shape: `SceneManager`, generic, in `scene/runtime`

```kotlin
// awake:scene:runtime — new file
/** Owns the currently-loaded [Scene], if any. The only thing that may create or destroy a
 * [Scene] against [world] -- a caller that instantiates one directly and never registers it
 * here has opted out of safe switching, same as bypassing any other single-owner resource. */
class SceneManager(private val world: World) {
    var current: Scene? = null
        private set

    /** Tears down whatever's currently loaded (if anything), then instantiates [document].
     * One call, not a manual teardown-then-load pair -- there is no window where a caller can
     * forget the teardown half. */
    fun switchTo(document: SceneDocument): Scene {
        current?.destroy()
        val scene = SceneLoader.instantiate(document, world)
        current = scene
        return scene
    }

    /** Tears down the current scene without loading a replacement -- e.g. app shutdown. */
    fun close() {
        current?.destroy()
        current = null
    }
}
```

`SceneGameRuntime` gains one, since it already owns `world`:

```kotlin
// SceneGameRuntime.kt
val sceneManager: SceneManager by lazy { SceneManager(world) }
```

Any `Game` built on `SceneGameRuntime` — not just Studio — now gets safe scene switching for
free, the same "fix it once, every consumer benefits" reasoning `Scene.destroy()` already
followed.

## `ExampleLoader` becomes a thin Studio-specific wrapper

`ExampleLoader` keeps everything that's genuinely Studio's own concern — the `StudioExamples`
list, the preloaded-document cache, the inspector's `authoredRenderables`/`boundsByEntity`
tracking, `onActivated` hooks for examples needing extra wiring (glTF viewer, particle demo) —
but delegates the actual swap to `SceneGameRuntime.sceneManager` instead of hand-rolling
teardown:

```kotlin
// ExampleLoader.kt, target shape
fun activate(exampleId: String, runtime: SceneGameRuntime) {
    val example = requireNotNull(StudioExamples.find { it.id == exampleId }) { "Unknown example '$exampleId'." }
    val document = requireNotNull(documents[exampleId]) { "Example '$exampleId' was not preloaded." }
    val instance = runtime.sceneManager.switchTo(document)   // was: teardown() + SceneLoader.instantiate(...)
    val library = runtime.requireAssetLibrary()
    authoredRenderables.clear()
    boundsByEntity.clear()
    instance.renderableRequests.forEach { request ->
        runtime.world.add(request.entity, library.resolve(runtime, request))
        authoredRenderables[request.entity] = request.meshRenderer
        StudioMeshBounds[request.meshRenderer.mesh]?.let { boundsByEntity[request.entity.id] = it }
    }
    example.onActivated?.invoke(instance, runtime)
}

fun teardown() {
    runtime.sceneManager.close()
    authoredRenderables.clear()
    boundsByEntity.clear()
}
```

`ExampleLoader`'s own `activeScene` field goes away entirely — `SceneManager.current` is now
the single source of truth for "what's loaded," not duplicated in two places.

## What this does NOT solve (explicitly out of scope, don't bundle in)

- **Play-mode snapshot/revert** (raised earlier today — Unity reverts to an in-memory
  pre-play snapshot on Stop, Awake currently reloads from disk instead). `SceneManager` is the
  right *place* this would eventually live (`snapshot()`/`revertToSnapshot()` alongside
  `switchTo`), but it's new behavior, not implied by this shape — a separate follow-up.
- **Play-mode hides editor chrome.** Was blocked on today's teardown bug; that block is now
  lifted, but hiding chrome is still a separate UI change, not part of this doc.
- **A `SceneManager` per non-Studio consumer's actual usage.** This doc proposes the shape;
  no other sample currently switches scenes at runtime, so there's nothing else to migrate
  onto it yet.

## Sequencing

1. `SceneManager` (this doc) — small, mechanical, `awake:scene:runtime`.
2. `ExampleLoader` migrates onto it (small, mechanical, `samples:studio`).
3. Play-mode-hides-chrome, now unblocked — separate task.
4. Play-mode snapshot/revert — separate, larger follow-up, only if actually wanted.
