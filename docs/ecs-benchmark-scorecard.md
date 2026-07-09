# ECS Benchmark Scorecard

> Run: 2026-07-09 on `Rons-MacBook-Pro-2.local` via
> `./gradlew :awake-ecs-benchmark:mainBenchmark`
>
> Harness: `kotlinx-benchmark` 0.4.17 / JMH, 1 fork, 2 warmup iterations,
> 3 measurement iterations, 1 second per iteration. Short-run numbers are useful for
> relative direction, not final release claims.
>
> Awake benchmark shape: pure `awake-ecs` runtime plus `awake-scene` components/systems.

## Results

| Benchmark | Size | Awake sparse-set | Fleks 2.14 | Faster |
|---|---:|---:|---:|---|
| Entity create/destroy | 10k entities | 4,798.465 ops/s | 6,925.674 ops/s | Fleks |
| Entity create/destroy | 100k entities | 661.946 ops/s | 679.470 ops/s | Tie-ish |
| Component add/remove | 10k entities | 1,855.741 ops/s | 1,586.974 ops/s | Awake |
| Component add/remove | 100k entities | 223.971 ops/s | 189.201 ops/s | Awake |
| Transform+MeshRenderer query | 10k entities | 58,259.364 ops/s | 78,563.332 ops/s | Fleks |
| Transform+MeshRenderer query | 100k entities | 2,685.512 ops/s | 7,570.603 ops/s | Fleks |
| TransformSystem propagation | depth 10 | 614,658.626 ops/s | 692,681.141 ops/s | Fleks |
| TransformSystem propagation | depth 50 | 70,089.221 ops/s | 134,836.272 ops/s | Fleks |

## Takeaway

Awake's structural path improved after replacing list-backed component stores with
primitive sparse/dense arrays. In this short run, component add/remove is now ahead of
Fleks at both measured sizes, and 100k create/destroy is close enough to treat as
noise-sensitive rather than a decisive gap.

Transform+MeshRenderer iteration improved from the first scorecard after adding maintained
family caches and component-only family iteration for systems that do not need entity
handles. It is still behind Fleks at 10k and 100k entities, especially at 100k. The next
custom ECS optimization should focus on type-specialized family storage or generated
system accessors, not another fallback query cache.
