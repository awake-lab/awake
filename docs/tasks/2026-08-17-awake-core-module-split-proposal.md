# 2026-08-17: awake:core module split proposal

Status: research & analysis, not yet built. This is a snapshot of one day's thinking, not a
living doc -- when a module described here actually gets built (the way `animation` was
extracted from `awake:core` this same day, see `awake/core/README.md`), move its description
out of here and into the real module's own README in that commit. Don't let this file drift
into describing modules as "proposed" after they exist.

## Goal

Split `awake:core` (currently one module holding `math`/`input`/`utils`/`colors`/
`application`/`graphics` as packages) into focused leaf modules with a strict, unidirectional
dependency flow, eliminating circular dependencies as the engine grows.

## Proposed dependency graph

```
                  ┌──────────────────────┐
                  │   awake:core:math    │◄────────────────────────┐
                  └──────────▲───────────┘                         │
                             │ Loaded By                           │
                  ┌──────────┴───────────┐                         │
                  │ awake:core:geometry  │◄──────────┐             │
                  └──────────▲───────────┘           │             │
                             │ References            │             │ Used By
       ┌─────────────────────┼────────────────────┐  │ Used By     │
       │                     │                    │  │             │
┌──────┴─────────────┐┌──────┴─────────────┐┌─────┴───────┐┌──────┴──────┐
│ awake:core:physics ││awake:core:rendering││ awake:core: ││ awake:core: │
│                    ││                    ││   assets    ││    audio    │
└──────▲─────────────┘└──────▲─────────────┘└─────▲───────┘└──────▲──────┘
       │                     │                    │               │
       └─────────────────────┼────────────────────┴───────────────┘
                             │ Managed & Driven By
                  ┌──────────┴───────────┐
                  │       awake:ecs      │
                  └──────────────────────┘
```

Note: ECS is already a real, separate module today at `:awake:ecs` (not `:awake:core:ecs`
as an earlier draft of this proposal had it), and rendering already exists at
`:awake:engine:render:*` (not `:awake:core:rendering`) -- this diagram proposes new leaf
modules under `awake:core`, it does not propose moving those two.

## Proposed modules

- `awake:core:math` (foundation)
    - Pure mathematical primitives and linear algebra. No dependencies.
    - Vector3, Matrix4, Quaternion, Ray, fixed-point math, trig lookup tables.
    - Already exists as a **package** inside `awake:core` today
      (`io.github.ronjunevaldoz.awake.core.math`); this proposes extracting it to its own
      module, the same move already made for `geometry` and `animation`.
- `awake:core:assets` (resource management)
    - File I/O, deserialization, memory lifecycle of engine assets.
    - Depends on: `awake:core:geometry`, `awake:core:math`.
    - AssetManager, Loader, Serializer, ResourceCache.
- `awake:core:physics` (simulation)
    - Real-time rigid body dynamics, collision detection, cinematic constraints.
    - Depends on: `awake:core:geometry`, `awake:core:math`.
    - RigidBody, CollisionDetection, Constraints, Solvers, ForceGenerators.
- `awake:core:audio` (sound engine)
    - Spatial audio propagation, playback state, decoding.
    - Depends on: `awake:core:math` (3D spatial calculations).
    - AudioSource, AudioListener, AudioClip, AudioEngine.
- `awake:core:platform` (the OS bridge)
    - WindowContext, ApplicationLifecycle, FileSystemAbstraction, DisplaySettings, platform
      capability detection.
    - Sits directly above math, parallel to geometry.
- `awake:core:input` (the human interface)
    - Remappable action structures decoupled from rendering/physics loops.
    - InputManager, keyboard/mouse listeners, GamepadState, TouchGestures, action mapping.
    - Already exists as a **package** inside `awake:core` today; this proposes extraction.
    - Sits directly above platform, feeding event metrics to `awake:ecs`.
- `awake:core:time` (the engine heartbeat)
    - Delta time, tick steps, and cross-device simulation synchronization.
    - GameLoop, Clock, DeltaTimeEstimator, FixedUpdateTicker, ProfileTimer.
    - Base leaf module, parallel to math and platform.
- `awake:core:diagnostics` (the dev sandbox)
    - Memory allocation monitoring, secure cross-platform logging, performance profiling.
    - Logger, Profiler, AssertionEngine, MemoryTracker, CrashReporter.
    - Global leaf dependency, connected across all layers.

## Rendering + physics backend shape (already real, referenced for context)

**Corrected 2026-08-18** -- the previous version of this section named a
`RenderDriverFactory` class. Verified against source: no such class exists anywhere in the
tree. Selection is plain KMP `expect`/`actual`, not a runtime factory object.

Rendering and physics live outside this proposal already, at `awake:engine:render:*` and
`awake:backend:*`, but the shape is worth restating since any new leaf module above should
assume the same pattern:

```
awake:engine:render:contract        -- interface: Camera, Material, Renderer (commonMain)
  awake:backend:vulkan              -- expect class Renderer : contract.Renderer
    awake:backend:vulkan:bindings           -- generated Vulkan API bindings
    awake:backend:vulkan:bindings:android-native
    awake:backend:vulkan:generator          -- the codegen tool that produces bindings/
  awake:backend:webgpu              -- expect class Renderer : contract.Renderer (wasmJs)
  awake:backend:jolt                -- Jolt Physics via JoltC (SecondHalfGames/JoltC),
                                        prebuilt binary on iOS; not a render backend, physics
```

Selection happens at compile time per KMP source set (`expect class Renderer(...)` in
`awake-vulkan`, an `actual` per platform target) -- there is no runtime factory to route
through.

## References

- https://github.com/github/awesome-copilot/blob/main/skills/game-engine/references/game-engine-core-principles.md
