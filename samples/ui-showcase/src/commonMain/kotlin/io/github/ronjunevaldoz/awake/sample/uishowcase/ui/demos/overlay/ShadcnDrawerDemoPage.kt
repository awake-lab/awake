package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.demos.overlay

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSupportingText
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.spacer

internal fun ColumnScope.drawShadcnDrawerDemoPreview() {
    shadcnBadge(id = "showcase-badge-drawer", label = "DRAWER", variant = ShadcnBadgeVariant.Outline)
    spacer(Modifier.height(8f.dp))
    shadcnSupportingText("Slide-panel placement and dismissal remain Headless behavior; the branded drawer recipe supplies only visuals.")
}
