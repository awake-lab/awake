// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.testing.ui.UiComponentFrame
import io.github.ronjunevaldoz.awake.testing.ui.UiTestSession
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
    fontScale: Float = 1f,
    input: UiInputState = testSnapshot(),
    deltaSeconds: Float = 1f / 60f,
    content: UiScope.(root: UiBounds) -> Unit,
): UiComponentFrame = renderUiComponent(
    width = width,
    height = height,
    font = font,
    density = density,
    fontScale = fontScale,
    input = input,
    deltaSeconds = deltaSeconds,
    rootProvider = { subtree -> shadcnTheme(theme = theme, content = subtree) },
    content = content,
)

/** Multi-frame counterpart to [renderShadcnComponent]. */
internal class ShadcnTestSession(
    private val session: UiTestSession,
) : AutoCloseable {
    val ui get() = session.ui

    fun frame(
        x: Float = -100f,
        y: Float = -100f,
        down: Boolean = false,
        deltaSeconds: Float = 1f / 60f,
        content: UiScope.(root: UiBounds) -> Unit,
    ): UiComponentFrame = session.frame(x = x, y = y, down = down, deltaSeconds = deltaSeconds, content = content)

    /** Uses an exact input snapshot for probes that exercise wheel or secondary-pointer input. */
    fun frame(
        input: UiInputState,
        deltaSeconds: Float = 1f / 60f,
        content: UiScope.(root: UiBounds) -> Unit,
    ): UiComponentFrame = session.frame(input = input, deltaSeconds = deltaSeconds, content = content)

    fun hover(x: Float, y: Float, content: UiScope.(root: UiBounds) -> Unit): UiComponentFrame =
        session.hover(x, y, content)

    fun click(x: Float, y: Float, content: UiScope.(root: UiBounds) -> Unit): UiComponentFrame =
        session.click(x, y, content)

    fun doubleClick(x: Float, y: Float, content: UiScope.(root: UiBounds) -> Unit): UiComponentFrame =
        session.doubleClick(x, y, content)

    fun longPress(
        x: Float,
        y: Float,
        durationSeconds: Float,
        content: UiScope.(root: UiBounds) -> Unit,
    ): UiComponentFrame = session.longPress(x, y, durationSeconds, content)

    fun rightClick(x: Float, y: Float, content: UiScope.(root: UiBounds) -> Unit): UiComponentFrame =
        session.rightClick(x, y, content)

    fun drag(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        steps: Int = 1,
        content: UiScope.(root: UiBounds) -> Unit,
    ): UiComponentFrame = session.drag(startX, startY, endX, endY, steps, content)

    override fun close() = session.close()
}

internal fun <T> shadcnTestSession(
    width: Float = 400f,
    height: Float = 400f,
    theme: ShadcnThemeValues = shadcnThemeValues(),
    font: UiFont = UiFonts.default(),
    density: Float = 1f,
    fontScale: Float = 1f,
    block: ShadcnTestSession.() -> T,
): T = ShadcnTestSession(
    session = UiTestSession(
        width = width,
        height = height,
        font = font,
        density = density,
        fontScale = fontScale,
        rootProvider = { subtree -> shadcnTheme(theme = theme, content = subtree) },
    ),
).use(block)
