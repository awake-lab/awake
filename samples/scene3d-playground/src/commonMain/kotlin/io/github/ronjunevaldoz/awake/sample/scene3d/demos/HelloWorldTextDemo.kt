// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d.demos

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.core.math.Camera as CoreCamera
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.ecs.Entity
import io.github.ronjunevaldoz.awake.sample.scene3d.Scene3DDemo
import io.github.ronjunevaldoz.awake.scene.components.Camera as SceneCamera
import io.github.ronjunevaldoz.awake.ui.designsystem.components.input.shadcnFieldSliderWithValue
import io.github.ronjunevaldoz.awake.ui.designsystem.components.selection.shadcnSwitch
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSurface
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.box
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxHeight
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.sp
import io.github.ronjunevaldoz.awake.ui.theme.TextStyle
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text

/** Simplest possible playground entry -- proves the menu/viewport/controls wiring with plain
 * 2D text instead of any real 3D content. Every other demo in this package follows this same
 * shape: local mutable state up top, [renderViewport] reads it, [renderControls] writes it.
 *
 * Still spawns a trivial primary [SceneCamera] entity on activate (despite having no mesh
 * content) -- [io.github.ronjunevaldoz.awake.scene.systems.RenderSystem] only calls
 * `renderer.draw()` when a primary camera exists in the world; without one, the swapchain frame
 * never gets its color pass at all this frame, and the subsequent `drawUi()` 2D pass paints onto
 * whatever the OS left behind -- a blank pale-gray window, not a crash (same failure mode
 * `GameUiRuntime.render`'s own doc comment describes for its `EMPTY_UI_ONLY_CAMERA` fallback,
 * which this mirrors). */
internal object HelloWorldTextDemo {
    private var fontSizeDp = 28f
    private var centered = true
    private var cameraEntity: Entity? = null

    val entry = Scene3DDemo(
        id = "hello-world-text",
        title = "Hello world text",
        renderViewport = {
            // The text's own modifier only sizes/centers it HORIZONTALLY within this row --
            // without an outer box, it always sat at the column's top edge instead of the
            // viewport's vertical center (the reported bug). Centering the whole
            // fillMaxWidth-sized text block inside a fillMaxHeight box fixes the vertical axis
            // without touching the existing `centered` switch's horizontal behavior.
            box(modifier = Modifier.fillMaxWidth().fillMaxHeight(), contentAlignment = UiAlignment.Center) {
                text(
                    label = "Hello, Awake!",
                    modifier = Modifier.fillMaxWidth().height((fontSizeDp * 1.4f).dp),
                    color = Color(0.15f, 0.13f, 0.36f, 1f),
                    centered = centered,
                    // The modifier's height only sizes the slot text is centered within -- it
                    // never scaled the glyphs themselves, so dragging the slider just moved
                    // "Hello, Awake!" up/down inside a taller/shorter box instead of actually
                    // growing/shrinking it (a separate, earlier reported bug). Real size comes
                    // from textStyle.size; height above is scaled 1.4x that so there's still
                    // headroom to vertically center a tall font size.
                    textStyle = TextStyle(size = fontSizeDp.sp)
                )
            }
        },
        renderControls = {
            shadcnSurface(id = "hello-world-controls-panel", modifier = Modifier.fillMaxWidth()) {
                fontSizeDp = shadcnFieldSliderWithValue(
                    id = "hello-world-font-size",
                    label = "Font size",
                    min = 12f,
                    max = 64f,
                    value = fontSizeDp
                )
                centered = shadcnSwitch(
                    id = "hello-world-centered",
                    checked = centered,
                    label = "Centered"
                )
            }
        },
        onActivate = {
            val entity = world.create()
            world.add(entity, SceneCamera(EMPTY_CAMERA, isPrimary = true))
            cameraEntity = entity
        },
        onDeactivate = { world ->
            cameraEntity?.let { world.destroy(it) }
            cameraEntity = null
        }
    )

    // Same values as GameUiRuntime.EMPTY_UI_ONLY_CAMERA -- unused for any real 3D transform,
    // exists only so RenderSystem finds a primary camera and actually calls renderer.draw()
    // this frame (see the class doc comment above).
    private val EMPTY_CAMERA = CoreCamera(
        eye = Vec3(0f, 0f, 1f),
        center = Vec3(0f, 0f, 0f),
        fovYRadians = 1f,
        near = 0.1f,
        far = 10f
    )
}
