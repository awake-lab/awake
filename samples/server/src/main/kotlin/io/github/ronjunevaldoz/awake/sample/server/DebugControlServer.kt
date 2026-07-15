// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.server

import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import java.net.BindException
import java.net.ServerSocket
import java.util.concurrent.ConcurrentLinkedQueue
import kotlinx.coroutines.CompletableDeferred

/**
 * A generic, protocol-agnostic Ktor WebSocket debug-control channel -- lets an AI agent (or
 * any other WebSocket client) drive/inspect a running desktop app deterministically, without
 * simulating mouse clicks or fighting real-GUI-window screenshot timing. Originally built for
 * `samples/hello-cube` (see `docs/MVP_PLAN.md`'s D24 decision log entry for that history) and
 * pulled out into this standalone module so any future desktop sample can reuse it without
 * depending on `hello-cube`'s own demo/command types -- this module knows nothing about
 * [TCommand]/[TResponse]'s shape, [parseCommand]/[encodeResponse] are supplied by the caller.
 *
 * **Hard constraint (see `docs/architecture.md`'s threading-model rules): one thread owns
 * every Vulkan call, per app instance, and `Application.update(delta)` is that thread's
 * synchronous entry point.** This server's own WebSocket handler runs on Ktor's own engine
 * thread/coroutine (`embeddedServer(...).start(wait = false)` -- non-blocking, background),
 * and must NEVER directly touch a consumer's live app state itself. Instead, every incoming
 * command is enqueued onto [commandQueue] as a `(TCommand, CompletableDeferred<TResponse>)`
 * pair and the handler suspends on the deferred; the consumer's own per-frame render loop
 * (the real render-thread owner) calls [drainCommands] once per frame, applies each
 * command's effect, and completes the paired deferred with the resulting [TResponse] -- the
 * only place any command's mutation or state read actually happens. Completing a
 * `CompletableDeferred` from one thread and awaiting it from a coroutine on another is a
 * normal, correct cross-thread synchronization primitive; it doesn't touch a Vulkan handle
 * itself, so it doesn't violate the rule above.
 */
class WebSocketDebugTransport<TCommand, TResponse>(
    private val port: Int = AWAKE_DEBUG_CONTROL_PORT,
    private val parseCommand: (String) -> TCommand?,
    private val encodeResponse: (TResponse) -> String
) : DebugTransport<TCommand, TResponse> {
    private val commandQueue = ConcurrentLinkedQueue<Pair<TCommand, CompletableDeferred<TResponse>>>()
    private var server: EmbeddedServer<*, *>? = null

    override fun start() {
        try {
            ServerSocket(port).use { }
        } catch (error: BindException) {
            throw BindException(port.toString()).also { it.initCause(error) }
        }
        server = embeddedServer(CIO, port = port) {
            install(WebSockets)
            routing {
                webSocket("/debug") {
                    for (frame in incoming) {
                        if (frame !is Frame.Text) continue
                        val command = parseCommand(frame.readText()) ?: continue
                        val deferred = CompletableDeferred<TResponse>()
                        commandQueue.add(command to deferred)
                        val response = deferred.await()
                        send(Frame.Text(encodeResponse(response)))
                    }
                }
            }
        }.also { it.start(wait = false) }
    }

    /** Called once per frame from the consumer's own render loop, on the render thread --
     * drains every command queued since the last call. Returns a plain list (not a
     * `Sequence`) so the caller can iterate it freely without re-touching [commandQueue]. */
    override fun drainCommands(): List<Pair<TCommand, CompletableDeferred<TResponse>>> {
        val drained = mutableListOf<Pair<TCommand, CompletableDeferred<TResponse>>>()
        while (true) {
            drained += commandQueue.poll() ?: break
        }
        return drained
    }

    override fun stop() {
        server?.stop(gracePeriodMillis = 200, timeoutMillis = 1000)
    }
}
