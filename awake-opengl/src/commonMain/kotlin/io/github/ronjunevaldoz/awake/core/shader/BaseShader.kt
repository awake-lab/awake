package io.github.ronjunevaldoz.awake.core.shader

import io.github.ronjunevaldoz.awake.core.AwakeContext
import io.github.ronjunevaldoz.awake.core.graphics.opengl.ext.tryCreateShader

abstract class BaseShader : Shader {
    override var program = -1
    abstract fun getVertexSource(): String
    abstract fun getFragmentSource(): String
    override fun compile() {
        val (vertString, fragString) = getShaderSources()
        program = AwakeContext.gl.tryCreateShader(
            vertex = vertString,
            fragment = fragString
        )
    }

    override fun begin() {
        AwakeContext.gl.useProgram(program)
    }

    override fun end() {
        AwakeContext.gl.useProgram(0)
    }

    override fun delete() {
        AwakeContext.gl.deleteProgram(program)
    }

    private fun getShaderSources(): Pair<String, String> = with(this) {
        val vertString = getVertexSource()
        val fragString = getFragmentSource()
        return Pair(vertString, fragString)
    }
}