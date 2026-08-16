// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.ui

import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.core.components.Name
import io.github.ronjunevaldoz.awake.testing.ui.uiTestSession
import io.github.ronjunevaldoz.awake.ui.designsystem.shadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.shadcnThemeValues
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class HierarchyPanelTest {
    @Test
    fun clickingAnEntityRowDispatchesItsEntityId() {
        val world = World()
        val camera = world.create()
        world.add(camera, Name("Camera"))
        var selected: Int? = null

        uiTestSession(width = 280f, height = 400f, font = BitmapFont()) {
            fun draw(x: Float, y: Float, down: Boolean = false) = frame(x = x, y = y, down = down) {
                shadcnTheme(theme = shadcnThemeValues(dark = true)) {
                    drawHierarchyPanel(world, selectedEntityId = selected) { selected = it }
                }
            }

            val first = draw(-1f, -1f)
            val row = assertNotNull(first.semantics.firstOrNull { it.id == "studio-hierarchy-entity-${camera.id}" })
            val x = row.bounds.x + row.bounds.width / 2f
            val y = row.bounds.y + row.bounds.height / 2f
            draw(x, y, down = true)
            draw(x, y, down = false)
        }

        assertEquals(camera.id, selected)
    }
}
