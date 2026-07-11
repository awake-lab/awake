// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package scene

import io.github.ronjunevaldoz.awake.core.AwakeContext.Companion.gl
import io.github.ronjunevaldoz.awake.core.geometry.Attribute
import io.github.ronjunevaldoz.awake.core.graphics.Disposable
import io.github.ronjunevaldoz.awake.core.graphics.Drawable
import io.github.ronjunevaldoz.awake.core.graphics.opengl.CommonGL.GL_TEXTURE0
import io.github.ronjunevaldoz.awake.core.graphics.opengl.CommonGL.GL_TEXTURE_2D
import io.github.ronjunevaldoz.awake.core.graphics.opengl.CommonGL.GL_UNSIGNED_BYTE
import io.github.ronjunevaldoz.awake.core.graphics.opengl.OpenGL
import io.github.ronjunevaldoz.awake.core.isMobile
import io.github.ronjunevaldoz.awake.core.rendering.VertexArrayObject
import io.github.ronjunevaldoz.awake.core.rendering.addIndexBuffer
import io.github.ronjunevaldoz.awake.core.rendering.addVertexBuffer
import io.github.ronjunevaldoz.awake.core.rendering.createVAO
import io.github.ronjunevaldoz.awake.core.rendering.use
import io.github.ronjunevaldoz.awake.core.shader.SimpleShader
import io.github.ronjunevaldoz.awake.core.shader.use
import io.github.ronjunevaldoz.awake.core.utils.AssetUtils


/**
 * Draw texture using elements
 */
class DemoTexture : Drawable, Disposable {
    private lateinit var shader: SimpleShader

    private val aPosition by lazy { shader.position }
    private val aColor by lazy { shader.color }
    private val aTexCoords by lazy { shader.texCoords }

    private var vao = VertexArrayObject()

    private lateinit var attributes: List<Attribute>
    private val indices = byteArrayOf(
        0, 1, 2, // first triangle
        0, 3, 2  // second triangle
    )

    suspend fun load() {
        shader = SimpleShader.create(
            vertFile = if (isMobile) "text.mobile.vert" else "text.vert",
            fragFile = if (isMobile) "text.mobile.frag" else "text.frag"
        )
        shader.compile()
        AssetUtils.texture.load(textureKey, "assets/fonts/calibri.png")
        attributes = listOf(
            Attribute(
                aPosition, floatArrayOf(
                    1f, 1f, 0.0f,// top right
                    1.0f, -1f, 0.0f, // bottom right
                    -1f, -1f, 0.0f,
                    -1f, 1f, 0.0f
                ), 3
            ),
            Attribute(
                aColor, floatArrayOf(
                    1.0f, 0.0f, 0.0f,
                    0.0f, 1.0f, 0.0f,
                    0.0f, 0.0f, 1.0f,
                    1.0f, 1.0f, 0.0f
                ), 3
            ),
            Attribute(
                aTexCoords, floatArrayOf(
                    1.0f, 1.0f,
                    1.0f, 0.0f,
                    0.0f, 0.0f,
                    0.0f, 1.0f
                ), 2
            )
        )
        createBuffers()
    }

    private fun createBuffers() {
        vao = createVAO {
            addIndexBuffer(indices)
            attributes.forEach(::addVertexBuffer)

        }
    }

    override fun draw() {
        shader.use {
            vao.use {
                gl.activeTexture(GL_TEXTURE0)
                gl.bindTexture(GL_TEXTURE_2D, AssetUtils.texture.get(textureKey))
                gl.drawElements(
                    OpenGL.DrawMode.Triangles,
                    indices.size,
                    GL_UNSIGNED_BYTE,
                    null
                )
                gl.bindTexture(GL_TEXTURE_2D, 0)
            }
        }
    }

    override fun dispose() {
        shader.delete()
        vao.delete()
        AssetUtils.texture.remove(textureKey)
    }

    companion object {
        const val textureKey = "texture"
    }
}