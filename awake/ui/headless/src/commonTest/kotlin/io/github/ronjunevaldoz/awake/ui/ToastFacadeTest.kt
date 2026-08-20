// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.testing.ui.renderUiComponent
import io.github.ronjunevaldoz.awake.testing.ui.uiTestSession
import io.github.ronjunevaldoz.awake.ui.headless.toast
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Facade-level smoke coverage for [io.github.ronjunevaldoz.awake.ui.headless.toast] driven only
 * through the public `UiScope` API -- no `headless.internal.*` import and no
 * `primitive.context.createAbsolute` reach-through, unlike `ToastTest`.
 */
class ToastFacadeTest {

    @Test
    fun rendersASemanticNodeWithTheMessageWhileVisible() {
        val frame = renderUiComponent(width = 300f, height = 100f) {
            val visible = toast(id = "toast.smoke", message = "Saved")
            assertTrue(visible, "a freshly-shown toast must be visible")
        }

        val node = frame.node("toast.smoke")
        assertEquals("Saved", node.label)
    }

    @Test
    fun returnsFalseOnceItsDurationHasElapsed() {
        var lastVisible = true

        // 5 frames at 100ms each = 500ms elapsed, past a 300ms duration.
        uiTestSession(width = 300f, height = 100f) {
            repeat(5) {
                frame(deltaSeconds = 0.1f) {
                    lastVisible = toast(id = "toast.smoke", message = "Saved", durationMs = 300f)
                }
            }
        }

        assertFalse(lastVisible, "toast should have expired after its 300ms duration elapsed")
    }
}
