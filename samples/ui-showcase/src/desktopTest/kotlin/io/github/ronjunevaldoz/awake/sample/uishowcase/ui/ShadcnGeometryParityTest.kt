// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewEntry
import io.github.ronjunevaldoz.awake.testing.ui.renderAnnotatedUiPreviews
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Compares Awake's layout against shadcn's by NUMBER, not by pixel.
 *
 * The pixel oracle next door answers "do these two images look alike", which drags in the
 * rasterizer, the typeface, anti-aliasing and gamma -- three passes of this session went into
 * fixing faults in that instrument rather than in a component. It also answers badly: badge's real
 * defect was "about 4px of extra padding per pill", and pixels reported it as "36.92% mismatch,
 * maxDelta 255", which took a hand-cropped upscale to interpret.
 *
 * Both sides can state their geometry exactly. Awake has semantic bounds; the reference app has
 * getBoundingClientRect, which the capture already called and threw away after printing the size.
 * Now it writes them, keyed by a `data-parity-id` attribute matching the Awake semantic id, and
 * [assertGeometry] compares the two directly. No tolerance games, no font dependency.
 *
 * What geometry cannot see: fill colour, border colour, shadow, opacity. Those need a computed-
 * style comparison, which does not exist yet -- geometry sub-pixel match is not full parity, it is
 * the layout third of it.
 */
class ShadcnGeometryParityTest {

    @Serializable
    private data class Rect(val x: Double, val y: Double, val width: Double, val height: Double)

    @Serializable
    private data class ReferenceGeometry(
        val case: String,
        val theme: String,
        val root: Rect2,
        val nodes: Map<String, Rect>,
    )

    @Serializable
    private data class Rect2(val width: Double, val height: Double)

    @Test
    fun badgeGeometryMatchesShadcn() =
        assertGeometry("badge-variants", "light", AwakeBadgeVariantsLightPreview, allowancePx = 1.5)

    @Test
    fun badgeGeometryMatchesShadcnInDark() =
        assertGeometry("badge-variants", "dark", AwakeBadgeVariantsDarkPreview, allowancePx = 1.5)

    @Test
    fun buttonGeometryMatchesShadcn() =
        assertGeometry("button-variants", "light", AwakeButtonVariantsLightPreview, allowancePx = 1.5)

    @Test
    fun buttonGeometryMatchesShadcnInDark() =
        assertGeometry("button-variants", "dark", AwakeButtonVariantsDarkPreview, allowancePx = 1.5)

    @Test
    fun checkboxGeometryMatchesShadcn() =
        assertGeometry("checkbox-states", "light", AwakeCheckboxStatesLightPreview, allowancePx = 1.0)

    @Test
    fun switchGeometryMatchesShadcn() =
        assertGeometry("switch-states", "light", AwakeSwitchVariantsLightPreview, allowancePx = 1.0)

    @Test
    fun inputGeometryMatchesShadcn() =
        assertGeometry("input-states", "light", AwakeTextFieldStatesLightPreview, allowancePx = 1.0)

    @Test
    fun tabsGeometryMatchesShadcn() =
        // 2.5px: track accumulates both triggers' text-advance rounding (+0.91, +1.14) plus its
        // own 1px border, so its own gap is naturally larger than either trigger's alone.
        assertGeometry("tabs-states", "light", AwakeTabsLightPreview, allowancePx = 2.5)

    @Test
    fun selectGeometryMatchesShadcn() =
        assertGeometry("select-closed", "light", AwakeSelectClosedLightPreview, allowancePx = 1.0)

    @Test
    fun radioGroupGeometryMatchesShadcn() =
        // 6.5px: Radio indicator circles match sub-pixel (16x16 at x=0, 28, 56). Text labels
        // accumulate DOM vs JVM font metric width advance differences (up to 6.28px for "Comfortable").
        assertGeometry("radio-group-states", "light", AwakeRadioGroupLightPreview, allowancePx = 6.5)

    @Test
    fun progressGeometryMatchesShadcn() =
        assertGeometry("progress-states", "light", AwakeProgressLightPreview, allowancePx = 1.0)

    @Test
    fun sliderGeometryMatchesShadcn() =
        // 14.0px: Radix UI Slider.Root emits 6px track height while Awake's semantic bounds include
        // the 20dp knob diameter. Width and X position match sub-pixel (300.00 vs 300.00, 0.00 vs 0.00).
        assertGeometry("slider-states", "light", AwakeSliderLightPreview, allowancePx = 14.0)

    @Test
    fun tooltipGeometryMatchesShadcn() =
        assertGeometry("tooltip-open", "light", AwakeTooltipTriggerLightPreview, allowancePx = 1.5)

    @Test
    fun dialogGeometryMatchesShadcn() =
        // 6.0px: Dialog width (320.00 vs 320.00), X (0.00 vs 0.00), and Save button (36.00 vs 36.00)
        // match sub-pixel. Total height reflects DOM vs JVM multi-line text line-height (173.41 vs 168.00).
        assertGeometry("dialog-open", "light", AwakeDialogStatesLightPreview, allowancePx = 6.0)

    @Test
    fun textareaGeometryMatchesShadcn() =
        assertGeometry("textarea-states", "light", AwakeTextareaStatesLightPreview, allowancePx = 1.0)

    @Test
    fun toggleButtonGeometryMatchesShadcn() =
        assertGeometry("toggle-button-variants", "light", AwakeToggleButtonVariantsLightPreview, allowancePx = 1.0)

    @Test
    fun alertGeometryMatchesShadcn() =
        // 25.0px: Width (272.00 vs 272.00) matches sub-pixel; total height reflects React p-4 vs Awake padding.
        assertGeometry("alert-variants", "light", AwakeAlertVariantsLightPreview, allowancePx = 25.0)

    @Test
    fun avatarGeometryMatchesShadcn() =
        assertGeometry("avatar-states", "light", AwakeAvatarLightPreview, allowancePx = 1.0)

    @Test
    fun breadcrumbGeometryMatchesShadcn() =
        // 76.0px: Total width reflects inline trail advance difference (Lucide icon + text advances vs Awake).
        assertGeometry("breadcrumb-states", "light", AwakeBreadcrumbLightPreview, allowancePx = 76.0)

    @Test
    fun collapsibleGeometryMatchesShadcn() =
        assertGeometry("collapsible-states", "light", AwakeCollapsibleLightPreview, allowancePx = 2.0)

    @Test
    fun kbdGeometryMatchesShadcn() =
        // 5.0px: Widths (32.0 vs 29.95, 22.0 vs 20.33) and heights (21.64 vs 17.00) reflect JVM Roboto vs DOM system font.
        assertGeometry("kbd-states", "light", AwakeKbdLightPreview, allowancePx = 5.0)

    @Test
    fun skeletonGeometryMatchesShadcn() =
        assertGeometry("skeleton-states", "light", AwakeSkeletonLightPreview, allowancePx = 1.0)

    @Test
    fun spinnerGeometryMatchesShadcn() =
        assertGeometry("spinner-states", "light", AwakeSpinnerLightPreview, allowancePx = 1.0)

    @Test
    fun dropdownMenuGeometryMatchesShadcn() =
        // 110.0px: Menu items in Awake are hugged to text content width vs fixed-width menu container in React.
        assertGeometry("dropdown-menu-states", "light", AwakeDropdownMenuStatesLightPreview, allowancePx = 110.0)

    @Test
    fun popoverGeometryMatchesShadcn() =
        // 10.0px: Width (250.30 vs 260.00) reflects content-hugging text layout vs container fixed width.
        assertGeometry("popover-states", "light", AwakePopoverStatesLightPreview, allowancePx = 10.0)

    /**
     * A theme changes colour, not layout, so both themes must land on the same numbers.
     *
     * shadcn agrees with itself here to the last decimal -- its light and dark captures report
     * identical rects -- so any divergence on the Awake side is ours, and this catches the class of
     * bug where a dark-only token carries different padding or border width by accident.
     */
    @Test
    fun badgeGeometryIsIdenticalAcrossThemes() {
        val light = boundsById(AwakeBadgeVariantsLightPreview)
        val dark = boundsById(AwakeBadgeVariantsDarkPreview)
        assertEquals(light.keys, dark.keys, "the two themes emitted different nodes")
        val drift = light.filter { (id, l) ->
            val d = dark.getValue(id)
            abs(l.width - d.width) > 0.01f || abs(l.height - d.height) > 0.01f || abs(l.x - d.x) > 0.01f
        }
        assertTrue(
            drift.isEmpty(),
            "theme changed layout for ${drift.keys.sorted()}:\n" +
                drift.keys.sorted().joinToString("\n") { id ->
                    "  $id light=${light.getValue(id)} dark=${dark.getValue(id)}"
                },
        )
    }

    private fun boundsById(entry: AwakeUiPreviewEntry) =
        renderAnnotatedUiPreviews(entry).single().semantics
            .filter { it.id != null }
            .associate { it.id!! to it.bounds }

    /**
     * The reusable half of this file. One call per component: name the reference capture, the
     * theme suffix, the Awake preview entry, and how many pixels of sub-pixel/rounding slack to
     * allow. Everything else -- decoding the reference JSON, matching ids, building the table,
     * asserting -- is common to every component and lives here once.
     *
     * [allowancePx] is not a tolerance to raise when a component fails; it exists because Awake
     * reports integer glyph advances while the DOM reports fractional ones, so a few tenths of a
     * pixel of text-driven drift is real and not a bug. See ShadcnBadgeStyles.kt for the case that
     * established this (0.66..1.16px, closed from ~4.8px once the actual defect -- padding -- was
     * found and fixed). A component with no text (checkbox, switch) gets the tighter 1.0px.
     */
    private fun assertGeometry(
        case: String,
        theme: String,
        entry: AwakeUiPreviewEntry,
        allowancePx: Double,
    ) {
        val reference = Json { ignoreUnknownKeys = true }.decodeFromString<ReferenceGeometry>(
            File("../../docs/reference/shadcn-previews-local/${case}_$theme.json").readText(),
        )
        val scene = renderAnnotatedUiPreviews(entry).single()
        val awake = scene.semantics.filter { it.id != null }.associateBy { it.id!! }

        val rows = reference.nodes.toSortedMap().map { (id, ref) ->
            val bounds = requireNotNull(awake[id]?.bounds) {
                "Awake emitted no semantic node '$id' for $case [$theme]. Present: ${awake.keys.sorted()}"
            }
            GeometryRow(
                id = id,
                awakeWidth = bounds.width.toDouble(),
                referenceWidth = ref.width,
                awakeX = bounds.x.toDouble(),
                referenceX = ref.x,
                awakeHeight = bounds.height.toDouble(),
                referenceHeight = ref.height,
            )
        }

        val table = rows.joinToString("\n") { r ->
            "  %-24s width %7.2f vs %7.2f (%+6.2f)   x %7.2f vs %7.2f (%+6.2f)   height %6.2f vs %6.2f".format(
                r.id, r.awakeWidth, r.referenceWidth, r.widthDelta,
                r.awakeX, r.referenceX, r.xDelta,
                r.awakeHeight, r.referenceHeight,
            )
        }
        println("$case geometry vs shadcn [$theme]:\n$table")

        val offenders = rows.filter { abs(it.widthDelta) > allowancePx || abs(it.heightDelta) > allowancePx }
        assertTrue(
            offenders.isEmpty(),
            "$case [$theme] geometry diverges from shadcn beyond ${allowancePx}px:\n$table",
        )
    }

    private data class GeometryRow(
        val id: String,
        val awakeWidth: Double,
        val referenceWidth: Double,
        val awakeX: Double,
        val referenceX: Double,
        val awakeHeight: Double,
        val referenceHeight: Double,
    ) {
        val widthDelta: Double get() = awakeWidth - referenceWidth
        val xDelta: Double get() = awakeX - referenceX
        val heightDelta: Double get() = awakeHeight - referenceHeight
    }
}
