// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.testing.ui

import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.api.theme.UiThemeValues
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.context.UiFrameInput
import io.github.ronjunevaldoz.awake.ui.context.LocalFont
import io.github.ronjunevaldoz.awake.ui.context.LocalTheme
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.createUiScope
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.forceActive
import io.github.ronjunevaldoz.awake.ui.modifier.forceFocus
import io.github.ronjunevaldoz.awake.ui.modifier.forceHover
import io.github.ronjunevaldoz.awake.ui.theme.UiDefaultTheme
import io.github.ronjunevaldoz.awake.ui.theme.asRuntimeTheme
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope as HeadlessColumnScope

/**
 * Automates the rendering of a component in multiple interaction states using forced modifiers.
 */
fun AwakeUiPreviewMetadata.componentStateMatrix(
    font: UiFont = UiFonts.default(),
    theme: UiThemeValues? = null,
    block: ColumnScope.(UiModifier) -> Unit,
): List<AwakeUiPreviewSample> {
    val states = listOf(
        "default" to Modifier,
        "hover" to Modifier.forceHover(),
        "active" to Modifier.forceActive(),
        "focus" to Modifier.forceFocus(),
    )

    return states.map { (idSuffix, forcedModifier) ->
        val ui = UiContext()
        val resolvedTheme = theme ?: UiDefaultTheme
        ui.beginFrame(UiFrameInput(width.toFloat(), height.toFloat(), UiInputState()))
        ui.pushLocal(LocalFont, font)
        ui.pushLocal(LocalTheme, resolvedTheme.asRuntimeTheme())
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
                background = resolvedTheme.colors.background,
                font = font,
                semantics = frameOutput.semantics,
            ),
        )
    }
}

/**
 * Automates the rendering of a component in multiple interaction states using forced modifiers,
 * targeting Headless [HeadlessColumnScope].
 */
fun AwakeUiPreviewMetadata.headlessComponentStateMatrix(
    font: UiFont = UiFonts.default(),
    theme: UiThemeValues? = null,
    /**
     * Installs app-specific locals around each sample root. `ui:testing` deliberately does not
     * know design systems; a caller can provide its own composition boundary here.
     */
    rootProvider: UiTestRootProvider = { content -> content() },
    block: HeadlessColumnScope.(UiModifier) -> Unit,
): List<AwakeUiPreviewSample> {
    val states = listOf(
        "default" to Modifier,
        "hover" to Modifier.forceHover(),
        "active" to Modifier.forceActive(),
        "focus" to Modifier.forceFocus(),
    )

    return states.map { (idSuffix, forcedModifier) ->
        val ui = UiContext()
        val resolvedTheme = theme ?: UiDefaultTheme
        ui.beginFrame(UiFrameInput(width.toFloat(), height.toFloat(), UiInputState()))
        ui.pushLocal(LocalFont, font)
        ui.pushLocal(LocalTheme, resolvedTheme.asRuntimeTheme())
        ui.createUiScope(UiBounds(0f, 0f, width.toFloat(), height.toFloat())).rootProvider {
            column {
                block(forcedModifier)
            }
        }
        val frameOutput = ui.finishFrame()
        sample(
            idSuffix = idSuffix,
            titleSuffix = idSuffix.replaceFirstChar { it.uppercase() },
            frame = AwakeUiPreviewFrame(
                primitives = frameOutput.primitives,
                background = resolvedTheme.colors.background,
                font = font,
                semantics = frameOutput.semantics,
            ),
        )
    }
}
