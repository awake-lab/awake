// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.server

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DebugControlLoopTest {

    @Test
    fun beforeFrameAppliesCommandsAndCompletesResponses() = runBlocking {
        val transport = RecordingDebugControlTransport<String, String>()
        val handled = mutableListOf<String>()
        val loop = DebugControlLoop(
            transport = transport,
            applyCommand = handled::add,
            snapshot = { handled.joinToString(separator = ",") }
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
}
