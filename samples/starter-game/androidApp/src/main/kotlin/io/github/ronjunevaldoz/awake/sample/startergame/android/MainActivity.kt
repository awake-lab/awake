// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.startergame.android

import android.app.Activity
import android.os.Bundle
import io.github.ronjunevaldoz.awake.core.graphics.VulkanView
import io.github.ronjunevaldoz.awake.sample.startergame.app.createStarterGameVulkanApplication

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(VulkanView(this, createStarterGameVulkanApplication()))
    }
}
