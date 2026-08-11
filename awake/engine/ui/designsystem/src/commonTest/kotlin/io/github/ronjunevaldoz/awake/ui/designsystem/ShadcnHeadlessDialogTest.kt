// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.shadcnDialog
import io.github.ronjunevaldoz.awake.ui.headless.createUiScope
import kotlin.test.Test
import kotlin.test.assertTrue

class ShadcnHeadlessDialogTest {
    @Test
    fun facadeDialogResolvesShadcnTokensBeforeCallingHeadless() {
        val context = UiContext()
        context.pushTheme(ShadcnTheme)
        context.beginFrame(200f, 120f, UiInputState())

        context.createUiScope(UiBounds(0f, 0f, 200f, 120f)).shadcnDialog(
            id = "confirm",
            expanded = true,
            width = Dimension.Fixed(100f.dp),
            height = Dimension.Fixed(80f.dp),
        ) { }

        assertTrue(
            context.endFrame()
                .filterIsInstance<UiDrawPrimitive.RoundedQuad>()
                .any { it.color == ShadcnTheme.colors.card },
            "the branded surface must resolve in the design-system adapter",
        )
    }
}
