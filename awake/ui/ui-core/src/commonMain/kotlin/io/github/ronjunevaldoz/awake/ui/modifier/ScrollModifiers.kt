// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.modifier

import io.github.ronjunevaldoz.awake.ui.UiScrollConfig
import io.github.ronjunevaldoz.awake.ui.UiScrollState

fun UiModifier.verticalScroll(
    state: UiScrollState,
    config: UiScrollConfig = UiScrollConfig.Default,
): UiModifier = copy(
    scrollState = state,
    scrollConfig = config,
)

fun UiModifier.horizontalScroll(
    state: UiScrollState,
    config: UiScrollConfig = UiScrollConfig.Default,
): UiModifier = copy(
    scrollState = state,
    scrollConfig = config,
)
