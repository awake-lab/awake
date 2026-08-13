// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.ui

import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.core.components.Name
import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.shadcnTheme
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.headless.createUiScope
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class HierarchyPanelTest {
    @Test
    fun clickingAnEntityRowDispatchesItsEntityId() {
        val world = World()
        val camera = world.create()
        world.add(camera, Name("Camera"))
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(shadcnTheme(dark = true))
        var selected: Int? = null

        fun draw(input: UiInputState) = ui.apply {
            beginFrame(280f, 400f, input)
            createUiScope(UiBounds(0f, 0f, 280f, 400f))
                .drawHierarchyPanel(world, selectedEntityId = selected) { selected = it }
        }.finishFrame()

        val first = draw(UiInputState(pointerX = -1f, pointerY = -1f))
        val row = assertNotNull(first.semantics.firstOrNull { it.id == "studio-hierarchy-entity-${camera.id}" })
        val x = row.bounds.x + row.bounds.width / 2f
        val y = row.bounds.y + row.bounds.height / 2f
        draw(UiInputState(pointerX = x, pointerY = y, pointerDown = true))
        draw(UiInputState(pointerX = x, pointerY = y, pointerDown = false))

        assertEquals(camera.id, selected)
    }
}
