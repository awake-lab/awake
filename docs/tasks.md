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
- Do we want the first UI DSL slice to target inspector panels only, or should it also
  cover HUD/menu composition in the same pass?

## Fix Lanes

- Dev: Core split
- Dev: UI DSL and style audit
- Beta: None yet
- Stable: Refresh runtime docs after the module split lands

## Task Log

- [2026-07-09-decouple-world](tasks/2026-07-09-decouple-world.md)
- [2026-07-10-scene-runtime](tasks/2026-07-10-scene-runtime.md)
- [2026-07-14-ui-dsl-audit](tasks/2026-07-14-ui-dsl-audit.md)

## Archive Index

- None yet
