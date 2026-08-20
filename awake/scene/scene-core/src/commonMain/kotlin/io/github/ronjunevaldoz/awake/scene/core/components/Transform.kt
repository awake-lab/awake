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
    /**
     * Computes the local transform matrix into [target] with zero allocations.
     *
     * @param target The target [Mat4] matrix to write the computed local transformation into.
     * @return The [target] matrix containing the computed local transformation.
     */
    fun computeLocalMatrix(target: Mat4 = worldMatrix): Mat4 {
        target.setEulerTRS(
            position.x, position.y, position.z,
            rotation.x, rotation.y, rotation.z,
            scale.x, scale.y, scale.z,
        )
        return target
    }

    /**
     * Computes and returns a newly allocated local matrix.
     *
     * Prefer [computeLocalMatrix] in hot loops to avoid per-frame allocations.
     *
     * @return A fresh [Mat4] instance containing this transform's local matrix.
     */
    fun localMatrix(): Mat4 = computeLocalMatrix(Mat4())

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
