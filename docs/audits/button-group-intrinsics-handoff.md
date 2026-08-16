# Button group: intrinsic cross-axis sizing + per-corner radii

Handoff for a fresh session. Reference implementation is the user's own
[shadcn-compose `ShadcnButtonGroup.kt`](https://github.com/ronjunevaldoz/shadcn-compose);
the fix is to make Awake's group the same plain `row`/`column` it is there.

## Root cause

The group leans on modifier tricks (`widthIn(min)`, `wrapContentWidthOrDefault()`,
per-child `fillMaxWidth()`/`fillMaxHeight()`) to stand in for two primitives the UI stack
does not have. In Compose the same component needs neither: a row/column plus
`IntrinsicSize.Min` on the cross axis, and `RoundedCornerShape` per member.

## Done

`UiShapeSpec.RoundedCorners(topLeft, topRight, bottomRight, bottomLeft)` — commit `fe90a848`.
Filled path, not a `RoundedQuad` (that SDF carries one radius) and not a clip (both backends
clip with a rectangular scissor, and clipping would eat children's focus rings — same reason
the reference gives at its line 147). Each corner clamps to half the shorter side on its own.
Covered by `UiPathTest.roundedCorners*`.

Nothing consumes it yet, so there is no visual change to look at.

## Next: `Dimension.IntrinsicMin`

```kotlin
sealed class Dimension {
    data class Fixed(val dp: Dp) : Dimension()
    data object FillMax : Dimension()
    data object WrapContent : Dimension()
    data object IntrinsicMin : Dimension()   // NEW
}
```

`WrapContent` sizes to what children asked for. `IntrinsicMin` sizes to the largest *minimum*
a child needs and then hands that back down as the constraint children resolve `fillMax*`
against — that second half is what fixes the vertical group's width, not the first.

Spec is already in the tree as a failing test:
`verticalButtonGroupWrapsContentWidthAndButtonsFillMaxWidth` (pre-existing failure, confirmed
by stashing).

Then the group becomes:

```kotlin
Horizontal -> row(modifier = Modifier.height(Dimension.IntrinsicMin)) { content() }
Vertical   -> column(modifier = Modifier.width(Dimension.IntrinsicMin)) { content() }
```

Delete `minWidth`, `widthIn`, and `wrapContentWidthOrDefault`.

Whether the per-child `fillMax*` survives depends on how `IntrinsicMin` propagates constraints.
The reference keeps `fillMaxHeight()` on the *separator* precisely because a bounded cross axis
gives it something to resolve against, so expect the same here — but let the measure pass decide.

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
