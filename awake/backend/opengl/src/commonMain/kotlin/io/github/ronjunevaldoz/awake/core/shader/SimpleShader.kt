// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.shader

import io.github.ronjunevaldoz.awake.core.utils.readResourceBytes

/**
 * TODO move to separate library AwakeCompose
 *
 * Web demo (see docs/MVP_PLAN.md's decision log): construction is a `suspend` factory
 * ([create]), not a plain constructor -- loading shader source is now `suspend` (real
 * async browser `fetch()` on wasmJs), and `suspend` calls aren't allowed in a property
 * initializer. [Shader]/[BaseShader]'s own `compile()` contract stays fully synchronous:
 * [getVertexSource]/[getFragmentSource] just return the already-loaded strings.
 */
class SimpleShader private constructor(
    private val vertSource: String,
    private val fragSource: String
) : DefaultShader() {

    var transformMatrix by uniform
    var modelViewMatrix by uniform
    var projectionViewMatrix by uniform

    override fun getVertexSource(): String = vertSource

    override fun getFragmentSource(): String = fragSource

    companion object {
        private const val shaderDir = "assets/shader"

        suspend fun create(vertFile: String, fragFile: String, define: String = ""): SimpleShader {
            val vertString = readResourceBytes("$shaderDir/$vertFile").decodeToString()
            val fragString = readResourceBytes("$shaderDir/$fragFile").decodeToString()
            return SimpleShader(define + vertString, define + fragString)
        }
    }
}
