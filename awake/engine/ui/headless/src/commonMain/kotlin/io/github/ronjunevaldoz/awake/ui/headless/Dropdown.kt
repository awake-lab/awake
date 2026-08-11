// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.unstyled.input.select as primitiveSelect
import io.github.ronjunevaldoz.awake.ui.unstyled.components.contextMenuTrigger as primitiveContextMenuTrigger

/** Generic select behavior with a stable Headless receiver. */
fun UiScope.select(
    id: String,
    options: List<String>,
    selectedIndex: Int? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
): Int? = primitive.primitiveSelect(
    id = id,
    options = options,
    selectedIndex = selectedIndex ?: -1,
    modifier = modifier.asPrimitiveModifier(),
    enabled = enabled,
)

/** Neutral combobox baseline; skins may add a filter row around the same menu contract. */
fun UiScope.combobox(
    id: String,
    options: List<String>,
    selectedIndex: Int? = null,
    enabled: Boolean = true,
): Int? = select(
    id = id,
    options = options,
    selectedIndex = selectedIndex,
    enabled = enabled,
)

/** Secondary-click detection and cursor anchoring without exposing Core's trigger type. */
fun UiScope.contextMenuTrigger(
    id: String,
    expanded: Boolean,
    target: UiBounds,
): HeadlessContextMenuTrigger {
    val trigger = primitive.primitiveContextMenuTrigger(id, expanded, target)
    return HeadlessContextMenuTrigger(trigger.shouldOpen, trigger.anchor)
}

data class HeadlessContextMenuTrigger(
    val shouldOpen: Boolean,
    val anchor: UiBounds,
)
