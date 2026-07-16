// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.graphics

import android.content.Context
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import io.github.ronjunevaldoz.awake.core.application.AndroidGameLoop
import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.core.input.syncAwakeKeyInput
import io.github.ronjunevaldoz.awake.core.input.syncAwakePointerInput
import io.github.ronjunevaldoz.awake.core.utils.Frame
import io.github.ronjunevaldoz.awake.ui.UiDensity


class VulkanView(
    context: Context,
    private val application: Application
) : SurfaceView(context), SurfaceHolder.Callback2 {

    @Volatile
    private var running = false
    private var renderThread: Thread? = null

    init {
        holder.addCallback(this)
        isFocusable = true
        isFocusableInTouchMode = true
        requestFocus()
        syncUiDensity()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        syncUiDensity()
        Frame.width = width
        Frame.height = height
        application.create(holder.surface)
        // AndroidGameLoop.startLoop runs a single tick (delta/FPS bookkeeping + frame-rate
        // throttling) per call -- the caller owns the actual repetition, same contract
        // desktop's createFrame() uses via its own `while (!window.shouldClose())` loop.
        // This used to call startLoop exactly once here with no surrounding loop at all, so
        // frameCount (and therefore the demo cube's rotation) only ever advanced by a
        // single frame before rendering stopped. A dedicated thread is required rather than
        // looping on the calling (UI) thread: drawFrame()'s vkWaitForFences/
        // vkQueuePresentKHR block, and SurfaceView (unlike GLSurfaceView) has no built-in
        // render thread of its own.
        running = true
        renderThread = Thread({
            while (running) {
                AndroidGameLoop.startLoop { deltaTime ->
                    application.update(deltaTime.toFloat())
                }
            }
        }, "VulkanView-Render").apply { start() }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        syncUiDensity()
        application.resize(0, 0, width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        running = false
        renderThread?.join()
        renderThread = null
        Input.clearKeys()
        application.dispose()
    }

    // Fires on the UI thread, not the dedicated "VulkanView-Render" thread `update()` runs
    // on -- Input's fields are @Volatile specifically so this cross-thread write is safe
    // to read from the render thread's next update() call without further synchronization.
    // Only ACTION_DOWN/MOVE/UP are handled (single-pointer); multi-touch isn't modeled by
    // Input yet, matching this pass's "minimal, not exhaustive" input scope.
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return event.syncAwakePointerInput() || super.onTouchEvent(event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return event.syncAwakeKeyInput(down = true) || super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        return event.syncAwakeKeyInput(down = false) || super.onKeyUp(keyCode, event)
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        if (!hasWindowFocus) {
            Input.clearKeys()
        }
    }

    override fun surfaceRedrawNeeded(holder: SurfaceHolder) {
        //        TODO("Not yet implemented")
    }

    private fun syncUiDensity() {
        UiDensity.scale = resources.displayMetrics.density
    }
}
