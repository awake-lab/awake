// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.UiImageVector
import io.github.ronjunevaldoz.awake.ui.headless.HeroIcons

/** The single icon registry every `shadcn*` component reaches for -- `var`, not `val`, so a
 * consuming app can swap the whole icon set (a different open-source library, or hand-drawn
 * brand glyphs) by reassigning fields here once at startup, without editing
 * [io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCollapsible],
 * [shadcnCollapsibleCard], or any other component that draws a chevron/check/etc. Defaults to
 * [HeroIcons] (real shadcn/ui's own default icon set is Lucide, but this engine only has
 * Heroicons ported so far -- see [HeroIcons]'s own doc comment) -- add a field here, not a
 * one-off `UiImageVector` inline in whichever component first needs a new glyph, so every future
 * caller gets the same override seam instead of a component-specific one. */
object ShadcnIcons {
    var chevronDown: UiImageVector = HeroIcons.Solid20Mini.chevronDown
    var chevronUp: UiImageVector = HeroIcons.Solid20Mini.chevronUp
}
