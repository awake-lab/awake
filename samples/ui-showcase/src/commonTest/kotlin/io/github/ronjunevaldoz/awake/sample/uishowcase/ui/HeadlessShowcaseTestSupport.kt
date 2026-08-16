// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.testing.ui.UiComponentFrame
import io.github.ronjunevaldoz.awake.testing.ui.UiTestSession
import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewMetadata
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewSample
import io.github.ronjunevaldoz.awake.testing.ui.headlessComponentStateMatrix
import io.github.ronjunevaldoz.awake.ui.designsystem.ShadcnThemeValues
import io.github.ronjunevaldoz.awake.ui.designsystem.shadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.shadcnThemeValues
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.createUiScope
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier

/** Direct-context counterpart to [showcaseTestSession] for probes that manage their own frames. */
internal fun UiContext.showcaseRoot(
    theme: ShadcnThemeValues = shadcnThemeValues(),
    bounds: UiBounds = frameBounds(),
    content: UiScope.() -> Unit,
) {
    createUiScope(bounds).shadcnTheme(theme = theme, content = content)
}

/** Showcase counterpart to the neutral test session: installs the DS-local Shadcn scope per frame. */
internal class ShowcaseTestSession(
    private val session: UiTestSession,
) : AutoCloseable {
    val ui get() = session.ui
    val input get() = session.input

    fun frame(
        x: Float = -100f,
        y: Float = -100f,
        down: Boolean = false,
        deltaSeconds: Float = 1f / 60f,
        content: UiScope.(root: UiBounds) -> Unit,
    ): UiComponentFrame = session.frame(x = x, y = y, down = down, deltaSeconds = deltaSeconds, content = content)

    fun frame(
        input: UiInputState,
        deltaSeconds: Float = 1f / 60f,
        content: UiScope.(root: UiBounds) -> Unit,
    ): UiComponentFrame = session.frame(input = input, deltaSeconds = deltaSeconds, content = content)

    override fun close() = session.close()
}

internal fun <T> showcaseTestSession(
    width: Float = 400f,
    height: Float = 400f,
    theme: ShadcnThemeValues = shadcnThemeValues(),
    font: UiFont = UiFonts.default(),
    density: Float = 1f,
    fontScale: Float = 1f,
    block: ShowcaseTestSession.() -> T,
): T = ShowcaseTestSession(
    UiTestSession(
        width = width,
        height = height,
        font = font,
        density = density,
        fontScale = fontScale,
        rootProvider = { subtree -> shadcnTheme(theme = theme, content = subtree) },
    ),
).use(block)

/** State-matrix entry point for Shadcn previews; the generic helper stays design-system-free. */
internal fun AwakeUiPreviewMetadata.shadcnComponentStateMatrix(
    theme: ShadcnThemeValues = shadcnThemeValues(),
    font: UiFont = UiFonts.default(),
    block: ColumnScope.(Modifier) -> Unit,
): List<AwakeUiPreviewSample> = headlessComponentStateMatrix(
    theme = theme,
    font = font,
    rootProvider = { content -> shadcnTheme(theme = theme, content = content) },
    block = block,
)
