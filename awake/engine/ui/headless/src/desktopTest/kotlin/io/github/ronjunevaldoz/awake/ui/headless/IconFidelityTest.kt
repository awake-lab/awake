// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.testing.ui.rasterize
import io.github.ronjunevaldoz.awake.ui.UiImageVector
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.testSnapshot
import io.github.ronjunevaldoz.awake.ui.uiImageVector
import io.github.ronjunevaldoz.awake.ui.unstyled.HeroIcons
import io.github.ronjunevaldoz.awake.ui.unstyled.components.icon
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Proves each shipped [io.github.ronjunevaldoz.awake.ui.unstyled.HeroIcons] glyph renders the *same shape* as the official Heroicons SVG
 * it was generated from, automatically -- see `skills/awake-icon-authoring/SKILL.md`. Three icon
 * defects shipped in one session before this existed, and every one was only caught by a human
 * looking at a picture.
 *
 * Both sides render the same viewBox at the same pixel size through independent pipelines
 * (Chromium/Skia for the reference, `UiRasterizer`'s software rasterizer for ours), so this is
 * shape-based coverage-mask comparison (intersection-over-union of "is this pixel part of the
 * white glyph"), not a byte-identical pixel diff -- see [passThreshold]'s doc comment for the
 * measured correct-vs-corrupted separation that sets the gate.
 */
class IconFidelityTest {

    private val size = 128

    private fun repoRoot(): File {
        var dir = File(System.getProperty("user.dir")).absoluteFile
        while (!File(dir, "settings.gradle.kts").exists()) {
            dir = dir.parentFile
                ?: error(
                    "could not locate repo root (settings.gradle.kts) above ${
                        System.getProperty(
                            "user.dir",
                        )
                    }",
                )
        }
        return dir
    }

    private data class ManifestEntry(
        val name: String,
        val kotlinTier: String,
        val kotlinProperty: String,
    ) {
        val fileStem get() = "$name-$kotlinTier"
    }

    // heroicons_manifest.json is a flat array of flat string-valued objects, generated only by
    // tools/capture_heroicons_reference.py -- a tiny regex scan avoids pulling a JSON library
    // into ui-headless's desktopTest classpath just to read four fields we control ourselves.
    private fun loadManifest(): List<ManifestEntry> {
        val file = File(repoRoot(), "tools/heroicons_manifest.json")
        val objectPattern = Regex("\\{[^}]*}")
        val fieldPattern = Regex("\"(\\w+)\"\\s*:\\s*\"([^\"]*)\"")
        return objectPattern.findAll(file.readText()).map { match ->
            val fields = fieldPattern.findAll(match.value)
                .associate { it.groupValues[1] to it.groupValues[2] }
            ManifestEntry(
                name = fields.getValue("name"),
                kotlinTier = fields.getValue("kotlinTier"),
                kotlinProperty = fields.getValue("kotlinProperty"),
            )
        }.toList()
    }

    // Explicit, not reflective -- 19 entries is little enough code that a typo shows up as a
    // compile error instead of a silently-skipped icon.
    private fun vectorFor(entry: ManifestEntry): UiImageVector =
        when (entry.kotlinTier to entry.kotlinProperty) {
            "Solid20Mini" to "xMark" -> HeroIcons.Solid20Mini.xMark
            "Solid20Mini" to "chevronDown" -> HeroIcons.Solid20Mini.chevronDown
            "Solid20Mini" to "chevronUp" -> HeroIcons.Solid20Mini.chevronUp
            "Solid20Mini" to "chevronLeft" -> HeroIcons.Solid20Mini.chevronLeft
            "Solid20Mini" to "chevronRight" -> HeroIcons.Solid20Mini.chevronRight
            "Solid20Mini" to "squares2x2" -> HeroIcons.Solid20Mini.squares2x2
            "Solid20Mini" to "square3Stack3d" -> HeroIcons.Solid20Mini.square3Stack3d
            "Solid20Mini" to "tableCells" -> HeroIcons.Solid20Mini.tableCells
            "Solid20Mini" to "globeAlt" -> HeroIcons.Solid20Mini.globeAlt
            "Solid20Mini" to "clock" -> HeroIcons.Solid20Mini.clock
            "Solid20Mini" to "arrowPath" -> HeroIcons.Solid20Mini.arrowPath
            "Solid20Mini" to "camera" -> HeroIcons.Solid20Mini.camera
            "Solid20Mini" to "sparkles" -> HeroIcons.Solid20Mini.sparkles
            "Solid20Mini" to "cube" -> HeroIcons.Solid20Mini.cube
            "Solid20Mini" to "cog6Tooth" -> HeroIcons.Solid20Mini.cog6Tooth
            "Solid20Mini" to "arrowUpTray" -> HeroIcons.Solid20Mini.arrowUpTray
            "Solid20Mini" to "arrowDownTray" -> HeroIcons.Solid20Mini.arrowDownTray
            "Solid20Mini" to "puzzlePiece" -> HeroIcons.Solid20Mini.puzzlePiece
            "Solid20Mini" to "pencilSquare" -> HeroIcons.Solid20Mini.pencilSquare
            "Solid20Mini" to "play" -> HeroIcons.Solid20Mini.play
            "Solid20Mini" to "magnifyingGlass" -> HeroIcons.Solid20Mini.magnifyingGlass
            "Solid20Mini" to "videoCamera" -> HeroIcons.Solid20Mini.videoCamera
            "Solid20Mini" to "eye" -> HeroIcons.Solid20Mini.eye
            "Solid20Mini" to "eyeSlash" -> HeroIcons.Solid20Mini.eyeSlash
            "Solid20Mini" to "sun" -> HeroIcons.Solid20Mini.sun
            "Solid20Mini" to "user" -> HeroIcons.Solid20Mini.user
            "Solid20Mini" to "documentText" -> HeroIcons.Solid20Mini.documentText
            "Solid20Mini" to "plus" -> HeroIcons.Solid20Mini.plus
            "Solid20Mini" to "trash" -> HeroIcons.Solid20Mini.trash
            "Solid20Mini" to "lockClosed" -> HeroIcons.Solid20Mini.lockClosed
            "Solid20Mini" to "bars3" -> HeroIcons.Solid20Mini.bars3
            "Solid20Mini" to "funnel" -> HeroIcons.Solid20Mini.funnel
            "Solid20Mini" to "adjustmentsHorizontal" -> HeroIcons.Solid20Mini.adjustmentsHorizontal
            "Solid20Mini" to "stop" -> HeroIcons.Solid20Mini.stop
            "Solid20Mini" to "pause" -> HeroIcons.Solid20Mini.pause
            "Solid24" to "arrowDownOnSquareStack" -> HeroIcons.Solid24.arrowDownOnSquareStack
            "Solid24" to "arrowUpOnSquareStack" -> HeroIcons.Solid24.arrowUpOnSquareStack
            "Solid24" to "circleStack" -> HeroIcons.Solid24.circleStack
            "Solid24" to "inboxStack" -> HeroIcons.Solid24.inboxStack
            "Solid24" to "rectangleStack" -> HeroIcons.Solid24.rectangleStack
            "Solid24" to "serverStack" -> HeroIcons.Solid24.serverStack
            "Solid24" to "square2Stack" -> HeroIcons.Solid24.square2Stack
            "Solid24" to "square3Stack3d" -> HeroIcons.Solid24.square3Stack3d
            "Outline24" to "academicCap" -> HeroIcons.Outline24.academicCap
            "Outline24" to "clock" -> HeroIcons.Outline24.clock
            "Outline24" to "camera" -> HeroIcons.Outline24.camera
            "Outline24" to "arrowPath" -> HeroIcons.Outline24.arrowPath
            else -> error("no vectorFor mapping for ${entry.kotlinTier}.${entry.kotlinProperty} -- add one")
        }

    /** Renders through the real headless pipeline: [UiContext] -> `icon()` widget -> the same
     * CPU [rasterize] `UiSnapshotWriter`/`SkeletonShimmerPixelTest` use for real-pixel proof. */
    private fun renderIcon(vector: UiImageVector): ByteArray {
        val ui = UiContext()
        ui.beginFrame(size.toFloat(), size.toFloat(), testSnapshot(), deltaSeconds = 1f / 60f)
        val scope = ui.createAbsolute(x = 0f, y = 0f)
        scope.icon(vector, modifier = Modifier.width(size.dp).height(size.dp), tint = Color.White)
        val primitives = ui.endFrame()
        return primitives.rasterize(size, size, background = Color.Black)
    }

    /** White-on-black at [size]px in both pipelines -- luminance>50% is "part of the glyph". */
    private fun coverageMask(rgba: ByteArray): BooleanArray = BooleanArray(size * size) { i ->
        val o = i * 4
        val r = rgba[o].toInt() and 0xFF
        val g = rgba[o + 1].toInt() and 0xFF
        val b = rgba[o + 2].toInt() and 0xFF
        (r + g + b) / 3 > 128
    }

    private fun coverageMask(image: BufferedImage): BooleanArray {
        val mask = BooleanArray(image.width * image.height)
        var i = 0
        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                val rgb = image.getRGB(x, y)
                val r = (rgb shr 16) and 0xFF
                val g = (rgb shr 8) and 0xFF
                val b = rgb and 0xFF
                mask[i] = (r + g + b) / 3 > 128
                i++
            }
        }
        return mask
    }

    private data class Metrics(val iou: Float, val mismatchPct: Float)

    private fun compare(reference: BooleanArray, ours: BooleanArray): Metrics {
        var intersection = 0
        var union = 0
        var mismatch = 0
        for (i in reference.indices) {
            if (reference[i] || ours[i]) union++
            if (reference[i] && ours[i]) intersection++
            if (reference[i] != ours[i]) mismatch++
        }
        val iou = if (union == 0) 1f else intersection.toFloat() / union
        return Metrics(iou, mismatch.toFloat() / reference.size * 100f)
    }

    /** reference | ours | diff (gray=both, red=reference-only/"missing", cyan=ours-only/"extra")
     * -- a human can eyeball exactly where a shape diverges without reading numbers. */
    private fun writeDiffPng(
        reference: BufferedImage,
        oursRgba: ByteArray,
        referenceMask: BooleanArray,
        oursMask: BooleanArray,
        outFile: File,
    ) {
        val composite = BufferedImage(size * 3, size, BufferedImage.TYPE_INT_ARGB)
        for (y in 0 until size) {
            for (x in 0 until size) {
                composite.setRGB(x, y, reference.getRGB(x, y) or (0xFF shl 24))
                val o = (y * size + x) * 4
                val r = oursRgba[o].toInt() and 0xFF
                val g = oursRgba[o + 1].toInt() and 0xFF
                val b = oursRgba[o + 2].toInt() and 0xFF
                composite.setRGB(size + x, y, (0xFF shl 24) or (r shl 16) or (g shl 8) or b)
                val i = y * size + x
                val diffArgb = when {
                    referenceMask[i] && oursMask[i] -> 0xFF303030.toInt()
                    referenceMask[i] && !oursMask[i] -> 0xFFFF0000.toInt()
                    !referenceMask[i] && oursMask[i] -> 0xFF00FFFF.toInt()
                    else -> 0xFF000000.toInt()
                }
                composite.setRGB(size * 2 + x, y, diffArgb)
            }
        }
        outFile.parentFile.mkdirs()
        ImageIO.write(composite, "png", outFile)
    }

    /**
     * Pass threshold justification (measured, not guessed -- see [corruptedIconIsCaughtByTheGuard]):
     * every one of the 19 shipped icons measured IoU 0.9664-0.9971 against its reference (worst:
     * the four 20/solid chevrons at ~0.966-0.971; the small loss versus 1.0 is Chromium/Skia vs
     * our rasterizer's differing antialiasing coverage at glyph edges, not a shape error). A
     * deliberately corrupted vector (a plain full-viewport square substituted for
     * `chevron-down`'s real path data, built in-test -- never edits shipped data) measured
     * IoU=0.049 against the same reference. That is a ~19x gap between the two clusters (worst
     * real icon 0.966 vs corrupted 0.049); 0.75 sits in the middle of that gap, nowhere near
     * either side, so AA noise can never false-fail and a real shape regression cannot hide in
     * the margin.
     */
    private val passThreshold = 0.75f

    @Test
    fun everyShippedIconMatchesItsHeroiconsReference() {
        val manifest = loadManifest()
        val reportDir = File("build/reports/icon-fidelity")
        val results = manifest.map { entry ->
            val referenceFile =
                File(repoRoot(), "docs/reference/icon-previews/${entry.fileStem}.png")
            val reference = ImageIO.read(referenceFile)
                ?: error("missing reference PNG for ${entry.fileStem} -- run tools/capture_heroicons_reference.py")
            val oursRgba = renderIcon(vectorFor(entry))
            val referenceMask = coverageMask(reference)
            val oursMask = coverageMask(oursRgba)
            val metrics = compare(referenceMask, oursMask)
            writeDiffPng(
                reference,
                oursRgba,
                referenceMask,
                oursMask,
                File(reportDir, "${entry.fileStem}.png"),
            )
            Triple(entry.fileStem, metrics.iou, metrics.mismatchPct)
        }.sortedBy { it.second }

        reportDir.mkdirs()
        File(reportDir, "metrics.tsv").writeText(
            "icon\tiou\tmismatchPct\n" +
                results.joinToString("\n") { (name, iou, mismatchPct) -> "$name\t$iou\t$mismatchPct" } + "\n",
        )

        val failures = results.filter { it.second < passThreshold }
        assertTrue(
            failures.isEmpty(),
            "icon(s) below the fidelity threshold (IoU >= $passThreshold) -- inspect " +
                "build/reports/icon-fidelity/<icon>.png (reference | ours | diff):\n" +
                failures.joinToString("\n") { (name, iou, mismatchPct) -> "  $name: iou=$iou mismatchPct=$mismatchPct%" },
        )
    }

    /**
     * Proves the guard actually fails on a real regression instead of always passing. Builds a
     * deliberately wrong vector in-test (never touches [HeroIcons]/`chevron-down`'s shipped
     * data) and confirms its IoU against the real `chevron-down` reference collapses far below
     * [passThreshold] -- the measured drop that [passThreshold]'s doc comment cites.
     */
    @Test
    fun corruptedIconIsCaughtByTheGuard() {
        val correct = HeroIcons.Solid20Mini.chevronDown
        val corrupted = uiImageVector(
            defaultWidth = correct.defaultWidth,
            defaultHeight = correct.defaultHeight,
            viewportWidth = correct.viewportWidth,
            viewportHeight = correct.viewportHeight,
        ) {
            // A full-viewport filled square has nothing to do with a chevron's shape --
            // stands in for "path data got mangled" without editing shipped icon data.
            path {
                moveTo(0f, 0f)
                lineTo(correct.viewportWidth, 0f)
                lineTo(correct.viewportWidth, correct.viewportHeight)
                lineTo(0f, correct.viewportHeight)
                close()
            }
        }

        val referenceFile =
            File(repoRoot(), "docs/reference/icon-previews/chevron-down-Solid20Mini.png")
        val reference = ImageIO.read(referenceFile)
            ?: error("missing reference PNG -- run tools/capture_heroicons_reference.py")
        val referenceMask = coverageMask(reference)

        val correctMetrics = compare(referenceMask, coverageMask(renderIcon(correct)))
        val corruptedMetrics = compare(referenceMask, coverageMask(renderIcon(corrupted)))

        assertTrue(
            correctMetrics.iou >= passThreshold,
            "sanity: the real chevron-down must pass its own threshold (iou=${correctMetrics.iou})",
        )
        assertTrue(
            corruptedMetrics.iou < passThreshold,
            "the guard must reject a corrupted icon: corrupted iou=${corruptedMetrics.iou} " +
                "vs correct iou=${correctMetrics.iou} (threshold=$passThreshold)",
        )
    }
}
