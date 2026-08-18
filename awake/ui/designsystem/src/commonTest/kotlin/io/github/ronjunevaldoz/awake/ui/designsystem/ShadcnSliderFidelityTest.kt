// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewDimensionRule
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewFrame
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewMetadata
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewValidationConfig
import io.github.ronjunevaldoz.awake.testing.ui.renderUiComponent
import io.github.ronjunevaldoz.awake.testing.ui.validateAwakeUiPreview
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnFieldSliderWithValue
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSlider
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxSize
import io.github.ronjunevaldoz.awake.ui.modifier.width
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
        val sliderWidth = 240f

        val frameOutput = renderUiComponent(
            width = 400f,
            height = 200f,
            rootProvider = { content -> shadcnTheme { content() } },
        ) {
            shadcnSlider(
                id = "test-slider",
                min = 0f,
                max = 100f,
                value = 50f,
                modifier = Modifier.width(sliderWidth.dp),
            )
        }

        val sliderNode = frameOutput.semantics.firstOrNull { it.id == "test-slider" }
        requireNotNull(sliderNode) { "Slider node 'test-slider' must exist in semantic tree" }

        // Assert slider height matches shadcn 20dp spec (20px) exactly
        assertEquals(20f, sliderNode.bounds.height, 0.5f, "Slider height must be 20dp")

        // Assert slider contains no internal child text nodes overlaying the track
        val overlayTextNodes = frameOutput.semantics.filter { it.id?.startsWith("test-slider.") == true }
        assertTrue(overlayTextNodes.isEmpty(), "Slider must not render text overlay nodes across the track")

        val config = AwakeUiPreviewValidationConfig(
            dimensionRules = listOf(
                AwakeUiPreviewDimensionRule(
                    nodeId = "test-slider",
                    exactHeight = 20f,
                    exactWidth = sliderWidth,
                ),
            ),
        )

        validateAwakeUiPreview(
            metadata = AwakeUiPreviewMetadata(
                id = "slider-fidelity",
                title = "Slider Fidelity",
                group = "Control",
                summary = "Slider 20dp height and track overlay contract test",
                width = 400,
                height = 200,
            ),
            frame = AwakeUiPreviewFrame(
                primitives = frameOutput.primitives,
                background = ShadcnTheme.colors.background,
                font = frameOutput.font,
                semantics = frameOutput.semantics,
            ),
            config = config,
        ).requireClean()
    }

    @Test
    fun shadcnFieldSliderWithValueProximitySpacingFidelity() = runTest {
        val frameOutput = renderUiComponent(
            width = 400f,
            height = 200f,
            rootProvider = { content -> shadcnTheme { content() } },
        ) {
            column(modifier = Modifier.fillMaxSize()) {
                shadcnFieldSliderWithValue(
                    id = "field-slider-test",
                    label = "Exposure",
                    min = 0f,
                    max = 100f,
                    value = 52f,
                )
            }
        }

        val sliderNode = frameOutput.semantics.firstOrNull { it.id == "field-slider-test" }
        requireNotNull(sliderNode) { "Slider node 'field-slider-test' must exist" }

        // Assert slider widget height is 20dp (not 32dp or 40dp)
        assertEquals(20f, sliderNode.bounds.height, 0.5f, "Field slider control height must be 20dp")

        val config = AwakeUiPreviewValidationConfig(
            dimensionRules = listOf(
                AwakeUiPreviewDimensionRule(
                    nodeId = "field-slider-test",
                    exactHeight = 20f,
                ),
            ),
        )

        validateAwakeUiPreview(
            metadata = AwakeUiPreviewMetadata(
                id = "field-slider-fidelity",
                title = "Field Slider Fidelity",
                group = "Input",
                summary = "Field slider 20dp height and label proximity check",
                width = 400,
                height = 200,
            ),
            frame = AwakeUiPreviewFrame(
                primitives = frameOutput.primitives,
                background = ShadcnTheme.colors.background,
                font = frameOutput.font,
                semantics = frameOutput.semantics,
            ),
            config = config,
        ).requireClean()
    }
}
