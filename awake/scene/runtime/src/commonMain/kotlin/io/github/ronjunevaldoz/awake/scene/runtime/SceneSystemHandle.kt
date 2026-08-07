// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime

import io.github.ronjunevaldoz.awake.ecs.System

class SceneSystemHandle<T : System>(
    val name: String,
)

class SceneSystemRegistration(
    val handle: SceneSystemHandle<out System>,
    val phase: SceneSystemPhase,
    val factory: SceneSystemFactory,
)
