# Developer Docs

Awake now has two documentation lanes, because one format is not enough for an engine:

1. `Dokka` for API reference
2. Snapshot-backed tutorial pages for visual and composition-heavy APIs

That split keeps reference docs exhaustive while still giving us proof that a widget,
layout primitive, or style composition actually looks right.

## Current Workflow

Run:

```bash
./gradlew developerDocs
```

This currently builds:

- module API references through `dokkaGeneratePublicationHtml`
- the Game DSL tutorial guide at
  `awake/engine/game-dsl/build/reports/game-dsl-tutorials/index.html`
- the UI DSL tutorial guide at
  `awake/engine/ui-dsl/build/reports/ui-dsl-tutorials/index.html`
- the UI snapshot gallery at
  `awake/engine/ui/ui-unstyled/build/reports/ui-snapshots/index.html`
- the curated UI tutorial guide at
  `awake/engine/ui/ui-unstyled/build/reports/ui-tutorials/index.html`

The rollout tracker for module-by-module coverage lives in
`docs/reference/tutorial-coverage.md`.

The DSL module map and recommended composition style live in
`docs/reference/dsl-modules.md`.

The root game-shell cookbook lives in
`docs/reference/game-dsl.md`.

## Repo Guidance Layout

Awake keeps project guidance in three layers:

1. `docs/*` for canonical project truth
2. agent entrypoints for startup hints
3. `skills/awake/*` for repo-local execution guidance

Use them like this:

- `docs/architecture.md`
  - stable architecture, module boundaries, threading model, and long-lived technical rules
- `docs/reference/ui-ownership.md`
  - canonical placement rules for reusable UI primitives, compositions, design-system pieces,
    and sample adapters
- `docs/reference/ui-validation.md`
  - canonical UI correctness gate for previews, semantic checks, truncation/content-fit rules,
    and animation/state proof requirements
- `docs/reference/ai-collaboration.md`
  - the cross-agent contract for `docs/*`, entrypoints, and `skills/*`
- `docs/reference/agent-catalog.md`
  - the canonical roster, naming convention, and responsibility map for repo-local agents
- `docs/reference/agent-starter-pack.md`
  - the reusable downstream starter shape for multi-agent repo setup
- `docs/reference/agent-routing.md`
  - real Awake examples for choosing the right repo-local agent
- `AGENTS.md`, `CLAUDE.md`, `GEMINI.md`, `.claude/AGENTS.md`
  - thin startup files that point assistants at the canonical docs
- `skills/awake/agents/*.md`
  - task-specific working guidance for ECS, engine, and other Awake domains
- `skills/awake/commands/*.md`
  - repo-local operational commands and review workflows
- `skills/awake/templates/*.md`
  - reusable templates for new repo-local agent docs

Rule of thumb:

- if the guidance answers "how is Awake designed?", put it in `docs/*`
- if it answers "how should an agent work on Awake?", put it in `skills/*`

## What Belongs Where

### API Reference

Use `Dokka` for:

- public types
- function signatures
- parameter semantics
- module-level package docs
- lifecycle and threading contracts

Every published module should keep its KDoc good enough that Dokka is worth opening.

### Tutorial Docs

Use snapshot-backed guides for:

- the declarative UI facade in `awake:engine:ui-dsl`
- UI widgets
- style composition
- layout patterns
- render/debug overlays
- future scene tooling and editor panels

These docs should answer: "How do I actually build this?" rather than "What is the
signature?"

## Live Preview Loop

For fast iteration on `samples/ui-showcase` pages or the `ui-designsystem`/`ui-unstyled`
components they render, don't run the full `desktopTest` suite -- it runs every test in the
module, including unrelated pre-existing failures. Instead, scope to the single test class
that writes the preview gallery:

```bash
./gradlew :samples:ui-showcase:desktopTest --tests "*UiShowcasePreviewDocsTest*" --continuous
```

`--continuous` is Gradle's own file-watch mode: it watches the inputs of every task in the
graph (so edits to `ui-designsystem`/`ui-unstyled` sources that the showcase depends on
trigger a rebuild too, not just edits inside `samples/ui-showcase` itself) and reruns on
change -- regenerating `samples/ui-showcase/build/reports/ui-previews/index.html` in a few
seconds per change, since `uiShowcasePreviewReport` is `finalizedBy` the test task.

To also auto-reload an open browser tab, run the wrapper script instead, which pairs the
same `--continuous` task with a tiny static file server that injects a reload-on-change poll
into the served HTML:

```bash
./tools/ui_preview_watch.sh 8090
# open http://127.0.0.1:8090
```

This is also wired into `.claude/launch.json` as the `ui-preview-watch` configuration,
matching the `wasmjs-*` entries' launch pattern.

## Adding a UI Tutorial

1. Add or update a curated test in either:
  - `awake/engine/ui-dsl/src/desktopTest/kotlin/io/github/ronjunevaldoz/awake/ui/snapshot/UiDslTutorialDocsTest.kt`
   - `awake/engine/ui/ui-unstyled/src/desktopTest/kotlin/io/github/ronjunevaldoz/awake/ui/snapshot/UiTutorialDocsTest.kt`
2. Render the example with `saveUiTutorialSnapshot(...)`
3. Keep the title and summary short and tutorial-oriented
4. Add machine-checkable validation for semantics, text fit, clipping, and state coverage per
   `docs/reference/ui-validation.md`
5. Re-run the matching desktop test task
6. Open the generated HTML report for that module

The important convention is that tutorial screenshots are generated from tests, not from
manually curated images. That gives us docs that stay close to the code and fail loudly when
the rendering surface changes.

## Rollout Plan

### Phase 1: UI

Now in place:

- curated UI DSL tutorial snapshots
- curated UI tutorial snapshots
- generated HTML tutorial page
- generated visual review gallery

### Phase 2: Scene and Runtime

Next, mirror the same idea for:

- `awake:scene` scene-graph setup guides
- runtime bootstrap examples from `awake:engine:game`
- sample-driven walkthroughs for camera/debug systems

These may use screenshots, diagrams, or generated JSON snippets depending on the surface.

### Phase 3: Per-Module Tutorial Index

Add one durable guide page per public module under `docs/reference/`, for example:

- `docs/reference/base.md`
- `docs/reference/ecs.md`
- `docs/reference/scene.md`
- `docs/reference/ui.md`
- `docs/reference/render-api.md`

Each page should contain:

- what the module is for
- the 3-5 most important entry points
- one minimal example
- one "composition" example
- links to its Dokka output

## Why This Shape

If we try to force everything into prose docs, they drift.

If we rely only on API reference, people can see the types but not the intended
composition.

The combination we want is:

- reference docs generated from KDoc
- tutorial docs generated from examples and tests
- screenshots generated automatically where visuals matter

That gives us a docs system we can grow alongside the upcoming DSL instead of bolting it on
after the fact.
