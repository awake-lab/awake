package io.github.ronjunevaldoz.awake.core.graphics.opengl

import io.github.ronjunevaldoz.awake.core.graphics.Bitmap
import io.github.ronjunevaldoz.awake.core.graphics.opengl.ext.checkGL
import io.github.ronjunevaldoz.awake.core.memory.Buffer
import io.github.ronjunevaldoz.awake.core.memory.FloatBuf
import io.github.ronjunevaldoz.awake.core.memory.IntBuf


internal expect object Agl : OpenGL
internal object AglDebuggable : OpenGL {
    override fun clearColor(r: Float, g: Float, b: Float, a: Float) {
        Agl.clearColor(r, g, b, a)
        checkGL()
    }

    override fun clear(mask: Int) {
        Agl.clear(mask)
        checkGL()
    }

    override fun viewport(x: Int, y: Int, width: Int, height: Int) {
        Agl.viewport(x, y, width, height)
        checkGL()
    }

    override fun createShader(type: OpenGL.ShaderType): Int {
        return Agl.createShader(type).apply {
            checkGL()
        }
    }

    override fun shaderSource(shader: ShaderId, content: String) {
        Agl.shaderSource(shader, content)
        checkGL()
    }

    override fun compileShader(shader: ShaderId) {
        Agl.compileShader(shader)
        checkGL()
    }

    override fun getShaderInfoLog(shader: ShaderId): String {
        return Agl.getShaderInfoLog(shader).apply {
            checkGL()
        }
    }

    override fun getShaderiv(shader: ShaderId, status: OpenGL.Status, buffer: IntBuf) {
        Agl.getShaderiv(shader, status, buffer)
        checkGL()
    }

    override fun createProgram(): ProgramId {
        return Agl.createProgram().apply {
            checkGL()
        }
    }

    override fun attachShader(program: ProgramId, shader: ShaderId) {
        Agl.attachShader(program, shader)
        checkGL()
    }

    override fun linkProgram(program: ProgramId) {
        Agl.linkProgram(program)
        checkGL()
    }

    override fun getProgramInfoLog(program: ProgramId): String {
        return Agl.getProgramInfoLog(program).apply {
            checkGL()
        }
    }

    override fun getProgramiv(program: ProgramId, status: OpenGL.Status, buffer: IntBuf) {
        Agl.getProgramiv(program, status, buffer)
        checkGL()
    }

    override fun deleteShader(shader: ShaderId) {
        Agl.deleteShader(shader)
        checkGL()
    }

    override fun useProgram(program: ProgramId) {
        Agl.useProgram(program)
        checkGL()
    }

    override fun deleteProgram(program: ProgramId) {
        Agl.deleteProgram(program)
        checkGL()
    }

    override fun getUniformLocation(program: ProgramId, name: String): Int {
        return Agl.getUniformLocation(program, name).apply {
            checkGL()
        }
    }

    override fun bindAttribLocation(program: ProgramId, index: Int, name: String) {
        Agl.bindAttribLocation(program, index, name)
        checkGL()
    }

    override fun getAttribLocation(program: ProgramId, name: String): Int {
        return Agl.getAttribLocation(program, name).apply {
            checkGL()
        }
    }

    override fun vertexAttribPointer(
        index: Int,
        size: Int,
        type: Int,
        normalized: Boolean,
        stride: Int,
        ptr: Buffer?
    ) {
        Agl.vertexAttribPointer(index, size, type, normalized, stride, ptr)
        checkGL()
    }

    override fun vertexAttribPointer(
        index: Int,
        size: Int,
        type: Int,
        normalized: Boolean,
        stride: Int,
        offset: Int
    ) {
        Agl.vertexAttribPointer(index, size, type, normalized, stride, offset)
        checkGL()
    }

    override fun enableVertexAttribArray(index: Int) {
        Agl.enableVertexAttribArray(index)
        checkGL()
    }

    override fun drawArrays(mode: OpenGL.DrawMode, first: Int, count: Int) {
        Agl.drawArrays(mode, first, count)
        checkGL()
    }

    override fun disableVertexAttribArray(index: Int) {
        Agl.disableVertexAttribArray(index)
        checkGL()
    }

    override fun genBuffers(): Int {
        return Agl.genBuffers().also {
            checkGL()
        }
    }

    override fun genBuffers(n: Int, buffers: IntBuf) {
        Agl.genBuffers(n, buffers)
        checkGL()
    }

    override fun bindBuffer(target: OpenGL.BufferType, buffer: BufferId) {
        Agl.bindBuffer(target, buffer)
        checkGL()
    }

    override fun bufferData(
        target: OpenGL.BufferType,
        data: Buffer,
        usage: OpenGL.DrawType
    ) {
        Agl.bufferData(target, data, usage)
        checkGL()
    }

    override fun genVertexArrays(): Int {
        return Agl.genVertexArrays().also {
            checkGL()
        }
    }

    override fun deleteBuffers(n: Int, buffers: IntBuf) {
        Agl.deleteBuffers(n, buffers)
        checkGL()
    }

    override fun deleteVertexArrays(arrays: Int) {
        Agl.deleteVertexArrays(arrays)
        checkGL()
    }

    override fun deleteVertexArrays(n: Int, arrays: IntBuf) {
        Agl.deleteVertexArrays(n, arrays)
        checkGL()
    }

    override fun deleteTextures(textures: Int) {
        Agl.deleteTextures(textures)
        checkGL()
    }

    override fun deleteTextures(n: Int, textures: IntBuf) {
        Agl.deleteTextures(n, textures)
        checkGL()
    }

    override fun deleteFrameBuffers(frameBuffer: Int) {
        Agl.deleteFrameBuffers(frameBuffer)
        checkGL()
    }

    override fun deleteRenderBuffers(renderBuffer: Int) {
        Agl.deleteRenderBuffers(renderBuffer)
        checkGL()
    }

    override fun genVertexArrays(n: Int, arrays: IntBuf) {
        Agl.genVertexArrays(n, arrays)
        checkGL()
    }

    override fun bindVertexArray(array: ArrayId) {
        Agl.bindVertexArray(array)
        checkGL()
    }

    override fun drawElements(mode: OpenGL.DrawMode, count: Int, type: Int, indices: Buffer?) {
        Agl.drawElements(mode, count, type, indices)
        checkGL()
    }

    override fun genTextures(): Int {
        return Agl.genTextures().also {
            checkGL()
        }
    }

    override fun genTextures(n: Int, textures: IntBuf) {
        Agl.genTextures(n, textures)
        checkGL()
    }

    override fun bindTexture(target: Int, texture: Int) {
        Agl.bindTexture(target, texture)
        checkGL()
    }

    override fun activeTexture(texture: Int) {
        Agl.activeTexture(texture)
        checkGL()
    }

    override fun readPixels(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        format: Int,
        type: Int,
        pixels: Buffer
    ) {
        Agl.readPixels(x, y, width, height, format, type, pixels)
        checkGL()
    }

    override fun texImage2D(
        target: Int,
        level: Int,
        internalFormat: Int,
        width: Int,
        height: Int,
        border: Int,
        format: Int,
        type: Int,
        buffer: Buffer?
    ) {
        Agl.texImage2D(target, level, internalFormat, width, height, border, format, type, buffer)
        checkGL()
    }

    override fun texImage2D(
        target: Int,
        level: Int,
        internalFormat: Int,
        bitmap: Bitmap,
        border: Int
    ) {
        Agl.texImage2D(target, level, internalFormat, bitmap, border)
        checkGL()
    }

    override fun texSubImage2D(
        target: Int,
        level: Int,
        xOffset: Int,
        yOffset: Int,
        width: Int,
        height: Int,
        format: Int,
        type: Int,
        buffer: Buffer?
    ) {
        Agl.texSubImage2D(target, level, xOffset, yOffset, width, height, format, type, buffer)
        checkGL()
    }

    override fun texSubImage2D(
        target: Int,
        level: Int,
        xOffset: Int,
        yOffset: Int,
        bitmap: Bitmap,
        format: Int
    ) {
        Agl.texSubImage2D(target, level, xOffset, yOffset, bitmap, format)
        checkGL()
    }

    override fun texParameteri(target: Int, pname: Int, param: Int) {
        Agl.texParameteri(target, pname, param)
        checkGL()
    }

    override fun generateMipmap(target: Int) {
        Agl.generateMipmap(target)
        checkGL()
    }

    override fun uniform(location: Int, x: Int) {
        Agl.uniform(location, x)
        checkGL()
    }

    override fun uniform(location: Int, x: Float) {
        Agl.uniform(location, x)
        checkGL()
    }

    override fun uniform(location: Int, x: Int, y: Int, z: Int) {
        Agl.uniform(location, x, y, z)
        checkGL()
    }

    override fun uniform(location: Int, x: Float, y: Float, z: Float) {
        Agl.uniform(location, x, y, z)
        checkGL()
    }

    override fun uniform(location: Int, x: Float, y: Float, z: Float, w: Float) {
        Agl.uniform(location, x, y, z, w)
        checkGL()
    }

    override fun uniformMatrix4fv(location: Int, count: Int, transpose: Boolean, value: FloatBuf) {
        Agl.uniformMatrix4fv(location, count, transpose, value)
        checkGL()
    }

    override fun genFrameBuffers(): Int {
        return Agl.genFrameBuffers().also {
            checkGL()
        }
    }

    override fun bindFramebuffer(target: Int, frameBuffer: Int) {
        Agl.bindFramebuffer(target, frameBuffer)
        checkGL()
    }

    override fun framebufferTexture2D(
        target: Int,
        attachment: Int,
        texTarget: Int,
        texture: Int,
        level: Int
    ) {
        Agl.framebufferTexture2D(target, attachment, texTarget, texture, level)
        checkGL()
    }

    override fun genRenderBuffers(): Int {
        return Agl.genRenderBuffers().also {
            checkGL()
        }
    }

    override fun bindRenderBuffers(target: Int, renderBuffer: Int) {
        Agl.bindRenderBuffers(target, renderBuffer)
        checkGL()
    }

    override fun renderBufferStorage(target: Int, internalFormat: Int, width: Int, height: Int) {
        Agl.renderBufferStorage(target, internalFormat, width, height)
        checkGL()
    }

    override fun framebufferRenderBuffer(
        target: Int,
        attachment: Int,
        renderBufferTarget: Int,
        renderBuffer: Int
    ) {
        Agl.framebufferRenderBuffer(target, attachment, renderBufferTarget, renderBuffer)
        checkGL()
    }

    override fun checkFramebufferStatus(target: Int): Int {
        return Agl.checkFramebufferStatus(target).also {
            checkGL()
        }
    }

    override fun cullFace(face: Int) {
        Agl.cullFace(face)
        checkGL()
    }

    override fun frontFace(mode: Int) {
        Agl.frontFace(mode)
        checkGL()
    }

    override fun enable(cap: Int) {
        Agl.enable(cap)
        checkGL()
    }

    override fun disable(cap: Int) {
        Agl.disable(cap)
        checkGL()
    }

    override fun blendFunc(sFactor: Int, dFactor: Int) {
        Agl.blendFunc(sFactor, dFactor)
        checkGL()
    }

    override fun deleteBuffers(buffers: Int) {
        Agl.deleteBuffers(buffers)
        checkGL()
    }

    override fun getError(): Int {
        return Agl.getError()
    }
}