// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.graphics

import android.content.Context
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import io.github.ronjunevaldoz.awake.core.application.AndroidGameLoop
import io.github.ronjunevaldoz.awake.core.input.AndroidSoftKeyboardBridge
import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.core.input.createAwakeInputConnection
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
    
    private val input: Input get() = application.input
        
    private val softKeyboardBridge by lazy { AndroidSoftKeyboardBridge(this, input) }

    init {
        holder.addCallback(this)
        isFocusable = true
        isFocusableInTouchMode = true
        requestFocus()
        syncUiDensity()
    }

    override fun onCheckIsTextEditor(): Boolean = true

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection =
        createAwakeInputConnection(outAttrs, input)

    override fun surfaceCreated(holder: SurfaceHolder) {
        syncUiDensity()
        Frame.width = width
        Frame.height = height
        application.create(holder.surface)
        running = true
        renderThread = Thread({
            while (running) {
                AndroidGameLoop.startLoop { deltaTime ->
                    application.update(deltaTime.toFloat())
                    post { softKeyboardBridge.syncSoftKeyboardVisibility() }
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
        input.clearKeys()
        application.dispose()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return event.syncAwakePointerInput(input) || super.onTouchEvent(event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return event.syncAwakeKeyInput(down = true, input) || super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        return event.syncAwakeKeyInput(down = false, input) || super.onKeyUp(keyCode, event)
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        if (!hasWindowFocus) {
            input.clearKeys()
        }
    }

    override fun surfaceRedrawNeeded(holder: SurfaceHolder) {}

    private fun syncUiDensity() {
        UiDensity.scale = resources.displayMetrics.density
    }
}
