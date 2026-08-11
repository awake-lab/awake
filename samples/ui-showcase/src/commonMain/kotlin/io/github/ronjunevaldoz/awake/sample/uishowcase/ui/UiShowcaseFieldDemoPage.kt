package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSupportingText
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.spacer

internal fun ColumnScope.drawUiShowcaseFieldDemoPreview() {
    shadcnBadge(id = "showcase-badge-fields", label = "FIELDS", variant = ShadcnBadgeVariant.Outline)
    spacer(Modifier.height(8f.dp))
    shadcnSupportingText("Field labels, descriptions, validation, and input controls compose through the public Headless field contract.")
}
