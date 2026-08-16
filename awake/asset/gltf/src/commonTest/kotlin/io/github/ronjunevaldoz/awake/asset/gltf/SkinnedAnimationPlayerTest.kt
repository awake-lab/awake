// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.asset.gltf

import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.core.math.Quat
import io.github.ronjunevaldoz.awake.core.math.Vec3
import kotlin.test.Test
import kotlin.test.assertEquals

/** [SkinnedAnimationPlayer] tests -- a synthetic 2-node scene (root joint + child joint,
 * translated 1 unit up), not a real glTF file, so the joint-palette math itself is isolated
 * from parsing. */
class SkinnedAnimationPlayerTest {
    private fun node(translation: Vec3 = Vec3(0f, 0f, 0f), children: List<Int> = emptyList()): LoadedNode =
        LoadedNode(translation, Quat.IDENTITY, Vec3(1f, 1f, 1f), matrix = null, children = children, mesh = null, skin = null)

    private fun twoJointScene(): LoadedSkinnedScene = LoadedSkinnedScene(
        nodes = listOf(node(children = listOf(1)), node(translation = Vec3(0f, 1f, 0f))),
        meshes = emptyList(),
        skins = emptyList(),
        animations = emptyList(),
        rootNodes = listOf(0),
    )

    private fun identitySkin(): LoadedSkin = LoadedSkin(joints = listOf(0, 1), inverseBindMatrices = listOf(Mat4(), Mat4()))

    @Test
    fun jointPaletteAtBindPoseMatchesNodeHierarchy() {
        val player = SkinnedAnimationPlayer(twoJointScene())

        val palette = player.jointPalette(identitySkin())

        // Joint 0 (root): identity. Joint 1 (child): translate (0,1,0) -- m13 is the
        // translation-Y component in this codebase's column-major Mat4 layout.
        assertEquals(0f, palette[13])
        assertEquals(1f, palette[16 + 13])
    }

    @Test
    fun jointPaletteIsDeterministic() {
        val player = SkinnedAnimationPlayer(twoJointScene())
        val skin = identitySkin()

        val first = player.jointPalette(skin)
        val second = player.jointPalette(skin)

        assertEquals(first.toList(), second.toList())
    }

    @Test
    fun sampleAppliesAnimatedRotationToJointPalette() {
        val scene = twoJointScene()
        val player = SkinnedAnimationPlayer(scene)
        val skin = identitySkin()
        // Rotates joint 1 from identity (t=0) to 90 degrees about Y (t=1).
        val animation = LoadedAnimation(
            channels = listOf(
                LoadedAnimationChannel(
                    targetNode = 1,
                    targetPath = "rotation",
                    sampler = LoadedAnimationSampler(
                        times = floatArrayOf(0f, 1f),
                        values = floatArrayOf(0f, 0f, 0f, 1f, 0f, 0.70710677f, 0f, 0.70710677f),
                        componentsPerKeyframe = 4,
                    ),
                ),
            ),
        )

        player.sample(animation, timeSeconds = 0f)
        val atStart = player.jointPalette(skin)
        player.sample(animation, timeSeconds = 1f)
        val atEnd = player.jointPalette(skin)

        // Joint 1's rotation column changed between the two sampled times.
        assertEquals(1f, atStart[16]) // m00 of joint 1 at identity rotation.
        assertEquals(true, kotlin.math.abs(atEnd[16]) < 0.01f) // m00 near 0 after ~90 degree turn.
    }

    @Test
    fun durationIsLastKeyframeTimeAcrossChannels() {
        val player = SkinnedAnimationPlayer(twoJointScene())
        val animation = LoadedAnimation(
            channels = listOf(
                LoadedAnimationChannel(
                    targetNode = 1,
                    targetPath = "translation",
                    sampler = LoadedAnimationSampler(
                        times = floatArrayOf(0f, 0.5f, 2.5f),
                        values = FloatArray(9),
                        componentsPerKeyframe = 3,
                    ),
                ),
            ),
        )

        assertEquals(2.5f, player.duration(animation))
    }
}
