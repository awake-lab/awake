package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBodyText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCard
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSectionTitle
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSupportingText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.typography.shadcnSupportingLines
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.spacer

internal fun ColumnScope.drawUiShowcaseOverviewPreview() {
    shadcnBadge(id = "showcase-badge-showcase", label = "SHOWCASE", variant = ShadcnBadgeVariant.Secondary)
    shadcnBodyText("Dedicated sample route")
    shadcnSupportingText("The showcase is built from Headless behavior and design-system recipes.")
    spacer(Modifier.height(8f.dp))
    shadcnSupportingLines(listOf("Named IDs make state and semantic output inspectable.", "Theme values are injected through the public Ui API."))
}

internal fun ColumnScope.drawUiShowcaseReferenceComparisonPreview() {
    row(horizontalArrangement = Arrangement.spacedBy(12f.dp)) {
        shadcnCard(id = "ui-showcase-reference-spec", modifier = Modifier.height(180f.dp)) {
            shadcnSectionTitle("Reference cues")
            shadcnSupportingText("Compact controls and restrained surfaces.")
        }
        shadcnCard(id = "ui-showcase-reference-awake", modifier = Modifier.height(180f.dp)) {
            shadcnSectionTitle("Awake")
            shadcnSupportingText("The same structure rendered through the public Headless boundary.")
        }
    }
}

internal fun ColumnScope.drawUiShowcaseControlsPreview(state: UiShowcaseRuntimeState) {
    shadcnSupportingText("Theme controls are intentionally kept as a small public-surface proof.")
    spacer(Modifier.height(12f.dp))
    shadcnButton(
        id = "showcase-live",
        label = if (state.showcaseLiveBadge) "Live animation" else "Animation paused",
        variant = if (state.showcaseLiveBadge) ShadcnButtonVariant.Primary else ShadcnButtonVariant.Outline,
    ).let { if (it) state.showcaseLiveBadge = !state.showcaseLiveBadge }
    spacer(Modifier.height(8f.dp))
    shadcnButton(
        id = "showcase-danger-mode",
        label = if (state.showcaseDangerMode) "Danger treatment" else "Normal treatment",
        variant = if (state.showcaseDangerMode) ShadcnButtonVariant.Danger else ShadcnButtonVariant.Ghost,
    ).let { if (it) state.showcaseDangerMode = !state.showcaseDangerMode }
}
