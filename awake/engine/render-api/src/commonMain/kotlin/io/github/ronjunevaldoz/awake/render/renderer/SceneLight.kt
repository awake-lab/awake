// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.render.renderer

import io.github.ronjunevaldoz.awake.core.math.Vec3

/** Scene-wide directional light data for [Renderer.draw]'s single lit pass -- [direction] the
 * light shines FROM, [color] already intensity-multiplied (matching how every lit shader
 * consumes it: one multiply, not a separate intensity uniform). Backend-neutral, same shape
 * [io.github.ronjunevaldoz.awake.scene.components.Light] provides -- `RenderSystem` (which can
 * depend on this render-api module, unlike the reverse) is what actually builds one from a
 * scene's `Light` entity. */
data class SceneLight(
    val direction: Vec3,
    val color: Vec3
)
