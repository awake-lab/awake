// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene

import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.components.SpinControl
import io.github.ronjunevaldoz.awake.scene.components.Transform
import io.github.ronjunevaldoz.awake.scene.systems.SpinSystem
import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals

/** SpinSystem doesn't advance [SpinControl.radians] itself -- whoever owns the spin rate
 * (auto-play clock, UI scrub slider, gameplay code) sets it before this system runs; this
 * system only composes [Transform.worldMatrix] from whatever [SpinControl] already holds. */
class SpinSystemTest {
    @Test
    fun composesWorldMatrixFromOffsetAndRadians() {
        val world = World()
        val entity = world.create()
        world.add(entity, Transform())
        world.add(entity, SpinControl().apply {
            offset = Vec3(0f, 2f, 0f)
            radians = (PI / 2).toFloat()
        })

        SpinSystem().update(world, 1f / 60f)

        val worldMatrix = world.get(entity, Transform::class)!!.worldMatrix
        // translate(0, 2, 0) * rotateY(90deg) -- translation column carries the offset,
        // untouched by the rotation composed after it.
        assertEquals(0f, worldMatrix.m03, ABSOLUTE_TOLERANCE)
        assertEquals(2f, worldMatrix.m13, ABSOLUTE_TOLERANCE)
        assertEquals(0f, worldMatrix.m23, ABSOLUTE_TOLERANCE)
    }

    @Test
    fun entityWithoutSpinControlIsSkippedWithoutThrowing() {
        val world = World()
        val entity = world.create()
        world.add(entity, Transform())

        SpinSystem().update(world, 1f / 60f)
    }

    @Test
    fun recomposesEveryUpdateCallFromWhateverRadiansIsCurrentlySet() {
        val world = World()
        val entity = world.create()
        world.add(entity, Transform())
        val spin = SpinControl()
        world.add(entity, spin)
        val system = SpinSystem()

        spin.radians = 0f
        system.update(world, 1f / 60f)
        val atZero = world.get(entity, Transform::class)!!.worldMatrix.m00

        spin.radians = (PI / 2).toFloat()
        system.update(world, 1f / 60f)
        val atQuarterTurn = world.get(entity, Transform::class)!!.worldMatrix.m00

        assertEquals(1f, atZero, ABSOLUTE_TOLERANCE)
        assertEquals(0f, atQuarterTurn, ABSOLUTE_TOLERANCE)
    }

    private companion object {
        const val ABSOLUTE_TOLERANCE = 0.0001f
    }
}
