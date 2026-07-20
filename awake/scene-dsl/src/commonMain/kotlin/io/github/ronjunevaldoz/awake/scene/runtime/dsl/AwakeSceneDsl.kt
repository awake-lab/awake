// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime.dsl

import io.github.ronjunevaldoz.awake.scene.runtime.SceneDocument

@DslMarker
annotation class AwakeSceneDsl

/**
 * Entry point for the declarative scene document DSL.
 */
fun scene(
    name: String? = null,
    block: SceneDocumentDsl.() -> Unit
): SceneDocument {
    val builder = SceneDocumentDsl(name)
    builder.block()
    return builder.build()
}
