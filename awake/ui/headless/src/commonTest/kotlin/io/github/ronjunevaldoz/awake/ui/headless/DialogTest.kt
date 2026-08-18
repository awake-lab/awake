// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.testing.ui.renderUiComponent
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.api.UiPopupResult
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.style.Style
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DialogTest {
    @Test
    fun dialogCentersItsPopupAndUsesCallerSuppliedNeutralVisuals() {
        var result: UiPopupResult? = null
        val output = renderUiComponent(width = 200f, height = 120f) {
            result = dialog(
                id = "confirm", expanded = true, width = Dimension.Fixed(100f.dp), height = Dimension.Fixed(80f.dp),
                style = Style { background(Color.White); shape(4f.dp) },
                properties = DialogProperties(showScrim = true, scrimColor = Color.Black),
            ) { }
        }

        assertEquals(UiBounds(50f, 20f, 100f, 80f), result?.slot)
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
