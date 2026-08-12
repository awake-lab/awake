// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DialogTest {
    @Test
    fun dialogCentersItsPopupAndUsesCallerSuppliedNeutralVisuals() {
        val context = UiContext()
        context.beginFrame(200f, 120f, UiInputState())
        val scope = context.createUiScope(UiBounds(0f, 0f, 200f, 120f))

        val result = scope.dialog(
            id = "confirm",
            expanded = true,
            width = Dimension.Fixed(100f.dp),
            height = Dimension.Fixed(80f.dp),
            properties = DialogProperties(
                showScrim = true,
                scrimColor = Color.Black,
                surface = SurfaceStyle(background = Color.White, cornerRadius = 4f.dp),
            ),
        ) { }

        assertEquals(UiBounds(50f, 20f, 100f, 80f), result.slot)
        val primitives = context.endFrame()
        assertTrue(primitives.filterIsInstance<UiDrawPrimitive.Quad>().any { it.color == Color.Black })
        assertTrue(primitives.filterIsInstance<UiDrawPrimitive.RoundedQuad>().any { it.color == Color.White })
    }
}
