// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d.demos

import io.github.ronjunevaldoz.awake.core.math.OrbitCameraController
import io.github.ronjunevaldoz.awake.ui.designsystem.components.input.shadcnFieldSliderWithValue
import io.github.ronjunevaldoz.awake.ui.designsystem.components.selection.shadcnSwitch
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCollapsibleCard
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text

/**
 * Reusable default camera control -- every demo with a 3D viewport gets this same Camera/
 * Projection panel via [renderOrbitCameraControls] instead of hand-rolling its own (previously
 * [RotatingCubeDemo] had the full set and `GltfViewerDemo` had a smaller ad-hoc one; a demo now
 * only adds controls for what's actually specific to it -- e.g. glTF viewer's own "Auto-rotate"
 * switch -- on top of this shared panel, not a second camera implementation). The camera math
 * itself ([OrbitCameraController]) is engine-level (`awake:base`'s `core.math`), UI-free -- this
 * file only binds it to shadcn controls, which is why it stays here rather than moving with it.
 */

/** Renders [controller]'s Camera + Projection groups -- [idPrefix] keeps this demo's slider ids
 * distinct from any other demo's copy of the same panel; [targetLabel] names whatever
 * [OrbitCameraController.lockTargetToPoint]'s switch is locking onto (e.g. "cube", "model"). */
internal fun ColumnScope.renderOrbitCameraControls(
    controller: OrbitCameraController,
    idPrefix: String,
    targetLabel: String
) {
    shadcnCollapsibleCard(
        id = "$idPrefix-controls-camera",
        expanded = controller.cameraGroupExpanded,
        onExpandedChange = { controller.cameraGroupExpanded = it },
        header = { text("Camera", verticallyCentered = true) }
    ) {
        controller.orbitDegrees = shadcnFieldSliderWithValue(id = "$idPrefix-orbit", label = "Orbit", min = 0f, max = 360f, value = controller.orbitDegrees)
        controller.pitchDegrees = shadcnFieldSliderWithValue(id = "$idPrefix-pitch", label = "Pitch", min = 0f, max = 89f, value = controller.pitchDegrees)
        controller.rollDegrees = shadcnFieldSliderWithValue(id = "$idPrefix-roll", label = "Roll", min = -180f, max = 180f, value = controller.rollDegrees)
        controller.freeLook = shadcnSwitch(id = "$idPrefix-free-look", checked = controller.freeLook, label = "Free look")
        controller.zoom = shadcnFieldSliderWithValue(id = "$idPrefix-zoom", label = "Zoom", min = controller.zoomMin, max = controller.zoomMax, value = controller.zoom, enabled = !controller.freeLook)
        controller.lockTargetToPoint = shadcnSwitch(id = "$idPrefix-lock-target", checked = controller.lockTargetToPoint, label = "Look at $targetLabel", enabled = !controller.freeLook)
        // Meaningless combined with lockTargetToPoint only in orbit mode (the target is forced
        // to the caller's target position, these fields have nothing to affect); in freeLook
        // they're always live since they're the *eye* position there, not a look-at target.
        val targetFieldsEnabled = controller.freeLook || !controller.lockTargetToPoint
        controller.panX = shadcnFieldSliderWithValue(id = "$idPrefix-pan-x", label = "Pan X", min = -controller.panRange, max = controller.panRange, value = controller.panX, enabled = targetFieldsEnabled)
        controller.panZ = shadcnFieldSliderWithValue(id = "$idPrefix-pan-z", label = "Pan Z", min = -controller.panRange, max = controller.panRange, value = controller.panZ, enabled = targetFieldsEnabled)
        controller.elevation = shadcnFieldSliderWithValue(id = "$idPrefix-elevation", label = "Elevation", min = -controller.panRange, max = controller.panRange, value = controller.elevation, enabled = targetFieldsEnabled)
    }
    shadcnCollapsibleCard(
        id = "$idPrefix-controls-projection",
        expanded = controller.projectionGroupExpanded,
        onExpandedChange = { controller.projectionGroupExpanded = it },
        header = { text("Projection", verticallyCentered = true) }
    ) {
        controller.near = shadcnFieldSliderWithValue(id = "$idPrefix-near", label = "Near", min = 0.01f, max = 5f, value = controller.near)
        controller.far = shadcnFieldSliderWithValue(id = "$idPrefix-far", label = "Far", min = 10f, max = controller.zoomMax * 40f, value = controller.far)
        controller.fovDegrees = shadcnFieldSliderWithValue(id = "$idPrefix-fov", label = "FOV", min = 10f, max = 120f, value = controller.fovDegrees)
    }
}
