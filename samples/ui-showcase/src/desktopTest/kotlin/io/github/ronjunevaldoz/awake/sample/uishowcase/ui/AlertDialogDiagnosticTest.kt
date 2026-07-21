// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.core.input.InputSnapshot
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreview
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewEntry
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewFrame
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewMetadata
import io.github.ronjunevaldoz.awake.testing.ui.renderAnnotatedUiPreview
import io.github.ronjunevaldoz.awake.testing.ui.saveAwakeUiPreview
import io.github.ronjunevaldoz.awake.ui.UiContext
import io.github.ronjunevaldoz.awake.ui.unstyled.UiButtonVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.alertDialog
import io.github.ronjunevaldoz.awake.ui.designsystem.awakeShadcnTheme
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.toUiInputState
import io.github.ronjunevaldoz.awake.ui.layouts.ext.column
import kotlin.test.Test

/** Builds a one-off [InputSnapshot] for a preview frame -- [Input] is a per-session
 * instance now (no longer a global object), so tests construct their own throwaway one. */
private fun diagnosticTestSnapshot(): InputSnapshot {
    val input = Input()
    input.setPointer(down = false, x = -100f, y = -100f)
    return input.updateSnapshot()
}

class AlertDialogDiagnosticTest {
    @Test
    fun writeAlertDialogDiagnosticScreenshot() {
        saveAwakeUiPreview(renderAnnotatedUiPreview(AlertDialogDiagnosticPreview))
    }
}

@AwakeUiPreview(
    id = "diagnostic-alert-dialog",
    title = "Diagnostic: Alert Dialog",
    group = "Diagnostic",
    summary = "Real alertDialog() usage from UiShowcaseOverlayPages.kt, same title/message/confirmLabel, to diagnose reported spacing/pixelation issues.",
    width = 400,
    height = 400
)
internal object AlertDialogDiagnosticPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame {
        val theme = awakeShadcnTheme(dark = false)
        val font = UiFonts.default()
        val ui = UiContext()
        ui.beginFrame(metadata.width.toFloat(), metadata.height.toFloat(), diagnosticTestSnapshot().toUiInputState())
        ui.column(x = 0f, y = 0f, width = metadata.width.toFloat(), font = font, theme = theme) {
            alertDialog(
                id = "diagnostic-delete-dialog",
                expanded = true,
                title = "Delete showcase card?",
                message = "This sample does not really delete anything. It exists to prove the alert dialog composition and confirm or dismiss flow.",
                confirmLabel = "Delete",
                confirmVariant = UiButtonVariant.Filled
            )
        }
        return AwakeUiPreviewFrame(
            primitives = ui.endFrame(),
            background = theme.tokens.background,
            font = font,
            semantics = ui.semanticNodes()
        )
    }
}
