// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.UiSpacing
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.theme.UiThemeValues
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.theme.UiComponentStyles

/** Shadcn's override of Core's neutral component defaults. */
internal class ShadcnComponentStyles(values: UiThemeValues) : UiComponentStyles {
    private val colors = values.colors
    private val type = values.typography
    private val shapes = values.shapes

    override val button = Style { background(colors.primary, "primary"); foreground(colors.primaryForeground, "primary-foreground"); shape(shapes.md); textSize(type.label) }
    override val toggle = Style { background(colors.secondary, "secondary"); foreground(colors.secondaryForeground, "secondary-foreground"); shape(shapes.md); textSize(type.label) }
    override val checkbox = Style { background(colors.background, "background"); foreground(colors.foreground, "foreground"); border(1f.dp, colors.input, "input"); shape(4f.dp); textSize(type.label) }
    override val slider = Style { background(colors.input, "input"); foreground(colors.foreground, "foreground"); border(1f.dp, colors.input, "input"); shape(shapes.full); textSize(type.label) }
    override val dropdown = Style { background(colors.background, "background"); foreground(colors.foreground, "foreground"); border(1f.dp, colors.input, "input"); shape(shapes.md); contentPadding(UiSpacing.sm); textSize(type.label) }
    override val separator = Style { background(colors.border, "border"); shape(UiShape.none) }
    override val surface = Style { background(colors.card, "card"); foreground(colors.cardForeground, "card-foreground"); border(1f.dp, colors.border, "border"); shape(shapes.xl); contentPadding(UiSpacing.sm) }
    override val textField = Style { background(colors.background, "background"); foreground(colors.foreground, "foreground"); border(1f.dp, colors.input, "input"); shape(shapes.md); contentPadding(UiSpacing.sm); textSize(type.label); focused { borderColor(colors.ring, "ring") } }
    override val avatar = Style { background(colors.muted, "muted"); foreground(colors.foreground, "foreground"); textSize(type.label) }
}
