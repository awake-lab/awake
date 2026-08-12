// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.ui.designsystem.components.ShadcnIcons
import io.github.ronjunevaldoz.awake.ui.unstyled.HeroIcons

/**
 * Binds [HeroIcons] (in ui-headless) to the [ShadcnIcons] registry (in ui-designsystem).
 *
 * This avoids a circular dependency while allowing design-system recipes to use the default
 * Heroicons set. Call this once at app startup before any UI frame is rendered.
 */
fun initializeShadcnIcons() {
    val mini = HeroIcons.Solid20Mini
    ShadcnIcons.apply {
        chevronDown = mini.chevronDown
        chevronUp = mini.chevronUp
        chevronLeft = mini.chevronLeft
        chevronRight = mini.chevronRight
        squares2x2 = mini.squares2x2
        xMark = mini.xMark
        sparkles = mini.sparkles
        cube = mini.cube
        cog6Tooth = mini.cog6Tooth
        arrowUpTray = mini.arrowUpTray
        arrowDownTray = mini.arrowDownTray
        puzzlePiece = mini.puzzlePiece
        pencilSquare = mini.pencilSquare
        play = mini.play
        magnifyingGlass = mini.magnifyingGlass
        videoCamera = mini.videoCamera
        eye = mini.eye
        eyeSlash = mini.eyeSlash
        sun = mini.sun
        user = mini.user
        documentText = mini.documentText
        plus = mini.plus
        trash = mini.trash
        lockClosed = mini.lockClosed
        bars3 = mini.bars3
        funnel = mini.funnel
        adjustmentsHorizontal = mini.adjustmentsHorizontal
        stop = mini.stop
        pause = mini.pause
    }
}
