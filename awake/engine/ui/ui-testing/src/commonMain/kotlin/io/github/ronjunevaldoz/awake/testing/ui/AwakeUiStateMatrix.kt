// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.testing.ui

import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.theme.UiTheme
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.forceActive
import io.github.ronjunevaldoz.awake.ui.modifier.forceFocus
import io.github.ronjunevaldoz.awake.ui.modifier.forceHover

/**
 * Automates the rendering of a component in multiple interaction states using forced modifiers.
 */
fun AwakeUiPreviewMetadata.componentStateMatrix(
    font: UiFont = UiFonts.default(),
    theme: UiTheme? = null,
    block: ColumnScope.(UiModifier) -> Unit
): List<AwakeUiPreviewSample> {
    val states = listOf(
        "default" to Modifier,
        "hover" to Modifier.forceHover(),
        "active" to Modifier.forceActive(),
        "focus" to Modifier.forceFocus()
    )

    return states.map { (idSuffix, forcedModifier) ->
        val ui = UiContext()
        val resolvedTheme = theme ?: io.github.ronjunevaldoz.awake.ui.CoreUiTheme
        ui.beginFrame(width.toFloat(), height.toFloat(), UiInputState())
        ui.pushFont(font)
        ui.pushTheme(resolvedTheme)
        ui.createColumn(
            x = 0f,
            y = 0f,
            width = width.toFloat(),
        ).block(forcedModifier)
        val frameOutput = ui.finishFrame()
        sample(
            idSuffix = idSuffix,
            titleSuffix = idSuffix.replaceFirstChar { it.uppercase() },
            frame = AwakeUiPreviewFrame(
                primitives = frameOutput.primitives,
                background = resolvedTheme.tokens.background,
                font = font,
                semantics = frameOutput.semantics
            )
        )
    }
}
