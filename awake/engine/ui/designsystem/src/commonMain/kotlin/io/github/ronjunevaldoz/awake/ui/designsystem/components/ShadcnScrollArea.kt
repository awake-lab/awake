// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiScrollConfig
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.surface
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.verticalScroll
import io.github.ronjunevaldoz.awake.ui.rememberScrollState
import io.github.ronjunevaldoz.awake.ui.style.Style

/** Real shadcn's overlay scrollbar thumb: `w-2.5` (10dp), `rounded-full`, `bg-border`, a small
 * gap from the container's own edge. The scroll physics/wheel handling and the thumb draw itself
 * both already exist in `ui-core` ([io.github.ronjunevaldoz.awake.ui.modifier.verticalScroll] +
 * [io.github.ronjunevaldoz.awake.ui.scrollPanel]'s own [io.github.ronjunevaldoz.awake.ui.verticalScrollThumb]
 * call) -- this only supplies the shadcn-shaped [UiScrollConfig], not new scroll behaviour. */
// gap matches real ScrollAreaScrollbar's `p-px` -- literally 1px, Tailwind's px step, not
// p-0.5(2dp). Stays a plain literal for the same reason shadcn's own source uses `p-px`.
private val ShadcnScrollAreaConfig = UiScrollConfig(width = 10f.dp, gap = 1f.dp)

/**
 * Real shadcn's `ScrollArea`: a viewport with a styled overlay scrollbar that only appears once
 * content overflows. Transparent by default (real `ScrollArea` carries no background/border of
 * its own) -- pass [style] for a bordered/backed variant.
 */
fun UiScope.shadcnScrollArea(
    id: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiBounds {
    val scrollState = rememberScrollState("$id.scroll")
    return surface(
        id = id,
        modifier = modifier.verticalScroll(scrollState, ShadcnScrollAreaConfig),
        style = style,
        content = content,
    )
}

/** [shadcnScrollArea] override for [ColumnScope]. */
fun ColumnScope.shadcnScrollArea(
    id: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiBounds = (this as UiScope).shadcnScrollArea(id, modifier, style, content)
