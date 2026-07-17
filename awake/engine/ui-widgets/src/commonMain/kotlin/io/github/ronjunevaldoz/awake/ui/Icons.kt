// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

/** Small built-in vector icons shared by widgets that need an affordance glyph (e.g.
 * [dropdown]'s expand indicator) without depending on an external icon font/asset pipeline. */
object UiIcons {
    /** Solid downward-pointing triangle -- the classic native-select expand affordance,
     * chosen over a stroked chevron polyline since [UiImageVector]/[icon] only fill paths. */
    val chevronDown: UiImageVector = uiImageVector(
        defaultWidth = 10f.dp,
        defaultHeight = 6f.dp,
        viewportWidth = 10f,
        viewportHeight = 6f
    ) {
        path {
            moveTo(0f, 0f)
            lineTo(10f, 0f)
            lineTo(5f, 6f)
            close()
        }
    }
}
