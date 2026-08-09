// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.authoring.infrastructure

import io.github.ronjunevaldoz.awake.scene.authoring.SceneGameDsl
import io.github.ronjunevaldoz.awake.scene.core.systems.TransformSystem
import io.github.ronjunevaldoz.awake.scene.rendering.systems.RenderSystem

/**
 * Registers mandatory engine systems that must run every frame.
 * Extracted from core DSL to ensure Scene module doesn't hardcode system knowledge.
 */
internal fun SceneGameDsl.installInfrastructureSystems() {
    // These run LAST in the pipeline as they are added at the end of the build process.
    frameSystem("transform") { TransformSystem() }
    frameSystem("render") { RenderSystem(renderer) }
}
