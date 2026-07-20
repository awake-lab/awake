// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime.dsl

import io.github.ronjunevaldoz.awake.scene.runtime.SceneCamera
import io.github.ronjunevaldoz.awake.scene.runtime.SceneLight
import io.github.ronjunevaldoz.awake.scene.runtime.SceneMeshRenderer
import io.github.ronjunevaldoz.awake.scene.runtime.SceneNode
import io.github.ronjunevaldoz.awake.scene.runtime.SceneTransform

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
