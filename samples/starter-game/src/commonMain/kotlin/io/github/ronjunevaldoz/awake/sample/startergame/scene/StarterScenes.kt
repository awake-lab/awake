// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.startergame.scene

import io.github.ronjunevaldoz.awake.scene.components.MovementControl
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameSpec
import io.github.ronjunevaldoz.awake.scene.runtime.entities.cameraEntity
import io.github.ronjunevaldoz.awake.scene.runtime.entities.meshEntity
import io.github.ronjunevaldoz.awake.scene.runtime.systems.followCameraSystem
import io.github.ronjunevaldoz.awake.scene.runtime.systems.freeFlyCameraSystem
import io.github.ronjunevaldoz.awake.scene.runtime.systems.orbitCameraSystem
import io.github.ronjunevaldoz.awake.scene.runtime.systems.playerControlSystem
import io.github.ronjunevaldoz.awake.scene.runtime.sceneGame
import io.github.ronjunevaldoz.awake.scene.systems.PlayerMovementSystem

internal const val STARTER_SCENE_OVERVIEW = "overview"
internal const val STARTER_SCENE_EDITOR = "editor"
internal const val STARTER_SCENE_PLAYGROUND = "playground"
internal const val STARTER_SCENE_FOLLOW = "follow"

internal fun starterOverviewSceneSpec(): SceneGameSpec = sceneGame {
    name(STARTER_SCENE_OVERVIEW)
    cameraEntity(
        name = "camera",
        transform = { position(0f, 0f, 7f) },
        camera = {
            eye(0f, 1f, 7f)
            center(0f, 0f, 0f)
            primary(true)
        }
    )
    meshEntity(
        name = "cube",
        mesh = "cube",
        material = "default",
        transform = {
            scale(1.25f, 1.25f, 1.25f)
        }
    )
    sharedStarterAssets()
    playerControlSystem()
    orbitCameraSystem(target = "cube", camera = "camera", initialDistance = 7f, initialPitch = 0.3f, autoRotateSpeed = 0.35f)
}

internal fun starterEditorSceneSpec(): SceneGameSpec = sceneGame {
    name(STARTER_SCENE_EDITOR)
    cameraEntity(
        name = "camera",
        transform = { position(0f, 2f, 10f) },
        camera = {
            eye(0f, 2f, 10f)
            center(0f, 0f, 0f)
            primary(true)
        }
    )
    meshEntity(
        name = "centerpiece",
        mesh = "cube",
        material = "default",
        transform = {
            scale(1.2f, 1.2f, 1.2f)
        }
    )
    meshEntity(
        name = "left",
        mesh = "cube",
        material = "default",
        transform = {
            position(-3f, 0f, 0f)
            scale(0.65f, 0.65f, 0.65f)
        }
    )
    meshEntity(
        name = "right",
        mesh = "cube",
        material = "default",
        transform = {
            position(3f, 0f, 0f)
            scale(0.65f, 0.65f, 0.65f)
        }
    )
    sharedStarterAssets()
    playerControlSystem()
    orbitCameraSystem(target = "centerpiece", camera = "camera", initialDistance = 10f, autoRotateSpeed = 0.2f)
}

internal fun starterPlaygroundSceneSpec(): SceneGameSpec = sceneGame {
    name(STARTER_SCENE_PLAYGROUND)
    cameraEntity(
        name = "camera",
        transform = { position(0f, 3f, 10f) },
        camera = {
            eye(0f, 3f, 10f)
            center(0f, 0f, 0f)
            primary(true)
        }
    )
    meshEntity(
        name = "origin",
        mesh = "cube",
        material = "default",
        transform = { scale(1f, 1f, 1f) }
    )
    meshEntity(
        name = "raised",
        mesh = "cube",
        material = "default",
        transform = {
            position(0f, 2.5f, 0f)
            scale(0.7f, 0.7f, 0.7f)
        }
    )
    meshEntity(
        name = "offset",
        mesh = "cube",
        material = "default",
        transform = {
            position(-2.5f, 1f, -2.5f)
            scale(0.8f, 0.8f, 0.8f)
        }
    )
    sharedStarterAssets()
    playerControlSystem()
    freeFlyCameraSystem(camera = "camera")
}

/**
 * Third-person/POV camera demo (see followCameraSystem/FollowControl): WASD moves the "hero"
 * entity via [PlayerMovementSystem] + [MovementControl] -- PlayerControlSystem already fans
 * WASD out to MovementControl for any entity that has one, same mechanism as the starter
 * playground scene's kinematic movement -- and the camera trails behind it.
 */
internal fun starterFollowSceneSpec(): SceneGameSpec = sceneGame {
    name(STARTER_SCENE_FOLLOW)
    cameraEntity(
        name = "camera",
        transform = { position(0f, 3f, 6f) },
        camera = {
            eye(0f, 3f, 6f)
            center(0f, 0f, 0f)
            primary(true)
        }
    )
    meshEntity(
        name = "hero",
        mesh = "cube",
        material = "default",
        transform = { scale(1f, 1f, 1f) }
    )
    sharedStarterAssets()
    onReady {
        world.add(requireEntity("hero"), MovementControl())
    }
    playerControlSystem()
    system("heroMovement") { PlayerMovementSystem(requireTransform("hero")) }
    followCameraSystem(target = "hero", camera = "camera")
}

private fun io.github.ronjunevaldoz.awake.scene.runtime.SceneGameDsl.sharedStarterAssets() {
    assets {
        mesh("cube") {
            renderer.createMesh(starterCubeGeometry)
        }
        material("default") {
            renderer.createMaterial()
        }
    }
}
