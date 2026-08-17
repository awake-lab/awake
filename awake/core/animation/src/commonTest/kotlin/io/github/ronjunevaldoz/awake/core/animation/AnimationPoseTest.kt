// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.animation

import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.core.math.Quat
import io.github.ronjunevaldoz.awake.core.math.Vec3
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** [AnimationPose] tests -- a synthetic 2-bone skeleton (root joint + child joint, translated 1
 * unit up), not a real glTF file, so the joint-palette math itself is isolated from any
 * importer's parsing. */
class AnimationPoseTest {
    private fun bone(translation: Vec3 = Vec3(0f, 0f, 0f), children: List<Int> = emptyList()): Bone =
        Bone(translation, Quat.IDENTITY, Vec3(1f, 1f, 1f), matrix = null, children = children)

    private fun twoJointSkeleton(): Skeleton = Skeleton(
        bones = listOf(bone(children = listOf(1)), bone(translation = Vec3(0f, 1f, 0f))),
        roots = listOf(0),
    )

    private fun identitySkin(): Skin = Skin(joints = listOf(0, 1), inverseBindMatrices = listOf(Mat4(), Mat4()))

    @Test
    fun jointPaletteAtBindPoseMatchesSkeletonHierarchy() {
        val pose = AnimationPose(twoJointSkeleton())

        val palette = pose.jointPalette(identitySkin())

        // Joint 0 (root): identity. Joint 1 (child): translate (0,1,0) -- m13 is the
        // translation-Y component in this codebase's column-major Mat4 layout.
        assertEquals(0f, palette[13])
        assertEquals(1f, palette[16 + 13])
    }

    @Test
    fun jointPaletteIsDeterministic() {
        val pose = AnimationPose(twoJointSkeleton())
        val skin = identitySkin()

        val first = pose.jointPalette(skin)
        val second = pose.jointPalette(skin)

        assertEquals(first.toList(), second.toList())
    }

    @Test
    fun sampleAppliesAnimatedRotationToJointPalette() {
        val pose = AnimationPose(twoJointSkeleton())
        val skin = identitySkin()
        // Rotates joint 1 from identity (t=0) to 90 degrees about Y (t=1).
        val clip = AnimationClip(
            name = null,
            channels = listOf(
                AnimationChannel(
                    targetBone = 1,
                    property = AnimationProperty.Rotation,
                    sampler = AnimationSampler(
                        times = floatArrayOf(0f, 1f),
                        values = floatArrayOf(0f, 0f, 0f, 1f, 0f, 0.70710677f, 0f, 0.70710677f),
                        componentsPerKeyframe = 4,
                    ),
                ),
            ),
        )

        pose.sample(clip, timeSeconds = 0f)
        val atStart = pose.jointPalette(skin)
        pose.sample(clip, timeSeconds = 1f)
        val atEnd = pose.jointPalette(skin)

        // Joint 1's rotation column changed between the two sampled times.
        assertEquals(1f, atStart[16]) // m00 of joint 1 at identity rotation.
        assertTrue(kotlin.math.abs(atEnd[16]) < 0.01f) // m00 near 0 after ~90 degree turn.
    }

    @Test
    fun durationIsLastKeyframeTimeAcrossChannels() {
        val clip = AnimationClip(
            name = null,
            channels = listOf(
                AnimationChannel(
                    targetBone = 1,
                    property = AnimationProperty.Translation,
                    sampler = AnimationSampler(
                        times = floatArrayOf(0f, 0.5f, 2.5f),
                        values = FloatArray(9),
                        componentsPerKeyframe = 3,
                    ),
                ),
            ),
        )

        assertEquals(2.5f, clip.duration)
    }

    private fun translationOnlyClip(boneIndex: Int, x: Float): AnimationClip = AnimationClip(
        name = null,
        channels = listOf(
            AnimationChannel(
                targetBone = boneIndex,
                property = AnimationProperty.Translation,
                sampler = AnimationSampler(
                    times = floatArrayOf(0f),
                    values = floatArrayOf(x, 0f, 0f),
                    componentsPerKeyframe = 3,
                ),
            ),
        ),
    )

    @Test
    fun blendAtWeightZeroLeavesThisPoseUnchanged() {
        val skeleton = twoJointSkeleton()
        val a = AnimationPose(skeleton).apply { sample(translationOnlyClip(0, 4f), 0f) }
        val b = AnimationPose(skeleton).apply { sample(translationOnlyClip(0, 10f), 0f) }

        a.blend(b, 0f)

        assertEquals(4f, a.jointPalette(identitySkin())[12]) // m03 -- translation X of joint 0.
    }

    @Test
    fun blendAtWeightOneAdoptsTheOtherPoseExactly() {
        val skeleton = twoJointSkeleton()
        val a = AnimationPose(skeleton).apply { sample(translationOnlyClip(0, 4f), 0f) }
        val b = AnimationPose(skeleton).apply { sample(translationOnlyClip(0, 10f), 0f) }

        a.blend(b, 1f)

        assertEquals(10f, a.jointPalette(identitySkin())[12])
    }

    @Test
    fun blendAtHalfWeightIsTheArithmeticMeanForTranslation() {
        val skeleton = twoJointSkeleton()
        val a = AnimationPose(skeleton).apply { sample(translationOnlyClip(0, 4f), 0f) }
        val b = AnimationPose(skeleton).apply { sample(translationOnlyClip(0, 10f), 0f) }

        a.blend(b, 0.5f)

        assertEquals(7f, a.jointPalette(identitySkin())[12])
    }

    @Test
    fun blendInterpolatesRotationViaNlerpNotComponentLerp() {
        val skeleton = twoJointSkeleton()
        // 180-degree rotations about Y at opposite signs -- a naive per-component lerp of the
        // raw (x,y,z,w) quaternion values would degenerate toward a near-zero-length quaternion
        // at weight 0.5 (since the two are numerically opposite), while nlerp normalizes the
        // interpolated result to a unit quaternion, keeping it a valid rotation.
        val a = AnimationPose(skeleton).apply {
            sample(rotationClip(0, Quat(0f, 1f, 0f, 0f)), 0f)
        }
        val b = AnimationPose(skeleton).apply {
            sample(rotationClip(0, Quat(0f, -1f, 0f, 0f)), 0f)
        }

        a.blend(b, 0.5f)

        val blended = a.jointPalette(identitySkin())
        // A valid rotation matrix's basis columns stay unit length -- m00^2 + m01^2 + m02^2 == 1
        // (within tolerance). A degenerate near-zero quaternion would fail this.
        val lengthSquared = blended[0] * blended[0] + blended[1] * blended[1] + blended[2] * blended[2]
        assertTrue(kotlin.math.abs(lengthSquared - 1f) < 0.01f)
    }

    private fun rotationClip(boneIndex: Int, q: Quat): AnimationClip = AnimationClip(
        name = null,
        channels = listOf(
            AnimationChannel(
                targetBone = boneIndex,
                property = AnimationProperty.Rotation,
                sampler = AnimationSampler(
                    times = floatArrayOf(0f),
                    values = floatArrayOf(q.x, q.y, q.z, q.w),
                    componentsPerKeyframe = 4,
                ),
            ),
        ),
    )
}
