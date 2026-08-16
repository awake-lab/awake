// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.testing.ui.inspectBoundsFit
import io.github.ronjunevaldoz.awake.testing.ui.inspectDensityParity
import io.github.ronjunevaldoz.awake.testing.ui.inspectThemeParity
import io.github.ronjunevaldoz.awake.testing.ui.measureUiFrame
import io.github.ronjunevaldoz.awake.testing.ui.renderUiComponent
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.api.theme.UiThemeValues
import io.github.ronjunevaldoz.awake.ui.designsystem.shadcnThemeValues
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.headless.button
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import kotlin.test.Test

class UiCrossPlatformQualityTest {

    @Test
    fun buttonStructureStaysStableAcrossShadcnThemes() {
        val font = UiFonts.default()
        val dark = measureButtonMetrics(shadcnThemeValues(dark = true), font)
        val light = measureButtonMetrics(shadcnThemeValues(dark = false), font)

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
        val frame = renderUiComponent(
            width = 220f,
            height = 96f,
            theme = shadcnThemeValues(dark = false),
            font = font,
        ) {
            primitive.context.createAbsolute(x = 20f, y = 20f)
                .button("fit", label = "Awake Button", modifier = Modifier.width(180f.px).height(44f.px))
        }

        val metrics = measureUiFrame(frame.primitives, frame.root)
        inspectBoundsFit(
            label = "button",
            metrics = metrics,
            allowedBounds = UiBounds(20f, 20f, 180f, 44f),
            tolerancePx = 1f,
        ).requireClean()
    }

    private fun measureButtonMetrics(theme: UiThemeValues, font: UiFont) =
        measureScaledButtonMetrics(font, 220f, 96f, 180f, 44f, theme)

    private fun measureScaledButtonMetrics(
        font: UiFont,
        frameWidth: Float,
        frameHeight: Float,
        buttonWidth: Float,
        buttonHeight: Float,
        theme: UiThemeValues = shadcnThemeValues(dark = true),
    ) = renderUiComponent(
        width = frameWidth,
        height = frameHeight,
        theme = theme,
        font = font,
    ) {
        primitive.context.createAbsolute(x = (frameWidth - buttonWidth) / 2f, y = (frameHeight - buttonHeight) / 2f)
            .button("quality", label = "Awake Button", modifier = Modifier.width(buttonWidth.px).height(buttonHeight.px))
    }.let { frame ->
        measureUiFrame(frame.primitives, frame.root)
    }
}
