# UI Showcase Parity Tracker

## Purpose

This is the execution tracker for the post-boundary-migration UI cleanup. It is intentionally
separate from the architecture migration plan: a public component can be correctly separated
into `ui-headless` and still render incorrectly.

**A checkbox may be marked complete only when its evidence column contains all required proof.**
Source changes, a green compile, or an Awake-to-Awake snapshot alone do not complete an item.

## Completion protocol

For every component or layout change, record:

1. the pinned shadcn/Radix source or local reference capture that defines the expected result;
2. a focused behavior/semantic test, including constrained or long-text coverage where relevant;
3. a regenerated Awake preview plus human inspection of its component crop and heatmap;
4. an automated crop result with an explicit reviewed threshold, or an explained reason why the
   crop cannot yet be measured; and
5. the relevant compile/test command run against the final commit.

Do not use `-DAWAKE_RECORD_SNAPSHOTS=true` to turn a failure green before the reference diff is
reviewed. `docs/reference/ui-validation.md` remains the policy source of truth; this file is the
current work ledger.

## Boundary and modifier audit

| Item | Status | Evidence / remaining action |
|---|---|---|
| Public design-system to Headless migration | complete | `reportUiDesignsystemMigrationProgress`: 23/23 public recipe files are Headless-backed. |
| Production Design System classpath boundary | complete | Audit reports no `ui-core` on the public compile classpath. |
| Test-only compatibility bridge removal | in progress | The checkbox-based legacy `ShadcnRadioGroup` bridge is deleted. Checkbox, Radio, and Progress parity fixtures now use public Headless; 57 legacy bridge files remain. Remove each only after its remaining fixture/caller has moved. |
| One public modifier facade | complete | `ui-headless.Modifier` is the sole public modifier type for Headless and Design System recipes. |
| Raw Core modifier containment | complete | `ui-core.UiModifier` does not appear in Headless/Design System public signatures; `HeadlessModifier` adapts it internally. |
| Unstyled primitive encapsulation | pending | `ui.unstyled` still has Kotlin-public declarations and is imported by the compatibility bridge plus a small number of production/test callers. Migrate those callers, then make these implementation primitives module-internal under the Headless implementation package. |
| Alias cleanup | pending | `HeadlessModifier` import aliases in `ui-designsystem-compat` are bridge source compatibility, not a second type. Remove with the test-only bridge. |
| Parity CLI | complete | `scripts/awake ui` dispatches manifest-backed official capture, Awake preview, semantic debug overlay, and crop validation. Unsupported fixture states fail explicitly instead of being silently substituted. |

`HeadlessModifier` itself is **not a duplicate public API**. It is one internal adapter that
wraps the Core `UiModifier`; the duplicate-looking names are import aliases in temporary
compatibility files. The separation is still aligned, but that bridge must not expand.

`ui.unstyled` is currently the lower-level implementation behind Headless, not a competing
consumer API. Its public visibility is legacy leakage, however, so it remains explicitly
pending until only `ui-headless` exposes supported widgets to other modules.

## Visual-fidelity gate

Current component crop coverage: **10/23 recipe families (43%)**. All ten pairs currently
report `REVIEW`, not `PASS`, because no reviewed mismatch threshold has been accepted. Therefore
the verified visual-completion count is **0/23**.

| Crop case | State | Next proof |
|---|---|---|
| Button, light and dark | review | Inspect regenerated crop/heatmap; resolve font and width drift; set threshold only after review. |
| Badge | review | Verify pill height, padding, text fit, and variant colors. |
| Switch | review | Verify geometry, knob travel, state colors, and disabled state. |
| Checkbox | review | Verify 16px square, 4px radius, check path, and disabled state. |
| Radio Group | review | Public Headless recipe now has `size-4` rings, `size-2` dots, and inline `gap-2` labels. Review remaining font/raster drift. |
| Progress | review | Public Headless fixture matches the official 212×32 geometry, 25%/65% fills, and `bg-primary/20` (`#ccc`) track at rest. Review remaining canvas/raster-edge drift. |
| Input | review | Verify input border/focus geometry and text baseline. |
| Select | review | Verify trigger plus separately add open-menu crop/interaction coverage. |
| Tabs | review | Verify intrinsic (`w-fit`) track, active shadow, and selected state. |

## Reported showcase defects

| Item | Source-owned layer | State | Required completion evidence |
|---|---|---|---|
| Input OTP was cropped / invisible | `ui-designsystem` recipe + Headless layout | in review | Regenerate page; confirm all six slots have non-zero bounds and fit their preview card. |
| Progress had reversed white/black fill | design-system visual mapping | fixed, visual review pending | Its legacy Core-receiver fixture was rendering the compatibility recipe. The public Headless fixture now settles the live animation before capture and uses the borderless `bg-primary/20` + primary recipe. |
| Breadcrumb baseline misalignment | Headless row alignment + design-system recipe | in review | Bounds probe and reference crop showing vertically centered items. |
| Tabs filled available width | Headless wrap-content layout + design-system recipe | in review | Semantics/bounds probe proving intrinsic track width, then component crop. |
| Radio rendered checkbox checkmark | Headless selection behavior | in review | Legacy checkbox-based bridge removed. Public recipe has named `size-4`/`size-2`/`gap-2`/`gap-3` metrics, an inline bounds test, and `Radio` semantic role. Current real-reference crop is 32.57% `REVIEW`; remaining drift is visual/font review, not checkbox behavior. |
| Checkbox did not match shadcn geometry | Headless selection behavior + recipe | in review | Updated crop reviewed against pinned checkbox reference. |
| Catalog sidebar labels centered / groups weak | design-system sidebar recipe + showcase composition | in review | Full showcase screenshot proving left-aligned menu rows, visible group headers, and active row. |
| Dropdown options rendered as trigger buttons | Headless menu semantics + recipe | in review | Open-menu interaction test and crop proving menu-item rows. |

## Component-family coverage backlog

The migration report counts recipe files, while this table tracks customer-visible component
families. A family is verified only after the visual-fidelity gate above is met.

- [ ] Inputs: Button, Badge, Text Field, Text Area, Input OTP, Checkbox, Radio Group, Switch,
  Slider, Select/Combobox
- [ ] Layout/navigation: Card, Tabs, Accordion, Collapsible, Breadcrumb, Sidebar, Table,
  Resizable, Scroll Area, Separator
- [ ] Overlays: Dialog, Drawer, Sheet, Popover, Tooltip, Dropdown Menu, Context Menu
- [ ] Status/typography: Alert, Progress, Skeleton, Spinner, Toast, Avatar, Kbd
- [ ] Showcase chrome: catalog sidebar, page header, preview/code tabs, preview card, notes card

## Immediate execution order

1. Convert the remaining Core-receiver parity fixtures to public Headless in component batches,
   then remove their matching compatibility files; compilation without the test bridge is the
   completion gate for that batch.
2. Add direct bounds probes for OTP, breadcrumb, tabs, radio, and sidebar; do not infer layout
   correctness from source.
3. Open the generated crops and heatmaps for the ten existing cases; record an explanation for
   every mismatch before tuning or setting thresholds.
4. Add component cases for OTP, radio, progress, breadcrumb, sidebar, and open dropdown.
5. Convert the remaining `REVIEW` crop cases to `PASS` only when the source, visual, semantic,
   and final build evidence are all linked here.
