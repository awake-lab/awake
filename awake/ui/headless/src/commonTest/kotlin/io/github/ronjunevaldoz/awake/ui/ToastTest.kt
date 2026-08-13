// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.headless.internal.controls.toast
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** [toast]'s whole contract is its return value's lifecycle across frames -- stays `true` while
 * under [toast]'s own `durationMs`, flips to `false`
 * once elapsed time crosses it, mirroring how a caller's own "active toasts" list would drop it. */
class ToastTest {

    @Test
    fun toastStaysVisibleUntilItsDurationElapses() {
        val ui = UiContext()

        // 10 frames at 100ms each = 1000ms elapsed, under the 3000ms default duration.
        repeat(10) {
            ui.beginFrame(200f, 100f, testSnapshot(), deltaSeconds = 0.1f)
            val visible = ui.createAbsolute(x = 20f, y = 20f).toast("t", "Saved")
            ui.endFrame()
            assertTrue(visible, "toast should still be visible at 1000ms of a 3000ms duration")
        }
    }

    @Test
    fun toastReturnsFalseOnceItsDurationHasElapsed() {
        val ui = UiContext()
        var lastVisible = true

        // 35 frames at 100ms each = 3500ms elapsed, past the 3000ms default duration.
        repeat(35) {
            ui.beginFrame(200f, 100f, testSnapshot(), deltaSeconds = 0.1f)
            lastVisible = ui.createAbsolute(x = 20f, y = 20f).toast("t", "Saved")
            ui.endFrame()
        }

        assertFalse(lastVisible, "toast should have expired after its 3000ms duration elapsed")
    }
}
