// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

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

fun UiColumnDslScope.textLines(
    lines: Iterable<String>,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
) {
    lines.forEach { line ->
        text(line, modifier = modifier, style = style)
    }
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
