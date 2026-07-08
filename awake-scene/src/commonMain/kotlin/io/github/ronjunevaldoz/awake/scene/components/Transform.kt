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

package io.github.ronjunevaldoz.awake.scene.components

import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.ecs.Entity

data class Transform(
    var position: Vec3 = Vec3(0f, 0f, 0f),
    var rotation: Vec3 = Vec3(0f, 0f, 0f),
    var scale: Vec3 = Vec3(1f, 1f, 1f),
    var parent: Entity? = null,
    var worldMatrix: Mat4 = Mat4()
) {
    fun localMatrix(): Mat4 {
        return Mat4()
            .translate(position.x, position.y, position.z)
            .rotateZ(rotation.z)
            .rotateY(rotation.y)
            .rotateX(rotation.x)
            .scale(scale.x, scale.y, scale.z)
    }
}
