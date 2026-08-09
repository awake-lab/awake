// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.ui

import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.ShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebar
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarMenu
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarMenuItem
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.forceHover
import kotlin.test.Test

class ScratchProbeTest {
    @Test
    fun probeActiveBackground() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        ui.beginFrame(400f, 600f, UiInputState(pointerX = -100f, pointerY = -100f))
        ui.createAbsolute(slot = ui.frameBounds()).shadcnSidebar(id = "sidebar") {
            shadcnSidebarMenu {
                shadcnSidebarMenuItem(id = "item-active", label = "Active", active = true, modifier = Modifier.forceHover(true))
                shadcnSidebarMenuItem(id = "item-inactive", label = "Inactive", active = false)
            }
        }
        val out = ui.finishFrame()
        out.semantics.filter { it.id == "item-active" || it.id == "item-inactive" }.forEach {
            println("PROBE ${it.id} bg=${it.backgroundColor} fg=${it.foregroundColor}")
        }
    }
}
