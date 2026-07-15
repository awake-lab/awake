# /awake:review-ui-snapshots $ARGUMENTS

Analyze `awake:engine:ui`'s pixel-baseline UI snapshot gallery for legibility/occlusion
issues, using vision on the raw PNGs. This is the visual-review tool the design-system
work (shadcn-style variants, dark/light theme) explicitly needs — the widgets are
rasterized without a real GPU (see `snapshot/UiRasterizer.kt`'s doc comment), so no
numeric assertion catches "the label is covered by its own check-fill" the way this
audit does.

Search root: `$ARGUMENTS` (defaults to `.` — the current project root)

---

## Step 1 — Run the snapshot tests

```bash
./gradlew :awake:engine:ui:desktopTest
```

This regenerates every PNG under `awake/engine/ui/build/ui-snapshots/*.png` and, via the
`uiSnapshotReport` task (`finalizedBy desktopTest`), a self-contained HTML gallery at
`awake/engine/ui/build/reports/ui-snapshots/index.html` -- unconditionally, pass or fail.

---

## Step 2 — Find the snapshots

```bash
find "${ARGUMENTS:-.}/awake/engine/ui/build/ui-snapshots" -name "*.png" | sort
```

If none found: print `No UI snapshots found -- did desktopTest run?` and stop.

---

## Step 3 — For each snapshot, run a visual review

Read each PNG with vision. Remember the rasterizer is deliberately crude (flat rects, no
real glyph shapes, no real texture sampling -- see `UiRasterizer.kt`) so judge what it
actually can show: **relative color contrast and occlusion between primitives**, not font
rendering quality or corner rounding.

### Label visibility
- [ ] A widget's label glyph blocks (small inset rects within a larger colored rect) are
      visible against their background quad -- flag if the glyph color and background
      color look nearly identical (low contrast) or if a later-drawn quad appears to sit
      directly on top of where the label should be (occlusion, not just low contrast --
      compare an "on"/"checked"/"active" variant against its "off"/"unchecked" counterpart
      of the same widget to tell which case it is)
- [ ] Text is legible against every theme variant present in the gallery, not just the
      default one

### Theme/variant consistency
- [ ] Each named variant (e.g. `*-checked` vs `*-unchecked`, or future `*-danger`/
      `*-outline`/`*-ghost`/`*-dark`/`*-light` snapshots) is visually distinguishable from
      its siblings -- flag if two differently-named snapshots look identical (the theme
      swap may not actually be reaching the widget)
- [ ] No snapshot is solid one flat color edge-to-edge with nothing else visible (usually
      means every primitive drew fully transparent, fully occluded, or off-canvas)

### Clip/bounds sanity
- [ ] No primitive appears to bleed outside the snapshot's own canvas edges in a way that
      looks unintentional (could indicate a `claimSlot`/layout math regression)

---

## Step 4 — Output

For each snapshot:

```
SNAPSHOT: toggle-checked.png

  ⚠️  Label visibility — check-fill quad appears to fully cover the label glyph blocks
                         visible in toggle-unchecked.png; likely occlusion, not just contrast
  ✅ Theme consistency  — distinguishable from toggle-unchecked.png (fill color differs)
  ✅ Clip/bounds        — nothing bleeds outside canvas
```

Aggregate summary:

```
UI SNAPSHOT REVIEW: <N> snapshots

  PASS:    <N>
  WARNING: <N>   (contrast concerns, worth a closer look)
  FAIL:    <N>   (label fully occluded/invisible, variant indistinguishable from another)

RESULT: PASS | NEEDS ATTENTION
```

---

## Step 5 — Recommended fixes

For each WARNING or FAIL, tie it to the actual widget code, not a generic suggestion:

| Finding | Fix | File |
|---|---|---|
| Label occluded by a state-fill quad | Draw the state-fill quad BEFORE the label glyph (reorder `emit()` calls), or shrink/reposition the fill so it doesn't overlap the label's centered region | `Widgets.kt` |
| Low-contrast label vs background | Pick a `foreground`/`labelColor` token with better contrast against that specific `UiStyle`'s resolved background for that state | `UiTheme.kt` |
| Two variants look identical | Confirm the theme/style parameter is actually threaded into the widget call in the snapshot test, not defaulting to `DefaultUiTheme` | the snapshot test itself (`UiSnapshotTest.kt` or its successor) |

---

## Notes

- This is a supplement to, not a replacement for, `awake:engine:ui`'s own pass/fail unit
  tests (`UiContextTest.kt`, `LayoutTest.kt`, etc, kept in a separate package on purpose —
  see the session decision to keep unit tests and snapshot/visual tests apart).
- Use [docs/reference/ui-ownership.md](/Users/ronvaldoz/StudioProjects/awaken/docs/reference/ui-ownership.md)
  as the canonical placement guide when routing a fix to `ui-core`, `ui-widgets`, `ui`, or
  `ui-designsystem`.
- Run this after adding a new theme, widget variant, or state (checked/hovered/active/
  disabled) to `UiSnapshotTest.kt` (or wherever new snapshot tests land), or after any
  change to `Widgets.kt`'s emit order.
- If the gallery is empty or stale, re-run Step 1 -- `uiSnapshotReport` always regenerates
  from whatever is currently in `build/ui-snapshots/`, so a stale gallery means the test
  task didn't run, not that the tool is broken.
