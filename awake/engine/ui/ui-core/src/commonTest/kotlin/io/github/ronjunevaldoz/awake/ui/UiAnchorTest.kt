// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import kotlin.test.Test
import kotlin.test.assertEquals

class UiAnchorTest {
//
//    @Test
//    fun anchoredPlacesSlotAgainstRequestedCorner() {
//        val bounds = UiSlot(0f, 0f, 320f, 240f)
//
//        assertEquals(
//            UiSlot(228f, 10f, 80f, 8f),
//            bounds.anchored(
//                anchor = UiAnchor.TopRight,
//                width = 80f,
//                height = 8f,
//                margin = UiInsets(start = 4f.dp, top = 10f.dp, end = 12f.dp, bottom = 4f.dp)
//            )
//        )
//        assertEquals(
//            UiSlot(14f, 216f, 80f, 8f),
//            bounds.anchored(
//                anchor = UiAnchor.BottomLeft,
//                width = 80f,
//                height = 8f,
//                margin = UiInsets(start = 14f.dp, top = 4f.dp, end = 4f.dp, bottom = 16f.dp)
//            )
//        )
//    }

    @Test
    fun textBlockHeightUsesFontCellSizeAndGap() {
        val font = BitmapFont()

        assertEquals(0f, font.textBlockHeight(lineCount = 0))
        assertEquals(24f, font.textBlockHeight(lineCount = 1, textScale = 2f))
        assertEquals(80f, font.textBlockHeight(lineCount = 3, textScale = 2f, gap = 4f))
    }
}
