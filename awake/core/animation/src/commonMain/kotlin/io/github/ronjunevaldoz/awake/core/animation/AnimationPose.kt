// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.animation

import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.core.math.Quat
import io.github.ronjunevaldoz.awake.core.math.Vec3

/**
 * One [skeleton]'s current working pose -- a mutable per-bone TRS buffer, seeded from bind pose.
 * [sample] overwrites it from an [AnimationClip]; [blend] mixes it with another pose; [jointPalette]
 * turns it into the flat joint-matrix array a skinned material's uniform buffer expects.
 *
 * A pose is independent, cheap-to-construct state -- unlike this type's glTF-coupled predecessor
 * (`SkinnedAnimationPlayer`, one shared TRS buffer per loaded scene), nothing stops a caller from
 * holding several `AnimationPose`s over the same [Skeleton] at once (independently-timed crowd
 * instances, or the outgoing/current pair [AnimationCrossfade] needs for blending).
 */
class AnimationPose(private val skeleton: Skeleton) {
    private val translation = Array(skeleton.bones.size) { skeleton.bones[it].translation }
    private val rotation = Array(skeleton.bones.size) { skeleton.bones[it].rotation }
    private val scale = Array(skeleton.bones.size) { skeleton.bones[it].scale }

    /** Samples [clip] at [timeSeconds] into this pose's working TRS arrays -- a bone with no
     * channel targeting it keeps whatever TRS it already had (its authored bind pose, until/
     * unless a previous [sample] call overwrote it). */
    fun sample(clip: AnimationClip, timeSeconds: Float) {
        clip.channels.forEach { channel ->
            val value = sampleChannel(channel.sampler, timeSeconds)
            when (channel.property) {
                AnimationProperty.Translation -> translation[channel.targetBone] = Vec3(value[0], value[1], value[2])
                AnimationProperty.Scale -> scale[channel.targetBone] = Vec3(value[0], value[1], value[2])
                AnimationProperty.Rotation ->
                    rotation[channel.targetBone] = Quat(value[0], value[1], value[2], value[3])
            }
        }
    }

    /** Finds [timeSeconds]'s enclosing keyframe pair in [sampler] and lerps (rotation: nlerps)
     * between them -- clamps to the first/last keyframe outside the clip's own time range. */
    private fun sampleChannel(sampler: AnimationSampler, timeSeconds: Float): FloatArray {
        val times = sampler.times
        val comps = sampler.componentsPerKeyframe
        if (times.isEmpty()) return FloatArray(comps)
        if (timeSeconds <= times.first()) return sampler.values.copyOfRange(0, comps)
        if (timeSeconds >= times.last()) {
            val lastBase = (times.size - 1) * comps
            return sampler.values.copyOfRange(lastBase, lastBase + comps)
        }
        var hi = 1
        while (hi < times.size && times[hi] < timeSeconds) hi++
        val lo = hi - 1
        val t = (timeSeconds - times[lo]) / (times[hi] - times[lo])
        val loBase = lo * comps
        val hiBase = hi * comps
        return if (comps == 4) {
            val a = Quat(sampler.values[loBase], sampler.values[loBase + 1], sampler.values[loBase + 2], sampler.values[loBase + 3])
            val b = Quat(sampler.values[hiBase], sampler.values[hiBase + 1], sampler.values[hiBase + 2], sampler.values[hiBase + 3])
            val r = Quat.nlerp(a, b, t)
            floatArrayOf(r.x, r.y, r.z, r.w)
        } else {
            FloatArray(comps) { i -> sampler.values[loBase + i] + (sampler.values[hiBase + i] - sampler.values[loBase + i]) * t }
        }
    }

    /** Overwrites this pose's working TRS arrays with [other]'s -- used to reset a scratch pose
     * to a frozen base before blending it toward a live one each frame, so repeated blending
     * doesn't compound (see [AnimationCrossfade]). */
    fun copyFrom(other: AnimationPose) {
        for (i in translation.indices) {
            translation[i] = other.translation[i]
            rotation[i] = other.rotation[i]
            scale[i] = other.scale[i]
        }
    }

    /** Mutates this pose in place to become the interpolation of (this, [other]) at [weight] --
     * `0` leaves this pose unchanged, `1` adopts [other]'s pose exactly. Per-bone: translation/
     * scale lerp componentwise, rotation nlerps (never a naive per-component lerp, which would
     * visibly cut corners on a large rotation instead of sweeping through it). Both poses must
     * share the same [Skeleton] -- this is the caller's responsibility, same as every other
     * method here that assumes bone-index alignment. */
    fun blend(other: AnimationPose, weight: Float) {
        for (i in translation.indices) {
            translation[i] = translation[i].lerp(other.translation[i], weight)
            rotation[i] = Quat.nlerp(rotation[i], other.rotation[i], weight)
            scale[i] = scale[i].lerp(other.scale[i], weight)
        }
    }

    /** Every joint's current global transform (walking [skeleton]'s bone hierarchy from its own
     * roots, multiplying local transforms along the way) times that joint's own inverse-bind
     * matrix, flattened into `16 * skin.joints.size` floats -- one 4x4 column-major matrix per
     * joint, the layout a skinned material's `jointPalette` uniform array expects. */
    fun jointPalette(skin: Skin): FloatArray {
        val globalTransforms = arrayOfNulls<Mat4>(skeleton.bones.size)

        fun computeGlobal(boneIndex: Int, parent: Mat4): Mat4 {
            globalTransforms[boneIndex]?.let { return it }
            val bone = skeleton.bones[boneIndex]
            val local = bone.matrix ?: Mat4.fromTrs(translation[boneIndex], rotation[boneIndex], scale[boneIndex])
            val global = Mat4.multiplyColumnMajor(parent, local)
            globalTransforms[boneIndex] = global
            return global
        }

        fun visit(boneIndex: Int, parent: Mat4) {
            val global = computeGlobal(boneIndex, parent)
            skeleton.bones[boneIndex].children.forEach { visit(it, global) }
        }
        skeleton.roots.forEach { visit(it, Mat4()) }

        val result = FloatArray(skin.joints.size * 16)
        skin.joints.forEachIndexed { i, boneIndex ->
            // A joint bone unreachable from any skeleton root would leave this null -- shouldn't
            // happen for a well-formed skeleton (every joint is part of the hierarchy), identity
            // is a harmless fallback rather than a crash if one ever is.
            val global = globalTransforms[boneIndex] ?: Mat4()
            val jointMatrix = Mat4.multiplyColumnMajor(global, skin.inverseBindMatrices[i])
            for (component in 0 until 16) result[i * 16 + component] = jointMatrix.data[component]
        }
        return result
    }
}
