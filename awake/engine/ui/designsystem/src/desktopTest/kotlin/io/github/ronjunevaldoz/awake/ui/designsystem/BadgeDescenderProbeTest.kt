// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.testing.ui.rasterize
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test

class BadgeDescenderProbeTest {

    @Test
    fun probe() {
        val font = UiFonts.default()
        val w = 220
        val h = 90
        val ui = UiContext()
        ui.pushFont(font)
        ui.pushTheme(ShadcnTheme)
        ui.beginFrame(w.toFloat(), h.toFloat(), testSnapshot())
        ui.createAbsolute(x = 12f, y = 10f).text(label = "Biography gpyj")
        ui.createAbsolute(x = 12f, y = 38f).shadcnBadge(label = "Biography gpyj")
        val primitives = ui.endFrame()

        println("g offsetYEm=${font.uvFor('g')!!.offsetYEm} heightEm=${font.uvFor('g')!!.heightEm} bottom=${font.uvFor('g')!!.offsetYEm + font.uvFor('g')!!.heightEm}")
        println("ascentEm=${font.ascentEm} lineHeightEm=${font.lineHeightEm} visibleTopEm=${font.visibleTopEm} visibleBottomEm=${font.visibleBottomEm}")

        primitives.forEach { p ->
            when (p) {
                is UiDrawPrimitive.Glyph -> println("GLYPH x=${p.x} y=${p.y} w=${p.w} h=${p.h}")
                is UiDrawPrimitive.RoundedQuad -> println("ROUNDEDQUAD x=${p.x} y=${p.y} w=${p.w} h=${p.h}")
                is UiDrawPrimitive.Quad -> println("QUAD x=${p.x} y=${p.y} w=${p.w} h=${p.h}")
                is UiDrawPrimitive.ClipPush -> println("CLIPPUSH rect=${p.rect}")
                is UiDrawPrimitive.ClipPop -> println("CLIPPOP")
                else -> println(p::class.simpleName)
            }
        }

        val pixels = primitives.rasterize(w, h, font = font)
        val img = java.awt.image.BufferedImage(w * 8, h * 8, java.awt.image.BufferedImage.TYPE_INT_ARGB)
        for (y in 0 until h) {
            for (x in 0 until w) {
                val off = (y * w + x) * 4
                val r = pixels[off].toInt() and 0xFF
                val g = pixels[off + 1].toInt() and 0xFF
                val b = pixels[off + 2].toInt() and 0xFF
                val a = pixels[off + 3].toInt() and 0xFF
                val argb = (a shl 24) or (r shl 16) or (g shl 8) or b
                for (sy in 0 until 8) for (sx in 0 until 8) img.setRGB(x * 8 + sx, y * 8 + sy, argb)
            }
        }
        val out = File("/tmp/badge_descender_probe.png")
        ImageIO.write(img, "png", out)
        println("wrote ${out.absolutePath}")
    }
}
