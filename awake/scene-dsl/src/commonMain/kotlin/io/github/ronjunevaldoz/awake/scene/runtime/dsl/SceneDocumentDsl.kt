// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime.dsl

import io.github.ronjunevaldoz.awake.scene.runtime.SceneDocument
import io.github.ronjunevaldoz.awake.scene.runtime.SceneNode

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
