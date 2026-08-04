// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.engine.application.GameUiRuntime
import io.github.ronjunevaldoz.awake.engine.application.frame
import io.github.ronjunevaldoz.awake.engine.application.frameStats
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebar
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarMenu
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarMenuItem
import io.github.ronjunevaldoz.awake.ui.designsystem.shadcnTheme
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.box
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.offset
import io.github.ronjunevaldoz.awake.ui.modifier.verticalScroll
import io.github.ronjunevaldoz.awake.ui.modifier.weight
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.rememberScrollState
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text

/** Same light theme [Scene3DPlaygroundFeature.kt] declares via `theme(shadcnTheme(dark =
 * false))` -- that DSL call only sets [GameUiRuntime.theme], an inert property nothing in
 * rendering actually reads. The real per-frame ambient theme lives on [GameUiRuntime.uiContext]'s
 * own theme stack (defaults to [io.github.ronjunevaldoz.awake.ui.theme.UiDefaultTheme]) and has
 * to be pushed explicitly every frame -- the same [io.github.ronjunevaldoz.awake.ui.context.UiContext.pushTheme]
 * call `UiShowcaseUi.kt`'s `drawUiShowcaseOverlay` already makes; without it, this shell
 * silently rendered in the library's dark default no matter what `theme(...)` was told. */
private val PlaygroundTheme = shadcnTheme(dark = false)

/** Three-column playground shell: demo menu (left) | live viewport (center) | per-demo controls
 * (right). Only lays out chrome and delegates to whichever [Scene3DDemo] is active for both the
 * center and right panes -- see [Scene3DDemos]'s doc comment for how to add a new demo. */
internal fun GameUiRuntime.drawScene3DPlaygroundOverlay(state: Scene3DPlaygroundState) {
    val runtime = this
    uiContext.pushTheme(PlaygroundTheme)
    frame {
        row(
            id = "scene3d-playground-shell",
            horizontalArrangement = Arrangement.spacedBy(0f.dp),
            modifier = Modifier.width(Dimension.FillMax).height(Dimension.FillMax)
        ) {
            shadcnSidebar(
                id = "scene3d-demo-menu",
                modifier = Modifier.width(200f.dp).height(Dimension.FillMax)
            ) {
                shadcnSidebarMenu {
                    Scene3DDemos.forEach { demo ->
                        shadcnSidebarMenuItem(
                            id = "scene3d-menu-${demo.id}",
                            label = demo.title,
                            active = state.activeDemoId == demo.id,
                            onClick = { state.activeDemoId = demo.id }
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
            box(modifier = Modifier.weight(1f).height(Dimension.FillMax)) {
                column(
                    id = "scene3d-viewport",
                    modifier = Modifier.width(Dimension.FillMax).height(Dimension.FillMax)
                ) {
                    activeDemo.renderViewport(this)
                }
                val stats = runtime.frameStats()
                text(
                    label = "${stats.frameTimeMs}ms  ${stats.fps.toInt()} fps",
                    modifier = Modifier.offset(8f.dp, 8f.dp),
                    color = Color(0.3f, 1f, 0.4f, 1f),
                    textStyle = null
                )
            }

            val controlsScroll = runtime.uiContext.rememberScrollState("scene3d-controls-scroll")
            column(
                id = "scene3d-controls-column",
                verticalArrangement = Arrangement.spacedBy(16f.dp),
                modifier = Modifier.width(220f.dp).height(Dimension.FillMax).verticalScroll(controlsScroll)
            ) {
                activeDemo.renderControls(this)
            }
        }
    }
}
