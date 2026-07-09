# Tasks

## Current Objective

Start the core split after the scene bootstrap proof, beginning with the pure shared
pieces.

## Active Phase

- 2026-07-10: `VulkanApplication` now loads `scene.json` through `SceneRuntimeHost`; next
  we peel shared code into smaller modules, starting with math/runtime/utils.

## Open Questions

- Should the core split start with `math`/`runtime`/`utils`, or should render boundaries be
  carved out first?
- Which code should remain in `awake-core` once the pure shared pieces move?
- Do we split `physics` now, or leave it until the scene/runtime shape is settled?

## Fix Lanes

- Dev: Core split
- Beta: None yet
- Stable: Refresh runtime docs after the module split lands

## Task Log

- [2026-07-09-decouple-world](tasks/2026-07-09-decouple-world.md)
- [2026-07-10-scene-runtime](tasks/2026-07-10-scene-runtime.md)

## Archive Index

- None yet
