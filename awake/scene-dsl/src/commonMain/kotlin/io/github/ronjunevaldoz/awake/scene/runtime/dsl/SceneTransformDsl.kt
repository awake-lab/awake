// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime.dsl

import io.github.ronjunevaldoz.awake.scene.runtime.SceneTransform
import io.github.ronjunevaldoz.awake.scene.runtime.SceneVec3

@AwakeSceneDsl
class SceneTransformDsl internal constructor(
    source: SceneTransform
) {
    private var position: SceneVec3 = source.position
    private var rotation: SceneVec3 = source.rotation
    private var scale: SceneVec3 = source.scale

    fun position(x: Float, y: Float, z: Float) {
        position = SceneVec3(x, y, z)
    }

    fun rotation(x: Float, y: Float, z: Float) {
        rotation = SceneVec3(x, y, z)
    }

    fun scale(x: Float, y: Float, z: Float) {
        scale = SceneVec3(x, y, z)
    }

    internal fun build(): SceneTransform = SceneTransform(
        position = position,
        rotation = rotation,
        scale = scale
    )
}
