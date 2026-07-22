// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime

import io.github.ronjunevaldoz.awake.scene.runtime.dsl.SceneDocumentDsl

/**
 * Compatibility entry point for document-authored scenes kept in the runtime package.
 *
 * The canonical DSL now lives under `scene.runtime.dsl`, but callers in
 * `scene.runtime` still expect the top-level `scene(...)` builder here.
 */
@Deprecated(
    message = "Compatibility scene entry point slated for future removal. Prefer io.github.ronjunevaldoz.awake.scene.runtime.dsl.SceneDocumentDsl or the canonical scene DSL entrypoint."
)
fun scene(
    name: String? = null,
    block: SceneDocumentDsl.() -> Unit
): SceneDocument {
    val builder = SceneDocumentDsl(name)
    builder.block()
    return builder.build()
}
