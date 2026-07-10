package io.github.ronjunevaldoz.awake.core.application

import io.github.ronjunevaldoz.awake.core.graphics.opengl.GlfwWindow
import io.github.ronjunevaldoz.awake.core.utils.Frame


fun createFrame(
    width: Int,
    height: Int,
    onInit: () -> Unit,
    onResize: (width: Int, height: Int) -> Unit,
    onUpdate: (delta: Float) -> Unit,
    onDispose: () -> Unit
) {
    Frame.width = width
    Frame.height = height
    val window = GlfwWindow(width, height, "Desktop")
    onInit()
    window.setFrameSizeListener { w, h ->
        Frame.width = width
        Frame.height = height
        onResize(w, h)
    }
    while (!window.shouldClose()) {
        // updates
        window.pollEvents()
        // render
        DesktopGameLoop.startLoop { deltaTime ->
            onUpdate(deltaTime.toFloat())
        }
        // swap buffer
        window.swapBuffers()
    }
    onDispose()
    window.dispose()
}