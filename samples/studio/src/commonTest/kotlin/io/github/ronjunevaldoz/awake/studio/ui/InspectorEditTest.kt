// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.ui

import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.ecs.ensure
import io.github.ronjunevaldoz.awake.scene.core.components.Name
import io.github.ronjunevaldoz.awake.scene.core.components.Transform
import io.github.ronjunevaldoz.awake.testing.ui.uiTestSession
import io.github.ronjunevaldoz.awake.ui.designsystem.shadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.shadcnThemeValues
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class InspectorEditTest {

    @Test
    fun typingIntoAnAxisFieldWritesTheValueIntoTheLiveTransform() {
        val world = World()
        val entity = world.create()
        world.add(entity, Name("Cube"))
        val transform = world.ensure(entity, ::Transform)

        uiTestSession(width = 320f, height = 600f, font = BitmapFont()) {
            fun draw(x: Float, y: Float, down: Boolean = false) = frame(x = x, y = y, down = down) {
                shadcnTheme(theme = shadcnThemeValues(dark = true)) {
                    drawInspectorPanel(world, selectedEntityId = entity.id)
                }
            }

            // The transform rows live inside the entity's collapsible, collapsed by default.
            val header = assertNotNull(
                draw(-1f, -1f).semantics.firstOrNull { it.id == "studio-inspector-row-${entity.id}.trigger" },
                "entity section must render",
            )
            val headerX = header.bounds.x + header.bounds.width / 2f
            val headerY = header.bounds.y + header.bounds.height / 2f
            draw(headerX, headerY, down = true)
            draw(headerX, headerY, down = false)

            val field = assertNotNull(
                draw(-1f, -1f).semantics.firstOrNull { it.id == "studio-inspector-${entity.id}-position-x" },
                "position X field must render once the section is expanded",
            )
            val fieldX = field.bounds.x + field.bounds.width / 2f
            val fieldY = field.bounds.y + field.bounds.height / 2f
            draw(fieldX, fieldY, down = true)
            draw(fieldX, fieldY, down = false)
            input.pushTypedText("5")
            draw(fieldX, fieldY)
        }

        assertTrue(
            transform.position.x != 0f,
            "typing into the X field must reach the live Transform, was ${transform.position.x}",
        )
        assertEquals(0f, transform.position.y, "untouched axes must not change")
    }

    /** A system owning the same value (a spin control, the camera) must win over a stale field:
     * otherwise the inspector writes its old number back every frame and the animation freezes. */
    @Test
    fun anExternalChangeRefreshesTheFieldInsteadOfBeingOverwritten() {
        val world = World()
        val entity = world.create()
        world.add(entity, Name("Cube"))
        val transform = world.ensure(entity, ::Transform)

        uiTestSession(width = 320f, height = 600f, font = BitmapFont()) {
            fun draw() = frame(x = -1f, y = -1f, down = false) {
                shadcnTheme(theme = shadcnThemeValues(dark = true)) {
                    drawInspectorPanel(world, selectedEntityId = entity.id)
                }
            }
            draw()
            transform.position.x = 3f
            draw()
            draw()
        }

        assertEquals(3f, transform.position.x, "the inspector must not write a stale value back")
    }
}
