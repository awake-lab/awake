// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.animation

/** Which TRS component a channel drives -- a closed enum instead of an unchecked source-format
 * string (e.g. glTF's `"translation"/"rotation"/"scale"`), so [AnimationPose.sample]'s dispatch
 * can't silently no-op on a typo or an unrecognized value. */
enum class AnimationProperty { Translation, Rotation, Scale }

/** One channel's keyframe data -- [times] is strictly increasing (seconds), [values] is
 * flattened to `times.size * componentsPerKeyframe` floats (3 for translation/scale, 4 for a
 * rotation quaternion). Only linear interpolation between keyframes is sampled -- see
 * [AnimationPose.sample]'s own doc comment for why step/cubic-spline interpolation modes aren't
 * carried here. */
data class AnimationSampler(
    val times: FloatArray,
    val values: FloatArray,
    val componentsPerKeyframe: Int,
)

/** Targets one [Bone] (by index into the clip's [Skeleton]) with one [property]'s worth of
 * keyframes. */
data class AnimationChannel(
    val targetBone: Int,
    val property: AnimationProperty,
    val sampler: AnimationSampler,
)

/** A named, format-neutral animation clip -- [name] is the source asset's own clip name when it
 * had one (an importer that can't recover a name passes `null`). [duration] is the last keyframe
 * time across every channel, i.e. the clip's own length in seconds -- a property of the clip
 * itself, not of whatever is currently playing it. */
data class AnimationClip(
    val name: String?,
    val channels: List<AnimationChannel>,
) {
    val duration: Float
        get() = channels.maxOfOrNull { it.sampler.times.lastOrNull() ?: 0f } ?: 0f
}
