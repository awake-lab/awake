// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
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
fun UiScope.childColumn(
    slot: UiBounds,
    verticalArrangement: Arrangement = defaultArrangement(),
    modifier: UiModifier = Modifier,
    hasBoundedFillWidth: Boolean = true,
    hasBoundedFillHeight: Boolean = true,
    horizontalAlignment: UiAlignment.Horizontal = UiAlignment.Horizontal.Start,
): ColumnScope = context.createColumn(
    slot = slot,
    insets = modifier.insets,
    verticalArrangement = verticalArrangement,
    testTag = modifier.testTag,
    hasBoundedFillWidth = hasBoundedFillWidth,
    hasBoundedFillHeight = hasBoundedFillHeight,
    overlayOnly = emitsToOverlay,
    horizontalAlignment = horizontalAlignment,
)

fun UiScope.childRow(
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

fun UiScope.childAbsolute(
    slot: UiBounds,
    modifier: UiModifier = Modifier,
): AbsoluteScope = context.createAbsolute(
    slot = slot,
    insets = modifier.insets,
    testTag = modifier.testTag,
    overlayOnly = emitsToOverlay,
)

fun UiScope.childBox(
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
