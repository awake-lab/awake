// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.animation

/**
 * Smoothly crosses from whatever's currently playing into a new [AnimationClip] instead of
 * snapping to it -- a single in-flight transition (no queued/stacked transition graph; see
 * [play]'s own doc comment for what happens if it's called again mid-blend). This is the
 * gameplay-facing "play this instead" API; [AnimationPose.blend] is the underlying per-bone math
 * this composes.
 */
class AnimationCrossfade(private val skeleton: Skeleton) {
    private var currentClip: AnimationClip? = null
    private var currentTime = 0f
    private val currentPose = AnimationPose(skeleton)

    // A frozen snapshot of the pose being faded FROM -- written only in play(), never resampled
    // or mutated by advance(). Blending from a fixed snapshot (via blendScratch below) rather
    // than mutating this directly avoids compounding: AnimationPose.blend() moves ITS OWN state
    // toward the target, so re-blending the same object every frame at an increasing weight
    // would overshoot past that weight instead of landing exactly on it.
    private var outgoingPose: AnimationPose? = null
    private val blendScratch = AnimationPose(skeleton)
    private var blendElapsed = 0f
    private var blendDuration = 0f

    /** Starts playing [clip] from time 0, crossfading from whatever's currently playing over
     * [blendSeconds]. A no-op if [clip] is already the current clip. Calling this again while an
     * earlier transition is still blending immediately snaps the current on-screen blended pose
     * as the new outgoing base and starts a fresh transition to [clip] -- transitions never
     * queue or stack. */
    fun play(clip: AnimationClip, blendSeconds: Float = DEFAULT_BLEND_SECONDS) {
        if (clip === currentClip) return
        val activeClip = currentClip
        outgoingPose = if (activeClip == null) {
            null
        } else {
            val frozen = AnimationPose(skeleton)
            val stillBlendingFrom = outgoingPose
            if (stillBlendingFrom != null) {
                val weight = (blendElapsed / blendDuration.coerceAtLeast(MIN_DURATION)).coerceIn(0f, 1f)
                frozen.copyFrom(stillBlendingFrom)
                frozen.blend(currentPose, weight)
            } else {
                frozen.sample(activeClip, currentTime)
            }
            frozen
        }
        currentClip = clip
        currentTime = 0f
        blendElapsed = 0f
        blendDuration = blendSeconds
    }

    /** Advances all in-flight clocks by [delta] and returns this frame's joint palette against
     * [skin] -- blended with the frozen outgoing pose while a transition is in flight (weight
     * ramps `0..1` over [blendDuration]), otherwise just the current clip's own pose. */
    fun advance(delta: Float, skin: Skin): FloatArray {
        val clip = currentClip ?: return currentPose.jointPalette(skin)
        currentTime += delta
        currentPose.sample(clip, currentTime % clip.duration.coerceAtLeast(MIN_DURATION))

        val outPose = outgoingPose ?: return currentPose.jointPalette(skin)
        blendElapsed += delta
        val weight = (blendElapsed / blendDuration.coerceAtLeast(MIN_DURATION)).coerceIn(0f, 1f)
        blendScratch.copyFrom(outPose)
        blendScratch.blend(currentPose, weight)
        if (weight >= 1f) outgoingPose = null
        return blendScratch.jointPalette(skin)
    }

    private companion object {
        const val DEFAULT_BLEND_SECONDS = 0.25f

        // Guards duration/blendDuration against a zero-length clip or transition, where % or /
        // would otherwise divide by zero.
        const val MIN_DURATION = 0.0001f
    }
}
