# AGENTS.md — Awake

Awake is a Kotlin Multiplatform game engine library. Use this file as a startup index, not
as the long-form home for project policy.

## Read First

- [docs/architecture.md](/Users/ronvaldoz/StudioProjects/awaken/docs/architecture.md)
- [docs/reference/ai-collaboration.md](/Users/ronvaldoz/StudioProjects/awaken/docs/reference/ai-collaboration.md)
- [docs/reference/agent-catalog.md](/Users/ronvaldoz/StudioProjects/awaken/docs/reference/agent-catalog.md)
- [docs/reference/ui-ownership.md](/Users/ronvaldoz/StudioProjects/awaken/docs/reference/ui-ownership.md)
- [docs/reference/game-structure.md](/Users/ronvaldoz/StudioProjects/awaken/docs/reference/game-structure.md)
- [docs/MVP_PLAN.md](/Users/ronvaldoz/StudioProjects/awaken/docs/MVP_PLAN.md)
- [docs/tasks.md](/Users/ronvaldoz/StudioProjects/awaken/docs/tasks.md)

## Project Summary

- Group: `io.github.ronjunevaldoz`
- Published artifacts: `awake-base`, `awake-core`, `awake-opengl`, `awake-ecs`,
  `awake-scene`, `awake-vulkan`
- Current direction: backend-neutral runtime, reusable scene/UI DSL, and editor-ready
  engine modules

## Critical Guardrails

- The Android Vulkan sample remains the regression gate for backend work.
- Do not hand-edit generated JNI Accessor/Mutator C++ files; regenerate them.
- Engine modules do not follow the app-style 6-layer clean-architecture split.
- Reusable UI boundaries are canonical in
  [docs/reference/ui-ownership.md](/Users/ronvaldoz/StudioProjects/awaken/docs/reference/ui-ownership.md).

## Skill Routing

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

## Repo-Local Skill Sources

- Canonical repo-local skill files live under `skills/awake/`.
- `.claude/agents` and `.claude/commands/awake` are symlinks into `skills/awake/`.
- Edit the tracked files under `skills/awake/`, not the symlinked `.claude/` paths.
