// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.testing.ui.inspectBoundsFit
import io.github.ronjunevaldoz.awake.testing.ui.inspectDensityParity
import io.github.ronjunevaldoz.awake.testing.ui.inspectThemeParity
import io.github.ronjunevaldoz.awake.testing.ui.measureUiFrame
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.shadcnTheme
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.headless.button
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.theme.UiTheme
import kotlin.test.Test

class UiCrossPlatformQualityTest {

    @Test
    fun buttonStructureStaysStableAcrossShadcnThemes() {
        val font = UiFonts.default()
        val dark = measureButtonMetrics(shadcnTheme(dark = true), font)
        val light = measureButtonMetrics(shadcnTheme(dark = false), font)

        inspectThemeParity(dark, light).requireClean()
    }

    @Test
    fun buttonKeepsItsRelativeFootprintAcrossDensityScales() {
        val font = UiFonts.default()
        val compact = measureScaledButtonMetrics(font, frameWidth = 220f, frameHeight = 96f, buttonWidth = 180f, buttonHeight = 44f)
        val expanded = measureScaledButtonMetrics(font, frameWidth = 440f, frameHeight = 192f, buttonWidth = 360f, buttonHeight = 88f)

        inspectDensityParity(compact, expanded).requireClean()
    }

    @Test
    fun buttonContentFitsRequestedBounds() {
        val font = UiFonts.default()
        val frame = UiBounds(0f, 0f, 220f, 96f)
        val context = UiContext()
        context.beginFrame(frame.width, frame.height, testSnapshot())
        context.pushFont(font)
        context.pushTheme(shadcnTheme(dark = false))
        context.createAbsolute(x = 20f, y = 20f)
            .button("fit", label = "Awake Button", modifier = Modifier.width(180f.px).height(44f.px))

        val metrics = measureUiFrame(context.endFrame(), frame)
        inspectBoundsFit(
            label = "button",
            metrics = metrics,
            allowedBounds = UiBounds(20f, 20f, 180f, 44f),
            tolerancePx = 1f,
        ).requireClean()
    }

    private fun measureButtonMetrics(theme: UiTheme, font: UiFont) =
        measureScaledButtonMetrics(font, 220f, 96f, 180f, 44f, theme)

    private fun measureScaledButtonMetrics(
        font: UiFont,
        frameWidth: Float,
        frameHeight: Float,
        buttonWidth: Float,
        buttonHeight: Float,
        theme: UiTheme = shadcnTheme(dark = true),
    ) = UiContext().let { context ->
        context.beginFrame(frameWidth, frameHeight, testSnapshot())
        context.pushFont(font)
        context.pushTheme(theme)
        context.createAbsolute(x = (frameWidth - buttonWidth) / 2f, y = (frameHeight - buttonHeight) / 2f)
            .button("quality", label = "Awake Button", modifier = Modifier.width(buttonWidth.px).height(buttonHeight.px))
        measureUiFrame(context.endFrame(), UiBounds(0f, 0f, frameWidth, frameHeight))
    }
}
