// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime

import io.github.ronjunevaldoz.awake.engine.application.GameDsl
import io.github.ronjunevaldoz.awake.engine.application.GameModuleDsl

fun GameDsl.scenes(block: SceneRouterDsl.() -> Unit) {
    install(sceneRouter(block))
}

fun GameModuleDsl.scenes(block: SceneRouterDsl.() -> Unit) {
    install(sceneRouter(block))
}

fun sceneRouter(block: SceneRouterDsl.() -> Unit): SceneRouterSpec {
    return SceneRouterDsl().apply(block).build()
}

class SceneRouterDsl internal constructor() {
    private val routes = mutableListOf<SceneRoute>()
    private var initialRouteId: String? = null

    fun initial(id: String) {
        initialRouteId = id
    }

    fun route(
        id: String,
        label: String = id,
        spec: SceneGameSpec
    ) {
        require(routes.none { it.id == id }) { "Scene route '$id' is already registered." }
        routes += SceneRoute(id = id, label = label, spec = spec)
        if (initialRouteId == null) {
            initialRouteId = id
        }
    }

    fun route(
        id: String,
        label: String = id,
        block: SceneGameDsl.() -> Unit
    ) {
        route(
            id = id,
            label = label,
            spec = sceneGame {
                name(id)
                block()
            }
        )
    }

    internal fun build(): SceneRouterSpec {
        val initial = checkNotNull(initialRouteId) { "Scene router requires an initial route." }
        return SceneRouterSpec(routes = routes, initialRouteId = initial)
    }
}
