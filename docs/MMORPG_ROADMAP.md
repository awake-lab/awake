# MMORPG Engine Roadmap

Companion to [MVP_PLAN.md](./MVP_PLAN.md). That document covers the near-term "spinning
cube" MVP (Phases 0–8) — a single-player ECS/Vulkan/WebGPU rendering demo. **This document
is the long-horizon architecture blueprint toward the actual end goal: a working MMORPG.**

Nothing here is scheduled against a timeline yet. It exists to:
1. Give every future feature a home in the eventual architecture, so nothing gets bolted on
   ad hoc later.
2. Sequence an enormous scope into shippable milestones instead of one undifferentiated
   backlog — an MMORPG can't be built as a single MVP, so this splits it into six.

## How to read this

**Status**
- ✅ Done — exists and is verified in the codebase today
- 🚧 Partial — real groundwork exists, but the item itself isn't built
- 🔲 Not started

**Priority** (relative to its own MVP stage, not the whole roadmap)
- **P0** — blocks that stage from being called done
- **P1** — core to the stage's purpose, but the stage can ship without it if squeezed
- **P2** — polish/scale, defer to a later stage under time pressure
- **P3** — nice-to-have, cut first

**Approach** (only tagged where there's a real modern-vs-legacy engineering choice)
- 🆕 **Modern** — GPU-driven / bindless / compute-shader-based
- 🕰️ **Legacy** — CPU-bound / fixed-function / traditional; sometimes what this engine
  already does today and would need to migrate off, sometimes a deliberately simpler
  stepping stone to build first

---

## MVP Ladder

| Stage | Goal | Status |
|---|---|---|
| **MVP0 — Engine Foundation** | Single-player render loop: ECS, Vulkan + WebGPU rendering, fixed-timestep loop, `scene.json` loading | ✅ Done — this is `MVP_PLAN.md`'s entire scope |
| **MVP1 — Single-Player Vertical Slice** | One playable animated character in one hand-built world: movement, camera, simple AI, minimal UI, a render-graph-based renderer with real skinning | 🔲 Not started |
| **MVP2 — Networked Prototype** | 2–10 players in the same world: client-server split, reliable transport, prediction/reconciliation | 🔲 Not started |
| **MVP3 — Persistent Shard** | One authoritative server, database-backed state, basic anti-cheat, automated regression testing | 🔲 Not started |
| **MVP4 — Scalable World** | Hundreds of concurrent players: spatial partitioning at scale, clustering, environmental simulation, crowd rendering | 🔲 Not started |
| **MVP5 — Production Polish** | Full creator tools, advanced audio, streaming/memory optimization, anti-cheat hardening | 🔲 Not started |

Each stage below is broken into the same four system categories the original blueprint
used, so the categorical view (what kind of system is this) and the sequencing view
(when do we build it) both stay readable.

---

## 🎛️ Level 1 — Creator & Gameplay Layers

### World Editor & Tools

| Item | Status | Priority | Stage | Approach |
|---|---|---|---|---|
| In-Engine ImGui Catalog & Profiler Overlay | 🔲 Not started | P1 | MVP1 | — |
| Hot-Reloading SPIR-V Shader Compilation Toolchain | 🔲 Not started | P2 | MVP5 | 🆕 |
| Compute-Shader Heightmap Deformation Brushes | 🔲 Not started | P2 | MVP5 | 🆕 |
| Procedural L-System Tree Generation Toolchain | 🔲 Not started | P2 | MVP5 | 🆕 |

### Declarative User Interface

| Item | Status | Priority | Stage | Approach |
|---|---|---|---|---|
| Retained Layer Compositor Node Trees | 🔲 Not started | P1 | MVP1 | 🆕 (vs. immediate-mode) |
| State Tree UI Pipeline Mapping | 🔲 Not started | P1 | MVP1 | — |

### Gameplay Mechanics & AI Systems

| Item | Status | Priority | Stage | Approach |
|---|---|---|---|---|
| Recast/Detour NavMesh Generation & Dynamic Obstacle Avoidance | 🔲 Not started | P0 | MVP1 | — |
| ECS-Coupled Behavior Trees | 🔲 Not started | P0 | MVP1 | — |
| Hierarchical Pathfinding / HPA* | 🔲 Not started | P1 | MVP4 | 🆕 (only pays off at world scale) |
| Utility AI System Data Blocks | 🔲 Not started | P2 | MVP4 | 🆕 (vs. plain behavior trees) |

### Persistent Data & Economics

| Item | Status | Priority | Stage | Approach |
|---|---|---|---|---|
| Data-Driven Item & Ability Master Database | 🔲 Not started | P1 | MVP1 | — |
| Stateful Entity Inventory CRUD Processing | 🔲 Not started | P0 | MVP3 | — |

---

## 🔍 Level 2 — Simulation & Environment Pipelines

### World Space & Serialization

| Item | Status | Priority | Stage | Approach |
|---|---|---|---|---|
| Double-Precision (64-bit) Coordinate Space | 🔲 Not started | P0 | MVP4 | 🆕 (pick one of these two) |
| Continuous World Origin Shifting / Floating Origin | 🔲 Not started | — | MVP4 | 🕰️ (alternative to 64-bit coords, not both) |
| Incremental Asset Patching & Live Manifest Appending | 🔲 Not started | P1 | MVP5 | — |
| Virtual File System (VFS) Pak/Oodle Compression Mounting | 🔲 Not started | P2 | MVP5 | — |

### Environmental Simulation

| Item | Status | Priority | Stage | Approach |
|---|---|---|---|---|
| Dynamic Skybox Day/Night Cycle (Sun & Moon Ephemeris) | 🔲 Not started | P1 | MVP4 | — |
| Gerstner Wave Dynamic Water Mesh Grids | 🔲 Not started | P2 | MVP4 | 🆕 (vs. static water plane) |
| Global Volumetric Wind & Global Uniform Sway Vectors | 🔲 Not started | P2 | MVP4 | — |
| GPU Compute Particle Systems (Rain, Snow, Weather Cells) | 🔲 Not started | P2 | MVP4 | 🆕 (vs. CPU particles) |

### Audio Pipeline

| Item | Status | Priority | Stage | Approach |
|---|---|---|---|---|
| Asynchronous Audio Context Stepper | 🔲 Not started | P1 | MVP1 | — |
| HRTF (Head-Related Transfer Function) Spatial Audio | 🔲 Not started | P2 | MVP5 | 🆕 (vs. simple stereo pan) |

---

## ⚖️ Level 3 — Validation & Network Synchronization

### Security, Validation & Anti-Cheat

| Item | Status | Priority | Stage | Approach |
|---|---|---|---|---|
| Server-Authoritative Simulation Gateways | 🔲 Not started | P0 | MVP2 | — |
| Headless Server Collision-Mesh Verification | 🔲 Not started | P1 | MVP3 | — |
| Cryptographic Packet Handshake & Obfuscation Layer | 🔲 Not started | P1 | MVP3 | — |

### Network Infrastructure

| Item | Status | Priority | Stage | Approach |
|---|---|---|---|---|
| Reliable UDP / UDP Protocol (ENet, KCP) | 🔲 Not started | P0 | MVP2 | 🆕 (vs. raw TCP's head-of-line blocking) |
| Client-Side Prediction & Reconciliation | 🔲 Not started | P0 | MVP2 | — |
| State Synchronization Pipelines | 🔲 Not started | P0 | MVP2 | — |
| Client-Side Entity Interpolation / Dead-Reckoning | 🔲 Not started | P0 | MVP2 | — |
| Lag Compensation & Server Interpolation | 🔲 Not started | P1 | MVP2 | — |
| Spatial Partitioning (Quadtrees / Octrees) | 🔲 Not started | P0 | MVP4 | — |
| Interest Management / Net-Culling | 🔲 Not started | P0 | MVP4 | — |
| Delta-Compressed Network State Snapshot-Baking | 🔲 Not started | P1 | MVP4 | 🆕 (vs. full-state snapshots) |
| Distributed Proxy/Gateway Router Load Balancing | 🔲 Not started | P1 | MVP4 | — |
| Cross-Node Cluster RPC Communication | 🔲 Not started | P1 | MVP4 | — |
| Database Transaction Isolation & Anti-Duping Locks | 🔲 Not started | P0 | MVP3 | — |

### Automated Testing Systems

| Item | Status | Priority | Stage | Approach |
|---|---|---|---|---|
| Decoupled Pure-Kotlin Simulation Testing | 🚧 Partial | P1 | MVP1 | — |
| Deterministic Input Macro Playback Testing | 🔲 Not started | P2 | MVP3 | — |
| UI Tree Snapshot Hierarchy Validation | 🔲 Not started | P2 | MVP5 | — |

> **Partial today**: `awake-ecs`/`awake-scene` already have real unit-test suites
> (`SceneLoaderTest`, ECS family/query tests, the lavapipe Vulkan smoke test in
> `awake-backend-vulkan:desktopTest`) — but none of it is simulation-level (running the
> actual fixed-timestep loop headlessly across many ticks and asserting on final state),
> which is what this item means for an MMORPG's server-authoritative simulation.

---

## ⚙️ Level 4 — Hardware & Execution Core

### Graphics Backend & Crowd Rendering

| Item | Status | Priority | Stage | Approach |
|---|---|---|---|---|
| Render / Frame Graph Architecture | 🔲 Not started | P0 | MVP1 | 🆕 — adopt before more rendering features stack on top |
| Vulkan Dynamic Rendering (Bypassing Legacy VkRenderPass) | 🔲 Not started | P1 | MVP1 | 🆕 (engine's `RenderPipeline` uses classic `VkRenderPass`/framebuffers today — 🕰️) |
| Spirv-Reflect Automated Pipeline Layout Compilation | 🔲 Not started | P1 | MVP1 | 🆕 (vs. today's hand-written pipeline layouts) |
| GPU Skinning Matrix Palettes | 🔲 Not started | P0 | MVP1 | 🆕 (vs. CPU skinning) |
| Shared UI Vertex Buffer Invalidating | 🔲 Not started | P2 | MVP1 | — |
| Instance-Indexed Animation Offsets via SSBO | 🔲 Not started | P1 | MVP4 | 🆕 — needed once many characters render at once |
| Multi-LOD Skinned Mesh Switching | 🔲 Not started | P2 | MVP4 | — |
| Index Buffer LOD Swapping | 🔲 Not started | P2 | MVP4 | 🕰️ (superseded by meshlet culling below) |
| Edge Collapse Quadric Error Metrics (QEM) | 🔲 Not started | P2 | MVP4 | — |
| Animation Texture Baking / Vertex Animation Textures (VAT) | 🔲 Not started | P2 | MVP4 | 🆕 |
| Compute Shader Skinning Pipelines | 🔲 Not started | P2 | MVP4 | 🆕 |
| GPU-Driven Occlusion Culling via Two-Pass Depth Pyramids | 🔲 Not started | P1 | MVP4 | 🆕 (engine has no culling at all today — 🕰️ baseline) |
| GPU-Driven Meshlet Culling (Vulkan Mesh Shaders) | 🔲 Not started | P2 | MVP4 | 🆕 |
| Virtual Texturing / Megatexture Terrain Splatting | 🔲 Not started | P2 | MVP5 | 🆕 |
| Anamorphic Compute Shader Lens Flares & Bloom | 🔲 Not started | P3 | MVP5 | 🆕 |

### Low-Level Graphics Verification

| Item | Status | Priority | Stage | Approach |
|---|---|---|---|---|
| Headless Vulkan Driver Verification | 🚧 Partial | P1 | MVP1 | — |
| Surfaceless Offscreen Frame Buffering | 🔲 Not started | P2 | MVP1 | — |
| Pixel-Hash Automated Snapshot Testing | 🔲 Not started | P2 | MVP1 | — |

> **Partial today**: `awake-backend-vulkan`'s `desktopTest` already does real headless
> Vulkan instance/device creation against `lavapipe` in CI (`VulkanDesktopNativeSmokeTest`)
> — the missing piece is surfaceless offscreen rendering + pixel-hash comparison on top of
> that foundation, not the headless driver bring-up itself.

### Multi-Threaded Engine Scheduling

| Item | Status | Priority | Stage | Approach |
|---|---|---|---|---|
| Vulkan Double/Triple Buffering Command Pools | 🚧 Partial | P1 | MVP1 | 🆕 (double-buffered today via `MAX_FRAMES_IN_FLIGHT = 2`; triple not yet) |
| Multi-Threaded Command Buffer Recording | 🔲 Not started | P1 | MVP4 | — |
| Fiber-Based Job System / N:M Coroutine Threading | 🔲 Not started | P1 | MVP4 | 🆕 (vs. today's single-threaded fixed-timestep loop — 🕰️) |
| Vulkan Timeline Semaphores | 🔲 Not started | P2 | MVP4 | 🆕 (vs. today's binary semaphores + fences — 🕰️) |
| Naughty Dog-Style Frame Pipelining (3-Frames-In-Flight) | 🔲 Not started | P2 | MVP4 | 🆕 |

### Multiplatform Memory Management

| Item | Status | Priority | Stage | Approach |
|---|---|---|---|---|
| KMP Native C-Interop Memory Allocation | ✅ Done | — | MVP0 | JNI (Android/Desktop) + cinterop (iOS), already shipping |
| Asynchronous Asset Streaming (Mesh/Texture Chunks) | 🔲 Not started | P1 | MVP4 | 🆕 (vs. today's whole-file `suspend fetch()` — 🕰️, fine for MVP0/1) |
| Vulkan Bindless Rendering Descriptor Indexing | 🔲 Not started | P1 | MVP4 | 🆕 (vs. today's per-object descriptor sets — 🕰️) |
| Memory-Mapped Vulkan Staging Buffers | 🔲 Not started | P2 | MVP4 | 🆕 (vs. today's copy-then-destroy staging buffer per upload — 🕰️) |
| Garbage-Collection-Free Game Loop (Zero-Alloc) | 🔲 Not started | P2 | MVP4 | 🆕 |

---

## Notes for future sessions

- This roadmap is deliberately not broken into `- [ ]` checkboxes like `MVP_PLAN.md`'s
  phases — the scope here is too large and too likely to be re-sequenced as MVP1 actually
  starts. Re-derive current status from the codebase, don't trust this doc's tags blindly
  once real work starts landing against it (see the top-level `CLAUDE.md` guidance on
  verifying before recommending from memory).
- The 🕰️→🆕 pairs called out above (`VkRenderPass`→dynamic rendering, binary
  semaphores→timeline semaphores, per-object descriptors→bindless, copy-staging→
  memory-mapped staging, single-thread loop→fiber job system) are real migrations off
  choices this engine already made in `awake-backend-vulkan`, not greenfield decisions —
  each one touches working code, not just new code.
- MVP1's P0/P1 items (render graph, dynamic rendering, GPU skinning, NavMesh, behavior
  trees) are intentionally front-loaded with rendering-architecture work before gameplay
  content, since the vertical slice's animated character rendering depends on them —
  building gameplay on top of the current fixed VkRenderPass/no-skinning pipeline would
  mean redoing it once MVP1 needs real characters anyway.
