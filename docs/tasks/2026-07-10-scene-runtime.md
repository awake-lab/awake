# Scene Runtime

## Current Objective

Turn the serialized scene contract into the runtime bootstrap path for the MVP scene.

## Result

- Added `SceneDocument`, `SceneNode`, `SceneTransform`, `SceneCamera`, `SceneLight`, and
  `SceneMeshRenderer` as the serializable scene contract in `awake-scene`.
- Added `SceneLoader` plus `SceneInstance` so a bundled `scene.json` can become a live ECS
  `World`.
- Added `Name` as a tiny runtime label component for hierarchy/editor views.
- Added a bundled `scenes/mvp.scene.json` fixture and desktop tests for JSON round-tripping
  and world hydration.

## Next Step

Wire `SceneInstance.renderableRequests` into the app-side asset resolver so the MVP scene
can attach real `MeshRenderer` components instead of only describing them.
