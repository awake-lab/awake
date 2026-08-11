package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBodyText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCard
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSupportingText
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.spacer

internal fun ColumnScope.drawUiShowcaseCounterPreview(state: UiShowcaseRuntimeState) {
    shadcnBadge(id = "showcase-badge-counter", label = "STATE", variant = ShadcnBadgeVariant.Outline)
    spacer(Modifier.height(8f.dp))
    shadcnButton(
        id = "showcase-counter",
        label = if (state.showcasePrimaryClicks == 0) "Counter 0" else "Counter ${state.showcasePrimaryClicks}",
        variant = ShadcnButtonVariant.Primary,
    ).let { if (it) state.showcasePrimaryClicks++ }
}

internal fun ColumnScope.drawUiShowcasePopupPreview() {
    shadcnBadge(id = "showcase-badge-popup", label = "POPUP", variant = ShadcnBadgeVariant.Outline)
    spacer(Modifier.height(8f.dp))
    shadcnCard(id = "showcase-popup-card", modifier = Modifier.height(120f.dp)) {
        shadcnBodyText("Popup behavior is supplied by the Headless popup primitive and can be skinned independently.")
    }
}

internal fun ColumnScope.drawUiShowcaseTooltipPreview() {
    shadcnBadge(id = "showcase-badge-tooltip", label = "TOOLTIP", variant = ShadcnBadgeVariant.Outline)
    spacer(Modifier.height(8f.dp))
    shadcnSupportingText("Tooltips use an anchor slot and a neutral popup result; the recipe owns only shadcn visuals.")
}

internal fun ColumnScope.drawUiShowcasePopoverPreview() {
    shadcnBadge(id = "showcase-badge-popover", label = "POPOVER", variant = ShadcnBadgeVariant.Outline)
    spacer(Modifier.height(8f.dp))
    shadcnSupportingText("Popover placement and dismissal remain Headless behavior.")
}
