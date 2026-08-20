# Architecture Governance Standardization Plan

Date: 2026-08-20  
Status: Draft — no module or public-API rename is authorized by this document

## Objective

Make Awake's build graph, active architecture documentation, agent instructions, and generated
developer documentation describe the same module layout. The immediate goal is reliable routing
and verification; module extraction is deliberately a later, evidence-driven decision.

## Findings This Plan Addresses

| Finding | Evidence | Required outcome |
|---|---|---|
| Developer documentation cannot run | `./gradlew developerDocs --dry-run` fails because the task names `:awake:ui:ui-designsystem` and `:awake:ui:ui-headless`, while the included projects are `:awake:ui:designsystem` and `:awake:ui:headless`. | `developerDocs` resolves and dry-runs successfully. |
| Current docs disagree about the module graph | Several active reference pages still name removed pre-split paths such as `:awake:engine:game-authoring` and `:awake:engine:ui-dsl`; `docs/tasks.md` also says `:awake:core` no longer exists even though it is included. | One active module map, with historical paths kept only in archived decisions/audits. |
| Agent routing is stale | `.codex/agents/*.toml` still names pre-split modules and paths; the runtime-agent description still names removed application modules. | Every agent entrypoint routes to a current module and required domain skill. |
| Skill sync gives a false green result | `tools/verify_agent_skills_sync.py` reports success even when a deployed `.claude` domain skill differs from its tracked source. | Sync verification compares content and symlink destinations, not only inventory. |
| Facade/leaf consumption is inconsistent | `:awake:scene` is an umbrella facade, but some consumers also declare direct leaf dependencies; WebGPU depends on the scene facade. | A documented and enforced consumption rule. |

## Decisions To Make First

These are intentionally small decisions. Do not start a broad package or module rename before
they are recorded.

1. **Canonical current graph:** `settings.gradle.kts` is the executable source of truth for
   current Gradle project paths. `docs/architecture.md` is the human-readable source of truth
   for ownership and allowed dependency direction.
2. **Current-path vocabulary:** use the paths in the table below in active docs, agent files,
   task wiring, and validation scripts. Historical names belong in archived plans or a clearly
   marked migration-history section only.
3. **Consumption boundary:** published/sample consumers use a facade; implementation modules
   use precise leaves; a backend may depend only on renderer/platform contracts unless it is
   explicitly declared as a scene-aware adapter.
4. **No premature core split:** `:awake:core:math`, `:awake:core:input`, and
   `:awake:core:time` remain proposals until a separate decision records their dependency,
   publishing, and migration cost. This plan only removes contradictory claims.

## Current Naming Standard

### Gradle paths

Use colon nesting for a true ownership boundary and a concise leaf name for the capability:

```text
:awake:<domain>[:<subdomain>]:<capability>
```

Examples:

| Domain | Current canonical paths |
|---|---|
| Core | `:awake:core`, `:awake:core:geometry`, `:awake:core:animation` |
| Engine | `:awake:engine:platform`, `:awake:engine:bootstrap`, `:awake:engine:app`, `:awake:engine:render:contract`, `:awake:engine:render:passes` |
| Scene | `:awake:scene` (facade), `:awake:scene:scene-core`, `:awake:scene:rendering`, `:awake:scene:physics`, `:awake:scene:controls`, `:awake:scene:runtime`, `:awake:scene:authoring` |
| UI | `:awake:ui:ui-core`, `:awake:ui:headless`, `:awake:ui:designsystem`, `:awake:ui:graphics`, `:awake:ui:text`, `:awake:ui:animation`, `:awake:ui:testing` |
| Backends | `:awake:backend:vulkan`, `:awake:backend:vulkan:bindings`, `:awake:backend:vulkan:bindings:android-native`, `:awake:backend:webgpu`, `:awake:backend:jolt` |

Do not infer a new module path from package names. Add a path to `settings.gradle.kts` first,
then update the architecture map, module README, and registry in the same change.

### Folder and package rules

- The directory layout mirrors the Gradle path: `awake/engine/render/contract` maps to
  `:awake:engine:render:contract`.
- Kotlin packages express API ownership, not Gradle spelling. Preserve package compatibility
  unless a separately approved public API migration says otherwise.
- Keep `src/<sourceSet>/kotlin` as the only Kotlin source root. Intentional custom source sets
  (`appMain`, `vulkanMain`) must be declared in the module build script and documented in that
  module README.
- A module README is required for a public API, native boundary, code generator, or standalone
  tool. Benchmarks and tiny internal leaves need one only when their workflow is non-obvious.

## Phased Delivery

### Phase 0 — Restore truthful verification

1. Correct the `developerDocs` task paths and its stale report-path comments.
2. Add CI coverage for `./gradlew developerDocs --dry-run`.
3. Add a lightweight `verifyModuleReferences` task/script that reads the included project paths
   and fails on invalid project dependencies or task dependencies.

**Acceptance:** `./gradlew developerDocs --dry-run` succeeds; the new check runs in CI.

### Phase 1 — Publish one active module map

1. Add a compact **Current Modules** table to `docs/architecture.md`, generated or checked
   against `settings.gradle.kts`.
2. Update active files that still use retired paths, beginning with:
   `docs/reference/dsl-modules.md`, `docs/reference/developer-docs.md`,
   `docs/reference/tutorial-coverage.md`, `docs/reference/ui-component-coverage.md`, and
   `docs/tasks.md`.
3. Mark old names in decision/audit documents as historical; do not rewrite their contemporaneous
   evidence unless a dead link prevents reading it.
4. Resolve the conflicting core-split status by either superseding the proposal or changing its
   status to a future roadmap. It must not describe nonexistent present-day work as mandatory.

**Acceptance:** active documentation contains no retired module path except in an explicitly
labelled historical section; the architecture table matches `settings.gradle.kts`.

### Phase 2 — Standardize agent and skill routing

1. Treat `docs/reference/agent-catalog.md` as the roster and responsibility source of truth;
   `skills/awake/agents/*.md` are role playbooks; `.codex/agents/*.toml` are thin Codex-specific
   overlays.
2. Rewrite the Codex profiles to reference current modules and the canonical docs/skills rather
   than embedding pre-split implementation paths or roadmap assumptions.
3. Correct the runtime-agent description to list `platform`, `bootstrap`, and `app` only.
4. Narrow routing without multiplying personas:
   - core engineer: core/ECS/scene/asset contracts;
   - render-backend engineer: GPU backend and native bridge;
   - Jolt work is a named physics sub-lane requiring `awake-physics-jolt`;
   - architecture auditor owns module-policy and validation gates.
5. Upgrade `verify_agent_skills_sync.py` to compare canonical and deployed skill contents,
   confirm symlink destinations, validate frontmatter, and ensure referenced skills exist.

**Acceptance:** no active agent names a removed module/path; a changed deployed skill makes the
sync command fail.

### Phase 3 — Enforce facade and leaf boundaries

1. Document the following rule in `docs/architecture.md` and module READMEs:

   | Consumer | Allowed dependency shape |
   |---|---|
   | Sample, game, external consumer | facade only (`:awake:scene`, when using the scene suite) |
   | Engine implementation module | exact leaf modules only |
   | Renderer/backend | render/platform/asset contracts only; scene dependency requires an explicit scene-adapter justification |
   | Tests/benchmarks | direct leaves permitted when testing that leaf |

2. Remove redundant facade-plus-leaf declarations from samples where the facade already exports
   the required surface.
3. Investigate WebGPU's scene dependency. If it is only for a few shared data types, move those
   types behind a narrow contract; otherwise declare and document a scene-aware WebGPU adapter.
4. Add a dependency-graph check for prohibited backend-to-scene and sample-to-leaf edges, with
   a small checked-in allowlist for justified exceptions.

**Acceptance:** dependency rules run in CI and every exception carries its reason beside the
dependency declaration.

### Phase 4 — Normalize native ownership without a risky rewrite

1. Document `:awake:backend:vulkan:bindings:android-native` as an Android NDK build host while
   its CMake and generated JNI source remain siblings.
2. Decide whether to move native inputs into that module. Do so only after proving CMake,
   generation, Android packaging, and desktop/iOS bindings remain independent.
3. If source stays shared, rename documentation—not Kotlin packages—to make the exception
   explicit and add an ownership test that protects the approved cross-directory inputs.

**Acceptance:** the Android native boundary has one written ownership model and no accidental
cross-module source output.

## Non-Goals

- Renaming Kotlin packages or published Maven coordinates.
- Extracting core math/input/time merely to satisfy a folder taxonomy.
- Replacing the scene facade while it is still the intended public compatibility surface.
- Adding more agents before the existing agents are current and enforceable.

## Validation Matrix

```bash
./gradlew help
./gradlew developerDocs --dry-run
python3 tools/verify_agent_skills_sync.py
python3 /Users/ronvaldoz/.agents/skills/kmp-audit/scripts/audit_project.py .
```

Run focused Gradle compilation/tests for each module whose dependency declaration changes.
For native-boundary work, also follow `skills/awake-render-vulkan/SKILL.md` and retain the
Android Vulkan regression gate.

## Suggested Change Sets

1. `build: repair developerDocs paths and add dry-run gate`
2. `docs: publish current module registry and retire stale active paths`
3. `agents: rebase Codex profiles and make skill sync content-aware`
4. `architecture: enforce facade versus leaf dependency rules`
5. `vulkan: document or complete Android-native source ownership`

Each change set must remain independently buildable and must not mix public API renames with
documentation cleanup.
