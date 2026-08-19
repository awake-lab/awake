// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.render.passes

import io.github.ronjunevaldoz.awake.render.command.BufferHandle
import io.github.ronjunevaldoz.awake.render.command.CommandRecorder
import io.github.ronjunevaldoz.awake.render.command.MaterialBinding
import io.github.ronjunevaldoz.awake.render.command.PipelineHandle
import io.github.ronjunevaldoz.awake.render.command.PreparedDraw
import kotlin.test.Test
import kotlin.test.assertEquals

/** Records the call sequence instead of issuing it -- the whole point of the port is that this
 * is possible with no GPU. */
private class FakeRecorder : CommandRecorder {
    val calls = mutableListOf<String>()
    override fun bindPipeline(pipeline: PipelineHandle) = log("pipeline(${(pipeline as Named).name})")
    override fun bindMaterial(set: Int, binding: MaterialBinding) = log("material($set,${(binding as Named).name})")
    override fun bindVertexBuffer(binding: Int, buffer: BufferHandle) = log("vertex($binding,${(buffer as Named).name})")
    override fun bindIndexBuffer(buffer: BufferHandle) = log("index(${(buffer as Named).name})")
    override fun draw(vertexCount: Int, instanceCount: Int) = log("draw($vertexCount,$instanceCount)")
    override fun drawIndexed(indexCount: Int, instanceCount: Int) = log("drawIndexed($indexCount,$instanceCount)")
    private fun log(call: String) {
        calls += call
    }
}

private class Named(val name: String) :
    PipelineHandle,
    MaterialBinding,
    BufferHandle

private class FakeDraw(
    override val pipeline: PipelineHandle,
    override val materialBinding: MaterialBinding,
    override val vertexBuffer: BufferHandle?,
    override val indexBuffer: BufferHandle?,
    override val elementCount: Int,
    override val instances: Int = 1,
    override val instanceVertexBuffer: BufferHandle? = null,
    override val jointPaletteBinding: MaterialBinding? = null,
) : PreparedDraw

class SharedOpaqueRenderFeatureTest {

    @Test
    fun primaryGroupThenLinesThenRemainingGroups() {
        val primary = Named("primary")
        val extra = Named("extra")
        val linePipeline = Named("lines")
        val feature = SharedOpaqueRenderFeature()
        val recorder = FakeRecorder()

        feature.recordCommands(
            recorder = recorder,
            primaryPipeline = primary,
            grouped = linkedMapOf(
                extra to listOf(draw(extra, "e0")),
                primary to listOf(draw(primary, "p0")),
            ),
            lines = FakeDraw(
                pipeline = linePipeline,
                materialBinding = Named("lineUniform"),
                vertexBuffer = Named("lineVerts"),
                indexBuffer = null,
                elementCount = 4,
            ),
        )

        assertEquals(
            listOf(
                "pipeline(primary)",
                "vertex(0,p0v)", "material(0,p0m)", "index(p0i)", "drawIndexed(3,1)",
                "pipeline(lines)",
                "vertex(0,lineVerts)", "material(0,lineUniform)", "draw(4,1)",
                "pipeline(extra)",
                "vertex(0,e0v)", "material(0,e0m)", "index(e0i)", "drawIndexed(3,1)",
            ),
            recorder.calls,
        )
    }

    @Test
    fun emptyLineMeshSkipsItsPipelineBind() {
        val primary = Named("primary")
        val recorder = FakeRecorder()
        SharedOpaqueRenderFeature().recordCommands(
            recorder = recorder,
            primaryPipeline = primary,
            grouped = emptyMap(),
            lines = FakeDraw(
                pipeline = Named("lines"),
                materialBinding = Named("lineUniform"),
                vertexBuffer = Named("lineVerts"),
                indexBuffer = null,
                elementCount = 0,
            ),
        )
        assertEquals(listOf("pipeline(primary)"), recorder.calls)
    }

    @Test
    fun instancedDrawBindsModelsAndPaletteBeforeDrawing() {
        val primary = Named("primary")
        val recorder = FakeRecorder()
        SharedOpaqueRenderFeature().recordCommands(
            recorder = recorder,
            primaryPipeline = primary,
            grouped = mapOf(
                primary to listOf(
                    FakeDraw(
                        pipeline = primary,
                        materialBinding = Named("m"),
                        vertexBuffer = Named("v"),
                        indexBuffer = Named("i"),
                        elementCount = 6,
                        instances = 12,
                        instanceVertexBuffer = Named("models"),
                        jointPaletteBinding = Named("palette"),
                    ),
                ),
            ),
        )
        assertEquals(
            listOf(
                "pipeline(primary)",
                "vertex(0,v)",
                "material(0,m)",
                "vertex(1,models)",
                "material(1,palette)",
                "index(i)",
                "drawIndexed(6,12)",
            ),
            recorder.calls,
        )
    }

    private fun draw(pipeline: PipelineHandle, id: String) = FakeDraw(
        pipeline = pipeline,
        materialBinding = Named("${id}m"),
        vertexBuffer = Named("${id}v"),
        indexBuffer = Named("${id}i"),
        elementCount = 3,
    )
}
