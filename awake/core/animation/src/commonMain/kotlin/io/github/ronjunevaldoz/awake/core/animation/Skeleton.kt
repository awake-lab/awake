// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.animation

import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.core.math.Quat
import io.github.ronjunevaldoz.awake.core.math.Vec3

/** One bone, resolved for skeletal playback -- [translation]/[rotation]/[scale] kept as separate
 * components (not pre-composed into one [Mat4]) so animation playback can overwrite just the one
 * component a channel targets, leaving the others at their authored bind-pose value; [matrix] is
 * non-null only for a bone whose source used an explicit baked matrix instead of TRS (mutually
 * exclusive with TRS, and not itself animatable, so it never changes). [children] indexes other
 * bones in the same [Skeleton.bones] list this bone belongs to. Format-neutral -- an importer
 * (e.g. glTF's [io.github.ronjunevaldoz.awake.asset.gltf.GltfParser]) is responsible for mapping
 * its own source format's node/joint addressing onto this flat, index-based hierarchy. */
data class Bone(
    val translation: Vec3,
    val rotation: Quat,
    val scale: Vec3,
    val matrix: Mat4?,
    val children: List<Int>,
) {
    /** This bone's local transform -- [matrix] verbatim when present, otherwise composed from
     * [translation]/[rotation]/[scale] (which playback may have already overwritten). */
    fun localTransform(): Mat4 = matrix ?: Mat4.fromTrs(translation, rotation, scale)
}

/** A full bone hierarchy -- [bones] indexed the same way [AnimationChannel.targetBone] and
 * [Skin.joints] address them, [roots] the indices with no parent (walked top-down to compute
 * global transforms). Includes every ancestor a joint might need, not just the bones a [Skin]
 * calls out as actual skinning joints -- a joint's global transform depends on its whole
 * ancestor chain, which may include non-joint bones. */
data class Skeleton(
    val bones: List<Bone>,
    val roots: List<Int>,
)
