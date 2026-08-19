// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime

import io.github.ronjunevaldoz.awake.core.input.InputSnapshot
import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.scene.rendering.components.MeshRenderer

typealias SceneRenderableFactory = SceneAppLifecycleRuntime.(SceneRenderableRequest) -> MeshRenderer
typealias SceneSystemFactory = SceneAppLifecycleRuntime.() -> System
typealias SceneUpdateBlock = SceneAppLifecycleRuntime.(delta: Float, input: InputSnapshot) -> Unit
typealias SceneOverlayBlock = SceneAppLifecycleRuntime.(viewportWidth: Float, viewportHeight: Float) -> Unit
typealias SceneReadyBlock = suspend SceneAppLifecycleRuntime.() -> Unit
typealias SceneDisposeBlock = SceneAppLifecycleRuntime.() -> Unit
