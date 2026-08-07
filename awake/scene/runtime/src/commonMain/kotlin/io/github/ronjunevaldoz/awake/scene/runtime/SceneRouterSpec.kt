// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime

import io.github.ronjunevaldoz.awake.engine.application.Game
import io.github.ronjunevaldoz.awake.engine.application.GameInstaller
import io.github.ronjunevaldoz.awake.engine.application.GameServiceLookup
import io.github.ronjunevaldoz.awake.engine.application.GameSpecBuilder
import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine

data class SceneRoute(
    val id: String,
    val label: String,
    val spec: SceneGameSpec,
)

data class SceneRouteInfo(
    val id: String,
    val label: String,
)

class SceneRouterSpec(
    routes: List<SceneRoute>,
    private val initialRouteId: String,
) : GameInstaller {
    internal val routes: List<SceneRoute> = routes.toList()

    init {
        require(this.routes.isNotEmpty()) { "Scene router requires at least one route." }
        require(this.routes.any { it.id == initialRouteId }) {
            "Scene router initial route '$initialRouteId' is not registered."
        }
    }

    override fun install(into: GameSpecBuilder) {
        val runtime = SceneRouterRuntime(routes, initialRouteId, into.serviceLookup())
        into.service(SceneRouterRuntime::class, runtime)
        into.ready { renderer -> runtime.ready(renderer) }
        into.render { delta, viewportWidth, viewportHeight -> runtime.render(delta, viewportWidth, viewportHeight) }
        into.resize { width, height -> runtime.resize(width, height) }
        into.pause { runtime.pause() }
        into.resume { runtime.resume() }
        into.dispose { runtime.dispose() }
    }
}

class SceneRouterRuntime internal constructor(
    routes: List<SceneRoute>,
    initialRouteId: String,
    private val services: GameServiceLookup,
) : Game {
    private val routesById = routes.associateBy(SceneRoute::id)
    val scenes: List<SceneRouteInfo> = routes.map { SceneRouteInfo(id = it.id, label = it.label) }

    private val initialRoute = routesById.getValue(initialRouteId)
    private var pendingRouteId: String? = null
    private var currentRoute: SceneRoute = initialRoute
    private var currentRuntime: SceneGameRuntime? = null
    private var renderer: Renderer? = null

    val activeSceneId: String
        get() = currentRoute.id

    val activeSceneLabel: String
        get() = currentRoute.label

    val sceneRuntime: SceneGameRuntime
        get() = checkNotNull(currentRuntime) { "Scene router is not ready yet." }

    override suspend fun ready(renderer: Renderer) {
        this.renderer = renderer
        activate(initialRoute.id)
    }

    override fun render(delta: Float, viewportWidth: Float, viewportHeight: Float) {
        applyPendingRouteIfNeeded()
        sceneRuntime.render(delta, viewportWidth, viewportHeight)
    }

    override fun resize(width: Float, height: Float) {
        currentRuntime?.resize(width, height)
    }

    override fun pause() {
        currentRuntime?.pause()
    }

    override fun resume() {
        currentRuntime?.resume()
    }

    override fun dispose() {
        currentRuntime?.dispose()
        currentRuntime = null
        pendingRouteId = null
        renderer = null
    }

    fun switchTo(sceneId: String) {
        require(routesById.containsKey(sceneId)) { "Scene '$sceneId' is not registered." }
        pendingRouteId = sceneId
    }

    fun currentScene(): SceneRouteInfo = SceneRouteInfo(activeSceneId, activeSceneLabel)

    private fun applyPendingRouteIfNeeded() {
        val nextRouteId = pendingRouteId ?: return
        pendingRouteId = null
        if (nextRouteId == activeSceneId) return
        activate(nextRouteId)
    }

    private fun activate(sceneId: String) {
        val route = routesById.getValue(sceneId)
        val activeRenderer = checkNotNull(renderer) { "Scene router cannot activate '$sceneId' before ready()." }
        currentRuntime?.dispose()
        currentRoute = route
        currentRuntime = SceneGameRuntime(route.spec).also { runtime ->
            runtime.initialize(services)
            runImmediateReady {
                runtime.ready(activeRenderer)
            }
        }
    }
}

private fun runImmediateReady(block: suspend () -> Unit) {
    var completed = false
    var failure: Throwable? = null
    block.startCoroutine(
        object : Continuation<Unit> {
            override val context = EmptyCoroutineContext

            override fun resumeWith(result: Result<Unit>) {
                completed = true
                failure = result.exceptionOrNull()
            }
        },
    )
    check(completed) {
        "Routed scene activation cannot suspend after startup. Keep routed scene ready() work immediate."
    }
    failure?.let { throw it }
}
