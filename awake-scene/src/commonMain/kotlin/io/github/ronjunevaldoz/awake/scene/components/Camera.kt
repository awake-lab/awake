// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.components

import io.github.ronjunevaldoz.awake.core.math.Camera as CoreCamera

data class Camera(
    val camera: CoreCamera,
    val isPrimary: Boolean = true
)
