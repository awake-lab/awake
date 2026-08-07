// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d.demos

import io.github.ronjunevaldoz.awake.ecs.Entity
import io.github.ronjunevaldoz.awake.ecs.Poolable
import io.github.ronjunevaldoz.awake.ecs.component
import io.github.ronjunevaldoz.awake.sample.scene3d.Scene3DDemo
import io.github.ronjunevaldoz.awake.scene.components.CameraComponent
import io.github.ronjunevaldoz.awake.scene.components.CameraMode
import io.github.ronjunevaldoz.awake.scene.components.Name
import io.github.ronjunevaldoz.awake.scene.components.SpinControl
import io.github.ronjunevaldoz.awake.scene.components.Transform
import io.github.ronjunevaldoz.awake.scene.runtime.dsl.Modifier
import io.github.ronjunevaldoz.awake.scene.runtime.dsl.camera
import io.github.ronjunevaldoz.awake.scene.runtime.dsl.scene
import io.github.ronjunevaldoz.awake.scene.runtime.dsl.transform
import io.github.ronjunevaldoz.awake.ui.designsystem.components.selection.shadcnSwitch

class RotatingCubeSimplifiedState : Poolable {
    var wireframe: Boolean = false
    var showAimMarkers: Boolean = false

    override fun reset() {
        wireframe = false
        showAimMarkers = false
    }
}

internal object RotatingCubeSimplified {
    val entry = Scene3DDemo(
        id = "rotating-cube-simplified",
        title = "Rotating cube (Simplified)",
        renderViewport = { },
        renderControls = { scope ->
            val stateEntity = findEntity("DemoState") 
            if (stateEntity != null) {
                val demo by world.component<RotatingCubeSimplifiedState>(stateEntity)
                val cameraEntity = findEntity("MainCamera")
                if (cameraEntity != null) {
                    val camera by world.component<CameraComponent>(cameraEntity)

                    scope.renderCameraModeToggle(camera.mode) { camera.mode = it }
                    
                    demo.wireframe = scope.shadcnSwitch("ui-wireframe", demo.wireframe, "Wireframe")
                    demo.showAimMarkers = scope.shadcnSwitch("ui-markers", demo.showAimMarkers, "Show Markers")
                }
            }
        },
        onActivate = {
            world.scene {
                val cube = entity("SpinningCube", Modifier()
                    .transform(y = 0.5f)
                    .with(SpinControl().apply { speed = 1.5f })
                )

                entity("DemoState", Modifier().configure(::RotatingCubeSimplifiedState) {
                    showAimMarkers = true
                })

                entity("MainCamera", Modifier().camera(CameraMode.ThirdPerson, target = cube))
            }
        },
        onUpdate = { delta ->
            renderer.drawReferenceGrid()
            
            val stateEntity = findEntity("DemoState")
            if (stateEntity != null) {
                val demo by world.component<RotatingCubeSimplifiedState>(stateEntity)
                val cubeEntity = findEntity("SpinningCube")
                if (cubeEntity != null) {
                    val spin by world.component<SpinControl>(cubeEntity)
                    spin.radians = (spin.radians + delta * spin.speed)
                }
            }

            val cameraEntity = findEntity("MainCamera")
            val cubeEntity = findEntity("SpinningCube")
            if (cameraEntity != null && cubeEntity != null) {
                updateDemoCamera(
                    world = world,
                    cameraEntity = cameraEntity,
                    targetEntity = cubeEntity,
                    panningEntity = null,
                    onPanningEntityCreated = { }
                )
            }
        },
        onDeactivate = { world ->
            val entities = mutableListOf<Entity>()
            world.queryEach(RotatingCubeSimplifiedState::class) { entity, _ -> entities.add(entity) }
            world.queryEach(SpinControl::class) { entity, _ -> entities.add(entity) }
            // Find named entities
            world.queryEach(Name::class) { entity, name ->
                if (name.value == "MainCamera" || name.value == "SpinningCube" || name.value == "DemoState") {
                    entities.add(entity)
                }
            }
            entities.forEach { world.destroy(it) }
        }
    )
}
