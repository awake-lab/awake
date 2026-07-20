// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime.dsl

import io.github.ronjunevaldoz.awake.scene.runtime.SceneCamera
import io.github.ronjunevaldoz.awake.scene.runtime.SceneVec3

@AwakeSceneDsl
class SceneCameraDsl internal constructor(
    source: SceneCamera
) {
    private var eye: SceneVec3 = source.eye
    private var center: SceneVec3 = source.center
    private var up: SceneVec3 = source.up
    private var fovYDegrees: Float = source.fovYDegrees
    private var near: Float = source.near
    private var far: Float = source.far
    private var primary: Boolean = source.primary

    fun eye(x: Float, y: Float, z: Float) {
        eye = SceneVec3(x, y, z)
    }

    fun center(x: Float, y: Float, z: Float) {
        center = SceneVec3(x, y, z)
    }

    fun up(x: Float, y: Float, z: Float) {
        up = SceneVec3(x, y, z)
    }

    fun perspective(
        fovYDegrees: Float = this.fovYDegrees,
        near: Float = this.near,
        far: Float = this.far
    ) {
        this.fovYDegrees = fovYDegrees
        this.near = near
        this.far = far
    }

    fun primary(value: Boolean) {
        primary = value
    }

    internal fun build(): SceneCamera = SceneCamera(
        eye = eye,
        center = center,
        up = up,
        fovYDegrees = fovYDegrees,
        near = near,
        far = far,
        primary = primary
    )
}
