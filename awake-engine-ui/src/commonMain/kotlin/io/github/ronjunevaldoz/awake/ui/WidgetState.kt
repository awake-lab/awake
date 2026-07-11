// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

/** Per-widget-id state a [UiContext] keeps across frames -- currently only used by
 * [UiContext.dropdown] to remember whether it's expanded. */
class WidgetState {
    var boolFlag: Boolean = false
}
