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
| Input OTP was cropped / invisible | `ui-designsystem` recipe + Headless layout | fixed, visual review pending | OTP slots updated to fixed 36dp size; visible in showcase and preview cards. |
| Progress had reversed white/black fill | design-system visual mapping | fixed, visual review pending | Its legacy Core-receiver fixture was rendering the compatibility recipe. The public Headless fixture now settles the live animation before capture and uses the borderless `bg-primary/20` + primary recipe. |
| Breadcrumb baseline misalignment | Headless row alignment + design-system recipe | fixed, visual review pending | Consistently uses `caption` size for labels and separators; vertically centered in row. |
| Tabs filled available width | Headless wrap-content layout + design-system recipe | fixed, visual review pending | Added `wrapContentWidth()` to internal track row; track now hugs tab items. |
| Radio rendered checkbox checkmark | Headless selection behavior | fixed, visual review pending | Legacy checkbox-based bridge removed. Public recipe has named `size-4`/`size-2`/`gap-2`/`gap-3` metrics, an inline bounds test, and `Radio` semantic role. Current real-reference crop is 32.57% `REVIEW`; remaining drift is visual/font review, not checkbox behavior. |
| Checkbox did not match shadcn geometry | Headless selection behavior + recipe | in review | Updated crop reviewed against pinned checkbox reference. |
| Catalog sidebar labels centered / groups weak | design-system sidebar recipe + showcase composition | fixed, visual review pending | `pill` width fixed to `wrapContent`; Typography recipes explicitly left-aligned; Group headers styled with `text-xs font-medium`. |
| Dropdown options rendered as trigger buttons | Headless menu semantics + recipe | in review | Open-menu interaction test and crop proving menu-item rows. |

## Catalog restructure (2026-08-14)

The showcase now publishes one catalog: `ShowcasePages` in `ShowcaseCatalog.kt`, one file per
page under `ui/pages/<category>/`. What this replaced, and what it exposed:

| Before | After |
|---|---|
| 3 catalogs: 30 shipped pages, 31 test-only preview entries, ~29 parity fixtures | 1 catalog of 54 pages; preview fixtures derived from it |
| `showcasePageById` fell back to `ShowcasePages.first()` | `showcasePageOrNull` returns null; unknown ids fail loudly |
| 5 fixtures fingerprinted the Introduction page while naming other components | `everyPageProducesADistinctLayout` asserts no two pages share a fingerprint |
| 16 preview functions compiled but were never registered | an unregistered preview is dead code, not a hidden page |
| 4 registered pages rendered prose instead of the component (Tooltip, Drawer, Resizable, Context Menu) | all four render the real component |
| preview metadata read from a JVM annotation; iOS + wasmJs early-returned | metadata is page data; both targets now run the assertions |

Defects the new coverage surfaced and fixed, all in shared code rather than the sample:

- `UiScope.combobox` passed `semanticId = id` to its trigger label while also recording a
  `Dropdown` node with `id` -- a duplicate semantic id. `primitiveSelect` had always used the
  `.label` suffix; the public widget now matches it.
- `ShadcnTableScope.row` emitted `cell.$index` and an unnamed separator for every row, so any
  table with more than one row produced duplicate ids. Ids are now row-qualified.
- `separator()` derived its id from its orientation alone, so one frame could not contain two
  horizontal separators. It now takes an optional explicit `id`.

- `shadcnField`, `shadcnFieldSet`, and `shadcnFieldGroup` were plain columns, so their children
  wrap-content and a text field inside one collapsed to roughly its label width and truncated
  its own value. All three are block-level in shadcn (`fieldVariants` opens `flex w-full`,
  `FieldGroup` repeats `w-full`, `FieldSet` is a `<fieldset>`), so they now apply a new
  `Modifier.fillMaxWidthOrDefault()` -- the width counterpart of the existing
  `heightOrDefault`, which keeps an explicit caller width winning. `ShadcnFieldContainerWidthTest`
  covers both directions and was checked against the unfixed recipe before landing.
- `shadcnFieldSet`/`shadcnFieldGroup` accepted an `id` they never passed to their column. Wired
  through. `shadcnField`'s horizontal branch still drops it: `ui-headless` has no `row(id = ...)`
  overload.

## Component-family coverage backlog

The migration report counts recipe files, while this table tracks customer-visible component
families. A family is verified only after the visual-fidelity gate above is met.

Every family below now has a catalog page. A checkbox still means *visually verified* against
the pinned reference, which the crop gate above still governs -- having a page is necessary,
not sufficient.

- [ ] Inputs: Button, Badge, Text Field, Text Area, Input OTP, Input Group, Checkbox,
  Radio Group, Switch, Toggle, Toggle Group, Slider, Range Slider, Select, Combobox, Field
- [ ] Layout/navigation: Card, Collapsible Card, Tabs, Accordion, Collapsible, Breadcrumb,
  Sidebar, Resizable, Table, Scroll Area, Separator, Surface, Canvas
- [ ] Overlays: Dialog, Alert Dialog, Drawer, Sheet, Popover, Dropdown Menu, Context Menu,
  Tooltip
- [ ] Status/typography: Alert, Avatar, Progress, Skeleton, Spinner, Toast, Kbd, Empty,
  Typography
- [ ] Showcase chrome: catalog sidebar, page header, preview/code tabs, preview card, notes card

Registered as placeholders -- no Awake primitive exists yet, and each page says so on screen:
Form, Button Group, Item, Chart, Carousel, Date Picker.

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
