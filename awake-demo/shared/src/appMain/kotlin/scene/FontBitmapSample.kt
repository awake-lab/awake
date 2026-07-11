// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package scene

import io.github.ronjunevaldoz.awake.core.AwakeContext.Companion.gl
import io.github.ronjunevaldoz.awake.core.fonts.createTrueType
import io.github.ronjunevaldoz.awake.core.geometry.Attribute
import io.github.ronjunevaldoz.awake.core.getPlatform
import io.github.ronjunevaldoz.awake.core.graphics.Disposable
import io.github.ronjunevaldoz.awake.core.graphics.Drawable
import io.github.ronjunevaldoz.awake.core.graphics.opengl.CommonGL
import io.github.ronjunevaldoz.awake.core.graphics.opengl.OpenGL
import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.core.rendering.Texture
import io.github.ronjunevaldoz.awake.core.rendering.VertexArrayObject
import io.github.ronjunevaldoz.awake.core.rendering.addIndexBuffer
import io.github.ronjunevaldoz.awake.core.rendering.addVertexBuffer
import io.github.ronjunevaldoz.awake.core.rendering.createVAO
import io.github.ronjunevaldoz.awake.core.rendering.use
import io.github.ronjunevaldoz.awake.core.shader.SimpleShader
import io.github.ronjunevaldoz.awake.core.shader.use
import io.github.ronjunevaldoz.awake.core.utils.Frame
import io.github.ronjunevaldoz.awake.core.utils.Time


class FontBitmapSample : Drawable, Disposable {

    private lateinit var shader: SimpleShader

    private val aPosition by lazy { shader.position }
    private val aTexCoords by lazy { shader.texCoords }
    private val aColor by lazy { shader.color }

    private var attributes: List<Attribute> = emptyList()
    private lateinit var vao: VertexArrayObject
    private var fontTexture: Texture? = null

    private var totalElapsedTime = 0f

    private var updateCamera = true
    private val indices = byteArrayOf(
        0, 1, 2,
        2, 3, 0
    )
    private val ttf = createTrueType("assets/fonts/roboto.ttf", 24f)

    suspend fun load() {
        shader = SimpleShader.create(
            vertFile = "fonttext.vert",
            fragFile = "fonttext.frag",
            define = if (getPlatform().isMobile) "#version 300 es\n" else "#version 330 core\n"
        )
        shader.compile()
        fontTexture = ttf.texture
    }

    override fun draw() {
        if (attributes.isEmpty()) {
            createBuffers()
        }
        updateCamera()
        totalElapsedTime += Time.Delta.toFloat()
        if (totalElapsedTime >= 0.25f) {
            totalElapsedTime = 0f
        }
        shader.use {
            fontTexture?.let { texture ->
                vao.use {
//                    gl.disable(CommonGL.GL_CULL_FACE)
                    gl.enable(CommonGL.GL_BLEND)
                    gl.blendFunc(CommonGL.GL_SRC_ALPHA, CommonGL.GL_ONE_MINUS_SRC_ALPHA)
                    gl.activeTexture(CommonGL.GL_TEXTURE0)
                    texture.use {
                        gl.drawElements(
                            OpenGL.DrawMode.Triangles,
                            indices.size,
                            CommonGL.GL_UNSIGNED_BYTE,
                            null
                        )
                    }
                    gl.disable(CommonGL.GL_BLEND)
                }
            }
        }
    }

    override fun dispose() {
        ttf.dispose()
//        fontTexture?.delete()
        shader.delete()
        vao.delete()
    }

    private fun updateCamera() {
        if (updateCamera) {
            shader.use {
                if (Frame.width > 0 && Frame.height > 0) {
                    val width = Frame.width.toFloat()
                    val height = Frame.height.toFloat()
                    val projection = Mat4.orthographic(
                        left = 0f,
                        right = width,
                        bottom = 0f,
                        top = height,
                        near = -1f,
                        far = 1f
                    )
                    shader.projectionViewMatrix = projection
                    shader.modelViewMatrix = Mat4()
                }
            }
            updateCamera = false
        }
    }


    private fun createBuffers() {
        val screenWidth = Frame.width.toFloat()
        val screenHeight = Frame.height.toFloat()
        if (fontTexture != null) {
            val textWidth = fontTexture!!.width.toFloat()
            val textHeight = fontTexture!!.height.toFloat()
            val x1 = (screenWidth - textWidth) / 2f
            val y1 = (screenHeight - textHeight) / 2f
            val x2 = x1 + textWidth
            val y2 = y1 + textHeight
            attributes = listOf(
                Attribute(
                    aPosition, floatArrayOf(
                        x1, y1,
                        x2, y1,
                        x2, y2,
                        x1, y2,
                    ), 2
                ),
                Attribute(
                    aColor, floatArrayOf(
                        1f, 1f, 1f,
                        1f, 1f, 1f,
                        1f, 1f, 1f,
                        1f, 1f, 1f,
                    ), 3
                ),
                Attribute(
                    aTexCoords, floatArrayOf(
                        0.0f, 0.0f, //  bottom-right
                        1.0f, 0.0f, //  top-right
                        1.0f, 1.0f, // bottom-left
                        0.0f, 1.0f  // top-left
                    ), 2
                ),
            )
            vao = createVAO {
                addIndexBuffer(indices)
                attributes.forEach(::addVertexBuffer)
            }
        }
    }
}