// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime

import kotlinx.serialization.Serializable

@Serializable
data class SceneDocument(
    val name: String? = null,
    val nodes: List<SceneNode> = emptyList()
)

@Serializable
data class SceneNode(
    val name: String? = null,
    val transform: SceneTransform = SceneTransform(),
    val camera: SceneCamera? = null,
    val light: SceneLight? = null,
    val meshRenderer: SceneMeshRenderer? = null,
    val children: List<SceneNode> = emptyList()
)

@Serializable
data class SceneTransform(
    val position: SceneVec3 = SceneVec3(),
    val rotation: SceneVec3 = SceneVec3(),
    val scale: SceneVec3 = SceneVec3(1f, 1f, 1f)
)

@Serializable
data class SceneVec3(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f
)

@Serializable
data class SceneCamera(
    val eye: SceneVec3 = SceneVec3(0f, 0f, 5f),
    val center: SceneVec3 = SceneVec3(0f, 0f, 0f),
    val up: SceneVec3 = SceneVec3(0f, 1f, 0f),
    val fovYDegrees: Float = 60f,
    val near: Float = 0.1f,
    val far: Float = 100f,
    val primary: Boolean = true,
    val flipYForClipSpace: Boolean = true
)

@Serializable
data class SceneLight(
    val color: SceneVec3 = SceneVec3(1f, 1f, 1f),
    val intensity: Float = 1f,
    val type: Type = Type.Point
) {
    @Serializable
    enum class Type {
        Directional,
        Point
    }
}

@Serializable
data class SceneMeshRenderer(
    val mesh: String,
    val material: String
)
