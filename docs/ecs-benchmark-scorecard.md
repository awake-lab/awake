# ECS Benchmark Scorecard

> Table below: run 2026-07-09 on `Rons-MacBook-Pro-2.local` via
> `./gradlew :awake-ecs-benchmark:benchmark`, machine otherwise idle, 1 fork, 2 warmup
> iterations, 3 measurement iterations, 1 second per iteration — the last *reliable* run,
> predating the fixes described below. The JMH config has since been bumped to 5/5
> iterations (see "Fixes applied" section); re-run on an idle machine to refresh this table.
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
