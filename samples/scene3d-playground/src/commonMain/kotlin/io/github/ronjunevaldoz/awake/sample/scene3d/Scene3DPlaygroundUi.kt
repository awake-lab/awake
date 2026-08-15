// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d

import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime
import io.github.ronjunevaldoz.awake.scene.runtime.frameStats
import io.github.ronjunevaldoz.awake.scene.runtime.headlessFrame
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebar
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarMenu
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarMenuItem
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.ui.designsystem.shadcnThemeValues
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.box
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxHeight
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.headless.offset
import io.github.ronjunevaldoz.awake.ui.headless.padding
import io.github.ronjunevaldoz.awake.ui.headless.rememberScrollState
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.verticalScroll
import io.github.ronjunevaldoz.awake.ui.headless.weight
import io.github.ronjunevaldoz.awake.ui.headless.width

/** shadcn-compose's own library default is dark = true; every other Awake sample (ui-showcase)
 * explicitly opts into light instead of inheriting that default. The per-frame ambient theme
 * lives on [SceneGameRuntime.uiContext]'s own theme stack (defaults to
 * [io.github.ronjunevaldoz.awake.ui.theme.UiDefaultTheme]) and has to be pushed explicitly every
 * frame -- the same [io.github.ronjunevaldoz.awake.ui.context.UiContext.pushTheme] call
 * `UiShowcaseUi.kt`'s `drawUiShowcaseOverlay` already makes. */
private val PlaygroundTheme = shadcnThemeValues(dark = false)

/** Three-column playground shell: demo menu (left) | live viewport (center) | per-demo controls
 * (right). Only lays out chrome and delegates to whichever [Scene3DDemo] is active for both the
 * center and right panes -- see [Scene3DDemos]'s doc comment for how to add a new demo. */
internal fun SceneGameRuntime.drawScene3DPlaygroundOverlay(
    state: Scene3DPlaygroundState,
    viewportWidth: Float,
    viewportHeight: Float,
) {
    val runtime = this
    uiContext.pushTheme(PlaygroundTheme)
    headlessFrame(viewportWidth, viewportHeight) {
        row(
            horizontalArrangement = Arrangement.spacedBy(0f.dp),
            modifier = Modifier.fillMaxWidth().fillMaxHeight()
                .padding(8.dp),
        ) {
            shadcnSidebar(
                id = "scene3d-demo-menu",
                modifier = Modifier.width(200f.dp).fillMaxHeight(),
            ) {
                shadcnSidebarMenu {
                    Scene3DDemos.forEach { demo ->
                        shadcnSidebarMenuItem(
                            id = "scene3d-menu-${demo.id}",
                            label = demo.title,
                            active = state.activeDemoId == demo.id,
                            onClick = { state.activeDemoId = demo.id },
                        )
                    }
                }
            }

            val activeDemo = Scene3DDemos.first { it.id == state.activeDemoId }

            // box, not column, so the frame-stats badge below can overlay the top-left corner
            // of just this viewport pane -- the built-in GameUiRuntime.debugOverlayEnabled perf
            // HUD anchors to the whole window (via frame{}'s root-level box), which lands over
            // the sidebar instead of this pane when this shell isn't full-window. Reading
            // frameStats() directly here keeps the badge scoped to where it visually belongs.
            box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                column(
                    modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                ) {
                    activeDemo.renderViewport(runtime, this)
                }
                val stats = runtime.frameStats()
                shadcnText(
                    label = "${stats.frameTimeMs}ms  ${stats.fps.toInt()} fps",
                    modifier = Modifier.offset(8f.dp, 8f.dp),
                    muted = true,
                )
            }

            val controlsScroll = runtime.uiContext.rememberScrollState("scene3d-controls-scroll")
            column(
                verticalArrangement = Arrangement.spacedBy(16f.dp),
                modifier = Modifier.width(220f.dp).fillMaxHeight()
                    .padding(8.dp)
                    .verticalScroll(controlsScroll),
            ) {
                activeDemo.renderControls(runtime, this)
            }
        }
    }
}
