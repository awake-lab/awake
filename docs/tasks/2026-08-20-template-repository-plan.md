# Awake Template Repository Plan

Date: 2026-08-20  
Status: Draft

## Objective

Create a public, minimal `awake-game-template` that proves Awake can be consumed from a clean
repository. Use it as the base for a separate private `awake-mmorpg-template`; keep MMO product
policy and services out of both Awake and the public template.

## Repository Topology

```text
awake                         public framework and published artifacts
awake-game-template           public general-purpose consumer template
awake-mmorpg-template         private MMO-oriented consumer template
<game-name>                   private production game created from the MMO template
```

The public and private templates are separate repositories, not long-lived branches in Awake.
GitHub's template flow uses the default branch, so separate repositories give a predictable
creation path and independent visibility/access control.

## Preconditions in Awake

The template must consume supported public APIs, not reproduce setup knowledge from the Awake
source tree. Complete these before calling the public template stable:

1. Repair `developerDocs` and stale module-path references from
   `2026-08-20-architecture-governance-standardization-plan.md`.
2. Publish and document the supported consumer module set, target matrix, JDK/Gradle baseline,
   and artifact coordinates.
3. Decide the supported first-template targets. Start with Desktop and Android; Web/iOS are added
   only after the same consumer path is proven on each target.
4. Provide two explicit consumption modes:
   - a pinned released Awake version for ordinary template users;
   - an opt-in composite-build/local-development mode for framework contributors.

## Phase 1 — Public `awake-game-template`

### Scope

Use a deliberately small module shape:

```text
:app          platform launchers and packaging only
:game         scene, gameplay composition, UI overlay, and assets
```

The first template must demonstrate one complete golden path:

```text
clean clone -> documented environment setup -> desktop run
           -> one controllable scene and UI overlay -> focused tests -> CI green
```

### Rules

- Depend only on documented public Awake APIs and the approved facade modules.
- Pin Awake versions in the version catalog; do not use a Git submodule.
- Keep the sample’s gameplay intentionally generic: camera, movement, a small scene, and UI
  chrome are enough. No account, database, server, protocol, or MMO concepts.
- Add a `TEMPLATE_CUSTOMIZATION.md` describing the intended extension points and the exact
  commands to build, run, and test from a clean clone.

### Acceptance

- A new clone builds without an adjacent Awake checkout.
- CI builds and tests the template on its declared targets.
- A framework contributor can switch to the documented composite-build mode without changing
  application source imports.
- Any need for `internal` Awake APIs is fixed in Awake or rejected; it is never copied into the
  template as a workaround.

## Phase 2 — External Consumer Validation

Treat the public template as Awake's release integration test.

1. In a clean directory, generate or clone the template and run its documented commands.
2. Test against the currently published Awake release, not project substitution.
3. Test a candidate Awake release against the template in CI before publication.
4. Record compatibility in the template changelog: Awake version range, Kotlin/Gradle/JDK
   baseline, supported targets, and known platform exceptions.

## Phase 3 — Private `awake-mmorpg-template`

Create the private template by copying the public template's released baseline, then add only the
MMO foundation shape:

```text
:client        client composition and presentation
:simulation    pure deterministic gameplay rules; no transport or database
:protocol      versioned command and snapshot types
:server        authoritative host and transport adapter
:testkit       bot clients, replay scenarios, latency/loss simulation
```

Do not add accounts, a database, economic systems, zones, or deployment tooling in the first
slice. Its first end-to-end acceptance scenario is:

```text
two clients -> one headless server -> movement commands -> server snapshots
-> client interpolation -> deterministic replay test
```

After this loop is reliable, add persistence, identity, world topology, and live operations as
separate capability plans in the private repository.

## Phase 4 — Framework Promotion Gate

Apply `docs/reference/framework-game-boundary.md` to every request to move code from the private
MMO template back into Awake. The MMO template is one consumer. Promote a capability only when
there are two credible consumers or a documented public-Awake limitation that a consumer-side
adapter cannot solve.

## ECC Kotlin Skill Adoption Plan

ECC is an optional external source, not a replacement for Awake's repo-local skills. Review skills
at a pinned commit before adoption; do not install the full ECC plugin or its hooks into Awake as
part of this plan.

| ECC component | Recommended action | Reason |
|---|---|---|
| `kotlin-patterns` | Evaluate first | Useful general Kotlin conventions, but overlap with Awake math/ECS/DSL invariants must not be overwritten. |
| `kotlin-coroutines-flows` | Evaluate for the private MMO template | Relevant once server/session/protocol flows exist; avoid applying it to frame-loop code without Awake-specific review. |
| `kotlin-testing` | Adapt, do not copy directly | It assumes Kotest, MockK, and Kover; Awake currently has KMP-specific test conventions. Extract only compatible coroutine/property-test ideas. |
| `kotlin-reviewer` agent | Trial in the private template | Useful second-review lane after local architecture/domain skills; it should not own Awake module policy. |
| `kotlin-build-resolver` agent | Trial on build failures only | Useful operationally; avoid making it an always-on authoring rule. |

### Adoption steps

1. Pin the ECC Git commit/release and inspect each selected `SKILL.md`, agent, and any referenced
   scripts before installation.
2. Compare it with the existing Awake/KMP skills; retain Awake-specific constraints where they
   conflict.
3. Install only the selected skill directories into the relevant template repository, not into
   Awake's canonical `skills/` tree.
4. Run one representative Kotlin/KMP task through each adopted skill and record the result.
5. Keep, adapt, or remove it based on observed value. Do not install ECC's global hooks, memory,
   or broad rules unless separately approved.

## Delivery Order

1. Complete Awake governance/documentation repair.
2. Define artifact/version and supported-target contract.
3. Build and externally validate the public game template.
4. Create the private MMO template from that validated baseline.
5. Add the authoritative movement/replay vertical slice.
6. Trial the selected ECC Kotlin components in the private template.
