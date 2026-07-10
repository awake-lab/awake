package io.github.ronjunevaldoz.awake.core.graphics.opengl

import io.github.aakira.napier.Napier
import io.github.ronjunevaldoz.awake.core.AwakeContext
import io.github.ronjunevaldoz.awake.core.graphics.Application
import io.github.ronjunevaldoz.awake.core.utils.Frame
import io.github.ronjunevaldoz.awake.core.utils.Time
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ObjCAction
import platform.CoreFoundation.CFTimeInterval
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGRectGetHeight
import platform.CoreGraphics.CGRectGetWidth
import platform.EAGL.EAGLContext
import platform.EAGL.kEAGLRenderingAPIOpenGLES1
import platform.EAGL.kEAGLRenderingAPIOpenGLES2
import platform.EAGL.kEAGLRenderingAPIOpenGLES3
import platform.Foundation.NSDate
import platform.Foundation.NSRunLoop
import platform.Foundation.NSRunLoopCommonModes
import platform.Foundation.NSSelectorFromString
import platform.Foundation.dateWithTimeIntervalSinceNow
import platform.Foundation.runUntilDate
import platform.GLKit.GLKView
import platform.GLKit.GLKViewDelegateProtocol
import platform.GLKit.GLKViewDrawableColorFormatRGBA8888
import platform.GLKit.GLKViewDrawableDepthFormat24
import platform.GLKit.GLKViewDrawableStencilFormat8
import platform.QuartzCore.CADisplayLink
import platform.UIKit.UIScreen
import platform.UIKit.UIWindow


class GameView(frame: CValue<CGRect>, val renderer: Application) : GLKView(frame),
    GLKViewDelegateProtocol {

    private var displayLink: CADisplayLink? = null
    private val targetFPS: Int = AwakeContext.config.fps
    private val preferredFrameDuration = 1.0 / targetFPS.toDouble()
    private var previousTimestamp: CFTimeInterval = 0.0

    private var fpsCallback: ((Int) -> Unit)? = null
    private var frames: Int = 0
    private var frameTimer: Double = 0.0

    init {
        val context = when (AwakeContext.config.glVersion) {
            OpenGL.Version.ES1 -> EAGLContext(kEAGLRenderingAPIOpenGLES1)
            OpenGL.Version.ES2 -> EAGLContext(kEAGLRenderingAPIOpenGLES2)
            OpenGL.Version.ES3 -> EAGLContext(kEAGLRenderingAPIOpenGLES3)
        }
        // create context from es version
        this.context = context
        EAGLContext.setCurrentContext(this.context)

        drawableColorFormat = GLKViewDrawableColorFormatRGBA8888
        drawableDepthFormat = GLKViewDrawableDepthFormat24
        drawableStencilFormat = GLKViewDrawableStencilFormat8

        // avoid redraw component on component update
        enableSetNeedsDisplay = false

        renderer.create()
    }

    @ObjCAction
    private fun update(displayLink: CADisplayLink) {
        val currentTimestamp = displayLink.timestamp
        val deltaTime = currentTimestamp - previousTimestamp
        Time.Delta = deltaTime
        // frame counter
        frames++
        frameTimer += deltaTime
        if (frameTimer >= 1.0) {
            // update fps each second
            fpsCallback?.invoke(frames)
            frames = 0
            frameTimer = 0.0
        }
        // Update previous timestamp for the next frame
        previousTimestamp = currentTimestamp
        display()
        sync(displayLink)
    }

    /**
     * TODO Experimental will it cause to inaccuracy?
     */
    private fun sync(displayLink: CADisplayLink) {
        // Calculate the elapsed time since the previous frame
        val elapsedTime = displayLink.duration

        // Check if the elapsed time is less than the preferred frame duration
        if (elapsedTime < preferredFrameDuration) {
            // Calculate the remaining time to wait
            val remainingTime = preferredFrameDuration - elapsedTime

            // Run the run loop until the remaining time has elapsed
            NSRunLoop.currentRunLoop.runUntilDate(NSDate.dateWithTimeIntervalSinceNow(remainingTime))
        }
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        updateViewport()
    }

    private fun updateViewport() {
        val scale = UIScreen.mainScreen.scale
        val width = CGRectGetWidth(bounds)
        val height = CGRectGetHeight(bounds)
        val viewportWidth = width * scale
        val viewportHeight = height * scale

        Napier.i("Resize $viewportWidth | $viewportHeight")
        Frame.width = viewportWidth.toInt()
        Frame.height = viewportHeight.toInt()
        renderer.resize(0, 0, Frame.width, Frame.height)
    }

    override fun willMoveToWindow(newWindow: UIWindow?) {
        super.willMoveToWindow(newWindow)
        if (newWindow != null) {
            startRenderLoop()
        } else {
            stopRenderLoop()
        }
    }


    private fun startRenderLoop() {
        fpsCallback = { fps ->
            Time.FpsString = "$fps"
        }
        if (displayLink == null) {
            Napier.i("start render")
            renderer.resume()
            displayLink = UIScreen.mainScreen.displayLinkWithTarget(
                this,
                NSSelectorFromString("update:")
            )?.apply {
                preferredFramesPerSecond = AwakeContext.config.fps.toLong()
                addToRunLoop(NSRunLoop.currentRunLoop, NSRunLoopCommonModes)
            }
            delegate = this
        }
    }

    private fun stopRenderLoop() {
        Napier.i("stop render")
        renderer.pause()
        displayLink?.invalidate()
        displayLink = null
        fpsCallback = null
    }


    override fun glkView(view: GLKView, drawInRect: CValue<CGRect>) {
        renderer.update(Time.Delta.toFloat())
    }
}