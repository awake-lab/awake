package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.demos.inputs

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnInputOTP
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnMuted
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.rememberStateValue
import io.github.ronjunevaldoz.awake.ui.headless.spacer

internal fun ColumnScope.drawShadcnInputOtpDemoPreview() {
    var value by rememberStateValue("showcase-otp-demo", "value") { "482019" }
    shadcnBadge(id = "showcase-badge-input-otp", label = "INPUT OTP", variant = ShadcnBadgeVariant.Primary)
    spacer(Modifier.height(8f.dp))
    shadcnMuted("Segmented one-time password entry.")
    spacer(Modifier.height(8f.dp))
    value = shadcnInputOTP(id = "showcase-otp-input", value = value, length = 6)
}
