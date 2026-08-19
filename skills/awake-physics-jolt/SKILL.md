---
name: awake-physics-jolt
description: >
  Rules and invariants for Awake's physics system (`awake:physics:api` and `awake:backend:jolt` C++ bridge).
  Read before touching physics simulation steps, rigid bodies, colliders, contact listeners, raycasting, or ECS physics wiring.
  Trigger keywords - Jolt, physics, RigidBody, Collider, BoxShape, SphereShape, ContactListener, RayCast, PhysicsSystem.
---

# Jolt Physics Backend Engineering in Awake

Awake's physics subsystem is partitioned into two clean layers:
1. `:awake:physics:api` — the pure Kotlin, dependency-free physics contract (rigid bodies, shapes, queries, collision events).
2. `:awake:backend:jolt` — the high-performance C++ JNI bridge to Jolt Physics.

Read [docs/architecture.md](../../docs/architecture.md), [docs/reference/game-structure.md](../../docs/reference/game-structure.md), and [skills/awake-ecs-authoring/SKILL.md](../awake-ecs-authoring/SKILL.md) first.

## 1. Clean Architecture Boundary

- `:awake:physics:api` has **zero native dependencies** and **zero rendering dependencies**. It defines pure value classes and interfaces (`RigidBodyHandle`, `PhysicsShape`, `RayCastHit`, `CollisionEvent`).
- `:awake:backend:jolt` implements the physics simulation and wraps Jolt's native memory allocations.
- Never leak raw Jolt pointer addresses (`Long` or `NativePointer`) into gameplay code or scene authoring DSL.

## 2. Fixed-Timestep Simulation Loop

- Physics simulation runs on a deterministic fixed timestep (typically 60 Hz / 16.66ms) inside `PhysicsSystem.update()`, decoupled from variable rendering framerates.
- Gameplay systems consume accumulated sub-steps via `FixedTimestepLoop` to prevent physics tunneling and instability at low frame rates.

## 3. Native Memory & Handle Cleanup

- Every created rigid body, shape, and contact listener must have an explicit destruction call on the native side.
- Scene teardown or entity destruction must notify the physics world to remove and destroy bodies before recycling entity IDs.

## 4. ECS Physics Synchronization

- `PhysicsSystem` reads entity `Transform` components to initialize body positions, steps the physics world, and updates `Transform.position` and `Transform.rotation` from the simulation output.
- **Rule**: Avoid per-frame allocation when reading body velocities, transforms, or contact points. Reuse scratch `Vector3` and `Quaternion` instances.
