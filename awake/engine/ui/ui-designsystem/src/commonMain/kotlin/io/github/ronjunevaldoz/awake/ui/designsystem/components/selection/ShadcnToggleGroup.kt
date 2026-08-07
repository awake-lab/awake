// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components.selection

import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.input.toggle.toggleGroup
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier

/** Real shadcn's `ToggleGroup` multi-select form: toggles in [selectedIndices] can be active
 * simultaneously (e.g. bold+italic both pressed). Delegates entirely to [toggleGroup]. */
fun UiScope.shadcnToggleGroup(
    id: String,
    options: List<String>,
    selectedIndices: Set<Int>,
    modifier: UiModifier = Modifier,
    onSelectedIndicesChange: (Set<Int>) -> Unit = {},
) = toggleGroup(id, options, selectedIndices, modifier, onSelectedIndicesChange)

/** Real shadcn's `ToggleGroup` single-select convenience form: a row of mutually exclusive
 * [shadcnToggle]-style buttons. Delegates entirely to [toggleGroup]. */
fun UiScope.shadcnToggleGroup(
    id: String,
    options: List<String>,
    selectedIndex: Int,
    modifier: UiModifier = Modifier,
    onIndexChange: (Int) -> Unit = {},
) = toggleGroup(id, options, selectedIndex, modifier, onIndexChange)
