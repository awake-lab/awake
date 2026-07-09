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
- Wired the Vulkan demo bootstrap through `SceneRuntimeHost` so the app now loads the
  scene contract at runtime and attaches real `MeshRenderer` components in one place.

## Next Step

Start the core split from the cleaned-up bootstrap boundary.
