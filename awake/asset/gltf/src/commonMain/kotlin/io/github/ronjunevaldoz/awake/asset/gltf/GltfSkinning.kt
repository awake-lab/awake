// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.mesh.gltf

import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.core.math.Quat
import io.github.ronjunevaldoz.awake.core.math.Vec3

/** One glTF node, resolved for skeletal playback -- [translation]/[rotation]/[scale] kept as
 * separate components (not pre-composed into one [Mat4]) so animation playback can overwrite
 * just the one component a channel targets, leaving the others at their authored bind-pose
 * value; [matrix] is non-null only for a node that used glTF's explicit `matrix` form instead of
 * TRS (per the spec, mutually exclusive -- and not itself animatable, so it never changes).
 * [children]/[mesh]/[skin] carried straight from the source node. Indexed the same way
 * [GltfDocument.nodes] is, so a joint index from [LoadedSkin.joints] is a direct index into the
 * scene's node list. */
data class LoadedNode(
    val translation: Vec3,
    val rotation: Quat,
    val scale: Vec3,
    val matrix: Mat4?,
    val children: List<Int>,
    val mesh: Int?,
    val skin: Int?,
) {
    /** This node's local transform -- [matrix] verbatim when present, otherwise composed from
     * [translation]/[rotation]/[scale] (which playback may have already overwritten). */
    fun localTransform(): Mat4 = matrix ?: Mat4.fromTrs(translation, rotation, scale)
}

/** [joints] is a list of node indices (into [LoadedSkinnedScene.nodes]); [inverseBindMatrices]
 * is the same length, one matrix per joint, already decoded from the skin's `MAT4` accessor. */
data class LoadedSkin(
    val joints: List<Int>,
    val inverseBindMatrices: List<Mat4>,
)

/** One glTF animation sampler, decoded -- [times] is the keyframe input accessor (seconds,
 * strictly increasing), [values] is the output accessor flattened to
 * `times.size * componentsPerKeyframe` floats (3 for translation/scale, 4 for a rotation
 * quaternion). Only `LINEAR` interpolation is sampled -- see [GltfAnimationSampler]'s own doc
 * comment. */
data class LoadedAnimationSampler(
    val times: FloatArray,
    val values: FloatArray,
    val componentsPerKeyframe: Int,
)

data class LoadedAnimationChannel(
    val targetNode: Int,
    val targetPath: String,
    val sampler: LoadedAnimationSampler,
)

data class LoadedAnimation(val channels: List<LoadedAnimationChannel>)

/** Everything [SkinnedMeshDemo][io.github.ronjunevaldoz.awake.sample.scene3d.demos.SkinnedMeshDemo]
 * needs to render and animate a skinned glTF asset -- the full node hierarchy (so joint-global
 * transforms can be walked), every mesh definition (keyed by [GltfDocument.meshes] index, each
 * already carrying its own [GltfMesh.jointIndices]/[GltfMesh.jointWeights]), every skin, every
 * animation clip, and which nodes [rootNodes] are the scene's own roots. See
 * [GltfParser.parseSkinned]. */
data class LoadedSkinnedScene(
    val nodes: List<LoadedNode>,
    val meshes: List<GltfMesh>,
    val skins: List<LoadedSkin>,
    val animations: List<LoadedAnimation>,
    val rootNodes: List<Int>,
)
