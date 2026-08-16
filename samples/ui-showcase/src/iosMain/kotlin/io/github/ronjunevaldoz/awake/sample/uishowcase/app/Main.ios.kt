// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.github.ronjunevaldoz.awake.sample.uishowcase.app

import io.github.ronjunevaldoz.awake.vulkan.application.makeVulkanGameViewController
import platform.UIKit.UIViewController

fun makeUiShowcaseViewController(): UIViewController = makeVulkanGameViewController(createUiShowcaseVulkanApplication())
