// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.github.ronjunevaldoz.awake.sample.scene3d.app

import io.github.ronjunevaldoz.awake.vulkan.application.makeVulkanGameViewController
import platform.UIKit.UIViewController

fun makeScene3DPlaygroundViewController(): UIViewController =
    makeVulkanGameViewController(createScene3DPlaygroundVulkanApplication())
