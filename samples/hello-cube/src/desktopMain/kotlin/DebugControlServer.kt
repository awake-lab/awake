// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import java.util.concurrent.ConcurrentLinkedQueue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Desktop-only Ktor WebSocket debug-control channel for this sample -- lets an AI agent (or
 * any other WebSocket client) drive/inspect the running desktop demo deterministically
 * (switch demos, move the camera, read HUD state) without simulating mouse clicks or
 * fighting real-GLFW-window screenshot timing. wasmJs/browser don't need this (a browser
 * automation tool can already execute arbitrary JS in the running page); Android has no
 * equivalent network-reachable dev loop today either, so this is scoped to desktop only.
 *
 * **Hard constraint (see this project's `.claude/AGENTS.md` "Threading model" section): one
 * thread owns every Vulkan call, per app instance, and `Application.update(delta)` is that
 * thread's synchronous entry point.** This server's own WebSocket handler runs on Ktor's own
 * engine thread/coroutine (`embeddedServer(...).start(wait = false)` -- non-blocking,
 * background), and must NEVER directly touch [DemoCatalog]/ECS/`Transform`/`Camera` state
 * itself. Instead, every incoming command is enqueued onto [commandQueue] as a
 * `(DebugCommand, CompletableDeferred<DebugSnapshot>)` pair and the handler suspends on the
 * deferred; `Main.kt`'s own per-frame loop (the real render-thread owner) calls
 * [drainCommands] once per frame, applies each command's effect, and completes the paired
 * deferred with the resulting [DebugSnapshot] -- the only place any command's mutation or
 * state read actually happens. Completing a `CompletableDeferred` from one thread and
 * awaiting it from a coroutine on another is a normal, correct cross-thread synchronization
 * primitive; it doesn't touch a Vulkan handle itself, so it doesn't violate the rule above.
 */
class DebugControlServer(private val port: Int = 8090) {
    private val commandQueue = ConcurrentLinkedQueue<Pair<DebugCommand, CompletableDeferred<DebugSnapshot>>>()
    private var server: EmbeddedServer<*, *>? = null

    fun start() {
        server = embeddedServer(CIO, port = port) {
            install(WebSockets)
            routing {
                webSocket("/debug") {
                    for (frame in incoming) {
                        if (frame !is Frame.Text) continue
                        val command = parseDebugCommand(frame.readText()) ?: continue
                        val deferred = CompletableDeferred<DebugSnapshot>()
                        commandQueue.add(command to deferred)
                        val snapshot = deferred.await()
                        send(Frame.Text(Json.encodeToString(snapshot)))
                    }
                }
            }
        }.also { it.start(wait = false) }
    }

    /** Called once per frame from `Main.kt`'s render loop, on the render thread -- drains
     * every command queued since the last call. Returns a plain list (not a `Sequence`) so
     * the caller can iterate it freely without re-touching [commandQueue]. */
    fun drainCommands(): List<Pair<DebugCommand, CompletableDeferred<DebugSnapshot>>> {
        val drained = mutableListOf<Pair<DebugCommand, CompletableDeferred<DebugSnapshot>>>()
        while (true) {
            drained += commandQueue.poll() ?: break
        }
        return drained
    }

    fun stop() {
        server?.stop(gracePeriodMillis = 200, timeoutMillis = 1000)
    }
}
