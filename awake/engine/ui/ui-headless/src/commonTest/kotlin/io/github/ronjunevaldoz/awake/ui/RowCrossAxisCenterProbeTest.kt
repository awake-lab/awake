// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.headless.input.text.text
import kotlin.test.Test
import kotlin.test.assertEquals

class RowCrossAxisCenterProbeTest {
    @Test
    fun rowCentersChildrenOfDifferentHeightsAtSameMidpoint() {
        val ui = UiContext()
        ui.beginFrame(300f, 100f, testSnapshot())
        var shortBounds: UiBounds? = null
        var tallBounds: UiBounds? = null
        ui.createColumn(x = 0f, y = 0f, width = 300f).row(
            horizontalArrangement = Arrangement.spacedBy(8f.dp),
            verticalAlignment = UiAlignment.Vertical.Center,
            modifier = Modifier.width(Dimension.FillMax).height(Dimension.Fixed(32f.dp))
        ) {
            shortBounds = text("+", modifier = Modifier.width(12f.dp).height(6f.dp), verticallyCentered = true)
            tallBounds = text("Header", modifier = Modifier.width(60f.dp).height(13f.dp), verticallyCentered = true)
        }
        ui.endFrame()
        val short = shortBounds!!
        val tall = tallBounds!!
        val shortMid = short.y + short.height / 2f
        val tallMid = tall.y + tall.height / 2f
        println("short=$short mid=$shortMid")
        println("tall=$tall mid=$tallMid")
        assertEquals(tallMid, shortMid, absoluteTolerance = 0.01f, message = "row(verticalAlignment=Center) should center children of different heights at the same midpoint")
    }
}
