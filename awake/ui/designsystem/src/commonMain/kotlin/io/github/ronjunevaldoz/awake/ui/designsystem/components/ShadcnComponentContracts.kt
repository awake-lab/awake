// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.UiImageVector
import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.api.Sp
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.ShadcnTheme
import io.github.ronjunevaldoz.ui.heroicons.icon.HeroIcons

internal val SHADCN_CHECKBOX_CORNER_RADIUS = 4f.dp

/** Compatibility icon registry for the Headless and Design System recipes. */
object ShadcnIcons {
    // Initialized by Headless or Sample on startup to avoid ui-designsystem -> ui-headless
    // (HeroIcons) circular dependency.
    lateinit var chevronDown: UiImageVector
    lateinit var chevronUp: UiImageVector
    lateinit var chevronLeft: UiImageVector
    lateinit var chevronRight: UiImageVector
    lateinit var squares2x2: UiImageVector
    lateinit var xMark: UiImageVector
    lateinit var sparkles: UiImageVector
    lateinit var cube: UiImageVector
    lateinit var cog6Tooth: UiImageVector
    lateinit var arrowUpTray: UiImageVector
    lateinit var arrowDownTray: UiImageVector
    lateinit var puzzlePiece: UiImageVector
    lateinit var pencilSquare: UiImageVector
    lateinit var play: UiImageVector
    lateinit var magnifyingGlass: UiImageVector
    lateinit var videoCamera: UiImageVector
    lateinit var eye: UiImageVector
    lateinit var eyeSlash: UiImageVector
    lateinit var sun: UiImageVector
    lateinit var user: UiImageVector
    lateinit var documentText: UiImageVector
    lateinit var plus: UiImageVector
    lateinit var trash: UiImageVector
    lateinit var lockClosed: UiImageVector
    lateinit var bars3: UiImageVector
    lateinit var funnel: UiImageVector
    lateinit var adjustmentsHorizontal: UiImageVector
    lateinit var stop: UiImageVector
    lateinit var pause: UiImageVector

    init {
        initializeShadcnIcons()
    }

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

}

/**
 * Measurements from `new-york-v4/ui/radio-group.tsx` in the pinned shadcn-ui checkout.
 *
 * `RadioGroupItem` is `size-4`; its indicator icon is `size-2`; an authored label row is
 * `flex items-center gap-2`; and the group root is `grid gap-3`. Keep these named so a recipe
 * cannot silently turn a radio into a checkbox-shaped 24dp control again.
 */
internal object ShadcnRadioMetrics {
    val itemSize: Dp = 16f.dp
    val indicatorSize: Dp = 8f.dp
    val labelGap: Dp = 8f.dp
    val groupGap: Dp = 12f.dp
}

/**
 * Measurements from `new-york-v4/ui/sidebar.tsx` in the pinned shadcn-ui checkout.
 *
 * These are component-recipe values, not generic layout defaults. The showcase may choose its
 * own outer width and content, but a shadcn Sidebar's internal padding and menu rhythm must use
 * this one contract.
 */
internal object ShadcnSidebarMetrics {
    val contentPadding: Dp = 8f.dp
    val groupGap: Dp = 4f.dp
    val menuGap: Dp = 4f.dp
    val menuButtonHeight: Dp = 32f.dp
    val submenuIndent: Dp = 14f.dp
    val submenuGap: Dp = 2f.dp
    val submenuButtonHeight: Dp = 28f.dp
}

/** Branded avatar dimensions; the structure and drawing remain Headless-owned. */
enum class ShadcnAvatarSize(val boxSize: Dp, val textSize: Sp, val badgeSize: Dp) {
    Sm(24f.dp, ShadcnTheme.typography.caption, 8f.dp),
    Default(32f.dp, ShadcnTheme.typography.label, 10f.dp),
    Lg(40f.dp, ShadcnTheme.typography.body, 12f.dp),
}

/** Table column metadata is policy, while row/cell layout is Headless behavior. */
data class ShadcnTableColumn(
    val header: String,
    val weight: Float = 1f,
    val align: ShadcnTableCellAlign = ShadcnTableCellAlign.Start,
)

enum class ShadcnTableCellAlign { Start, End }
