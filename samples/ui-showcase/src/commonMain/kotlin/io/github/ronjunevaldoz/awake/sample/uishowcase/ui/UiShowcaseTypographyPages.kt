// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBodyText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnHeadline
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnLabel
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSectionHeader
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSectionTitle
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSupportingText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.typography.shadcnSupportingLines
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.headless.input.text.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.headless.input.text.UiTextWrap
import io.github.ronjunevaldoz.awake.ui.headless.input.text.text
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.layout.UiInsets
import io.github.ronjunevaldoz.awake.ui.layout.toDimension
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.layouts.spacer
import io.github.ronjunevaldoz.awake.ui.layouts.surface
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.sp
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.theme.TextStyle

internal fun ColumnScope.drawUiShowcaseTypographySpecimenPreview() {
    shadcnSectionHeader(
        title = "Typography",
        description = "The shadcn text component family: section title, headline, body, supporting/caption, and generic muted text.",
    )
    spacer(Modifier.height(12f.dp))
    shadcnSectionTitle("Section Title")
    spacer(Modifier.height(8f.dp))
    shadcnHeadline("Headline text sets the tone for a page or panel.")
    spacer(Modifier.height(8f.dp))
    shadcnBodyText("Body text is the default reading size for paragraphs and descriptions across every shadcn component.")
    spacer(Modifier.height(8f.dp))
    shadcnSupportingText("Supporting text is the muted caption size, used for helper copy and secondary detail.")
    spacer(Modifier.height(8f.dp))
    shadcnText("Generic shadcn text, unmuted.")
    shadcnText("Generic shadcn text, muted.", muted = true)
    spacer(Modifier.height(12f.dp))
    shadcnSupportingText("Label: a purely presentational field label -- shadcnFieldLabel (used throughout the Text Input and Checkout Form pages) is sugar over this.")
    spacer(Modifier.height(4f.dp))
    row(horizontalArrangement = Arrangement.spacedBy(16f.dp), modifier = Modifier.height(20f.dp.toDimension())) {
        shadcnLabel("Name")
        shadcnLabel("Email", required = true)
        shadcnLabel("Legacy field", disabled = true)
    }
}

internal fun ColumnScope.drawUiShowcaseFontsPreview() {
    shadcnBadge("TYPOGRAPHY", variant = ShadcnBadgeVariant.Outline)
    shadcnSupportingLines(
        listOf(
            "The same specimen rendered through each Awake UI font path so we can judge edge quality and spacing directly.",
        ),
    )
    spacer(Modifier.height(8f.dp))
    row(
        horizontalArrangement = Arrangement.spacedBy(12f.dp),
        modifier = Modifier.height(292f.dp.toDimension()),
    ) {
        surface(
            id = "showcase-font-bitmap",
            style = Style { shape(14f.dp) },
            modifier = Modifier.copy(
                widthDimension = Dimension.Fixed(264f.dp),
                heightDimension = Dimension.Fixed(292f.dp),
            ),
        ) { slot ->
            drawUiShowcaseFontSpecimen(
                slot = slot,
                label = "Bitmap",
                detail = "Coverage-alpha atlas from the original grid source.",
                previewFont = BitmapFont(),
            )
        }
        surface(
            id = "showcase-font-truesans",
            style = Style { shape(14f.dp) },
            modifier = Modifier.copy(
                widthDimension = Dimension.Fixed(264f.dp),
                heightDimension = Dimension.Fixed(292f.dp),
            ),
        ) { slot ->
            drawUiShowcaseFontSpecimen(
                slot = slot,
                label = "True Font",
                detail = "Real Roboto glyph atlas baked from a TTF source with proportional quad metrics.",
                previewFont = UiFonts.trueSans(),
            )
        }
    }
    spacer(Modifier.height(8f.dp))
    shadcnSupportingLines(
        listOf(
            "Bitmap stays closer to the authored pixel grid and remains useful for low-fi or debug surfaces.",
            "True Font uses real outline-derived glyphs, so spacing and letterforms stop fighting the renderer.",
        ),
    )
}

private fun ColumnScope.drawUiShowcaseFontSpecimen(
    slot: UiBounds,
    label: String,
    detail: String,
    previewFont: UiFont,
) {
    context.pushFont(previewFont)
    val specimenScope = context.createColumn(
        slot = slot,
        insets = UiInsets(16f.dp),
        verticalArrangement = Arrangement.spacedBy(8f.dp),
        overlayOnly = emitsToOverlay,
    )
    specimenScope.shadcnBadge(
        label.uppercase(),
        modifier = Modifier.width(120f.dp).height(28f.dp),
        variant = ShadcnBadgeVariant.Outline,
    )
    specimenScope.text(
        label = "Awake UI",
        color = theme.colors.foreground,
        textStyle = TextStyle(size = 18f.sp),
    )
    specimenScope.text(
        label = "Sphinx 123",
        color = theme.colors.foreground,
        textStyle = TextStyle(size = 16f.sp),
    )
    specimenScope.text(
        label = "THE QUICK BROWN FOX",
        color = theme.colors.foreground,
        textStyle = TextStyle(size = 12.sp),
    )
    specimenScope.text(
        label = detail,
        slot = specimenScope.claimSlot(Dimension.FillMax, Dimension.Fixed(44f.dp)),
        color = theme.colors.mutedForeground,
        wrap = UiTextWrap.Word,
        overflow = UiTextOverflow.Ellipsis,
        maxLines = 3,
        textStyle = TextStyle(size = 11.sp),
    )
    context.popFont()
}
