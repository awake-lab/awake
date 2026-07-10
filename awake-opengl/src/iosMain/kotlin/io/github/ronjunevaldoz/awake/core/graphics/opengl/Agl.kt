package io.github.ronjunevaldoz.awake.core.graphics.opengl

import io.github.ronjunevaldoz.awake.core.graphics.Bitmap
import io.github.ronjunevaldoz.awake.core.memory.Buffer
import io.github.ronjunevaldoz.awake.core.memory.ByteBuffer
import io.github.ronjunevaldoz.awake.core.memory.FloatBuf
import io.github.ronjunevaldoz.awake.core.memory.FloatBuffer
import io.github.ronjunevaldoz.awake.core.memory.IntBuf
import io.github.ronjunevaldoz.awake.core.memory.IntBuffer
import io.github.ronjunevaldoz.awake.core.memory.ShortBuffer
import io.github.ronjunevaldoz.awake.core.rendering.toFormat
import io.github.ronjunevaldoz.awake.core.utils.BufferUtils
import io.github.ronjunevaldoz.awake.core.utils.sizeBytes
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.FloatVar
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.ShortVar
import kotlinx.cinterop.cValuesOf
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.refTo
import kotlinx.cinterop.toCPointer
import platform.gles.glBindFramebufferOES
import platform.gles.glBindRenderbufferOES
import platform.gles.glBlendFunc
import platform.gles.glCheckFramebufferStatusOES
import platform.gles.glCullFace
import platform.gles.glDeleteFramebuffersOES
import platform.gles.glDisable
import platform.gles.glEnable
import platform.gles.glFramebufferRenderbufferOES
import platform.gles.glFramebufferTexture2DOES
import platform.gles.glGenFramebuffersOES
import platform.gles.glGenRenderbuffersOES
import platform.gles.glReadPixels
import platform.gles.glRenderbufferStorageOES
import platform.gles.glTexSubImage2D
import platform.gles2.GL_FALSE
import platform.gles2.GL_INFO_LOG_LENGTH
import platform.gles2.GL_TRUE
import platform.gles2.GL_UNSIGNED_BYTE
import platform.gles2.glActiveTexture
import platform.gles2.glAttachShader
import platform.gles2.glBindAttribLocation
import platform.gles2.glBindBuffer
import platform.gles2.glBindTexture
import platform.gles2.glBindVertexArrayOES
import platform.gles2.glBufferData
import platform.gles2.glClear
import platform.gles2.glClearColor
import platform.gles2.glCompileShader
import platform.gles2.glCreateProgram
import platform.gles2.glCreateShader
import platform.gles2.glDeleteBuffers
import platform.gles2.glDeleteProgram
import platform.gles2.glDeleteShader
import platform.gles2.glDeleteTextures
import platform.gles2.glDeleteVertexArraysOES
import platform.gles2.glDisableVertexAttribArray
import platform.gles2.glDrawArrays
import platform.gles2.glDrawElements
import platform.gles2.glEnableVertexAttribArray
import platform.gles2.glFrontFace
import platform.gles2.glGenBuffers
import platform.gles2.glGenTextures
import platform.gles2.glGenVertexArraysOES
import platform.gles2.glGenerateMipmap
import platform.gles2.glGetAttribLocation
import platform.gles2.glGetError
import platform.gles2.glGetProgramInfoLog
import platform.gles2.glGetProgramiv
import platform.gles2.glGetShaderInfoLog
import platform.gles2.glGetShaderiv
import platform.gles2.glGetUniformLocation
import platform.gles2.glLinkProgram
import platform.gles2.glShaderSource
import platform.gles2.glTexImage2D
import platform.gles2.glTexParameteri
import platform.gles2.glUniform1f
import platform.gles2.glUniform1i
import platform.gles2.glUniform3f
import platform.gles2.glUniform3i
import platform.gles2.glUniform4f
import platform.gles2.glUniformMatrix4fv
import platform.gles2.glUseProgram
import platform.gles2.glVertexAttribPointer
import platform.gles2.glViewport

internal actual object Agl : OpenGL {

    override fun clearColor(r: Float, g: Float, b: Float, a: Float) {
        glClearColor(r, g, b, a)
    }

    override fun clear(mask: Int) {
        glClear(mask.toUInt())
    }

    override fun viewport(x: Int, y: Int, width: Int, height: Int) {
        glViewport(x, y, width, height)
    }

    override fun createShader(type: OpenGL.ShaderType): Int {
        return glCreateShader(type.value.toUInt()).toInt()
    }

    override fun shaderSource(shader: Int, content: String) {
        memScoped {
            glShaderSource(shader.toUInt(), 1, cValuesOf(content.cstr.ptr), null)
        }
    }

    override fun compileShader(shader: Int) {
        glCompileShader(shader.toUInt())
    }

    override fun getShaderInfoLog(shader: Int): String {
        val logLengthBuffer = IntArray(1)
        glGetShaderiv(shader.toUInt(), GL_INFO_LOG_LENGTH, logLengthBuffer.refTo(0))
        val logLength = logLengthBuffer[0]

        return if (logLength > 0) {
            val logBuffer = ByteArray(logLength)
            glGetShaderInfoLog(shader.toUInt(), logLength, null, logBuffer.refTo(0))
            logBuffer.decodeToString()
        } else {
            ""
        }
    }

    override fun getShaderiv(shader: Int, status: OpenGL.Status, buffer: IntBuf) {
        glGetShaderiv(shader.toUInt(), status.value.toUInt(), buffer.get())
    }

    override fun createProgram(): Int {
        return glCreateProgram().toInt()
    }

    override fun attachShader(program: Int, shader: Int) {
        glAttachShader(program.toUInt(), shader.toUInt())
    }

    override fun linkProgram(program: Int) {
        glLinkProgram(program.toUInt())
    }

    override fun getProgramInfoLog(program: Int): String {
        val logLengthBuffer = IntArray(1)
        glGetProgramiv(program.toUInt(), GL_INFO_LOG_LENGTH, logLengthBuffer.refTo(0))
        val logLength = logLengthBuffer[0]

        val logBuffer = ByteArray(logLength)
        glGetProgramInfoLog(program.toUInt(), logLength, null, logBuffer.refTo(0))
        return logBuffer.decodeToString()
    }

    override fun getProgramiv(program: Int, status: OpenGL.Status, buffer: IntBuf) {
        glGetProgramiv(program.toUInt(), status.value.toUInt(), buffer.get())
    }

    override fun deleteShader(shader: Int) {
        glDeleteShader(shader.toUInt())
    }

    override fun useProgram(program: Int) {
        glUseProgram(program.toUInt())
    }

    override fun deleteProgram(program: Int) {
        glDeleteProgram(program.toUInt())
    }

    override fun getUniformLocation(program: Int, name: String): Int {
        return glGetUniformLocation(program.toUInt(), name)
    }

    override fun bindAttribLocation(program: ProgramId, index: Int, name: String) {
        glBindAttribLocation(program.toUInt(), index.toUInt(), name)
    }

    override fun getAttribLocation(program: Int, name: String): Int {
        return glGetAttribLocation(program.toUInt(), name)
    }

    override fun vertexAttribPointer(
        index: Int,
        size: Int,
        type: Int,
        normalized: Boolean,
        stride: Int,
        ptr: Buffer?
    ) {
        val pointer = ptr?.get() ?: 0.toLong().toCPointer<ByteVar>()
        glVertexAttribPointer(
            indx = index.toUInt(),
            size = size,
            type = type.toUInt(),
            normalized = if (normalized) GL_TRUE.toUByte() else GL_FALSE.toUByte(),
            stride = stride,
            ptr = pointer
        )
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
            indx = index.toUInt(),
            size = size,
            type = type.toUInt(),
            normalized = if (normalized) GL_TRUE.toUByte() else GL_FALSE.toUByte(),
            stride = stride,
            ptr = offset.toLong().toCPointer<ByteVar>()
        )
    }

    override fun enableVertexAttribArray(index: Int) {
        glEnableVertexAttribArray(index.toUInt())
    }

    override fun drawArrays(mode: OpenGL.DrawMode, first: Int, count: Int) {
        glDrawArrays(mode.value.toUInt(), first, count)
    }

    override fun disableVertexAttribArray(index: Int) {
        glDisableVertexAttribArray(index.toUInt())
    }

    override fun genBuffers(): Int {
        val buffer = BufferUtils.allocateInt(1)
        glGenBuffers(1, buffer.get())
        return buffer[0]
    }

    override fun genBuffers(n: Int, buffers: IntBuf) {
        glGenBuffers(n, buffers.get())
    }

    override fun bindBuffer(target: OpenGL.BufferType, buffer: BufferId) {
        glBindBuffer(target.value.toUInt(), buffer.toUInt())
    }

    override fun bufferData(target: OpenGL.BufferType, data: Buffer, usage: OpenGL.DrawType) {
        val buffer = when (data) {
            is ByteBuffer -> data.get<ByteVar>()
            is ShortBuffer -> data.get<ShortVar>()
            is IntBuffer -> data.get<IntVar>()
            is FloatBuffer -> data.get<FloatVar>()
            else -> throw Exception("Buffer not supported")
        }
        glBufferData(
            target.value.toUInt(),
            data.size * data.sizeBytes().toLong(),
            buffer,
            usage.value.toUInt()
        )
    }

    override fun genVertexArrays(): Int {
        val buffer = BufferUtils.allocateInt(1)
        glGenVertexArraysOES(1, buffer.get())
        return buffer[0]
    }

    override fun deleteBuffers(n: Int, buffers: IntBuf) {
        glDeleteBuffers(n, buffers.get())
    }

    override fun deleteVertexArrays(arrays: Int) {
        val buffer = BufferUtils.allocateInt(1)
        buffer[0] = arrays
        glDeleteVertexArraysOES(1, buffer.get())
    }

    override fun deleteVertexArrays(n: Int, arrays: IntBuf) {
        glDeleteVertexArraysOES(n, arrays.get())
    }

    override fun deleteTextures(textures: Int) {
        val buffer = BufferUtils.allocateInt(1)
        buffer[0] = textures
        glDeleteTextures(1, buffer.get())
    }

    override fun deleteTextures(n: Int, textures: IntBuf) {
        glDeleteTextures(n, textures.get())
    }

    override fun deleteFrameBuffers(frameBuffer: Int) {
        val buffer = BufferUtils.allocateInt(1)
        buffer[0] = frameBuffer
        glDeleteFramebuffersOES(1, buffer.get())
    }

    override fun deleteRenderBuffers(renderBuffer: Int) {
        val buffer = BufferUtils.allocateInt(1)
        buffer[0] = renderBuffer
        glDeleteFramebuffersOES(1, buffer.get())
    }

    override fun genVertexArrays(n: Int, arrays: IntBuf) {
        glGenVertexArraysOES(n, arrays.get())
    }

    override fun bindVertexArray(array: ArrayId) {
        glBindVertexArrayOES(array.toUInt())
    }

    override fun drawElements(mode: OpenGL.DrawMode, count: Int, type: Int, indices: Buffer?) {
        val buffer = indices?.get() ?: 0.toLong().toCPointer<ByteVar>()
        glDrawElements(mode.value.toUInt(), count, type.toUInt(), buffer)
    }

    override fun genTextures(): Int {
        val buffer = BufferUtils.allocateInt(1)
        glGenTextures(1, buffer.get())
        return buffer[0]
    }

    override fun genTextures(n: Int, textures: IntBuf) {
        glGenTextures(n, textures.get())
    }

    override fun bindTexture(target: Int, texture: Int) {
        glBindTexture(target.toUInt(), texture.toUInt())
    }

    override fun activeTexture(texture: Int) {
        glActiveTexture(texture.toUInt())
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
        val pixelBuffer = pixels.get<ByteVar>()
        glReadPixels(x, y, width, height, format.toUInt(), type.toUInt(), pixelBuffer)
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
        val pixelBuffer = buffer?.get() ?: 0.toLong().toCPointer<ByteVar>()
        glTexImage2D(
            target.toUInt(),
            level,
            internalFormat,
            width,
            height,
            border,
            format.toUInt(),
            type.toUInt(),
            pixelBuffer
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
        val pixelBuffer = buffer?.get() ?: 0.toLong().toCPointer<ByteVar>()
        glTexSubImage2D(
            target = target.toUInt(),
            level = level,
            xoffset = xOffset,
            yoffset = yOffset,
            width = width,
            height = height,
            format = format.toUInt(),
            type = GL_UNSIGNED_BYTE,
            pixels = pixelBuffer
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
        glTexSubImage2D(
            target = target.toUInt(),
            level = level,
            xoffset = xOffset,
            yoffset = yOffset,
            width = bitmap.width,
            height = bitmap.height,
            format = format.toUInt(),
            type = GL_UNSIGNED_BYTE,
            pixels = bitmap.pixels.refTo(0)
        )
    }

    override fun texImage2D(
        target: Int,
        level: Int,
        internalFormat: Int,
        bitmap: Bitmap,
        border: Int
    ) {
        glTexImage2D(
            target = target.toUInt(),
            level = level,
            internalformat = internalFormat,
            width = bitmap.width,
            height = bitmap.height,
            border = 0,
            format = internalFormat.toFormat().toUInt(),
            type = GL_UNSIGNED_BYTE,
            pixels = bitmap.pixels.refTo(0)
        )
    }

    override fun texParameteri(target: Int, pname: Int, param: Int) {
        glTexParameteri(target.toUInt(), pname.toUInt(), param)
    }

    override fun generateMipmap(target: Int) {
        glGenerateMipmap(target.toUInt())
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
        glUniformMatrix4fv(
            location,
            count,
            if (transpose) GL_TRUE.toUByte() else GL_FALSE.toUByte(),
            value.get()
        )
    }

    override fun genFrameBuffers(): Int {
        val buffer = BufferUtils.allocateInt(1)
        glGenFramebuffersOES(1, buffer.get())
        return buffer[0]
    }

    override fun bindFramebuffer(target: Int, frameBuffer: Int) {
        glBindFramebufferOES(target.toUInt(), frameBuffer.toUInt())
    }

    override fun framebufferTexture2D(
        target: Int,
        attachment: Int,
        texTarget: Int,
        texture: Int,
        level: Int
    ) {
        glFramebufferTexture2DOES(
            target.toUInt(),
            attachment.toUInt(),
            texTarget.toUInt(),
            texture.toUInt(),
            level
        )
    }

    override fun genRenderBuffers(): Int {
        val buffer = BufferUtils.allocateInt(1)
        glGenRenderbuffersOES(1, buffer.get())
        return buffer[0]
    }

    override fun bindRenderBuffers(target: Int, renderBuffer: Int) {
        glBindRenderbufferOES(target.toUInt(), renderBuffer.toUInt())
    }

    override fun renderBufferStorage(target: Int, internalFormat: Int, width: Int, height: Int) {
        glRenderbufferStorageOES(target.toUInt(), internalFormat.toUInt(), width, height)
    }

    override fun framebufferRenderBuffer(
        target: Int,
        attachment: Int,
        renderBufferTarget: Int,
        renderBuffer: Int
    ) {
        glFramebufferRenderbufferOES(
            target.toUInt(),
            attachment.toUInt(),
            renderBufferTarget.toUInt(),
            renderBuffer.toUInt()
        )
    }

    override fun checkFramebufferStatus(target: Int): Int {
        return glCheckFramebufferStatusOES(target.toUInt()).toInt()
    }

    override fun cullFace(face: Int) {
        glCullFace(face.toUInt())
    }

    override fun frontFace(mode: Int) {
        glFrontFace(mode.toUInt())
    }

    override fun enable(cap: Int) {
        glEnable(cap.toUInt())
    }

    override fun disable(cap: Int) {
        glDisable(cap.toUInt())
    }

    override fun blendFunc(sFactor: Int, dFactor: Int) {
        glBlendFunc(sFactor.toUInt(), dFactor.toUInt())
    }

    override fun deleteBuffers(buffers: Int) {
        val buffer = BufferUtils.allocateInt(1)
        buffer[0] = buffers
        glDeleteBuffers(1, buffer.get())
    }

    override fun getError(): Int {
        return glGetError().toInt()
    }
}