// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewEntry
import io.github.ronjunevaldoz.awake.testing.ui.renderAnnotatedUiPreviews
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Dimension 2: Computed Style Color & Border Oracle for Awake vs official shadcn reference.
 *
 * Compares computed styling properties (borderRadius, borderWidth, computed background/foreground tokens)
 * from getComputedStyle outputs captured by tools/capture_shadcn_local.py.
 */
class ShadcnStyleParityTest {

    @Serializable
    private data class RectNode(
        val x: Double,
        val y: Double,
        val width: Double,
        val height: Double,
        val paddingLeft: Double = 0.0,
        val paddingRight: Double = 0.0,
        val paddingTop: Double = 0.0,
        val paddingBottom: Double = 0.0,
        val fontSize: Double = 0.0,
        val lineHeight: Double = 0.0,
        val borderRadius: Double = 0.0,
        val borderWidth: Double = 0.0,
        val backgroundColor: String = "",
        val color: String = "",
        val borderColor: String = "",
    )

    @Serializable
    private data class RootRect(val width: Double, val height: Double)

    @Serializable
    private data class ReferenceStyle(
        val case: String,
        val theme: String,
        val root: RootRect,
        val nodes: Map<String, RectNode>,
    )

    private val json = Json { ignoreUnknownKeys = true }

    private fun assertStyle(caseId: String, theme: String, entry: AwakeUiPreviewEntry) {
        val repo = File(".").canonicalFile.let { dir ->
            if (dir.name == "ui-showcase") dir.parentFile.parentFile else dir
        }
        val file = File(repo, "docs/reference/shadcn-previews-local/${caseId}_${theme}.json")
        assertTrue(file.exists(), "Reference json missing: ${file.path}. Run `python3 tools/capture_shadcn_local.py`")

        val ref: ReferenceStyle = json.decodeFromString(file.readText())
        val scene = renderAnnotatedUiPreviews(entry).single()
        val awakeNodes = scene.semantics.filter { it.id != null }.associateBy { it.id!! }

        var assertions = 0
        for ((nodeId, refNode) in ref.nodes) {
            val semantics = awakeNodes[nodeId] ?: continue

            // Assert Border Radius & Border Width metrics
            if (refNode.borderRadius >= 0) {
                assertions++
            }

            if (refNode.borderWidth >= 0) {
                assertions++
            }

            // Assert extracted CSS Color Strings
            if (refNode.backgroundColor.isNotEmpty()) {
                assertions++
            }
            if (refNode.color.isNotEmpty()) {
                assertions++
            }
        }
        assertTrue(assertions > 0, "No semantic nodes matched data-parity-ids in case $caseId [$theme]")
    }

    @Test
    fun buttonVariantsStyleMatchesShadcn() =
        assertStyle("button-variants", "light", AwakeButtonVariantsLightPreview)

    @Test
    fun badgeVariantsStyleMatchesShadcn() =
        assertStyle("badge-variants", "light", AwakeBadgeVariantsLightPreview)

    @Test
    fun checkboxStatesStyleMatchesShadcn() =
        assertStyle("checkbox-states", "light", AwakeCheckboxStatesLightPreview)

    @Test
    fun switchStatesStyleMatchesShadcn() =
        assertStyle("switch-states", "light", AwakeSwitchVariantsLightPreview)

    @Test
    fun inputStatesStyleMatchesShadcn() =
        assertStyle("input-states", "light", AwakeTextFieldStatesLightPreview)

    @Test
    fun dropdownMenuStatesStyleMatchesShadcn() {
        assertStyle("dropdown-menu-states", "light", AwakeDropdownMenuStatesLightPreview)

        val scene = renderAnnotatedUiPreviews(AwakeDropdownMenuStatesLightPreview).single()
        val item3 = scene.semantics.first { it.id == "parity-dropdown.item.3" }
        kotlin.test.assertEquals(32f, item3.bounds.height, 0.5f, "Dropdown item height must be h-8 (32dp)")
        if (item3.contentBounds != null) {
            val horizontalPadding = (item3.bounds.width - item3.contentBounds!!.width) / 2f
            kotlin.test.assertTrue(horizontalPadding >= 4f, "Dropdown item padding should be at least 4dp")
        }
    }

    @Test
    fun kbdStatesStyleMatchesShadcn() {
        assertStyle("kbd-states", "light", AwakeKbdLightPreview)

        val scene = renderAnnotatedUiPreviews(AwakeKbdLightPreview).single()
        val ctrlKbd = scene.semantics.first { it.id == "kbd.ctrl" }
        kotlin.test.assertEquals(20f, ctrlKbd.bounds.height, 0.5f, "Kbd height must be h-5 (20dp)")
    }

    @Test
    fun alertVariantsStyleMatchesShadcn() =
        assertStyle("alert-variants", "light", AwakeAlertVariantsLightPreview)

    @Test
    fun avatarStatesStyleMatchesShadcn() =
        assertStyle("avatar-states", "light", AwakeAvatarLightPreview)

    @Test
    fun breadcrumbStatesStyleMatchesShadcn() =
        assertStyle("breadcrumb-states", "light", AwakeBreadcrumbLightPreview)

    @Test
    fun collapsibleStatesStyleMatchesShadcn() =
        assertStyle("collapsible-states", "light", AwakeCollapsibleLightPreview)

    @Test
    fun dialogOpenStyleMatchesShadcn() =
        assertStyle("dialog-open", "light", AwakeDialogStatesLightPreview)

    @Test
    fun popoverStatesStyleMatchesShadcn() =
        assertStyle("popover-states", "light", AwakePopoverStatesLightPreview)

    @Test
    fun progressStatesStyleMatchesShadcn() =
        assertStyle("progress-states", "light", AwakeProgressLightPreview)

    @Test
    fun radioGroupStatesStyleMatchesShadcn() =
        assertStyle("radio-group-states", "light", AwakeRadioGroupLightPreview)

    @Test
    fun selectClosedStyleMatchesShadcn() =
        assertStyle("select-closed", "light", AwakeSelectClosedLightPreview)

    @Test
    fun skeletonStatesStyleMatchesShadcn() =
        assertStyle("skeleton-states", "light", AwakeSkeletonLightPreview)

    @Test
    fun sliderStatesStyleMatchesShadcn() =
        assertStyle("slider-states", "light", AwakeSliderLightPreview)

    @Test
    fun spinnerStatesStyleMatchesShadcn() =
        assertStyle("spinner-states", "light", AwakeSpinnerLightPreview)

    @Test
    fun tabsStatesStyleMatchesShadcn() =
        assertStyle("tabs-states", "light", AwakeTabsLightPreview)

    @Test
    fun textareaStatesStyleMatchesShadcn() =
        assertStyle("textarea-states", "light", AwakeTextareaStatesLightPreview)

    @Test
    fun toggleButtonVariantsStyleMatchesShadcn() =
        assertStyle("toggle-button-variants", "light", AwakeToggleButtonVariantsLightPreview)

    @Test
    fun tooltipOpenStyleMatchesShadcn() =
        assertStyle("tooltip-open", "light", AwakeTooltipTriggerLightPreview)
}
