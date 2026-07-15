// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.server

import java.net.BindException
import java.util.concurrent.ConcurrentLinkedQueue
import kotlinx.coroutines.CompletableDeferred

const val AWAKE_DEBUG_CONTROL_PORT = 42770

interface DebugService<TCommand, TSnapshot> {
    fun handle(command: TCommand)
    fun snapshot(): TSnapshot
}

interface DebugTransport<TCommand, TSnapshot> {
    fun start()
    fun drainCommands(): List<Pair<TCommand, CompletableDeferred<TSnapshot>>>
    fun stop()
}

class DebugServiceLoop<TCommand, TSnapshot>(
    private val transport: DebugTransport<TCommand, TSnapshot>,
    private val service: DebugService<TCommand, TSnapshot>
) {
    fun start() {
        transport.start()
    }

    fun beforeFrame() {
        transport.drainCommands().forEach { (command, deferred) ->
            service.handle(command)
            deferred.complete(service.snapshot())
        }
    }

    fun stop() {
        transport.stop()
    }
}

fun <TCommand, TSnapshot> webSocketDebugLoop(
    port: Int = AWAKE_DEBUG_CONTROL_PORT,
    parseCommand: (String) -> TCommand?,
    encodeResponse: (TSnapshot) -> String,
    service: DebugService<TCommand, TSnapshot>
): DebugServiceLoop<TCommand, TSnapshot> {
    return DebugServiceLoop(
        transport = WebSocketDebugTransport(
            port = port,
            parseCommand = parseCommand,
            encodeResponse = encodeResponse
        ),
        service = service
    )
}

fun <TCommand, TSnapshot> withOptionalDebugLoop(
    enabled: Boolean,
    createLoop: () -> DebugServiceLoop<TCommand, TSnapshot>,
    run: (beforeFrame: () -> Unit, afterLoop: () -> Unit) -> Unit
) {
    var loop = if (enabled) createLoop() else null
    var stopped = false

    val stopLoop: () -> Unit = {
        if (!stopped) {
            stopped = true
            loop?.stop()
        }
    }

    if (loop != null) {
        try {
            loop.start()
        } catch (error: BindException) {
            val port = error.message
                ?.substringAfterLast(':')
                ?.trim()
                ?.toIntOrNull()
                ?: AWAKE_DEBUG_CONTROL_PORT
            System.err.println("Awake debug controls disabled: port $port is already in use.")
            loop = null
        }
    }

    try {
        run({ loop?.beforeFrame() }, stopLoop)
    } catch (t: Throwable) {
        stopLoop()
        throw t
    }
}

internal class RecordingDebugTransport<TCommand, TSnapshot> : DebugTransport<TCommand, TSnapshot> {
    private val queue = ConcurrentLinkedQueue<Pair<TCommand, CompletableDeferred<TSnapshot>>>()
    var started = false
        private set
    var stopped = false
        private set

    override fun start() {
        started = true
    }

    override fun drainCommands(): List<Pair<TCommand, CompletableDeferred<TSnapshot>>> {
        val drained = mutableListOf<Pair<TCommand, CompletableDeferred<TSnapshot>>>()
        while (true) {
            drained += queue.poll() ?: break
        }
        return drained
    }

    override fun stop() {
        stopped = true
    }

    fun enqueue(command: TCommand): CompletableDeferred<TSnapshot> {
        val deferred = CompletableDeferred<TSnapshot>()
        queue += command to deferred
        return deferred
    }
}
