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
- Should Awake v1 use one universal `Style`, or separate style types immediately for
  text/button/panel families?
- Which properties stay in `UiModifier`, and which must move into the new `Style` layer?
- Should the first UI DSL slice target inspector panels only, or should it also cover
  HUD/menu composition in the same pass?
- Do we want `animate { }` support in the first style pass, or only after the static style
  property model settles?

## Fix Lanes

- Dev: Core split
- Dev: UI DSL and style audit
- Beta: None yet
- Stable: Refresh runtime docs after the module split lands

## Task Log

- [2026-07-09-decouple-world](tasks/2026-07-09-decouple-world.md)
- [2026-07-10-scene-runtime](tasks/2026-07-10-scene-runtime.md)
- [2026-07-14-ui-dsl-audit](tasks/2026-07-14-ui-dsl-audit.md)
- [2026-07-14-ui-module-split](tasks/2026-07-14-ui-module-split.md)

## Archive Index

- None yet
