// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package scene

import io.github.ronjunevaldoz.awake.core.AwakeContext.Companion.gl
import io.github.ronjunevaldoz.awake.core.geometry.Attribute
import io.github.ronjunevaldoz.awake.core.getPlatform
import io.github.ronjunevaldoz.awake.core.graphics.Disposable
import io.github.ronjunevaldoz.awake.core.graphics.Drawable
import io.github.ronjunevaldoz.awake.core.graphics.opengl.CommonGL
import io.github.ronjunevaldoz.awake.core.graphics.opengl.OpenGL
import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.core.math.angleRad
import io.github.ronjunevaldoz.awake.core.rendering.VertexArrayObject
import io.github.ronjunevaldoz.awake.core.rendering.addIndexBuffer
import io.github.ronjunevaldoz.awake.core.rendering.addVertexBuffer
import io.github.ronjunevaldoz.awake.core.rendering.createVAO
import io.github.ronjunevaldoz.awake.core.rendering.use
import io.github.ronjunevaldoz.awake.core.shader.SimpleShader
import io.github.ronjunevaldoz.awake.core.shader.use
import io.github.ronjunevaldoz.awake.core.utils.AssetUtils
import io.github.ronjunevaldoz.awake.core.utils.Time

class TransformTriangle : Drawable, Disposable {

    private lateinit var shader: SimpleShader

    private val aPosition by lazy { shader.position }
    private val aTexCoords by lazy { shader.texCoords }
    private val aColor by lazy { shader.color }
    private val attributes by lazy {
        listOf(
            Attribute(
                aPosition, floatArrayOf(
                    1f, -1f, 0f,
                    1f, 1f, 0f,
                    -1f, 1f, 0f,
                    -1f, -1f, 0f
                ), 3
            ),
            Attribute(
                aTexCoords, floatArrayOf(
                    1.0f, 1.0f,
                    1.0f, 0.0f,
                    0.0f, 0.0f,
                    0.0f, 1.0f
                ), 2
            ),
            Attribute(
                aColor, floatArrayOf(
                    1f, 0f, 0f,
                    0f, 1f, 0f,
                    0f, 0f, 1f,
                    0f, 0f, 0f,
                ), 3
            ),
        )
    }

    private val indices = byteArrayOf(
        0, 1, 3, // first triangle
        1, 2, 3  // second triangle
    )
    private lateinit var vao: VertexArrayObject
    private var angle = 0f
    private var previousAngle = 0f
    private val trans = Mat4()

    suspend fun load() {
        shader = SimpleShader.create(
            vertFile = "transform.vert",
            fragFile = "transform.frag",
            define = if (getPlatform().isMobile) "#version 300 es\n" else "#version 330 core\n"
        )
        shader.compile()
        AssetUtils.texture.load(DemoTexture.textureKey, "assets/fonts/calibri.png")
        createBuffers()
    }

    override fun draw() {
        shader.use {
            previousAngle = angle
            angle += Time.Delta.toFloat() * 1.angleRad
            shader.transformMatrix = trans
                .translate(0f, 0f, 0f)
                .scale(0.5f, 0.5f, 0.5f)
                .rotateX(angle)
                .rotateY(angle)
                .rotateZ(angle)

            vao.use {
                gl.activeTexture(CommonGL.GL_TEXTURE0)
                gl.bindTexture(
                    CommonGL.GL_TEXTURE_2D, AssetUtils.texture.get(
                        DemoTexture.textureKey
                    )
                )
                gl.drawElements(
                    OpenGL.DrawMode.Triangles,
                    indices.size,
                    CommonGL.GL_UNSIGNED_BYTE,
                    null
                )
                gl.bindTexture(
                    CommonGL.GL_TEXTURE_2D, 0
                )
            }
        }
    }

    override fun dispose() {
        shader.delete()
        vao.delete()
    }


    private fun createBuffers() {
        vao = createVAO {
            addIndexBuffer(indices)
            attributes.forEach(::addVertexBuffer)
        }
    }
}