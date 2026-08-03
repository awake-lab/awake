// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d.demos

import io.github.ronjunevaldoz.awake.sample.scene3d.Scene3DDemo
import io.github.ronjunevaldoz.awake.ui.designsystem.components.controls.shadcnSelect
import io.github.ronjunevaldoz.awake.ui.designsystem.components.selection.shadcnSwitch
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text

/** Scaffold only -- Awake has no glTF loader yet, so this has nothing real to load. Proves the
 * menu/controls wiring for the eventual model-picker/animation-clip shape; wiring a real model
 * needs a glTF import pipeline (mesh/material/skin/animation parsing) that doesn't exist in this
 * engine yet -- a much larger, separate project, not part of this scaffold. */
internal object GltfViewerDemo {
    private var modelIndex = 0
    private var animationClipIndex = 0
    private var autoplay = true
    private val models = listOf("DamagedHelmet", "Fox", "Avocado")
    private val clips = listOf("Idle", "Walk", "Run")

    val entry = Scene3DDemo(
        id = "gltf-viewer",
        title = "glTF viewer",
        renderViewport = {
            text(label = "glTF viewer -- no loader in this engine yet")
        },
        renderControls = {
            modelIndex = shadcnSelect(id = "gltf-model", options = models, selectedIndex = modelIndex) ?: modelIndex
            animationClipIndex = shadcnSelect(id = "gltf-clip", options = clips, selectedIndex = animationClipIndex) ?: animationClipIndex
            autoplay = shadcnSwitch(id = "gltf-autoplay", checked = autoplay, label = "Autoplay")
        }
    )
}
