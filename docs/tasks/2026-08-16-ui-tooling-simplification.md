# UI tooling simplification plan

## Goal

Make Awake UI tooling easy to enter, trustworthy when it reports status, and small enough that
each tool has one job. The intended end state is:

- one developer front door: `scripts/awake ui`;
- one generated source for UI fidelity/status reporting;
- separate, clearly named lanes for interaction tests, layout fidelity, visual regression, and
  external reference assets; and
- no transition adapters, print-only tests, or duplicate hand-maintained status reports left
  without an explicit reason.

This is a simplification and correctness plan. It does not change component behaviour or loosen
any existing verification gate.

## Findings that drive the plan

| Finding | Consequence | Decision |
|---|---|---|
| Several tools still read the former `awake/engine/ui/...` layout. | Generated reports inspect missing directories and can publish false results. | Repair paths before trusting or consolidating reports. |
| `shadcn-parity.md` currently reports zero implemented components while the manual coverage document reports 41. | Two status documents contradict the code and each other. | Keep generated reporting, repair it, then retire duplicate manual status sources. |
| `ShowcaseFramePerfProbeTest` only prints timing. | It consumes normal test time without making a pass/fail claim. | Remove it or replace it with a real benchmark; do not keep it as a test. |
| `LegacyShowcaseCoreAdapters.kt` still has real test callers. | It is transitional, not dead code. | Migrate its callers first; delete only when no caller remains. |
| `test_awake_ui_cli.py` has useful mapping tests but is not invoked by a normal verification task. | It can silently rot. | Wire it into a dependable UI verification task, or remove it if that is not practical. |

## Work sequence

### 1. Repair the existing pipeline

Update moved paths in UI tools, manifests, docs, and skills. This includes the shadcn token
extractor, parity/status generators, and font-reference manifest.

**Completion checks**

- `extract_shadcn_tokens.py` writes to the current design-system test source path.
- font capture resolves the current Roboto resource.
- both status generators inspect existing UI source roots.
- regenerated reports identify the current revision and no longer claim zero implementations.

**Risk:** low. This changes tooling paths and generated reporting, not UI runtime behaviour.

### 2. Make reporting have one owner per question

Keep `ui-fidelity-status.md` as the generated system-level status matrix. Extend the repaired
parity-report generator, or add a small dedicated generator, so component coverage is generated
from the current source and manifests.

Move durable, actionable risks from the hand-maintained `ui-status.md` into the appropriate
task or issue. Retire `ui-status.md` and the hand-maintained form of `ui-component-coverage.md`
only after the generated replacement covers their useful information.

**Completion checks**

- no two canonical documents report component implementation counts;
- each remaining report says how to regenerate it;
- a generated report cannot silently inspect a missing source root.

**Risk:** medium. The migration must preserve useful open risks rather than deleting context.

### 3. Remove non-gating and transitional test tooling

Delete `ShowcaseFramePerfProbeTest`, or move its scenario into a dedicated benchmark module
with a defined measurement policy. Migrate the remaining showcase tests that require
`LegacyShowcaseCoreAdapters.kt` to the supported `UiScope` and shared test-session helpers.
Then delete the adapter.

**Completion checks**

- no normal test merely prints a performance result;
- no imports or call sites reference `LegacyShowcaseCoreAdapters.kt`;
- affected showcase tests still compile and pass through the shared UI test lifecycle.

**Risk:** medium. Receiver migration needs focused compilation and tests, but does not require
production component changes.

### 4. Make the CLI contract executable

Run `tools/test_awake_ui_cli.py` from the normal UI verification path, with its dependencies
declared and reproducible. If project policy does not allow a Python dependency in verification,
port the small mapping tests to Kotlin instead; do not leave a standalone test suite unwired.

**Completion checks**

- CI/local verification runs the CLI contract tests;
- the test environment declares Pillow/Python requirements or has no Python dependency;
- unsupported component states remain explicit failures.

**Risk:** medium. The decision is mostly about build-environment ownership.

### 5. Finish the documentation boundary

Keep `tools/README.md` as the short operational entry point. Keep
`docs/reference/ui-validation.md` as the policy for what proof is required. Keep detailed
implementation notes beside their tools or in the shadcn reference-pipeline document.

Update skills after the final commands and report names are stable. Do not duplicate the full
README in skills; skills should route an agent to the canonical document and enforce the
verification rules.

**Completion checks**

- `tools/README.md` starts with `scripts/awake ui` and the four proof lanes;
- live preview is consistently described as optional/manual;
- docs and skills contain no stale UI module paths;
- one status report is canonical for each question.

## What remains intentionally separate

These tools are not redundant because they answer different questions:

| Tool or gate | Keep because it answers |
|---|---|
| `ShadcnGeometryParityTest` | Whether layout bounds match the pinned shadcn reference. |
| Snapshot tests | Whether Awake output changed from an accepted baseline. |
| `ShadcnReferenceComparisonTest` / `compare_parity.py` | Whether visual colour/border/radius regression worsened; not layout correctness. |
| `compare_component_crops.py` | Whether a semantic Awake component crop can be compared without manual image editing. |
| Font and Heroicons capture tools | Whether Awake rendering agrees with the original external asset. |
| `ui_preview_watch.sh` | Fast manual visual iteration; it is not a gate. |

## Recommended implementation order

1. Phase 1: stale path repair and report regeneration.
2. Phase 2: generated-report consolidation.
3. Phase 3: remove print-only and legacy test tooling.
4. Phase 4: wire the CLI contract tests.
5. Phase 5: synchronize docs and skills, then remove superseded documents.

Do not delete a report, tool, or adapter merely because it has a similar name. Delete it only
after its distinct question is covered elsewhere and its callers or documentation references are
gone.
