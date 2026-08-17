// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.asset.gltf

import io.github.ronjunevaldoz.awake.core.animation.AnimationClip
import io.github.ronjunevaldoz.awake.core.animation.Skeleton
import io.github.ronjunevaldoz.awake.core.animation.Skin

/** One glTF node that carries both a mesh and a skin reference -- the "find the thing to render
 * and animate" entry point every skinned-scene consumer starts from. [boneIndex] indexes
 * [LoadedSkinnedScene.skeleton]'s bones, [meshIndex]/[skinIndex] index [LoadedSkinnedScene.meshes]/
 * `.skins`. glTF-specific (a node/mesh/skin cross-reference is part of glTF's own scene-graph
 * schema, not something an engine-neutral [io.github.ronjunevaldoz.awake.core.animation.Bone]
 * carries) -- kept here rather than on `Bone` for that reason. */
data class SkinnedNodeRef(
    val boneIndex: Int,
    val meshIndex: Int,
    val skinIndex: Int,
)

/** Everything [SkinnedMeshDemo][io.github.ronjunevaldoz.awake.sample.scene3d.demos.SkinnedMeshDemo]
 * needs to render and animate a skinned glTF asset -- [skeleton] is the whole node hierarchy
 * (so joint-global transforms can be walked), every mesh definition (keyed by
 * [GltfDocument.meshes] index, each already carrying its own [GltfMesh.jointIndices]/
 * [GltfMesh.jointWeights]), every skin, every animation clip -- all in the engine-neutral shapes
 * [io.github.ronjunevaldoz.awake.core.animation] defines, not glTF's own JSON schema -- and
 * [skinnedNodes], the glTF-specific mesh+skin cross-references a consumer needs to find which
 * bone/mesh/skin triple to actually render. See [GltfParser.parseSkinned]. */
data class LoadedSkinnedScene(
    val skeleton: Skeleton,
    val meshes: List<GltfMesh>,
    val skins: List<Skin>,
    val clips: List<AnimationClip>,
    val skinnedNodes: List<SkinnedNodeRef>,
)

/** The mesh/skin/clip triple for one [SkinnedNodeRef] -- [clip] is `null` when the asset has no
 * animation clips at all (a static-pose skinned mesh is valid glTF). Bundles what every
 * skinned-mesh consumer immediately looks up after [GltfParser.parseSkinned] (find the node,
 * resolve its mesh/skin, grab the first clip), so that four-line lookup isn't hand-duplicated at
 * every call site. */
data class LoadedSkinnedAsset(
    val skeleton: Skeleton,
    val mesh: GltfMesh,
    val skin: Skin,
    val clip: AnimationClip?,
)

/** Resolves [node] (one of this scene's own [LoadedSkinnedScene.skinnedNodes]) into its mesh/
 * skin/first-clip triple. */
fun LoadedSkinnedScene.resolve(node: SkinnedNodeRef): LoadedSkinnedAsset = LoadedSkinnedAsset(
    skeleton = skeleton,
    mesh = meshes[node.meshIndex],
    skin = skins[node.skinIndex],
    clip = clips.firstOrNull(),
)

/** Convenience for the common "one skinned mesh per file" case -- resolves the FIRST skinned
 * node, or `null` if the file has none. */
fun LoadedSkinnedScene.firstSkinnedAsset(): LoadedSkinnedAsset? = skinnedNodes.firstOrNull()?.let { resolve(it) }
