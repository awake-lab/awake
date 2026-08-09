// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.core.components

import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.ecs.Entity
import io.github.ronjunevaldoz.awake.ecs.Poolable

data class Transform(
    var position: Vec3 = Vec3(0f, 0f, 0f),
    var rotation: Vec3 = Vec3(0f, 0f, 0f),
    var scale: Vec3 = Vec3(1f, 1f, 1f),
    var parent: Entity? = null,
    var worldMatrix: Mat4 = Mat4(),
) : Poolable {
    fun localMatrix(): Mat4 = Mat4()
        .translate(position.x, position.y, position.z)
        .rotateZ(rotation.z)
        .rotateY(rotation.y)
        .rotateX(rotation.x)
        .scale(scale.x, scale.y, scale.z)

    override fun reset() {
        position.x = 0f
        position.y = 0f
        position.z = 0f
        rotation.x = 0f
        rotation.y = 0f
        rotation.z = 0f
        scale.x = 1f
        scale.y = 1f
        scale.z = 1f
        parent = null
        worldMatrix.identity()
    }
}
