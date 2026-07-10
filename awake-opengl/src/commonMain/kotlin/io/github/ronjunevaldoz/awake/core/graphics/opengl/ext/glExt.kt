package io.github.ronjunevaldoz.awake.core.graphics.opengl.ext

import androidx.compose.ui.graphics.Color
import io.github.ronjunevaldoz.awake.core.graphics.opengl.CommonGL
import io.github.ronjunevaldoz.awake.core.graphics.opengl.OpenGL
import io.github.ronjunevaldoz.awake.core.utils.BufferUtils

/**
 * Extensions
 */
fun OpenGL.clearColor(r: Double, g: Double, b: Double, a: Double) {
    clearColor(r.toFloat(), g.toFloat(), b.toFloat(), a.toFloat())
}

fun OpenGL.clearColor(color: Color) {
    clearColor(color.red, color.green, color.blue, color.alpha)
}

fun OpenGL.clearColor(hex: Long) {
    clearColor(Color(hex))
}

fun OpenGL.clearColor(hexCode: String, a: Float) {
    val color = hexCode.toColor()
    clearColor(color.red, color.green, color.blue, a)
}

fun String.toColor(): Color {
    return Color(toLong(16))
}

fun OpenGL.tryCompileShader(type: OpenGL.ShaderType, content: String): Int {
    val shader = createShader(type)
    shaderSource(shader, content)
    compileShader(shader)
    // check shader compilation
    checkShaderCompilation(shader)
    return shader
}

fun OpenGL.checkShaderCompilation(shader: Int) {
    val compileStatus = BufferUtils.allocateInt(1)
    getShaderiv(shader, OpenGL.Status.Compile, compileStatus)
    check(compileStatus[0] == CommonGL.GL_TRUE) {
        val infoLog = getShaderInfoLog(shader)
        deleteShader(shader)
        infoLog
    }
}

fun OpenGL.tryLinkProgram(
    vertexShader: Int,
    fragmentShader: Int
): Int {
    val programId = createProgram()
    attachShader(programId, vertexShader)
    attachShader(programId, fragmentShader)
    linkProgram(programId)
    // check valid
    checkProgramLinking(programId)
    deleteShader(vertexShader)
    deleteShader(fragmentShader)
    return programId
}

fun OpenGL.checkProgramLinking(programId: Int) {
    val linkStatus = BufferUtils.allocateInt(1)
    getProgramiv(programId, OpenGL.Status.Link, linkStatus)
    check(linkStatus[0] == CommonGL.GL_TRUE) {
        val infoLog = getProgramInfoLog(programId)
        deleteProgram(programId)
        infoLog
    }
}

fun OpenGL.tryCreateShader(
    vertex: String,
    fragment: String
): Int {
    // create  vertex shader
    val vertexShader = tryCompileShader(OpenGL.ShaderType.Vertex, vertex)
    // create  fragment shader
    val fragmentShader = tryCompileShader(OpenGL.ShaderType.Fragment, fragment)
    // create program
    return tryLinkProgram(vertexShader, fragmentShader)
}

fun OpenGL.checkGL() {
    getError().takeIf { it != 0 }?.let {
        error("BUG $it")
    }
}