// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.theme.TextStyle
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.createColumn
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.typography.supportingLines
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.height
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.ext.row
import io.github.ronjunevaldoz.awake.ui.layouts.ext.spacer
import io.github.ronjunevaldoz.awake.ui.layouts.ext.surface
import io.github.ronjunevaldoz.awake.ui.sp
import io.github.ronjunevaldoz.awake.ui.padding
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.UiTextWrap
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import io.github.ronjunevaldoz.awake.ui.width

internal fun ColumnScope.drawUiShowcaseFontsPreview() {
    awakeShadcnBadge("TYPOGRAPHY", variant = AwakeShadcnBadgeVariant.Outline)
    supportingLines(
        listOf(
            "The same specimen rendered through each Awake UI font path so we can judge edge quality and spacing directly."
        )
    )
    spacer(UiModifier().height(8f.dp))
    row(height = 292f.dp, horizontalArrangement = Arrangement.spacedBy(12f.dp)) {
        surface(
            id = "showcase-font-bitmap",
            width = Dimension.Fixed(264f.dp),
            height = Dimension.Fixed(292f.dp),
            style = Style { shape(14f.dp) }
        ) { slot ->
            drawUiShowcaseFontSpecimen(
                slot = slot,
                label = "Bitmap",
                detail = "Coverage-alpha atlas from the original grid source.",
                previewFont = BitmapFont()
            )
        }
        surface(
            id = "showcase-font-truesans",
            width = Dimension.Fixed(264f.dp),
            height = Dimension.Fixed(292f.dp),
            style = Style { shape(14f.dp) }
        ) { slot ->
            drawUiShowcaseFontSpecimen(
                slot = slot,
                label = "True Font",
                detail = "Real Roboto glyph atlas baked from a TTF source with proportional quad metrics.",
                previewFont = UiFonts.trueSans()
            )
        }
    }
    spacer(UiModifier().height(8f.dp))
    supportingLines(
        listOf(
            "Bitmap stays closer to the authored pixel grid and remains useful for low-fi or debug surfaces.",
            "True Font uses real outline-derived glyphs, so spacing and letterforms stop fighting the renderer."
        )
    )
}

private fun ColumnScope.drawUiShowcaseFontSpecimen(
    slot: UiSlot,
    label: String,
    detail: String,
    previewFont: UiFont,
) {
    context.pushFont(previewFont)
    val specimenScope = context.createColumn(
        slot = slot,
        modifier = UiModifier().padding(16f.dp),
        verticalArrangement = Arrangement.spacedBy(8f.dp),
        overlayOnly = emitsToOverlay
    )
    specimenScope.awakeShadcnBadge(
        label.uppercase(),
        modifier = UiModifier().width(120f.dp).height(28f.dp),
        variant = AwakeShadcnBadgeVariant.Outline
    )
    specimenScope.text(
        label = "Awake UI",
        color = theme.tokens.foreground,
        textStyle = TextStyle(size = 18f.sp)
    )
    specimenScope.text(
        label = "Sphinx 123",
        color = theme.tokens.foreground,
        textStyle = TextStyle(size = 16f.sp)
    )
    specimenScope.text(
        label = "THE QUICK BROWN FOX",
        color = theme.tokens.foreground,
        textStyle = TextStyle(size = 12.sp)
    )
    specimenScope.text(
        label = detail,
        slot = specimenScope.claimSlot(Dimension.FillMax, Dimension.Fixed(44f.dp)),
        color = theme.tokens.mutedForeground,
        wrap = UiTextWrap.Word,
        overflow = UiTextOverflow.Ellipsis,
        maxLines = 3,
        textStyle = TextStyle(size = 11.sp)
    )
    context.popFont()
}
