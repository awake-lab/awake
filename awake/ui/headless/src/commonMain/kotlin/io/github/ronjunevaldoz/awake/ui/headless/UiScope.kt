// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.AwakeUiDsl
import io.github.ronjunevaldoz.awake.ui.provideTextStyle
import io.github.ronjunevaldoz.awake.ui.UiPrimitiveScope
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.headless.internal.layout.withIntrinsicLabelSize as primitiveWithIntrinsicLabelSize
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.theme.TextStyle
import io.github.ronjunevaldoz.awake.ui.font
import io.github.ronjunevaldoz.awake.ui.scope.resolveGlyphPx as primitiveResolveGlyphPx
import io.github.ronjunevaldoz.awake.ui.scope.recordSemantic

/**
 * Public receiver for ordinary Headless widgets and design-system recipes.
 *
 * The runtime primitive scope is intentionally internal: callers compose behavior through this
 * type and cannot reach Core's frame, draw, or input escape hatches from a widget recipe.
 */
@AwakeUiDsl
interface UiScope {
    val primitive: UiPrimitiveScope

}

internal class DefaultUiScope internal constructor(
    override val primitive: UiPrimitiveScope,
) : UiScope

fun UiScope(primitive: UiPrimitiveScope): UiScope = DefaultUiScope(primitive)

/** Requests keyboard focus for a Headless-owned input or composite widget. */
fun UiScope.requestFocus(id: String) = primitive.context.requestFocus(id)

/** Current composition font for Headless measurement recipes. */
val UiScope.currentFont: UiFont
    get() = primitive.font

/** Resolves a text style through the current composition font and density. */
fun UiScope.resolveGlyphPx(textStyle: TextStyle): Float = primitive.primitiveResolveGlyphPx(textStyle = textStyle)

/** Records a generic panel semantic for a Headless-owned composite region. */
fun UiScope.panelSemantics(id: String, bounds: UiBounds) {
    primitive.recordSemantic(role = UiSemanticRole.Panel, id = id, bounds = bounds)
}

/**
 * Provides an inheritable text style to this subtree.
 *
 * The facade counterpart to Core's `provideTextStyle`. Without it, a caller inside a scope that
 * already provides a text style (`shadcnTheme { }` provides the body style) had no way to
 * override it without dropping to `primitive.` and losing the headless receiver for the whole
 * subtree -- which is why the snapshot fixtures still push theme/text-style imperatively.
 */
fun UiScope.provideTextStyle(style: TextStyle, content: UiScope.() -> Unit) {
    primitive.provideTextStyle(style) { content(UiScope(this)) }
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
