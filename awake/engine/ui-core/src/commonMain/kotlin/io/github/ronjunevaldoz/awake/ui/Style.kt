// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

data class UiInsets(
    val start: Dp = UiShape.none,
    val top: Dp = UiShape.none,
    val end: Dp = UiShape.none,
    val bottom: Dp = UiShape.none
) {
    companion object {
        val Zero = UiInsets()
    }
}

fun UiInsets(all: Dp): UiInsets = UiInsets(all, all, all, all)

fun UiInsets(horizontal: Dp, vertical: Dp): UiInsets = UiInsets(horizontal, vertical, horizontal, vertical)

internal fun UiInsets.horizontalPx(): Float = (start + end).toPx()

internal fun UiInsets.verticalPx(): Float = (top + bottom).toPx()

fun UiSlot.inset(insets: UiInsets): UiSlot {
    val startPx = insets.start.toPx()
    val topPx = insets.top.toPx()
    val endPx = insets.end.toPx()
    val bottomPx = insets.bottom.toPx()
    return UiSlot(
        x = x + startPx,
        y = y + topPx,
        width = (width - startPx - endPx).coerceAtLeast(0f),
        height = (height - topPx - bottomPx).coerceAtLeast(0f)
    )
}

class StyleStateKey<T>(val defaultValue: T)

interface StyleState {
    val hovered: Boolean
    val active: Boolean
    val focused: Boolean
    val disabled: Boolean
    val selected: Boolean

    operator fun <T> get(key: StyleStateKey<T>): T
}

class MutableStyleState(
    override var hovered: Boolean = false,
    override var active: Boolean = false,
    override var focused: Boolean = false,
    override var disabled: Boolean = false,
    override var selected: Boolean = false
) : StyleState {
    private val values = HashMap<StyleStateKey<*>, Any?>()

    @Suppress("UNCHECKED_CAST")
    override operator fun <T> get(key: StyleStateKey<T>): T = values[key] as T? ?: key.defaultValue

    fun <T> set(key: StyleStateKey<T>, value: T): MutableStyleState {
        values[key] = value
        return this
    }
}

data class ResolvedStyle(
    val background: FloatArray? = null,
    val foreground: FloatArray? = null,
    val borderWidth: Dp = UiShape.none,
    val borderColor: FloatArray? = null,
    val shape: Dp = UiShape.none,
    val shapeSpec: UiShapeSpec? = null,
    val textScale: Float,
    val contentPadding: UiInsets = UiInsets.Zero
)

interface StyleScope {
    fun background(color: FloatArray)
    fun foreground(color: FloatArray)
    fun border(width: Dp, color: FloatArray) {
        borderWidth(width)
        borderColor(color)
    }

    fun borderWidth(width: Dp)
    fun borderColor(color: FloatArray)
    fun shape(radius: Dp)
    fun shape(shape: UiShapeSpec)
    fun textScale(scale: Float)
    fun contentPadding(all: Dp)
    fun contentPadding(horizontal: Dp, vertical: Dp)
    fun contentPadding(start: Dp, top: Dp, end: Dp, bottom: Dp)
    fun hovered(block: StyleScope.() -> Unit)
    fun active(block: StyleScope.() -> Unit)
    fun pressed(block: StyleScope.() -> Unit) = active(block)
    fun focused(block: StyleScope.() -> Unit)
    fun disabled(block: StyleScope.() -> Unit)
    fun selected(block: StyleScope.() -> Unit)
    fun <T> state(key: StyleStateKey<T>, value: T, block: StyleScope.() -> Unit)
}

class Style private constructor(
    private val rules: List<StyleRule>
) {
    companion object {
        val Empty = Style(emptyList())

        operator fun invoke(block: StyleScope.() -> Unit): Style {
            val builder = StyleBuilder()
            builder.block()
            return Style(builder.build())
        }
    }

    infix fun then(other: Style): Style = when {
        this === Empty -> other
        other === Empty -> this
        else -> Style(rules + other.rules)
    }

    fun resolve(state: StyleState = MutableStyleState(), fallbackTextScale: Float = 1f): ResolvedStyle {
        val builder = ResolvedStyleBuilder(textScale = fallbackTextScale)
        rules.forEach { it.apply(state, builder) }
        return builder.build()
    }
}

private class ResolvedStyleBuilder(
    var background: FloatArray? = null,
    var foreground: FloatArray? = null,
    var borderWidth: Dp = UiShape.none,
    var borderColor: FloatArray? = null,
    var shape: Dp = UiShape.none,
    var shapeSpec: UiShapeSpec? = null,
    var textScale: Float,
    var contentPadding: UiInsets = UiInsets.Zero
) {
    fun build(): ResolvedStyle = ResolvedStyle(
        background = background,
        foreground = foreground,
        borderWidth = borderWidth,
        borderColor = borderColor,
        shape = shape,
        shapeSpec = shapeSpec,
        textScale = textScale,
        contentPadding = contentPadding
    )
}

private data class StyleRule(
    val predicate: (StyleState) -> Boolean,
    val mutation: ResolvedStyleBuilder.() -> Unit
) {
    fun apply(state: StyleState, builder: ResolvedStyleBuilder) {
        if (predicate(state)) builder.mutation()
    }
}

private class StyleBuilder(
    private val predicate: (StyleState) -> Boolean = { true }
) : StyleScope {
    private val rules = ArrayList<StyleRule>()

    fun build(): List<StyleRule> = rules

    override fun background(color: FloatArray) {
        rules += StyleRule(predicate) { background = color }
    }

    override fun foreground(color: FloatArray) {
        rules += StyleRule(predicate) { foreground = color }
    }

    override fun borderWidth(width: Dp) {
        rules += StyleRule(predicate) { borderWidth = width }
    }

    override fun borderColor(color: FloatArray) {
        rules += StyleRule(predicate) { borderColor = color }
    }

    override fun shape(radius: Dp) {
        rules += StyleRule(predicate) {
            shape = radius
            shapeSpec = null
        }
    }

    override fun shape(shape: UiShapeSpec) {
        rules += StyleRule(predicate) {
            this.shape = UiShape.none
            shapeSpec = shape
        }
    }

    override fun textScale(scale: Float) {
        rules += StyleRule(predicate) { textScale = scale }
    }

    override fun contentPadding(all: Dp) {
        contentPadding(UiInsets(all))
    }

    override fun contentPadding(horizontal: Dp, vertical: Dp) {
        contentPadding(UiInsets(horizontal, vertical))
    }

    override fun contentPadding(start: Dp, top: Dp, end: Dp, bottom: Dp) {
        contentPadding(UiInsets(start, top, end, bottom))
    }

    private fun contentPadding(insets: UiInsets) {
        rules += StyleRule(predicate) { contentPadding = insets }
    }

    override fun hovered(block: StyleScope.() -> Unit) {
        nested({ it.hovered }, block)
    }

    override fun active(block: StyleScope.() -> Unit) {
        nested({ it.active }, block)
    }

    override fun focused(block: StyleScope.() -> Unit) {
        nested({ it.focused }, block)
    }

    override fun disabled(block: StyleScope.() -> Unit) {
        nested({ it.disabled }, block)
    }

    override fun selected(block: StyleScope.() -> Unit) {
        nested({ it.selected }, block)
    }

    override fun <T> state(key: StyleStateKey<T>, value: T, block: StyleScope.() -> Unit) {
        nested({ it[key] == value }, block)
    }

    private fun nested(extraPredicate: (StyleState) -> Boolean, block: StyleScope.() -> Unit) {
        val nestedBuilder = StyleBuilder { state -> predicate(state) && extraPredicate(state) }
        nestedBuilder.block()
        rules += nestedBuilder.build()
    }
}

private operator fun Dp.plus(other: Dp): Dp = Dp(value + other.value)
