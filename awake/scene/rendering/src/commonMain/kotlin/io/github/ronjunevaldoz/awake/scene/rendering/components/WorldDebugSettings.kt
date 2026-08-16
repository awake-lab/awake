// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.rendering.components

/** Singleton toggle set for [io.github.ronjunevaldoz.awake.scene.rendering.systems
 * .DebugVisualizationSystem] -- add at most one to a `World` (an editor/debug entity), same
 * "system reads whatever's currently set" shape [PbrMaterial]'s `var` fields already use, so a
 * UI panel (a checkbox per field) can drive these live. Both `false` by default: adding this
 * component with no changes draws nothing extra. */
data class WorldDebugSettings(
    var showFrustum: Boolean = false,
    var showBounds: Boolean = false,
)
