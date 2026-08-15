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
import io.github.ronjunevaldoz.awake.ui.style.Style
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
                surface = Style { background(Color.White); shape(4f.dp) },
            ),
        ) { }

        assertEquals(UiBounds(50f, 20f, 100f, 80f), result.slot)
        val output = context.finishFrame()
        assertTrue(output.primitives.filterIsInstance<UiDrawPrimitive.Quad>().any { it.color == Color.Black })
        assertTrue(
            output.primitives.any { primitive ->
                (primitive is UiDrawPrimitive.RoundedQuad && primitive.color == Color.White) ||
                    (primitive is UiDrawPrimitive.Quad && primitive.color == Color.White)
            },
            "expected a white surface quad/rounded-quad in output primitives: ${output.primitives}",
        )
    }
}
