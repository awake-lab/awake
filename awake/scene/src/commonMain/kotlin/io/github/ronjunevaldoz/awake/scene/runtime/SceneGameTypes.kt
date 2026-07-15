// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime

import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.scene.components.MeshRenderer

typealias SceneRenderableFactory = SceneGameRuntime.(SceneRenderableRequest) -> MeshRenderer
typealias SceneSystemFactory = SceneGameRuntime.() -> System
typealias SceneUpdateBlock = SceneGameRuntime.(delta: Float) -> Unit
typealias SceneOverlayBlock = SceneGameRuntime.(viewportWidth: Float, viewportHeight: Float) -> Unit
typealias SceneReadyBlock = suspend SceneGameRuntime.() -> Unit
typealias SceneDisposeBlock = SceneGameRuntime.() -> Unit
