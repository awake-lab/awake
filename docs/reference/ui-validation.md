# UI Validation

This document is the canonical source for Awake's shared UI verification rule.

## Goal

Stop relying on eyeballing for shared UI regressions.

Awake's reusable UI is only considered done when it has machine-checkable proof for:

- visual rendering
- semantic structure
- text fit and truncation
- content bounds and clipping
- state coverage
- animation coverage when the component animates

## Non-Negotiable Rule

Any change to shared UI in:

- `awake:engine:ui:ui-core`
- `awake:engine:ui:ui-unstyled`
- `awake:engine:ui-dsl`
- `awake:engine:ui:ui-designsystem`
- shared showcase or docs previews that demonstrate those modules

must ship with automated validation.

Manual browser review is useful, but it is not enough on its own.

## Required Proof By UI Type

### Static Shared Widgets

Every shared widget change must include:

1. a preview or snapshot that renders the widget
2. semantic validation
3. text-fit validation
4. content-fit and clipping validation
5. a stable regression signature when the output is intentionally locked

Required states:

- default
- hovered, pressed, or focused when supported
- disabled when supported
- selected/checked/expanded when supported
- long-text or constrained-width case when text is visible
- light/dark or theme variants when the widget is theme-sensitive

### Shared Layout Or Container APIs

Every layout/container change must include:

1. a preview or snapshot of the composed layout
2. overlap validation for sibling controls that must not collide
3. content-fit validation for clipped or padded regions
4. a stable layout signature or snapshot signature

### Animated Components

Animated shared UI must not be validated only at one rest frame.

Required proof:

1. at least three sampled visual states:
   - rest
   - in-flight
   - settled
2. the same semantic/content-fit checks at each sampled frame
3. explicit allowance for intentional truncation or overlap only when documented in the test

If a component shimmers, expands, fades, slides, or eases between values, the preview/test
must capture more than the final state.

## Canonical Test Surfaces

Use these first:

- `AwakeUiPreview` for preview-backed docs and reusable gallery entries
- `validateAwakeUiPreview(...)` for shared preview validation
- `inspectUiFrame(...)` for primitive-level rendering checks
- `inspectSemanticNodes(...)` for semantic integrity
- `inspectSemanticContentFit(...)` for bounds and clipping correctness
- `inspectTextTruncation(...)` for accidental clipping or overflow
- `inspectSemanticOverlaps(...)` for sibling-control collision checks
- snapshot signature tests for locked visual baselines
- layout signature tests for page-level semantic layout baselines
- `ShadcnParityScreenshotTest` (`samples/ui-showcase`) for pixel-baseline parity against the
  shadcn reference — `./gradlew :samples:ui-showcase:desktopTest --tests
  "*ShadcnParityScreenshotTest*"`. Regenerate goldens with `-DAWAKE_RECORD_SNAPSHOTS=true`
  only after inspecting the recorded diff PNG confirms the drift is an intended change, never
  blind
- the `RendererHeadlessPixelBaselineTest`-style pattern (`awake:backend:vulkan:desktopTest`)
  for headlessly rendering a real frame and dumping it as a PNG when no live window/browser is
  available — the go-to for "does this actually render right" questions on 3D/backend work
- `UiShowcaseLayoutCostTest` (`samples/ui-showcase:desktopTest`) for frame-cost/perf
  regressions — measures real trial-measure pass counts and wall-clock time, not estimates
- the throwaway-probe-test idiom: build the real widget/scene through the real `UiContext`/
  renderer and read its actual `UiBounds`/pixels, instead of reasoning about spacing,
  centering, or collapse behavior from source alone. This is the highest-leverage tool in this
  list — used to settle real "is X actually centered/tighter/regressed" questions with numbers
  instead of guesses (see `RowCrossAxisCenterProbeTest`, `UiShowcaseSidebarGapProbeTest`,
  `TypographyPaddingProbeTest` for the pattern). Keep the probe as permanent regression
  coverage when it proves something worth locking in, delete it when it was purely diagnostic

## Allowed Exceptions

Exceptions must be explicit in the test, not implicit in the UI:

- intentional truncation must be allowlisted by semantic id
- intentional overlap must be checked in a targeted overlap rule
- intentionally empty semantics are allowed only for non-interactive pure-decoration previews

If an exception is real, encode it in the validation config so the next regression stays loud.

## Required Commands

For shared UI work, run the smallest relevant verification task before considering the work
done:

```bash
./gradlew :awake:engine:ui:ui-testing:commonTest
./gradlew :awake:engine:ui:ui-unstyled:desktopTest
./gradlew :samples:ui-showcase:desktopTest
```

Add module-specific tasks when the change lives outside those surfaces.

## Placement Rule

Put long-lived policy here in `docs/reference/ui-validation.md`.

Keep only a short reminder in:

- `AGENTS.md`
- `.claude/AGENTS.md`
- repo-local UI quality skills

The rule should stay canonical in docs, not get copied into every agent entrypoint.

## Definition Of Done

A shared UI change is done only when:

1. the relevant preview/snapshot exists
2. validation is automated and green
3. the docs/gallery artifact can be regenerated from tests
4. any intentional exception is encoded in test config
5. a human can open the generated report and confirm the result without discovering new obvious breakage
