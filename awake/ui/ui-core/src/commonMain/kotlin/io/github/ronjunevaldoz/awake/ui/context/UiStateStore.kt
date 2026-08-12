// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.context

import io.github.ronjunevaldoz.awake.ui.WidgetState

internal class UiStateStore {
    private val widgetStates = HashMap<String, WidgetState>()

    fun widgetState(id: String): WidgetState = widgetStates.getOrPut(id) { WidgetState() }
}
