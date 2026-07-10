package scene

import io.github.ronjunevaldoz.awake.core.AwakeContext.Companion.gl
import io.github.ronjunevaldoz.awake.core.graphics.Disposable
import io.github.ronjunevaldoz.awake.core.graphics.Drawable
import io.github.ronjunevaldoz.awake.core.graphics.opengl.OpenGL
import io.github.ronjunevaldoz.awake.core.isMobile
import io.github.ronjunevaldoz.awake.core.rendering.VertexArrayObject
import io.github.ronjunevaldoz.awake.core.rendering.addVertexBuffer
import io.github.ronjunevaldoz.awake.core.rendering.build
import io.github.ronjunevaldoz.awake.core.shader.SimpleShader
import io.github.ronjunevaldoz.awake.core.shader.use

/**
 * Draw triangle using arrays
 */
class DemoTriangle : Drawable, Disposable {
    private lateinit var shader: SimpleShader

    private val aPosition by lazy { shader.position }

    private val vertices = floatArrayOf(
        0.0f, 1.0f,
        -1.0f, -1.0f,
        1.0f, -1.0f
    )

    private val vao = VertexArrayObject()

    suspend fun load() {
        shader = SimpleShader.create(
            vertFile = if (isMobile) "triangle.mobile.vert" else "triangle.vert",
            fragFile = if (isMobile) "triangle.mobile.frag" else "triangle.frag"
        )
        shader.compile()
        createBuffers()
    }

    private fun createBuffers() {
        // VAO
        vao.addVertexBuffer(
            index = aPosition, // position
            size = 2,
            data = vertices
        )
        vao.build()
    }

    override fun draw() {
        shader.use {
            vao.bind()
            gl.enableVertexAttribArray(aPosition)
            gl.drawArrays(
                OpenGL.DrawMode.Triangles,
                0,
                vertices.size / 2
            )
            gl.disableVertexAttribArray(aPosition)
            vao.unbind()
        }
    }

    override fun dispose() {
        vao.delete()
        // Delete the shader program
        shader.delete()
    }
}