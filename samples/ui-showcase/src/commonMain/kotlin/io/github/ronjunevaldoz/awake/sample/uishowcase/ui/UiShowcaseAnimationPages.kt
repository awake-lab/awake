package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCard
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnMuted
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.spacer

internal fun ColumnScope.drawUiShowcaseShimmerPreview() {
    shadcnBadge(id = "showcase-badge-shimmer", label = "SHIMMER", variant = ShadcnBadgeVariant.Outline)
    spacer(Modifier.height(8f.dp))
    shadcnCard(id = "showcase-shimmer-card", modifier = Modifier.height(92f.dp)) {
        shadcnText("A neutral placeholder surface keeps animation behavior independent of a skin.")
    }
}

internal fun ColumnScope.drawUiShowcaseEasingPreview() {
    shadcnBadge(id = "showcase-badge-easing", label = "EASING", variant = ShadcnBadgeVariant.Outline)
    spacer(Modifier.height(8f.dp))
    shadcnMuted("Animation curves are runtime behavior; the design system only supplies the surrounding visual recipe.")
}

internal fun ColumnScope.drawUiShowcaseFadeVisibilityPreview() {
    shadcnBadge(id = "showcase-badge-fade", label = "FADE VISIBILITY", variant = ShadcnBadgeVariant.Outline)
    spacer(Modifier.height(8f.dp))
    shadcnMuted("Visibility transitions remain composable with Headless slots and named component IDs.")
}
