# Awake Backend Jolt

Jolt Physics backend for [Awake](../../README.md) implementing `:awake:physics:api` via native C++ bindings (JoltC). Provides high-performance 3D rigid body dynamics, collision detection, and raycasting across Desktop (JVM), Android (NDK), iOS (prebuilt binary), and WasmJs.

## Installation

```kotlin
implementation(project(":awake:backend:jolt"))
```

## Key Primitives

- `JoltPhysicsWorld` — implements `PhysicsWorld` wrapping Jolt's native physics system and body interface.
- `JoltNative` — JNI / C-interop bridge calling native JoltC entrypoints.
- `QuatEuler` — quaternion-to-Euler conversion utilities for native rotation synchronization.

## Architecture

- **Desktop (JVM)**: Dynamic native library loaded at runtime via JNI.
- **Android**: NDK shared library loaded through `System.loadLibrary`.
- **iOS**: Linked against prebuilt static binary via Kotlin/Native cinterop.
- **WasmJs**: WebAssembly module compiled via Emscripten.

## Related Modules

- [`:awake:physics:api`](../../physics/api/README.md) — pure Kotlin physics interface implemented by this backend.
- [`:awake:scene:physics`](../../scene/physics/README.md) — ECS physics synchronization systems.
