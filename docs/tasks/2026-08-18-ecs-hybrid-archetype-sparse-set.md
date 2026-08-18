# 2026-08-18: ECS hybrid archetype + sparse-set

Status: decision recorded, not yet built. `awake:ecs` today is pure sparse-set (see
`skills/awake/agents/awake-ecs-performance-engineer.md`). This proposes moving stable
core components to archetype storage while keeping dynamic/transient components on
sparse sets — a hybrid, not a full archetype rewrite.

## Why pure sparse-set isn't the final answer

Sparse-set gives O(1) add/remove with no migration cost, which is why it was chosen for
Phase 3's modest scope (four component types, two systems). It doesn't give the
contiguous-memory cache locality archetype storage gives for large uniform iteration —
the RenderSystem/TransformSystem hot path over thousands of `Transform`+`MeshRenderer`
entities is exactly the case archetype storage is built for.

## Why pure archetype isn't the answer either

**Archetype explosion.** A character with 10 optional status effects (`IsBurning`,
`IsStunned`, `PoisonDamage`, `SpeedBuff`, `Invulnerable`, ...) can produce up to 2^10
(1,024) distinct archetype tables under pure archetype storage — one per unique
component combination. Instead of few large contiguous tables, data fragments into
hundreds of near-empty ones, destroying the cache-locality benefit archetypes exist for.
Toggling one status effect on 100 entities mid-fight forces ripping all 100 out of their
current table, allocating/copying into a new one — including every unrelated field
(inventory, stats, model refs) — for a single tag flip. That's the "archetype nightmare"
case: gameplay-visible stutter from a routine combat action.

## Reference: Flecs

[github.com/SanderMertens/flecs](https://github.com/SanderMertens/flecs) (C/C++/Rust) is
primarily archetype-based but adds **tags** and **pairs** that avoid heavy structural
table migration for exactly this case — sparse-set flexibility layered onto an archetype
core, not a separate system. This proposal follows the same shape. See its own FAQ for
the archetype-explosion problem stated from the library author's side:
[flecs/docs/FAQ.md](https://github.com/SanderMertens/flecs/blob/master/docs/FAQ.md).

## The hybrid split

| Storage | Owns | Example | Why |
|---|---|---|---|
| Archetype tables | Stable, dense core components every relevant entity has | `Transform`, `MeshRenderer` | Contiguous memory; physics/render loops stream sequentially at max cache speed, zero migration once an entity's archetype is set |
| Sparse sets | Dynamic, transient, frequently-toggled components | `IsBurning`, `IsStunned`, gameplay tags/buffs | O(1) add/remove; freezing 100 entities pushes 100 IDs into one sparse array, no table rebuild |

An entity's core data lives once, contiguously, in its archetype table. Dynamic tags
reference that same entity ID through independent sparse lookup maps — adding/removing a
tag never touches the archetype table at all.

## Two implementation patterns (pick one, don't need both)

1. **Tag system (sparse arrays for flags)** — marker/status components with no heavy
   payload (`IsDead`, `IsSelected`, `IsBurning`) live in global sparse sets entirely
   outside the archetype tables. Adding a tag is a pure sparse-set insert, zero
   archetype-table involvement.
2. **Component holder pattern** — a single `StatusEffects` component lives *inside* the
   archetype table (so the entity's archetype membership never changes when effects
   change) and internally holds a small dynamic array of active buffs. The archetype
   stays perfectly stable; gameplay logic stays fully dynamic inside one field.

Tag system is simpler and matches Flecs's own approach most directly. Component holder
keeps everything for one entity in one place at the cost of an extra indirection inside
the component. Pick per-component-shape, not a single blanket rule -- a boolean-shaped
tag (`IsStunned`) fits tag-system; a value-bearing, frequently-queried-by-magnitude effect
(`PoisonDamage: Float`) may fit component-holder better.

## What does NOT change

- `Entity`'s generation-counter value class, the 64-component-type hard limit, the
  reified-generics hot-path rule -- all still apply regardless of storage backing.
- `awake:ecs:benchmark`'s Fleks comparison stays the validation mechanism; add hybrid
  vs. pure-sparse-set vs. pure-archetype as three benchmarked shapes, not just
  ECS-vs-Fleks, once this is built.

## Before building

1. Decide the concrete component split for Phase 3+ scope: which components are
   "archetype-stable" (rarely added/removed after spawn) vs "sparse-dynamic" (frequently
   toggled). `Transform`/`MeshRenderer`/`Camera`/`Light` read as archetype-stable today;
   no sparse-dynamic components exist yet in this codebase (RPG-style status effects are
   not built) -- so there's no forcing example in-tree yet, only the general risk.
2. Benchmark the current sparse-set implementation against a hybrid prototype before
   committing -- per the ECS agent's existing rule, this is a claim to prove with numbers,
   not assert.
3. Update `skills/awake/agents/awake-ecs-performance-engineer.md`'s architecture section
   to describe the hybrid as built, and delete this proposal's "not yet built" framing, in
   the same commit that lands it -- same discipline as every other proposal doc this
   project keeps.
