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
| Entity create/destroy | 10k entities | 3,437.209 ops/s | 7,720.156 ops/s | Fleks |
| Entity create/destroy | 100k entities | 247.408 ops/s | 753.186 ops/s | Fleks |
| Component add/remove | 10k entities | 896.149 ops/s | 2,806.517 ops/s | Fleks |
| Component add/remove | 100k entities | 75.981 ops/s | 163.633 ops/s | Fleks |
| Transform+MeshRenderer query | 10k entities | 23,181.040 ops/s | 78,912.326 ops/s | Fleks |
| Transform+MeshRenderer query | 100k entities | 726.123 ops/s | 7,243.667 ops/s | Fleks |
| TransformSystem propagation | depth 10 | 233,345.137 ops/s | 715,333.477 ops/s | Fleks |
| TransformSystem propagation | depth 50 | 62,368.025 ops/s | 124,769.081 ops/s | Fleks |

## Takeaway

Awake's query path improved after adding cached untyped queries plus typed sparse-store
iteration for systems that need component values. Compared with the first scorecard run,
Transform+MeshRenderer query throughput increased about 9.3x at 10k entities and 3.2x at
100k entities.

Fleks is still faster for stable family iteration, especially at 100k entities, where it
measured about 10x faster in this short run. Awake's custom ECS still fits the current
Phase 3 scope because the runtime is small, owned by the engine, and avoids taking a
general-purpose ECS dependency into `awake-ecs`; if future scenes approach large entity
counts, the next custom optimization should be a maintained family index for common
component combinations.
