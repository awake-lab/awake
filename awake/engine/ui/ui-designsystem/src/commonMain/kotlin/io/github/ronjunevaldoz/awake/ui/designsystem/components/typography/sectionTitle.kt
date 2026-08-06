package io.github.ronjunevaldoz.awake.ui.designsystem.components.typography

import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

fun UiScope.shadcnSectionTitle(
    title: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Companion {
        foreground(theme.colors.mutedForeground)
        textSize(theme.typography.label)
    }
): UiBounds = text(title, modifier = modifier, style = style)

@Deprecated(
    message = "Use shadcnSectionTitle for UI design system typography functions",
    replaceWith = ReplaceWith("shadcnSectionTitle(title, modifier, style)")
)
fun UiScope.sectionTitle(
    title: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Companion {
        foreground(theme.colors.mutedForeground)
        textSize(theme.typography.label)
    }
): UiBounds = shadcnSectionTitle(title = title, modifier = modifier, style = style)
