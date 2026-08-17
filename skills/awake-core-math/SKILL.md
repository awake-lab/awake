---
name: awake-core-math
description: Rules for using Awake's `core.math` types (Vec3, Mat4, Camera) and for writing per-frame ECS systems without allocating. Read this before touching vector/matrix math or writing anything inside a `System.update`. Trigger keywords - Vec3, Mat4, normalize, cross, dot, lerp, camera math, view direction, forward vector, per-frame allocation, System.update, movement basis.
---

# Awake core math and per-frame code

## Why this exists

A shipped bug: the camera kept losing its subject. Root cause was one line.

```kotlin
val dir = core.center - core.eye
dir.y = 0f
dir.normalize()      // <-- result discarded; `dir` unchanged
forward.set(dir)     // <-- so `forward` kept the camera's DISTANCE as its length
```

`normalize()` returned a new vector and did not touch the receiver. `forward` therefore had
length ~5 (third-person) or ~15 (top-down) instead of 1, and it was multiplied into the walk
speed. The player moved 5-15x too fast, outran the smoothed follow camera, and left frame.

It compiled, it ran, and nothing warned. The author reasonably assumed `normalize()` mutated,
because in that same class `set()`, `add()` and `lerp()` all do. **This was an API-naming
failure, not a careless author** - which is why the rule below is now mechanical.

## Rule 1 - the mutation contract

`Vec3` is a *mutable* type with two families of operations. The name tells you which:

| Form | Mutates receiver? | Allocates? | Examples |
|---|---|---|---|
| Bare imperative verb | **Yes**, returns `this` for chaining | No | `set`, `add`, `sub`, `scale`, `lerp`, `normalize` |
| `-ed` suffix | No | Yes | `normalized()` |
| Operator | No | Yes | `+`, `-`, `*` |
| Product / query | No | Depends | `dot` (Float), `length3` (Float), `cross` (new Vec3) |

```kotlin
// Mutating: no garbage, safe in a hot loop. Returns `this`, so it chains.
eye.set(target.position).add(offset)
forward.set(dirX, 0f, dirZ).normalize()

// Pure: fine in setup/expression code, allocates a new Vec3 each call.
val unit = (center - eye).normalized()
```

**Dropping the result of a pure call is always a bug.** `v.normalized()` on its own line does
nothing. If you meant to change `v`, you wanted `v.normalize()`.

Adding a new operation? Follow the same contract, or you re-create the trap.

## Rule 2 - no allocation inside `System.update`

`update()` runs every frame forever. Hoist scratch vectors into fields and reuse them.

```kotlin
class MatrixRelativeMovementSystem : System {
    // Allocated once.
    private val forward = Vec3()
    private val right = Vec3()

    override fun update(world: World, delta: Float) {
        forward.set(0f, 0f, -1f)   // reset, don't reallocate
        right.set(1f, 0f, 0f)
        ...
    }
}
```

Watch for the non-obvious allocators: `a - b`, `a + b`, `a * s`, `a.cross(b)`, `a.normalized()`
and every `listOf(...)` / `map { }` all produce garbage. Where a closed form exists, prefer it -
`forward x (0,1,0)` is just `(-forward.z, 0f, forward.x)`, no cross product and no allocation.

Static geometry belongs in a `val`, not in `onUpdate`:

```kotlin
// Before: ~100 objects per frame, per demo.
internal fun Renderer.drawReferenceGrid() {
    val lines = Grid.lines(10f, 10).map { (a, b) -> LineSegment(a, b, GRID_COLOR) }
    drawDebugLines(lines + axisLines)
}
// After: built once.
private val REFERENCE_LINES: List<LineSegment> = /* ... */
internal fun Renderer.drawReferenceGrid() = drawDebugLines(REFERENCE_LINES)
```

## Rule 3 - `Mat4` is pure; `Transform` is what you actually want

`Mat4.translate/rotate/scale` **return new matrices**. They do not mutate.

```kotlin
mat.identity()
mat.scale(s, s, s)   // no-op: result discarded, plus 2 wasted Mat4 allocations
```

Beyond that, do not write `Transform.worldMatrix` by hand at all - `TransformSystem` recomposes
it from `position`/`rotation`/`scale` every frame and will overwrite you.

```kotlin
// Do this instead.
world.get<Transform>(entity)?.scale?.set(s, s, s)
```

## Rule 4 - never share a mutable constant

`Vec3` has `var x/y/z`, so a shared instance can be aliased into a `set()`/`add()` chain and
corrupted for every other caller. `Vec3.up()` is a **function** returning a fresh vector for
exactly this reason. Do not reintroduce `val UP = Vec3(0f, 1f, 0f)`.

## Rule 5 - one basis for all camera modes

Derive every camera mode's aim from one shared forward vector. Two modes that each build their
own spherical math will disagree on sign, and switching modes will mirror the controls - which
is the second bug this codebase shipped.

```kotlin
private fun forwardFrom(yaw: Float, pitch: Float, out: Vec3) {
    val cp = cos(pitch)
    out.set(sin(yaw) * cp, sin(pitch), -cos(yaw) * cp)
}
// first-person:  center = eye + forward
// third-person:  eye = (target + offset) - forward * distance;  center = target + offset
```

Convention: yaw 0 faces **-Z**, +yaw turns **right**, +pitch looks **up**.

Clamp pitch short of +/-90 degrees (85 is used). At exactly a right angle the aim is parallel
to the camera's up vector and `setLookAt`'s cross product collapses to a degenerate view matrix.

Also: a mode that ignores an input axis must not accumulate it. Otherwise the hidden value
drifts while the camera looks frozen, and it snaps the moment you switch modes. `CameraMode`
declares `usesYaw` / `usesPitch` / `usesZoom` so `CameraSystem` can skip what a mode ignores.

## Rule 6 - clip space belongs to the renderer, never to a camera

The three backends disagree on **two independent axes**:

| | Y axis | depth range |
|---|---|---|
| OpenGL | up | `-1 .. 1` |
| Vulkan | **down** | **`0 .. 1`** |
| WebGPU | up | **`0 .. 1`** |

Both failure modes are quiet. Get Y wrong and you render a plausible, vertically mirrored
scene. Get depth wrong and everything nearer than roughly twice your near plane is silently
clipped. Neither crashes, and both look like a camera bug.

This was a real, shipped defect, and its three causes are the rules:

1. **The convention lived on the wrong object.** `Camera` -- a scene concept -- stored a
   backend concept, so every demo that built a camera chose a backend by accident. A camera
   describes a lens: eye, target, up, field of view, near, far. All convention-free. The
   `Renderer` owns [ClipSpace] because the renderer owns the API, and it supplies it when it
   builds the matrix. A demo, scene or test then *cannot* bake in the wrong one.
2. **It had a default.** `flipYForClipSpace: Boolean = true` turned "didn't think about it"
   into "silently Vulkan". `clipSpace` takes no default anywhere; forgetting it is a compile
   error, which is the entire point.
3. **It was a boolean.** Three conventions, two axes -- one flag can only name one of them,
   and it named the axis that was already correct, hiding the depth bug completely.

Generalise it: **a value that must agree with the environment should be derived from the
environment, not passed as a parameter with a default.** If you find yourself adding a
`Boolean` that selects between platforms, you probably want an enum owned by whichever object
already knows the answer.

Verify with a contract test per backend -- project a point on the near plane and one on the
far plane, and assert where each lands in NDC according to that API's published spec. Assert
the contract, not the current implementation.

## Rule 7 - angle units are stated by naming, not by a wrapper type

`core.math.Angle` (a `value class` wrapper with `fromDegrees`/`fromRadians`/`angleDeg`/`angleRad`)
exists in the codebase but is not used by any production call site -- `Camera.fovYRadians` and
every other angle-carrying field states its unit in the identifier instead (`xRadians`/`xDeg`
suffix), which every hot-path caller already follows for free, with no wrapper-type migration
across `Vec3`/`Mat4`/`Camera`/`Transform`. Do not propose adopting `Angle` to "fix" this -- it was
evaluated (2026-08-17) and rejected: the naming convention already solves the ambiguity this
wrapper targets, and migrating existing raw-`Float` angle fields to it is a wide, low-value diff
across every hot per-frame path this skill exists to protect. If a genuine degrees/radians mixup
bug surfaces that naming alone didn't catch, that's new evidence -- re-open the question then,
don't reason from first principles again.

## Checklist before committing math or per-frame code

- [ ] Every `normalized()` / `cross()` / operator result is assigned or passed - none dropped.
- [ ] Anything inside `System.update` reuses scratch fields instead of allocating.
- [ ] No `Mat4.translate/rotate/scale` result discarded; no hand-written `worldMatrix`.
- [ ] No shared mutable `Vec3` constants.
- [ ] New `Vec3`/`Mat4` members follow the naming contract in Rule 1.
- [ ] Camera changes keep all modes on one `forwardFrom` basis.
- [ ] No new platform-selecting `Boolean`, and no default on a value that must match the
      active backend -- see Rule 6.

Regression tests live in `awake/base/src/commonTest/.../Vec3MutabilityTest.kt`,
`awake/scene/controls/src/commonTest/.../CameraRelativeMovementTest.kt` and
`.../CameraModeConsistencyTest.kt`. Extend them rather than replacing them.
