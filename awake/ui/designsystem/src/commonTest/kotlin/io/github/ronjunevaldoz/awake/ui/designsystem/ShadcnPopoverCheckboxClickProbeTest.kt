// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.testing.ui.UiComponentFrame
import io.github.ronjunevaldoz.awake.testing.ui.UiTestSession
import io.github.ronjunevaldoz.awake.testing.ui.uiTestSession
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCheckbox
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnPopover
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.headless.uiScope
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A checkbox nested in a popover nested in a *wrap-content* (no explicit width) trigger button
 * -- `samples/studio/.../StudioPills.kt`'s "Debug" menu, minus the workaround it took (an
 * explicit `Modifier.width(64f.dp)` on the trigger). Root cause: `UiContext.measuring` is
 * declared but never toggled true during a trial measurement pass, so a wrap-content trigger's
 * content lambda -- which runs 2-3 times per frame to resolve its intrinsic width -- processes
 * every trial invocation as a real click. On release, the first (trial) invocation clears the
 * single global `activeId` before the real draw-pass invocation ever sees an active claim to
 * release, so the click is silently swallowed. Every other popover-with-a-trigger call site
 * already routes around this with an explicit trigger width; this probe pins the underlying bug
 * directly instead of trusting the workaround to keep being copied correctly.
 */
class ShadcnPopoverCheckboxClickProbeTest {

    private var checked: Boolean = false
    private var expanded: Boolean = true
    private var checkboxInvocations: Int = 0

    private fun buildFrame(
        session: UiTestSession,
        x: Float,
        y: Float,
        down: Boolean,
    ): UiComponentFrame = session.frame(x = x, y = y, down = down) {
        // No explicit width: the trigger must wrap-content-size around its label, which is
        // exactly the shape that runs its content lambda through multiple trial measure passes.
        shadcnButton(
            id = "popover-probe-trigger",
        ) { trigger ->
            uiScope().shadcnPopover(
                id = "popover-probe-popover",
                anchorSlot = trigger,
                expanded = expanded,
                width = Dimension.Fixed(160f.dp),
            ) {
                checkboxInvocations++
                checked = shadcnCheckbox(
                    id = "popover-probe-checkbox",
                    checked = checked,
                    label = "Frustum",
                )
            }
        }
    }

    @Test
    fun checkboxInsideAWrapContentTriggerPopoverTogglesOnClick() {
        checked = false
        expanded = true
        uiTestSession(
            width = 400f,
            height = 300f,
            font = BitmapFont(),
            rootProvider = { content -> shadcnTheme { content() } },
        ) {
            var frame = buildFrame(this, x = -100f, y = -100f, down = false)
            val target = frame.node("popover-probe-checkbox").bounds
            val px = target.x + target.width / 2f
            val py = target.y + target.height / 2f

            checkboxInvocations = 0
            buildFrame(this, px, py, down = true)
            println("press frame invocations: $checkboxInvocations")
            checkboxInvocations = 0
            frame = buildFrame(this, px, py, down = false)
            println("release frame invocations: $checkboxInvocations")

            assertEquals(true, checked, "clicking the popover checkbox should check it")
        }
    }
}
