# Button group: intrinsic cross-axis sizing + per-corner radii

Handoff for a fresh session. Reference implementation is the user's own
[shadcn-compose `ShadcnButtonGroup.kt`](https://github.com/ronjunevaldoz/shadcn-compose);
the fix is to make Awake's group the same plain `row`/`column` it is there.

## Root cause

The group leans on modifier tricks (`widthIn(min)`, `wrapContentWidthOrDefault()`,
per-child `fillMaxWidth()`/`fillMaxHeight()`) to stand in for two primitives the UI stack
does not have. In Compose the same component needs neither: a row/column plus
`IntrinsicSize.Min` on the cross axis, and `RoundedCornerShape` per member.

## Look at it first

```bash
./gradlew :awake:ui:designsystem:desktopTest --tests "*ShadcnButtonGroupCaptureTest*" --rerun-tasks
```

Writes `awake/ui/designsystem/build/ui-snapshots/button-group.png`. Three defects are visible
there, and only the second was known when this doc was first written:

1. The HORIZONTAL group renders no labels at all. This is the variant used as the studio toolbar
   pill. Most likely the same root as (2) — `withIntrinsicLabelWidth` returns early because the
   group put `fillMaxWidth()`/`fillMaxHeight()` on the member — so fixing it may fix both.
2. The vertical group fills its frame instead of wrapping.
3. Members do not fill the group's height; the card shows through as a band underneath.

## Done

`UiShapeSpec.RoundedCorners(topLeft, topRight, bottomRight, bottomLeft)` — commit `fe90a848`.
Filled path, not a `RoundedQuad` (that SDF carries one radius) and not a clip (both backends
clip with a rectangular scissor, and clipping would eat children's focus rings — same reason
the reference gives at its line 147). Each corner clamps to half the shorter side on its own.
Covered by `UiPathTest.roundedCorners*`.

Nothing consumes it yet, so there is no visual change to look at.

## Next: let a FillMax child report its intrinsic size to a WrapContent parent

No new `Dimension` is needed — an earlier draft of this doc proposed `Dimension.IntrinsicMin`
before the root cause was located. The bug is in the measure pass, at `ColumnScope.claimSlot`:

```kotlin
context.recordMeasuredSlot(slot, contributesToWrapWidth = width != Dimension.FillMax)
```

Every member of a vertical group is `fillMaxWidth()`, so every child is excluded, the column
measures 0 wide, and `wrapContentWidthOrDefault()` falls back to the frame — hence 600px.

That exclusion is right for a BOUNDED column and wrong for a wrap-content one. `IntrinsicSize.Min`
is exactly this distinction: a `fillMaxWidth` child still reports its minimum intrinsic width
upward, then fills whatever the parent resolves to. During a wrap-content measure pass, resolve a
`FillMax` child as `WrapContent` so it contributes; symmetric in `RowScope` for height.

Fixes every wrap+fill combination, not just this component. Spec is already in the tree as a
failing test: `verticalButtonGroupWrapsContentWidthAndButtonsFillMaxWidth` (pre-existing failure,
confirmed by stashing).

The group then becomes a plain `row`/`column`; delete `minWidth`, `widthIn`, and
`wrapContentWidthOrDefault`. Keep the per-child `fillMax*` — that is what the reference does, and
it is what makes the members share the resolved cross-axis size.

## Then: corners

Assign by index, outer radius on the outer side and `0.dp` where a member meets a neighbour
(reference lines 99 / 200 / 221). This needs the child COUNT, which the group cannot know until
its content lambda has run — with a bounded cross axis it falls out of the measure pass. Do not
reach for a count remembered from the previous frame; a frame-lagged corner is worse than the
overhang it replaces.

## Watch out

- Stage commits by explicit file path. `git add <dir>` sweeps the in-flight designsystem work.
- Gradle reports UP-TO-DATE tasks as silent success. Use `--rerun-tasks` when a clean result matters.
- Pre-existing failures, not yours: 3 `shadcnTheme`-scope UI capture tests, designsystem detekt,
  and 2 `MagicNumber:UiPath.kt$15f` findings in graphics detekt.
