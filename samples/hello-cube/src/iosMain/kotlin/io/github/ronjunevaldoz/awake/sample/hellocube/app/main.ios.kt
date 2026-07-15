// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.github.ronjunevaldoz.awake.sample.hellocube.app

import io.github.ronjunevaldoz.awake.vulkan.application.makeVulkanGameViewController
import platform.UIKit.UIViewController

fun makeSampleViewController(): UIViewController = makeVulkanGameViewController(createHelloCubeVulkanApplication())
