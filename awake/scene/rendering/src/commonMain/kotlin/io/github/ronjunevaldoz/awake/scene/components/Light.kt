// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.components

import io.github.ronjunevaldoz.awake.core.math.Vec3

data class Light(
    val color: Vec3 = Vec3(1f, 1f, 1f),
    val intensity: Float = 1f,
    val type: Type = Type.Point
) {
    enum class Type {
        Directional,
        Point
    }
}
