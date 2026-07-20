// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.testing.ui.inspectBoundsFit
import io.github.ronjunevaldoz.awake.testing.ui.inspectDensityParity
import io.github.ronjunevaldoz.awake.testing.ui.inspectThemeParity
import io.github.ronjunevaldoz.awake.testing.ui.measureUiFrame
import io.github.ronjunevaldoz.awake.ui.designsystem.awakeShadcnTheme
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import kotlin.test.Test

class UiCrossPlatformQualityTest {

    @Test
    fun buttonStructureStaysStableAcrossShadcnThemes() {
        val font = UiFonts.default()
        val dark = measureButtonMetrics(awakeShadcnTheme(dark = true), font)
        val light = measureButtonMetrics(awakeShadcnTheme(dark = false), font)

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
        val frame = UiSlot(0f, 0f, 220f, 96f)
        val context = UiContext()
        context.beginFrame(frame.width, frame.height, testSnapshot())
        context.absolute(20f, 20f, font = font, theme = awakeShadcnTheme(dark = false))
            .button("fit", label = "Awake Button", modifier = UiModifier().width(180f.px).height(44f.px))

        val metrics = measureUiFrame(context.endFrame(), frame)
        inspectBoundsFit(
            label = "button",
            metrics = metrics,
            allowedBounds = UiSlot(20f, 20f, 180f, 44f),
            tolerancePx = 1f
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
        theme: UiTheme = awakeShadcnTheme(dark = true)
    ) = UiContext().let { context ->
        context.beginFrame(frameWidth, frameHeight, testSnapshot())
        context.absolute((frameWidth - buttonWidth) / 2f, (frameHeight - buttonHeight) / 2f, font = font, theme = theme)
            .button("quality", label = "Awake Button", modifier = UiModifier().width(buttonWidth.px).height(buttonHeight.px))
        measureUiFrame(context.endFrame(), UiSlot(0f, 0f, frameWidth, frameHeight))
    }
}
