// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

/**
 * DEMOTED. [ShadcnGeometryParityTest] is the primary parity oracle now, comparing Awake's
 * semantic bounds against shadcn's own getBoundingClientRect numbers -- exact, in pixels,
 * independent of rasterizer/font/anti-aliasing. This test is what is left over: colour, corner
 * radius, border width, shadow -- the dimensions geometry cannot see.
 *
 * It was the primary oracle for one session, and every promotion of a number it produced turned
 * out to be wrong in a way geometry would not have been:
 *  - a mis-framed reference (256x6 for a 300x20 render) scored as a fidelity number until the
 *    coverage gate below started reporting framing
 *  - the reference app rendered in no particular font (a self-referential CSS variable) until
 *    that was matched, moving four numbers with zero Awake-side change
 *  - even matched, the reference initially rendered every weight as 400 (one @font-face
 *    covering "100 900"), so a component tuned against it would have been tuned against the
 *    wrong weight
 * Three real bugs in the instrument, only one real bug in a component (badge's padding, found by
 * geometry in one pass once the instrument was fixed). That ratio is why this demotes.
 *
 * mismatchPct below stays informative -- printed, tracked in the metrics JSON, ratcheted against
 * regression -- but it is no longer where a padding or advance-width question gets decided.
 * [ShadcnGeometryParityTest] decides those. This decides "does the badge still look red."
 *
 * Renders its own Awake-side previews (rather than depending on [ShadcnParityScreenshotTest]
 * having already run in the same invocation) so `--tests "*ShadcnReferenceComparisonTest*"`
 * alone is sufficient -- Gradle doesn't guarantee cross-class ordering under a test filter.
 */
import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreview
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewEntry
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewFrame
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewMetadata
import io.github.ronjunevaldoz.awake.testing.ui.renderAnnotatedUiPreviews
import io.github.ronjunevaldoz.awake.testing.ui.saveAwakeUiPreview
import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCard
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnInput
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSmall
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.ShadcnTextStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnTooltipText
import io.github.ronjunevaldoz.awake.ui.designsystem.shadcnThemeValues
import io.github.ronjunevaldoz.awake.ui.designsystem.shadcnTheme
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.createUiScope
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.offset
import io.github.ronjunevaldoz.awake.ui.headless.spacer
import io.github.ronjunevaldoz.awake.ui.headless.uiScope
import io.github.ronjunevaldoz.awake.ui.headless.width
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.toUiInputState
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.math.round
import kotlin.test.Test
import kotlin.test.assertTrue
import io.github.ronjunevaldoz.awake.ui.context.UiFrameInput
import io.github.ronjunevaldoz.awake.ui.context.LocalFont

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
    summary = "Matches docs/reference/shadcn-previews-local/card-login_light.png's login-card arrangement for a direct " +
        "side-by-side. Composition mirrors tools/shadcn-reference-app/src/cases.tsx's card-login case exactly now: " +
        "one Label+Input(email)+full-width Login button, all in the content slot -- the previous version added a " +
        "password field the reference never shows and put Login in shadcnCard's footer slot, which the real markup " +
        "doesn't use either, so the aligned crop is comparing the same one-field card content. The compatibility " +
        "shadcnCard now follows the reference's explicit CardHeader/CardContent spacing and does not inject a " +
        "separator that the source case never requests.",
    width = 288,
    height = 234,
)
internal object AwakeCardLightPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame {
        val theme = shadcnThemeValues(dark = false)
        val font = UiFonts.default()
        val ui = UiContext()
        ui.beginFrame(UiFrameInput(viewportWidth = metadata.width.toFloat(), viewportHeight = metadata.height.toFloat(), input = comparisonTestSnapshot()))
        ui.pushLocal(LocalFont, font)
        ui.showcaseRoot(theme = theme, bounds = UiBounds(0f, 0f, metadata.width.toFloat(), metadata.height.toFloat())) {
            column(
            modifier = Modifier.offset(8f.dp, 8f.dp).width(272f.dp)
                .height((metadata.height.toFloat() - 16f).dp),
            ) {
            shadcnCard(
                id = "parity-card",
                modifier = Modifier.width(272f.px),
                header = {
                    shadcnText(
                        "Login to your account",
                        style = ShadcnTextStyle.Title,
                    )
                },
            ) { _ ->
                uiScope().shadcnSmall("Email")
                // The reference CardContent uses `flex flex-col gap-3` (12px) between every
                // child: Label -> Input -> Button. Keep the fixture's composition aligned with
                // the pinned shadcn case instead of compensating for the old core default gap.
                spacer(Modifier.height(12f.dp))
                shadcnInput(
                    "parity-card-email",
                    value = "",
                    placeholder = "Email",
                    modifier = Modifier.width(240f.px).height(36f.px),
                )
                spacer(Modifier.height(12f.dp))
                shadcnButton(
                    "parity-card-login",
                    "Login",
                    modifier = Modifier.width(240f.px).height(36f.px),
                )
            }
            }
        }
        val output = ui.finishFrame()
        return AwakeUiPreviewFrame(
            primitives = output.primitives,
            background = theme.colors.background,
            font = font,
            semantics = output.semantics,
        )
    }
}

@AwakeUiPreview(
    id = "awake-tooltip-content-light",
    title = "Awake Tooltip Content (light)",
    group = "Shadcn Parity",
    summary = "Matches docs/reference/shadcn-previews-local/tooltip-open_light.png -- that capture's selector is " +
        "`[data-slot=\"tooltip-content\"]` (tools/shadcn_reference_cases.json), i.e. only the open bubble itself, " +
        "not the trigger. The pair used to compare against awake-tooltip-trigger-light (a 'Hover me' button), a " +
        "genuine wrong-content pairing bug, not a crop/alignment issue -- no amount of aligned-crop tuning makes a " +
        "button resemble a tooltip bubble. This renders just the bubble via the real shadcnTooltipText, no visible " +
        "trigger widget, matching the reference's own framing and text ('Add to library', tools/shadcn-reference-app" +
        "/src/cases.tsx's tooltip-open case). Canvas width is pinned close to the expected bubble width rather than " +
        "generously oversized: a WrapContent popup's word-wrapped text measures its width from the *available* " +
        "space (windowBounds.width, io.github.ronjunevaldoz.awake.ui.UiPopup.kt's `popup()`), not its intrinsic " +
        "text width, so a wide canvas would report a wide bubble no matter how short the text is -- worth flagging " +
        "as a real WrapContent-sizing gap, not something this preview can paper over except by keeping the canvas " +
        "itself close to the intended size.",
    width = 112,
    height = 32,
)
internal object AwakeTooltipContentLightPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame {
        val theme = shadcnThemeValues(dark = false)
        val font = UiFonts.default()
        val ui = UiContext()
        ui.beginFrame(UiFrameInput(viewportWidth = metadata.width.toFloat(), viewportHeight = metadata.height.toFloat(), input = comparisonTestSnapshot()))
        ui.pushLocal(LocalFont, font)
        // Anchor is a 1px sliver, not drawn -- just enough for BottomCenter/TopCenter +
        // spacing.xs to place the bubble, so the canvas doesn't waste rows on a full-size
        // trigger the reference (bubble-only capture) never shows either.
        val anchor = UiBounds(x = 0f, y = 0f, width = metadata.width.toFloat(), height = 1f)
        ui.showcaseRoot(
            theme = theme,
            bounds = UiBounds(x = 0f, y = 0f, width = metadata.width.toFloat(), height = metadata.height.toFloat()),
        ) {
            shadcnTooltipText(
                anchorSlot = anchor,
                visible = true,
                text = "Add to library",
                id = "parity-tooltip",
            )
        }
        val output = ui.finishFrame()
        return AwakeUiPreviewFrame(
            primitives = output.primitives,
            background = theme.colors.background,
            font = font,
            semantics = output.semantics,
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

/** Committed regression baseline -- see this file's class doc and
 * docs/reference/ui-validation.md's "Shadcn Parity Regression Gate" section. [tolerancePct] is
 * an absolute percentage-point allowance above the recorded value (not a relative multiplier).
 * [excluded] pairs still render and appear in the printed report / metrics JSON, they're just
 * not compared against a baseline -- each entry names why its crop alignment can't be trusted.
 * [comment]/[toleranceNote] round-trip through record mode (unlike a hand-added JSON comment
 * with no matching property, which `encodeToString` would silently drop the next time someone
 * records) -- keep both current if the reasoning behind the gate changes. */
@Serializable
private data class ShadcnParityBaseline(
    @SerialName("_comment") val comment: String = "",
    @SerialName("_toleranceNote") val toleranceNote: String = "",
    val tolerancePct: Double = 1.0,
    val excluded: Map<String, String> = emptyMap(),
    val baseline: Map<String, Double> = emptyMap(),
)

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
) {
    /** Compared area as a share of the larger image -- 100% when both sides framed the same box. */
    fun coveragePct(): Double {
        val compared = (comparedSize[0].toDouble() * comparedSize[1]).coerceAtLeast(0.0)
        val largest = maxOf(
            awakeSize[0].toDouble() * awakeSize[1],
            referenceSize[0].toDouble() * referenceSize[1],
        ).coerceAtLeast(1.0)
        return compared / largest * 100.0
    }
}

/** Below this, the pair is mis-framed rather than merely different, and its mismatch%% is noise. */
private const val MIN_COVERAGE_PCT = 80.0

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
    while (bottom > top &&
        (left until right).all { x ->
            close(
                image.getRGB(
                    x,
                    bottom - 1,
                ),
            )
        }
    ) {
        bottom--
    }

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
private fun compareAgainstReference(
    awakeFile: File,
    referenceFile: File,
    outDir: File,
    name: String,
): ShadcnParityMetric {
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
            AwakeButtonVariantsDarkPreview,
            AwakeBadgeVariantsLightPreview,
            AwakeCheckboxStatesLightPreview,
            AwakeSwitchVariantsLightPreview,
            AwakeTextFieldStatesLightPreview,
            AwakeSelectClosedLightPreview,
            AwakeTabsLightPreview,
            AwakeCardLightPreview,
            AwakeSliderLightPreview,
            AwakeTooltipContentLightPreview,
            AwakeDialogStatesLightPreview,
            AwakeBadgeVariantsDarkPreview,
            AwakeRadioGroupLightPreview,
            AwakeProgressLightPreview,
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

        printParityTable(results)

        metricsFile.parentFile.mkdirs()
        metricsFile.writeText(Json { prettyPrint = true }.encodeToString(results))
        println("wrote ${metricsFile.path}")

        assertTrue(
            results.isNotEmpty(),
            "expected at least one awake/reference pair to compare -- got 0, check manifest/paths above",
        )

        gateAgainstBaseline(results)
    }

    /** Same `-DAWAKE_RECORD_SNAPSHOTS=true` convention as [ShadcnParityScreenshotTest] and
     * `UiShowcasePreviewDocsTest`: without it, compares every non-excluded pair's mismatchPct
     * against `tools/shadcn_parity_baseline.json` and fails if it drifted worse by more than
     * [ShadcnParityBaseline.tolerancePct]. With it, overwrites the baseline with the current
     * numbers instead of gating -- follow the same re-record discipline as any other baseline
     * here (run without recording first, read the diff PNG, explain the drift, only then
     * record; see skills/awake-ui-verification/SKILL.md). */
    private fun gateAgainstBaseline(results: List<ShadcnParityMetric>) {
        val record = System.getProperty("AWAKE_RECORD_SNAPSHOTS")?.toBoolean() ?: false
        val baselineFile = File("../../tools/shadcn_parity_baseline.json")
        // encodeDefaults: without it, a field left at its default (tolerancePct == 1.0, or
        // either comment) is silently dropped on write instead of round-tripped.
        val baselineJson = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
        val current = baselineJson.decodeFromString<ShadcnParityBaseline>(baselineFile.readText())

        if (record) {
            val recorded = current.copy(
                baseline = results.filter { it.name !in current.excluded }
                    .associate { it.name to it.mismatchPct },
            )
            baselineFile.writeText(baselineJson.encodeToString(recorded))
            println("recorded shadcn parity baseline -> ${baselineFile.path}")
            return
        }

        requireConsistentFraming(results, current.excluded.keys)

        val missing = mutableListOf<String>()
        val regressed = mutableListOf<String>()
        results.forEach { r ->
            if (r.name in current.excluded) return@forEach
            val recordedPct = current.baseline[r.name]
            if (recordedPct == null) {
                missing += r.name
                return@forEach
            }
            val drift = r.mismatchPct - recordedPct
            if (drift > current.tolerancePct) {
                regressed += "${r.name}: baseline $recordedPct% -> now ${r.mismatchPct}% " +
                    "(+${round2(drift)}pp, tolerance ${current.tolerancePct}pp)"
            }
        }

        if (missing.isNotEmpty()) {
            throw AssertionError(
                "No recorded baseline for: ${missing.joinToString()}. New pair, or " +
                    "tools/shadcn_parity_baseline.json is out of sync with tools/shadcn_parity_pairs.json. " +
                    "After confirming build/reports/shadcn-parity/<name>_diff.png looks right, record with " +
                    "-DAWAKE_RECORD_SNAPSHOTS=true.",
            )
        }
        if (regressed.isNotEmpty()) {
            throw AssertionError(
                "Shadcn parity regressed beyond tolerance (${current.tolerancePct}pp):\n" +
                    regressed.joinToString("\n") +
                    "\nInspect build/reports/shadcn-parity/<name>_diff.png. If this is a real " +
                    "regression, fix the component. If the drift is intentional, re-record with " +
                    "-DAWAKE_RECORD_SNAPSHOTS=true after confirming the diff image looks right.",
            )
        }
    }
}

private fun printParityTable(results: List<ShadcnParityMetric>) {
    println(
        "%-24s %10s %10s %11s %9s %-11s %-11s".format(
            "name",
            "mismatch%",
            "maxDelta",
            "meanDelta",
            "covered%",
            "awake",
            "reference",
        ),
    )
    results.forEach { r ->
        println(
            "%-24s %9.2f%% %10d %11.2f %8.1f%% %-11s %-11s".format(
                r.name,
                r.mismatchPct,
                r.maxChannelDelta,
                r.meanDelta,
                r.coveragePct(),
                "${r.awakeSize[0]}x${r.awakeSize[1]}",
                "${r.referenceSize[0]}x${r.referenceSize[1]}",
            ),
        )
    }
}

private fun requireConsistentFraming(results: List<ShadcnParityMetric>, excluded: Set<String>) {
    // Framing is checked before drift, and is NOT ratcheted.
    //
    // compareAgainstReference walks the intersection of the two images -- min(width) by
    // min(height) -- so a reference captured at a different size is silently compared on its
    // top-left corner only, and the leftover area is neither compared nor reported. The
    // resulting number still reads as a fidelity score. slider-local-light sat at 50.98%
    // that way against a 256x6 reference of a 300x20 render: the capture cropped the thumb
    // out entirely, so the figure described the crop, not the slider.
    //
    // A ratchet cannot catch this -- a mis-framed pair is stable, so it passes forever at
    // whatever number the framing produces.
    val misframed = results.filter { it.name !in excluded && it.coveragePct() < MIN_COVERAGE_PCT }
    if (misframed.isNotEmpty()) {
        throw AssertionError(
            "Reference and render disagree on size, so the mismatch%% below is measured on a " +
                "crop and means nothing:\n" +
                misframed.joinToString("\n") { r ->
                    "  ${r.name}: awake ${r.awakeSize[0]}x${r.awakeSize[1]}, reference " +
                        "${r.referenceSize[0]}x${r.referenceSize[1]}, compared " +
                        "${r.comparedSize[0]}x${r.comparedSize[1]} " +
                        "(${round2(r.coveragePct())}%% of the larger image)"
                } +
                "\nRe-capture the reference with tools/capture_shadcn_reference.py so it frames " +
                "the same content, or fix the Awake preview's canvas to match it.",
        )
    }
}
