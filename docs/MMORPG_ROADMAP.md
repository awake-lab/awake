# MMORPG Engine Roadmap

Companion to [MVP_PLAN.md](./MVP_PLAN.md). That document covers the near-term "spinning
cube" MVP (Phases 0–8) — a single-player ECS/Vulkan/WebGPU rendering demo. **This document
is the long-horizon architecture blueprint toward the actual end goal: a working MMORPG.**

Nothing here is scheduled against a timeline yet. It exists to:
1. Give every future feature a home in the eventual architecture, so nothing gets bolted on
   ad hoc later.
2. Sequence an enormous scope into shippable milestones instead of one undifferentiated
   backlog — an MMORPG can't be built as a single MVP, so this splits it into stages.

## Sequencing rationale — why single-player comes before networking

**Networking synchronizes a game that already works — it isn't the first thing to build.**
The first pass at this roadmap front-loaded rendering-architecture work (render graph,
GPU skinning) into "MVP1" and then reasoned "the fastest path is to skip straight to a
networked-cube spike" — that was backwards, caught during review, and corrected below.

Reasons single-player gameplay has to come first:
- **You can't validate what's fun over a network you don't have yet.** Movement feel,
  camera behavior, and AI need iteration loops measured in seconds (change code, hot
  reload, feel it) — multiplayer testing is inherently slower (two clients, a server, more
  moving parts to blame when something feels off).
- **Networking is expensive to redo.** Once client-side prediction and state sync exist,
  every gameplay change has to be threaded through "what state now needs to sync, and how
  does prediction reconcile it." Doing that against gameplay systems that are still
  actively changing multiplies the cost of every iteration. Prove the gameplay shape
  first, then network the settled shape.
- **This project's actual proven risk was rendering plumbing (KMP + Vulkan + WebGPU),
  not networking.** That risk is retired — Phases 0–2.5 and this session's web demo prove
  the multiplatform rendering stack works end to end. The next unproven thing is "is
  there a game here," which is a single-player question.

So the ladder below puts a **fast, rendering-cheap single-player prototype first**
(MVP1a), defers the heavier rendering-architecture investment to a **follow-up polish
pass** (MVP1b) that only needs to happen once the gameplay loop is worth polishing, and
only *then* starts networking (MVP2).

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
| **MVP1a — Playable Prototype** | One controllable character moving around one hand-built world with a simple NavMesh-driven AI to chase/avoid, using the *existing* renderer as-is (no skinning, no render-graph rewrite) | 🚧 In progress — kinematic movement + third-person follow camera done (2026-07-11); NavMesh-driven chase AI done for desktop+Android (2026-07-11, `recast4j`); visible ground plane done (2026-07-11, Vulkan+WebGPU); iOS/wasmJs NavMesh backend and dodge/avoid behavior still open |
| **MVP1b — Vertical Slice Polish** | Upgrade the renderer to support what a shippable slice actually needs: render graph, dynamic rendering, real animated (skinned) characters | 🔲 Not started — only worth doing once MVP1a proves the gameplay loop |
| **MVP2 — Networked Prototype** | 2–10 players in the same world: client-server split, reliable transport, prediction/reconciliation — *layered on top of the now-proven single-player loop* | 🔲 Not started |
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
| In-Engine ImGui Catalog & Profiler Overlay | 🔲 Not started | P1 | MVP1a | — |
| Hot-Reloading SPIR-V Shader Compilation Toolchain | 🔲 Not started | P2 | MVP5 | 🆕 |
| Compute-Shader Heightmap Deformation Brushes | 🔲 Not started | P2 | MVP5 | 🆕 |
| Procedural L-System Tree Generation Toolchain | 🔲 Not started | P2 | MVP5 | 🆕 |

### Declarative User Interface

| Item | Status | Priority | Stage | Approach |
|---|---|---|---|---|
| Retained Layer Compositor Node Trees | 🔲 Not started | P2 | MVP4 | 🆕 (vs. immediate-mode) |
| State Tree UI Pipeline Mapping | 🔲 Not started | P2 | MVP4 | — |

> **Cheaper than it looks for MVP1a**: `awake-demo:shared` already renders a real Compose
> Multiplatform UI overlay (`App.kt`, `DemoDrawer.kt`) on 4 of 5 targets today. A basic
> HUD/menu for the playable prototype can reuse that directly — a custom retained-node UI
> engine is a later, more specialized need (in-world 3D UI, controller navigation), not a
> blocker for a first playable build. **Confirmed (2026-07-11)**: a debug player-position
> readout was added this way — a second `Text` in `App.kt`'s existing FPS overlay `Box`,
> polling a new `DebugHud` singleton (mirrors `Time`'s existing polled-singleton shape).
> wasmJs has no Compose UI at all by design, so it's skipped there for now.

> **First real per-frame `Camera` writer (2026-07-11)**: every scene previously authored
> `Camera.eye`/`center` once in `scenes/mvp.scene.json` and never touched them again. The
> new `CameraFollowSystem` (`awake-scene/.../systems/`) is the first system to mutate them
> at runtime — a fixed third-person offset tracking the player's `Transform.position`, no
> collision/occlusion handling yet (matches `PlayerMovementSystem`'s "deliberately simple"
> scope for this slice).

### Gameplay Mechanics & AI Systems

| Item | Status | Priority | Stage | Approach |
|---|---|---|---|---|
| Recast/Detour NavMesh Generation & Dynamic Obstacle Avoidance | 🚧 Partial (2026-07-11) | P0 | MVP1a | `recast4j` (pure-Java Recast/Detour port, no JNI) generates a real navmesh and finds paths that route around a solid obstacle -- desktop + Android only (recast4j is JVM-only); iOS/wasmJs get `null` (deferred, no pre-built C wrapper like Jolt's `JoltC` exists for Recast yet). Dynamic obstacle avoidance (re-baking around moving obstacles) not done -- this slice repaths periodically toward a moving target, but the navmesh geometry itself is still static. |
| ECS-Coupled Behavior Trees | 🚧 Partial (2026-07-11) | P0 | MVP1a | First real behavior via `ChaseAiSystem` (`awake-scene/.../systems/`) -- one fixed chase behavior (periodic repath + kinematic waypoint steering), not a general branching behavior-tree framework. Proves the ECS-coupled-AI pattern end-to-end; multiple/composable behaviors are a follow-up. |
| Hierarchical Pathfinding / HPA* | 🔲 Not started | P1 | MVP4 | 🆕 (only pays off at world scale) |
| Utility AI System Data Blocks | 🔲 Not started | P2 | MVP4 | 🆕 (vs. plain behavior trees) |

### Persistent Data & Economics

| Item | Status | Priority | Stage | Approach |
|---|---|---|---|---|
| Data-Driven Item & Ability Master Database | 🔲 Not started | P2 | MVP1b | — |
| Stateful Entity Inventory CRUD Processing | 🔲 Not started | P0 | MVP3 | — |

---

## 🔍 Level 2 — Simulation & Environment Pipelines

### Physics Simulation

| Item | Status | Priority | Stage | Approach |
|---|---|---|---|---|
| Kinematic Character Movement (no physics engine) | ✅ Done (2026-07-11) | P0 | MVP1a | 🕰️ deliberately simple — `Transform` moved directly from input via `PlayerMovementSystem` (`awake-scene`); WASD/arrows on desktop+wasmJs, touch-drag on Android+iOS (both already fed `Input.pointerDown/X/Y` from existing touch handlers, no new platform code needed). Not yet NavMesh-clamped — that's the next MVP1a item. |
| Jolt Physics JNI/cinterop Bindings | 🔲 Not started | P1 | MVP1b | 🆕 — see [D5](./MVP_PLAN.md#d5--physics-engine): Jolt over Bullet/PhysX/Rapier, coarse-grained `step(dt)` + batched transform read-back, not a 1:1 API mirror |
| Dynamic Rigid Bodies (ragdolls, projectiles, knockback) | 🔲 Not started | P2 | MVP4 | 🆕 — only once gameplay needs real dynamics, not before |
| 2D Physics (kbox2d, pure Kotlin) | 🔲 Not started | P3 | — | 🆕 — only if a 2D minigame/UI-physics need ever comes up; covers Wasm/JS for free since it's pure Kotlin |

> **Don't build a custom physics engine.** Unlike the Vulkan/WebGPU rendering work in this
> repo (which is custom *because* nothing else gives KMP a real backend-neutral renderer),
> there's no multiplatform-necessity argument for rolling your own rigid-body solver —
> Jolt already solves the hard part (broad/narrow-phase collision, constraint solving,
> continuous collision detection); the only real engineering work left is the JNI/cinterop
> binding layer, using the same pattern already proven for Vulkan.

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
| Asynchronous Audio Context Stepper | 🔲 Not started | P2 | MVP1b | — |
| HRTF (Head-Related Transfer Function) Spatial Audio | 🔲 Not started | P2 | MVP5 | 🆕 (vs. simple stereo pan) |

---

## ⚖️ Level 3 — Validation & Network Synchronization

**Nothing in this whole level starts before MVP2** — it's all downstream of "single-player
gameplay is proven" (see Sequencing rationale above).

### Security, Validation & Anti-Cheat

| Item | Status | Priority | Stage | Approach |
|---|---|---|---|---|
| Server-Authoritative Simulation Gateways | 🔲 Not started | P0 | MVP2 | — |
| Headless Server Collision-Mesh Verification | 🔲 Not started | P1 | MVP3 | — |
| Cryptographic Packet Handshake & Obfuscation Layer | 🔲 Not started | P1 | MVP3 | — |

### Network Infrastructure

| Item | Status | Priority | Stage | Approach |
|---|---|---|---|---|
| Reliable Message Transport (Ktor WebSocket) | 🔲 Not started | P0 | MVP2 | 🆕 for this project — see note below |
| Client-Side Prediction & Reconciliation | 🔲 Not started | P0 | MVP2 | — |
| State Synchronization Pipelines | 🔲 Not started | P0 | MVP2 | — |
| Client-Side Entity Interpolation / Dead-Reckoning | 🔲 Not started | P0 | MVP2 | — |
| Lag Compensation & Server Interpolation | 🔲 Not started | P1 | MVP2 | — |
| Reliable UDP (ENet, KCP) for native clients | 🔲 Not started | P2 | MVP4 | 🆕 (optimization once WebSocket proves the loop) |
| Spatial Partitioning (Quadtrees / Octrees) | 🔲 Not started | P0 | MVP4 | — |
| Interest Management / Net-Culling | 🔲 Not started | P0 | MVP4 | — |
| Delta-Compressed Network State Snapshot-Baking | 🔲 Not started | P1 | MVP4 | 🆕 (vs. full-state snapshots) |
| Distributed Proxy/Gateway Router Load Balancing | 🔲 Not started | P1 | MVP4 | — |
| Cross-Node Cluster RPC Communication | 🔲 Not started | P1 | MVP4 | — |
| Database Transaction Isolation & Anti-Duping Locks | 🔲 Not started | P0 | MVP3 | — |

> **Transport correction from the first draft of this roadmap**: raw UDP (ENet/KCP) was
> originally tagged as the "modern" choice. That's true for a typical native-only MMORPG,
> but this project already has a **working browser client** (this session's WebGPU demo),
> and browsers cannot open raw UDP sockets at all. The uniform choice across all 4 targets
> (desktop/Android/iOS/wasmJs) is **WebSocket** (Ktor has real KMP support for it,
> including JS/Wasm) — real UDP becomes a later *optimization* for native clients only,
> not the starting point.

### Automated Testing Systems

| Item | Status | Priority | Stage | Approach |
|---|---|---|---|---|
| Decoupled Pure-Kotlin Simulation Testing | 🚧 Partial | P1 | MVP1a | — |
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
| Render / Frame Graph Architecture | 🔲 Not started | P0 | MVP1b | 🆕 — adopt once real render-pass variety shows up |
| Vulkan Dynamic Rendering (Bypassing Legacy VkRenderPass) | 🔲 Not started | P1 | MVP1b | 🆕 (engine's `RenderPipeline` uses classic `VkRenderPass`/framebuffers today — 🕰️) |
| Spirv-Reflect Automated Pipeline Layout Compilation | 🔲 Not started | P1 | MVP1b | 🆕 (vs. today's hand-written pipeline layouts) |
| GPU Skinning Matrix Palettes | 🔲 Not started | P0 | MVP1b | 🆕 (vs. CPU skinning; MVP1a uses static/rigid meshes instead) |
| Shared UI Vertex Buffer Invalidating | 🔲 Not started | P2 | MVP1b | — |
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
| Headless Vulkan Driver Verification | 🚧 Partial | P1 | MVP1a | — |
| Surfaceless Offscreen Frame Buffering | 🔲 Not started | P2 | MVP1b | — |
| Pixel-Hash Automated Snapshot Testing | 🔲 Not started | P2 | MVP1b | — |

> **Partial today**: `awake-backend-vulkan`'s `desktopTest` already does real headless
> Vulkan instance/device creation against `lavapipe` in CI (`VulkanDesktopNativeSmokeTest`)
> — the missing piece is surfaceless offscreen rendering + pixel-hash comparison on top of
> that foundation, not the headless driver bring-up itself.

### Multi-Threaded Engine Scheduling

| Item | Status | Priority | Stage | Approach |
|---|---|---|---|---|
| Vulkan Double/Triple Buffering Command Pools | 🚧 Partial | P2 | MVP1b | 🆕 (double-buffered today via `MAX_FRAMES_IN_FLIGHT = 2`; triple not yet) |
| Multi-Threaded Command Buffer Recording | 🔲 Not started | P1 | MVP4 | — |
| Fiber-Based Job System / N:M Coroutine Threading | 🔲 Not started | P1 | MVP4 | 🆕 (vs. today's single-threaded fixed-timestep loop — 🕰️) |
| Vulkan Timeline Semaphores | 🔲 Not started | P2 | MVP4 | 🆕 (vs. today's binary semaphores + fences — 🕰️) |
| Naughty Dog-Style Frame Pipelining (3-Frames-In-Flight) | 🔲 Not started | P2 | MVP4 | 🆕 |

### Multiplatform Memory Management

| Item | Status | Priority | Stage | Approach |
|---|---|---|---|---|
| KMP Native C-Interop Memory Allocation | ✅ Done | — | MVP0 | JNI (Android/Desktop) + cinterop (iOS), already shipping |
| Asynchronous Asset Streaming (Mesh/Texture Chunks) | 🔲 Not started | P1 | MVP4 | 🆕 (vs. today's whole-file `suspend fetch()` — 🕰️, fine through MVP1) |
| Vulkan Bindless Rendering Descriptor Indexing | 🔲 Not started | P1 | MVP4 | 🆕 (vs. today's per-object descriptor sets — 🕰️) |
| Memory-Mapped Vulkan Staging Buffers | 🔲 Not started | P2 | MVP4 | 🆕 (vs. today's copy-then-destroy staging buffer per upload — 🕰️) |
| Garbage-Collection-Free Game Loop (Zero-Alloc) | 🔲 Not started | P2 | MVP4 | 🆕 |

---

## Notes for future sessions

- This roadmap is deliberately not broken into `- [ ]` checkboxes like `MVP_PLAN.md`'s
  phases — the scope here is too large and too likely to be re-sequenced as MVP1a actually
  starts. Re-derive current status from the codebase, don't trust this doc's tags blindly
  once real work starts landing against it (see the top-level `CLAUDE.md` guidance on
  verifying before recommending from memory).
- The 🕰️→🆕 pairs called out above (`VkRenderPass`→dynamic rendering, binary
  semaphores→timeline semaphores, per-object descriptors→bindless, copy-staging→
  memory-mapped staging, single-thread loop→fiber job system) are real migrations off
  choices this engine already made in `awake-backend-vulkan`, not greenfield decisions —
  each one touches working code, not just new code.
- **MVP1a is the fastest real next milestone**: it needs almost no new rendering work —
  reuse the existing `VulkanApplication`/`WebGpuApplication` renderer as-is with
  static/rigid placeholder meshes, and reuse the existing Compose UI overlay for a basic
  HUD. The actual new work is gameplay-layer only: input → movement, camera follow,
  NavMesh + a simple behavior tree for one chasing/fleeing AI. MVP1b (the
  render-architecture upgrade) and MVP2 (networking) both come after, once there's a
  gameplay loop worth polishing and synchronizing.
