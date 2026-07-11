// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.hellocube.android

import SampleApplication
import android.app.Activity
import android.os.Bundle
import io.github.ronjunevaldoz.awake.core.graphics.VulkanView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(VulkanView(this, SampleApplication()))
    }
}
