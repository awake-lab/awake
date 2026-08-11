// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.demos.inputs

import io.github.ronjunevaldoz.awake.ui.designsystem.components.property.shadcnField
import io.github.ronjunevaldoz.awake.ui.designsystem.components.property.shadcnFieldDescription
import io.github.ronjunevaldoz.awake.ui.designsystem.components.property.shadcnFieldLabel
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnInputOTP
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSupportingText
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.spacer
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.rememberStateValue

internal fun ColumnScope.drawShadcnInputOtpDemoPreview() {
    var otpValue by context.rememberStateValue("showcase-otp-demo", "value") { "482019" }

    shadcnBadge("INPUT OTP", variant = ShadcnBadgeVariant.Primary)
    shadcnSupportingText("Segmented One-Time Password digit entry with automated character spacing and length masking.")
    spacer(Modifier.height(8f.dp))

    shadcnField(id = "showcase-otp-field") {
        shadcnFieldLabel("Verification Code")
        otpValue = shadcnInputOTP(
            id = "showcase-otp-input",
            value = otpValue,
            length = 6,
        )
        shadcnFieldDescription("Current entered code: $otpValue")
    }
}
