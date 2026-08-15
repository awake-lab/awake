// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.style

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.UiShapeSpec
import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.api.Sp
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets
import io.github.ronjunevaldoz.awake.ui.font.FontWeight
import io.github.ronjunevaldoz.awake.ui.theme.TextStyle

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
    override var selected: Boolean = false,
) : StyleState {
    private val values = HashMap<StyleStateKey<*>, Any?>()

    // values is only ever written by set(key: StyleStateKey<T>, value: T), so any entry
    // found under `key` was stored as that same key's own T.
    @Suppress("UNCHECKED_CAST")
    override operator fun <T> get(key: StyleStateKey<T>): T = values[key] as T? ?: key.defaultValue

    fun <T> set(key: StyleStateKey<T>, value: T): MutableStyleState {
        values[key] = value
        return this
    }
}

data class ResolvedStyle(
    val background: Color? = null,
    val backgroundToken: String? = null,
    val foreground: Color? = null,
    val foregroundToken: String? = null,
    val borderWidth: Dp = UiShape.none,
    val borderColor: Color? = null,
    val borderColorToken: String? = null,
    val shape: Dp = UiShape.none,
    val shapeSpec: UiShapeSpec? = null,
    val shadow: UiShadow? = null,
    val textStyle: TextStyle = TextStyle.Default,
    val textStyleToken: String? = null,
    val contentPadding: UiInsets = UiInsets.Zero,
    val animation: StyleAnimation? = null,
)

data class UiShadow(
    val color: Color,
    val offsetX: Dp = 0f.dp,
    val offsetY: Dp = 0f.dp,
    val blurRadius: Dp = 0f.dp,
    val spread: Dp = 0f.dp,
    val tokenId: String? = null,
)

data class StyleAnimation(
    val target: Style,
    val responsiveness: Float = 12f,
)

interface StyleScope {
    fun background(color: Color, tokenId: String? = null)
    fun foreground(color: Color, tokenId: String? = null)
    fun border(width: Dp, color: Color, tokenId: String? = null) {
        borderWidth(width)
        borderColor(color, tokenId)
    }
    fun borderWidth(width: Dp)
    fun borderColor(color: Color, tokenId: String? = null)
    fun shape(radius: Dp)
    fun shape(shape: UiShapeSpec)
    fun shadow(
        color: Color,
        offsetX: Dp = 0f.dp,
        offsetY: Dp = 0f.dp,
        blurRadius: Dp = 0f.dp,
        spread: Dp = 0f.dp,
        tokenId: String? = null,
    )
    fun textStyle(style: TextStyle, tokenId: String? = null)
    fun textScale(scale: Float)
    fun textSize(size: Sp, tokenId: String? = null)
    fun fontSize(size: Sp, tokenId: String? = null) = textSize(size, tokenId)
    fun lineHeight(height: Sp)
    fun fontWeight(weight: FontWeight)
    fun letterSpacing(spacing: Sp)
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
    fun animate(style: Style, responsiveness: Float = 12f)
}

class Style private constructor(
    private val rules: List<StyleRule>,
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

    // State rules (hovered/active/focused/disabled/selected) must win over any unconditional
    // rule regardless of declaration order: `then` concatenates rule lists, and a later Style
    // block's own unconditional override (e.g. a themed component's static background) would
    // otherwise clobber an earlier block's state-conditional rule even when the state predicate
    // matches -- this silently killed hover/active/focus styling on slider/toggle/switch/
    // textField wherever a theme layered a plain color on top of a state-varying fallback.
    fun resolve(state: StyleState = MutableStyleState(), fallbackTextStyle: TextStyle = TextStyle.Default): ResolvedStyle {
        val builder = ResolvedStyleBuilder(textStyle = fallbackTextStyle)
        rules.filter { !it.isConditional }.forEach { it.apply(state, builder) }
        rules.filter { it.isConditional }.forEach { it.apply(state, builder) }
        return builder.build()
    }
}

private class ResolvedStyleBuilder(
    var background: Color? = null,
    var backgroundToken: String? = null,
    var foreground: Color? = null,
    var foregroundToken: String? = null,
    var borderWidth: Dp = UiShape.none,
    var borderColor: Color? = null,
    var borderColorToken: String? = null,
    var shape: Dp = UiShape.none,
    var shapeSpec: UiShapeSpec? = null,
    var shadow: UiShadow? = null,
    var textStyle: TextStyle = TextStyle.Default,
    var textStyleToken: String? = null,
    var contentPadding: UiInsets = UiInsets.Zero,
    var animation: StyleAnimation? = null,
) {
    fun build(): ResolvedStyle = ResolvedStyle(
        background = background,
        backgroundToken = backgroundToken,
        foreground = foreground,
        foregroundToken = foregroundToken,
        borderWidth = borderWidth,
        borderColor = borderColor,
        borderColorToken = borderColorToken,
        shape = shape,
        shapeSpec = shapeSpec,
        shadow = shadow,
        textStyle = textStyle,
        textStyleToken = textStyleToken,
        contentPadding = contentPadding,
        animation = animation,
    )
}

private data class StyleRule(
    val predicate: (StyleState) -> Boolean,
    val isConditional: Boolean,
    val mutation: ResolvedStyleBuilder.() -> Unit,
) {
    fun apply(state: StyleState, builder: ResolvedStyleBuilder) {
        if (predicate(state)) builder.mutation()
    }
}

private class StyleBuilder(
    private val predicate: (StyleState) -> Boolean = { true },
    private val isConditional: Boolean = false,
) : StyleScope {
    private val rules = ArrayList<StyleRule>()

    private fun rule(mutation: ResolvedStyleBuilder.() -> Unit) {
        rules += StyleRule(predicate, isConditional, mutation)
    }

    fun build(): List<StyleRule> = rules

    override fun background(color: Color, tokenId: String?) {
        rule {
            background = color
            backgroundToken = tokenId
        }
    }

    override fun foreground(color: Color, tokenId: String?) {
        rule {
            foreground = color
            foregroundToken = tokenId
        }
    }

    override fun borderWidth(width: Dp) {
        rule { borderWidth = width }
    }

    override fun borderColor(color: Color, tokenId: String?) {
        rule {
            borderColor = color
            borderColorToken = tokenId
        }
    }

    override fun shape(radius: Dp) {
        rule {
            shape = radius
            shapeSpec = null
        }
    }

    override fun shape(shape: UiShapeSpec) {
        rule {
            this.shape = UiShape.none
            shapeSpec = shape
        }
    }

    override fun shadow(
        color: Color,
        offsetX: Dp,
        offsetY: Dp,
        blurRadius: Dp,
        spread: Dp,
        tokenId: String?,
    ) {
        rule {
            shadow = UiShadow(
                color = color,
                offsetX = offsetX,
                offsetY = offsetY,
                blurRadius = blurRadius,
                spread = spread,
                tokenId = tokenId,
            )
        }
    }

    override fun textStyle(style: TextStyle, tokenId: String?) {
        rule {
            textStyle = textStyle then style
            textStyleToken = tokenId
        }
    }

    override fun textScale(scale: Float) {
        rule { textStyle = textStyle.copy(scale = scale) }
    }

    override fun textSize(size: Sp, tokenId: String?) {
        rule {
            textStyle = textStyle.copy(size = size)
            textStyleToken = tokenId
        }
    }

    override fun lineHeight(height: Sp) {
        rule { textStyle = textStyle.copy(lineHeight = height) }
    }

    override fun fontWeight(weight: FontWeight) {
        rule { textStyle = textStyle.copy(weight = weight) }
    }

    override fun letterSpacing(spacing: Sp) {
        rule { textStyle = textStyle.copy(letterSpacing = spacing) }
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
        rule { contentPadding = insets }
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

    override fun animate(style: Style, responsiveness: Float) {
        rule { animation = StyleAnimation(style, responsiveness) }
    }

    private fun nested(extraPredicate: (StyleState) -> Boolean, block: StyleScope.() -> Unit) {
        val nestedBuilder = StyleBuilder(
            predicate = { state -> predicate(state) && extraPredicate(state) },
            isConditional = true,
        )
        nestedBuilder.block()
        rules += nestedBuilder.build()
    }
}
