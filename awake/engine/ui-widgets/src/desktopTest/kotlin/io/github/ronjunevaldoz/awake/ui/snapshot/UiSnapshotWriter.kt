// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.snapshot

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.testing.ui.rasterize
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

private val tutorialManifestLock = Any()

/**
 * Rasterizes [primitives] ([rasterize]) and writes it as a real, viewable PNG under
 * `build/ui-snapshots/[name].png` -- desktop JVM only (`javax.imageio`, same as
 * `awake:backend:vulkan`'s `RendererHeadlessPixelBaselineTest`), so this lives in
 * `desktopTest` while [rasterize] stays shared in `awake:engine:testing`.
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
    background: Color = Color(0.1f, 0.1f, 0.12f, 1f),
    font: BitmapFont? = null
) {
    val pixels = primitives.rasterize(width, height, background, font)
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

fun saveUiTutorialSnapshot(
    name: String,
    title: String,
    summary: String,
    primitives: List<UiDrawPrimitive>,
    width: Int,
    height: Int,
    background: Color = Color(0.1f, 0.1f, 0.12f, 1f),
    font: BitmapFont? = null
) {
    saveUiSnapshot(name, primitives, width, height, background, font)

    synchronized(tutorialManifestLock) {
        val manifest = File("build/ui-snapshots/tutorials.tsv")
        manifest.parentFile.mkdirs()
        val escapedSummary = summary.replace('\t', ' ').replace('\n', ' ')
        val escapedTitle = title.replace('\t', ' ').replace('\n', ' ')
        val line = listOf(name, escapedTitle, escapedSummary, width.toString(), height.toString()).joinToString("\t")
        val entries = linkedMapOf<String, String>()
        if (manifest.exists()) {
            manifest.readLines()
                .filter { it.isNotBlank() }
                .forEach { existing ->
                    val key = existing.substringBefore('\t')
                    entries[key] = existing
                }
        }
        entries[name] = line
        manifest.writeText(entries.values.joinToString(separator = "\n", postfix = "\n"))
    }
}
