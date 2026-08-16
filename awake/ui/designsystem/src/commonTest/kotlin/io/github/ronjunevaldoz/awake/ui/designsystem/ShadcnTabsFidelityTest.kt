// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.api.UiTabItem
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnTabs
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxSize
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * The tabs recipe's own geometry contract.
 *
 * This used to assert only that a node with each id existed, which every one of this repo's real
 * tab bugs would have passed: three triggers stacked at (0,0,0,0) all "exist". Existence is what
 * the showcase parity screenshot already covers better. What it does not cover is the arithmetic,
 * so that is what this asserts.
 */
class ShadcnTabsFidelityTest {

    @Test
    fun shadcnTabsRendersTrackAndTriggers() {
        val frame = renderShadcnComponent(width = 400f, height = 200f, font = BitmapFont()) {
            column(Modifier.fillMaxSize()) {
                shadcnTabs(
                    id = "tabs-fidelity",
                    items = listOf(UiTabItem("account", "Account"), UiTabItem("password", "Password")),
                    selected = "account",
                )
            }
        }
        val nodes = frame.semantics.associateBy { it.id }
        fun bounds(id: String) = requireNotNull(nodes[id]) {
            "no semantic node '$id'; ids present: ${nodes.keys.filterNotNull().sorted()}"
        }.bounds

        val track = bounds("tabs-fidelity.track")
        val account = bounds("tabs-fidelity.account")
        val password = bounds("tabs-fidelity.password")

        assertTrue(account.width > 0f && account.height > 0f, "account trigger is degenerate: $account")
        assertTrue(password.width > 0f && password.height > 0f, "password trigger is degenerate: $password")
        assertTrue(
            account.x + account.width <= password.x + 1f,
            "triggers must sit side by side, not overlap: account ends at " +
                "${account.x + account.width}, password starts at ${password.x}.",
        )
        assertTrue(
            account.y >= track.y - 1f && account.y + account.height <= track.y + track.height + 1f,
            "triggers must stay inside the track: account ${account.y}..${account.y + account.height} " +
                "in a track ${track.y}..${track.y + track.height}.",
        )
    }
}
