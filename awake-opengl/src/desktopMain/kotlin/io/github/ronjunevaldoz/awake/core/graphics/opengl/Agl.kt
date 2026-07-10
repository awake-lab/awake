package io.github.ronjunevaldoz.awake.core.graphics.opengl

import io.github.ronjunevaldoz.awake.core.graphics.Bitmap
import io.github.ronjunevaldoz.awake.core.memory.Buffer
import io.github.ronjunevaldoz.awake.core.memory.FloatBuf
import io.github.ronjunevaldoz.awake.core.memory.IntBuf
import io.github.ronjunevaldoz.awake.core.memory.`when`
import io.github.ronjunevaldoz.awake.core.rendering.toFormat
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.glCullFace
import org.lwjgl.opengl.GL11.glDeleteTextures
import org.lwjgl.opengl.GL11.glDisable
import org.lwjgl.opengl.GL11.glFrontFace
import org.lwjgl.opengl.GL11.glGetError
import org.lwjgl.opengl.GL11.glReadPixels
import org.lwjgl.opengl.GL11.glTexSubImage2D
import org.lwjgl.opengl.GL20.glActiveTexture
import org.lwjgl.opengl.GL20.glAttachShader
import org.lwjgl.opengl.GL20.glBindAttribLocation
import org.lwjgl.opengl.GL20.glBindBuffer
import org.lwjgl.opengl.GL20.glBindTexture
import org.lwjgl.opengl.GL20.glBufferData
import org.lwjgl.opengl.GL20.glClear
import org.lwjgl.opengl.GL20.glClearColor
import org.lwjgl.opengl.GL20.glCompileShader
import org.lwjgl.opengl.GL20.glCreateProgram
import org.lwjgl.opengl.GL20.glCreateShader
import org.lwjgl.opengl.GL20.glDeleteBuffers
import org.lwjgl.opengl.GL20.glDeleteProgram
import org.lwjgl.opengl.GL20.glDeleteShader
import org.lwjgl.opengl.GL20.glDisableVertexAttribArray
import org.lwjgl.opengl.GL20.glDrawArrays
import org.lwjgl.opengl.GL20.glDrawElements
import org.lwjgl.opengl.GL20.glEnableVertexAttribArray
import org.lwjgl.opengl.GL20.glGenBuffers
import org.lwjgl.opengl.GL20.glGenTextures
import org.lwjgl.opengl.GL20.glGetAttribLocation
import org.lwjgl.opengl.GL20.glGetProgramInfoLog
import org.lwjgl.opengl.GL20.glGetProgramiv
import org.lwjgl.opengl.GL20.glGetShaderInfoLog
import org.lwjgl.opengl.GL20.glGetShaderiv
import org.lwjgl.opengl.GL20.glGetUniformLocation
import org.lwjgl.opengl.GL20.glLinkProgram
import org.lwjgl.opengl.GL20.glShaderSource
import org.lwjgl.opengl.GL20.glTexImage2D
import org.lwjgl.opengl.GL20.glTexParameteri
import org.lwjgl.opengl.GL20.glUniform1f
import org.lwjgl.opengl.GL20.glUniform1i
import org.lwjgl.opengl.GL20.glUniform3f
import org.lwjgl.opengl.GL20.glUniform3i
import org.lwjgl.opengl.GL20.glUniform4f
import org.lwjgl.opengl.GL20.glUniformMatrix4fv
import org.lwjgl.opengl.GL20.glUseProgram
import org.lwjgl.opengl.GL20.glVertexAttribPointer
import org.lwjgl.opengl.GL20.glViewport
import org.lwjgl.opengl.GL30.glBindFramebuffer
import org.lwjgl.opengl.GL30.glBindRenderbuffer
import org.lwjgl.opengl.GL30.glBindVertexArray
import org.lwjgl.opengl.GL30.glCheckFramebufferStatus
import org.lwjgl.opengl.GL30.glDeleteFramebuffers
import org.lwjgl.opengl.GL30.glDeleteRenderbuffers
import org.lwjgl.opengl.GL30.glDeleteVertexArrays
import org.lwjgl.opengl.GL30.glFramebufferRenderbuffer
import org.lwjgl.opengl.GL30.glFramebufferTexture2D
import org.lwjgl.opengl.GL30.glGenFramebuffers
import org.lwjgl.opengl.GL30.glGenRenderbuffers
import org.lwjgl.opengl.GL30.glGenVertexArrays
import org.lwjgl.opengl.GL30.glGenerateMipmap
import org.lwjgl.opengl.GL30.glRenderbufferStorage

internal actual object Agl : OpenGL {
    override fun clearColor(r: Float, g: Float, b: Float, a: Float) {
        glClearColor(r, g, b, a)
    }

    override fun clear(mask: Int) {
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
        ptr?.`when`(
            byte = {
                glVertexAttribPointer(
                    index,
                    size,
                    type,
                    normalized,
                    stride,
                    this
                )
            },
            short = {
                glVertexAttribPointer(
                    index,
                    size,
                    type,
                    normalized,
                    stride,
                    this
                )
            },
            int = {
                glVertexAttribPointer(
                    index,
                    size,
                    type,
                    normalized,
                    stride,
                    this
                )
            },
            float = {
                glVertexAttribPointer(
                    index,
                    size,
                    type,
                    normalized,
                    stride,
                    this
                )
            }
        ) ?: glVertexAttribPointer(
            index,
            size,
            type,
            normalized,
            stride,
            0
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
            index,
            size,
            type,
            normalized,
            stride,
            offset.toLong()
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
        return glGenBuffers()
    }

    override fun genBuffers(n: Int, buffers: IntBuf) {
        glGenBuffers(buffers.get())
    }

    override fun bindBuffer(target: OpenGL.BufferType, buffer: BufferId) {
        glBindBuffer(target.value, buffer)
    }

    override fun bufferData(
        target: OpenGL.BufferType,
        data: Buffer,
        usage: OpenGL.DrawType
    ) {
        data.`when`(
            byte = {
                glBufferData(
                    target.value,
                    this,
                    usage.value
                )
            },
            short = {
                glBufferData(
                    target.value,
                    this,
                    usage.value
                )
            },
            int = {
                glBufferData(
                    target.value,
                    this,
                    usage.value
                )
            },
            float = {
                glBufferData(
                    target.value,
                    this,
                    usage.value
                )
            }
        )
    }

    override fun genVertexArrays(): Int {
        return glGenVertexArrays()
    }

    override fun deleteBuffers(n: Int, buffers: IntBuf) {
        glDeleteBuffers(buffers.get())
    }

    override fun deleteVertexArrays(arrays: Int) {
        glDeleteVertexArrays(arrays)
    }

    override fun deleteVertexArrays(n: Int, arrays: IntBuf) {
        glDeleteVertexArrays(arrays.get())
    }

    override fun deleteTextures(textures: Int) {
        glDeleteTextures(textures)
    }

    override fun deleteTextures(n: Int, textures: IntBuf) {
        glDeleteTextures(textures.get())
    }

    override fun deleteFrameBuffers(frameBuffer: Int) {
        glDeleteFramebuffers(frameBuffer)
    }

    override fun deleteRenderBuffers(renderBuffer: Int) {
        glDeleteRenderbuffers(renderBuffer)
    }

    override fun genVertexArrays(n: Int, arrays: IntBuf) {
        glGenVertexArrays(arrays.get())
    }

    override fun bindVertexArray(array: ArrayId) {
        glBindVertexArray(array)
    }

    override fun drawElements(mode: OpenGL.DrawMode, count: Int, type: Int, indices: Buffer?) {
        indices?.`when`(
            byte = { glDrawElements(mode.value, this) },
            short = { glDrawElements(mode.value, this) },
            int = { glDrawElements(mode.value, this) }
        ) ?: glDrawElements(mode.value, count, type, 0)
    }

    override fun genTextures(): Int {
        return glGenTextures()
    }

    override fun genTextures(n: Int, textures: IntBuf) {
        glGenTextures(textures.get())
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
        pixels.`when`(
            byte = { glReadPixels(x, y, width, height, format, type, this) },
            int = { glReadPixels(x, y, width, height, format, type, this) },
            short = { glReadPixels(x, y, width, height, format, type, this) },
            float = { glReadPixels(x, y, width, height, format, type, this) },
        )
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
        if (buffer == null) {
            glTexImage2D(
                target,
                level,
                internalFormat,
                width,
                height,
                border,
                format,
                type,
                0
            )
        } else {
            buffer.`when`(
                byte = {
                    glTexImage2D(
                        target,
                        level,
                        internalFormat,
                        width,
                        height,
                        border,
                        format,
                        type,
                        this
                    )
                },
                short = {
                    glTexImage2D(
                        target,
                        level,
                        internalFormat,
                        width,
                        height,
                        border,
                        format,
                        type,
                        this
                    )
                },
                int = {
                    glTexImage2D(
                        target,
                        level,
                        internalFormat,
                        width,
                        height,
                        border,
                        format,
                        type,
                        this
                    )
                },
                float = {
                    glTexImage2D(
                        target,
                        level,
                        internalFormat,
                        width,
                        height,
                        border,
                        format,
                        type,
                        this
                    )
                }
            )
        }
    }

    override fun texImage2D(
        target: Int,
        level: Int,
        internalFormat: Int,
        bitmap: Bitmap,
        border: Int
    ) {
        glTexImage2D(
            target,
            level,
            internalFormat,
            bitmap.width,
            bitmap.height,
            border,
            internalFormat.toFormat(),
            CommonGL.GL_UNSIGNED_INT,
            bitmap.pixels
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
        buffer?.`when`(
            byte = {
                glTexSubImage2D(
                    target,
                    level,
                    xOffset,
                    yOffset,
                    width,
                    height,
                    format,
                    type,
                    this
                )
            },
            int = {
                glTexSubImage2D(
                    target,
                    level,
                    xOffset,
                    yOffset,
                    width,
                    height,
                    format,
                    type,
                    this
                )
            },
            short = {
                glTexSubImage2D(
                    target,
                    level,
                    xOffset,
                    yOffset,
                    width,
                    height,
                    format,
                    type,
                    this
                )
            },
            float = {
                glTexSubImage2D(
                    target,
                    level,
                    xOffset,
                    yOffset,
                    width,
                    height,
                    format,
                    type,
                    this
                )
            }
        ) ?: glTexSubImage2D(
            target,
            level,
            xOffset,
            yOffset,
            width,
            height,
            format,
            type,
            0
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
            target,
            level,
            xOffset,
            yOffset,
            bitmap.width,
            bitmap.height,
            format,
            CommonGL.GL_UNSIGNED_INT,
            bitmap.pixels
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
        glUniformMatrix4fv(location, transpose, value.get())
    }

    override fun genFrameBuffers(): Int {
        return glGenFramebuffers()
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
        return glGenRenderbuffers()
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
        GL11.glEnable(cap)
    }

    override fun disable(cap: Int) {
        glDisable(cap)
    }

    override fun blendFunc(sFactor: Int, dFactor: Int) {
        GL11.glBlendFunc(sFactor, dFactor)
    }

    override fun deleteBuffers(buffers: Int) {
        glDeleteBuffers(buffers)
    }

    override fun getError(): Int {
        return glGetError()
    }
}