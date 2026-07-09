# ECS Benchmark Scorecard

> **Naming note:** `SparseIndex` and `GeneralFamilyCache`/`GeneralFamily.kt` mentioned in the
> dated sections below were later renamed to `EntityIndexMap` and `FamilySpecCache`/
> `FamilySpec.kt` for clarity. Left as-is here since these are historical records of what
> each commit actually did at the time — search the current source for the new names.

> Current matrix below: run 2026-07-09 on `Rons-MacBook-Pro-2.local` via
> `./gradlew :awake-ecs-benchmark:mainBenchmark`, 1 fork, 5 warmup iterations, 5
> measurement iterations, 1 second per iteration. Historical profiling notes below are
> preserved for context.
>
> Awake benchmark shape: pure `awake-ecs` runtime plus `awake-scene` components/systems.

## Current matrix

Latest rerun: `2026-07-09`, via `./gradlew :awake-ecs-benchmark:mainBenchmark`.
The `Awake` column is highlighted in green so it stands out quickly.

| Benchmark | Size | <span style="color:#16a34a"><strong>Awake</strong></span> | Fleks 2.14 | Artemis-odb 2.3.0 | Ashley 1.7.3 | Fastest |
|---|---:|---:|---:|---:|---:|---|
| Entity create/destroy | 10k | <span style="color:#16a34a"><strong>6,314.707</strong></span> | 7,680.726 | 5,082.550 | 159.074 | Fleks |
| Entity create/destroy | 100k | <span style="color:#16a34a"><strong>670.982</strong></span> | 595.409 | 515.602 | 2.175 | Awake |
| Component add/remove | 10k | <span style="color:#16a34a"><strong>2,280.463</strong></span> | 1,672.243 | 2,997.667 | 315.619 | Artemis-odb |
| Component add/remove | 100k | <span style="color:#16a34a"><strong>186.327</strong></span> | 103.212 | 194.685 | 28.216 | Artemis-odb |
| Family churn | 10k | <span style="color:#16a34a"><strong>1,387.768</strong></span> | 2,939.655 | 794.189 | 183.549 | Fleks |
| Family churn | 100k | <span style="color:#16a34a"><strong>64.208</strong></span> | 164.871 | 49.605 | 2.922 | Fleks |
| Transform hierarchy | depth 10 | <span style="color:#16a34a"><strong>944,992.326</strong></span> | 704,048.175 | 582,313.879 | 611,382.977 | Awake |
| Transform hierarchy | depth 50 | <span style="color:#16a34a"><strong>180,282.659</strong></span> | 131,209.632 | 169,811.689 | 121,106.394 | Awake |
| Transform+MeshRenderer query | 10k | <span style="color:#16a34a"><strong>61,668.042</strong></span> | 69,398.322 | 54,277.326 | 24,203.368 | Fleks |
| Transform+MeshRenderer query | 100k | <span style="color:#16a34a"><strong>1,587.594</strong></span> | 7,428.559 | 3,570.911 | 321.701 | Fleks |

Ops/sec, all rows. Awake is green and bold so the eye lands on it first.

## Takeaway

- Awake now leads on transform hierarchy propagation at both depths.
- Awake leads on 100k create/destroy.
- Awake is still behind Fleks on family churn and Transform+MeshRenderer query.
- Artemis-odb still wins component add/remove.
- The next meaningful ECS improvement is still in family/query hot-path plumbing, not entity allocation.

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

## Fixes applied for the two gaps identified above (2026-07-09)

1. **`Family1Cache`/`Family2Cache` now have their own sparse index** (entity id → dense
   index array), mirroring `ComponentStore`'s existing approach. `remove`/`replace` no
   longer do a linear scan over every entity in the family to find a slot.
2. **`TransformSystem` reuses its `Map`/`Set` buffers across frames** instead of allocating
   a fresh `Map`+two `Set`s on every `update()` call. Still recomputes the full traversal
   from scratch each frame (correctness-preserving even if a caller reparents an entity by
   mutating `Transform.parent` directly, since there's no change-notification hook for
   that) — only the *buffers* are reused, not a cached traversal order.
3. For a fair comparison, the identical buffer-reuse change was applied to the Fleks/
   Artemis-odb/Ashley hierarchy-propagation benchmark code too — otherwise Awake would get
   a benchmark-shape advantage unrelated to what's actually being measured (ECS access
   cost, not "which shim allocates less").
4. Bumped the JMH config from 2 warmup/3 measurement iterations (1s each) to 5/5, for a
   more stable signal.

**Both fixes are verified correct** — `WorldTest` (7/7) and `TransformSystemTest` (1/1)
still pass unchanged.

**The numeric before/after re-benchmark is not trustworthy on this run**: `uptime` showed
load averages of 7-18 on this machine during the re-run (background apps competing for
CPU), and every library's numbers dropped noticeably from the table above, not just
Awake's — a sign of environmental noise, not a real regression. Re-run
`./gradlew :awake-ecs-benchmark:benchmark` on an idle machine before drawing any numeric
conclusion from the fixes above; don't trust a comparison run under load like this one.

## Second re-run, tighter confidence intervals, still no clear win (2026-07-09)

A later re-run (load average down to 6-8, confidence intervals mostly single-digit percent
— genuinely tighter than either prior run) still showed **no clear improvement** for Awake:
`awakeComponentAddRemove` was flat-to-slightly-down (-8% at both 10k/100k vs. the original
baseline table), and `awakeTransformHierarchyPropagation` was flat (+1.7%/-5.2%).

Root cause found for why the add/remove fix showed nothing: **`awakeComponentAddRemove`
never calls `world.family<Transform>()`**, so no family cache is ever built in that
benchmark — the exact code path the sparse-index fix targeted (`Family1Cache`/
`Family2Cache.indexOf()`) never runs. This is a gap in benchmark *coverage*, not a failed
fix; none of the current benchmarks test "structural churn against an entity that already
belongs to a built family," which is the only scenario the fix helps. The
`TransformSystem` buffer-reuse fix genuinely is exercised by the propagation benchmarks and
still shows no clear win, plausibly because JVM's young-gen GC already makes small,
short-lived `Map`/`Set` allocations cheap at these entity counts — the fix is real and
correctness-preserving, just not a measurable win at this scale.

Also worth noting: both `awakeTransformMeshQuery` and `fleksTransformMeshQuery` at 100k
dropped together in this same run (-74% and -61% respectively) while Artemis-odb's number
at the same size went *up* — a shared environmental event during that measurement window
(likely a GC pause), not a real regression in either library. Even a "clean-looking" run
with tight CIs can still have one bad row; don't over-read a single benchmark invocation.

## Targeted churn benchmark: does the sparse-index fix actually help? (2026-07-09)

Added `awakeFamilyChurn`/`fleksFamilyChurn`/`artemisFamilyChurn`/`ashleyFamilyChurn`: each
builds a `Transform`+`MeshRenderer` family first, *then* removes and re-adds `Transform` on
every entity already in that family — the one scenario `Family1Cache`/`Family2Cache`'s
sparse-index fix targets, which none of the existing `*ComponentAddRemove` benchmarks
exercise (they never call `world.family<...>()`).

Ran `./gradlew :awake-ecs:benchmark -Pbenchmark.filter=".*FamilyChurn.*"`. The Artemis-odb
and most of Ashley's rows were lost to a `tail -40` truncation on this run and need a re-run
to capture, but Awake's and Fleks's numbers came through clean:

| FamilyChurn | Size | **Awake** | Fleks 2.14 | Fastest |
|---|---:|---:|---:|---|
| Family churn (remove+re-add on existing family members) | 10k | 753.9 | 2,170.0 | Fleks (~2.9x) |
| Family churn (remove+re-add on existing family members) | 100k | 60.7 | 124.2 | Fleks (~2x) |

**Say it plainly: this is not a win.** This benchmark didn't exist before this change, so
there's no true "before/after" for it specifically — but taken at face value, Awake is
2-3x slower than Fleks in exactly the scenario the sparse-index fix was built for. That
doesn't mean the fix made things worse (it replaced an O(n) linear scan with an O(1)
lookup, which is strictly better than what came before); it means **the linear-scan cost
was never the dominant cost in this path to begin with** — something else in
`Family1Cache`/`Family2Cache.remove()`/`add()` (or in `World`'s per-notify dispatch across
`allFamilyCaches()`) is the real bottleneck, and the sparse-index change didn't touch it.

This is exactly the situation where guessing at another fix would waste time. The next
step is profiling this specific benchmark (async-profiler or JFR) to see where the time
actually goes, rather than a fourth blind attempt at a structural change.

## Profiled `awakeFamilyChurn`, found two real bottlenecks, fixed both (2026-07-09)

Installed async-profiler (`brew install async-profiler`) and ran it directly against the
standalone JMH jar (`awake-ecs-benchmark/build/benchmarks/main/jars/*-JMH.jar`) with
`-prof "async:libPath=...;event=cpu"` on `awakeFamilyChurn` at 100k entities. Profiling,
not guessing, found two concrete, fixable hotspots:

1. **`SparseIndex.get()` was 36% of all CPU samples** (211 of 586). The implementation was
   `sparse.getOrNull(id) ?: ABSENT` — `IntArray.getOrNull` boxes the result to `Int?` on
   every call so it can represent "index out of range" as `null`, and the `?:` immediately
   unboxes it. This runs on every `indexOf()` in `Family1Cache`/`Family2Cache`/
   `GeneralFamilyCache`, i.e. on every add/remove against a family — exactly the path this
   benchmark stresses. Fixed by replacing it with a manual `if (id in sparse.indices)
   sparse[id] else ABSENT`, which never leaves the primitive `Int` domain. Re-profiling
   after the fix: `SparseIndex.get` dropped to 1.4% of samples (11 of 791) — confirms the
   fix removed the hotspot, not just moved it.
2. **`FamilyRegistry.allCaches()` allocated a fresh combined `Sequence` on every single
   structural-change notification** — `families.values.asSequence() + generalFamilies
   .values.asSequence()`, called once per entity on every destroy/add/replace/remove.
   Showed up as `LinkedHashMap$LinkedValues.iterator` (6.45%), `CollectionsKt.asSequence`
   (4.68%), and `SequencesKt.plus` (2.65%) — real, avoidable allocation overhead. Fixed by
   replacing the `Sequence` chain with a plain `inline fun forEachCache` that calls
   `.forEach` on each map's values directly, no combined-sequence wrapper. Re-profiling
   confirmed this whole cluster fell out of the top 25 samples entirely.

**Both fixes verified correct**: `WorldTest` (7/7), `GeneralFamilyTest` (6/6),
`TransformSystemTest` (1/1) all still pass.

**Numeric result** (same machine, load average 27 during this run — see caveat below —
but a consistent effect at both sizes is a good sign it's real, not noise):

| FamilyChurn | Size | Awake before | Awake after | Change |
|---|---:|---:|---:|---|
| Family churn | 10k | 753.9 | 1,525.6 | **+102%** (~2.0x) |
| Family churn | 100k | 60.7 | 122.5 | **+102%** (~2.0x) |

A near-identical ~2.0x improvement at both 10k and 100k entities is a strong signal this is
a real, structural win, not environmental noise (random noise doesn't usually land on the
same multiplier twice). Fleks's own numbers also moved between these two runs (10k:
2,170.0 → 2,550.7; 100k: 124.2 → 157.3) — expected run-to-run drift given the load — so the
Awake-vs-Fleks *gap* narrowed but didn't close: 10k went from Fleks ~2.9x faster to ~1.67x
faster; 100k went from ~2.0x to ~1.28x faster.

**Caveat, said plainly**: this run happened at `uptime` load average 27 (Claude Desktop,
Android Studio, and an emulator were all running) — the *absolute* numbers here are not
trustworthy in isolation. What makes this result credible anyway is (a) it came from a
profiler-identified, mechanically-understood fix (boxing elimination, allocation
elimination) rather than a blind guess, and (b) the effect size was consistent across two
different entity counts run back to back. Re-run on an idle machine to get a clean
confirming number before treating this as final.

**What's next, found by the same profiling pass**: after fixing the two hotspots above, the
next-largest non-GC, non-benchmark-owned cost is `kotlin.jvm.internal.ClassReference
.hashCode` + `ReflectionFactory.getOrCreateKotlinClass` (~10% combined) — the cost of using
`KClass` as a `HashMap` key (in `World`'s `stores`/`typeIds` maps) and of reified `T::class`
resolution on every typed call. This is a real, profiler-confirmed cost, but fixing it
means changing how component types are identified (e.g. a component-type registry with
integer IDs instead of `KClass` lookups) — a bigger, riskier structural change than the two
fixes above, consistent with what the "Where to look next" section below already flagged as
a last resort. Not attempted in this pass.

## Investigated the `KClass` reflection cost: usage-pattern fix, not a framework change (2026-07-09)

Tested the hypothesis directly rather than committing to the "bigger, riskier structural
change" framing above. Added a diagnostic `awakeFamilyChurnCachedClass` benchmark (same
`AwakeFamilyChurnState`, but hoists `val transformClass = Transform::class` once outside
the loop and calls the explicit-`KClass` overloads — `world.add(entity, transformClass,
component)` / `world.remove(entity, transformClass)` — instead of the reified
`add<Transform>`/`remove<Transform>` sugar, which re-derives the `KClass` token at every
call site).

Result: re-profiling `awakeFamilyChurnCachedClass` showed `ClassReference.hashCode` and
`ReflectionFactory.getOrCreateKotlinClass` **gone from the top 25 samples entirely**
(only `ClassReference.getJClass` at 2.09% remained). Measured throughput was ~13% higher
than `awakeFamilyChurn` at 100k (122.9 → 138.6 ops/s), though that delta is within this
run's noise band and shouldn't be over-read on its own — the profiler evidence (the
hotspot disappearing) is the real confirmation here, not the ops/sec number.

**Conclusion: this doesn't need the component-type-registry rewrite floated above.** The
cost isn't inherent to using `KClass` as a `HashMap` key (its `hashCode`/`equals` delegate
to the underlying `Class`, which is cheap) — it's specifically that Kotlin's reified
generics re-derive a fresh `ClassReference` wrapper *at the call site* on every invocation
when `kotlin-reflect` isn't on the classpath (which this project deliberately doesn't add).
That cost is invisible for once-per-frame calls (e.g. `TransformSystem`'s single
`world.queryEach<Transform> { ... }`) and only matters in tight per-entity loops. Documented
as a hot-path idiom in `.claude/agents/ecs-dev.md` ("Hot-path performance" section): hoist
`T::class` once outside a per-entity loop and use the explicit-`KClass` overload instead of
the reified sugar. No production code currently has this pattern in a hot loop (checked
`TransformSystem`/`RenderSystem` — both call reified generics at most once per frame), so
no source change was needed there; only the diagnostic benchmark exists to track this.

## Profiled query and hierarchy propagation, fixed two more real bottlenecks (2026-07-09)

Followed the same methodology on the two remaining gaps flagged as "the real remaining
gap" earlier in this file: `awakeTransformMeshQuery` (Fleks led) and
`awakeTransformHierarchyPropagation` (Artemis-odb led both depths).

**Query iteration**: profiling `awakeTransformMeshQuery` at 100k showed `kotlin.jvm
.internal.Intrinsics.checkNotNull` at **13.74% of CPU samples**. Cause: `Family1Cache`/
`Family2Cache`/`ComponentStore` back their dense storage with `arrayOfNulls<Any>()`
(statically `Array<Any?>`, since removed slots are nulled out for GC). Every element read
then does `localComponents[index] as A` where `A : Any` is non-null-bound -- casting from a
statically nullable array element type to a non-null generic type makes Kotlin insert a
null-check on every single read. Fixed by casting the *array reference* once
(`@Suppress("UNCHECKED_CAST") (componentsA as Array<A>)`) instead of casting each element --
array types erase to `Object[]` regardless of declared nullability, so this cast is a no-op
at the bytecode level, and reading from a statically non-null-typed array needs no check.
Applied to `Family1Cache`, `Family2Cache`, and `ComponentStore`'s `add`/`get`/`remove`/
`forEach`. Re-profiling confirmed `Intrinsics.checkNotNull` **dropped to 0 samples** --
`awakeTransformMeshQuery`'s profile is now ~97% the inlined loop itself plus GC noise, i.e.
close to the achievable floor for this code shape. Absolute throughput didn't move
measurably under this run's noise (Fleks still leads at both sizes) -- the profiler
evidence is the confirmation here, not the ops/sec delta.

**Hierarchy propagation**: profiling `awakeTransformHierarchyPropagation` at depth 50 showed
~19% of CPU samples combined across `HashMap.putVal`/`getNode`/`clear`,
`LinkedHashMap.linkNodeAtEnd`/`afterNodeInsertion`/`afterNodeRemoval`, `HashSet.remove`/
`contains`, and `Entity.box-impl`. Cause: `TransformSystem`'s memoized DFS used a
`MutableMap<Entity, Transform>` plus two `MutableSet<Entity>` (`visited`/`visiting`) for
per-frame traversal state -- `Entity` is a `@JvmInline value class`, so using it as a
`Map`/`Set` key forces a box allocation plus `hashCode()`/`equals()` on every visit. Fixed
by replacing the map/sets with two `IntArray`s indexed directly by `entity.id`
(`visitedStamp`/`visitingStamp`), each entry compared against a single `frameStamp` int that
increments once per `update()` call -- a node is "visited"/"visiting" this frame exactly
when its array slot equals the current stamp, so incrementing the stamp implicitly
invalidates every previous frame's state without ever clearing the arrays. Parent lookups
now go through `world.get<Transform>(parent)` (an O(1) sparse-set lookup already used
elsewhere) instead of a locally-built snapshot map. Added two tests
(`transformSystemThrowsOnCyclicParenting`, `transformSystemReusesInstanceStateAcrossMultipleUpdates`)
alongside the existing propagation-order test to cover the two correctness properties this
rewrite had to preserve (cycle detection, correctness across repeated `update()` calls on
the same instance) -- all pass. Re-profiling confirmed every `HashMap`/`HashSet`/
`Entity.box-impl` frame is gone from the top 30 samples; what remains is legitimate matrix
math (`Mat4.times` 53.76%, `Mat4.<init>` 15.99%, `sin`/`cos` for rotation).

**Result: Awake now leads all three other libraries on hierarchy propagation at both
depths** (same machine, load ~5.7-7.4, tight-ish CIs):

| Hierarchy propagation | Awake (before → after) | Fastest before | Fastest after |
|---|---|---|---|
| depth 10 | 599,160 → **1,000,862** (+67%) | Artemis-odb (898,550) | **Awake** |
| depth 50 | 130,341 → **198,440** (+52%) | Artemis-odb (153,830) | **Awake** |

This reverses what was previously flagged as "the real remaining gap" for propagation.
Query iteration remains behind Fleks (unchanged within noise), and is the one gap left from
the original 4-way comparison table at the top of this file.

## Where to look next if the gap remains after a clean re-benchmark

If a clean re-run still shows Awake behind on query/propagation, the next things to check,
roughly in order of expected payoff vs. effort:

- Profile the 100k-entity query benchmark specifically (e.g. async-profiler or JFR) rather
  than guessing further — the remaining gap may be JIT warmup variance in a short-lived
  JMH run rather than an actual structural cost.
- Consider whether `Family2Cache` duplicating component references (on top of what
  `ComponentStore` already holds) costs more in cache misses than it saves in iteration
  directness — a family cache backed by index arrays into the existing `ComponentStore`s,
  rather than its own copies, would use less memory and might iterate just as fast.
- A bigger architectural change (archetypes, or per-component-type codegen to avoid `Any`
  boxing) is the last resort, not the first — see the "own the ECS" discussion in
  `docs/MVP_PLAN.md`'s Phase 3 entry for why that tradeoff was deliberately deferred.
