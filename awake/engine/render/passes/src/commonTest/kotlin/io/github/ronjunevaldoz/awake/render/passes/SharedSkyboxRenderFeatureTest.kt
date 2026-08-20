// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.render.passes

import io.github.ronjunevaldoz.awake.render.command.BufferHandle
import io.github.ronjunevaldoz.awake.render.command.CommandRecorder
import io.github.ronjunevaldoz.awake.render.command.MaterialBinding
import io.github.ronjunevaldoz.awake.render.command.PipelineHandle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SharedSkyboxRenderFeatureTest {

    private class RecordingCommandRecorder : CommandRecorder {
        val calls = mutableListOf<String>()
        var boundPipeline: PipelineHandle? = null
        var boundMaterial: MaterialBinding? = null
        var drawnVertexCount: Int = 0

        override fun bindPipeline(pipeline: PipelineHandle) {
            calls += "bindPipeline"
            boundPipeline = pipeline
        }

        override fun bindMaterial(set: Int, binding: MaterialBinding) {
            calls += "bindMaterial:$set"
            boundMaterial = binding
        }

        override fun bindVertexBuffer(binding: Int, buffer: BufferHandle) {
            calls += "bindVertexBuffer:$binding"
        }

        override fun bindIndexBuffer(buffer: BufferHandle) {
            calls += "bindIndexBuffer"
        }

        override fun draw(vertexCount: Int, instanceCount: Int) {
            calls += "draw:$vertexCount:$instanceCount"
            drawnVertexCount = vertexCount
        }

        override fun drawIndexed(indexCount: Int, instanceCount: Int) {
            calls += "drawIndexed:$indexCount:$instanceCount"
        }
    }

    private object FakePipeline : PipelineHandle
    private object FakeBinding : MaterialBinding

    @Test
    fun testSkyboxRecording() {
        val recorder = RecordingCommandRecorder()
        val feature = SharedSkyboxRenderFeature()

        feature.recordCommands(
            recorder = recorder,
            pipeline = FakePipeline,
            uniformBinding = FakeBinding,
        )

        assertEquals(listOf("bindPipeline", "bindMaterial:0", "draw:3:1"), recorder.calls)
        assertEquals(FakePipeline, recorder.boundPipeline)
        assertEquals(FakeBinding, recorder.boundMaterial)
        assertEquals(3, recorder.drawnVertexCount)
    }
}
