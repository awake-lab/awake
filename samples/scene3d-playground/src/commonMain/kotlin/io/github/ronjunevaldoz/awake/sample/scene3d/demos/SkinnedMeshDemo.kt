// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d.demos

import io.github.ronjunevaldoz.awake.core.math.Camera as CoreCamera
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.ecs.Entity
import io.github.ronjunevaldoz.awake.sample.scene3d.Scene3DDemo
import io.github.ronjunevaldoz.awake.scene.components.Camera as SceneCamera
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
 * larger project than this scaffold.
 *
 * Still spawns a trivial primary camera on activate -- see [HelloWorldTextDemo]'s doc comment
 * for why a camera-less demo otherwise renders a blank window ([io.github.ronjunevaldoz.awake
 * .scene.systems.RenderSystem] skips `renderer.draw()` entirely when no primary camera exists). */
internal object SkinnedMeshDemo {
    private var boneIndex = 0
    private var scrub = 0f
    private var loop = true
    private val bones = listOf("Root", "Spine", "Head", "Hand.L")
    private var cameraEntity: Entity? = null

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
        },
        onActivate = {
            val entity = world.create()
            world.add(entity, SceneCamera(EMPTY_CAMERA, isPrimary = true))
            cameraEntity = entity
        },
        onDeactivate = { world ->
            cameraEntity?.let { world.destroy(it) }
            cameraEntity = null
        }
    )

    // Same values as GameUiRuntime.EMPTY_UI_ONLY_CAMERA -- unused for any real 3D transform.
    private val EMPTY_CAMERA = CoreCamera(
        eye = Vec3(0f, 0f, 1f),
        center = Vec3(0f, 0f, 0f),
        fovYRadians = 1f,
        near = 0.1f,
        far = 10f
    )
}
