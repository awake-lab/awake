// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.animation

import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.core.math.Quat
import io.github.ronjunevaldoz.awake.core.math.Vec3
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/** [AnimationCrossfade] tests -- a synthetic single-joint skeleton with two static (single-
 * keyframe) translation clips, so blend weight can be asserted precisely without keyframe-
 * interpolation noise. */
class AnimationCrossfadeTest {
    private fun oneJointSkeleton(): Skeleton = Skeleton(
        bones = listOf(Bone(Vec3.ZERO, Quat.IDENTITY, Vec3(1f, 1f, 1f), matrix = null, children = emptyList())),
        roots = listOf(0),
    )

    private fun identitySkin(): Skin = Skin(joints = listOf(0), inverseBindMatrices = listOf(Mat4()))

    private fun staticTranslationClip(x: Float): AnimationClip = AnimationClip(
        name = null,
        channels = listOf(
            AnimationChannel(
                targetBone = 0,
                property = AnimationProperty.Translation,
                sampler = AnimationSampler(times = floatArrayOf(0f), values = floatArrayOf(x, 0f, 0f), componentsPerKeyframe = 3),
            ),
        ),
    )

    private fun translationX(palette: FloatArray) = palette[12] // m03 -- translation X of joint 0.

    @Test
    fun playWithTheSameClipIsANoOp() {
        val skeleton = oneJointSkeleton()
        val crossfade = AnimationCrossfade(skeleton)
        val clip = staticTranslationClip(5f)
        crossfade.play(clip)
        crossfade.advance(1f, identitySkin()) // finish the (no outgoing) transition

        crossfade.play(clip) // same clip instance again
        val palette = crossfade.advance(0f, identitySkin())

        assertEquals(5f, translationX(palette))
    }

    @Test
    fun advanceDuringATransitionReturnsAWeightedBlend() {
        val skeleton = oneJointSkeleton()
        val crossfade = AnimationCrossfade(skeleton)
        crossfade.play(staticTranslationClip(0f))
        crossfade.advance(0f, identitySkin()) // establish the outgoing clip as "currently playing"

        crossfade.play(staticTranslationClip(10f), blendSeconds = 1f)
        val midway = crossfade.advance(0.5f, identitySkin())

        val x = translationX(midway)
        assertNotEquals(0f, x)
        assertNotEquals(10f, x)
        assertEquals(5f, x) // half the 1-second blend elapsed -> weight 0.5 -> arithmetic mean.
    }

    @Test
    fun transitionCompletesAndReturnsToPlainCurrentPoseAfterBlendDuration() {
        val skeleton = oneJointSkeleton()
        val crossfade = AnimationCrossfade(skeleton)
        crossfade.play(staticTranslationClip(0f))
        crossfade.advance(0f, identitySkin())

        crossfade.play(staticTranslationClip(10f), blendSeconds = 1f)
        crossfade.advance(1f, identitySkin()) // exactly the full blend duration

        val afterBlend = crossfade.advance(0f, identitySkin())
        assertEquals(10f, translationX(afterBlend))
    }

    @Test
    fun playAgainMidTransitionSnapsToANewTransitionInsteadOfStacking() {
        val skeleton = oneJointSkeleton()
        val crossfade = AnimationCrossfade(skeleton)
        crossfade.play(staticTranslationClip(0f))
        crossfade.advance(0f, identitySkin())

        crossfade.play(staticTranslationClip(10f), blendSeconds = 1f)
        crossfade.advance(0.5f, identitySkin()) // now 50% blended toward 10

        // Change our mind mid-blend -- the new outgoing base should be the ~5 we were showing,
        // not a hard reset back to the original 0.
        crossfade.play(staticTranslationClip(20f), blendSeconds = 1f)
        val justAfterSnap = crossfade.advance(0f, identitySkin())

        val x = translationX(justAfterSnap)
        assertNotEquals(0f, x)
        assertNotEquals(20f, x)
    }
}
