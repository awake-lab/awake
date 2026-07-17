// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

enum class UiWidthSizeClass {
    Compact,
    Medium,
    Expanded
}

data class UiBoxConstraints(
    val maxWidthPx: Float,
    val maxHeightPx: Float,
    val densityScale: Float = UiDensity.scale
) {
    val maxWidth: Float = maxWidthPx / densityScale
    val maxHeight: Float = maxHeightPx / densityScale
    val maxWidthDp: Float get() = maxWidth
    val maxHeightDp: Float get() = maxHeight

    val widthSizeClass: UiWidthSizeClass = when {
        maxWidth < 600f -> UiWidthSizeClass.Compact
        maxWidth < 840f -> UiWidthSizeClass.Medium
        else -> UiWidthSizeClass.Expanded
    }

    val isCompact: Boolean get() = widthSizeClass == UiWidthSizeClass.Compact
    val isMedium: Boolean get() = widthSizeClass == UiWidthSizeClass.Medium
    val isExpanded: Boolean get() = widthSizeClass == UiWidthSizeClass.Expanded
}

/** Convenience sugar for simple corner-anchored HUDs -- [overlayBox] is the preferred,
 * responsive overlay root for authored layouts; reach for this only when the layout is
 * genuinely just "stick a pane in a corner." */
fun GameUiRuntime.overlayShell(
    viewportWidth: Float,
    viewportHeight: Float,
    block: OverlayShellScope.() -> Unit
) {
    OverlayShellScope(
        runtime = this,
        bounds = UiSlot(0f, 0f, viewportWidth, viewportHeight)
    ).block()
}

fun GameUiRuntime.overlayBox(
    viewportWidth: Float,
    viewportHeight: Float,
    theme: UiTheme = this.theme,
    textScale: Float = 1f,
    contentAlignment: UiAlignment = UiAlignment.TopStart,
    block: UiBoxDslScope.(constraints: UiBoxConstraints) -> Unit
) {
    val rootSlot = UiSlot(0f, 0f, viewportWidth, viewportHeight)
    UiBoxDslScope(
        uiContext.box(
            slot = rootSlot,
            font = font,
            theme = theme,
            textScale = textScale,
            contentAlignment = contentAlignment
        )
    ).block(
        UiBoxConstraints(
            maxWidthPx = viewportWidth,
            maxHeightPx = viewportHeight
        )
    )
}

class OverlayShellScope internal constructor(
    private val runtime: GameUiRuntime,
    private val bounds: UiSlot
) {
    private fun shellModifier(width: Dp, height: Dp): UiModifier = UiModifier().width(width).height(height)

    private fun resolveShellExtent(requested: Dimension?, available: Float, axis: String): Float = when (val dimension = requested ?: Dimension.FillMax) {
        is Dimension.Fixed -> dimension.dp.toPx()
        Dimension.FillMax -> available
        Dimension.WrapContent -> error("WrapContent $axis is not supported for overlay shell placement; resolve the size before anchoring it.")
    }.coerceAtLeast(0f)

    fun slot(
        anchor: UiAnchor,
        modifier: UiModifier = UiModifier(),
        margin: UiInsets = UiInsets.Zero
    ): UiSlot = bounds.anchored(
        anchor = anchor,
        width = resolveShellExtent(modifier.width, bounds.width - margin.horizontalPx(), "width"),
        height = resolveShellExtent(modifier.height, bounds.height - margin.verticalPx(), "height"),
        margin = margin
    )

    fun slot(
        anchor: UiAnchor,
        width: Dp,
        height: Dp,
        margin: UiInsets = UiInsets.Zero
    ): UiSlot = slot(anchor, shellModifier(width, height), margin)

    @Deprecated("Prefer Dp-based sizing or modifier-based placement in overlay shell code.")
    fun slot(
        anchor: UiAnchor,
        width: Float,
        height: Float,
        margin: UiInsets = UiInsets.Zero
    ): UiSlot = slot(anchor, width.px, height.px, margin)

    @Deprecated("Prefer slot(UiAnchor.TopLeft, modifier, margin) -- one generic entry point for all four corners.", ReplaceWith("slot(UiAnchor.TopLeft, modifier, margin)"))
    fun topLeftSlot(
        modifier: UiModifier = UiModifier(),
        margin: UiInsets = UiInsets.Zero
    ): UiSlot = slot(UiAnchor.TopLeft, modifier, margin)

    @Deprecated("Prefer slot(UiAnchor.TopRight, modifier, margin) -- one generic entry point for all four corners.", ReplaceWith("slot(UiAnchor.TopRight, modifier, margin)"))
    fun topRightSlot(
        modifier: UiModifier = UiModifier(),
        margin: UiInsets = UiInsets.Zero
    ): UiSlot = slot(UiAnchor.TopRight, modifier, margin)

    @Deprecated("Prefer slot(UiAnchor.BottomLeft, modifier, margin) -- one generic entry point for all four corners.", ReplaceWith("slot(UiAnchor.BottomLeft, modifier, margin)"))
    fun bottomLeftSlot(
        modifier: UiModifier = UiModifier(),
        margin: UiInsets = UiInsets.Zero
    ): UiSlot = slot(UiAnchor.BottomLeft, modifier, margin)

    @Deprecated("Prefer slot(UiAnchor.BottomRight, modifier, margin) -- one generic entry point for all four corners.", ReplaceWith("slot(UiAnchor.BottomRight, modifier, margin)"))
    fun bottomRightSlot(
        modifier: UiModifier = UiModifier(),
        margin: UiInsets = UiInsets.Zero
    ): UiSlot = slot(UiAnchor.BottomRight, modifier, margin)

    @Deprecated("Prefer slot(UiAnchor.TopLeft, width, height, margin).", ReplaceWith("slot(UiAnchor.TopLeft, width, height, margin)"))
    fun topLeftSlot(
        width: Dp,
        height: Dp,
        margin: UiInsets = UiInsets.Zero
    ): UiSlot = topLeftSlot(shellModifier(width, height), margin)

    @Deprecated("Prefer Dp-based sizing or modifier-based placement in overlay shell code.")
    fun topLeftSlot(
        width: Float,
        height: Float,
        margin: UiInsets = UiInsets.Zero
    ): UiSlot = topLeftSlot(width.px, height.px, margin)

    @Deprecated("Prefer slot(UiAnchor.TopRight, width, height, margin).", ReplaceWith("slot(UiAnchor.TopRight, width, height, margin)"))
    fun topRightSlot(
        width: Dp,
        height: Dp,
        margin: UiInsets = UiInsets.Zero
    ): UiSlot = topRightSlot(shellModifier(width, height), margin)

    @Deprecated("Prefer Dp-based sizing or modifier-based placement in overlay shell code.")
    fun topRightSlot(
        width: Float,
        height: Float,
        margin: UiInsets = UiInsets.Zero
    ): UiSlot = topRightSlot(width.px, height.px, margin)

    @Deprecated("Prefer slot(UiAnchor.BottomLeft, width, height, margin).", ReplaceWith("slot(UiAnchor.BottomLeft, width, height, margin)"))
    fun bottomLeftSlot(
        width: Dp,
        height: Dp,
        margin: UiInsets = UiInsets.Zero
    ): UiSlot = bottomLeftSlot(shellModifier(width, height), margin)

    @Deprecated("Prefer Dp-based sizing or modifier-based placement in overlay shell code.")
    fun bottomLeftSlot(
        width: Float,
        height: Float,
        margin: UiInsets = UiInsets.Zero
    ): UiSlot = bottomLeftSlot(width.px, height.px, margin)

    @Deprecated("Prefer slot(UiAnchor.BottomRight, width, height, margin).", ReplaceWith("slot(UiAnchor.BottomRight, width, height, margin)"))
    fun bottomRightSlot(
        width: Dp,
        height: Dp,
        margin: UiInsets = UiInsets.Zero
    ): UiSlot = bottomRightSlot(shellModifier(width, height), margin)

    @Deprecated("Prefer Dp-based sizing or modifier-based placement in overlay shell code.")
    fun bottomRightSlot(
        width: Float,
        height: Float,
        margin: UiInsets = UiInsets.Zero
    ): UiSlot = bottomRightSlot(width.px, height.px, margin)

    fun place(
        anchor: UiAnchor,
        modifier: UiModifier = UiModifier(),
        margin: UiInsets = UiInsets.Zero,
        content: GameUiRuntime.(slot: UiSlot) -> Unit
    ) {
        runtime.content(slot(anchor = anchor, modifier = modifier, margin = margin))
    }

    fun place(
        anchor: UiAnchor,
        width: Dp,
        height: Dp,
        margin: UiInsets = UiInsets.Zero,
        content: GameUiRuntime.(slot: UiSlot) -> Unit
    ) {
        place(anchor, shellModifier(width, height), margin, content)
    }

    @Deprecated("Prefer Dp-based sizing or modifier-based placement in overlay shell code.")
    fun place(
        anchor: UiAnchor,
        width: Float,
        height: Float,
        margin: UiInsets = UiInsets.Zero,
        content: GameUiRuntime.(slot: UiSlot) -> Unit
    ) {
        place(anchor, width.px, height.px, margin, content)
    }

    @Deprecated("Prefer place(UiAnchor.TopLeft, ...) -- one generic entry point for all four corners.", ReplaceWith("place(UiAnchor.TopLeft, width, height, margin, content)"))
    fun topLeft(
        modifier: UiModifier = UiModifier(),
        margin: UiInsets = UiInsets.Zero,
        content: GameUiRuntime.(slot: UiSlot) -> Unit
    ) {
        place(UiAnchor.TopLeft, modifier, margin, content)
    }

    @Deprecated("Prefer place(UiAnchor.TopLeft, ...) -- one generic entry point for all four corners.", ReplaceWith("place(UiAnchor.TopLeft, width, height, margin, content)"))
    fun topLeft(
        width: Dp,
        height: Dp,
        margin: UiInsets = UiInsets.Zero,
        content: GameUiRuntime.(slot: UiSlot) -> Unit
    ) {
        topLeft(shellModifier(width, height), margin, content)
    }

    @Deprecated("Prefer Dp-based sizing or modifier-based placement in overlay shell code.")
    fun topLeft(
        width: Float,
        height: Float,
        margin: UiInsets = UiInsets.Zero,
        content: GameUiRuntime.(slot: UiSlot) -> Unit
    ) {
        topLeft(width.px, height.px, margin, content)
    }

    @Deprecated("Prefer place(UiAnchor.TopRight, ...) -- one generic entry point for all four corners.", ReplaceWith("place(UiAnchor.TopRight, width, height, margin, content)"))
    fun topRight(
        modifier: UiModifier = UiModifier(),
        margin: UiInsets = UiInsets.Zero,
        content: GameUiRuntime.(slot: UiSlot) -> Unit
    ) {
        place(UiAnchor.TopRight, modifier, margin, content)
    }

    @Deprecated("Prefer place(UiAnchor.TopRight, ...) -- one generic entry point for all four corners.", ReplaceWith("place(UiAnchor.TopRight, width, height, margin, content)"))
    fun topRight(
        width: Dp,
        height: Dp,
        margin: UiInsets = UiInsets.Zero,
        content: GameUiRuntime.(slot: UiSlot) -> Unit
    ) {
        topRight(shellModifier(width, height), margin, content)
    }

    @Deprecated("Prefer Dp-based sizing or modifier-based placement in overlay shell code.")
    fun topRight(
        width: Float,
        height: Float,
        margin: UiInsets = UiInsets.Zero,
        content: GameUiRuntime.(slot: UiSlot) -> Unit
    ) {
        topRight(width.px, height.px, margin, content)
    }

    @Deprecated("Prefer place(UiAnchor.BottomLeft, ...) -- one generic entry point for all four corners.", ReplaceWith("place(UiAnchor.BottomLeft, width, height, margin, content)"))
    fun bottomLeft(
        modifier: UiModifier = UiModifier(),
        margin: UiInsets = UiInsets.Zero,
        content: GameUiRuntime.(slot: UiSlot) -> Unit
    ) {
        place(UiAnchor.BottomLeft, modifier, margin, content)
    }

    @Deprecated("Prefer place(UiAnchor.BottomLeft, ...) -- one generic entry point for all four corners.", ReplaceWith("place(UiAnchor.BottomLeft, width, height, margin, content)"))
    fun bottomLeft(
        width: Dp,
        height: Dp,
        margin: UiInsets = UiInsets.Zero,
        content: GameUiRuntime.(slot: UiSlot) -> Unit
    ) {
        bottomLeft(shellModifier(width, height), margin, content)
    }

    @Deprecated("Prefer Dp-based sizing or modifier-based placement in overlay shell code.")
    fun bottomLeft(
        width: Float,
        height: Float,
        margin: UiInsets = UiInsets.Zero,
        content: GameUiRuntime.(slot: UiSlot) -> Unit
    ) {
        bottomLeft(width.px, height.px, margin, content)
    }

    @Deprecated("Prefer place(UiAnchor.BottomRight, ...) -- one generic entry point for all four corners.", ReplaceWith("place(UiAnchor.BottomRight, width, height, margin, content)"))
    fun bottomRight(
        modifier: UiModifier = UiModifier(),
        margin: UiInsets = UiInsets.Zero,
        content: GameUiRuntime.(slot: UiSlot) -> Unit
    ) {
        place(UiAnchor.BottomRight, modifier, margin, content)
    }

    @Deprecated("Prefer place(UiAnchor.BottomRight, ...) -- one generic entry point for all four corners.", ReplaceWith("place(UiAnchor.BottomRight, width, height, margin, content)"))
    fun bottomRight(
        width: Dp,
        height: Dp,
        margin: UiInsets = UiInsets.Zero,
        content: GameUiRuntime.(slot: UiSlot) -> Unit
    ) {
        bottomRight(shellModifier(width, height), margin, content)
    }

    @Deprecated("Prefer Dp-based sizing or modifier-based placement in overlay shell code.")
    fun bottomRight(
        width: Float,
        height: Float,
        margin: UiInsets = UiInsets.Zero,
        content: GameUiRuntime.(slot: UiSlot) -> Unit
    ) {
        bottomRight(width.px, height.px, margin, content)
    }

    fun pane(
        anchor: UiAnchor,
        maxWidth: Dimension = Dimension.FillMax,
        margin: UiInsets = UiInsets.Zero,
        theme: UiTheme = runtime.theme,
        gap: Float = UiSpacing.sm.toPx(),
        textScale: Float = 1f,
        insets: UiInsets = UiInsets(12f.dp),
        radius: Dp = UiShape.md,
        borderWidth: Dp = 1f.dp,
        style: Style = Style.Empty,
        clipContent: Boolean = false,
        content: UiColumnDslScope.(slot: UiSlot) -> Unit
    ): UiSlot {
        val outerSlot = measuredPaneSlot(
            anchor = anchor,
            maxWidth = resolveShellExtent(maxWidth, bounds.width - margin.horizontalPx(), "maxWidth"),
            margin = margin,
            theme = theme,
            gap = gap,
            textScale = textScale,
            insets = insets,
            radius = radius,
            borderWidth = borderWidth,
            style = style,
            content = content
        )
        return runtime.shellPane(
            slot = outerSlot,
            id = "pane-${paneAnchorId(anchor)}-${outerSlot.x.toInt()}-${outerSlot.y.toInt()}",
            theme = theme,
            gap = gap,
            textScale = textScale,
            insets = insets,
            radius = radius,
            borderWidth = borderWidth,
            style = style,
            clipContent = clipContent,
            content = content
        )
    }

    fun pane(
        anchor: UiAnchor,
        maxWidth: Dp,
        margin: UiInsets = UiInsets.Zero,
        theme: UiTheme = runtime.theme,
        gap: Float = UiSpacing.sm.toPx(),
        textScale: Float = 1f,
        insets: UiInsets = UiInsets(12f.dp),
        radius: Dp = UiShape.md,
        borderWidth: Dp = 1f.dp,
        style: Style = Style.Empty,
        clipContent: Boolean = false,
        content: UiColumnDslScope.(slot: UiSlot) -> Unit
    ): UiSlot = pane(anchor, Dimension.Fixed(maxWidth), margin, theme, gap, textScale, insets, radius, borderWidth, style, clipContent, content)

    @Deprecated("Prefer Dp-based sizing or Dimension-based placement in overlay shell code.")
    fun pane(
        anchor: UiAnchor,
        maxWidth: Float,
        margin: UiInsets = UiInsets.Zero,
        theme: UiTheme = runtime.theme,
        gap: Float = UiSpacing.sm.toPx(),
        textScale: Float = 1f,
        insets: UiInsets = UiInsets(12f.dp),
        radius: Dp = UiShape.md,
        borderWidth: Dp = 1f.dp,
        style: Style = Style.Empty,
        clipContent: Boolean = false,
        content: UiColumnDslScope.(slot: UiSlot) -> Unit
    ): UiSlot = pane(anchor, maxWidth.px, margin, theme, gap, textScale, insets, radius, borderWidth, style, clipContent, content)

    @Deprecated("Prefer pane(UiAnchor.TopLeft, ...) -- one generic entry point for all four corners.", ReplaceWith("pane(UiAnchor.TopLeft, maxWidth, margin, theme, gap, textScale, insets, radius, borderWidth, style, clipContent, content)"))
    fun topLeftPane(
        maxWidth: Dimension = Dimension.FillMax,
        margin: UiInsets = UiInsets.Zero,
        theme: UiTheme = runtime.theme,
        gap: Float = UiSpacing.sm.toPx(),
        textScale: Float = 1f,
        insets: UiInsets = UiInsets(12f.dp),
        radius: Dp = UiShape.md,
        borderWidth: Dp = 1f.dp,
        style: Style = Style.Empty,
        clipContent: Boolean = false,
        content: UiColumnDslScope.(slot: UiSlot) -> Unit
    ): UiSlot = pane(UiAnchor.TopLeft, maxWidth, margin, theme, gap, textScale, insets, radius, borderWidth, style, clipContent, content)

    @Deprecated("Prefer pane(UiAnchor.TopLeft, ...) -- one generic entry point for all four corners.", ReplaceWith("pane(UiAnchor.TopLeft, maxWidth, margin, theme, gap, textScale, insets, radius, borderWidth, style, clipContent, content)"))
    fun topLeftPane(
        maxWidth: Dp,
        margin: UiInsets = UiInsets.Zero,
        theme: UiTheme = runtime.theme,
        gap: Float = UiSpacing.sm.toPx(),
        textScale: Float = 1f,
        insets: UiInsets = UiInsets(12f.dp),
        radius: Dp = UiShape.md,
        borderWidth: Dp = 1f.dp,
        style: Style = Style.Empty,
        clipContent: Boolean = false,
        content: UiColumnDslScope.(slot: UiSlot) -> Unit
    ): UiSlot = topLeftPane(Dimension.Fixed(maxWidth), margin, theme, gap, textScale, insets, radius, borderWidth, style, clipContent, content)

    @Deprecated("Prefer Dp-based sizing or Dimension-based placement in overlay shell code.")
    fun topLeftPane(
        maxWidth: Float,
        margin: UiInsets = UiInsets.Zero,
        theme: UiTheme = runtime.theme,
        gap: Float = UiSpacing.sm.toPx(),
        textScale: Float = 1f,
        insets: UiInsets = UiInsets(12f.dp),
        radius: Dp = UiShape.md,
        borderWidth: Dp = 1f.dp,
        style: Style = Style.Empty,
        clipContent: Boolean = false,
        content: UiColumnDslScope.(slot: UiSlot) -> Unit
    ): UiSlot = topLeftPane(maxWidth.px, margin, theme, gap, textScale, insets, radius, borderWidth, style, clipContent, content)

    @Deprecated("Prefer pane(UiAnchor.TopRight, ...) -- one generic entry point for all four corners.", ReplaceWith("pane(UiAnchor.TopRight, maxWidth, margin, theme, gap, textScale, insets, radius, borderWidth, style, clipContent, content)"))
    fun topRightPane(
        maxWidth: Dimension = Dimension.FillMax,
        margin: UiInsets = UiInsets.Zero,
        theme: UiTheme = runtime.theme,
        gap: Float = UiSpacing.sm.toPx(),
        textScale: Float = 1f,
        insets: UiInsets = UiInsets(12f.dp),
        radius: Dp = UiShape.md,
        borderWidth: Dp = 1f.dp,
        style: Style = Style.Empty,
        clipContent: Boolean = false,
        content: UiColumnDslScope.(slot: UiSlot) -> Unit
    ): UiSlot = pane(UiAnchor.TopRight, maxWidth, margin, theme, gap, textScale, insets, radius, borderWidth, style, clipContent, content)

    @Deprecated("Prefer pane(UiAnchor.TopRight, ...) -- one generic entry point for all four corners.", ReplaceWith("pane(UiAnchor.TopRight, maxWidth, margin, theme, gap, textScale, insets, radius, borderWidth, style, clipContent, content)"))
    fun topRightPane(
        maxWidth: Dp,
        margin: UiInsets = UiInsets.Zero,
        theme: UiTheme = runtime.theme,
        gap: Float = UiSpacing.sm.toPx(),
        textScale: Float = 1f,
        insets: UiInsets = UiInsets(12f.dp),
        radius: Dp = UiShape.md,
        borderWidth: Dp = 1f.dp,
        style: Style = Style.Empty,
        clipContent: Boolean = false,
        content: UiColumnDslScope.(slot: UiSlot) -> Unit
    ): UiSlot = topRightPane(Dimension.Fixed(maxWidth), margin, theme, gap, textScale, insets, radius, borderWidth, style, clipContent, content)

    @Deprecated("Prefer Dp-based sizing or Dimension-based placement in overlay shell code.")
    fun topRightPane(
        maxWidth: Float,
        margin: UiInsets = UiInsets.Zero,
        theme: UiTheme = runtime.theme,
        gap: Float = UiSpacing.sm.toPx(),
        textScale: Float = 1f,
        insets: UiInsets = UiInsets(12f.dp),
        radius: Dp = UiShape.md,
        borderWidth: Dp = 1f.dp,
        style: Style = Style.Empty,
        clipContent: Boolean = false,
        content: UiColumnDslScope.(slot: UiSlot) -> Unit
    ): UiSlot = topRightPane(maxWidth.px, margin, theme, gap, textScale, insets, radius, borderWidth, style, clipContent, content)

    @Deprecated("Prefer pane(UiAnchor.BottomLeft, ...) -- one generic entry point for all four corners.", ReplaceWith("pane(UiAnchor.BottomLeft, maxWidth, margin, theme, gap, textScale, insets, radius, borderWidth, style, clipContent, content)"))
    fun bottomLeftPane(
        maxWidth: Dimension = Dimension.FillMax,
        margin: UiInsets = UiInsets.Zero,
        theme: UiTheme = runtime.theme,
        gap: Float = UiSpacing.sm.toPx(),
        textScale: Float = 1f,
        insets: UiInsets = UiInsets(12f.dp),
        radius: Dp = UiShape.md,
        borderWidth: Dp = 1f.dp,
        style: Style = Style.Empty,
        clipContent: Boolean = false,
        content: UiColumnDslScope.(slot: UiSlot) -> Unit
    ): UiSlot = pane(UiAnchor.BottomLeft, maxWidth, margin, theme, gap, textScale, insets, radius, borderWidth, style, clipContent, content)

    @Deprecated("Prefer pane(UiAnchor.BottomLeft, ...) -- one generic entry point for all four corners.", ReplaceWith("pane(UiAnchor.BottomLeft, maxWidth, margin, theme, gap, textScale, insets, radius, borderWidth, style, clipContent, content)"))
    fun bottomLeftPane(
        maxWidth: Dp,
        margin: UiInsets = UiInsets.Zero,
        theme: UiTheme = runtime.theme,
        gap: Float = UiSpacing.sm.toPx(),
        textScale: Float = 1f,
        insets: UiInsets = UiInsets(12f.dp),
        radius: Dp = UiShape.md,
        borderWidth: Dp = 1f.dp,
        style: Style = Style.Empty,
        clipContent: Boolean = false,
        content: UiColumnDslScope.(slot: UiSlot) -> Unit
    ): UiSlot = bottomLeftPane(Dimension.Fixed(maxWidth), margin, theme, gap, textScale, insets, radius, borderWidth, style, clipContent, content)

    @Deprecated("Prefer Dp-based sizing or Dimension-based placement in overlay shell code.")
    fun bottomLeftPane(
        maxWidth: Float,
        margin: UiInsets = UiInsets.Zero,
        theme: UiTheme = runtime.theme,
        gap: Float = UiSpacing.sm.toPx(),
        textScale: Float = 1f,
        insets: UiInsets = UiInsets(12f.dp),
        radius: Dp = UiShape.md,
        borderWidth: Dp = 1f.dp,
        style: Style = Style.Empty,
        clipContent: Boolean = false,
        content: UiColumnDslScope.(slot: UiSlot) -> Unit
    ): UiSlot = bottomLeftPane(maxWidth.px, margin, theme, gap, textScale, insets, radius, borderWidth, style, clipContent, content)

    @Deprecated("Prefer pane(UiAnchor.BottomRight, ...) -- one generic entry point for all four corners.", ReplaceWith("pane(UiAnchor.BottomRight, maxWidth, margin, theme, gap, textScale, insets, radius, borderWidth, style, clipContent, content)"))
    fun bottomRightPane(
        maxWidth: Dimension = Dimension.FillMax,
        margin: UiInsets = UiInsets.Zero,
        theme: UiTheme = runtime.theme,
        gap: Float = UiSpacing.sm.toPx(),
        textScale: Float = 1f,
        insets: UiInsets = UiInsets(12f.dp),
        radius: Dp = UiShape.md,
        borderWidth: Dp = 1f.dp,
        style: Style = Style.Empty,
        clipContent: Boolean = false,
        content: UiColumnDslScope.(slot: UiSlot) -> Unit
    ): UiSlot = pane(UiAnchor.BottomRight, maxWidth, margin, theme, gap, textScale, insets, radius, borderWidth, style, clipContent, content)

    @Deprecated("Prefer pane(UiAnchor.BottomRight, ...) -- one generic entry point for all four corners.", ReplaceWith("pane(UiAnchor.BottomRight, maxWidth, margin, theme, gap, textScale, insets, radius, borderWidth, style, clipContent, content)"))
    fun bottomRightPane(
        maxWidth: Dp,
        margin: UiInsets = UiInsets.Zero,
        theme: UiTheme = runtime.theme,
        gap: Float = UiSpacing.sm.toPx(),
        textScale: Float = 1f,
        insets: UiInsets = UiInsets(12f.dp),
        radius: Dp = UiShape.md,
        borderWidth: Dp = 1f.dp,
        style: Style = Style.Empty,
        clipContent: Boolean = false,
        content: UiColumnDslScope.(slot: UiSlot) -> Unit
    ): UiSlot = bottomRightPane(Dimension.Fixed(maxWidth), margin, theme, gap, textScale, insets, radius, borderWidth, style, clipContent, content)

    @Deprecated("Prefer Dp-based sizing or Dimension-based placement in overlay shell code.")
    fun bottomRightPane(
        maxWidth: Float,
        margin: UiInsets = UiInsets.Zero,
        theme: UiTheme = runtime.theme,
        gap: Float = UiSpacing.sm.toPx(),
        textScale: Float = 1f,
        insets: UiInsets = UiInsets(12f.dp),
        radius: Dp = UiShape.md,
        borderWidth: Dp = 1f.dp,
        style: Style = Style.Empty,
        clipContent: Boolean = false,
        content: UiColumnDslScope.(slot: UiSlot) -> Unit
    ): UiSlot = bottomRightPane(maxWidth.px, margin, theme, gap, textScale, insets, radius, borderWidth, style, clipContent, content)

    private fun measuredPaneSlot(
        anchor: UiAnchor,
        maxWidth: Float,
        margin: UiInsets,
        theme: UiTheme,
        gap: Float,
        textScale: Float,
        insets: UiInsets,
        radius: Dp,
        borderWidth: Dp,
        style: Style,
        content: UiColumnDslScope.(slot: UiSlot) -> Unit
    ): UiSlot {
        val resolved = runtime.uiContext.absolute(0f, 0f, font = runtime.font, theme = theme, textScale = textScale)
            .resolveStyle(
                style = style,
                defaults = theme.components.panel then Style {
                    shape(radius)
                    borderWidth(borderWidth)
                }
            )
        val outerInsetsWidth = insets.horizontalPx()
        val outerInsetsHeight = insets.verticalPx()
        val panelPaddingWidth = resolved.contentPadding.horizontalPx()
        val panelPaddingHeight = resolved.contentPadding.verticalPx()
        val panelMaxWidth = (maxWidth - outerInsetsWidth).coerceAtLeast(0f)
        val contentMaxWidth = (panelMaxWidth - panelPaddingWidth).coerceAtLeast(0f)
        val measured = runtime.uiContext.measureColumnContent(
            width = contentMaxWidth,
            font = runtime.font,
            theme = theme,
            gap = gap,
            textScale = resolved.textScale
        ) { measureSlot ->
            UiColumnDslScope(this).content(measureSlot)
        }
        val panelWidth = (measured.width + panelPaddingWidth).coerceAtMost(panelMaxWidth).coerceAtLeast(0f)
        val panelHeight = (measured.height + panelPaddingHeight).coerceAtLeast(0f)
        val panelSlot = slot(
            anchor = anchor,
            width = panelWidth.px,
            height = panelHeight.px,
            margin = margin
        )
        return UiSlot(
            x = (panelSlot.x - insets.start.toPx()).coerceAtLeast(bounds.x),
            y = (panelSlot.y - insets.top.toPx()).coerceAtLeast(bounds.y),
            width = (panelWidth + outerInsetsWidth).coerceAtMost(bounds.width),
            height = (panelHeight + outerInsetsHeight).coerceAtMost(bounds.height)
        )
    }
}

private fun paneAnchorId(anchor: UiAnchor): String = when (anchor) {
    UiAnchor.TopLeft -> "top-left"
    UiAnchor.TopRight -> "top-right"
    UiAnchor.BottomLeft -> "bottom-left"
    UiAnchor.BottomRight -> "bottom-right"
}

fun GameUiRuntime.shellPane(
    slot: UiSlot,
    id: String,
    theme: UiTheme = this.theme,
    gap: Float = UiSpacing.sm.toPx(),
    textScale: Float = 1f,
    insets: UiInsets = UiInsets(12f.dp),
    radius: Dp = UiShape.md,
    borderWidth: Dp = 1f.dp,
    style: Style = Style.Empty,
    clipContent: Boolean = false,
    content: UiColumnDslScope.(slot: UiSlot) -> Unit
): UiSlot {
    lateinit var panelSlot: UiSlot
    val panelHeight = (slot.height - insets.top.toPx() - insets.bottom.toPx()).coerceAtLeast(0f).toDimension()
    column(
        slot = slot,
        theme = theme,
        gap = gap,
        textScale = textScale,
        insets = insets
    ) {
        panelSlot = panel(
            id = id,
            height = panelHeight,
            radius = radius,
            borderWidth = borderWidth,
            style = style,
            clipContent = clipContent,
            content = content
        )
    }
    return panelSlot
}

fun UiColumnDslScope.sectionTitle(
    title: String,
    style: Style = Style {
        foreground(theme.tokens.mutedForeground)
    }
): UiSlot = text(title, style = style)

fun UiColumnDslScope.metaText(
    label: String,
    modifier: UiModifier = UiModifier(),
    style: Style = Style {
        foreground(theme.tokens.mutedForeground)
    },
    maxLines: Int = 1
): UiSlot = text(
    label = label,
    modifier = modifier,
    style = style,
    overflow = UiTextOverflow.Ellipsis,
    maxLines = maxLines
)

fun UiColumnDslScope.supportingText(
    label: String,
    modifier: UiModifier = UiModifier(),
    style: Style = Style {
        foreground(theme.tokens.mutedForeground)
    },
    maxLines: Int = Int.MAX_VALUE
): UiSlot = text(
    label = label,
    modifier = modifier,
    style = style,
    wrap = UiTextWrap.Word,
    overflow = UiTextOverflow.Ellipsis,
    maxLines = maxLines
)

fun UiColumnDslScope.textLines(
    lines: Iterable<String>,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty,
    wrap: UiTextWrap = UiTextWrap.None,
    overflow: UiTextOverflow = if (wrap == UiTextWrap.None) UiTextOverflow.Ellipsis else UiTextOverflow.Clip,
    maxLines: Int = if (wrap == UiTextWrap.None) 1 else Int.MAX_VALUE
) {
    lines.forEach { line ->
        text(
            label = line,
            modifier = modifier,
            style = style,
            wrap = wrap,
            overflow = overflow,
            maxLines = maxLines
        )
    }
}

fun UiColumnDslScope.supportingLines(
    lines: Iterable<String>,
    modifier: UiModifier = UiModifier(),
    style: Style = Style {
        foreground(theme.tokens.mutedForeground)
    },
    maxLines: Int = Int.MAX_VALUE
) {
    textLines(
        lines = lines,
        modifier = modifier,
        style = style,
        wrap = UiTextWrap.Word,
        overflow = UiTextOverflow.Ellipsis,
        maxLines = maxLines
    )
}

fun UiColumnDslScope.propertyToggle(
    id: String,
    checked: Boolean,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty,
    labelContent: UiAbsoluteDslScope.(slot: UiSlot) -> Unit
): Boolean {
    var resolved = checked
    propertyRow(
        modifier = modifier.height(28f.px),
        labelContent = labelContent
    ) { slot ->
        resolved = toggle(
            id = id,
            checked = checked,
            modifier = UiModifier().width(slot.width.px).height(slot.height.px),
            style = style
        )
    }
    return resolved
}

fun UiColumnDslScope.propertyToggle(
    id: String,
    checked: Boolean,
    height: Float,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty,
    labelContent: UiAbsoluteDslScope.(slot: UiSlot) -> Unit
): Boolean {
    var resolved = checked
    propertyRow(
        height = height.px,
        labelContent = labelContent
    ) { slot ->
        resolved = toggle(
            id = id,
            checked = checked,
            width = slot.width,
            height = slot.height,
            modifier = modifier,
            style = style
        )
    }
    return resolved
}

fun UiColumnDslScope.propertyToggle(
    id: String,
    label: String,
    checked: Boolean,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): Boolean = propertyToggle(
    id = id,
    checked = checked,
    modifier = modifier,
    style = style
) {
    text(label)
}

fun UiColumnDslScope.propertyToggle(
    id: String,
    label: String,
    checked: Boolean,
    height: Float,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): Boolean = propertyToggle(
    id = id,
    checked = checked,
    height = height,
    modifier = modifier,
    style = style
) {
    text(label)
}

fun UiColumnDslScope.propertyDropdown(
    id: String,
    options: List<String>,
    selectedIndex: Int,
    modifier: UiModifier = UiModifier(),
    labelWidth: Dp = 64f.dp,
    style: Style = Style.Empty,
    labelContent: UiAbsoluteDslScope.(slot: UiSlot) -> Unit
): Int? {
    var resolved: Int? = null
    propertyRow(
        modifier = modifier.height(28f.px),
        labelWidth = labelWidth,
        labelContent = labelContent
    ) { slot ->
        resolved = dropdown(
            id = id,
            options = options,
            selectedIndex = selectedIndex,
            modifier = UiModifier().width(slot.width.px).height(slot.height.px),
            style = style
        )
    }
    return resolved
}

fun UiColumnDslScope.propertyDropdown(
    id: String,
    options: List<String>,
    selectedIndex: Int,
    height: Float,
    labelWidth: Dp = 64f.dp,
    style: Style = Style.Empty,
    labelContent: UiAbsoluteDslScope.(slot: UiSlot) -> Unit
): Int? {
    var resolved: Int? = null
    propertyRow(
        height = height.px,
        labelWidth = labelWidth,
        labelContent = labelContent
    ) { slot ->
        resolved = dropdown(
            id = id,
            options = options,
            selectedIndex = selectedIndex,
            width = slot.width,
            height = slot.height,
            style = style
        )
    }
    return resolved
}

fun UiColumnDslScope.propertyDropdown(
    id: String,
    label: String,
    options: List<String>,
    selectedIndex: Int,
    modifier: UiModifier = UiModifier(),
    labelWidth: Dp = 64f.dp,
    style: Style = Style.Empty
): Int? = propertyDropdown(
    id = id,
    options = options,
    selectedIndex = selectedIndex,
    modifier = modifier,
    labelWidth = labelWidth,
    style = style
) {
    text(label)
}

fun UiColumnDslScope.propertyDropdown(
    id: String,
    label: String,
    options: List<String>,
    selectedIndex: Int,
    height: Float,
    labelWidth: Dp = 64f.dp,
    style: Style = Style.Empty
): Int? = propertyDropdown(
    id = id,
    options = options,
    selectedIndex = selectedIndex,
    height = height,
    labelWidth = labelWidth,
    style = style
) {
    text(label)
}

fun UiColumnDslScope.propertySlider(
    id: String,
    min: Float,
    max: Float,
    value: Float,
    modifier: UiModifier = UiModifier(),
    labelWidth: Dp = 64f.dp,
    style: Style = Style.Empty,
    labelContent: UiAbsoluteDslScope.(slot: UiSlot) -> Unit
): Float {
    var resolved = value
    propertyRow(
        modifier = modifier.height(28f.px),
        labelWidth = labelWidth,
        labelContent = labelContent
    ) { slot ->
        resolved = slider(
            id = id,
            min = min,
            max = max,
            value = value,
            modifier = UiModifier().width(slot.width.px).height(slot.height.px),
            style = style
        )
    }
    return resolved
}

fun UiColumnDslScope.propertySlider(
    id: String,
    min: Float,
    max: Float,
    value: Float,
    height: Float,
    labelWidth: Dp = 64f.dp,
    style: Style = Style.Empty,
    labelContent: UiAbsoluteDslScope.(slot: UiSlot) -> Unit
): Float {
    var resolved = value
    propertyRow(
        height = height.px,
        labelWidth = labelWidth,
        labelContent = labelContent
    ) { slot ->
        resolved = slider(
            id = id,
            min = min,
            max = max,
            value = value,
            width = slot.width,
            height = slot.height,
            style = style
        )
    }
    return resolved
}

fun UiColumnDslScope.propertySlider(
    id: String,
    label: String,
    min: Float,
    max: Float,
    value: Float,
    modifier: UiModifier = UiModifier(),
    labelWidth: Dp = 64f.dp,
    style: Style = Style.Empty
): Float = propertySlider(
    id = id,
    min = min,
    max = max,
    value = value,
    modifier = modifier,
    labelWidth = labelWidth,
    style = style
) {
    text(label)
}

fun UiColumnDslScope.propertySlider(
    id: String,
    label: String,
    min: Float,
    max: Float,
    value: Float,
    height: Float,
    labelWidth: Dp = 64f.dp,
    style: Style = Style.Empty
): Float = propertySlider(
    id = id,
    min = min,
    max = max,
    value = value,
    height = height,
    labelWidth = labelWidth,
    style = style
) {
    text(label)
}
