// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.layouts.AbsoluteScope
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.FillAwareScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.layouts.UiSpacing
import io.github.ronjunevaldoz.awake.ui.layouts.resolveAgainst
import kotlin.math.roundToInt

data class UiSlot(val x: Float, val y: Float, val width: Float, val height: Float)

/** Clamps this rect to the region it shares with [other] -- zero-size (not negative) if they
 * don't overlap at all. Pure function, used by [UiContext]'s clip stack to resolve nested
 * `clip { }` calls to a single already-intersected rect before it ever reaches a backend. */
fun UiSlot.intersect(other: UiSlot): UiSlot {
    val left = maxOf(x, other.x)
    val top = maxOf(y, other.y)
    val right = minOf(x + width, other.x + other.width)
    val bottom = minOf(y + height, other.y + other.height)
    return UiSlot(left, top, (right - left).coerceAtLeast(0f), (bottom - top).coerceAtLeast(0f))
}

fun pixelPerfectTextScale(requestedScale: Float, step: Float = 0.25f): Float {
    val safeStep = step.takeIf { it.isFinite() && it > 0f } ?: 0.25f
    val snapped = (requestedScale / safeStep).roundToInt().coerceAtLeast((1f / safeStep).roundToInt()) * safeStep
    return snapped.coerceAtLeast(1f)
}

fun UiScope.resolvedTextScale(): Float = pixelPerfectTextScale(context.currentTextStyle.scale, context.currentFont.textScaleStep)

fun pixelPerfectPixel(value: Float): Float = value.roundToInt().toFloat()

fun UiScope.resolveGlyphPx(
    font: UiFont = context.currentFont,
    textStyle: TextStyle = context.currentTextStyle
): Float {
    val baseSize = textStyle.size ?: context.currentTheme.typography.body
    val scale = pixelPerfectTextScale(textStyle.scale, font.textScaleStep)
    return pixelPerfectPixel(baseSize.value * UiDensity.scale * UiDensity.fontScale * scale).coerceAtLeast(1f)
}

/**
 * The full set of primitives any widget -- built-in or consumer-defined -- is built from.
 * Nothing here knows about buttons, toggles, or any specific widget shape; the library's own
 * button/toggle/slider/dropdown are just the library's own extension functions written
 * against this same public surface. A consumer writes a custom widget the identical way:
 * `fun UiScope.myWidget(...) { val slot = claimSlot(...); ... }` -- no library change, no
 * capability gap versus a built-in widget.
 */
@AwakeUiDsl
interface UiScope {
    /**
     * Whether this scope's own [emit] routes to the overlay layer (painted after every
     * regular primitive this frame, regardless of call order -- see
     * [UiContext.endFrame]/[emitOverlay]). A composite widget that opens a **new** nested
     * scope to draw part of its own content (e.g. [buttonSlotInternal]'s `context.absolute(...)`
     * for its label) must pass this through explicitly -- a fresh scope defaults to
     * non-overlay, so failing to propagate it silently splits one widget's background and
     * label across two different paint passes. Confirmed as a real bug this way: a dropdown
     * popup's [button]-based options drew their background quad to the overlay layer (correct,
     * inherited from the popup's own overlay scope) but their label glyphs to the regular
     * layer (wrong, from a freshly-defaulted nested scope) -- the background, painted last,
     * silently covered the label.
     */
    val emitsToOverlay: Boolean

    /**
     * Direct reference to the owning context -- mirrors kool-engine's `UiScope.surface`. Lets
     * a composite widget (e.g. [panel]) build a nested scope from the SAME public factories
     * ([UiContext.createColumn]/[row]/[box]/[absolute]) every top-level caller already uses,
     * instead of a bespoke nesting primitive.
     */
    val context: UiContext

    /**
     * Reserves the next layout position for a widget of the given size and returns its
     * resolved screen-space rect. What "next position" means is entirely up to the
     * implementing scope -- [ColumnScope] advances a Y cursor, [AbsoluteScope] ignores
     * width/height and returns the exact x/y it was constructed with.
     */
    fun claimSlot(width: Dimension, height: Dimension): UiSlot

    fun hitTest(slot: UiSlot): Boolean
    fun isActive(id: String): Boolean
    fun tryClaimActive(id: String, hovered: Boolean)
    fun releaseActiveIfMatches(id: String)

    /** Normal, in-order draw primitive -- painted in call order. */
    fun emit(primitive: UiDrawPrimitive)

    /**
     * Always painted after every [emit]-ed primitive this frame, regardless of call order --
     * see [UiContext.endFrame]. Used by widgets whose content must never be covered by a
     * sibling drawn later in the same frame (e.g. an expanded dropdown's option list).
     */
    fun emitOverlay(primitive: UiDrawPrimitive)

    fun widgetState(id: String): WidgetState
}

private fun UiScope.defaultAlignment(): UiAlignment = when (this) {
    is BoxScope -> contentAlignment
    else -> UiAlignment.TopStart
}

fun UiScope.fillWidthOrNull(): Float? = (this as? FillAwareScope)?.fillWidth

fun UiScope.fillHeightOrNull(): Float? = (this as? FillAwareScope)?.fillHeight

fun UiScope.hasBoundedFillWidth(): Boolean = (this as? FillAwareScope)?.hasBoundedFillWidth == true

fun UiScope.hasBoundedFillHeight(): Boolean = (this as? FillAwareScope)?.hasBoundedFillHeight == true

fun UiScope.debugScopeLabel(): String {
    val typeName = this::class.simpleName ?: "UiScope"
    val name = (this as? FillAwareScope)?.testTag
    return if (name.isNullOrBlank()) typeName else "'$name' ($typeName)"
}


fun UiScope.claimModifiedSlot(
    defaultWidth: Dimension = Dimension.WrapContent,
    defaultHeight: Dimension = Dimension.WrapContent,
    modifier: UiModifier = UiModifier()
): UiSlot {
    val requestedWidth = modifier.width ?: defaultWidth
    val requestedHeight = modifier.height ?: defaultHeight
    val containerSlot = claimSlot(requestedWidth, requestedHeight)
    val width = requestedWidth.resolveAgainst(containerSlot.width)
    val height = requestedHeight.resolveAgainst(containerSlot.height)
    return containerSlot.place(
        width = width,
        height = height,
        alignment = modifier.alignment ?: defaultAlignment(),
        insets = modifier.insets,
        offsetX = modifier.offsetX.toPx(),
        offsetY = modifier.offsetY.toPx()
    ).also(context::recordMeasuredSlot)
}

/**
 * Nested-scope factories that inherit [UiScope.font]/[UiScope.theme]/[UiScope.textScale]/
 * [UiScope.emitsToOverlay] from the receiver automatically, instead of every call site
 * threading those four values (especially `overlayOnly`) through [UiContext.createColumn]/[row]/
 * [absolute]/[box] by hand. A composite widget opening a nested scope to draw part of its own
 * content should always prefer these over the raw [UiContext] factories -- forgetting to pass
 * `overlayOnly = emitsToOverlay` on a raw call is exactly what caused dropdown option labels to
 * silently paint behind their own backgrounds (see [UiScope.emitsToOverlay]); these make that
 * mistake impossible to make.
 */
fun UiScope.ProvideTextStyle(style: TextStyle, content: UiScope.() -> Unit) {
    context.pushTextStyle(style)
    this.content()
    context.popTextStyle()
}

fun UiScope.ProvideTheme(theme: UiTheme, content: UiScope.() -> Unit) {
    context.pushTheme(theme)
    this.content()
    context.popTheme()
}

fun UiScope.ProvideFont(font: UiFont, content: UiScope.() -> Unit) {
    context.pushFont(font)
    this.content()
    context.popFont()
}

fun UiScope.childColumn(
    slot: UiSlot,
    gap: Float = UiSpacing.sm.toPx(),
    insets: UiInsets = UiInsets.Zero,
    testTag: String? = null,
    hasBoundedFillWidth: Boolean = true,
    hasBoundedFillHeight: Boolean = true
): ColumnScope = context.createColumn(
    slot,
    gap,
    insets,
    testTag = testTag,
    hasBoundedFillWidth = hasBoundedFillWidth,
    hasBoundedFillHeight = hasBoundedFillHeight,
    overlayOnly = emitsToOverlay
)

fun UiScope.childRow(
    slot: UiSlot,
    gap: Float = UiSpacing.sm.toPx(),
    insets: UiInsets = UiInsets.Zero,
    testTag: String? = null,
    hasBoundedFillWidth: Boolean = true,
    hasBoundedFillHeight: Boolean = true
): RowScope = context.createRow(
    slot,
    gap,
    insets,
    testTag = testTag,
    hasBoundedFillWidth = hasBoundedFillWidth,
    hasBoundedFillHeight = hasBoundedFillHeight,
    overlayOnly = emitsToOverlay
)

fun UiScope.childAbsolute(
    slot: UiSlot,
    insets: UiInsets = UiInsets.Zero,
    testTag: String? = null
): AbsoluteScope = context.createAbsolute(slot, insets, testTag = testTag, overlayOnly = emitsToOverlay)

fun UiScope.childBox(
    slot: UiSlot,
    insets: UiInsets = UiInsets.Zero,
    contentAlignment: UiAlignment = UiAlignment.TopStart,
    testTag: String? = null,
    hasBoundedFillWidth: Boolean = true,
    hasBoundedFillHeight: Boolean = true
): BoxScope = context.createBox(
    slot,
    insets,
    contentAlignment,
    testTag = testTag,
    hasBoundedFillWidth = hasBoundedFillWidth,
    hasBoundedFillHeight = hasBoundedFillHeight,
    overlayOnly = emitsToOverlay
)

fun UiScope.resolveStyle(
    style: Style = Style.Empty,
    defaults: Style = Style.Empty,
    state: StyleState = MutableStyleState()
): ResolvedStyle = (defaults then style).resolve(state, context.currentTextStyle)

fun UiScope.recordSemantic(
    role: UiSemanticRole,
    bounds: UiSlot,
    id: String? = null,
    label: String? = null,
    contentBounds: UiSlot? = null,
    clippedBounds: UiSlot? = null,
    truncated: Boolean = false,
    lineCount: Int = 0,
    selected: Boolean? = null
) {
    context.recordSemanticInternal(
        UiSemanticNode(
            role = role,
            bounds = bounds,
            id = id,
            label = label,
            contentBounds = contentBounds,
            clippedBounds = clippedBounds,
            truncated = truncated,
            lineCount = lineCount,
            selected = selected
        )
    )
}

/**
 * TODO revisit: Experimental only
 */
val UiScope.theme : UiTheme
    get() = context.currentTheme
/**
 * TODO revisit: Experimental only
 */
val UiScope.font : UiFont
    get() = context.currentFont
/**
 * TODO revisit: Experimental only
 */
val UiScope.textStyle : TextStyle
    get() = context.currentTextStyle
/**
 * TODO revisit: Experimental only
 */
val UiScope.resolvedThemeCaptionStyle : TextStyle
    get() = textStyle then TextStyle(size = theme.typography.caption)
