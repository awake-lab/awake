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
    fun slot(
        anchor: UiAnchor,
        width: Float,
        height: Float,
        margin: UiInsets = UiInsets.Zero
    ): UiSlot = bounds.anchored(anchor = anchor, width = width, height = height, margin = margin)

    fun topLeftSlot(
        width: Float,
        height: Float,
        margin: UiInsets = UiInsets.Zero
    ): UiSlot = slot(UiAnchor.TopLeft, width, height, margin)

    fun topRightSlot(
        width: Float,
        height: Float,
        margin: UiInsets = UiInsets.Zero
    ): UiSlot = slot(UiAnchor.TopRight, width, height, margin)

    fun bottomLeftSlot(
        width: Float,
        height: Float,
        margin: UiInsets = UiInsets.Zero
    ): UiSlot = slot(UiAnchor.BottomLeft, width, height, margin)

    fun bottomRightSlot(
        width: Float,
        height: Float,
        margin: UiInsets = UiInsets.Zero
    ): UiSlot = slot(UiAnchor.BottomRight, width, height, margin)

    fun place(
        anchor: UiAnchor,
        width: Float,
        height: Float,
        margin: UiInsets = UiInsets.Zero,
        content: GameUiRuntime.(slot: UiSlot) -> Unit
    ) {
        runtime.content(slot(anchor = anchor, width = width, height = height, margin = margin))
    }

    fun topLeft(
        width: Float,
        height: Float,
        margin: UiInsets = UiInsets.Zero,
        content: GameUiRuntime.(slot: UiSlot) -> Unit
    ) {
        place(UiAnchor.TopLeft, width, height, margin, content)
    }

    fun topRight(
        width: Float,
        height: Float,
        margin: UiInsets = UiInsets.Zero,
        content: GameUiRuntime.(slot: UiSlot) -> Unit
    ) {
        place(UiAnchor.TopRight, width, height, margin, content)
    }

    fun bottomLeft(
        width: Float,
        height: Float,
        margin: UiInsets = UiInsets.Zero,
        content: GameUiRuntime.(slot: UiSlot) -> Unit
    ) {
        place(UiAnchor.BottomLeft, width, height, margin, content)
    }

    fun bottomRight(
        width: Float,
        height: Float,
        margin: UiInsets = UiInsets.Zero,
        content: GameUiRuntime.(slot: UiSlot) -> Unit
    ) {
        place(UiAnchor.BottomRight, width, height, margin, content)
    }

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
    ): UiSlot {
        val outerSlot = measuredPaneSlot(
            anchor = anchor,
            maxWidth = maxWidth,
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
    ): UiSlot = pane(
        anchor = UiAnchor.TopLeft,
        maxWidth = maxWidth,
        margin = margin,
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
    ): UiSlot = pane(
        anchor = UiAnchor.TopRight,
        maxWidth = maxWidth,
        margin = margin,
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
    ): UiSlot = pane(
        anchor = UiAnchor.BottomLeft,
        maxWidth = maxWidth,
        margin = margin,
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
    ): UiSlot = pane(
        anchor = UiAnchor.BottomRight,
        maxWidth = maxWidth,
        margin = margin,
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
            width = panelWidth,
            height = panelHeight,
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
    label: String,
    checked: Boolean,
    height: Float = 28f,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): Boolean = propertyCheckbox(
    id = id,
    checked = checked,
    label = label,
    height = height,
    modifier = modifier,
    style = style
)

fun UiColumnDslScope.propertyDropdown(
    id: String,
    label: String,
    options: List<String>,
    selectedIndex: Int,
    height: Float = 28f,
    labelWidth: Dp = 64f.dp,
    style: Style = Style.Empty
): Int? {
    var resolved: Int? = null
    propertyRow(label, height = height, labelWidth = labelWidth) { slot ->
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

fun UiColumnDslScope.propertySlider(
    id: String,
    label: String,
    min: Float,
    max: Float,
    value: Float,
    height: Float = 28f,
    labelWidth: Dp = 64f.dp,
    style: Style = Style.Empty
): Float {
    var resolved = value
    propertyRow(label, height = height, labelWidth = labelWidth) { slot ->
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
