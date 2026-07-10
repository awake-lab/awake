/*
 * Awake
 * Awake.awake-core.main
 *
 * Copyright (c) ronjunevaldoz 2023.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.ronjunevaldoz.awake.core.graphics

import android.content.Context
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import io.github.ronjunevaldoz.awake.core.application.AndroidGameLoop
import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.core.utils.Frame


class VulkanView(
    context: Context,
    private val application: Application
) : SurfaceView(context), SurfaceHolder.Callback2 {

    @Volatile
    private var running = false
    private var renderThread: Thread? = null

    init {
        holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
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
        application.resize(0, 0, width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        running = false
        renderThread?.join()
        renderThread = null
        application.dispose()
    }

    // Fires on the UI thread, not the dedicated "VulkanView-Render" thread `update()` runs
    // on -- Input's fields are @Volatile specifically so this cross-thread write is safe
    // to read from the render thread's next update() call without further synchronization.
    // Only ACTION_DOWN/MOVE/UP are handled (single-pointer); multi-touch isn't modeled by
    // Input yet, matching this pass's "minimal, not exhaustive" input scope.
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE ->
                Input.setPointer(down = true, x = event.x, y = event.y)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                Input.setPointer(down = false, x = event.x, y = event.y)
        }
        return true
    }

    override fun surfaceRedrawNeeded(holder: SurfaceHolder) {
        //        TODO("Not yet implemented")
    }
}