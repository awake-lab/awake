// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.scene

import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameSpec
import io.github.ronjunevaldoz.awake.scene.runtime.entities.cameraEntity
import io.github.ronjunevaldoz.awake.scene.runtime.entities.meshEntity
import io.github.ronjunevaldoz.awake.scene.runtime.systems.orbitCameraSystem
import io.github.ronjunevaldoz.awake.scene.runtime.systems.playerControlSystem
import io.github.ronjunevaldoz.awake.scene.runtime.sceneGame

internal const val UI_SHOWCASE_SCENE_CATALOG = "catalog"

internal fun uiShowcaseSceneSpec(): SceneGameSpec = sceneGame {
    name(UI_SHOWCASE_SCENE_CATALOG)
    cameraEntity(
        name = "camera",
        transform = { position(0f, 1.4f, 8.5f) },
        camera = {
            eye(0f, 1.4f, 8.5f)
            center(0f, 0f, 0f)
            primary(true)
        }
    )
    meshEntity(
        name = "centerpiece",
        mesh = "cube",
        material = "default",
        transform = { scale(1.05f, 1.05f, 1.05f) }
    )
    meshEntity(
        name = "left",
        mesh = "cube",
        material = "default",
        transform = {
            position(-2.8f, 0.25f, -1.8f)
            scale(0.72f, 0.72f, 0.72f)
        }
    )
    meshEntity(
        name = "right",
        mesh = "cube",
        material = "default",
        transform = {
            position(2.8f, -0.15f, -1.5f)
            scale(0.82f, 0.82f, 0.82f)
        }
    )
    assets {
        mesh("cube") {
            renderer.createMesh(uiShowcaseCubeGeometry)
        }
        material("default") {
            renderer.createMaterial()
        }
    }
    val playerControl = playerControlSystem()
    val orbitSystem = orbitCameraSystem(
        target = "centerpiece",
        camera = "camera",
        initialDistance = 8.5f,
        initialPitch = 0.22f,
        autoRotateSpeed = 0.15f
    )
    update { delta, _ ->
        update(playerControl, delta)
        update(orbitSystem, delta)
    }
}
