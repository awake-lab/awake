# Awake Core

Dependency-free foundation for [Awake](../../README.md): math, input, fixed-timestep loop,
resource/bitmap I/O, and color handling. No dependency on `awake:ecs`, rendering, or any
platform-specific backend — every platform target compiles it directly.

## Installation

```kotlin
implementation(project(":awake:core"))
```

## Packages

- `io.github.ronjunevaldoz.awake.core.math` — Vector3, Matrix4, Quaternion, Ray, fixed-point
  math, trig lookup tables.
- `io.github.ronjunevaldoz.awake.core.input` — input event types and mapping, platform-neutral.
- `io.github.ronjunevaldoz.awake.core.application` — the fixed-timestep game loop and
  application lifecycle hooks.
- `io.github.ronjunevaldoz.awake.core.graphics` — bitmap and low-level resource I/O.
- `io.github.ronjunevaldoz.awake.core.colors` — color types and conversions.
- `io.github.ronjunevaldoz.awake.core.utils` — shared dependency-free helpers.

## Related modules

Split out of `awake:core` as they grew independent test surfaces and target-specific needs:

- [`awake:core:geometry`](geometry/README.md) — portable mesh geometry math (simplification,
  quantized vertex decoding), no file I/O.
- [`awake:core:animation`](animation/README.md) — skeletal animation runtime (`Skeleton`/`Skin`/`AnimationClip`/
  `AnimationPose`, crossfade blending).

## Proposed future modules

Not yet built. See
[`docs/tasks/2026-08-17-awake-core-module-split-proposal.md`](../../docs/tasks/2026-08-17-awake-core-module-split-proposal.md)
for the full proposal (dependency graph, per-module scope) before starting work on any of
these — `physics`, `assets`, `audio`, `platform`, `time`, `diagnostics`, and extracting
`math`/`input` out of this module into their own. When one of these actually gets built,
give it its own README (like `geometry`'s) and delete its section from that proposal doc in
the same commit — don't let a proposal document describe a module that already exists.
