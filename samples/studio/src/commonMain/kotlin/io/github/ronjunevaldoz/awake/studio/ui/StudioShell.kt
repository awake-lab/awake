// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.ui

import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime
import io.github.ronjunevaldoz.awake.scene.runtime.frame
import io.github.ronjunevaldoz.awake.studio.state.StudioContract
import io.github.ronjunevaldoz.awake.studio.state.StudioStore
import io.github.ronjunevaldoz.awake.ui.designsystem.shadcnTheme
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.padding
import io.github.ronjunevaldoz.awake.ui.modifier.weight
import io.github.ronjunevaldoz.awake.ui.modifier.width

private val StudioTheme = shadcnTheme(dark = false)

// Dark neutral gray, not pure black -- Renderer.clearColor otherwise defaults to stark black,
// which reads as "nothing rendered" rather than a real viewport background. Same reasoning
// scene3d-playground's own VIEWPORT_CLEAR_COLOR documents.
@Suppress("MagicNumber") // The literals define the constant; naming each channel adds nothing.
private val ViewportClearColor = floatArrayOf(0.14f, 0.14f, 0.16f, 1f)

internal fun SceneGameRuntime.drawStudioShell(store: StudioStore, viewportWidth: Float, viewportHeight: Float) {
    renderer.clearColor = ViewportClearColor
    uiContext.pushTheme(StudioTheme)
    frame(viewportWidth, viewportHeight) {
        row(
            id = "studio-shell",
            horizontalArrangement = Arrangement.spacedBy(0f.dp),
            modifier = Modifier.width(Dimension.FillMax).height(Dimension.FillMax).padding(8.dp),
        ) {
            drawIconRail(
                activeTool = store.state.value.toolRail.activeTool,
                onSelectTool = { store.dispatch(StudioContract.Intent.SelectTool(it)) },
                // Re-selecting the active example queues LoadExample, which tears the scene
                // down and re-instantiates it -- reset without a dedicated intent.
                onResetExample = { store.dispatch(StudioContract.Intent.SelectExample(store.state.value.examples.activeExampleId)) },
            )
            drawExampleRail(
                activeExampleId = store.state.value.examples.activeExampleId,
                onSelectExample = { store.dispatch(StudioContract.Intent.SelectExample(it)) },
            )
            column(
                id = "studio-viewport-column",
                verticalArrangement = Arrangement.spacedBy(8f.dp),
                modifier = Modifier.weight(1f).height(Dimension.FillMax).padding(8f.dp),
            ) {
                drawStudioToolbar(renderer)
            }
            drawInspectorPanel(world)
        }
    }
}
