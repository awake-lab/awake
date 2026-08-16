// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.testing.ui.UiComponentFrame
import io.github.ronjunevaldoz.awake.testing.ui.renderUiComponent
import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.testSnapshot

/**
 * Design-system test entry point. The generic renderer owns frame setup; this wrapper owns the
 * Shadcn composition boundary, so recipes read the same scoped value production code provides.
 */
internal fun renderShadcnComponent(
    width: Float = 400f,
    height: Float = 400f,
    theme: ShadcnThemeValues = shadcnThemeValues(),
    font: UiFont = UiFonts.default(),
    density: Float = 1f,
    input: UiInputState = testSnapshot(),
    content: UiScope.(root: UiBounds) -> Unit,
): UiComponentFrame = renderUiComponent(
    width = width,
    height = height,
    font = font,
    density = density,
    input = input,
) { root ->
    shadcnTheme(theme = theme) {
        content(root)
    }
}
