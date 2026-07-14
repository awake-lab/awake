// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.snapshot

import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

/**
 * Rasterizes [primitives] ([rasterize]) and writes it as a real, viewable PNG under
 * `build/ui-snapshots/[name].png` -- desktop JVM only (`javax.imageio`, same as
 * `awake:backend:vulkan`'s `RendererHeadlessPixelBaselineTest`), so this lives in
 * `desktopTest`, not `commonTest`, alongside [rasterize] itself (which stays common/pure).
 * Called unconditionally by a snapshot test regardless of pass/fail -- unlike the
 * pixel-baseline report (which only dumps on a mismatch, since its assertion already proves
 * correctness), these are a *design review* aid: is this theme/variant/state combination
 * legible, not "did a number match."
 */
fun saveUiSnapshot(
    name: String,
    primitives: List<UiDrawPrimitive>,
    width: Int,
    height: Int,
    background: FloatArray = floatArrayOf(0.1f, 0.1f, 0.12f, 1f)
) {
    val pixels = primitives.rasterize(width, height, background)
    val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    var offset = 0
    for (y in 0 until height) {
        for (x in 0 until width) {
            val r = pixels[offset].toInt() and 0xFF
            val g = pixels[offset + 1].toInt() and 0xFF
            val b = pixels[offset + 2].toInt() and 0xFF
            val a = pixels[offset + 3].toInt() and 0xFF
            image.setRGB(x, y, (a shl 24) or (r shl 16) or (g shl 8) or b)
            offset += 4
        }
    }
    val outDir = File("build/ui-snapshots").apply { mkdirs() }
    ImageIO.write(image, "png", File(outDir, "$name.png"))
}
