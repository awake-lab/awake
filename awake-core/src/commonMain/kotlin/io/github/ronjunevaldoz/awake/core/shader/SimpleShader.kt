package io.github.ronjunevaldoz.awake.core.shader

import io.github.ronjunevaldoz.awake.core.utils.readResourceBytes

/**
 * TODO move to separate library AwakeCompose
 */
class SimpleShader(
    private val vertFile: String,
    private val fragFile: String,
    private val define: String = "",
) : DefaultShader() {

    private val shaderDir = "assets/shader"

    var transformMatrix by uniform
    var modelViewMatrix by uniform
    var projectionViewMatrix by uniform

    override fun getVertexSource(): String {
        val vertString = readResourceBytes("$shaderDir/$vertFile").decodeToString()
        return define + vertString
    }

    override fun getFragmentSource(): String {
        val fragString = readResourceBytes("$shaderDir/$fragFile").decodeToString()
        return define + fragString
    }
}
