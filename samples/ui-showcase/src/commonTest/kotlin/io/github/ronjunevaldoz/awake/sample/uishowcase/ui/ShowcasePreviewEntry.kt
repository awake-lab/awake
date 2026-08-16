// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewEntry
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewFrame
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewMetadata
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSectionTitle
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSurface
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.offset
import io.github.ronjunevaldoz.awake.ui.headless.spacer
import io.github.ronjunevaldoz.awake.ui.headless.width
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.testing.ui.renderUiComponent
import io.github.ronjunevaldoz.awake.ui.designsystem.shadcnTheme
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement as HeadlessArrangement

/**
 * Preview fixtures are derived from [ShowcasePages], not maintained beside it. The previous
 * hand-written entry list drifted far enough that five of its ids no longer matched any page
 * and silently rendered the Introduction page instead.
 *
 * Metadata comes from the page's own data rather than a JVM annotation, so these fixtures run
 * on every target instead of no-opping wherever reflection is unavailable.
 */
internal class ShowcasePreviewEntry(
    val page: ShowcasePage,
    private val reportScale: Int = 2,
) : AwakeUiPreviewEntry {

    val metadata: AwakeUiPreviewMetadata = AwakeUiPreviewMetadata(
        id = "ui-showcase-${page.id}",
        title = page.title,
        group = page.category.title,
        summary = page.description,
        width = page.previewWidth,
        height = page.previewHeight,
        reportScale = reportScale,
    )

    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderShowcasePageFrame(metadata, page)
}

internal val UiShowcasePreviewEntries: List<ShowcasePreviewEntry> =
    ShowcasePages.map { ShowcasePreviewEntry(it) }

internal fun showcasePreviewEntry(pageId: String): ShowcasePreviewEntry =
    requireNotNull(UiShowcasePreviewEntries.firstOrNull { it.page.id == pageId }) {
        "No showcase page with id '$pageId'"
    }

private fun renderShowcasePageFrame(
    metadata: AwakeUiPreviewMetadata,
    page: ShowcasePage,
): AwakeUiPreviewFrame {
    val previewScale = metadata.reportScale.coerceAtLeast(1)
    val state = UiShowcaseRuntimeState()
    val theme = state.showcaseTheme()
    val font = UiFonts.default(cellSize = 12 * previewScale)
    val frame = run {
        val insetPx = 24f * previewScale
        val contentGapPx = 10f * previewScale
        renderUiComponent(
            width = metadata.rasterWidth.toFloat(),
            height = metadata.rasterHeight.toFloat(),
            font = font,
            density = previewScale.toFloat(),
            fontScale = 1f,
            rootProvider = { content -> shadcnTheme(theme = theme, content = content) },
        ) {
            column(
            modifier = Modifier
                .offset(insetPx.px, insetPx.px)
                .width((metadata.rasterWidth.toFloat() - insetPx * 2f).dp)
                .height((metadata.rasterHeight.toFloat() - insetPx * 2f).dp),
            verticalArrangement = HeadlessArrangement.spacedBy((contentGapPx / previewScale).dp),
            ) {
            shadcnSurface(
                id = "ui-showcase-preview-${page.id}",
                modifier = Modifier.fillMaxWidth(),
            ) {
                shadcnBadge(
                    id = "${page.id}.badge",
                    label = page.category.title.uppercase(),
                    variant = ShadcnBadgeVariant.Outline,
                )
                shadcnSectionTitle(title = page.title, description = page.description)
                spacer(Modifier.height(10f.dp))
                renderUiShowcasePagePreview(page, state)
            }
            }
        }

    }
    return AwakeUiPreviewFrame(
        primitives = frame.primitives,
        background = theme.colors.background,
        font = font,
        semantics = frame.semantics,
    )
}
