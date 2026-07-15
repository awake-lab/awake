// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime

@DslMarker
annotation class AwakeSceneDsl

fun scene(
    name: String? = null,
    block: SceneDocumentDsl.() -> Unit
): SceneDocument {
    val builder = SceneDocumentDsl(name)
    builder.block()
    return builder.build()
}

@AwakeSceneDsl
class SceneDocumentDsl internal constructor(
    private var name: String?
) {
    private val nodes = ArrayList<SceneNode>()

    fun name(value: String?): SceneDocumentDsl = apply {
        name = value
    }

    fun entity(
        name: String? = null,
        block: SceneNodeDsl.() -> Unit
    ) {
        val builder = SceneNodeDsl(name)
        builder.block()
        nodes += builder.build()
    }

    internal fun build(): SceneDocument = SceneDocument(
        name = name,
        nodes = nodes.toList()
    )
}

@AwakeSceneDsl
class SceneNodeDsl internal constructor(
    private var name: String?
) {
    private var transform: SceneTransform = SceneTransform()
    private var camera: SceneCamera? = null
    private var light: SceneLight? = null
    private var meshRenderer: SceneMeshRenderer? = null
    private val children = ArrayList<SceneNode>()

    fun name(value: String?): SceneNodeDsl = apply {
        name = value
    }

    fun transform(block: SceneTransformDsl.() -> Unit) {
        val builder = SceneTransformDsl(transform)
        builder.block()
        transform = builder.build()
    }

    fun camera(block: SceneCameraDsl.() -> Unit) {
        val builder = SceneCameraDsl(camera ?: SceneCamera())
        builder.block()
        camera = builder.build()
    }

    fun light(block: SceneLightDsl.() -> Unit) {
        val builder = SceneLightDsl(light ?: SceneLight())
        builder.block()
        light = builder.build()
    }

    fun meshRenderer(
        mesh: String,
        material: String
    ) {
        meshRenderer = SceneMeshRenderer(mesh = mesh, material = material)
    }

    fun entity(
        name: String? = null,
        block: SceneNodeDsl.() -> Unit
    ) {
        val builder = SceneNodeDsl(name)
        builder.block()
        children += builder.build()
    }

    internal fun build(): SceneNode = SceneNode(
        name = name,
        transform = transform,
        camera = camera,
        light = light,
        meshRenderer = meshRenderer,
        children = children.toList()
    )
}

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
