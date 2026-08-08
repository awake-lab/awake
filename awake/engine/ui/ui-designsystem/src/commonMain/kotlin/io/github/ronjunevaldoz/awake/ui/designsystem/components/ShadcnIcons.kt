// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.UiImageVector
import io.github.ronjunevaldoz.awake.ui.unstyled.HeroIcons

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
    var chevronLeft: UiImageVector = HeroIcons.Solid20Mini.chevronLeft
    var chevronRight: UiImageVector = HeroIcons.Solid20Mini.chevronRight
    var squares2x2: UiImageVector = HeroIcons.Solid20Mini.squares2x2
    var xMark: UiImageVector = HeroIcons.Solid20Mini.xMark
    var sparkles: UiImageVector = HeroIcons.Solid20Mini.sparkles
    var cube: UiImageVector = HeroIcons.Solid20Mini.cube
    var cog6Tooth: UiImageVector = HeroIcons.Solid20Mini.cog6Tooth
    var arrowUpTray: UiImageVector = HeroIcons.Solid20Mini.arrowUpTray
    var arrowDownTray: UiImageVector = HeroIcons.Solid20Mini.arrowDownTray
    var puzzlePiece: UiImageVector = HeroIcons.Solid20Mini.puzzlePiece
    var pencilSquare: UiImageVector = HeroIcons.Solid20Mini.pencilSquare
    var play: UiImageVector = HeroIcons.Solid20Mini.play
    var magnifyingGlass: UiImageVector = HeroIcons.Solid20Mini.magnifyingGlass
    var videoCamera: UiImageVector = HeroIcons.Solid20Mini.videoCamera
}
