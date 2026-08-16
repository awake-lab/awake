// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.testing.ui.rasterize
import io.github.ronjunevaldoz.awake.testing.ui.toBufferedImage
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.ShadcnButtonGroupOrientation
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButtonGroup
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButtonGroupSeparator
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxSize
import io.github.ronjunevaldoz.awake.ui.headless.height
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test

/** Writes both orientations to a viewable PNG. A design-review aid, not an assertion. */
class ShadcnButtonGroupCaptureTest {

    @Test
    fun captureBothOrientations() {
        val width = 420
        val height = 260
        val font = BitmapFont()
        val frame = renderShadcnComponent(
            width = width.toFloat(),
            height = height.toFloat(),
            font = font,
        ) {
            column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(24f.dp),
            ) {
                shadcnButtonGroup(id = "capture-horizontal", modifier = Modifier.height(40f.dp)) {
                    shadcnButton(id = "h-left", label = "Left")
                    shadcnButtonGroupSeparator("h-sep-1")
                    shadcnButton(id = "h-mid", label = "Middle")
                    shadcnButtonGroupSeparator("h-sep-2")
                    shadcnButton(id = "h-right", label = "Right")
                }
                shadcnButtonGroup(
                    id = "capture-vertical",
                    orientation = ShadcnButtonGroupOrientation.Vertical,
                    modifier = Modifier.height(160f.dp),
                ) {
                    shadcnButton(id = "v-top", label = "Top")
                    shadcnButtonGroupSeparator("v-sep-1")
                    shadcnButton(id = "v-center", label = "Longer Center Label")
                    shadcnButtonGroupSeparator("v-sep-2")
                    shadcnButton(id = "v-bottom", label = "Bottom")
                }
            }
        }

        val pixels = frame.primitives.rasterize(width, height, Color(0.08f, 0.08f, 0.1f, 1f), font)
        val image = pixels.toBufferedImage(width, height)
        val out = File("build/ui-snapshots/button-group.png")
        out.parentFile.mkdirs()
        ImageIO.write(image, "png", out)

        val h = frame.semantics.first { it.id == "capture-horizontal" }
        val v = frame.semantics.first { it.id == "capture-vertical" }
        println("horizontal=${h.bounds.width}x${h.bounds.height} vertical=${v.bounds.width}x${v.bounds.height}")
        println("wrote ${out.absolutePath}")
    }
}
