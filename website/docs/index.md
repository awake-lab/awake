# Awake Engine

![Awake Banner](https://github.com/awake-lab/awake/actions/workflows/build-and-publish.yml/badge.svg)

Awake is a Kotlin Multiplatform game engine (Vulkan, WebGPU, OpenGL) with a shared ECS runtime, targeting Android, iOS, Desktop (macOS/Windows/Linux), and the Web (Wasm/WebGPU) from one `commonMain` codebase.

## Features

- **Vulkan** — Android, Desktop (macOS/Windows/Linux), iOS (via MoltenVK)
- **WebGPU** — Web (Wasm), behind the same renderer abstraction as Vulkan
- **OpenGL** — Android, iOS, Desktop (frozen: bugfixes only, Vulkan is the active backend)
- **Shared ECS** (`awake:ecs`) + scene graph (`awake:scene`)
- **Type-safe UI** (`awake:engine:ui-dsl`) with a custom design system.

## Modules

- `awake:base` — Core foundational logic, math, and timing.
- `awake:ecs` — Sparse-set ECS runtime.
- `awake:scene` — Scene-graph components and systems.
- `awake:engine:game` — Backend-neutral game bootstrap.
- `awake:engine:ui:ui-core` — Stateless UI primitives and theme tokens.

[Getting Started](getting-started.md){ .md-button .md-button--primary }
[API Reference](api/index.html){ .md-button }
