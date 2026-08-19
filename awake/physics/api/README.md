# Awake Physics API

Pure Kotlin multiplatform physics contract for [Awake](../../README.md) — rigid body lifecycle, collision shapes, motion types, transforms, and raycasting. Zero native or backend dependencies, compiling for all targets.

## Installation

```kotlin
implementation(project(":awake:physics:api"))
```

## Key Primitives

- `PhysicsWorld` — contract for rigid body simulation, step updates, body creation, and raycasting.
- `BodyHandle` — type-safe lightweight handle identifying a simulated rigid body.
- `PhysicsShape` — sealed collision geometry (`Box`, `Sphere`, `Capsule`, `Mesh`).
- `MotionType` — rigid body mobility (`Static`, `Kinematic`, `Dynamic`).
- `BodyTransform` — position (`Vec3`) and orientation (`Quat`) representation.
- `RaycastHit` — raycast intersection result (hit position, normal, distance, body handle).

## Usage Example

```kotlin
import io.github.ronjunevaldoz.awake.physics.PhysicsWorld
import io.github.ronjunevaldoz.awake.physics.PhysicsShape
import io.github.ronjunevaldoz.awake.physics.MotionType

// Create dynamic body
val body = physicsWorld.createBody(
    shape = PhysicsShape.Box(halfExtents = Vec3(0.5f, 0.5f, 0.5f)),
    motionType = MotionType.Dynamic,
    position = Vec3(0f, 5f, 0f)
)

// Step simulation
physicsWorld.step(deltaTime = 1f / 60f)
```

## Related Modules

- [`:awake:backend:jolt`](../../backend/jolt/README.md) — Jolt Physics C++ native backend implementing this contract.
- [`:awake:scene:physics`](../../scene/physics/README.md) — ECS component and system bindings (`RigidBodyComponent`, `PhysicsSystem`).
