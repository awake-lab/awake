// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.components

import io.github.ronjunevaldoz.awake.core.math.Vec3

private const val DEFAULT_DIRECTION_X = 0.4f
private const val DEFAULT_DIRECTION_Y = 0.8f
private const val DEFAULT_DIRECTION_Z = 0.4f

/** [direction] only matters for [Type.Directional] -- [io.github.ronjunevaldoz.awake.scene
 * .systems.RenderSystem] reads it as a world-space direction the light shines FROM (matching
 * the shader-side `LIGHT_DIRECTION` convention every lit shader already used before this
 * component existed). [Type.Point] isn't implemented yet ([RenderSystem] treats every
 * [Light] entity as directional regardless of [type]) -- a real point light would need a
 * position, which means reading the light entity's own `Transform`, not this component alone;
 * left as a documented gap rather than a half-built field nobody reads. */
data class Light(
    val color: Vec3 = Vec3(1f, 1f, 1f),
    val intensity: Float = 1f,
    val type: Type = Type.Directional,
    val direction: Vec3 = Vec3(DEFAULT_DIRECTION_X, DEFAULT_DIRECTION_Y, DEFAULT_DIRECTION_Z),
) {
    enum class Type {
        Directional,
        Point,
    }
}
