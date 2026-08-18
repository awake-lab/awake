package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiImageVector
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.UiModifier
import io.github.ronjunevaldoz.awake.ui.headless.icon
import io.github.ronjunevaldoz.awake.ui.headless.size

/**
 * A group member's inner icon, not a member surface itself -- `icon()` paints no fill/border,
 * so it never needed the [LocalShadcnButtonGroup] corner-shape workaround the group's own
 * members (buttons) used to carry. Still reads the group local for its shadcn `size-4` (16dp)
 * icon-inside-button sizing default, which only applies inside a group.
 */
fun io.github.ronjunevaldoz.awake.ui.headless.UiScope.shadcnIcon(
    icon: UiImageVector,
    modifier: UiModifier = Modifier,
    tint: Color? = null
) {
    val insideGroup = currentLocal(LocalShadcnButtonGroup) != null
    icon(
        icon = icon,
        modifier = if (insideGroup) modifier.size(16.dp) else modifier,
        tint = tint,
    )
}