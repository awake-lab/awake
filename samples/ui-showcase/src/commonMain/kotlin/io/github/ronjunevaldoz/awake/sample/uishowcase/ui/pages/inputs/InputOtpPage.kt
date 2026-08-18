// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.inputs

import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcaseCategory
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcasePage
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnInputOTP
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnMuted
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.headless.rememberStateValue
import io.github.ronjunevaldoz.awake.ui.headless.spacer

internal val InputOtpPage = ShowcasePage(
    id = "input-otp",
    title = "Input OTP",
    category = ShowcaseCategory.Inputs,
    description = "Accessible one-time password component with copy paste functionality.",
    usageCode = """shadcnInputOTP(id = "otp", value = otpCode, length = 6)""",
    referenceExample = "registry/new-york-v4/examples/input-otp-demo.tsx",
    previewHeight = 280,
    notes = listOf("Segmented digits with length mask and space separation."),
    hero = {
        var value by rememberStateValue("showcase-otp-demo", "value") { "482019" }
        shadcnMuted("Segmented one-time password entry.")
        spacer(Modifier.height(8f.dp))
        value = shadcnInputOTP(id = "showcase-otp-input", value = value, length = 6)
    },
)
