// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.UiShapeSpec
import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiLocal
import io.github.ronjunevaldoz.awake.ui.context.uiLocalOf
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.UiModifier
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.UiSeparatorOrientation
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.surface
import io.github.ronjunevaldoz.awake.ui.headless.widthIn
import io.github.ronjunevaldoz.awake.ui.headless.wrapContentWidthOrDefault
import io.github.ronjunevaldoz.awake.ui.style.Style

enum class ShadcnButtonGroupOrientation {
    Horizontal,
    Vertical,
}

/**
 * Threads orientation and per-member corner-rounding position down to [shadcnButton] and
 * [shadcnButtonGroupSeparator] -- an ambient local, not an explicit `content` parameter, because
 * the group's `content: UiScope.() -> Unit` shape (a bare composition block, matching every
 * other headless container) has no per-child slot for a caller to pass an index through.
 *
 * [memberCount]/[nextIndex] are populated across the SAME frame's own trial-then-real dispatch
 * (see [io.github.ronjunevaldoz.awake.ui.context.UiContext.wrapContentPass]) -- `content` always
 * runs once as a WrapContent sizing trial before the real render, so the trial pass alone already
 * knows how many members exist by the time the real pass needs to color the first/last corners.
 * Never a count remembered from a PRIOR frame -- this instance is constructed fresh inside
 * [shadcnButtonGroup] every call, so a structural change (a caller adding/removing a button)
 * can never leave a stale corner behind.
 */
internal class ShadcnButtonGroupContext(val orientation: ShadcnButtonGroupOrientation) {
    private var memberCount: Int = 0
    private var nextIndex: Int = 0

    /** Called once per [shadcnButton]/[shadcnIcon] claim. Returns this member's 0-based
     * position during the real pass, or -1 during the counting trial (nothing paints then, so
     * the corner shape computed from it is never observed). */
    fun registerMember(isWrapContentPass: Boolean): Int {
        if (isWrapContentPass) {
            memberCount++
            return -1
        }
        return nextIndex++
    }

    /** [radius] on the group's own outer corner(s) for a member at [index], `0.dp` on every
     * edge that meets a neighbour -- shadcn's `[&>*:not(:first-child)]:rounded-l-none` etc.,
     * ported to explicit index math since this engine has no CSS-style structural selectors. */
    fun cornerShape(index: Int, radius: Dp): UiShapeSpec {
        val isFirst = index == 0
        val isLast = index == memberCount - 1
        val none = 0f.dp
        return when (orientation) {
            ShadcnButtonGroupOrientation.Horizontal -> UiShapeSpec.RoundedCorners(
                topLeft = if (isFirst) radius else none,
                topRight = if (isLast) radius else none,
                bottomRight = if (isLast) radius else none,
                bottomLeft = if (isFirst) radius else none,
            )

            ShadcnButtonGroupOrientation.Vertical -> UiShapeSpec.RoundedCorners(
                topLeft = if (isFirst) radius else none,
                topRight = if (isFirst) radius else none,
                bottomRight = if (isLast) radius else none,
                bottomLeft = if (isLast) radius else none,
            )
        }
    }
}

internal val LocalShadcnButtonGroup: UiLocal<ShadcnButtonGroupContext?> = uiLocalOf(null)

/** Registers the calling [shadcnButton]/[shadcnIcon] as the next member of [groupCtx] and
 * returns the per-corner [Style] it should paint -- `Style.Empty` during the counting trial
 * (see [ShadcnButtonGroupContext.registerMember]), where nothing observes it anyway. */
internal fun UiScope.cornerStyle(groupCtx: ShadcnButtonGroupContext): Style {
    val index = groupCtx.registerMember(primitive.context.isWrapContentPassInternal())
    if (index < 0) return Style.Empty
    return Style { shape(groupCtx.cornerShape(index, themeValues.shapes.md)) }
}

internal fun UiScope.pushLocal(
    local: UiLocal<ShadcnButtonGroupContext?>,
    value: ShadcnButtonGroupContext,
    content: () -> Unit,
) {
    primitive.context.pushLocal(local, value)
    try {
        content()
    } finally {
        primitive.context.popLocal(local)
    }
}

internal fun UiScope.currentLocal(
    local: UiLocal<ShadcnButtonGroupContext?>,
): ShadcnButtonGroupContext? = primitive.context.current(local)

/**
 * shadcn's `ButtonGroup`: buttons joined into one control, sharing a single border and outer
 * radius.
 *
 * Ported from `registry/new-york-v4/ui/button-group.tsx` in the pinned shadcn checkout.
 * Supports both [ShadcnButtonGroupOrientation.Horizontal] and
 * [ShadcnButtonGroupOrientation.Vertical] orientations.
 */
fun UiScope.shadcnButtonGroup(
    id: String,
    modifier: UiModifier = Modifier,
    orientation: ShadcnButtonGroupOrientation = ShadcnButtonGroupOrientation.Horizontal,
    content: UiScope.() -> Unit,
): UiBounds = groupSurface(id, orientation, modifier) {
    pushLocal(LocalShadcnButtonGroup, ShadcnButtonGroupContext(orientation)) {
        // wrapContentWidthOrDefault() is load-bearing, not decorative: this row/column is nested
        // inside a ColumnScope receiver, so it resolves through ColumnScope.row()/column()'s own
        // ui-core wrapper (Column.kt/Row.kt), whose width DEFAULT for a child hosted in a column
        // is FillMax (the cross axis fills its parent unless the child opts out) -- exactly the
        // opposite of what a wrap-content group needs. This explicitly opts back out to
        // WrapContent, the same override withIntrinsicLabelWidth's own reportsNaturalWidthDuringWrapTrial
        // branch (see IntrinsicSizing.kt) relies on to see FillMax on the *members* specifically,
        // not the group's own outer container.
        when (orientation) {
            ShadcnButtonGroupOrientation.Horizontal -> row(
                horizontalArrangement = Arrangement.spacedBy(0f.dp),
                verticalAlignment = UiAlignment.Vertical.Center,
                modifier = Modifier.wrapContentWidthOrDefault(),
            ) { content() }

            ShadcnButtonGroupOrientation.Vertical -> column(
                verticalArrangement = Arrangement.spacedBy(0f.dp),
                modifier = Modifier.wrapContentWidthOrDefault(),
            ) { content() }
        }
    }
}

/** Vertical icon-only groups (shadcn's zoom controls, the reference this ports from) have no
 * label to give the group a real tap-target width -- shadcn's own CSS sets a baseline `size-9`
 * (36dp) member width for exactly this case. Applied unconditionally here (not a caller-tunable
 * param -- ownership rule 6, `Modifier.widthIn(min=)` already expresses a caller override). */
private val verticalIconGroupMinWidth = 36f.dp

private fun UiScope.groupSurface(
    id: String,
    orientation: ShadcnButtonGroupOrientation,
    modifier: UiModifier,
    content: ColumnScope.() -> Unit,
): UiBounds = surface(
    id = id,
    modifier = modifier,
    style = Style {
        background(themeValues.colors.card)
        foreground(themeValues.colors.cardForeground)
        border(1f.dp, themeValues.colors.border)
        shape(themeValues.shapes.md)
        // Zero, as in shadcn: the children ARE the control's edges. Each member now carries its
        // own per-corner shape (see ShadcnButtonGroupContext.cornerShape) instead of the group
        // itself painting one uniform radius, so this container never needs its own visible
        // rounding -- a filled end member's real corner already matches the group's outer arc.
        contentPadding(0f.dp)
    },
) { content() }

/**
 * Hairline divider between members of a group.
 * Automatically reads [LocalShadcnButtonGroup] to pick [UiSeparatorOrientation.Vertical]
 * for horizontal groups and [UiSeparatorOrientation.Horizontal] for vertical groups.
 */
fun UiScope.shadcnButtonGroupSeparator(
    id: String? = null,
    modifier: UiModifier = Modifier,
): UiBounds {
    val groupCtx = currentLocal(LocalShadcnButtonGroup)
    val separatorOrientation = when (groupCtx?.orientation) {
        ShadcnButtonGroupOrientation.Vertical -> UiSeparatorOrientation.Horizontal
        else -> UiSeparatorOrientation.Vertical
    }
    return shadcnSeparator(
        id = id,
        modifier = modifier,
        orientation = separatorOrientation,
    )
}
