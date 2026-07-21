// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.testing.ui

import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.UiContext
import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiTheme
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.forceActive
import io.github.ronjunevaldoz.awake.ui.forceFocus
import io.github.ronjunevaldoz.awake.ui.forceHover

/**
 * Automates the rendering of a component in multiple interaction states using forced modifiers.
 */
fun AwakeUiPreviewMetadata.componentStateMatrix(
    font: UiFont = UiFonts.default(),
    theme: UiTheme? = null,
    block: ColumnScope.(UiModifier) -> Unit
): List<AwakeUiPreviewSample> {
    val states = listOf(
        "default" to UiModifier(),
        "hover" to UiModifier().forceHover(),
        "active" to UiModifier().forceActive(),
        "focus" to UiModifier().forceFocus()
    )

    return states.map { (idSuffix, forcedModifier) ->
        val ui = UiContext()
        ui.beginFrame(width.toFloat(), height.toFloat(), UiInputState())
        ui.createColumn(
            x = 0f,
            y = 0f,
            width = width.toFloat(),
            font = font,
            theme = theme ?: io.github.ronjunevaldoz.awake.ui.CoreUiTheme,
        ).block(forcedModifier)
        sample(
            idSuffix = idSuffix,
            titleSuffix = idSuffix.replaceFirstChar { it.uppercase() },
            frame = AwakeUiPreviewFrame(
                primitives = ui.endFrame(),
                background = (theme ?: io.github.ronjunevaldoz.awake.ui.CoreUiTheme).tokens.background,
                font = font,
                semantics = ui.semanticNodes()
            )
        )
    }
}
