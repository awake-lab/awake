// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.layouts.AbsoluteScope
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.layouts.defaultArrangement
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier

/**
 * Nested-scope factories that inherit the receiver's overlay behavior automatically.
 */
fun UiPrimitiveScope.childColumn(
    slot: UiBounds,
    verticalArrangement: Arrangement = defaultArrangement(),
    modifier: UiModifier = Modifier,
    hasBoundedFillWidth: Boolean = true,
    hasBoundedFillHeight: Boolean = true,
    horizontalAlignment: UiAlignment.Horizontal = UiAlignment.Horizontal.Start,
    /** Final per-child slots with weights already resolved -- see planWeightedColumnSlots(). */
    plannedSlots: List<UiBounds>? = null,
): ColumnScope = context.createColumn(
    slot = slot,
    insets = modifier.insets,
    verticalArrangement = verticalArrangement,
    testTag = modifier.testTag,
    hasBoundedFillWidth = hasBoundedFillWidth,
    hasBoundedFillHeight = hasBoundedFillHeight,
    plannedSlots = plannedSlots,
    overlayOnly = emitsToOverlay,
    horizontalAlignment = horizontalAlignment,
)

fun UiPrimitiveScope.childRow(
    slot: UiBounds,
    horizontalArrangement: Arrangement = defaultArrangement(),
    modifier: UiModifier = Modifier,
    hasBoundedFillWidth: Boolean = true,
    hasBoundedFillHeight: Boolean = true,
    verticalAlignment: UiAlignment.Vertical = UiAlignment.Vertical.Top,
): RowScope = context.createRow(
    slot = slot,
    insets = modifier.insets,
    horizontalArrangement = horizontalArrangement,
    testTag = modifier.testTag,
    hasBoundedFillWidth = hasBoundedFillWidth,
    hasBoundedFillHeight = hasBoundedFillHeight,
    overlayOnly = emitsToOverlay,
    verticalAlignment = verticalAlignment,
)

fun UiPrimitiveScope.childAbsolute(
    slot: UiBounds,
    modifier: UiModifier = Modifier,
): AbsoluteScope = context.createAbsolute(
    slot = slot,
    insets = modifier.insets,
    testTag = modifier.testTag,
    overlayOnly = emitsToOverlay,
)

/**
 * Runs a composite widget's own content so its descendants' slot/weight claims stay out of the
 * enclosing row/column's child list.
 *
 * `surface`/`column`/`row`/`box` already do this internally. A composite built from
 * [childAbsolute] -- a button with a caller-composed slot, say -- has no such wrapper, and every
 * node it draws is counted as a direct sibling of the widget itself. That shifts
 * `measuredSlots` out of step with `measuredWeights`, so a weighted sibling further down the
 * column is paired with someone else's slot and resolves to nothing.
 */
fun <T> UiPrimitiveScope.compositeContent(block: () -> T): T =
    context.withMeasuredRecordingSuppressed(block)

/**
 * Runs content whose interior layout is derived entirely from the bound the widget was given --
 * a scroll viewport's content, a fraction-splitting panel group -- so no descendant claim leaks
 * into an ancestor's WrapContent hugging. Stronger than [compositeContent]: it isolates the
 * wrap-extent trackers too, not just the direct-child slot/weight lists. Without this, a
 * FillMax resizable group measured inside a WrapContent card reported `fraction x trial-bound`
 * as intrinsic width, so dragging its handle re-sized the card itself.
 */
fun <T> UiPrimitiveScope.boundDerivedContent(block: () -> T): T =
    context.withMeasuredSubtreeIsolated(block)

fun UiPrimitiveScope.childBox(
    slot: UiBounds,
    modifier: UiModifier = Modifier,
    contentAlignment: UiAlignment = UiAlignment.TopStart,
    hasBoundedFillWidth: Boolean = true,
    hasBoundedFillHeight: Boolean = true,
): BoxScope = context.createBox(
    slot = slot,
    insets = modifier.insets,
    contentAlignment = contentAlignment,
    testTag = modifier.testTag,
    hasBoundedFillWidth = hasBoundedFillWidth,
    hasBoundedFillHeight = hasBoundedFillHeight,
    overlayOnly = emitsToOverlay,
)
