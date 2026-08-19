// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.app

import io.github.ronjunevaldoz.awake.engine.app.shaderSet
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.webgpu.application.WebGpuEngine

private val UiShowcaseShaders = shaderSet("triangle")

fun createUiShowcaseWebGpuApplication(): WebGpuEngine = WebGpuEngine(
    shaderSet = UiShowcaseShaders,
    vertexFormat = VertexFormat.PositionColorUv,
    appLifecycle = uiShowcase(),
)
