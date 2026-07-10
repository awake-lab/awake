package io.github.ronjunevaldoz.awake.core.graphics.opengl

import io.github.ronjunevaldoz.awake.core.graphics.Bitmap
import io.github.ronjunevaldoz.awake.core.memory.Buffer
import io.github.ronjunevaldoz.awake.core.memory.FloatBuf
import io.github.ronjunevaldoz.awake.core.memory.IntBuf

typealias ProgramId = Int
typealias ShaderId = Int
typealias BufferId = Int
typealias ArrayId = Int

interface OpenGL {
    enum class Version {
        ES1,
        ES2,
        ES3,
    }

    enum class Status(val value: Int) {
        Compile(CommonGL.GL_COMPILE_STATUS),
        Link(CommonGL.GL_LINK_STATUS),
        Delete(CommonGL.GL_DELETE_STATUS),
        Validate(CommonGL.GL_VALIDATE_STATUS)
    }

    enum class BufferBit(val value: Int) {
        Color(CommonGL.GL_COLOR_BUFFER_BIT),
        Depth(CommonGL.GL_DEPTH_BUFFER_BIT),
        Stencil(CommonGL.GL_STENCIL_BUFFER_BIT)
    }

    enum class DrawMode(val value: Int) {
        Triangles(CommonGL.GL_TRIANGLES),
        TriangleFan(CommonGL.GL_TRIANGLE_FAN),
        TriangleStrip(CommonGL.GL_TRIANGLE_STRIP),
    }

    enum class ShaderType(val value: Int) {
        Vertex(CommonGL.GL_VERTEX_SHADER),
        Fragment(CommonGL.GL_FRAGMENT_SHADER)
    }

    enum class BufferType(val value: Int) {
        Array(CommonGL.GL_ARRAY_BUFFER),
        ElementArray(CommonGL.GL_ELEMENT_ARRAY_BUFFER)
    }

    enum class DrawType(val value: Int) {
        Static(CommonGL.GL_STATIC_DRAW),
        Dynamic(CommonGL.GL_DYNAMIC_DRAW)
    }

    fun clearColor(r: Float, g: Float, b: Float, a: Float)
    fun clear(mask: Int)
    fun viewport(x: Int, y: Int, width: Int, height: Int)

    // shader
    fun createShader(type: ShaderType): Int
    fun shaderSource(shader: ShaderId, content: String)
    fun compileShader(shader: ShaderId)
    fun getShaderInfoLog(shader: ShaderId): String
    fun getShaderiv(shader: ShaderId, status: Status, buffer: IntBuf)
    fun createProgram(): ProgramId
    fun attachShader(program: ProgramId, shader: ShaderId)
    fun linkProgram(program: ProgramId)
    fun getProgramInfoLog(program: ProgramId): String
    fun getProgramiv(program: ProgramId, status: Status, buffer: IntBuf)
    fun deleteShader(shader: ShaderId)
    fun useProgram(program: ProgramId)
    fun deleteProgram(program: ProgramId)

    // Uniforms
    fun getUniformLocation(program: ProgramId, name: String): Int

    // Attributes
    fun bindAttribLocation(program: ProgramId, index: Int, name: String)
    fun getAttribLocation(program: ProgramId, name: String): Int
    fun vertexAttribPointer(
        index: Int,
        size: Int,
        type: Int,
        normalized: Boolean,
        stride: Int,
        ptr: Buffer? = null
    )

    fun vertexAttribPointer(
        index: Int,
        size: Int,
        type: Int,
        normalized: Boolean,
        stride: Int,
        offset: Int
    )

    fun enableVertexAttribArray(index: Int)
    fun drawArrays(mode: DrawMode, first: Int, count: Int)
    fun disableVertexAttribArray(index: Int)

    // buffers
    fun genBuffers(): Int
    fun genBuffers(n: Int, buffers: IntBuf)
    fun bindBuffer(target: BufferType, buffer: BufferId)
    fun bufferData(
        target: BufferType,
        data: Buffer,
        usage: DrawType
    )

    // available only for android gl 3.0 or up and for ios use EOS which available in 2.0
    fun genVertexArrays(): Int
    fun genVertexArrays(n: Int, arrays: IntBuf)
    fun bindVertexArray(array: ArrayId)
    fun drawElements(mode: DrawMode, count: Int, type: Int, indices: Buffer? = null)
    fun genTextures(): Int
    fun genTextures(n: Int, textures: IntBuf)
    fun bindTexture(target: Int, texture: Int)
    fun activeTexture(texture: Int)
    fun readPixels(x: Int, y: Int, width: Int, height: Int, format: Int, type: Int, pixels: Buffer)

    fun texImage2D(
        target: Int,
        level: Int,
        internalFormat: Int,
        width: Int,
        height: Int,
        border: Int,
        format: Int,
        type: Int,
        buffer: Buffer?
    )

    fun texImage2D(target: Int, level: Int, internalFormat: Int, bitmap: Bitmap, border: Int)
    fun texSubImage2D(
        target: Int,
        level: Int,
        xOffset: Int,
        yOffset: Int,
        width: Int,
        height: Int,
        format: Int,
        type: Int,
        buffer: Buffer?
    )

    fun texSubImage2D(
        target: Int,
        level: Int,
        xOffset: Int,
        yOffset: Int,
        bitmap: Bitmap,
        format: Int
    )

    fun texParameteri(target: Int, pname: Int, param: Int)
    fun generateMipmap(target: Int)
    fun uniform(location: Int, x: Int)
    fun uniform(location: Int, x: Float)
    fun uniform(location: Int, x: Int, y: Int, z: Int)
    fun uniform(location: Int, x: Float, y: Float, z: Float)
    fun uniform(location: Int, x: Float, y: Float, z: Float, w: Float)
    fun uniformMatrix4fv(location: Int, count: Int, transpose: Boolean, value: FloatBuf)
    fun genFrameBuffers(): Int
    fun bindFramebuffer(target: Int, frameBuffer: Int)
    fun framebufferTexture2D(target: Int, attachment: Int, texTarget: Int, texture: Int, level: Int)
    fun genRenderBuffers(): Int
    fun bindRenderBuffers(target: Int, renderBuffer: Int)
    fun renderBufferStorage(target: Int, internalFormat: Int, width: Int, height: Int)
    fun framebufferRenderBuffer(
        target: Int,
        attachment: Int,
        renderBufferTarget: Int,
        renderBuffer: Int
    )

    fun checkFramebufferStatus(target: Int): Int
    fun cullFace(face: Int)
    fun frontFace(mode: Int)
    fun enable(cap: Int)
    fun disable(cap: Int)
    fun blendFunc(sFactor: Int, dFactor: Int)
    fun deleteBuffers(buffers: Int)
    fun deleteBuffers(n: Int, buffers: IntBuf)
    fun deleteVertexArrays(arrays: Int)
    fun deleteVertexArrays(n: Int, arrays: IntBuf)
    fun deleteTextures(textures: Int)
    fun deleteTextures(n: Int, textures: IntBuf)
    fun deleteFrameBuffers(frameBuffer: Int)
    fun deleteRenderBuffers(renderBuffer: Int)
    fun getError(): Int
}

