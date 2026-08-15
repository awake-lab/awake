// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.UiPrimitiveScope
import io.github.ronjunevaldoz.awake.ui.ProvideTextStyle
import io.github.ronjunevaldoz.awake.ui.ProvideTheme
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.api.theme.UiThemeValues
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.headless.internal.layout.withIntrinsicLabelSize as primitiveWithIntrinsicLabelSize
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.theme.TextStyle
import io.github.ronjunevaldoz.awake.ui.theme.asRuntimeTheme

@DslMarker
annotation class AwakeUiDsl

/**
 * Public receiver for ordinary Headless widgets and design-system recipes.
 *
 * The runtime primitive scope is intentionally internal: callers compose behavior through this
 * type and cannot reach Core's frame, draw, or input escape hatches from a widget recipe.
 */
@AwakeUiDsl
interface UiScope {
    val primitive: UiPrimitiveScope

    /** Immutable values from the currently installed theme, without Core runtime access. */
    val themeValues: UiThemeValues
        get() = primitive.context.currentTheme

    /** Typography supplied by the nearest [provideTheme] block. */
    val typography
        get() = themeValues.typography

    /** Effective inherited text styling at this point in the UI tree. */
    val textStyle: TextStyle
        get() = primitive.context.currentTextStyle
}

internal class DefaultUiScope internal constructor(
    override val primitive: UiPrimitiveScope,
) : UiScope

fun UiScope(primitive: UiPrimitiveScope): UiScope = DefaultUiScope(primitive)

/** Requests keyboard focus for a Headless-owned input or composite widget. */
fun UiScope.requestFocus(id: String) = primitive.context.requestFocus(id)

/**
 * Provides theme values to this subtree. Child widgets read them from [themeValues] rather than
 * receiving theme or typography plumbing parameters.
 */
fun UiScope.provideTheme(values: UiThemeValues, content: UiScope.() -> Unit) {
    primitive.ProvideTheme(values.asRuntimeTheme()) { content(DefaultUiScope(this)) }
}

/** Provides an inheritable text-style override to this subtree. */
fun UiScope.provideTextStyle(style: TextStyle, content: UiScope.() -> Unit) {
    primitive.ProvideTextStyle(style) { content(DefaultUiScope(this)) }
}

/** Applies natural label width and font-metric height to surface recipes. */
fun UiScope.withIntrinsicLabelSize(
    label: String,
    modifier: Modifier = Modifier,
    style: Style = Style.Empty,
): Modifier = HeadlessModifier(
    primitive.primitiveWithIntrinsicLabelSize(
        modifier = modifier.asPrimitiveModifier(),
        label = label,
        style = style,
    ),
)

/** Compatibility overload while callers migrate to [Style]. */
@Deprecated("Use the Style overload")
fun UiScope.withIntrinsicLabelSize(
    label: String,
    modifier: Modifier = Modifier,
    style: SurfaceStyle,
): Modifier = withIntrinsicLabelSize(label, modifier, style.asPrimitiveStyle())

/**
 * Creates the public Headless receiver for a root UI region.
 *
 * App integration owns [UiContext]; ordinary widgets receive only [UiScope]. The explicit
 * [slot] keeps root sizing at the app/runtime boundary rather than inventing a Headless size.
 */
fun UiContext.createUiScope(slot: UiBounds): UiScope =
    UiScope(createBox(slot = slot))

/** Internal bridge used while Headless behavior is migrated from Core's raw receiver. */
internal fun UiPrimitiveScope.asUiScope(): UiScope = UiScope(this)
