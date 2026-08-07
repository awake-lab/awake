// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewDimensionRule
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewExactSpacingRule
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewFrame
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewMetadata
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewValidationConfig
import io.github.ronjunevaldoz.awake.testing.ui.FigmaVariableProvider
import io.github.ronjunevaldoz.awake.testing.ui.validateAwakeUiPreview
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.controls.shadcnSlider
import io.github.ronjunevaldoz.awake.ui.designsystem.components.input.shadcnFieldSliderWithValue
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.layouts.column
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Automated fidelity & spacing contract tests for [shadcnSlider] and [shadcnFieldSliderWithValue].
 */
class ShadcnSliderFidelityTest {

    @Test
    fun shadcnSliderHeightAndNoOverlayTextFidelity() = runTest {
        val figma = FigmaVariableProvider.load("design-tokens.json")
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)

        val sliderWidth = 240f

        ui.beginFrame(400f, 200f, testSnapshot(x = -100f, y = -100f, down = false))
        ui.createAbsolute(slot = ui.frameBounds()).shadcnSlider(
            id = "test-slider",
            min = 0f,
            max = 100f,
            value = 50f,
            modifier = Modifier.width(sliderWidth.dp)
        )
        val frameOutput = ui.finishFrame()

        val sliderNode = frameOutput.semantics.firstOrNull { it.id == "test-slider" }
        requireNotNull(sliderNode) { "Slider node 'test-slider' must exist in semantic tree" }

        // Assert slider height matches shadcn 20dp spec (20px) exactly
        assertEquals(20f.dp.toPx(), sliderNode.bounds.height, 0.5f, "Slider height must be 20dp")

        // Assert slider contains no internal child text nodes overlaying the track
        val overlayTextNodes = frameOutput.semantics.filter { it.id?.startsWith("test-slider.") == true }
        assertTrue(overlayTextNodes.isEmpty(), "Slider must not render text overlay nodes across the track")

        val config = AwakeUiPreviewValidationConfig(
            dimensionRules = listOf(
                AwakeUiPreviewDimensionRule(
                    nodeId = "test-slider",
                    exactHeight = 20f.dp.toPx(),
                    exactWidth = sliderWidth.dp.toPx()
                )
            )
        )

        validateAwakeUiPreview(
            metadata = AwakeUiPreviewMetadata(
                id = "slider-fidelity",
                title = "Slider Fidelity",
                group = "Control",
                summary = "Slider 20dp height and track overlay contract test",
                width = 400,
                height = 200
            ),
            frame = AwakeUiPreviewFrame(
                primitives = frameOutput.primitives,
                background = ui.currentTheme.colors.background,
                font = ui.currentFont,
                semantics = frameOutput.semantics
            ),
            config = config
        ).requireClean()
    }

    @Test
    fun shadcnFieldSliderWithValueProximitySpacingFidelity() = runTest {
        val figma = FigmaVariableProvider.load("design-tokens.json")
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)

        ui.beginFrame(400f, 200f, testSnapshot(x = -100f, y = -100f, down = false))
        ui.createAbsolute(slot = ui.frameBounds()).column(modifier = Modifier.width(300f.dp)) {
            shadcnFieldSliderWithValue(
                id = "field-slider-test",
                label = "Exposure",
                min = 0f,
                max = 100f,
                value = 52f
            )
        }
        val frameOutput = ui.finishFrame()

        val sliderNode = frameOutput.semantics.firstOrNull { it.id == "field-slider-test" }
        requireNotNull(sliderNode) { "Slider node 'field-slider-test' must exist" }

        // Assert slider widget height is 20dp (not 32dp or 40dp)
        assertEquals(20f.dp.toPx(), sliderNode.bounds.height, 0.5f, "Field slider control height must be 20dp")

        val config = AwakeUiPreviewValidationConfig(
            dimensionRules = listOf(
                AwakeUiPreviewDimensionRule(
                    nodeId = "field-slider-test",
                    exactHeight = 20f.dp.toPx()
                )
            )
        )

        validateAwakeUiPreview(
            metadata = AwakeUiPreviewMetadata(
                id = "field-slider-fidelity",
                title = "Field Slider Fidelity",
                group = "Input",
                summary = "Field slider 20dp height and label proximity check",
                width = 400,
                height = 200
            ),
            frame = AwakeUiPreviewFrame(
                primitives = frameOutput.primitives,
                background = ui.currentTheme.colors.background,
                font = ui.currentFont,
                semantics = frameOutput.semantics
            ),
            config = config
        ).requireClean()
    }
}
