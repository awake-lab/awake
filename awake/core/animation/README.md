# Awake Core Animation

Portable skeletal animation runtime for [Awake](../../README.md) — skeleton hierarchies, skin bindings, animation clips, and crossfade pose blending. No rendering or platform dependencies, compiling cleanly across all targets (Desktop, Android, iOS, WasmJs).

## Installation

```kotlin
implementation(project(":awake:core:animation"))
```

## Core Primitives

- `Skeleton` — joint hierarchy, local/model bind poses, and inverse bind matrices.
- `Skin` — vertex joint weights and indices mapping geometry to skeleton joints.
- `AnimationClip` — time-sampled translation, rotation (quaternion), and scale keyframe tracks.
- `AnimationPose` — sampled joint transforms at a specific playback time.
- `AnimationCrossfade` — linear and spherical (SLERP) interpolation blending two poses over a transition duration.

## Usage

```kotlin
import io.github.ronjunevaldoz.awake.core.animation.Skeleton
import io.github.ronjunevaldoz.awake.core.animation.AnimationClip
import io.github.ronjunevaldoz.awake.core.animation.AnimationCrossfade

// Sample pose at current playback time
val walkPose = walkClip.sample(time = 0.5f, skeleton = skeleton)
val runPose = runClip.sample(time = 0.2f, skeleton = skeleton)

// Crossfade between animations
val blendedPose = AnimationCrossfade.blend(
    fromPose = walkPose,
    toPose = runPose,
    weight = 0.3f // 30% run, 70% walk
)
```

## Scope

Dependency-free skeletal pose evaluation and blending. GPU joint matrix palette generation and skinning shaders are handled downstream by rendering backends (`awake:engine:render:contract` / `awake:backend:*`).
