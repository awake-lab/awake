// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.server

import kotlinx.coroutines.runBlocking
import java.net.BindException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DebugControlLoopTest {

    @Test
    fun beforeFrameAppliesCommandsAndCompletesResponses() = runBlocking {
        val transport = RecordingDebugTransport<String, String>()
        val handled = mutableListOf<String>()
        val loop = DebugServiceLoop(
            transport = transport,
            service = object : DebugService<String, String> {
                override fun handle(command: String) {
                    handled += command
                }

                override fun snapshot(): String = handled.joinToString(separator = ",")
            },
        )

        loop.start()
        val response = transport.enqueue("switch:editor")
        loop.beforeFrame()
        loop.stop()

        assertTrue(transport.started)
        assertTrue(transport.stopped)
        assertEquals(listOf("switch:editor"), handled)
        assertEquals("switch:editor", response.await())
    }

    @Test
    fun optionalHelperStartsAndStopsLoopExactlyOnce() = runBlocking {
        val transport = RecordingDebugTransport<String, String>()
        val handled = mutableListOf<String>()
        val deferred = transport.enqueue("switch:overview")

        withOptionalDebugLoop(
            enabled = true,
            createLoop = {
                DebugServiceLoop(
                    transport = transport,
                    service = object : DebugService<String, String> {
                        override fun handle(command: String) {
                            handled += command
                        }

                        override fun snapshot(): String = handled.joinToString(separator = ",")
                    },
                )
            },
        ) { beforeFrame, afterLoop ->
            beforeFrame()
            afterLoop()
            afterLoop()
        }

        assertTrue(transport.started)
        assertTrue(transport.stopped)
        assertEquals(listOf("switch:overview"), handled)
        assertEquals("switch:overview", deferred.await())
    }

    @Test
    fun optionalHelperSkipsLoopWhenDisabled() {
        var runCalls = 0

        withOptionalDebugLoop<String, String>(
            enabled = false,
            createLoop = { error("should not build loop") },
        ) { beforeFrame, afterLoop ->
            runCalls++
            beforeFrame()
            afterLoop()
        }

        assertEquals(1, runCalls)
    }

    @Test
    fun optionalHelperKeepsRunningWhenPortIsBusy() {
        var beforeFrameCalls = 0
        var afterLoopCalls = 0

        withOptionalDebugLoop<String, String>(
            enabled = true,
            createLoop = {
                DebugServiceLoop(
                    transport = object : DebugTransport<String, String> {
                        override fun start(): Unit = throw BindException(AWAKE_DEBUG_CONTROL_PORT.toString())

                        override fun drainCommands() = emptyList<Pair<String, kotlinx.coroutines.CompletableDeferred<String>>>()

                        override fun stop() = Unit
                    },
                    service = object : DebugService<String, String> {
                        override fun handle(command: String) = Unit

                        override fun snapshot(): String = ""
                    },
                )
            },
        ) { beforeFrame, afterLoop ->
            beforeFrameCalls += 1
            beforeFrame()
            afterLoopCalls += 1
            afterLoop()
        }

        assertEquals(1, beforeFrameCalls)
        assertEquals(1, afterLoopCalls)
    }
}
