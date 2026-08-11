// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
// Suppressed deliberately, and only here. This is a build-time generator, not shipped code:
// - LongParameterList: the cell-geometry arguments (cellX/cellY/cellWidth/cellHeight/baseline/
//   ascent) travel together through the packing loop, and AtlasResult is a DTO mirroring the
//   generated PackedUiFontData one-for-one. Grouping either into holder types would add
//   indirection between this file and the interface it emits without making it easier to read.
// - TooManyFunctions: 12 small top-level helpers, each named for one step of the pipeline,
//   reads better here than fewer, larger ones.
@file:Suppress("LongParameterList", "TooManyFunctions", "DestructuringDeclarationWithTooManyEntries")

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
import java.awt.Font
import java.awt.font.FontRenderContext
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import java.util.Base64
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min


private const val FONT_PACKAGE = "io.github.ronjunevaldoz.awake.ui.font"
/** Printable ASCII, space through tilde -- the range this atlas packs. */
private const val ASCII_FIRST = 32
private const val ASCII_LAST = 126
private val ASCII_GLYPHS: List<Char> = (ASCII_FIRST..ASCII_LAST).map { it.toChar() }

private const val FONT_PATH = "../ui-core/src/commonMain/resources/fonts/Roboto-Regular.ttf"
private const val OUT_DIR = "../ui-core/src/commonMain/kotlin"
private const val OBJECT_NAME = "RobotoRegularUiFontData"
private const val DISPLAY_NAME = "Roboto Regular"
private const val LOGICAL_CELL = 16
/** Atlas texels per em, as a multiple of [LOGICAL_CELL].
 *
 * Was 4 for the coverage atlas, which needed the extra raster resolution because a coverage
 * bitmap is only sharp near a 1:1 texel mapping. An MTSDF encodes the outline as a distance,
 * so it stays sharp at any size and gains nothing from oversampling. RGBA is already 4x the
 * bytes of coverage alpha, so keeping 4 here produced a 3.3 MB generated Kotlin source for no
 * rendering benefit. 2 keeps enough texels for msdfgen to resolve fine detail while landing at
 * roughly the old atlas's file size. */
private const val OVERSAMPLE = 2
private const val PADDING = 6
private const val COLUMNS = 16
private const val MSDFGEN = "msdfgen"
private const val RGBA_CHANNELS = 4
private const val BYTE_MASK = 0xFF
private const val ALPHA_SHIFT = 24
private const val RED_SHIFT = 16
private const val GREEN_SHIFT = 8
private const val ALPHA_BYTE = 3

/** A blank glyph (space) has no ink, so its UV rect is a small placeholder square rather than a
 * measured one -- a quarter of the cell, big enough to be obviously wrong if ever sampled. */
private const val BLANK_GLYPH_DIVISOR = 4

/** Wrap the base64 payload so the generated Kotlin stays diffable. */
private const val BASE64_LINE_WIDTH = 120

/** Distance-field spread in ATLAS TEXELS, passed to msdfgen as `-pxrange` and shipped as
 * `distanceFieldRangePx` so the glyph shader recovers the same range.
 *
 * Must stay well under the narrowest stem the atlas has to represent. A distance field cannot
 * encode a feature thinner than its own spread -- at 4, with [OVERSAMPLE] 2 putting an em at 32
 * texels, 'i' and 'l' stems (~2 texels) were narrower than the range and rendered visibly
 * eroded next to rounder glyphs. */
private const val DISTANCE_FIELD_RANGE_PX = 2

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
    val inkMetricsEm: FloatArray,
    val advancesEm: FloatArray,
    val encodedAtlasBase64: String,
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

    val atlasImage = BufferedImage(atlasWidth, atlasHeight, BufferedImage.TYPE_INT_ARGB)

    val uvBoundsPx = mutableListOf<Int>()
    val quadMetricsEm = mutableListOf<Float>()
    val inkMetricsEm = mutableListOf<Float>()
    val advancesEm = mutableListOf<Float>()

    ASCII_GLYPHS.forEachIndexed { index, char ->
        val col = index % COLUMNS
        val row = index / COLUMNS
        val cellX = col * cellWidthPx
        val cellY = row * cellHeightPx
        val baselineX = cellX + PADDING
        val baselineY = cellY + PADDING + ascentPxRender

        blitGlyphCell(
            atlasImage = atlasImage,
            fontFile = fontFile,
            char = char,
            cellX = cellX,
            cellY = cellY,
            cellWidthPx = cellWidthPx,
            cellHeightPx = cellHeightPx,
            ascentPxRender = ascentPxRender,
        )

        val metrics = glyphMetrics.getValue(char)
        val (left, top, right, bottom) = sampleRect(metrics, cellX, cellY, cellWidthPx, cellHeightPx, baselineX, baselineY, ascentPxRender)

        uvBoundsPx += listOf(left, top, right, bottom)
        // The RENDER quad must cover exactly what the UV rect samples -- the outline rect plus
        // CROP_BLEED plus the integer texel snap -- or the atlas region gets squeezed into an
        // outline-sized quad and every glyph renders at ink/(ink + bleed + snap) of its metrics
        // (~0.90x, growing past a pixel from 16px up), with the per-glyph snap scattering
        // baselines by a subpixel. Convert the snapped sample rect back to pen-relative em so
        // quad and UV describe the same texels 1:1. Ink metrics ship separately below; blank
        // glyphs keep a zero quad rather than the placeholder sample square.
        val scale = LOGICAL_CELL * OVERSAMPLE
        val lineTop = baselineY - ascentPxRender
        if (metrics.widthEm <= 0f || metrics.heightEm <= 0f) {
            quadMetricsEm += listOf(0f, 0f, 0f, 0f)
        } else {
            quadMetricsEm += listOf(
                (left - baselineX).toFloat() / scale,
                (top - lineTop) / scale,
                (right - left).toFloat() / scale,
                (bottom - top).toFloat() / scale,
            )
        }
        inkMetricsEm += listOf(metrics.offsetXEm, metrics.offsetYEm, metrics.widthEm, metrics.heightEm)
        advancesEm += metrics.advanceEm
    }

    return AtlasResult(
        lineHeightEm = (ascentPxRender + renderLineMetrics.descent) / OVERSAMPLE / LOGICAL_CELL,
        atlasWidth = atlasWidth,
        atlasHeight = atlasHeight,
        glyphOrder = ASCII_GLYPHS.joinToString(""),
        uvBoundsPx = uvBoundsPx.toIntArray(),
        quadMetricsEm = quadMetricsEm.toFloatArray(),
        inkMetricsEm = inkMetricsEm.toFloatArray(),
        advancesEm = advancesEm.toFloatArray(),
        encodedAtlasBase64 = Base64.getEncoder().encodeToString(rgbaBytes(atlasImage)),
    )
}

/**
 * Renders one glyph's MTSDF tile with `msdfgen` and blits it into [atlasImage] at the glyph's
 * cell.
 *
 * Placement reproduces exactly where the old `Graphics2D.drawGlyphVector` put the glyph, so the
 * UV derivation in [sampleRect] needs no changes: the glyph's baseline origin must land at cell
 * pixel `(PADDING, PADDING + ascentPxRender)`.
 *
 * Three coordinate conventions have to be reconciled, which is the whole difficulty here:
 * msdfgen's shape space is Y-UP with the baseline at zero, `-emnormalize` puts it in em units,
 * and the atlas is Y-DOWN. msdfgen maps a shape point to `(point + translate) * scale` measured
 * up from the image BOTTOM, so placing the baseline `PADDING + ascentPxRender` down from the
 * cell top means translating it `cellHeightPx - PADDING - ascentPxRender` up from the bottom.
 */
private fun blitGlyphCell(
    atlasImage: BufferedImage,
    fontFile: File,
    char: Char,
    cellX: Int,
    cellY: Int,
    cellWidthPx: Int,
    cellHeightPx: Int,
    ascentPxRender: Float,
) {
    val scale = (LOGICAL_CELL * OVERSAMPLE).toDouble()
    val translateX = PADDING / scale
    val translateY = (cellHeightPx - PADDING - ascentPxRender) / scale
    val out = File.createTempFile("awake-msdf-", ".png")
    try {
        val process = ProcessBuilder(
            MSDFGEN, "mtsdf",
            "-font", fontFile.absolutePath, char.code.toString(),
            "-emnormalize",
            "-size", cellWidthPx.toString(), cellHeightPx.toString(),
            "-pxrange", DISTANCE_FIELD_RANGE_PX.toString(),
            "-translate", translateX.toString(), translateY.toString(),
            "-scale", scale.toString(),
            "-o", out.absolutePath,
        ).redirectErrorStream(true).start()
        val log = process.inputStream.bufferedText()
        check(process.waitFor() == 0) { "msdfgen failed for '$char' (${char.code}):\n$log" }
        // A blank glyph (space) legitimately produces no output file.
        if (!out.exists() || out.length() == 0L) return
        val tile = ImageIO.read(out) ?: return
        for (y in 0 until minOf(tile.height, cellHeightPx)) {
            for (x in 0 until minOf(tile.width, cellWidthPx)) {
                atlasImage.setRGB(cellX + x, cellY + y, tile.getRGB(x, y))
            }
        }
    } finally {
        out.delete()
    }
}

private fun java.io.InputStream.bufferedText(): String = reader().use { it.readText() }

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
 * The atlas pixel rect sampled for a glyph's texture quad: the outline-based [metrics] rect
 * scaled into oversampled raster space, with [CROP_BLEED] texels of headroom on every side so
 * the distance field's antialiased edge isn't hard-clipped, snapped outward to whole texels.
 * The render quad (`quadMetricsEm`) is derived from THIS snapped rect so quad and UV always
 * describe the same texels 1:1; only ink metrics (`inkMetricsEm`) and advances stay
 * outline-true, so the bleed can never inflate a glyph's measured size or its advance (the old
 * overlap bug).
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
        val placeholder = max(1, (LOGICAL_CELL * OVERSAMPLE) / BLANK_GLYPH_DIVISOR)
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

/** Flattens to tightly-packed RGBA8, the layout `PackedUiFont.decodeAtlasPixels` hands straight
 * to a `VK_FORMAT_R8G8B8A8_UNORM` upload when `atlasChannels` is 4. */
private fun rgbaBytes(image: BufferedImage): ByteArray {
    val bytes = ByteArray(image.width * image.height * RGBA_CHANNELS)
    var index = 0
    for (y in 0 until image.height) {
        for (x in 0 until image.width) {
            val argb = image.getRGB(x, y)
            bytes[index] = ((argb shr RED_SHIFT) and BYTE_MASK).toByte()
            bytes[index + 1] = ((argb shr GREEN_SHIFT) and BYTE_MASK).toByte()
            bytes[index + 2] = (argb and BYTE_MASK).toByte()
            bytes[index + ALPHA_BYTE] = ((argb ushr ALPHA_SHIFT) and BYTE_MASK).toByte()
            index += RGBA_CHANNELS
        }
    }
    return bytes
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
                .initializer("%T.DistanceField", samplingModeType)
                .build(),
        )
        .addProperty(overrideProperty("fallbackChar", CHAR, "'?'"))
        .addProperty(overrideProperty("atlasChannels", INT, "%L", RGBA_CHANNELS))
        .addProperty(overrideProperty("distanceFieldRangePx", FLOAT, "%Lf", DISTANCE_FIELD_RANGE_PX))
        .addProperty(overrideProperty("glyphOrder", STRING, "%S", atlas.glyphOrder))
        .addProperty(intArrayProperty("uvBoundsPx", atlas.uvBoundsPx))
        .addProperty(floatArrayProperty("quadMetricsEm", atlas.quadMetricsEm))
        .addProperty(floatArrayProperty("inkMetricsEm", atlas.inkMetricsEm))
        .addProperty(floatArrayProperty("advancesEm", atlas.advancesEm))
        .addProperty(base64Property(atlas.encodedAtlasBase64))
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
    base64.chunked(BASE64_LINE_WIDTH).forEach { line -> block.add("%L\n", line) }
    block.add("\"\"\"")
    return PropertySpec.builder("encodedAtlasBase64", STRING)
        .addModifiers(KModifier.OVERRIDE)
        .initializer(block.build())
        .build()
}
