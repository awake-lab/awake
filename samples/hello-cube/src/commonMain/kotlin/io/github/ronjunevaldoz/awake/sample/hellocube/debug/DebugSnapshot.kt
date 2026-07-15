// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.hellocube.debug

import kotlinx.serialization.Serializable

@Serializable
data class DebugVec3(
    val x: Float,
    val y: Float,
    val z: Float
)

@Serializable
data class DebugSnapshot(
    val demoName: String,
    val debugLines: List<String>,
    val cameraEye: DebugVec3?,
    val cameraCenter: DebugVec3?,
    val minimapEnabled: Boolean?
)
