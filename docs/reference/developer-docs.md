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
- the UI snapshot gallery at
  `awake/engine/ui-widgets/build/reports/ui-snapshots/index.html`
- the curated UI tutorial guide at
  `awake/engine/ui-widgets/build/reports/ui-tutorials/index.html`

The rollout tracker for module-by-module coverage lives in
`docs/reference/tutorial-coverage.md`.

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

- UI widgets
- style composition
- layout patterns
- render/debug overlays
- future scene tooling and editor panels

These docs should answer: "How do I actually build this?" rather than "What is the
signature?"

## Adding a UI Tutorial

1. Add or update a curated test in
   `awake/engine/ui-widgets/src/desktopTest/kotlin/io/github/ronjunevaldoz/awake/ui/snapshot/UiTutorialDocsTest.kt`
2. Render the example with `saveUiTutorialSnapshot(...)`
3. Keep the title and summary short and tutorial-oriented
4. Re-run `./gradlew :awake:engine:ui-widgets:desktopTest`
5. Open `awake/engine/ui-widgets/build/reports/ui-tutorials/index.html`

The important convention is that tutorial screenshots are generated from tests, not from
manually curated images. That gives us docs that stay close to the code and fail loudly when
the rendering surface changes.

## Rollout Plan

### Phase 1: UI

Now in place:

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
