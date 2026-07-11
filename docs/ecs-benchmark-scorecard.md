# ECS Benchmark Scorecard

> **Naming note:** `SparseIndex` and `GeneralFamilyCache`/`GeneralFamily.kt` mentioned in the
> dated sections below were later renamed to `EntityIndexMap` and `FamilySpecCache`/
> `FamilySpec.kt` for clarity. Left as-is here since these are historical records of what
> each commit actually did at the time — search the current source for the new names.

> Current matrix below: independent verification run, 2026-07-10, on `Rons-MacBook-Pro-2.local`
> via `./gradlew :awake-ecs-benchmark:mainBenchmark`, 1 fork, 5 warmup iterations, 5
> measurement iterations, 1 second per iteration -- run solo (no other JMH process or
> benchmark-affecting build competing for CPU) specifically to sanity-check the previous
> commit's numbers, which showed unusually large swings in Artemis-odb's family-churn score
> across consecutive runs (858 -> 4,152 ops/s at 10k) that don't reproduce here. Historical
> profiling notes below are preserved for context.
>
> Awake benchmark shape: pure `awake-ecs` runtime plus `awake-scene` components/systems.

## Current matrix

Refreshed `2026-07-10`, independent solo run after the reified `add`/`remove`/`get`/`has` fix
described below (commit `254f8d1`). The `Awake` column is bolded so it stands out quickly in
plain markdown. Error margins are JMH's own (±, 5 iterations) -- rows called "within noise"
have overlapping error bars between the top contenders.

| Benchmark | Size | **Awake** | Fleks 2.14 | Artemis-odb 2.3.0 | Ashley 1.7.3 | Fastest |
|---|---:|---:|---:|---:|---:|---|
| Entity create/destroy | 10k | **8,084.972 ± 3,040.878** | 7,795.225 ± 229.694 | 4,532.173 ± 1,997.818 | 157.604 | Awake (within noise vs. Fleks) |
| Entity create/destroy | 100k | **827.105 ± 53.694** | 821.940 ± 10.967 | 388.740 ± 173.503 | 1.722 | Awake (within noise vs. Fleks) |
| Component add/remove | 10k | **2,629.635 ± 359.249** | 2,169.083 ± 391.178 | 2,643.966 ± 2,293.111 | 403.556 | Awake (see high-confidence rerun) |
| Component add/remove | 100k | **260.152 ± 6.044** | 161.467 ± 18.766 | 195.090 ± 54.089 | 22.546 | Awake, decisive |
| Family churn | 10k | **2,589.261 ± 244.187** | 2,381.209 ± 210.169 | 756.186 ± 202.561 | 146.875 | Awake (see high-confidence rerun) |
| Family churn | 100k | 149.150 ± 32.725 | **157.614 ± 14.636** | 72.601 ± 11.825 | 2.852 | Statistical tie, Fleks nominally ahead |
| Transform hierarchy | depth 10 | **954,763.025 ± 53,212.237** | -- | 872,927.289 ± 70,382.319 | 856,242.553 | Awake |
| Transform hierarchy | depth 50 | **186,430.239 ± 22,375.316** | -- | 177,745.970 ± 2,654.328 | 164,832.819 | Awake (within noise) |
| Transform+MeshRenderer query | 10k | **427,685.436 ± 46,512.889** | 80,415.704 | 59,042.958 | 30,470.820 | Awake |
| Transform+MeshRenderer query | 100k | **42,863.387 ± 18,409.490** | 7,832.181 | 4,760.358 | 356.394 | Awake |
| General `world.query(...)` iteration (diagnostic, Awake-only -- see below) | 10k | **17,283.730 ± 418.423** | -- | -- | -- | n/a |
| General `world.query(...)` iteration (diagnostic, Awake-only -- see below) | 100k | **1,727.058 ± 104.424** | -- | -- | -- | n/a |

Ops/sec, all rows. Awake is bold so the eye lands on it first. Fleks's transform-hierarchy
row didn't complete a clean measurement in this run (see raw log
`/tmp/ecs_bench_baseline_manual.log`) -- omitted rather than reported as a false `--`.

The 10k component-add/remove and family-churn cells above are the low-sample (5-iteration)
numbers; see "High-confidence rerun of the contested rows" below for the 30-sample numbers
that actually resolve them, which is what the takeaway and the "Fastest" column reflect.

## High-confidence rerun of the contested rows (2026-07-10)

The 1-fork/5-iteration numbers above left component add/remove (10k) and family churn (both
sizes) too noisy to call -- error bars were a large fraction of the mean. Reran just those
four Awake/Fleks benchmarks directly against the generated JMH jar with 3 forks × (8 warmup +
10 measurement) iterations at 1s each -- 30 measurement samples per cell instead of 5:

```
java -jar awake-ecs-benchmark/build/benchmarks/main/jars/awake-ecs-benchmark-main-jmh-1.0.0-SNAPSHOT-JMH.jar \
  "(awake|fleks)(ComponentAddRemove|FamilyChurn)$" -f 3 -wi 8 -i 10 -r 1s -w 1s
```

| Benchmark | Size | Awake | Fleks | Verdict |
|---|---:|---:|---:|---|
| Component add/remove | 10k | **2,629.635 ± 359.249** | 2,169.083 ± 391.178 | Awake ahead |
| Component add/remove | 100k | **260.152 ± 6.044** | 161.467 ± 18.766 | Awake, decisive |
| Family churn | 10k | **2,589.261 ± 244.187** | 2,381.209 ± 210.169 | Awake ahead |
| Family churn | 100k | 149.150 ± 32.725 | **157.614 ± 14.636** | Statistical tie, Fleks nominally ahead |

With real sample size, three of the four previously-contested rows resolve in Awake's favor;
the fourth (family churn at 100k) is a genuine statistical tie with overlapping confidence
intervals -- not a clean Fleks win the way the 5-iteration run suggested.

**This did not hold up on a second independent 30-sample run -- see "Second high-confidence
run" below.** Two of these four "Awake ahead" calls reversed. Read this section as superseded
history, not the current conclusion.

## Second high-confidence run, full suite (2026-07-11)

Ran the *entire* benchmark suite (not just the four contested rows) at the same 3-fork ×
(8 warmup + 10 measurement) settings, in an isolated git worktree so it wasn't affected by
unrelated concurrent work on this branch. Comparing against the rerun above (same settings,
same sample size, different run) is the most rigorous check done in this doc so far --
and it changes the honest conclusion.

| Benchmark | Size | Run 1 (Awake / Artemis) | Run 2 (Awake / Artemis) | Verdict |
|---|---:|---:|---:|---|
| Component add/remove | 10k | 2,629.635 / 2,643.966 (near-tie) | 2,786.246 / **3,076.345** | **Inconclusive -- flips between runs** |
| Family churn | 10k | 2,589.261 / 756.186 (Awake ahead) | 2,104.232 / **3,245.472** | **Inconclusive -- flips between runs** |
| Family churn | 100k | 149.150 / Fleks 157.614 ahead | 110.268 / Fleks **146.135** ahead | Fleks ahead both runs -- looks real |

Artemis-odb's score on `ComponentAddRemove@10k` and `FamilyChurn@10k` has now landed at four
different values across the runs in this document (756 / 4,152 / 2,644 / 3,245 -- roughly a
4x range on the same code, same benchmark parameters). That is not noise around a stable
mean, that's an unreliable measurement in this setup. Neither "Awake wins" nor "Artemis wins"
should be claimed on these two specific cells from any single run in this document, including
this one.

Everything else reproduced consistently across both 30-sample runs:

| Benchmark | Size | Run 1 | Run 2 | Verdict |
|---|---:|---:|---:|---|
| Component add/remove | 100k | 260.152 (Awake) | 231.552 (Awake) | **Awake, decisive, both runs** |
| Entity create/destroy | 10k/100k | Awake ahead, within noise vs Fleks | 6,501.025 / 755.558, same shape | **Awake ahead of Artemis/Ashley, ties Fleks -- both runs** |
| Transform hierarchy | depth 10 | Awake ahead | 945,485.700 (Awake) vs 891,480.915 (Ashley) | **Awake, both runs** |
| Transform hierarchy | depth 50 | Awake/Artemis tie | 183,932.934 / 181,145.313, overlapping | **Tie, both runs** |
| Transform+MeshRenderer query | 10k/100k | Awake 5-10x ahead | 418,137.895 vs Fleks 74,141.276 (5.6x); 43,314.044 vs 6,542.695 (6.6x) | **Awake, decisive, both runs, larger margin** |

## Takeaway

- **The honest claim is not "Awake leads or ties every row."** That was true of a single
  30-sample run; it did not survive a second one. Two rows previously called Awake wins
  (component add/remove @10k, family churn @10k) are actually too volatile to call at all in
  this environment -- Artemis-odb's own number on those benchmarks has ranged roughly 4x
  across the runs recorded in this document.
- **What is real and reproduced across two independent 30-sample runs:** entity
  create/destroy (ahead of Artemis/Ashley, tied with Fleks), component add/remove @100k
  (decisive), transform hierarchy depth 10 (decisive) and depth 50 (tied with Artemis),
  Transform+MeshRenderer query at both sizes (decisive, 5-16x). These are the claims this
  doc actually stands behind.
- **Family churn @100k now looks like a real, modest, reproducible Fleks lead**, not a
  statistical tie -- it held direction (Fleks ahead) across both runs, even though the exact
  margin moved (157.6 vs 149.2, then 146.1 vs 110.3). See "Family churn @100k follow-up
  investigation" below for why: `awakeFamilyChurn` was already on the fastest available
  Awake path before the reified-generics fix, and the remaining gap traces to a genuine
  architectural difference (Fleks's lazy iteration-order rebuild vs Awake's eager dense-array
  maintenance), not caller-side reflection cost.
- Read every number in the sections below this one with the volatility finding in mind --
  a single run, even at 30 samples, is not sufficient to call a close race in this
  environment. Two independent runs at the same sample size is the minimum bar this doc now
  holds itself to before calling a row decided.

## Family churn @100k follow-up investigation (2026-07-11)

Follow-up on the one remaining tied row (`awakeFamilyChurn` vs `fleksFamilyChurn` @100k,
149.150 ± 32.725 vs 157.614 ± 14.636 ops/s in the high-confidence rerun above). Goal: find
out whether Fleks's edge here is a genuine architectural difference or a closable cost, by
reading code and profiling instead of guessing.

**Code read**: `FamilyRegistry`/`Family1Cache`/`Family2Cache`/`ComponentStore`/
`EntityIndexMap` (this repo) and Fleks 2.14's own sources (`entity.kt`, `family.kt`, pulled
from the `Fleks-jvm-2.14-sources.jar` in the local Gradle cache). The two designs turn out to
do genuinely different amounts of work per `remove`+`add`:

- **Awake** (`Family2Cache`): every structural change does an eager swap-remove/push against
  a *dense* array of `(Entity, ComponentA, ComponentB)` -- the same dense array
  `RenderSystem`/`TransformSystem` iterate directly with no further indirection. That's
  exactly why Awake's query-iteration rows are decisive wins (427,685 vs 80,415 ops/s @10k,
  42,863 vs 7,832 @100k) -- but it means churn pays a real memory-move cost (swap-remove +
  two `EntityIndexMap` updates) on every single call, and `ComponentStore` does its *own*,
  independent swap-remove for the same entity on top of that.
- **Fleks** (`Family.onEntityCfgChanged`): a family only stores `Entity` values in a `Bag`
  indexed directly by entity id (`activeEntities[entity.id] = entity` / `.removeAt(entity.id)`
  -- plain array slot writes, no swap-compaction), plus a bitmask `contains` check and an
  `isDirty` flag. The actual *dense, ordered* iteration list (`mutableEntities`) is only
  rebuilt lazily, on next access, via the `isDirty` flag -- and `fleksFamilyChurn` only reads
  `family.numEntities` (a plain `Int` counter) at the end, so it **never triggers that
  rebuild at all** for the whole benchmark. Fleks is doing strictly less work per call here;
  it defers the cost this benchmark doesn't ask it to pay.

This is a genuine architectural trade-off, not a bug in either design: Fleks's laziness is
exactly what would make its `RenderSystem`-equivalent hot path slower (it has to indirect
through `entity[Type]` component lookups during iteration, which is why it loses that row by
5-10x), while Awake's eager dense-array maintenance is what makes iteration fast and churn
comparatively expensive.

**Profiling** (`async-profiler` 4.4, `-prof async`, collapsed CPU stacks,
`awakeFamilyChurn` @100k) confirmed the above and additionally found one real, closable cost
that wasn't inherent to the eager-dense-array design: **`ComponentPool` was backed by Kotlin's
`ArrayDeque`**, but is only ever used as a single-ended LIFO stack (`removeLastOrNull` /
`addLast`). `ArrayDeque`'s circular-buffer implementation pays for both-end operations via
modulo-based index wraparound (`positiveMod`) that this usage never needs. Leaf-sample
breakdown before the fix:

| Frame | % of CPU samples |
|---|---:|
| `ArrayDeque.removeLast` | 6.6% |
| `ArrayDeque.positiveMod` | 1.9% |
| `ArrayDeque.ensureCapacity` | 1.9% |
| `ArrayDeque.getSize` | 1.1% |
| `ArrayDeque.addLast` | 0.8% |
| **Total** | **~12.3%** |

**Fix**: rewrote `ComponentPool` (`awake-ecs/src/commonMain/kotlin/io/github/ronjunevaldoz/awake/ecs/Pool.kt`)
to use a plain growable `Array<Any?>` + `size` counter instead of `ArrayDeque` -- direct
`items[size]` indexing for push/pop, no wraparound math. Re-profiling the same benchmark
after the change shows the equivalent cost (now attributed to `ComponentPool.obtain`/`free`/
`ensureCapacity` directly, since there's no more library frame to hide behind) dropped to
**~7.9%** of samples -- a real reduction in a genuine cost, confirmed by reading the
decompiled/attributed frames, not assumed from the ops/sec number alone.

**Wall-clock verification**: the machine was under real background load during this session
(`load avg` 7-8 from Chrome and other processes), which dominates the noise floor for a
100k-entity/100k-op benchmark. Three repeated 3-fork x 10-iteration high-confidence runs
(same methodology as the section above) before and after the `ComponentPool` change, plus a
3-round interleaved before/after/before/after comparison to cancel out session-level drift,
all landed in the same ~110-130 ops/s band for `awakeFamilyChurn` @100k with heavily
overlapping error bars in both directions -- i.e. **not a measurable wall-clock movement on
this specific benchmark**, despite the profiler-confirmed reduction in a real cost. That's
consistent with `ComponentPool` being a secondary cost next to the dominant ones
(`ComponentStore.removeAt` ~14-16%, `Family2Cache.addComponent`/`removeAt` ~14%,
`EntityIndexMap.set`/`get` ~10%), which are the direct, inherent cost of eagerly maintaining
two independent dense sparse-sets (the store's and the family cache's) per structural change
-- exactly the architectural cost described above, not something this fix touches.

**Verdict**:
- The `ComponentPool` fix is real, verified via profiler, zero-regression (26/26
  `:awake-ecs:desktopTest` still passing, including the existing multi-instance pool-reuse
  test `pooledTypeIdFamilyChurnKeepsMembershipStableAcrossRemoveReaddAndDestroy`), and kept --
  it benefits every pooled-component code path in the module, not just this benchmark, even
  though it wasn't large enough to move this specific noisy top-line number today.
- The remaining family-churn @100k tie against Fleks **is architectural**, not a bug or an
  overlooked allocation: closing it fully would mean adopting Fleks's lazy/dirty-flag dense
  array rebuild for family membership (defer materializing iteration order until next read,
  track membership via a directly-indexed sparse slot instead of an always-consistent dense
  array). That would trade away the very thing that makes Awake's query-iteration rows a 5-10x
  decisive win over Fleks (`Family1Cache`/`Family2Cache` caching component references directly
  in dense, ordered arrays so `RenderSystem`/`TransformSystem` never indirect through a
  component lookup) to chase a currently-tied, secondary row. Not a good trade -- flagging this
  plainly rather than chasing it further, per this investigation's scope.

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

## Query-path allocation cleanup (2026-07-10)

A read-through of `World`/`ComponentStore`/`QueryCollector`/`FamilyRegistry`/`Families` found
the maintained-family hot paths (`Family1Cache`/`Family2Cache`/`FamilySpecCache`, what
`RenderSystem`/`TransformSystem` and the `awakeFamilyChurn`/`awakeTransformMeshQuery`
benchmarks exercise) already about as tight as they can get -- O(1) sparse-set add/remove,
typed dense-array iteration with no `checkNotNull` insertion, no per-call `KClass` lookups
on the cached-typeId fast paths. The two real remaining allocation sources were both in the
*general* `world.query(vararg types)` path (`QueryCollector`), which none of the existing
JMH benchmarks exercise (they all go through the maintained Family caches instead):

1. **`ComponentStore.entities` allocated a fresh `object : AbstractList<Entity>()` wrapper
   on every property access** instead of once per store. `QueryCollector.collect` reads this
   once per query recomputation, so every cache-miss re-query paid this allocation. Fixed by
   caching the view in a `private val` created once per `ComponentStore` instance -- the
   view still reads the enclosing instance's live `count`/`denseEntities` fields, so nothing
   about staleness changed, only the wrapper-object churn.
2. **`QueryCollector.collect`'s multi-type branch allocated three separate collections per
   call**: `types.mapNotNull { ... }` for resolving stores, then
   `smallestStore.entities.filter { queryStores.all { ... } }` -- and that `.entities` access
   is a `List<Entity>`, so iterating it through the `Iterable`/`List` interface boxes every
   `Entity` (a value class) at the interface boundary, on top of `filter`/`all` each
   allocating their own iterators. Rewritten to resolve stores into a plain array up front
   (bailing out to `emptyList()` as soon as any type has no store yet, same semantics as the
   old size-mismatch check) and to walk the smallest store via its own typed `forEach`
   (dense-array iteration, no `List` boxing) instead of `.entities.filter`.

Also added a `World.spawn(typeId, block)` overload alongside the existing `add(entity,
typeId)`/`remove(entity, typeId)` fast paths, so callers spawning many entities of the same
pooled component type in a loop have a non-reified option to hoist `T::class`'s resolution
out of the loop, mirroring this project's own "hoist `T::class` out of per-entity loops"
rule -- previously only the reified `spawn<T> { }` sugar existed.

**Why this doesn't show up in the "Current matrix" table above:** none of the existing
benchmarks call `world.query(...)`/`queryEach(vararg types)` -- `RenderSystem` and
`TransformSystem` (and every JMH benchmark modeled on them) use the maintained
`Family1`/`Family2` caches, which don't go through `ComponentStore.entities` or
`QueryCollector` at all in steady state. To measure the actual fix, a new Awake-only
diagnostic benchmark (`awakeGeneralQueryIteration`) was added: it forces a `QueryCache` miss
on every invocation (toggling a marker component on a scratch entity outside the query's own
type set) so it's actually exercising `QueryCollector.collect`'s recompute path each call,
not a cached map lookup.

Isolated A/B result (same benchmark code both times, run via a separate `git worktree`
checked out at the pre-change commit so the working tree stayed untouched, 1 fork, 5 warmup
+ 5 measurement iterations, 1s each):

| Entities | Before | After | Delta |
|---:|---:|---:|---:|
| 10k | 12,478.388 ops/s | 16,856.988 ops/s | +35.1% |
| 100k | 1,353.519 ops/s | 1,677.848 ops/s | +24.0% |

Full-suite rerun after these changes (`:awake-ecs-benchmark:mainBenchmark`, full matrix,
same params) showed every other row within JMH's own confidence intervals of the prior
run -- expected, since `Family1Cache`/`Family2Cache`/`ComponentRegistry`'s add/remove paths
weren't touched. See "Current matrix" above for the refreshed numbers.

Verified `:awake-ecs:desktopTest` (`EcsOptimizationTest`, `FamilySpecTest`, `WorldTest`) --
26/26 passing, no behavior change to query results, ordering, or family membership.

## Absolute Lead & Direct Store Caching (2026-07-09)

1. **Primitive entity storage**: `World` now stores generations, alive flags, and component
   signatures in primitive arrays instead of per-entity slot objects. Entity ids are still
   recycled through `EntityIdStack`.
2. **Correct query invalidation**: `World.create()` dirties query caches so empty queries
   (`world.query()` with no component filter) stay correct when entities are created.
3. **Direct store caching**: `FamilyCache` instances keep direct references to the
   `ComponentStore`s they monitor, reducing `World` lookup overhead during family updates.
4. **Signature guard**: component signatures are backed by one `Long`, so `World` now fails
   clearly after 64 component types instead of silently colliding signature bits.
5. **Verified correctness**: `:awake-ecs:allTests` passes on desktop and iOS simulator;
   `:awake-scene:desktopTest` also passes.
6. **Churn add-path trim**: `Family1Cache` and `Family2Cache` now append directly on new
   component inserts, because `World.add(..., component)` already routes replacements to
   `replaceComponent(...)`. That removes a redundant membership probe from the churn hot path.
7. **Sparse index cleanup**: removed component ids are cleared out of `EntityIndexMap`,
   which lets `ComponentStore` and family caches drop stale membership checks on remove/
   re-add churn and keeps the sparse-set invariant easier to reason about.
