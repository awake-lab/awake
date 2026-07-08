# AGENTS.md — Awake

This project uses [kmm-agent-skills](https://github.com/ronjunevaldoz/kmm-agent-skills).
Skills are installed in `.claude/skills/`.

## Project overview

Awake is a Kotlin Multiplatform **game engine library**: a cross-platform graphics wrapper
(OpenGL working; Vulkan in progress) evolving toward a full KMP game engine with ECS,
a Compose-style scene API, and a desktop editor. Native Vulkan access is via JNI (Android /
Desktop JVM) and MoltenVK cinterop (iOS, planned). C++ JNI bindings are generated with
[jni-binding-generator](https://github.com/ronjunevaldoz/jni-binding-generator).

Group ID: `io.github.ronjunevaldoz` · Artifacts: `awake-core`, `awake-vulkan`,
`awake-ecs`, `awake-scene` ·
Published to: Maven Central (Sonatype snapshots)

**The roadmap and task checklist live in [docs/MVP_PLAN.md](../docs/MVP_PLAN.md) — consult
it before starting work; phases and open decisions (D1–D4) are tracked there.**

## Skill routing

| Topic | Skill |
|---|---|
| Publishing to Maven Central | `kotlin-multiplatform-library-publishing` |
| iOS / SPM distribution, MoltenVK framework | `kotlin-multiplatform-xcframework-spm` |
| JNI bridge / C++ marshalling / memory safety | `kotlin-multiplatform-jni-pro` |
| Platform-specific implementations (`expect/actual`) | `kotlin-multiplatform-expect-actual` |
| Toolchain upgrade / KMP migration | `kotlin-multiplatform-migration` |
| Unit / integration tests | `kotlin-multiplatform-unit-testing` |
| Code quality (Detekt, Ktlint) | `kotlin-multiplatform-code-quality` |
| CI automation | `kotlin-multiplatform-ci-github-actions` |
| Desktop windowing / packaging (editor, GLFW host) | `kotlin-multiplatform-desktop-app` |
| Custom drawing / graphics layers (editor viewport) | `kotlin-multiplatform-graphics-modifiers` |
| Release / versioning / changelog | `kotlin-multiplatform-release` |
| Architecture audit | `kotlin-multiplatform-audit` |
| Capture lessons learned | `kotlin-multiplatform-lessons` |
| Harvest consumer lessons | `/kmm-harvest-lessons` |

## Module graph

| Module | Purpose | Published |
|---|---|---|
| `:awake-core` | Engine core: math, shaders, game loop, OpenGL wrapper | ✅ `awake-core` |
| `:awake-ecs` | Pure sparse-set ECS runtime: entities, component stores, world queries, systems | ✅ `awake-ecs` |
| `:awake-scene` | Awake scene components/systems on top of ECS (`Transform`, `Camera`, render integration) | ✅ `awake-scene` |
| `:awake-vulkan` | Vulkan KMP bindings — common API + JNI C++ (`src/main/cpp`) | ✅ `awake-vulkan` |
| `:awake-vulkan-generator` | Legacy bespoke C++ codegen — **being retired** (MVP Phase 1a) in favor of jni-binding-generator | ❌ |
| `:awake-demo:shared` | Sample app shared code (Vulkan triangle demo) | ❌ sample |
| `:awake-demo:androidApp` | Android sample entry point | ❌ sample |
| `:awake-demo:desktopApp` | Desktop sample entry point | ❌ sample |

## Project-specific rules

- **Regression gate:** the Android Vulkan triangle demo (`awake-demo`) must keep rendering
  after every change to `awake-vulkan` — it is the only working Vulkan backend today.
- The common Vulkan API in `awake-vulkan/src/commonMain` is the single source of truth;
  platform backends (`androidMain` JNI, `desktopMain` JNI, `iosMain` cinterop) implement it.
- Do not hand-edit generated C++ Accessor/Mutator files; regenerate via the binding generator.
- Engine modules do NOT follow the app 6-layer clean architecture
  (`:model/:api/:domain/:data/:presenter/:ui`) — that pattern is for app features, not engine
  subsystems. Do not restructure engine modules to match it.
- `.spv` shaders are compiled from GLSL via the `glslValidator` Gradle task (glslang).

## API surface rules

- Never remove or rename public symbols without a major version bump
- Public API changes to published modules (`awake-core`, `awake-vulkan`, `awake-ecs`,
  `awake-scene`) require a CHANGELOG.md entry
- Mark internal symbols `internal` — keep the published API surface minimal

## Commands installed

See `.claude/commands/kmm-*.md`. Key commands:
- `/kmm-run-audit` — architecture audit with per-finding remediation
- `/kmm-harvest-lessons` — collect patterns to upstream to skills
- `/kmm-verify` — full validation pipeline (build + test)
- `/kmm-review-changes` — review git diff against architecture rules
- `/kmm-check-updates` — check for skill updates
