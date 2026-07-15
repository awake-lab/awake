// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.server

import java.net.BindException
import java.util.concurrent.ConcurrentLinkedQueue
import kotlinx.coroutines.CompletableDeferred

const val AWAKE_DEBUG_CONTROL_PORT = 42770

interface DebugControlTransport<TCommand, TResponse> {
    fun start()
    fun drainCommands(): List<Pair<TCommand, CompletableDeferred<TResponse>>>
    fun stop()
}

class DebugControlLoop<TCommand, TResponse>(
    private val transport: DebugControlTransport<TCommand, TResponse>,
    private val applyCommand: (TCommand) -> Unit,
    private val snapshot: () -> TResponse
) {
    fun start() {
        transport.start()
    }

    fun beforeFrame() {
        transport.drainCommands().forEach { (command, deferred) ->
            applyCommand(command)
            deferred.complete(snapshot())
        }
    }

    fun stop() {
        transport.stop()
    }
}

fun <TCommand, TResponse> debugControlLoop(
    port: Int = AWAKE_DEBUG_CONTROL_PORT,
    parseCommand: (String) -> TCommand?,
    encodeResponse: (TResponse) -> String,
    applyCommand: (TCommand) -> Unit,
    snapshot: () -> TResponse
): DebugControlLoop<TCommand, TResponse> {
    return DebugControlLoop(
        transport = DebugControlServer(
            port = port,
            parseCommand = parseCommand,
            encodeResponse = encodeResponse
        ),
        applyCommand = applyCommand,
        snapshot = snapshot
    )
}

fun <TCommand, TResponse> withOptionalDebugControlLoop(
    enabled: Boolean,
    createLoop: () -> DebugControlLoop<TCommand, TResponse>,
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

internal class RecordingDebugControlTransport<TCommand, TResponse> : DebugControlTransport<TCommand, TResponse> {
    private val queue = ConcurrentLinkedQueue<Pair<TCommand, CompletableDeferred<TResponse>>>()
    var started = false
        private set
    var stopped = false
        private set

    override fun start() {
        started = true
    }

    override fun drainCommands(): List<Pair<TCommand, CompletableDeferred<TResponse>>> {
        val drained = mutableListOf<Pair<TCommand, CompletableDeferred<TResponse>>>()
        while (true) {
            drained += queue.poll() ?: break
        }
        return drained
    }

    override fun stop() {
        stopped = true
    }

    fun enqueue(command: TCommand): CompletableDeferred<TResponse> {
        val deferred = CompletableDeferred<TResponse>()
        queue += command to deferred
        return deferred
    }
}
