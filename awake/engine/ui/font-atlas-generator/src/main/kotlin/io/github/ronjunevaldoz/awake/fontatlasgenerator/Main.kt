// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.fontatlasgenerator

import com.squareup.kotlinpoet.CHAR
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.font.FontRenderContext
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.io.File
import java.util.Base64
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

private const val FONT_PACKAGE = "io.github.ronjunevaldoz.awake.ui.font"
private val ASCII_GLYPHS: List<Char> = (32..126).map { it.toChar() }

private const val FONT_PATH = "../ui-core/src/commonMain/resources/fonts/Roboto-Regular.ttf"
private const val OUT_DIR = "../ui-core/src/commonMain/kotlin"
private const val OBJECT_NAME = "RobotoRegularUiFontData"
private const val DISPLAY_NAME = "Roboto Regular"
private const val LOGICAL_CELL = 16
private const val OVERSAMPLE = 4
private const val PADDING = 6
private const val COLUMNS = 16

/** Manual/on-demand, matching `:awake:engine:ui:tailwind-generator`'s own shape -- run this
 * (`./gradlew :awake:engine:ui:font-atlas-generator:generateFontAtlas`) and commit the
 * regenerated `RobotoRegularUiFontData.kt`, don't wire it into every build.
 *
 * Replaces `tools/generate_ui_font_atlas.py`, which derived every vertical metric from the
 * antialiased RASTER ink bbox in integer pixels, quantizing everything to 1/(oversample*
 * logicalCell) em and giving round glyphs (o/O/S/e/c) a systematically different baseline than
 * flat ones (H/E/T/I) because AA overshoot nudges the raster bbox by up to a quarter-pixel.
 * This generator instead reads glyph metrics from the TTF's own OUTLINE geometry
 * ([Font.createGlyphVector]'s [java.awt.font.GlyphVector.getGlyphOutline]), which is exact
 * `Rectangle2D` doubles with no raster step -- metrics and the rasterized atlas bitmap are now
 * fully independent, so raster antialiasing can no longer leak into a glyph's measured size or
 * position.
 */
fun main() {
    val atlas = generateAtlas(File(FONT_PATH))
    val fileSpec = buildFileSpec(atlas)
    val outDir = File(OUT_DIR)
    fileSpec.writeTo(outDir)
    val written = outDir.resolve(FONT_PACKAGE.replace('.', '/')).resolve("$OBJECT_NAME.kt")
    println("Wrote $written")
}

/** One glyph's metrics, all normalized to a fraction of [LOGICAL_CELL] -- the "em" reference
 * every other font-facing consumer already assumes (see `PackedUiFontData.kt`'s doc comment on
 * `baseCellSize`). [offsetXEm] is pen-relative (same origin as [advanceEm]); [offsetYEm] is
 * relative to the font's own ascent line (baseline - ascent), the classic typographic line-top,
 * not a raster grid artifact. */
private data class GlyphMetrics(
    val offsetXEm: Float,
    val offsetYEm: Float,
    val widthEm: Float,
    val heightEm: Float,
    val advanceEm: Float,
)

private class AtlasResult(
    val lineHeightEm: Float,
    val atlasWidth: Int,
    val atlasHeight: Int,
    val glyphOrder: String,
    val uvBoundsPx: IntArray,
    val quadMetricsEm: FloatArray,
    val advancesEm: FloatArray,
    val encodedAlphaBase64: String,
)

private fun generateAtlas(fontFile: File): AtlasResult {
    val baseFont = Font.createFont(Font.TRUETYPE_FONT, fontFile)
    val frc = FontRenderContext(null, true, true)

    // Metrics come from a font derived at LOGICAL_CELL size purely so offsetXEm/widthEm/etc.
    // read as a direct fraction of that size once divided by LOGICAL_CELL -- the outline shape
    // itself is exact `Rectangle2D` geometry and scales linearly with size, so this choice of
    // measurement size has zero effect on precision.
    val measureFont = baseFont.deriveFont(LOGICAL_CELL.toFloat())
    val ascentEm = measureFont.getLineMetrics("Hg", frc).ascent

    val glyphMetrics = ASCII_GLYPHS.associateWith { char -> measureGlyph(measureFont, frc, ascentEm, char) }

    // Rasterization is a separate, oversampled pass -- only the atlas bitmap and the UV sample
    // rect (for texture lookup) come from it. Antialiasing headroom here can never leak into a
    // glyph's measured size/position because [glyphMetrics] above never looks at this raster.
    val renderSize = LOGICAL_CELL * OVERSAMPLE
    val renderFont = baseFont.deriveFont(renderSize.toFloat())
    val renderLineMetrics = renderFont.getLineMetrics("Hg", frc)
    val ascentPxRender = renderLineMetrics.ascent
    val cellHeightPx = ceil((ascentPxRender + renderLineMetrics.descent).toDouble()).toInt() + PADDING * 2
    val maxAdvancePxRender = ASCII_GLYPHS.filter { it != ' ' }.maxOf { char ->
        renderFont.createGlyphVector(frc, char.toString()).getGlyphMetrics(0).advanceX
    }
    val cellWidthPx = ceil(maxAdvancePxRender.toDouble()).toInt() + PADDING * 2

    val rows = ceil(ASCII_GLYPHS.size / COLUMNS.toDouble()).toInt()
    val atlasWidth = cellWidthPx * COLUMNS
    val atlasHeight = cellHeightPx * rows

    val atlasImage = BufferedImage(atlasWidth, atlasHeight, BufferedImage.TYPE_BYTE_GRAY)
    val graphics = atlasImage.createGraphics()
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
    graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)
    graphics.font = renderFont
    graphics.color = Color.WHITE

    val uvBoundsPx = mutableListOf<Int>()
    val quadMetricsEm = mutableListOf<Float>()
    val advancesEm = mutableListOf<Float>()

    ASCII_GLYPHS.forEachIndexed { index, char ->
        val col = index % COLUMNS
        val row = index / COLUMNS
        val cellX = col * cellWidthPx
        val cellY = row * cellHeightPx
        val baselineX = cellX + PADDING
        val baselineY = cellY + PADDING + ascentPxRender

        val gv = renderFont.createGlyphVector(frc, char.toString())
        graphics.drawGlyphVector(gv, baselineX.toFloat(), baselineY)

        val metrics = glyphMetrics.getValue(char)
        val (left, top, right, bottom) = sampleRect(metrics, cellX, cellY, cellWidthPx, cellHeightPx, baselineX, baselineY, ascentPxRender)

        uvBoundsPx += listOf(left, top, right, bottom)
        quadMetricsEm += listOf(metrics.offsetXEm, metrics.offsetYEm, metrics.widthEm, metrics.heightEm)
        advancesEm += metrics.advanceEm
    }
    graphics.dispose()

    return AtlasResult(
        lineHeightEm = (ascentPxRender + renderLineMetrics.descent) / OVERSAMPLE / LOGICAL_CELL,
        atlasWidth = atlasWidth,
        atlasHeight = atlasHeight,
        glyphOrder = ASCII_GLYPHS.joinToString(""),
        uvBoundsPx = uvBoundsPx.toIntArray(),
        quadMetricsEm = quadMetricsEm.toFloatArray(),
        advancesEm = advancesEm.toFloatArray(),
        encodedAlphaBase64 = Base64.getEncoder().encodeToString(grayBytes(atlasImage)),
    )
}

private fun measureGlyph(measureFont: Font, frc: FontRenderContext, ascentEm: Float, char: Char): GlyphMetrics {
    val gv = measureFont.createGlyphVector(frc, char.toString())
    val advanceEm = gv.getGlyphMetrics(0).advanceX / LOGICAL_CELL
    val bounds = gv.getGlyphOutline(0).bounds2D
    if (bounds.isEmpty) {
        return GlyphMetrics(0f, 0f, 0f, 0f, advanceEm)
    }
    return GlyphMetrics(
        offsetXEm = (bounds.x / LOGICAL_CELL).toFloat(),
        offsetYEm = ((bounds.y + ascentEm) / LOGICAL_CELL).toFloat(),
        widthEm = (bounds.width / LOGICAL_CELL).toFloat(),
        heightEm = (bounds.height / LOGICAL_CELL).toFloat(),
        advanceEm = advanceEm,
    )
}

/**
 * The atlas pixel rect sampled for a glyph's texture quad. Deliberately derived from the same
 * outline-based [metrics] used for the quad geometry, scaled into oversampled raster space, with
 * [CROP_BLEED] raster pixels of headroom added on every side purely so the antialiased edge isn't
 * hard-clipped when sampled. That bleed affects ONLY this sample rect -- unlike the old Python
 * generator, it is never fed back into [metrics]/quad geometry, so widening it cannot grow a
 * glyph's rendered quad without also growing its advance (the old overlap bug).
 */
private fun sampleRect(
    metrics: GlyphMetrics,
    cellX: Int,
    cellY: Int,
    cellWidthPx: Int,
    cellHeightPx: Int,
    baselineX: Int,
    baselineY: Float,
    ascentPxRender: Float,
): List<Int> {
    if (metrics.widthEm <= 0f || metrics.heightEm <= 0f) {
        val placeholder = max(1, (LOGICAL_CELL * OVERSAMPLE) / 4)
        return listOf(baselineX, cellY + PADDING, baselineX + placeholder, cellY + PADDING + placeholder)
    }
    val scale = LOGICAL_CELL * OVERSAMPLE
    val lineTop = baselineY - ascentPxRender
    val rawLeft = baselineX + metrics.offsetXEm * scale
    val rawTop = lineTop + metrics.offsetYEm * scale
    val rawRight = rawLeft + metrics.widthEm * scale
    val rawBottom = rawTop + metrics.heightEm * scale
    return listOf(
        max(cellX, floor(rawLeft - CROP_BLEED).toInt()),
        max(cellY, floor(rawTop - CROP_BLEED).toInt()),
        min(cellX + cellWidthPx, ceil(rawRight + CROP_BLEED).toInt()),
        min(cellY + cellHeightPx, ceil(rawBottom + CROP_BLEED).toInt()),
    )
}

private const val CROP_BLEED = 1

private fun grayBytes(image: BufferedImage): ByteArray {
    val data = (image.raster.dataBuffer as DataBufferByte).data
    return data.copyOf(image.width * image.height)
}

private fun buildFileSpec(atlas: AtlasResult): FileSpec {
    val dataInterface = ClassName(FONT_PACKAGE, "PackedUiFontData")
    val samplingModeType = ClassName(FONT_PACKAGE, "UiFontSamplingMode")

    val typeSpec = TypeSpec.objectBuilder(OBJECT_NAME)
        .addKdoc(
            "Generated by `:awake:engine:ui:font-atlas-generator` -- do not hand-edit. Re-run " +
                "that module's `generateFontAtlas` task and commit the diff if the font or atlas " +
                "metrics change.",
        )
        .addModifiers(KModifier.INTERNAL)
        .addSuperinterface(dataInterface)
        .addProperty(overrideProperty("name", STRING, "%S", DISPLAY_NAME))
        .addProperty(overrideProperty("baseCellSize", INT, "%L", LOGICAL_CELL))
        .addProperty(overrideProperty("lineHeightEm", FLOAT, "%Lf", "%.6f".format(atlas.lineHeightEm)))
        .addProperty(overrideProperty("textScaleStep", FLOAT, "0.25f"))
        .addProperty(overrideProperty("atlasWidth", INT, "%L", atlas.atlasWidth))
        .addProperty(overrideProperty("atlasHeight", INT, "%L", atlas.atlasHeight))
        .addProperty(
            PropertySpec.builder("samplingMode", samplingModeType)
                .addModifiers(KModifier.OVERRIDE)
                .initializer("%T.CoverageAlpha", samplingModeType)
                .build(),
        )
        .addProperty(overrideProperty("fallbackChar", CHAR, "'?'"))
        .addProperty(overrideProperty("glyphOrder", STRING, "%S", atlas.glyphOrder))
        .addProperty(intArrayProperty("uvBoundsPx", atlas.uvBoundsPx))
        .addProperty(floatArrayProperty("quadMetricsEm", atlas.quadMetricsEm))
        .addProperty(floatArrayProperty("advancesEm", atlas.advancesEm))
        .addProperty(base64Property(atlas.encodedAlphaBase64))
        .build()

    return FileSpec.builder(FONT_PACKAGE, OBJECT_NAME)
        .addFileComment("Copyright (c) Ron June Valdoz\nSPDX-License-Identifier: Apache-2.0\n")
        .addType(typeSpec)
        .build()
}

private fun overrideProperty(
    name: String,
    type: com.squareup.kotlinpoet.TypeName,
    format: String,
    vararg args: Any,
): PropertySpec =
    PropertySpec.builder(name, type)
        .addModifiers(KModifier.OVERRIDE)
        .initializer(format, *args)
        .build()

private fun intArrayProperty(name: String, values: IntArray): PropertySpec =
    PropertySpec.builder(name, ClassName("kotlin", "IntArray"))
        .addModifiers(KModifier.OVERRIDE)
        .initializer("intArrayOf(%L)", values.joinToString(", "))
        .build()

private fun floatArrayProperty(name: String, values: FloatArray): PropertySpec =
    PropertySpec.builder(name, ClassName("kotlin", "FloatArray"))
        .addModifiers(KModifier.OVERRIDE)
        .initializer("floatArrayOf(%L)", values.joinToString(", ") { "%.6ff".format(it) })
        .build()

private fun base64Property(base64: String): PropertySpec {
    val block = CodeBlock.builder().add("\"\"\"\n")
    base64.chunked(120).forEach { line -> block.add("%L\n", line) }
    block.add("\"\"\"")
    return PropertySpec.builder("encodedAlphaBase64", STRING)
        .addModifiers(KModifier.OVERRIDE)
        .initializer(block.build())
        .build()
}
