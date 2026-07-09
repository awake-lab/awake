# Tasks

## Current Objective

Validate the `World` split and decide whether any further internal decoupling is worth the
added complexity.

## Active Phase

- 2026-07-09: `EntityArena`, `ComponentRegistry`, and `QueryCache` have landed; we are
  checking whether the remaining `World` surface still needs to shrink.

## Open Questions

- Should query cache invalidation stay in `World`, or move into a dedicated `QueryCache`
  helper?
- Do we keep `typeId()` on `World` as the public entry point, or delegate it through a
  `ComponentRegistry`?
- Do we split pooling from store/type management immediately, or after the entity lifecycle
  extraction lands?

## Fix Lanes

- Dev: World decoupling plan
- Beta: None yet
- Stable: Refresh `docs/ecs-benchmark-scorecard.md` after the next verified churn run

## Task Log

- [2026-07-09-decouple-world](tasks/2026-07-09-decouple-world.md)

## Archive Index

- None yet
