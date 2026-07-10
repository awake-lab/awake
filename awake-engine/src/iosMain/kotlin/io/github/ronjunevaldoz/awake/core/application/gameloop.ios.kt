package io.github.ronjunevaldoz.awake.core.application

import io.github.ronjunevaldoz.awake.core.utils.Time
import kotlin.native.concurrent.ThreadLocal
import kotlin.time.TimeSource
import platform.posix.usleep

/**
 * Was previously an empty no-op stub (`startLoop` never called [onUpdate] at all) -- no
 * live call site exists yet (Phase 6, iOS via MoltenVK, is still post-desktop), but an
 * empty [GameLoop] is a footgun waiting for whoever wires the first iOS Vulkan surface up:
 * silently nothing would ever render or advance. Implemented for real, mirroring
 * [DesktopGameLoop]/[AndroidGameLoop]'s measure-delta/throttle-to-target-fps contract
 * (single tick per call, caller owns the repeat loop) using [TimeSource.Monotonic] instead
 * of `System.nanoTime()` since that's not available on Kotlin/Native. Not
 * hardware-verified -- there is no iOS app target driving a frame loop to verify it
 * against yet.
 */
@ThreadLocal
object IOSGameLoop : GameLoop {
    private val desiredFrameRate = EngineConfigHolder.config.fps
    private val desiredFrameTimeMicros = (1_000_000L / desiredFrameRate)
    private var previousMark = TimeSource.Monotonic.markNow()
    private var fpsTimerNanos = 0L
    private var frames = 0
    private var fps = 0

    override fun startLoop(onUpdate: (deltaTime: Double) -> Unit) {
        val currentMark = TimeSource.Monotonic.markNow()
        val elapsedNanos = (currentMark - previousMark).inWholeNanoseconds
        val deltaTime = elapsedNanos / 1e9

        Time.Delta = deltaTime
        onUpdate(deltaTime)

        frames++
        fpsTimerNanos += elapsedNanos
        if (fpsTimerNanos >= 1_000_000_000L) {
            fps = frames
            frames = 0
            fpsTimerNanos = 0
        }
        Time.FpsString = "$fps"

        val sleepTimeMicros = desiredFrameTimeMicros - elapsedNanos / 1_000L
        if (sleepTimeMicros > 0) {
            usleep(sleepTimeMicros.toUInt())
        }

        previousMark = currentMark
    }
}