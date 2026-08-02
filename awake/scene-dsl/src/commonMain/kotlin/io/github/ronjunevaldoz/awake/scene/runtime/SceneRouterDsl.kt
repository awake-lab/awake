// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime

import io.github.ronjunevaldoz.awake.engine.application.GameSpecDsl

fun GameSpecDsl.scenes(block: SceneRouterDsl.() -> Unit) {
    install(sceneFlow(block))
}

fun sceneRouter(block: SceneRouterDsl.() -> Unit): SceneRouterSpec {
    return sceneFlow(block)
}

fun GameSpecDsl.flow(block: SceneFlowDsl.() -> Unit) {
    install(sceneFlow(block))
}

fun sceneFlow(block: SceneFlowDsl.() -> Unit): SceneRouterSpec {
    return SceneFlowDsl().apply(block).build()
}

typealias SceneRouterDsl = SceneFlowDsl

class SceneFlowDsl internal constructor() {
    private val routes = mutableListOf<SceneRoute>()
    private var initialRouteId: String? = null

    fun initial(id: String) {
        initialRouteId = id
    }

    fun start(id: String) {
        initial(id)
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

    fun scene(
        id: String,
        label: String = id,
        spec: SceneGameSpec
    ) {
        route(id = id, label = label, spec = spec)
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

    fun scene(
        id: String,
        label: String = id,
        block: SceneGameDsl.() -> Unit
    ) {
        route(id = id, label = label, block = block)
    }

    internal fun build(): SceneRouterSpec {
        val initial = checkNotNull(initialRouteId) { "Scene router requires an initial route." }
        return SceneRouterSpec(routes = routes, initialRouteId = initial)
    }
}
