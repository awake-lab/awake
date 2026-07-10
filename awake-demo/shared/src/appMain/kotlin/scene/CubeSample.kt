package scene

import io.github.ronjunevaldoz.awake.core.AwakeContext.Companion.gl
import io.github.ronjunevaldoz.awake.core.geometry.Attribute
import io.github.ronjunevaldoz.awake.core.getPlatform
import io.github.ronjunevaldoz.awake.core.graphics.Disposable
import io.github.ronjunevaldoz.awake.core.graphics.Drawable
import io.github.ronjunevaldoz.awake.core.graphics.opengl.CommonGL
import io.github.ronjunevaldoz.awake.core.graphics.opengl.OpenGL
import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.core.math.angleRad
import io.github.ronjunevaldoz.awake.core.math.times
import io.github.ronjunevaldoz.awake.core.rendering.VertexArrayObject
import io.github.ronjunevaldoz.awake.core.rendering.addIndexBuffer
import io.github.ronjunevaldoz.awake.core.rendering.addVertexBuffer
import io.github.ronjunevaldoz.awake.core.rendering.createVAO
import io.github.ronjunevaldoz.awake.core.rendering.use
import io.github.ronjunevaldoz.awake.core.shader.SimpleShader
import io.github.ronjunevaldoz.awake.core.shader.use
import io.github.ronjunevaldoz.awake.core.utils.Frame
import io.github.ronjunevaldoz.awake.core.utils.Time

class CubeSample : Drawable, Disposable {

    private lateinit var shader: SimpleShader

    private val aPosition by lazy { shader.position }
    private val aColor by lazy { shader.color }

    private var attributes: List<Attribute> = emptyList()
    private lateinit var vao: VertexArrayObject
    private val indices = byteArrayOf(
        // Front face
        0, 1, 2, // Triangle 1
        1, 3, 2, // Triangle 2

        // Back face
        4, 5, 6, // Triangle 1
        5, 7, 6, // Triangle 2

        // Left face
        8, 9, 10, // Triangle 1
        9, 11, 10, // Triangle 2

        // Right face
        12, 13, 14, // Triangle 1
        13, 15, 14, // Triangle 2

        // Top face
        16, 17, 18, // Triangle 1
        17, 19, 18, // Triangle 2

        // Bottom face
        20, 21, 22, // Triangle 1
        21, 23, 22  // Triangle 2
    )

    private var angle = 0f

    suspend fun load() {
        shader = SimpleShader.create(
            vertFile = "cube.vert",
            fragFile = "cube.frag",
            define = if (getPlatform().isMobile) "#version 300 es\n" else "#version 330 core\n"
        )
        shader.compile()
        createBuffers()
    }

    override fun draw() {
        shader.use {
            val ratio = Frame.width.toFloat() / Frame.height.toFloat()
            val projection = Mat4.perspective(90f.angleRad, ratio, 0.1f, 100f)
//            val projection = Mat4f.frustum(-ratio, ratio, -1f, 1f, 0.1f, 100.0f)
            val eye = Vec3(0.0f, 0.0f, 5.0f)
            val target = Vec3(0.0f, 0.0f, 0.0f)
            val up = Vec3(0.0f, 1.0f, 0.0f)
            val viewMatrix = Mat4.setLookAt(
                eye,
                target,
                up
            )
            shader.projectionViewMatrix = viewMatrix * projection
            angle += Time.Delta.toFloat() * 1.angleRad
            val trans = Mat4()
                .translate(1f, 2f, 0f)
                .rotate(angle, Vec3(x = 1f, y = 1f, z = 1f))
                .scale(1.5f)
            shader.modelViewMatrix = trans
            vao.use {
                gl.enable(CommonGL.GL_CULL_FACE)
                gl.cullFace(CommonGL.GL_FRONT)
                gl.frontFace(CommonGL.GL_CCW)
                gl.drawElements(
                    OpenGL.DrawMode.Triangles,
                    indices.size,
                    CommonGL.GL_UNSIGNED_BYTE,
                    null
                )
                gl.disable(CommonGL.GL_CULL_FACE)
            }
        }
    }

    override fun dispose() {
        shader.delete()
        vao.delete()
    }


    private fun createBuffers() {
        val halfSize = 1f
        attributes = listOf(
            Attribute(
                aPosition, floatArrayOf(
                    // Front face
                    -0.5f, 0.5f, 0.5f, // Top-left
                    0.5f, 0.5f, 0.5f, // Top-right
                    -0.5f, -0.5f, 0.5f, // Bottom-left
                    0.5f, -0.5f, 0.5f, // Bottom-right

                    // Back face
                    0.5f, 0.5f, -0.5f, // Top-left
                    -0.5f, 0.5f, -0.5f, // Top-right
                    0.5f, -0.5f, -0.5f, // Bottom-left
                    -0.5f, -0.5f, -0.5f, // Bottom-right

                    // Left face
                    -0.5f, 0.5f, -0.5f, // Top-left
                    -0.5f, 0.5f, 0.5f, // Top-right
                    -0.5f, -0.5f, -0.5f, // Bottom-left
                    -0.5f, -0.5f, 0.5f, // Bottom-right

                    // Right face
                    0.5f, 0.5f, 0.5f, // Top-left
                    0.5f, 0.5f, -0.5f, // Top-right
                    0.5f, -0.5f, 0.5f, // Bottom-left
                    0.5f, -0.5f, -0.5f, // Bottom-right

                    // Top face
                    -0.5f, 0.5f, -0.5f, // Top-left
                    0.5f, 0.5f, -0.5f, // Top-right
                    -0.5f, 0.5f, 0.5f, // Bottom-left
                    0.5f, 0.5f, 0.5f, // Bottom-right

                    // Bottom face
                    -0.5f, -0.5f, 0.5f, // Top-left
                    0.5f, -0.5f, 0.5f, // Top-right
                    -0.5f, -0.5f, -0.5f, // Bottom-left
                    0.5f, -0.5f, -0.5f  // Bottom-right
                ), 3
            ),
            Attribute(
                aColor, floatArrayOf(
                    // Front face (red)
                    1.0f, 0.0f, 0.0f,
                    1.0f, 0.0f, 0.0f,
                    1.0f, 0.0f, 0.0f,
                    1.0f, 0.0f, 0.0f,

                    // Back face (green)
                    0.0f, 1.0f, 0.0f,
                    0.0f, 1.0f, 0.0f,
                    0.0f, 1.0f, 0.0f,
                    0.0f, 1.0f, 0.0f,

                    // Left face (blue)
                    0.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, 1.0f,

                    // Right face (yellow)
                    1.0f, 1.0f, 0.0f,
                    1.0f, 1.0f, 0.0f,
                    1.0f, 1.0f, 0.0f,
                    1.0f, 1.0f, 0.0f,

                    // Top face (cyan)
                    0.0f, 1.0f, 1.0f,
                    0.0f, 1.0f, 1.0f,
                    0.0f, 1.0f, 1.0f,
                    0.0f, 1.0f, 1.0f,

                    // Bottom face (magenta)
                    1.0f, 0.0f, 1.0f,
                    1.0f, 0.0f, 1.0f,
                    1.0f, 0.0f, 1.0f,
                    1.0f, 0.0f, 1.0f
                ), 3
            ),
        )
        vao = createVAO {
            addIndexBuffer(indices)
            attributes.forEach(::addVertexBuffer)
        }
    }
}