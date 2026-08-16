// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnDialog
import kotlin.test.Test
import kotlin.test.assertTrue

class ShadcnHeadlessDialogTest {
    @Test
    fun facadeDialogResolvesShadcnTokensBeforeCallingHeadless() {
        val frame = renderShadcnComponent(width = 200f, height = 120f) {
            shadcnDialog(
                id = "confirm",
                expanded = true,
                width = Dimension.Fixed(100f.dp),
                height = Dimension.Fixed(80f.dp),
            ) { }
        }

        assertTrue(
            frame.primitives
                .filterIsInstance<UiDrawPrimitive.RoundedQuad>()
                .any { it.color == ShadcnTheme.colors.card },
            "the branded surface must resolve in the design-system adapter",
        )
    }
}
