// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnProgress
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.width
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ShadcnProgressRecipeTest {

    @Test
    fun progressUsesBorderlessPrimaryTwentyPercentTrack() {
        val theme = shadcnTheme(dark = false)
        val ui = UiContext()
        ui.pushTheme(theme)
        ui.beginFrame(240f, 32f, testSnapshot(x = -100f, y = -100f, down = false))

        ui.headlessRoot().shadcnProgress(
            id = "progress",
            value = 0f,
            modifier = Modifier.width(212f.dp),
        )

        val primitives = ui.finishFrame().primitives
        val track = primitives.filterIsInstance<UiDrawPrimitive.RoundedQuad>().single()
        assertEquals(theme.colors.primary.withAlpha(0.2f), track.color)
        assertEquals(8f, track.h)
        assertTrue(
            primitives.none { primitive ->
                primitive is UiDrawPrimitive.StrokedPath ||
                    (primitive is UiDrawPrimitive.RoundedQuad && primitive !== track && primitive.color == Color.Transparent)
            },
            "shadcn Progress has no input-style border",
        )
    }
}
