/*
 * Awake
 * Awake.awake-scene.commonMain
 *
 * Copyright (c) ronjunevaldoz 2023.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.ronjunevaldoz.awake.scene.systems

import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.components.Camera
import io.github.ronjunevaldoz.awake.scene.components.Transform

/**
 * MVP1a's first real per-frame writer of [Camera.camera]'s `eye`/`center` -- every other
 * scene today authors these once in `scenes/mvp.scene.json` and never touches them again.
 * Fixed third-person offset, no collision/occlusion handling (matches [PlayerMovementSystem]'s
 * "deliberately simple" scope for this slice).
 */
class CameraFollowSystem(
    private val playerTransform: Transform,
    private val cameraComponent: Camera,
    private val offset: Vec3 = Vec3(0f, 3f, 6f)
) : System {
    override fun update(world: World, delta: Float) {
        val playerPosition = playerTransform.position
        val camera = cameraComponent.camera
        camera.eye.x = playerPosition.x + offset.x
        camera.eye.y = playerPosition.y + offset.y
        camera.eye.z = playerPosition.z + offset.z
        camera.center.x = playerPosition.x
        camera.center.y = playerPosition.y
        camera.center.z = playerPosition.z
    }
}
