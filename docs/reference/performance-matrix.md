# Engine Performance Matrix & Scorecard

High-level dashboard of Awake's performance baselines, micro-benchmark suites, algorithmic complexity guarantees, and coverage gap roadmap.

---

## 1. Current Benchmark Matrix

### A. ECS & Scene Graph Runtime (`:awake:ecs:benchmark`)

Ran via `./gradlew :awake:ecs:benchmark:mainBenchmark` (1 fork, 5 warmup iterations, 5 measurement iterations, 1s/iteration on Desktop JVM).
Detailed analysis and historic profiling notes live in [docs/ecs-benchmark-scorecard.md](../ecs-benchmark-scorecard.md).

| Benchmark Category | Scale / Depth | **Awake** (ops/sec) | Fleks 2.14 | Artemis-odb 2.3.0 | Ashley 1.7.3 | Leader |
|---|---:|---:|---:|---:|---:|---|
| **Entity Create/Destroy** | 10k | **7,691.075** | 6,920.511 | 4,439.211 | 156.036 | **Awake** |
| **Entity Create/Destroy** | 100k | **690.964** | 709.676 | 423.238 | 1.979 | Fleks |
| **Component Add/Remove** | 10k | **2,509.476** | 2,503.291 | 631.340 | 309.901 | **Awake** |
| **Component Add/Remove** | 100k | **161.924** | 180.652 | 181.071 | 20.227 | Artemis-odb |
| **Family / Query Churn** | 10k | **2,227.453** | 2,493.170 | 700.977 | 134.987 | Fleks |
| **Family / Query Churn** | 100k | **100.447** | 148.264 | 63.781 | 2.689 | Fleks |
| **Transform Hierarchy** | Depth 10 | **852,161.514** | 667,335.297 | 749,656.980 | 760,426.878 | **Awake** |
| **Transform Hierarchy** | Depth 50 | **175,466.418** | 128,070.116 | 159,120.649 | 138,849.430 | **Awake** |
| **Transform + Mesh Query** | 10k | **366,909.790** | 60,089.231 | 51,430.419 | 25,317.422 | **Awake (6x lead)** |
| **Transform + Mesh Query** | 100k | **38,423.623** | 4,323.288 | 1,106.614 | 265.553 | **Awake (8.8x lead)** |

---

### B. UI Layout & Frame Timing (`:awake:ui:benchmark`)

Ran via `./gradlew :awake:ui:benchmark:mainBenchmark`.
Measures pure `ui-core` layout pass execution time across deep row/column trees without widget styling noise.

| Benchmark Target | Nesting Depth | Metric | Target SLA |
|---|---:|---|---|
| `LayoutNestingBenchmark` | Depth 2 | Average Frame Time | $< 25\,\mu\text{s}$ |
| `LayoutNestingBenchmark` | Depth 4 | Average Frame Time | $< 75\,\mu\text{s}$ |
| `LayoutNestingBenchmark` | Depth 6 | Average Frame Time | $< 200\,\mu\text{s}$ |
| `LayoutNestingBenchmark` | Depth 8 | Average Frame Time | $< 500\,\mu\text{s}$ |

---

## 2. Subsystem Coverage & Gap Analysis

| Subsystem | Module | Benchmark Harness | Status | Roadmap & Gap Description |
|---|---|---|---|---|
| **ECS Storage** | `:awake:ecs` | `:awake:ecs:benchmark` | ✅ Covered | Entity arena, component bitmasks, query cache invalidation. |
| **Scene Graph** | `:awake:scene` | `:awake:ecs:benchmark` | ✅ Covered | Hierarchy matrix evaluation and transform propagation. |
| **UI Layout** | `:awake:ui:ui-core` | `:awake:ui:benchmark` | ✅ Covered | Deep nesting trial-measure curves. |
| **Geometry** | `:awake:core:geometry` | — | 🟡 Missing | Needs `MeshSimplifier` decimation throughput benchmark (50k–500k triangles). |
| **Animation** | `:awake:core:animation` | — | 🟡 Missing | Needs skeletal SLERP pose blending benchmark across 50–200 joint hierarchies. |
| **Asset I/O** | `:awake:asset:gltf` | — | 🟡 Missing | Needs `GltfParser` binary throughput benchmark ($\text{MB/s}$). |
| **Physics** | `:awake:backend:jolt` | — | 🟡 Missing | Needs `PhysicsWorld.step()` tick cost with 1,000 active dynamic bodies. |
| **Rendering** | `:awake:backend:vulkan` | — | 🟡 Missing | Needs command buffer recording and descriptor set binding throughput. |
| **Text Engine** | `:awake:ui:text` | — | 🟡 Missing | Needs font glyph lookup and multiline text measurement throughput. |

---

## 3. Execution & Verification Strategy

Awake uses a 3-tier strategy to maintain high performance without slowing down daily development:

```
┌────────────────────────────────────────────────────────────────────────┐
│ Tier 1: Pre-Push Hook (Fast, <100ms, Deterministic)                   │
│ - Exact trial count tests (e.g. WeightTrialReuseTest, TrialMeasureTest)│
│ - Algorithmic O(N) complexity checks without clock noise               │
│ - Performance Matrix documentation verification in pre-push            │
├────────────────────────────────────────────────────────────────────────┤
│ Tier 2: On-Demand Local Profiling (During Optimization Tasks)         │
│ - Run by awake-engine-core-engineer during ECS/math refactors          │
│ - ./gradlew :awake:ecs:benchmark:mainBenchmark                         │
│ - ./gradlew :awake:ui:benchmark:mainBenchmark                          │
├────────────────────────────────────────────────────────────────────────┤
│ Tier 3: Nightly & Release CI Gate                                      │
│ - Full benchmark suite executed on isolated CI runners                 │
│ - Automated regression alerts if throughput drops >5%                  │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 4. Engineer Responsibilities

- **When modifying hot paths** in `:awake:ecs`, `:awake:core:math`, or `:awake:ui:ui-core`:
  1. Run the respective benchmark suite before and after your change.
  2. Record the new numbers and ensure no regression occurred.
  3. Update this matrix and [docs/ecs-benchmark-scorecard.md](../ecs-benchmark-scorecard.md) in the same pull request.
