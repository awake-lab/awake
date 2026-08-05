// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d

import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.ecs.SystemFrequency
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.engine.application.GameModule
import io.github.ronjunevaldoz.awake.engine.application.gameModule
import io.github.ronjunevaldoz.awake.sample.scene3d.demos.GltfViewerDemo
import io.github.ronjunevaldoz.awake.sample.scene3d.demos.RotatingCubeDemo
import io.github.ronjunevaldoz.awake.sample.scene3d.demos.SkinnedMeshDemo
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime
import io.github.ronjunevaldoz.awake.scene.runtime.scene

/** The whole app -- this module's `app/Main.kt`/`app/main.kt` platform entry points install
 * this directly (see [io.github.ronjunevaldoz.awake.engine.application.gameDefinition]'s
 * `module(...)` call).
 *
 * Runs on the real ECS [io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime] (not
 * [io.github.ronjunevaldoz.awake.engine.application.GameUiRuntime]) -- every demo page spawns
 * its own [io.github.ronjunevaldoz.awake.ecs.World] entities on activation and destroys them on
 * deactivation (see [Scene3DDemo]'s own doc comment); the scene DSL's built-in infrastructure
 * render system is the one real `renderer.draw()` call per frame this relies on, replacing
 * `GameUiRuntime`'s old `provideDrawCalls` escape hatch. */
/** Dark neutral gray, the standard 3D-editor viewport background (Blender/Unity/Maya all use
 * a dark gray, not a light one) -- gives [RotatingCubeDemo]'s lighter gray grid lines and the
 * cube's own bright per-vertex colors actual contrast to pop against, unlike a light-gray
 * background sitting right next to light-gray grid lines. Applied unconditionally to every
 * demo's viewport pane (not just while [RotatingCubeDemo] is active) so the playground shell
 * looks consistent across pages instead of flashing to stark black on every other one. */
private val VIEWPORT_CLEAR_COLOR = floatArrayOf(0.14f, 0.14f, 0.16f, 1f)

internal fun scene3DPlaygroundModule(): GameModule {
    val state = Scene3DPlaygroundState()
    return gameModule {
        scene("scene3d-playground") {
            // Demo activate/deactivate/onUpdate must run at Infrastructure frequency (real
            // per-render-frame delta, called exactly once per SceneGameRuntime.render() call --
            // see FixedTimestepLoop's own doc comment), NOT via the scene{} DSL's `update{}`
            // block: that runs at the fixed SIMULATION rate, which can fire zero times in a
            // given render call whenever the real framerate dips even slightly below the fixed
            // step rate. RotatingCubeDemo's `renderer.drawDebugLines(...)` call only stages
            // lines for the CURRENT frame (unlike ECS component state, nothing resubmits a
            // frame's lines automatically) -- on a skipped-fixed-step frame that call simply
            // never happens, so the grid/axis lines (and, watching casually, the whole cube)
            // visibly blink out for that one frame. A real, reported regression from routing
            // this through `update{}` -- fixed here by driving it from a Infrastructure system
            // instead, exactly matching the pre-migration `GameUiRuntime.overlay{}` call site's
            // real-every-frame timing.
            system("demo-driver") {
                val runtime = this
                Scene3DDemoDriverSystem(runtime, state)
            }
            // The scene DSL appends its built-in transform/render infrastructure systems at
            // build time, after this demo-driver. Do not register another RenderSystem here:
            // doing so submits/presents the same frame twice, which shows up as scene3d UI
            // blinking even though ui-showcase (UI-only) stays stable.
            // Duck.gltf's byte load is suspend (see readResourceBytes) -- done once here,
            // regardless of which demo page is active at startup, so GltfViewerDemo.onActivate
            // (a plain non-suspend per-frame hook) only ever touches the already-parsed scene.
            // CesiumMan.gltf's own preload follows the exact same reason.
            onReady {
                GltfViewerDemo.preload()
                SkinnedMeshDemo.preload()
            }
            overlay { width, height ->
                renderer.clearColor = VIEWPORT_CLEAR_COLOR
                drawScene3DPlaygroundOverlay(state, width, height)
            }
        }
    }
}

/** Drives [Scene3DDemo.onActivate]/[onDeactivate]/[onUpdate] at Infrastructure frequency -- see
 * [scene3DPlaygroundModule]'s own comment for why this can't be the scene{} DSL's `update{}`
 * block. */
private class Scene3DDemoDriverSystem(
    private val runtime: SceneGameRuntime,
    private val state: Scene3DPlaygroundState
) : System {
    override val frequency: SystemFrequency = SystemFrequency.Infrastructure
    private var activatedDemoId: String? = null

    override fun update(world: World, delta: Float) {
        if (activatedDemoId != state.activeDemoId) {
            activatedDemoId?.let { previousId ->
                Scene3DDemos.first { it.id == previousId }.onDeactivate(world)
            }
            val activeDemo = Scene3DDemos.first { it.id == state.activeDemoId }
            activeDemo.onActivate(runtime)
            activatedDemoId = state.activeDemoId
        }
        Scene3DDemos.first { it.id == state.activeDemoId }.onUpdate(runtime, delta)
    }
}
