// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.asset.gltf

import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.core.math.Quat
import io.github.ronjunevaldoz.awake.core.math.Vec3

/**
 * Samples a [LoadedAnimation] at a given time into a working copy of every node's TRS, then
 * walks [scene]'s node hierarchy to turn a [LoadedSkin]'s joints into the flat joint-matrix
 * palette a skinned material's uniform buffer expects. One player per loaded scene (holds its
 * own mutable per-node TRS state).
 */
class SkinnedAnimationPlayer(private val scene: LoadedSkinnedScene) {
    private val currentTranslation = Array(scene.nodes.size) { scene.nodes[it].translation }
    private val currentRotation = Array(scene.nodes.size) { scene.nodes[it].rotation }
    private val currentScale = Array(scene.nodes.size) { scene.nodes[it].scale }

    /** Last keyframe time across every channel -- the clip's own length in seconds. */
    fun duration(animation: LoadedAnimation): Float =
        animation.channels.maxOfOrNull { it.sampler.times.lastOrNull() ?: 0f } ?: 0f

    /** Samples [animation] at [timeSeconds] into the working TRS arrays -- a node with no
     * channel targeting it keeps whatever TRS it already had (its authored bind pose, until/
     * unless a previous [sample] call overwrote it). */
    fun sample(animation: LoadedAnimation, timeSeconds: Float) {
        animation.channels.forEach { channel ->
            val value = sampleChannel(channel.sampler, timeSeconds)
            when (channel.targetPath) {
                "translation" -> currentTranslation[channel.targetNode] = Vec3(value[0], value[1], value[2])
                "scale" -> currentScale[channel.targetNode] = Vec3(value[0], value[1], value[2])
                "rotation" -> currentRotation[channel.targetNode] =
                    Quat(value[0], value[1], value[2], value[3])
            }
        }
    }

    /** Finds [timeSeconds]'s enclosing keyframe pair in [sampler] and lerps (rotation: nlerps)
     * between them -- clamps to the first/last keyframe outside the clip's own time range. */
    private fun sampleChannel(sampler: LoadedAnimationSampler, timeSeconds: Float): FloatArray {
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

    /** Every joint's current global transform (walking [scene]'s node hierarchy from its own
     * roots, multiplying local transforms along the way -- same [Mat4.multiplyColumnMajor]
     * convention [GltfParser.parseScene] itself uses) times that joint's own inverse-bind
     * matrix, flattened into `16 * skin.joints.size` floats -- exactly the layout `skinned.wgsl`'s
     * `jointPalette` uniform array expects. */
    fun jointPalette(skin: LoadedSkin): FloatArray {
        val globalTransforms = arrayOfNulls<Mat4>(scene.nodes.size)

        fun computeGlobal(nodeIndex: Int, parent: Mat4): Mat4 {
            globalTransforms[nodeIndex]?.let { return it }
            val node = scene.nodes[nodeIndex]
            val local = node.matrix ?: Mat4.fromTrs(currentTranslation[nodeIndex], currentRotation[nodeIndex], currentScale[nodeIndex])
            val global = Mat4.multiplyColumnMajor(parent, local)
            globalTransforms[nodeIndex] = global
            return global
        }

        fun visit(nodeIndex: Int, parent: Mat4) {
            val global = computeGlobal(nodeIndex, parent)
            scene.nodes[nodeIndex].children.forEach { visit(it, global) }
        }
        scene.rootNodes.forEach { visit(it, Mat4()) }

        val result = FloatArray(skin.joints.size * 16)
        skin.joints.forEachIndexed { i, nodeIndex ->
            // A joint node unreachable from any scene root would leave this null -- shouldn't
            // happen for a well-formed glTF file (every joint is part of the visible hierarchy),
            // identity is a harmless fallback rather than a crash if one ever is.
            val global = globalTransforms[nodeIndex] ?: Mat4()
            val jointMatrix = Mat4.multiplyColumnMajor(global, skin.inverseBindMatrices[i])
            for (component in 0 until 16) result[i * 16 + component] = jointMatrix.data[component]
        }
        return result
    }
}
