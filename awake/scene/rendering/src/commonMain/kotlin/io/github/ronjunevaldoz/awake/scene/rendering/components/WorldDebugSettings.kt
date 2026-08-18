// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.rendering.components

/** Singleton toggle set for [io.github.ronjunevaldoz.awake.scene.rendering.systems
 * .DebugVisualizationSystem] -- add at most one to a `World` (an editor/debug entity), same
 * "system reads whatever's currently set" shape [PbrMaterial]'s `var` fields already use, so a
 * UI panel (a checkbox per field) can drive these live. All `false` by default: adding this
 * component with no changes draws nothing extra. */
data class WorldDebugSettings(
    var showFrustum: Boolean = false,
    var showBounds: Boolean = false,
    var showOcclusion: Boolean = false,
    var showLights: Boolean = false,
    var showShadowFrustum: Boolean = false,
    /** Which entity's [io.github.ronjunevaldoz.awake.scene.rendering.components.Camera]
     * [showFrustum] draws -- deliberately NOT the world's own primary/viewing camera: drawing
     * that camera's frustum from inside its own view is geometrically invisible (the near
     * plane sits behind the eye, the far/side planes align with the screen edges). An editor
     * sets this to whatever entity is selected each frame; `null` (or an entity with no
     * `Camera`) draws nothing, same "opt-in, no target = no lines" posture every other field
     * here already has. */
    var frustumTargetEntityId: Int? = null,
)
