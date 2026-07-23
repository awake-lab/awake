// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.layouts.AbsoluteScope
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.layouts.baseSpacingPx
import io.github.ronjunevaldoz.awake.ui.layouts.defaultArrangement
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot

/**
 * Nested-scope factories that inherit the receiver's overlay behavior automatically.
 */
fun UiScope.childColumn(
    slot: UiSlot,
    verticalArrangement: Arrangement = defaultArrangement(),
    modifier: UiModifier = UiModifier(),
    hasBoundedFillWidth: Boolean = true,
    hasBoundedFillHeight: Boolean = true
): ColumnScope = context.createColumn(
    slot = slot,
    gap = verticalArrangement.baseSpacingPx(),
    insets = modifier.insets,
    verticalArrangement = verticalArrangement,
    testTag = modifier.testTag,
    hasBoundedFillWidth = hasBoundedFillWidth,
    hasBoundedFillHeight = hasBoundedFillHeight,
    overlayOnly = emitsToOverlay
)

fun UiScope.childRow(
    slot: UiSlot,
    horizontalArrangement: Arrangement = defaultArrangement(),
    modifier: UiModifier = UiModifier(),
    hasBoundedFillWidth: Boolean = true,
    hasBoundedFillHeight: Boolean = true
): RowScope = context.createRow(
    slot = slot,
    gap = horizontalArrangement.baseSpacingPx(),
    insets = modifier.insets,
    horizontalArrangement = horizontalArrangement,
    testTag = modifier.testTag,
    hasBoundedFillWidth = hasBoundedFillWidth,
    hasBoundedFillHeight = hasBoundedFillHeight,
    overlayOnly = emitsToOverlay
)

fun UiScope.childAbsolute(
    slot: UiSlot,
    modifier: UiModifier = UiModifier()
): AbsoluteScope = context.createAbsolute(
    slot = slot,
    insets = modifier.insets,
    testTag = modifier.testTag,
    overlayOnly = emitsToOverlay
)

fun UiScope.childBox(
    slot: UiSlot,
    modifier: UiModifier = UiModifier(),
    contentAlignment: UiAlignment = UiAlignment.TopStart,
    hasBoundedFillWidth: Boolean = true,
    hasBoundedFillHeight: Boolean = true
): BoxScope = context.createBox(
    slot = slot,
    insets = modifier.insets,
    contentAlignment = contentAlignment,
    testTag = modifier.testTag,
    hasBoundedFillWidth = hasBoundedFillWidth,
    hasBoundedFillHeight = hasBoundedFillHeight,
    overlayOnly = emitsToOverlay
)
