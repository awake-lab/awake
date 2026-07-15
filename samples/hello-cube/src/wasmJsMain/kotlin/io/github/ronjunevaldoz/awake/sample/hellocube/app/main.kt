// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.hellocube.app

import io.github.ronjunevaldoz.awake.webgpu.application.launchWebGpuGame

fun main() {
    launchWebGpuGame(applicationFactory = ::createHelloCubeWebGpuApplication)
}
