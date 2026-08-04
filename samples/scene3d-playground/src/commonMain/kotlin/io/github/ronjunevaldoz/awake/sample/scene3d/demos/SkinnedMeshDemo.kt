// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d.demos

import io.github.ronjunevaldoz.awake.sample.scene3d.Scene3DDemo
import io.github.ronjunevaldoz.awake.ui.designsystem.components.controls.shadcnSelect
import io.github.ronjunevaldoz.awake.ui.designsystem.components.input.shadcnFieldSliderWithValue
import io.github.ronjunevaldoz.awake.ui.designsystem.components.selection.shadcnSwitch
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSurface
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text

/** Scaffold only -- Awake's ECS/render-api has no bone/skinning support yet, so there's no real
 * animation to scrub. Proves the menu/controls wiring for the eventual bone-picker/scrub shape;
 * wiring a real skinned mesh needs skeletal animation support in the engine first, a separate,
 * larger project than this scaffold. */
internal object SkinnedMeshDemo {
    private var boneIndex = 0
    private var scrub = 0f
    private var loop = true
    private val bones = listOf("Root", "Spine", "Head", "Hand.L")

    val entry = Scene3DDemo(
        id = "skinned-mesh",
        title = "Skinned mesh",
        renderViewport = {
            text(label = "Skinned mesh -- no skeletal animation support yet")
        },
        renderControls = {
            shadcnSurface(id = "skinned-mesh-controls-panel", modifier = Modifier.fillMaxWidth()) {
                boneIndex = shadcnSelect(id = "skinned-bone", options = bones, selectedIndex = boneIndex) ?: boneIndex
                scrub = shadcnFieldSliderWithValue(id = "skinned-scrub", label = "Scrub", min = 0f, max = 100f, value = scrub)
                loop = shadcnSwitch(id = "skinned-loop", checked = loop, label = "Loop")
            }
        }
    )
}
