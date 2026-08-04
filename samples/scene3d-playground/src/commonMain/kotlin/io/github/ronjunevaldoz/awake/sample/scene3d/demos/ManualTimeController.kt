// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d.demos

/**
 * Auto/manual "time" driver for scrubbable per-frame animation -- shared shape any demo can
 * reuse, not coupled to what the time actually drives (rotation, orbit, a future skinned-mesh
 * clip). [autoPlay] (default `true`) advances [hours] every [advance] call by real elapsed
 * time; turning it off freezes [hours] at whatever a slider bound to it last set, so a visual
 * bug can be inspected at an exact frame instead of chased through a live animation and a
 * screenshot tool with no frame-accurate timing.
 *
 * [hours] is a 0..24 "clock face" framing (arbitrary but readable) rather than a raw radian/
 * second value -- a caller maps it to whatever unit it actually needs (e.g.
 * `hours * (360f / HOURS_PER_CYCLE)` for a full-turn rotation).
 */
internal class ManualTimeController(
    private val hoursPerSecond: Float = DEFAULT_HOURS_PER_SECOND
) {
    var autoPlay: Boolean = true

    var hours: Float = 0f
        set(value) {
            field = value % HOURS_PER_CYCLE
        }

    /** Call once per real frame -- no-ops while [autoPlay] is off, since [hours] is then only
     * ever set directly (by a slider bound to it). */
    fun advance(deltaSeconds: Float) {
        if (autoPlay) hours += deltaSeconds * hoursPerSecond
    }

    fun reset() {
        hours = 0f
    }

    companion object {
        const val HOURS_PER_CYCLE = 24f

        /** Same real-world pace [RotatingCubeDemo]'s old hardcoded spin rate used
         * (`0.8 radians/second` == one full turn every ~7.85s) -- `HOURS_PER_CYCLE` hours
         * covers a full turn, so this converts that same pace into hours/second. Not `const`:
         * `kotlin.math.PI` isn't a compile-time constant expression. */
        private val DEFAULT_HOURS_PER_SECOND = 0.8f / (2f * kotlin.math.PI.toFloat()) * HOURS_PER_CYCLE
    }
}
