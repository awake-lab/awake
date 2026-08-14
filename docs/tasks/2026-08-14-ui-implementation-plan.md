# UI implementation plan — from 2026-08-14

Ordered so each phase leaves the tree in a state the next one can trust. Phase 0 first because
everything after it needs a signal that is not lying.

Companion: `2026-08-14-ui-layout-weight-parking.md` (what was already ruled out, with evidence —
read it before re-investigating anything).

---

## Phase 0 — get the signal honest (blocking)

Nothing here changes behaviour. It makes the next four phases verifiable.

**0.1 Re-record baselines.** 4 preview scenes, 2 signature hashes, 1 showcase layout signature —
all moved by this session's text-centring and layout fixes. Its own commit, PNGs and hashes only,
no source. Justified because the shadcn parity gate (the one oracle that is not self-referential)
passes.

**0.2 Confirm the branch is green** except `ShadcnSidebarExampleLayoutTest`, which is a committed
reproduction of 1.1 below.

*Done when:* `./gradlew build` is green apart from that one named test.

---

## Phase 1 — finish the layout engine

**1.1 Weighted child directly inside a `surface` nested in a scroll viewport.** Still plans
nothing, so the child throws. `111793c8` (nested passes get their own accounting scope) was
necessary and not sufficient.

Reproduce: delete the wrapper column in `shadcnSidebar`, run
`ShadcnButtonScrollClickInteractionTest`.

Do NOT start from a hypothesis — four were disproved chasing this. Build the instrument first:
print what `planWeightedColumnSlots`' trial actually returns (`measured.weights`,
`measured.slots.size`) for that nesting. The answer picks the fix.

Closes: the sidebar wrapper column, `ShadcnSidebarExampleLayoutTest`, and very likely the
preview's `footer y=144`.

**1.2 Generate the per-scope rules instead of writing them.** `RowScope` and `ColumnScope` differ
only in which axis is "main", and the height side was missing the deferral the width side had
(`eb1bda4b`). Give the scope a main axis and write it once:

```kotlin
modifier.withSizeFallback(crossAxisDefault(), mainAxisDefault(modifier.layoutWeight))
```

**1.3 Delete `surface`'s dead overloads.** `AbsoluteScope`/`BoxScope` are `modifier = modifier`.
`ColumnScope`/`RowScope` only set the cross-axis default, which one `UiScope.surface` can do with
a `when (this)` — precedent at `Column.kt:163`. Safe now the matrix can prove it
behaviour-preserving. Rule is already in `awake-ui-authoring`.

**1.4 Extend the matrix** to `row`, `box`, `scrollPanel`. Expect the scroll no-ops to surface as
their own cells. 16 cells found 8 defects; this is the highest-value test work left.

*Done when:* matrix green across all containers, no wrapper workarounds left in recipes.

---

## Phase 2 — the reported UI bugs

Independent of each other; 2.1 and 2.2 may share a root cause.

**2.1 Radio group not clickable.** Suspected same `activeId` claim problem as the
toggle-group-blocks-popup blocker — a group swallowing its children's clicks. Check them together
before treating as two bugs.

**2.2 Toggle group blocks popups.** Pre-existing. Bisected already: wrapper alone passes, empty
header row passes, one toggle group fails. Instrument `activeId` across frames before changing
anything.

**2.3 Broken OTP input.** No diagnosis yet.

**2.4 Text not visible in icons/badge.** Needs a specific component from the reporter — the text
fix this session only touched the single-line centred path.

*Done when:* each has a `ui-core` or `ui-designsystem` regression test that fails without its fix.

---

## Phase 3 — showcase structure

**3.1 One reusable demo-page container:** title, subtitle, description, preview/code toggle,
consistent spacing. Kills the inconsistent container sizes and the over-weighted Notes section.

**3.2 Render the code tab from the same source as the preview**, so "code doesn't match preview"
cannot recur. This is what makes 3.1 worth doing over cosmetic fixes.

**3.3 Missing component previews** — fill the gaps once the container exists.

---

## Phase 4 — test hygiene

Audit already run on `ui-designsystem`; it is **not** the mess the file count suggests. 51 files,
244 assertions, only 2 files assert nothing but existence and only 32 assertions are misplaced
geometry.

**4.1 Delete** `ShadcnComboboxTest`, `ShadcnTabsFidelityTest` — existence-only, and the latter
claims fidelity while passing with every size and colour wrong.

**4.2 Move ~32 geometry assertions to `ui-core`** as matrix rows — `ShadcnBadgeIntrinsicWidthTest`
(6/6 geometry) and `DropdownMenuIntrinsicWidthTest` first.

**4.3 Collapse the sidebar trio** once 1.1 lands.

**4.4 Audit `ui-headless` (42 files) and `ui-showcase` (17)** — unmeasured. Expect `ui-showcase`
to hold the most waste per unit of maintenance, since each test needs a render and baselines.

---

## Standing rules (earned this session, in the skills)

- **Exact values, never thresholds.** Four sidebar tests passed against a visibly broken sidebar
  asserting "more than 48px".
- **A matrix beats hand-picked cases.** 4 targeted tests found nothing in a session; 12 cells
  found 8 defects in one run. New cases are ROWS.
- **Instrument before hypothesising.** Every guess this session was wrong; every measurement
  landed.
- **One rule, one place.** Duplication per scope/container caused four separate bugs here.
- **Loud over silent.** A dropped modifier must throw. Three defects survived by being ignored
  quietly.
- **Verify the task name exists** — `compileDesktopTestKotlin` does not, and Gradle's "task not
  found" reads as a clean compile under a `grep '^e:'` filter.
- **Scope the formatter** — `spotlessApply` hit 36 and then 48 unrelated files.
