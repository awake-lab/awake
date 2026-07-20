// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime.dsl

import io.github.ronjunevaldoz.awake.scene.runtime.SceneLight
import io.github.ronjunevaldoz.awake.scene.runtime.SceneVec3

@AwakeSceneDsl
class SceneLightDsl internal constructor(
    source: SceneLight
) {
    private var color: SceneVec3 = source.color
    private var intensity: Float = source.intensity
    private var type: SceneLight.Type = source.type

    fun color(r: Float, g: Float, b: Float) {
        color = SceneVec3(r, g, b)
    }

    fun intensity(value: Float) {
        intensity = value
    }

    fun directional() {
        type = SceneLight.Type.Directional
    }

    fun point() {
        type = SceneLight.Type.Point
    }

    internal fun build(): SceneLight = SceneLight(
        color = color,
        intensity = intensity,
        type = type
    )
}
