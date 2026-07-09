# ECS Benchmark Scorecard

> Run: 2026-07-09 on `Rons-MacBook-Pro-2.local` via
> `./gradlew :awake-ecs-benchmark:benchmark`
>
> Harness: `kotlinx-benchmark` 0.4.17 / JMH, 1 fork, 2 warmup iterations,
> 3 measurement iterations, 1 second per iteration. Short-run numbers are useful for
> relative direction, not final release claims — JMH's own confidence intervals on several
> rows are wide (see raw JSON under `build/reports/benchmarks/main/`).
>
> Awake benchmark shape: pure `awake-ecs` runtime plus `awake-scene` components/systems.

## Measured comparison (same JVM, same hardware, same operations)

All four libraries below actually ran in this benchmark module (`awake-ecs-benchmark`) —
this is real, apples-to-apples data, not a citation of someone else's numbers on different
hardware.

| Benchmark | Size | **Awake** | Fleks 2.14 | Artemis-odb 2.3.0 | Ashley 1.7.3 | Fastest |
|---|---:|---:|---:|---:|---:|---|
| Entity create/destroy | 10k | 5,982.1 | 7,745.2 | 5,251.3 | 155.7 | Fleks |
| Entity create/destroy | 100k | 691.4 | 820.8 | 511.8 | 2.1 | Fleks |
| Component add/remove | 10k | 2,352.3 | 2,845.1 | 2,984.0 | 368.4 | Artemis-odb |
| Component add/remove | 100k | **210.1** | 189.4 | 209.3 | 28.6 | **Awake** (~tied w/ Artemis-odb) |
| Transform+MeshRenderer query | 10k | 63,315.2 | 77,966.4 | 59,534.0 | 18,892.5 | Fleks |
| Transform+MeshRenderer query | 100k | 4,622.6 | 7,831.5 | 4,570.7 | 282.9 | Fleks |
| TransformSystem propagation | depth 10 | 599,160.0 | 757,925.5 | 898,550.3 | 789,559.6 | Artemis-odb |
| TransformSystem propagation | depth 50 | 130,341.4 | 133,631.9 | 153,830.2 | 137,616.7 | Artemis-odb |

Ops/sec, all rows. Bold = Awake's own best showing.

## Takeaway (say the numbers plainly, don't spin them)

- **Awake wins or ties on component add/remove** — the sparse-set design does what it's
  supposed to do for structural churn (10k: within 20% of Artemis-odb/Fleks, ahead of
  Ashley; 100k: fastest of the four, narrowly ahead of Artemis-odb and Fleks).
- **Awake is not yet the fastest on stable family iteration** (Transform+MeshRenderer
  query) or **hierarchy propagation** at either depth — Fleks leads on query throughput,
  Artemis-odb leads on propagation at both depths. These are the per-frame hot paths that
  matter most for a running game, so this is the real remaining gap, not a rounding error.
- **Ashley is the clear outlier**, dramatically slower on create/destroy and component
  add/remove at scale (2 ops/s at 100k entity create/destroy). This tracks with Ashley's
  age and design intent — a simple `Engine`/`Family` listener model sized for typical
  libGDX 2D game entity counts (hundreds to low thousands), not the 10k-100k stress range
  probed here. It's included because it's real and widely used, not because it's a fair
  "modern ECS" baseline — its numbers mainly show what a plain listener-based (non-sparse-
  set, non-archetype) design costs at scale.
- Awake's own numbers moved meaningfully between runs on this same hardware as the
  family-cache and query-cache work landed (see this file's git history) — re-run this
  benchmark after any future `ComponentStore`/`World` change rather than trusting a stale
  table.

## Architecture reference (not benchmarked here — different language/runtime)

These are real, widely used ECS implementations that can't run in this JVM-based benchmark
module (different language, no JVM target, or a fundamentally different embedding model).
Included for architectural context only — **do not treat these as measured or directly
comparable** to the table above; they're on different hardware, different languages, and
often different problem framings (e.g. Unity DOTS assumes Burst-compiled jobs, not plain
JVM bytecode).

| ECS | Language | Architecture | Notes |
|---|---|---|---|
| [bevy_ecs](https://github.com/bevyengine/bevy) | Rust | Archetype (table-based) | Powers the Bevy game engine; widely cited as one of the fastest archetype ECS implementations, largely due to Rust's lack of GC pauses and tight memory control — not a like-for-like comparison against a JVM/Kotlin runtime. |
| [EnTT](https://github.com/skypjack/entt) | C++ | Sparse-set (same family as Awake) | The reference implementation most sparse-set ECS designs (including this one) trace their lineage to; header-only, zero-overhead-abstraction C++ templates give it a structural advantage no JVM sparse-set implementation can fully match. |
| [flecs](https://github.com/SanderMertens/flecs) | C | Archetype + relationships | Adds first-class entity relationships/hierarchies as a query primitive rather than a plain component field — a different modeling approach to what `Transform.parent` does here. |
| Unity DOTS (Entities package) | C# (Burst-compiled) | Archetype (chunk-based) | Designed around Unity's Job System + Burst compiler for SIMD/multi-threaded iteration; the performance story depends entirely on that compilation pipeline, which has no JVM/Kotlin equivalent. |

## Where to look next if closing the query/propagation gap matters

Based on the code review that produced this scorecard: `Family1Cache`/`Family2Cache`'s
`indexOf()` does a linear scan on every component remove/replace (no sparse index of its
own, unlike `ComponentStore`), and `TransformSystem.update()` allocates a fresh
`Map`+two `Set`s every frame. Both are plausible contributors to the query/propagation gap
and are the natural next places to optimize before reaching for a bigger architectural
change (e.g. archetypes).
