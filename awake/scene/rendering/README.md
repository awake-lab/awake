# Awake Scene Rendering

Status: **stable**.

`awake:scene:rendering` bridges `awake:scene:scene-core` (ECS `Transform`, entities) and
`awake:engine:render:contract` (backend-neutral `Renderer`/`DrawCall`/`Mesh`/`Material`) --
it owns the components a scene author attaches to make an entity visible, and the systems
that turn those components into a frame's `DrawCall` list every frame.

## Installation

```kotlin
implementation(project(":awake:scene:rendering"))
```

`api`-depends on both `awake:scene:scene-core` and `awake:engine:render:contract`, so a
consumer gets `Transform`/`Mesh`/`Material`/`DrawCall` visible transitively.

## Components (`components/`)

- `MeshRenderer` -- one mesh/material pair drawn at an entity's `Transform`. The common case.
- `InstancedMeshRenderer` / `InstancedSkinnedMeshRenderer` -- many copies of one mesh/material
  drawn in a single GPU-instanced call; carries its own list of transforms (and joint palettes
  for the skinned variant) instead of relying on per-entity `Transform`s.
- `ParticleEmitter` -- a fixed-capacity pool of camera-facing billboard particles, spawned/aged
  by `ParticleSystem` and drawn instanced by `RenderSystem`. See its own doc comment for the
  full knob list (burst spawning, cone spread, gradient color, ground-stop, sprite-strip
  frames) and `spawnParticleBurst` for a one-shot "play this effect here" helper.
- `Camera` -- wraps `awake:core`'s `CoreCamera` math; `isPrimary` marks the one `RenderSystem`
  actually renders through.
- `Light` -- a single scene-wide directional light (`RenderSystem.sceneLight` falls back to
  `DEFAULT_SCENE_LIGHT` when no `Light` entity exists).
- `LodGroup` -- picks one mesh/material level by distance to the camera each frame.
- `MeshBounds` -- opt-in local AABB; entities without it are never frustum/occlusion-culled.
- `Occluder` -- opt-in occluder box; see `awake:core`'s `Occlusion.kt` for the containment test.
- `PbrMaterial` / `SkinnedPose` -- per-entity material factors / joint palette, read by
  `RenderSystem` into a `DrawCall`'s `extraUniformFloats`.
- `WorldDebugSettings` -- singleton toggles (`showFrustum`/`showBounds`/`showOcclusion`) read
  by `DebugVisualizationSystem`.

## Systems (`systems/`)

- `RenderSystem` -- the one system that assembles a frame's `DrawCall` list: frustum/occlusion
  culls `MeshRenderer`/`LodGroup` entities, builds instanced draw calls for
  `InstancedMeshRenderer`/`InstancedSkinnedMeshRenderer`/`ParticleEmitter`, resolves the scene
  light, and calls `Renderer.draw`.
- `ParticleSystem` -- spawns/advances every `ParticleEmitter`'s particle pool (position/age/
  fade, cone-spread velocity, ground-stop, burst cleanup). Kept separate from `RenderSystem`
  (draw-call assembly) so simulation stays a single-responsibility step.
- `DebugVisualizationSystem` -- turns `WorldDebugSettings`'s toggles into `Renderer`
  debug-line draws (frustum/bounds/occlusion wireframes).
- `RenderSystemSupport.kt` -- shared helpers (`primaryCamera`, `CONSERVATIVE_ASPECT`) used by
  both systems above.

## Scope

This module has no rendering-backend code of its own -- it only builds `DrawCall`s and hands
them to whatever `Renderer` the app is running (`awake:backend:vulkan`/`awake:backend:webgpu`).
It also has no scene-authoring/DSL surface -- that's `awake:scene:authoring`; this module is
consumed by it, not the other way around.
