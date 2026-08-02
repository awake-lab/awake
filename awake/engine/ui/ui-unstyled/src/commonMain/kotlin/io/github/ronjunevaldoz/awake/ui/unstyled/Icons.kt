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

    /** Same triangle as [chevronDown], mirrored vertically -- used for a collapsible/accordion
     * trigger's expanded state. No rotation support exists for [io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive.FilledPath]
     * yet (only [io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive.Quad]/`RoundedQuad`/`Glyph`/`Texture`
     * carry a `transform`, see `docs/tasks/2026-08-02-graphicslayer-rotation-scale.md`), so this is a
     * static direction-swap, not an animated 180-degree rotation of [chevronDown]. */
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
