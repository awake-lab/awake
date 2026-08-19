# 2026-08-17: awake:core module split proposal

Status: Active architecture guideline. This document defines the modular roadmap for splitting `awake:core` packages into focused leaf modules.

**Rule for Agents & Contributors**:
Whenever performing refactors, adding new foundation features, or extracting code from `awake:core` (`math`, `input`, `time`), **always follow the proposed dependency graph and module shapes defined in this document**.
When a module described here is built, create its dedicated `README.md` (matching `geometry` and `animation`) and update this document in the same commit.

---

## 1. Modules Already Extracted / Built

The following modules have been successfully extracted from `awake:core`:
- [`:awake:core:geometry`](../../awake/core/geometry/README.md) — portable mesh geometry algorithms (`MeshSimplifier`, Garland-Heckbert quadric error, `NormalizedInt`).
- [`:awake:core:animation`](../../awake/core/animation/README.md) — skeletal animation runtime (`Skeleton`, `Skin`, `AnimationClip`, `AnimationPose`, `AnimationCrossfade`).
- Subsystems partitioned outside `core/`:
  - **Physics**: Implemented as pure Kotlin contract in `:awake:physics:api` and native bridge in `:awake:backend:jolt`.
  - **Assets**: Partitioned under `:awake:asset:gltf`, `:awake:asset:mesh-optimizer`, and `:awake:asset:shaders`.
  - **Rendering**: Partitioned under `:awake:engine:render:contract` and `:awake:backend:*`.

---

## 2. Proposed Dependency Graph for Remaining Splits

```
                  ┌──────────────────────┐
                  │   awake:core:math    │◄────────────────────────┐
                  └──────────▲───────────┘                         │
                             │ Loaded By                           │
                  ┌──────────┴───────────┐                         │
                  │ awake:core:geometry  │◄──────────┐             │
                  └──────────▲───────────┘           │             │
                             │ References            │             │ Used By
        ┌────────────────────┴───────────────┐       │ Used By     │
        │                                    │       │             │
┌───────┴────────────┐              ┌────────┴───────┴┐   ┌────────┴──────┐
│  awake:core:time   │              │awake:core:input │   │awake:core:    │
│  (loop/ticker)     │              │(events/mapping) │   │audio (future) │
└───────▲────────────┘              └────────▲────────┘   └────────▲──────┘
        │                                    │                     │
        └────────────────────┬───────────────┴─────────────────────┘
                             │ Managed & Driven By
                  ┌──────────┴───────────┐
                  │      awake:engine    │
                  └──────────────────────┘
```

---

## 3. Remaining Proposed Modules

### 1. `awake:core:math` (Foundation — Priority 1)
- **Scope**: Pure mathematical primitives and linear algebra. Zero dependencies.
- **Types**: `Vector3`, `Matrix4`, `Quaternion`, `Ray`, `Transform`, `MathUtils`, fixed-point math, trig tables.
- **Current Home**: `io.github.ronjunevaldoz.awake.core.math` inside `awake:core`.
- **Target**: Extract to `:awake:core:math` subproject.

### 2. `awake:core:input` (Input Events — Priority 2)
- **Scope**: Remappable action structures, pointer state, and key events decoupled from rendering or physics loops.
- **Types**: `InputEvent`, `KeyCode`, `MouseEvent`, `TouchEvent`, `PointerState`.
- **Current Home**: `io.github.ronjunevaldoz.awake.core.input` inside `awake:core`.
- **Target**: Extract to `:awake:core:input` subproject.

### 3. `awake:core:time` (Engine Heartbeat — Priority 3)
- **Scope**: Delta time estimation, fixed-timestep loop, and simulation clock synchronization.
- **Types**: `GameLoop`, `Clock`, `DeltaTimeEstimator`, `FixedTimestepLoop`.
- **Current Home**: `io.github.ronjunevaldoz.awake.core.application` inside `awake:core`.
- **Target**: Extract to `:awake:core:time` subproject.

### 4. `awake:core:diagnostics` (Dev Sandbox)
- **Scope**: Memory allocation tracking, performance profiling, and assertion instrumentation.
- **Types**: `Profiler`, `MemoryTracker`, `AssertionEngine`.
- **Target**: Leaf module connected across layers.

### 5. `awake:core:audio` (Sound Engine — Post-MVP Roadmap)
- **Scope**: Spatial audio propagation, playback state, and decoding.
- **Dependencies**: `:awake:core:math` (3D spatial vectors).
- **Target**: Dedicated audio runtime module when audio milestone begins.
