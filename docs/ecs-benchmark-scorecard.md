# ECS Benchmark Scorecard

> Run: 2026-07-09 on `Rons-MacBook-Pro-2.local` via
> `./gradlew :awake-ecs-benchmark:mainBenchmark`
>
> Harness: `kotlinx-benchmark` 0.4.17 / JMH, 1 fork, 2 warmup iterations,
> 3 measurement iterations, 1 second per iteration. Short-run numbers are useful for
> relative direction, not final release claims.

## Results

| Benchmark | Size | Awake sparse-set | Fleks 2.14 | Faster |
|---|---:|---:|---:|---|
| Entity create/destroy | 10k entities | 3,249.747 ops/s | 6,752.976 ops/s | Fleks |
| Entity create/destroy | 100k entities | 788.694 ops/s | 712.517 ops/s | Awake |
| Component add/remove | 10k entities | 1,255.485 ops/s | 2,702.692 ops/s | Fleks |
| Component add/remove | 100k entities | 127.733 ops/s | 162.238 ops/s | Fleks |
| Transform+MeshRenderer query | 10k entities | 2,493.003 ops/s | 62,378.257 ops/s | Fleks |
| Transform+MeshRenderer query | 100k entities | 227.585 ops/s | 2,967.270 ops/s | Fleks |
| TransformSystem propagation | depth 10 | 330,533.812 ops/s | 580,263.842 ops/s | Fleks |
| TransformSystem propagation | depth 50 | 81,781.594 ops/s | 101,730.121 ops/s | Fleks |

## Takeaway

Fleks is meaningfully faster for the realistic hot path this phase cares about:
Transform+MeshRenderer query iteration. At 100k entities, Fleks measured about 13x faster
than Awake's initial sparse-set query. Awake's custom ECS still fits the current Phase 3
scope because the runtime is small, owned by the engine, and avoids taking a general-purpose
ECS dependency into `awake-ecs`; if scenes start approaching large entity counts, query
caching or family-style indexes should be the first custom ECS optimization to revisit.
