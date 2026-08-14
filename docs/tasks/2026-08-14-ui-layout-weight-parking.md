# UI layout: weight distribution — parked 2026-08-14

Everything below came out of one report: "sidebar footer is not visible". It ended in the layout
engine, not the sidebar.

## The open bug

**`weight()` does nothing on a column's vertical axis.** The child wraps its content instead of
taking its allotted share.

`LayoutSizingMatrixTest` records it — 11 of 16 cells red:

```
ok   Column/Fixed/Fixed             actual=64.0    expected=64.0
FAIL Column/*/Weight                actual=0.0     expected=304   (empty child)
FAIL Column/*/WeightWithContent     actual=24.0    expected=304   (wraps its 24px child)
FAIL Column/*/FillMax               actual=344.0   expected=304   (subtracts one sibling, not two)
FAIL Surface/*/FillMax              actual=328.0   expected=304   (same, minus surface padding)
```

Three separate defects, not one:

1. **weight → wrap.** The value returned is exactly the child's content height.
2. **fillMaxHeight is off by one sibling.** A filling child overlaps whatever follows it.
3. **Column and Surface disagree by 16px** (surface's content padding), so they are not even
   consistent with each other.

Only `Fixed/Fixed` is correct everywhere. Sizing works when nothing has to be divided.

### What is ruled out (with evidence, do not redo)

- The machinery exists and is right. `ColumnScope.claimSlot` substitutes `FillMax` for a weighted
  `WrapContent` child and records the weight; `resolveWeightedMainAxis`'s arithmetic checks out.
- Detection is not the problem. `resolveHasWeightedChild` is a plain passthrough when `cacheKey`
  is null, which is the matrix's case, so the trial does run.
- Not an empty-child artifact. `WeightWithContent` returns the inner child's height exactly.
- Not density. An earlier "it's density-sensitive" conclusion was **wrong** — that test only
  failed first because its threshold was looser than the others.
- Not `uiScope()` escaping the parent's measurement. Removed every one; the number did not move.

### The next probe

A weighted child that is **not a container** — `spacer(Modifier.weight(1f))`, which claims a slot
and nothing else. It returns `Unit`, so capture bounds some other way than assignment (that is
what broke the last attempt).

- If a weighted spacer sizes correctly → the bug is the child `column()` re-resolving its own
  height through `resolveMeasuredColumn` and overwriting the planned slot.
- If it is also 0 → the bug is the parent's planned-slot distribution.

Supporting clue: `LayoutTest.columnWeightSplitsRemainingSpaceEvenlyForEqualWeights` **passes** and
calls `claimSlot(...)` directly. The matrix fails and goes through `column(modifier = weight(...))`
→ `claimModifiedSlot`. That is the whole difference. Nested measurement contexts and their
recording suppression (`recordingSuppressionDepth`, `wrapContributionSuppressionDepth`) are where
to look.

## Landed

| commit | what |
|---|---|
| `2d8abbaa` | text centres on the cap box, not an inverted band — fixed descenders |
| `5cf62917` | scroll viewport measures with its real height, not the 100000px sentinel |
| `e72e8793` | why badge padding/weight diverge from upstream (a tried-and-killed experiment) |
| `167d75a7` | `row()`/`box()` throw on a scroll modifier they ignore |
| `8e8b339e` | skill: one widget entry point on `UiScope` |
| `e0f66992` | the layout sizing matrix (red by design) |
| `bcf11632` | a dropped weight throws instead of silently wrapping |
| `7d2f6f47` | skill: test tiers, and when not to write a test |

## Parked, uncommitted

**Sidebar + preview.** `ShadcnSidebarRecipes.kt` (the shadcn sidebar example), the
`shadcn-sidebar-example` preview and its two tests. The sidebar fix — all three slots inside one
plain column, content weighted — is **structurally right and cannot work until weight does**.
Do not "fix" it another way; it matches upstream's `SidebarHeader` / `SidebarContent (min-h-0
flex-1 overflow-auto)` / `SidebarFooter`.

Also uncommitted and unrelated to this: `awake/ui/README.md`, `awake/ui/text/README.md`,
`ShadcnNavigationRecipes.kt`, `ShadcnButtonStyles.kt`, `ScrollState.kt`, `UiShowcaseChrome.kt`,
`UiShowcaseUi.kt`, three snapshot PNGs.

## Also open

- **Baseline re-record.** Font and sidebar changes moved 4 preview scenes, 2 signature hashes and
  the showcase layout signature. Its own commit, never mixed with source. Justified because the
  shadcn parity gate — the one oracle that is not self-referential — passes.
- **Delete `surface`'s dead overloads.** `AbsoluteScope`/`BoxScope` are `modifier = modifier`;
  `ColumnScope`/`RowScope` only set the cross-axis default, which one `UiScope.surface` can do
  with a `when (this)` (precedent at `Column.kt:163`). Do it after the matrix is green so the
  deletion is provably behaviour-preserving.
- **Test audit.** `ui-designsystem` turned out **not** to be redundant: 2 files assert nothing but
  existence (`ShadcnComboboxTest`, `ShadcnTabsFidelityTest` — delete), 2 components have 3+ files,
  and only 32 of 244 assertions are layout geometry that belongs in `ui-core`. The unaudited
  modules are `ui-headless` (42 files) and `ui-showcase` (17, each needing a render).
- 4 pre-existing studio failures, unchanged since before the refactor.

## Method notes worth keeping

- **Exact values, never thresholds.** Four sidebar tests passed against a visibly broken sidebar
  because each asserted "more than 48px"; every wrong answer from 0 to 24 cleared it.
- **A matrix beats hand-picked cases.** Four targeted tests found nothing in a session. One
  12-cell matrix found eight defects in one run, and the passing cells narrowed the cause as much
  as the failing ones.
- **The preview JSON beat the PNG.** The image said "something is wrong"; the JSON gave
  `y=187.875, h=96` and turned guessing into arithmetic.
- **Check the task name exists.** `compileDesktopTestKotlin` does not exist in this project;
  Gradle's "task not found" was buried under a `grep '^e:'` filter and read as a clean compile.
  Grep `What went wrong` too.
- **Scope the formatter.** `spotlessApply` reformatted 36 and then 48 unrelated files, twice.
  Diff before applying, not after.
