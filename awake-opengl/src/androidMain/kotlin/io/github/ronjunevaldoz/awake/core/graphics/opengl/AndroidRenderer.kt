package io.github.ronjunevaldoz.awake.core.graphics.opengl

import android.opengl.GLSurfaceView
import io.github.ronjunevaldoz.awake.core.application.AndroidGameLoop
import io.github.ronjunevaldoz.awake.core.graphics.Application
import io.github.ronjunevaldoz.awake.core.utils.Frame
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class AndroidRenderer(private val renderer: Application) : GLSurfaceView.Renderer {
    private var isPaused: Boolean = false

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // Set the background frame color
        renderer.create()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        // Set the viewport to the size of the view.
        Frame.width = width
        Frame.height = height
        renderer.resize(0, 0, width, height)
    }

    fun pause() {
        isPaused = true
        renderer.pause()
    }

    fun resume() {
        isPaused = false
        renderer.resume()
    }

    fun dispose() {
        renderer.dispose()
    }

    override fun onDrawFrame(gl: GL10?) {
        if (isPaused) return
        AndroidGameLoop.startLoop { deltaTime ->
            renderer.update(deltaTime.toFloat())
        }
    }
}