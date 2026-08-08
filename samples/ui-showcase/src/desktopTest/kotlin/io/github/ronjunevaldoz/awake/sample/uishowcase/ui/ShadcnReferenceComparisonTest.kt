// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

/**
 * Answers a different question than [ShadcnParityScreenshotTest]. That test diffs Awake's
 * render against Awake's *own* previously recorded golden -- a regression lock, useful for
 * "did this change on purpose" but blind to "is this actually close to real shadcn/ui". This
 * test diffs Awake's render against a real `ui.shadcn.com` capture (every PNG checked into
 * `docs/reference/shadcn-previews`, refreshed by `tools/capture_shadcn_reference.py` straight
 * from the live docs site -- see that script's header for why the previous set of "shadcn
 * reference" images wasn't actually from shadcn/ui at all).
 *
 * Deliberately non-failing: mismatch against the real reference is real, expected, and not
 * something this pass is fixing -- see docs/reference/ui-validation.md's harness section. This
 * only asserts the harness itself ran and produced a report a human can open.
 *
 * Renders its own Awake-side previews (rather than depending on [ShadcnParityScreenshotTest]
 * having already run in the same invocation) so `--tests "*ShadcnReferenceComparisonTest*"`
 * alone is sufficient -- Gradle doesn't guarantee cross-class ordering under a test filter.
 */
import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreview
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewEntry
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewFrame
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewMetadata
import io.github.ronjunevaldoz.awake.testing.ui.renderAnnotatedUiPreviews
import io.github.ronjunevaldoz.awake.testing.ui.saveAwakeUiPreview
import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.controls.shadcnInput
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCard
import io.github.ronjunevaldoz.awake.ui.designsystem.shadcnTheme
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.headless.input.text.text
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.layouts.spacer
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.offset
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.toUiInputState
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.math.round
import kotlin.test.Test
import kotlin.test.assertTrue

private fun comparisonTestSnapshot(): UiInputState {
    val input = Input()
    input.setPointer(down = false, x = -100f, y = -100f)
    return input.updateSnapshot().toUiInputState()
}

/** [ShadcnParityScreenshotTest] has no Card entry yet (shadcn-parity.md's inventory still
 * lists `shadcnSurface(variant = Card)`, superseded by the dedicated [shadcnCard] recipe) --
 * added here rather than in that file, per this task's "new test file only" scope. */
@AwakeUiPreview(
    id = "awake-card-light",
    title = "Awake Card (light)",
    group = "Shadcn Parity",
    summary = "Matches docs/reference/shadcn-previews/card_states_light.png's login-card arrangement (header/body/footer) for a direct side-by-side. " +
        "Canvas hugs the card's own bounding box instead of the old 320x300 canvas (content was already 272x276, " +
        "close to the full canvas -- this pair was already the harness's one 'good' crop, tightened further here).",
    width = 288,
    height = 292,
)
internal object AwakeCardLightPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame {
        val theme = shadcnTheme(dark = false)
        val font = UiFonts.default()
        val ui = UiContext()
        ui.beginFrame(metadata.width.toFloat(), metadata.height.toFloat(), comparisonTestSnapshot())
        ui.pushFont(font)
        ui.pushTheme(theme)
        ui.column(
            modifier = Modifier.offset(8f.dp, 8f.dp).width(272f.dp)
                .height((metadata.height.toFloat() - 16f).dp),
        ) {
            shadcnCard(
                id = "parity-card",
                modifier = Modifier.width(272f.px),
                header = {
                    text("Login to your account", style = Style { textSize(theme.typography.title) })
                },
                footer = {
                    shadcnButton("parity-card-login", "Login", modifier = Modifier.width(240f.px).height(36f.px))
                },
            ) { _ ->
                shadcnInput("parity-card-email", value = "", placeholder = "Email", modifier = Modifier.width(240f.px).height(36f.px))
                spacer(Modifier.height(12f.dp))
                shadcnInput("parity-card-password", value = "", placeholder = "Password", modifier = Modifier.width(240f.px).height(36f.px))
            }
        }
        return AwakeUiPreviewFrame(
            primitives = ui.endFrame(),
            background = theme.colors.background,
            font = font,
            semantics = ui.semanticNodes(),
        )
    }
}

@Serializable
private data class ShadcnParityPair(
    val name: String,
    val awake: String,
    val reference: String,
    /** True when the reference comes from the local reference app rather than the legacy
     * ui.shadcn.com scrape. Local references render from the pinned shadcn checkout, so they
     * are authoritative; the scrape applies its own docs-site theming, which once produced a
     * token-bug report against us that the pinned source disproved. */
    val local: Boolean = false,
)

@Serializable
private data class ShadcnParityManifest(val pairs: List<ShadcnParityPair>)

/** One row of the report -- both the printed summary table and
 * `build/reports/shadcn-parity-metrics.json` are built from this. */
@Serializable
data class ShadcnParityMetric(
    val name: String,
    val mismatchPct: Double,
    val maxChannelDelta: Int,
    val meanDelta: Double,
    val awakeSize: List<Int>,
    val referenceSize: List<Int>,
    val comparedSize: List<Int>,
    val diffImage: String,
)

// Same heuristic and constants as tools/compare_parity.py's trim_uniform_border /
// PIXEL_MISMATCH_DELTA -- keep both in sync if the comparison approach changes.
private const val BORDER_TOLERANCE = 6
private const val PIXEL_MISMATCH_DELTA = 24.0

private fun round2(value: Double): Double = round(value * 100) / 100.0

/** Trims uniform-colour border pixels from every edge inward, down to the image's own real
 * content bounding box. Handles both a tight white-background Awake preview and a
 * full-viewport shadcn capture with a solid backdrop (dialog_states_light.png's gray overlay). */
private fun trimUniformBorder(image: BufferedImage): BufferedImage {
    val w = image.width
    val h = image.height
    val bg = image.getRGB(0, 0)
    fun channels(rgb: Int) = intArrayOf((rgb shr 16) and 0xFF, (rgb shr 8) and 0xFF, rgb and 0xFF)
    val bgChannels = channels(bg)
    fun close(rgb: Int): Boolean {
        val c = channels(rgb)
        return (0..2).all { abs(c[it] - bgChannels[it]) <= BORDER_TOLERANCE }
    }

    var left = 0
    while (left < w && (0 until h).all { y -> close(image.getRGB(left, y)) }) left++
    var right = w
    while (right > left && (0 until h).all { y -> close(image.getRGB(right - 1, y)) }) right--
    var top = 0
    while (top < h && (left until right).all { x -> close(image.getRGB(x, top)) }) top++
    var bottom = h
    while (bottom > top && (left until right).all { x -> close(image.getRGB(x, bottom - 1)) }) bottom--

    if (left >= right || top >= bottom) return image
    return image.getSubimage(left, top, right - left, bottom - top)
}

private fun heatmapColor(delta: Double): Int {
    val t = (delta / (PIXEL_MISMATCH_DELTA * 4)).coerceIn(0.0, 1.0)
    val r = (255 * t).toInt()
    val b = (255 * (1 - t)).toInt()
    return (0xFF shl 24) or (r shl 16) or b
}

/** Aligned-crop perceptual diff: trims each image's own border independently, compares at the
 * intersection size (top-left anchored), and writes a red/blue delta heatmap. Never asserts --
 * callers decide what, if anything, to do with the numbers. */
private fun compareAgainstReference(awakeFile: File, referenceFile: File, outDir: File, name: String): ShadcnParityMetric {
    val awakeImg = ImageIO.read(awakeFile)
    val refImg = ImageIO.read(referenceFile)
    val awakeTrim = trimUniformBorder(awakeImg)
    val refTrim = trimUniformBorder(refImg)
    val w = minOf(awakeTrim.width, refTrim.width)
    val h = minOf(awakeTrim.height, refTrim.height)
    val heatmap = BufferedImage(maxOf(w, 1), maxOf(h, 1), BufferedImage.TYPE_INT_ARGB)

    var mismatches = 0
    var maxChannelDelta = 0
    var sumDelta = 0.0
    for (y in 0 until h) {
        for (x in 0 until w) {
            val a = awakeTrim.getRGB(x, y)
            val r = refTrim.getRGB(x, y)
            val dr = abs(((a shr 16) and 0xFF) - ((r shr 16) and 0xFF))
            val dg = abs(((a shr 8) and 0xFF) - ((r shr 8) and 0xFF))
            val db = abs((a and 0xFF) - (r and 0xFF))
            val delta = (dr + dg + db) / 3.0
            sumDelta += delta
            if (dr > maxChannelDelta) maxChannelDelta = dr
            if (dg > maxChannelDelta) maxChannelDelta = dg
            if (db > maxChannelDelta) maxChannelDelta = db
            if (delta > PIXEL_MISMATCH_DELTA) mismatches++
            heatmap.setRGB(x, y, heatmapColor(delta))
        }
    }

    outDir.mkdirs()
    val diffFile = File(outDir, "${name}_diff.png")
    ImageIO.write(heatmap, "png", diffFile)

    val total = (w * h).coerceAtLeast(1)
    return ShadcnParityMetric(
        name = name,
        mismatchPct = round2(100.0 * mismatches / total),
        maxChannelDelta = maxChannelDelta,
        meanDelta = round2(sumDelta / total),
        awakeSize = listOf(awakeImg.width, awakeImg.height),
        referenceSize = listOf(refImg.width, refImg.height),
        comparedSize = listOf(w, h),
        diffImage = diffFile.path,
    )
}

class ShadcnReferenceComparisonTest {

    @Test
    fun compareAwakePreviewsAgainstRealShadcnReferences() {
        listOf(
            AwakeButtonVariantsLightPreview,
            AwakeBadgeVariantsLightPreview,
            AwakeCheckboxStatesLightPreview,
            AwakeSwitchVariantsLightPreview,
            AwakeTextFieldStatesLightPreview,
            AwakeSelectClosedLightPreview,
            AwakeTabsLightPreview,
            AwakeCardLightPreview,
            AwakeSliderLightPreview,
            AwakeTooltipTriggerLightPreview,
            AwakeDialogStatesLightPreview,
        ).forEach { entry -> renderAnnotatedUiPreviews(entry).forEach { saveAwakeUiPreview(it) } }

        // Gradle's Test task working dir is the module dir (samples/ui-showcase/) -- same
        // CWD-relative convention build/ui-previews already uses elsewhere in this module.
        val manifestFile = File("../../tools/shadcn_parity_pairs.json")
        val localReferenceDir = File("../../docs/reference/shadcn-previews-local")
        val awakeDir = File("build/ui-previews")
        val diffDir = File("build/reports/shadcn-parity")
        val metricsFile = File("build/reports/shadcn-parity-metrics.json")

        val json = Json { ignoreUnknownKeys = true }
        val manifest = json.decodeFromString<ShadcnParityManifest>(manifestFile.readText())

        val results = manifest.pairs.mapNotNull { pair ->
            val awakeFile = File(awakeDir, "${pair.awake}.png")
            val referenceFile = File(localReferenceDir, pair.reference)
            if (!awakeFile.exists() || !referenceFile.exists()) {
                println(
                    "SKIP ${pair.name}: awake exists=${awakeFile.exists()} (${awakeFile.path}), " +
                        "reference exists=${referenceFile.exists()} (${referenceFile.path})",
                )
                null
            } else {
                compareAgainstReference(awakeFile, referenceFile, diffDir, pair.name)
            }
        }.sortedByDescending { it.mismatchPct }

        println("%-10s %10s %10s %11s".format("name", "mismatch%", "maxDelta", "meanDelta"))
        results.forEach { r ->
            println("%-10s %9.2f%% %10d %11.2f".format(r.name, r.mismatchPct, r.maxChannelDelta, r.meanDelta))
        }

        metricsFile.parentFile.mkdirs()
        metricsFile.writeText(Json { prettyPrint = true }.encodeToString(results))
        println("wrote ${metricsFile.path}")

        // Non-failing on drift by design (see this file's header) -- only proves the harness
        // ran end to end and produced a report a human can open.
        assertTrue(results.isNotEmpty(), "expected at least one awake/reference pair to compare -- got 0, check manifest/paths above")
    }
}
