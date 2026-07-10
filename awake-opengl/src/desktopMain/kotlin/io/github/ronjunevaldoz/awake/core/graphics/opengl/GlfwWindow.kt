package io.github.ronjunevaldoz.awake.core.graphics.opengl

import org.lwjgl.glfw.Callbacks
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.system.MemoryStack.stackPush


class GlfwWindow(val width: Int, val height: Int, title: String) {
    val id: Long

    init {
        checkError()
        initHints()
        id = GLFW.glfwCreateWindow(width, height, title, 0, 0)
        createWindow()
        initOpenGL()
    }


    private fun initHints() {
        // Configure GLFW
        GLFW.glfwDefaultWindowHints() // optional, the current window hints are already the default
        GLFW.glfwWindowHint(
            GLFW.GLFW_VISIBLE,
            GLFW.GLFW_FALSE
        ) // the window will stay hidden after creation
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE) // the window will be resizable
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3)
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3)
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE)
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE)
    }

    private fun createWindow() {
        closeOnEscape()
        center()
        // Make the OpenGL context current
        GLFW.glfwMakeContextCurrent(id)
        // Enable v-sync
        GLFW.glfwSwapInterval(1)
        // Make the window visible
        GLFW.glfwShowWindow(id)
    }


    private fun initOpenGL() {
        GL.createCapabilities()
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
    }

    fun shouldClose(): Boolean {
        return GLFW.glfwWindowShouldClose(id)
    }

    fun setShouldClose(value: Boolean) {
        GLFW.glfwSetWindowShouldClose(id, value)
    }

    fun getTime(): Double {
        return GLFW.glfwGetTime()
    }

    fun setCloseListener(onClose: () -> Unit) {
        GLFW.glfwSetWindowCloseCallback(id) {
            onClose()
        }
    }

    fun setSizeListener(onResize: (w: Int, h: Int) -> Unit) {
        GLFW.glfwSetWindowSizeCallback(id) { _, w, h ->
            onResize(w, h)
        }
    }

    fun setFrameSizeListener(onResize: (w: Int, h: Int) -> Unit) {
        GLFW.glfwSetFramebufferSizeCallback(id) { _: Long, w: Int, h: Int ->
            onResize(w, h)
        }
    }

    fun closeOnEscape() {
        GLFW.glfwSetKeyCallback(id) { _: Long, key: Int, scanCode: Int, action: Int, mods: Int ->
            if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_RELEASE) {
                setShouldClose(true)
            }
        }
    }

    fun center() {
        useSize { w, h ->
            // Get the resolution of the primary monitor
            val vidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor())

            // Center the window
            GLFW.glfwSetWindowPos(
                id,
                (vidMode!!.width() - w) / 2,
                (vidMode.height() - h) / 2
            )
        }
    }

    fun useSize(size: (w: Int, h: Int) -> Unit) {
        stackPush().use { stack ->
            val wBuffer = stack.mallocInt(1) // int*
            val hBuffer = stack.mallocInt(1) // int*

            // Get the window size passed to glfwCreateWindow
            GLFW.glfwGetWindowSize(id, wBuffer, hBuffer)
            size(wBuffer[0], hBuffer[0])
        }
    }

    fun <T> getContentScale(scale: (x: Float, y: Float) -> T): T {
        return stackPush().use { s ->
            val px = s.mallocFloat(1)
            val py = s.mallocFloat(1)
            GLFW.glfwGetMonitorContentScale(id, px, py)
            scale(px[0], py[0])
        }
    }

    fun pollEvents() {
        GLFW.glfwPollEvents()
    }

    fun swapBuffers() {
        GLFW.glfwSwapBuffers(id)
    }

    fun dispose() {
        // Free the window callbacks and destroy the window
        Callbacks.glfwFreeCallbacks(id)
        GLFW.glfwDestroyWindow(id)

        GLFW.glfwTerminate()
        GLFW.glfwSetErrorCallback(null)!!.free()
    }

    fun checkError() {
        GLFWErrorCallback.createPrint(System.err).set()
        check(GLFW.glfwInit()) { "Unable to initialize GLFW" }
    }
}