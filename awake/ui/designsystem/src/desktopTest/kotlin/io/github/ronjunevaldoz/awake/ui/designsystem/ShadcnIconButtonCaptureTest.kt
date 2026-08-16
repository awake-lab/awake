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
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonSize
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxSize
import io.github.ronjunevaldoz.awake.ui.headless.size
import io.github.ronjunevaldoz.awake.ui.headless.surface
import io.github.ronjunevaldoz.awake.ui.style.Style
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals

/** Writes the horizontal/vertical x text/icon button-group matrix to a viewable PNG -- proof
 * for the icon-content overload's unconditional size(size.heightDp) square fix, and for dropping
 * the horizontal group's fillMaxHeight() override (every member already carries its own
 * heightOrDefault(size.heightDp), so uniform-size groups size correctly without it). */
class ShadcnIconButtonCaptureTest {

    private fun UiScope.iconGlyph(id: String) {
        surface(
            id = id,
            modifier = Modifier.size(16f.dp),
            style = Style { background(Color(1f, 1f, 1f, 1f)) },
        ) {}
    }

    private fun ColumnScope.textGroup(id: String, orientation: ShadcnButtonGroupOrientation) {
        shadcnButtonGroup(id = id, orientation = orientation) {
            shadcnButton(id = "$id-left", label = "Left", variant = ShadcnButtonVariant.Ghost)
            shadcnButton(id = "$id-mid", label = "Mid", variant = ShadcnButtonVariant.Primary)
            shadcnButton(id = "$id-right", label = "Right", variant = ShadcnButtonVariant.Ghost)
        }
    }

    private fun ColumnScope.iconGroup(id: String, orientation: ShadcnButtonGroupOrientation) {
        shadcnButtonGroup(id = id, orientation = orientation) {
            shadcnButton(id = "$id-left", label = "Left", variant = ShadcnButtonVariant.Ghost)
            shadcnButton(id = "$id-mid", variant = ShadcnButtonVariant.Primary, size = ShadcnButtonSize.Icon) {
                iconGlyph("$id-mid-glyph")
            }
            shadcnButton(id = "$id-right", label = "Right", variant = ShadcnButtonVariant.Ghost)
        }
    }

    @Test
    fun captureOrientationAndContentMatrix() {
        val width = 300
        val height = 420
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
                textGroup("horiz-text", ShadcnButtonGroupOrientation.Horizontal)
                iconGroup("horiz-icon", ShadcnButtonGroupOrientation.Horizontal)
                textGroup("vert-text", ShadcnButtonGroupOrientation.Vertical)
                iconGroup("vert-icon", ShadcnButtonGroupOrientation.Vertical)
            }
        }

        val pixels = frame.primitives.rasterize(width, height, Color(0.08f, 0.08f, 0.1f, 1f), font)
        val image = pixels.toBufferedImage(width, height)
        val out = File("build/ui-snapshots/icon-button.png")
        out.parentFile.mkdirs()
        ImageIO.write(image, "png", out)

        val horizIconMid = frame.semantics.first { it.id == "horiz-icon-mid" }
        val vertIconMid = frame.semantics.first { it.id == "vert-icon-mid" }
        println("horiz-icon-mid=${horizIconMid.bounds.width}x${horizIconMid.bounds.height}")
        println("vert-icon-mid=${vertIconMid.bounds.width}x${vertIconMid.bounds.height}")
        println("wrote ${out.absolutePath}")

        assertEquals(36f, horizIconMid.bounds.width, "icon member squared width in horizontal group")
        assertEquals(36f, horizIconMid.bounds.height, "icon member squared height in horizontal group")
    }
}
