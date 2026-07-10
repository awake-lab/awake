package scene

import io.github.ronjunevaldoz.awake.core.AwakeContext.Companion.gl
import io.github.ronjunevaldoz.awake.core.geometry.Attribute
import io.github.ronjunevaldoz.awake.core.graphics.Disposable
import io.github.ronjunevaldoz.awake.core.graphics.Drawable
import io.github.ronjunevaldoz.awake.core.graphics.opengl.CommonGL
import io.github.ronjunevaldoz.awake.core.graphics.opengl.OpenGL
import io.github.ronjunevaldoz.awake.core.isMobile
import io.github.ronjunevaldoz.awake.core.rendering.VertexArrayObject
import io.github.ronjunevaldoz.awake.core.rendering.addIndexBuffer
import io.github.ronjunevaldoz.awake.core.rendering.addVertexBuffer
import io.github.ronjunevaldoz.awake.core.rendering.createVAO
import io.github.ronjunevaldoz.awake.core.shader.SimpleShader
import io.github.ronjunevaldoz.awake.core.shader.use


class DemoColoredTriangle : Drawable, Disposable {

    private lateinit var shader: SimpleShader

    private val aPosition by lazy { shader.position }
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
                aColor, floatArrayOf(
                    1f, 0f, 0f, 1f,
                    0f, 1f, 0f, 1f,
                    0f, 0f, 1f, 1f,
                    0f, 0f, 0f, 1f,
                ), 4
            ),
        )
    }
    private val indices = byteArrayOf(
        0, 1, 2,
        2, 3, 0
    )

    private lateinit var vao: VertexArrayObject

    suspend fun load() {
        shader = SimpleShader.create(
            vertFile = if (isMobile) "colored-triangle.mobile.vert" else "colored-triangle.vert",
            fragFile = if (isMobile) "colored-triangle.mobile.frag" else "colored-triangle.frag"
        )
        shader.compile()
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
            vao.bind()
            gl.drawElements(
                OpenGL.DrawMode.Triangles,
                indices.size,
                CommonGL.GL_UNSIGNED_BYTE,
                null
            )
            vao.unbind()
        }
    }

    override fun dispose() {
        shader.delete()
        vao.delete()
    }
}