package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBodyText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnHeadline
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnLabel
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSectionHeader
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSectionTitle
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSupportingText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.spacer

internal fun ColumnScope.drawUiShowcaseTypographySpecimenPreview() {
    shadcnSectionHeader(title = "Typography", description = "Headless text structure with shadcn visual tokens.")
    spacer(Modifier.height(8f.dp))
    shadcnSectionTitle("Section Title")
    shadcnHeadline("Headline text sets the tone for a page or panel.")
    shadcnBodyText("Body text is the default reading size for paragraphs and descriptions.")
    shadcnSupportingText("Supporting text is muted helper copy.")
    shadcnText("Generic shadcn text.")
    row(horizontalArrangement = Arrangement.spacedBy(12f.dp), modifier = Modifier.height(24f.dp)) {
        shadcnText("Name")
        shadcnText("Email")
    }
}

internal fun ColumnScope.drawUiShowcaseFontsPreview() {
    shadcnBadge(id = "showcase-badge-typography", label = "TYPOGRAPHY", variant = ShadcnBadgeVariant.Outline)
    spacer(Modifier.height(8f.dp))
    shadcnSupportingText("Font metrics are supplied by the active runtime theme; components do not reach into Core font stacks.")
}
