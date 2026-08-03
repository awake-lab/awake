// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d

import io.github.ronjunevaldoz.awake.sample.scene3d.demos.GltfViewerDemo
import io.github.ronjunevaldoz.awake.sample.scene3d.demos.HelloWorldTextDemo
import io.github.ronjunevaldoz.awake.sample.scene3d.demos.RotatingCubeDemo
import io.github.ronjunevaldoz.awake.sample.scene3d.demos.SkinnedMeshDemo
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope

/** Menu entry + viewport/controls renderers for one entry in the 3D playground (see
 * [Scene3DPlaygroundUi]'s shell). Each real demo owns exactly one of these -- state, viewport
 * content, and controls panel content all live together in that demo's own file under
 * `demos/`, so a demo's pieces change together instead of being scattered across a shared
 * god-file. */
internal data class Scene3DDemo(
    val id: String,
    val title: String,
    val renderViewport: ColumnScope.() -> Unit,
    val renderControls: ColumnScope.() -> Unit,
)

/** Registry of every playground demo, in menu order. Add a new demo by creating a
 * `demos/YourDemo.kt` file (mirror [HelloWorldTextDemo]'s shape) and listing its `entry` here --
 * nothing else in this package needs to change. */
internal val Scene3DDemos: List<Scene3DDemo> = listOf(
    HelloWorldTextDemo.entry,
    RotatingCubeDemo.entry,
    GltfViewerDemo.entry,
    SkinnedMeshDemo.entry,
)

/** Which demo is active. Plain mutable holder (not a StateFlow) -- this playground has no
 * external observers to notify, unlike [io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState],
 * so the simpler shape is the honest one. */
internal class Scene3DPlaygroundState {
    var activeDemoId: String = Scene3DDemos.first().id
}
