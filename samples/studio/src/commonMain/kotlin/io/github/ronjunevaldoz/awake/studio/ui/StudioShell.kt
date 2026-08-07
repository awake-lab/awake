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

internal fun SceneGameRuntime.drawStudioShell(store: StudioStore, viewportWidth: Float, viewportHeight: Float) {
    uiContext.pushTheme(StudioTheme)
    frame(viewportWidth, viewportHeight) {
        row(
            id = "studio-shell",
            horizontalArrangement = Arrangement.spacedBy(0f.dp),
            modifier = Modifier.width(Dimension.FillMax).height(Dimension.FillMax).padding(8.dp),
        ) {
            drawExampleRail(
                activeExampleId = store.state.value.examples.activeExampleId,
                onSelectExample = { store.dispatch(StudioContract.Intent.SelectExample(it)) },
            )
            column(
                id = "studio-viewport-column",
                modifier = Modifier.weight(1f).height(Dimension.FillMax),
            ) {
                drawStudioToolbar(renderer)
            }
            drawInspectorPanel(world)
        }
    }
}
