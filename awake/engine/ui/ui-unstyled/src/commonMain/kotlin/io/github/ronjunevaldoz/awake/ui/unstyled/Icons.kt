// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.unstyled

import io.github.ronjunevaldoz.awake.ui.UiImageVector
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.uiImageVector

/** Small built-in vector icons shared by widgets that need an affordance glyph (e.g.
 * [io.github.ronjunevaldoz.awake.ui.unstyled.input.dropdown]'s expand indicator) without depending on an external icon font/asset pipeline. */
object UiIcons {
    /** Solid downward-pointing triangle -- the classic native-select expand affordance,
     * chosen over a stroked chevron polyline since [io.github.ronjunevaldoz.awake.ui.UiImageVector]/[io.github.ronjunevaldoz.awake.ui.unstyled.components.icon] only fill paths. */
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

    /** [chevronDown] flipped vertically -- the collapsed/expanded toggle affordance for
     * [io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCollapsible]'s trigger. */
    val chevronUp: UiImageVector = uiImageVector(
        defaultWidth = 10f.dp,
        defaultHeight = 6f.dp,
        viewportWidth = 10f,
        viewportHeight = 6f
    ) {
        path {
            moveTo(0f, 6f)
            lineTo(10f, 6f)
            lineTo(5f, 0f)
            close()
        }
    }
}
