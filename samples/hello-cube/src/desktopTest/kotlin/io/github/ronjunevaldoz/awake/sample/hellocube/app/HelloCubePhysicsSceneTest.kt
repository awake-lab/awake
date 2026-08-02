// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.hellocube.app

import io.github.ronjunevaldoz.awake.engine.application.gameSpec
import io.github.ronjunevaldoz.awake.engine.application.module
import io.github.ronjunevaldoz.awake.engine.application.requireService
import io.github.ronjunevaldoz.awake.sample.hellocube.state.HelloCubeRuntimeState
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

/**
 * Real, falsifiable proof the Phase 8 physics wiring (see docs/MVP_PLAN.md's Phase 8 exit
 * criteria: "PhysicsSystem integration with Phase 3 ECS" + "falling boxes onto a ground
 * plane on Android + desktop") actually runs inside the hello-cube scene end to end, not
 * just that it compiles -- mirrors
 * [io.github.ronjunevaldoz.awake.physics.jolt.JoltPhysicsWorldTest]'s own "step N fixed
 * ticks, assert the resulting position" shape, but through the full scene/ECS/PhysicsSystem
 * stack this sample wires up (real jolt-jni, real [SceneGameRuntime], real
 * `FixedTimestepLoop`) rather than talking to `PhysicsWorld` directly.
 *
 * Desktop-only (see [HelloCubeGameTest]'s commonTest suite for the platform-neutral wiring
 * assertions): [createPhysicsWorld] only has a real synchronous implementation on
 * desktop/Android/iOS today (see that file's own doc comment for why wasmJs returns `null`).
 */
class HelloCubePhysicsSceneTest {
    @Test
    fun fallingBoxSettlesOnGroundUnderRealGravity() = runTest {
        val state = HelloCubeRuntimeState()
        val game = gameSpec {
            window {
                title = "Physics"
                size(640, 480)
            }
            module(
                helloCubeGameModule(
                    state = state,
                    websocketControlsEnabled = false,
                    offscreenProofEnabled = false
                )
            )
        }.createGame()

        game.ready(FakeRenderer)
        val runtime = game.requireService<SceneGameRuntime>()
        val fallingBox = assertNotNull(
            runtime.findTransform("fallingBox"),
            "expected the hello-cube scene to have a 'fallingBox' entity with a Transform"
        )
        val startY = fallingBox.position.y

        val fixedDelta = 1f / 60f
        // 300 frames of exactly one fixed step each (frameDelta == fixedDelta -- see
        // FixedTimestepLoop.advance) = 5 simulated seconds, comfortably enough for a box
        // dropped from y=6 to fall and settle against the ground's y=0 top surface.
        repeat(300) { game.render(fixedDelta, 320f, 240f) }

        val settledY = fallingBox.position.y
        assertTrue(
            settledY < startY - 1f,
            "Expected the box to have fallen well below its start height $startY, but it's at $settledY"
        )
        assertTrue(
            abs(settledY - 0.5f) < 0.1f,
            "Expected the box to settle at ~0.5 (its half-extent, resting on the ground's " +
                "y=0 top surface) after 5s of simulated fall, but it settled at $settledY"
        )

        game.dispose()
    }
}
