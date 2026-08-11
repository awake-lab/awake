// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.api.UiPopupState
import io.github.ronjunevaldoz.awake.ui.rememberPopupState as rememberPrimitivePopupState

/** Public popup state contract for Headless recipes and design-system adapters. */
fun UiScope.rememberPopupState(
    id: String,
    key: String = "expanded",
    initial: Boolean = false,
): UiPopupState = primitive.rememberPrimitivePopupState(id, key, initial)
