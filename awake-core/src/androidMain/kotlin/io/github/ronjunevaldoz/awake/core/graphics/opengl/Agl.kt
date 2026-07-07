package io.github.ronjunevaldoz.awake.core.graphics.opengl

import android.opengl.GLES20.GL_UNSIGNED_BYTE
import android.opengl.GLES20.glActiveTexture
import android.opengl.GLES20.glAttachShader
import android.opengl.GLES20.glBindAttribLocation
import android.opengl.GLES20.glBindBuffer
import android.opengl.GLES20.glBindFramebuffer
import android.opengl.GLES20.glBindRenderbuffer
import android.opengl.GLES20.glBindTexture
import android.opengl.GLES20.glBlendFunc
import android.opengl.GLES20.glCheckFramebufferStatus
import android.opengl.GLES20.glClear
import android.opengl.GLES20.glClearColor
import android.opengl.GLES20.glCompileShader
import android.opengl.GLES20.glCreateProgram
import android.opengl.GLES20.glCreateShader
import android.opengl.GLES20.glCullFace
import android.opengl.GLES20.glDeleteBuffers
import android.opengl.GLES20.glDeleteFramebuffers
import android.opengl.GLES20.glDeleteProgram
import android.opengl.GLES20.glDeleteRenderbuffers
import android.opengl.GLES20.glDeleteShader
import android.opengl.GLES20.glDeleteTextures
import android.opengl.GLES20.glDisable
import android.opengl.GLES20.glDisableVertexAttribArray
import android.opengl.GLES20.glDrawArrays
import android.opengl.GLES20.glDrawElements
import android.opengl.GLES20.glEnable
import android.opengl.GLES20.glEnableVertexAttribArray
import android.opengl.GLES20.glFramebufferRenderbuffer
import android.opengl.GLES20.glFramebufferTexture2D
import android.opengl.GLES20.glFrontFace
import android.opengl.GLES20.glGenBuffers
import android.opengl.GLES20.glGenFramebuffers
import android.opengl.GLES20.glGenRenderbuffers
import android.opengl.GLES20.glGenTextures
import android.opengl.GLES20.glGenerateMipmap
import android.opengl.GLES20.glGetAttribLocation
import android.opengl.GLES20.glGetError
import android.opengl.GLES20.glGetProgramInfoLog
import android.opengl.GLES20.glGetProgramiv
import android.opengl.GLES20.glGetShaderInfoLog
import android.opengl.GLES20.glGetShaderiv
import android.opengl.GLES20.glGetUniformLocation
import android.opengl.GLES20.glLinkProgram
import android.opengl.GLES20.glReadPixels
import android.opengl.GLES20.glRenderbufferStorage
import android.opengl.GLES20.glShaderSource
import android.opengl.GLES20.glTexImage2D
import android.opengl.GLES20.glTexParameteri
import android.opengl.GLES20.glTexSubImage2D
import android.opengl.GLES20.glUniform1f
import android.opengl.GLES20.glUniform1i
import android.opengl.GLES20.glUniform3f
import android.opengl.GLES20.glUniform3i
import android.opengl.GLES20.glUniform4f
import android.opengl.GLES20.glUniformMatrix4fv
import android.opengl.GLES20.glUseProgram
import android.opengl.GLES20.glVertexAttribPointer
import android.opengl.GLES20.glViewport
import android.opengl.GLES30.glBindVertexArray
import android.opengl.GLES30.glBufferData
import android.opengl.GLES30.glDeleteVertexArrays
import android.opengl.GLES30.glGenVertexArrays
import io.github.ronjunevaldoz.awake.core.graphics.Bitmap
import io.github.ronjunevaldoz.awake.core.memory.Buffer
import io.github.ronjunevaldoz.awake.core.memory.FloatBuf
import io.github.ronjunevaldoz.awake.core.memory.IntBuf
import io.github.ronjunevaldoz.awake.core.memory.createIntBuffer
import io.github.ronjunevaldoz.awake.core.rendering.toFormat
import io.github.ronjunevaldoz.awake.core.utils.BufferUtils
import io.github.ronjunevaldoz.awake.core.utils.sizeBytes
import io.github.ronjunevaldoz.awake.core.utils.toUType

internal actual object Agl : OpenGL {
    override fun clearColor(r: Float, g: Float, b: Float, a: Float) {
        glClearColor(r, g, b, a)
    }

    override fun clear(mask: Int) {
//        val bitMask = mask.fold(0) { acc, m -> acc or m.value }
        glClear(mask)
    }

    override fun viewport(x: Int, y: Int, width: Int, height: Int) {
        glViewport(x, y, width, height)
    }

    override fun createShader(type: OpenGL.ShaderType): Int {
        return glCreateShader(type.value)
    }

    override fun shaderSource(shader: Int, content: String) {
        glShaderSource(shader, content)
    }

    override fun compileShader(shader: Int) {
        glCompileShader(shader)
    }

    override fun getShaderInfoLog(shader: Int): String {
        return glGetShaderInfoLog(shader)
    }

    override fun getShaderiv(shader: Int, status: OpenGL.Status, buffer: IntBuf) {
        glGetShaderiv(shader, status.value, buffer.get())
    }

    override fun createProgram(): Int {
        return glCreateProgram()
    }

    override fun attachShader(program: Int, shader: Int) {
        glAttachShader(program, shader)
    }

    override fun linkProgram(program: Int) {
        glLinkProgram(program)
    }

    override fun getProgramInfoLog(program: Int): String {
        return glGetProgramInfoLog(program)
    }

    override fun getProgramiv(program: Int, status: OpenGL.Status, buffer: IntBuf) {
        glGetProgramiv(program, status.value, buffer.get())
    }

    override fun deleteShader(shader: Int) {
        glDeleteShader(shader)
    }

    override fun useProgram(program: Int) {
        glUseProgram(program)
    }

    override fun deleteProgram(program: Int) {
        glDeleteProgram(program)
    }

    override fun getUniformLocation(program: Int, name: String): Int {
        return glGetUniformLocation(program, name)
    }

    override fun bindAttribLocation(program: ProgramId, index: Int, name: String) {
        glBindAttribLocation(program, index, name)
    }

    override fun getAttribLocation(program: Int, name: String): Int {
        return glGetAttribLocation(program, name)
    }

    override fun vertexAttribPointer(
        index: Int,
        size: Int,
        type: Int,
        normalized: Boolean,
        stride: Int,
        ptr: Buffer?
    ) {
        if (ptr == null) {
            vertexAttribPointer(
                index,
                size,
                type,
                normalized,
                stride,
                0
            )
        } else {
            glVertexAttribPointer(
                index,
                size,
                type,
                normalized,
                stride,
                ptr.get()
            )
        }
    }

    override fun vertexAttribPointer(
        index: Int,
        size: Int,
        type: Int,
        normalized: Boolean,
        stride: Int,
        offset: Int
    ) {
        glVertexAttribPointer(
            index,
            size,
            type,
            normalized,
            stride,
            offset
        )
    }

    override fun enableVertexAttribArray(index: Int) {
        glEnableVertexAttribArray(index)
    }

    override fun drawArrays(mode: OpenGL.DrawMode, first: Int, count: Int) {
        glDrawArrays(mode.value, first, count)
    }

    override fun disableVertexAttribArray(index: Int) {
        glDisableVertexAttribArray(index)
    }

    override fun genBuffers(): Int {
        // TODO experimental performance
        val buffer = BufferUtils.allocateInt(1)
        glGenBuffers(1, buffer.get())
        return buffer[0]
    }

    override fun genBuffers(n: Int, buffers: IntBuf) {
        glGenBuffers(n, buffers.get())
    }

    override fun bindBuffer(target: OpenGL.BufferType, buffer: BufferId) {
        glBindBuffer(target.value, buffer)
    }

    override fun bufferData(target: OpenGL.BufferType, data: Buffer, usage: OpenGL.DrawType) {
        glBufferData(
            target.value,
            data.size * data.sizeBytes(),
            data.get(),
            usage.value
        )
    }

    override fun genVertexArrays(): Int {
        // TODO dispose buffer after?
        val buffer = BufferUtils.allocateInt(1)
        glGenVertexArrays(1, buffer.get())
        return buffer[0]
    }

    override fun deleteBuffers(n: Int, buffers: IntBuf) {
        glDeleteBuffers(n, buffers.get())
    }

    override fun deleteVertexArrays(arrays: Int) {
        // TODO experimental performance
        val buffer = BufferUtils.allocateInt(1)
        buffer[0] = arrays
        glDeleteVertexArrays(1, buffer.get())
    }

    override fun deleteVertexArrays(n: Int, arrays: IntBuf) {
        glDeleteVertexArrays(n, arrays.get())
    }

    override fun deleteTextures(textures: Int) {
        // TODO experimental performance
        val buffer = BufferUtils.allocateInt(1)
        buffer[0] = textures
        glDeleteTextures(1, buffer.get())
    }

    override fun deleteTextures(n: Int, textures: IntBuf) {
        glDeleteTextures(n, textures.get())
    }

    override fun deleteFrameBuffers(frameBuffer: Int) {
        // TODO experimental performance
        val buffer = BufferUtils.allocateInt(1)
        buffer[0] = frameBuffer
        glDeleteFramebuffers(1, buffer.get())
    }

    override fun deleteRenderBuffers(renderBuffer: Int) {
        // TODO experimental performance
        val buffer = BufferUtils.allocateInt(1)
        buffer[0] = renderBuffer
        glDeleteRenderbuffers(1, buffer.get())
    }

    override fun genVertexArrays(n: Int, arrays: IntBuf) {
        glGenVertexArrays(n, arrays.get())
    }

    override fun bindVertexArray(array: ArrayId) {
        glBindVertexArray(array)
    }

    override fun drawElements(mode: OpenGL.DrawMode, count: Int, type: Int, indices: Buffer?) {
        if (indices == null) {
            glDrawElements(mode.value, count, type, 0)
        } else {
            glDrawElements(mode.value, count, type, indices.get())
        }
    }

    override fun genTextures(): Int {
        // TODO experimental performance
        val buffer = BufferUtils.allocateInt(1)
        glGenTextures(1, buffer.get())
        return buffer[0]
    }

    override fun genTextures(n: Int, textures: IntBuf) {
        glGenTextures(n, textures.get())
    }

    override fun bindTexture(target: Int, texture: Int) {
        glBindTexture(target, texture)
    }

    override fun activeTexture(texture: Int) {
        glActiveTexture(texture)
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
        glReadPixels(x, y, width, height, format, type, pixels.get())
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
        glTexImage2D(
            target,
            level,
            internalFormat,
            width,
            height,
            border,
            format,
            type,
            buffer?.get()
        )
    }

    override fun texImage2D(
        target: Int,
        level: Int,
        internalFormat: Int,
        bitmap: Bitmap,
        border: Int
    ) {
        val buffer = createIntBuffer(bitmap.pixels)
        glTexImage2D(
            target,
            level,
            internalFormat,
            bitmap.width,
            bitmap.height,
            border,
            internalFormat.toFormat(),
            GL_UNSIGNED_BYTE,
            buffer.get()
        )
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
        glTexSubImage2D(
            target,
            level,
            xOffset,
            yOffset,
            width,
            height,
            format,
            type,
            buffer?.get()
        )
    }

    override fun texSubImage2D(
        target: Int,
        level: Int,
        xOffset: Int,
        yOffset: Int,
        bitmap: Bitmap,
        format: Int
    ) {
        val buffer = createIntBuffer(bitmap.pixels)
        glTexSubImage2D(
            target,
            level,
            xOffset,
            yOffset,
            bitmap.width,
            bitmap.height,
            format,
            buffer.toUType(),
            buffer.get()
        )
    }

    override fun texParameteri(target: Int, pname: Int, param: Int) {
        glTexParameteri(target, pname, param)
    }

    override fun generateMipmap(target: Int) {
        glGenerateMipmap(target)
    }

    override fun uniform(location: Int, x: Int) {
        glUniform1i(location, x)
    }

    override fun uniform(location: Int, x: Float) {
        glUniform1f(location, x)
    }

    override fun uniform(location: Int, x: Int, y: Int, z: Int) {
        glUniform3i(location, x, y, z)
    }

    override fun uniform(location: Int, x: Float, y: Float, z: Float) {
        glUniform3f(location, x, y, z)
    }

    override fun uniform(location: Int, x: Float, y: Float, z: Float, w: Float) {
        glUniform4f(location, x, y, z, w)
    }

    override fun uniformMatrix4fv(
        location: Int,
        count: Int,
        transpose: Boolean,
        value: FloatBuf
    ) {
        glUniformMatrix4fv(location, count, transpose, value.get())
    }

    override fun genFrameBuffers(): Int {
        val buffer = BufferUtils.allocateInt(1)
        glGenFramebuffers(1, buffer.get())
        return buffer[0]
    }

    override fun bindFramebuffer(target: Int, frameBuffer: Int) {
        glBindFramebuffer(target, frameBuffer)
    }

    override fun framebufferTexture2D(
        target: Int,
        attachment: Int,
        texTarget: Int,
        texture: Int,
        level: Int
    ) {
        glFramebufferTexture2D(target, attachment, texTarget, texture, level)
    }

    override fun genRenderBuffers(): Int {
        val buffer = BufferUtils.allocateInt(1)
        glGenRenderbuffers(1, buffer.get())
        return buffer[0]
    }

    override fun bindRenderBuffers(target: Int, renderBuffer: Int) {
        glBindRenderbuffer(target, renderBuffer)
    }

    override fun renderBufferStorage(target: Int, internalFormat: Int, width: Int, height: Int) {
        glRenderbufferStorage(target, internalFormat, width, height)
    }

    override fun framebufferRenderBuffer(
        target: Int,
        attachment: Int,
        renderBufferTarget: Int,
        renderBuffer: Int
    ) {
        glFramebufferRenderbuffer(target, attachment, renderBufferTarget, renderBuffer)
    }

    override fun checkFramebufferStatus(target: Int): Int {
        return glCheckFramebufferStatus(target)
    }

    override fun cullFace(face: Int) {
        glCullFace(face)
    }

    override fun frontFace(mode: Int) {
        glFrontFace(mode)
    }

    override fun enable(cap: Int) {
        glEnable(cap)
    }

    override fun disable(cap: Int) {
        glDisable(cap)
    }

    override fun blendFunc(sFactor: Int, dFactor: Int) {
        glBlendFunc(sFactor, dFactor)
    }

    override fun deleteBuffers(buffers: Int) {
        val buffer = BufferUtils.allocateInt(1)
        buffer[0] = buffers
        glDeleteBuffers(1, buffer.get())
    }

    override fun getError(): Int {
        return glGetError()
    }
}