# Tasks

## Current Objective

Stabilize Awake's public API boundaries before the next core/scene split. The current focus
is separating core APIs, reusable helpers, and authoring sugar so future module moves do not
just spread unclear ownership across more folders.

## Active Phase

- 2026-08-05: API layering plan added. Use
  [docs/reference/api-layering.md](reference/api-layering.md) as the stable rule and
  [docs/tasks/2026-08-05-api-layering-plan.md](tasks/2026-08-05-api-layering-plan.md) as
  the active ECS/scene cleanup plan.
- 2026-08-05: Phase 2 ECS/scene API classification audit recorded in the API layering
  plan. First cleanup target is demo/navigation ownership in `awake:scene`, then
  `SceneRuntime` deprecation review.
- 2026-07-10: `VulkanApplication` now loads `scene.json` through `SceneRuntimeHost`; next
  we peel shared code into smaller modules, starting with math/runtime/utils.

## Open Questions

- Should the core split start with `math`/`runtime`/`utils`, or should render boundaries be
  carved out first?
- Which ECS/scene APIs are true core, which are reusable helpers, and which are only
  authoring sugar?
- Should scene split first by API layer (`core`/`authoring`) or domain capability
  (`core`/`rendering`/`physics`/`controls`) once classification is complete?
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
- Dev: ECS/scene API layering and classification
- Dev: UI DSL and style audit
- Beta: None yet
- Stable: Refresh runtime docs after the module split lands

## Task Log

- [2026-07-09-decouple-world](tasks/2026-07-09-decouple-world.md)
- [2026-07-10-scene-runtime](tasks/2026-07-10-scene-runtime.md)
- [2026-07-14-ui-dsl-audit](tasks/2026-07-14-ui-dsl-audit.md)
- [2026-07-14-ui-module-split](tasks/2026-07-14-ui-module-split.md)
- [2026-07-17-ui-api-simplification](tasks/2026-07-17-ui-api-simplification.md)
- [2026-08-05-api-layering-plan](tasks/2026-08-05-api-layering-plan.md)

## Archive Index

- None yet
