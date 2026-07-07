package io.github.ronjunevaldoz.awake.core.graphics.opengl

import androidx.compose.runtime.Composable
import androidx.compose.ui.ComposeScene
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import kotlinx.coroutines.Dispatchers
import org.jetbrains.skia.BackendRenderTarget
import org.jetbrains.skia.Color
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.DirectContext
import org.jetbrains.skia.FramebufferFormat
import org.jetbrains.skia.Surface
import org.jetbrains.skia.SurfaceColorFormat
import org.jetbrains.skia.SurfaceOrigin
import org.jetbrains.skiko.FrameDispatcher
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30


class GlfwCompose(window: GlfwWindow, content: @Composable () -> Unit) {

    init {
        createCompose(window, content)
    }

    private fun createCompose(window: GlfwWindow, content: @Composable () -> Unit) {
        val context = DirectContext.makeGL()
        var surface =
            createSurface(
                window.width,
                window.height,
                context
            ) // Skia Surface, bound to the OpenGL framebuffer

        val glfwDispatcher =
            Dispatchers.Glfw // a custom coroutine dispatcher, in which Compose will run

        window.setCloseListener {
            glfwDispatcher.stop()
        }

        var width = window.width
        var height = window.height

        lateinit var composeScene: ComposeScene

        fun render() {
            surface.canvas.clear(Color.WHITE)
            composeScene.constraints = Constraints(maxWidth = width, maxHeight = height)
            composeScene.render(surface.canvas, System.nanoTime())
            context.flush()
            window.swapBuffers()
        }

        val frameDispatcher = FrameDispatcher(glfwDispatcher) { render() }
        val density = Density(window.getContentScale { x, y -> x })
        composeScene =
            ComposeScene(glfwDispatcher, density, invalidate = frameDispatcher::scheduleFrame)
        window.setSizeListener { w, h ->
            width = w * 2
            height = h * 2
            surface.close()
            surface = createSurface(width, height, context)

            GLFW.glfwSwapInterval(0)
            render()
            GLFW.glfwSwapInterval(1)
        }

        composeScene.subscribeToGLFWEvents(window.id)
        composeScene.setContent(content)

        glfwDispatcher.runLoop()

        composeScene.close()
    }

    private fun createSurface(width: Int, height: Int, context: DirectContext): Surface {
        val fbId = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING)
        val renderTarget = BackendRenderTarget.makeGL(
            width, height, 0, 8, fbId,
            FramebufferFormat.GR_GL_RGBA8
        )
        return Surface.makeFromBackendRenderTarget(
            context,
            renderTarget,
            SurfaceOrigin.BOTTOM_LEFT,
            SurfaceColorFormat.RGBA_8888,
            ColorSpace.sRGB
        ) ?: throw Exception("Unable to create surface")
    }
}
