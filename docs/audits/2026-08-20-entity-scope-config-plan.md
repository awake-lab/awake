# Entity scope config — folding `EntityModifier` into `entity { }`'s block

Status: landed (`2b014ff25`). Follows the [Modifier -> EntityModifier rename](2026-08-20-dsl-convenience-sugar-plan.md)
that dropped the `fun Modifier()` wrapper mirroring Compose's `Modifier`. That mirroring was
the stated reason `EntityModifier` was a positional param separate from `entity()`'s trailing
block (Compose keeps `modifier` and `content` apart the same way). With that goal gone, the
split has no forcing function left -- this doc asks whether it should still exist.

## Current shape

```kotlin
entity("cube", EntityModifier().transform(y = 2f).with(SpinControl())) {
    entity("child", EntityModifier().transform(x = 1f))
}
```

`entity()` on `SceneBuilder`:

```kotlin
fun entity(
    name: String? = null,
    modifier: EntityModifier = EntityModifier(),
    block: SceneBuilder.() -> Unit = {},
): Entity {
    val currentEntity = world.create()
    if (name != null) world.add(currentEntity, Name(name))
    modifier.actions.forEach { action -> action(world, currentEntity) }   // <- deferred flush
    if (parentEntity != null) world.get<Transform>(currentEntity)?.parent = parentEntity
    val childContext = SceneBuilder(world, parentEntity = currentEntity)
    childContext.block()   // <- block is ONLY for children, cannot touch currentEntity's components
    return currentEntity
}
```

`EntityModifier` is a deferred-action list (`actions: MutableList<(World, Entity) -> Unit>`)
specifically because it's built *before* any entity exists -- `.transform().with()` chains run
standalone, then `entity()` creates the entity and flushes the queue onto it.

## Proposed shape

```kotlin
entity("cube") {
    transform(y = 2f)
    with(SpinControl())
    entity("child") {
        transform(x = 1f)
    }
}
```

Component config and child nesting share one block. Since `entity()` already calls
`world.create()` before running any block, config calls (`transform()`, `camera()`,
`meshRenderer()`, `with()`) can apply directly (`world.add(entity, ...)` on the spot) instead
of queuing into a deferred action list -- `EntityModifier.actions` goes away entirely, not just
its constructor wrapper.

## The real design problem: one receiver, two jobs

Folding config calls onto `SceneBuilder` directly (the block's current receiver) breaks at the
root: `scene { }`'s own block is also `SceneBuilder.() -> Unit`, but the scene root has no
entity to configure (`parentEntity == null` there). If `transform()`/`with()` become
`SceneBuilder` members, nothing stops:

```kotlin
scene {
    transform(y = 2f)   // compiles, but writes to... what? there is no entity here
}
```

Today this is impossible by construction: `EntityModifier` only exists as a value you build and
hand to `entity()`, never as `scene { }`'s own receiver. Folding config into the block loses
that compile-time guarantee unless the two roles get two distinct receiver types.

**Recommended fix**: a new `EntityScope` (name TBD) distinct from `SceneBuilder`:

- `SceneBuilder` (root, `scene { }`'s receiver) keeps exactly one member: `entity(name) { block: EntityScope.() -> Unit }`.
- `EntityScope` wraps `world` + a non-null `entity: Entity` (no `actions` queue -- writes go
  straight through). Exposes `transform()`, `camera()`, `meshRenderer()`, `with()` as direct
  members/extensions, **and** its own `entity(name) { }` for children, recursing into a fresh
  `EntityScope` the same way `SceneBuilder` recurses into a fresh `SceneBuilder` today.

This keeps the invariant `EntityModifier` gave for free (config calls only valid where an
entity actually exists) without reviving the two-arg split.

## Blast radius

Direct DSL usage (`entity(name, modifier) { }` / `EntityModifier()` chains) is confined to
`scene/authoring` itself:

- `SceneBuilder.kt` -- `entity()` signature, owns the change
- `EntityModifier.kt` -- becomes `EntityScope.kt`, drops the `actions` queue
- `EntityBlueprints.kt` -- `cameraEntity()`/`meshEntity()` sugar, rewritten to the block shape
- `SceneGameDsl.kt` -- re-exports, import cleanup
- `SceneDslTest.kt`, `SceneAppLifecycleDslTest.kt` -- rewritten call sites

Consumers (`samples/studio`, `samples/ui-showcase`) call the `cameraEntity()`/`meshEntity()`
sugar or drive scenes through `SceneDocument`/glTF loading, not hand-authored `entity()` chains
-- checked via repo-wide grep, zero direct `EntityModifier`/positional-`modifier` call sites
outside `scene/authoring`. External blast radius is effectively zero right now; the whole cost
is internal to one module.

## Pros and cons

**Pros**

- Drops `EntityModifier.actions` deferred-queue indirection entirely -- config calls write
  straight through, one less mechanism to explain.
- No separate "build a modifier value, then pass it positionally" step -- config and structure
  read as one flat block, matches the rest of the DSL's trailing-lambda idiom (`scene { }`,
  `game { }`) instead of being the one exception.
- Compile-time safety preserved (`transform()` etc. only valid where an entity exists) if
  `EntityScope` stays a distinct type from `SceneBuilder`.
- Closer to the Kool `addScene { addMesh { } }` example this comparison started from.

**Cons**

- Two near-identical recursive types instead of one clean split: `SceneBuilder.entity()` and
  `EntityScope.entity()` both create-a-child-and-recurse -- that logic risks living twice
  instead of once. `EntityScope` could subtype/delegate to `SceneBuilder` to avoid the literal
  duplication, but that's an extra layer to design, not free.
- Loses "build once, apply to many" composability. Today `EntityModifier` is a value --
  nothing stops constructing one and handing it to several `entity()` calls (not currently done
  anywhere in the repo, confirmed by grep, but the block shape can't express it at all without a
  `fun EntityScope.applyPreset(block: EntityScope.() -> Unit)` indirection of its own).
- One more concept for DSL authors: today it's "one builder type (`EntityModifier`) plus one
  structural type (`SceneBuilder`)"; proposed is "two scope types, each doing both jobs at its
  own level." Not obviously simpler to a newcomer despite being less code.
- Bigger diff than the rename just landed -- `entity()`'s signature, both recursion paths, and
  every DSL test rewritten, versus a mechanical 6-file substitution.

## Addressing the cons

**Duplicate recursion (con 1) -- fixed by delegation, not reimplementation.** `EntityScope`
doesn't reimplement child-creation; it wraps the same `SceneBuilder` and forwards to it. All
create-entity/wire-parent/recurse logic stays in exactly one place, same as today:

```kotlin
class SceneBuilder internal constructor(
    private val world: World,
    private val parentEntity: Entity? = null,
) {
    fun entity(name: String? = null, block: EntityScope.() -> Unit = {}): Entity {
        val currentEntity = world.create()
        if (name != null) world.add(currentEntity, Name(name))
        if (parentEntity != null) world.get<Transform>(currentEntity)?.parent = parentEntity
        val childBuilder = SceneBuilder(world, parentEntity = currentEntity)
        EntityScope(world, currentEntity, childBuilder).block()
        return currentEntity
    }
}

class EntityScope internal constructor(
    private val world: World,
    val entity: Entity,
    private val childBuilder: SceneBuilder,
) {
    fun with(component: Any) {
        @Suppress("UNCHECKED_CAST")
        world.add(entity, component::class as KClass<Any>, component)
    }

    // Pure delegate -- zero duplicated recursion logic.
    fun entity(name: String? = null, block: EntityScope.() -> Unit = {}): Entity =
        childBuilder.entity(name, block)
}
```

`transform()`/`camera()`/`meshRenderer()` become `EntityScope` extensions the same shape as
today's `EntityModifier` extensions, just calling `with(...)` / `world.ensure(...)` directly
instead of queuing an action.

**Lost reusable-value composability (con 2) -- not lost, just reshaped as a lambda preset.**
Kotlin lets a stored receiver-lambda be invoked inside a matching-receiver block by name (the
same mechanism Compose's own `@Composable fun Preset() { }` convention relies on):

```kotlin
val spinner: EntityScope.() -> Unit = {
    transform(y = 2f)
    with(SpinControl())
}

entity("e1") { spinner() }
entity("e2") { spinner() }
```

Full parity with "build once, apply to many" -- arguably closer to this DSL's existing idiom
(everything else is already a trailing lambda) than a builder-chain value was.

**One more concept to learn (con 3) -- mitigated by making it the *only* concept.** Keep
`SceneBuilder` effectively invisible: its sole public member is `entity()`, seen only once at
`scene { }`'s root. Every block after that is `EntityScope` -- config and nested `entity()`
both live on it. Net mental model: "one block type, entered once you're inside an entity,"
which is simpler than today's two explicitly-constructed types (`EntityModifier` value +
`SceneBuilder` block), not more.

**Bigger diff (con 4) -- shrinks once con 1's delegation avoids writing new recursion code.**
Confirmed blast radius (Blast radius section above) is already bounded to 6 files in
`scene/authoring`; delegation means `EntityScope` is mostly new plumbing, not new logic, so the
actual diff size tracks closer to the mechanical rename already shipped than a from-scratch
redesign.

Net: with delegation instead of duplication, and lambda presets instead of builder values, three
of the four cons resolve to "reshaped, not lost." The fourth (extra type to learn) inverts into
a simplification once `SceneBuilder` stops being a second visible block type. Worth implementing.

## Non-goals / open items

- No dual API. Given near-zero external consumers, this should be a hard cut -- old
  `entity(name, modifier)` overload removed outright, not kept alongside as a
  backwards-compatibility shim.
- `with(component: Any)` stays as the escape hatch for attaching a component with no named
  sugar function -- same role it plays today, just applied immediately instead of queued.
- `EntityScope`'s exact name not decided -- `EntityScope` is a placeholder, matches this repo's
  existing `*Scope` convention (`MeshGenerateScope`, `LightingScope` from the DSL sugar plan).
- Not addressed here: whether `scene { }` itself should gain a similar block-merge (already
  covered by the Gap 3 flattening overload in
  [2026-08-20-dsl-convenience-sugar-plan.md](2026-08-20-dsl-convenience-sugar-plan.md)) -- orthogonal, composes with this change rather than depending on it.
