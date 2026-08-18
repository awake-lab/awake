// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.systems

import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.core.components.SpinControl
import io.github.ronjunevaldoz.awake.scene.core.systems.SpinSystem
import io.github.ronjunevaldoz.awake.studio.state.StudioContract
import io.github.ronjunevaldoz.awake.studio.state.StudioStore

/** Advances every [SpinControl] by its own rate, which is the half [SpinSystem] deliberately
 * does not do. Runs before it, so the composed transform is this frame's angle. */
internal class SpinClockSystem : System {
    override fun update(world: World, delta: Float) {
        world.queryEach(SpinControl::class) { _, spin -> spin.radians += spin.speed * delta }
    }
}

/** Ticks [delegate] only in [StudioContract.Mode.Play]. A wrapper rather than a mode check
 * inside each system: the systems are engine-owned, and an editor's mode is not their concern. */
internal class PlayModeSystem(private val delegate: System, private val store: StudioStore) : System {
    override fun update(world: World, delta: Float) {
        if (store.state.value.mode == StudioContract.Mode.Play) delegate.update(world, delta)
    }
}
