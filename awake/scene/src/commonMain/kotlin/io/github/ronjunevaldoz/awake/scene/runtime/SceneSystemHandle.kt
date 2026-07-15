// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime

import io.github.ronjunevaldoz.awake.ecs.System

class SceneSystemHandle<T : System> internal constructor(
    internal val name: String
)

internal class SceneSystemRegistration internal constructor(
    val handle: SceneSystemHandle<out System>,
    val factory: SceneSystemFactory
)
