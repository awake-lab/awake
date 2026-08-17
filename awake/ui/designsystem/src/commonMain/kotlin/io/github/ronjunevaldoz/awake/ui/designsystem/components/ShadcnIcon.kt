package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiImageVector
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.icon
import io.github.ronjunevaldoz.awake.ui.headless.size
import io.github.ronjunevaldoz.awake.ui.headless.styleable
import io.github.ronjunevaldoz.awake.ui.style.Style

fun io.github.ronjunevaldoz.awake.ui.headless.UiScope.shadcnIcon(
    icon: UiImageVector,
    modifier: Modifier = Modifier,
    tint: Color? = null
) {
    val groupCtx = currentLocal(LocalShadcnButtonGroup)
    val groupStyle = if (groupCtx != null) Style { shape(0f.dp) } else Style.Empty
    val groupModifier =
        if (groupCtx != null) modifier.styleable(groupStyle).size(16.dp) else modifier
    icon(
        icon = icon,
        modifier = groupModifier,
        tint = tint,
    )
}