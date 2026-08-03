// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d.demos

import io.github.ronjunevaldoz.awake.sample.scene3d.Scene3DDemo
import io.github.ronjunevaldoz.awake.ui.designsystem.components.controls.shadcnSlider
import io.github.ronjunevaldoz.awake.ui.designsystem.components.selection.shadcnSwitch
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text

/** Scaffold only -- proves the menu/controls wiring, does not yet drive a real mesh/camera.
 * Wiring a real rotating cube here means routing this playground's viewport region into a
 * [io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime] 3D pass (camera + `meshEntity`
 * + an `orbitCameraSystem` this demo's own `orbit`/`zoom` state drives) -- deliberately not done
 * in this scaffolding pass; see this file as the placeholder to fill in next. */
internal object RotatingCubeDemo {
    private var orbitDegrees = 35f
    private var zoom = 6f
    private var wireframe = false

    val entry = Scene3DDemo(
        id = "rotating-cube",
        title = "Rotating cube",
        renderViewport = {
            text(label = "Rotating cube viewport -- not wired yet")
        },
        renderControls = {
            orbitDegrees = shadcnSlider(id = "cube-orbit", min = 0f, max = 360f, value = orbitDegrees, label = "Orbit")
            zoom = shadcnSlider(id = "cube-zoom", min = 1f, max = 10f, value = zoom, label = "Zoom")
            wireframe = shadcnSwitch(id = "cube-wireframe", checked = wireframe, label = "Wireframe")
        }
    )
}
