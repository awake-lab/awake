package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnH2
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnMuted
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSectionTitle
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSmall
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.spacer

internal fun ColumnScope.drawUiShowcaseTypographySpecimenPreview() {
    shadcnSectionTitle(title = "Typography", description = "Headless text structure with shadcn visual tokens.")
    spacer(Modifier.height(8f.dp))
    shadcnSectionTitle("Section Title")
    shadcnH2("Headline text sets the tone for a page or panel.")
    shadcnText("Body text is the default reading size for paragraphs and descriptions.")
    shadcnMuted("Supporting text is muted helper copy.")
    shadcnText("Generic shadcn text.")
    row(horizontalArrangement = Arrangement.spacedBy(12f.dp), modifier = Modifier.height(24f.dp)) {
        shadcnSmall("Small Text")
        shadcnBadge(id = "badge-small-text", label = "Badge", variant = ShadcnBadgeVariant.Outline)
    }
}

internal fun ColumnScope.drawUiShowcaseFontsPreview() {
    shadcnBadge(id = "showcase-badge-typography", label = "TYPOGRAPHY", variant = ShadcnBadgeVariant.Outline)
    spacer(Modifier.height(8f.dp))
    shadcnMuted("Font metrics are supplied by the active runtime theme; components do not reach into Core font stacks.")
}
