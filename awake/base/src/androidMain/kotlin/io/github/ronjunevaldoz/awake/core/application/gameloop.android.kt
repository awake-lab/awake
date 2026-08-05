// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.application

import io.github.ronjunevaldoz.awake.core.utils.Time


object AndroidGameLoop : GameLoop {
    private val desiredFrameRate = EngineConfigHolder.config.fps
    private val desiredFrameTimeMillis = (1000 / desiredFrameRate).toLong()
    private var previousFrameTime = System.nanoTime()
    private var fpsTimer = 0L
    private var frames = 0
    private var fps = 0


    override fun startLoop(onUpdate: (deltaTime: Double) -> Unit) {
        // Measure the current time using nanoseconds
        val currentFrameTime = System.nanoTime()

        // Calculate the elapsed time since the previous frame in seconds
        val deltaTime = (currentFrameTime - previousFrameTime) / 1e9

        // Update game logic
        Time.Delta = deltaTime
        onUpdate(deltaTime)

        // Calculate FPS
        frames++
        fpsTimer += currentFrameTime - previousFrameTime
        if (fpsTimer >= 1e9) { // 1 second in nanoseconds
            // Calculate frames per second (FPS)
            fps = frames
            frames = 0
            fpsTimer = 0
        }
        Time.FpsString = "$fps"

        // Delay to limit FPS
        val sleepTimeMillis =
            desiredFrameTimeMillis - (System.nanoTime() - currentFrameTime) / 1_000_000
        if (sleepTimeMillis > 0) {
            // Sleep the thread to achieve the desired frame rate
            Thread.sleep(sleepTimeMillis)
        }

        // Update the previous frame time for the next iteration
        previousFrameTime = currentFrameTime
    }
}
