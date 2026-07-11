import com.diffplug.gradle.spotless.SpotlessExtension

plugins {
    id("com.diffplug.spotless")
}

extensions.configure<SpotlessExtension> {
    kotlin {
        target("src/**/*.kt")
        targetExclude(
            "**/build/**",
            // The only file in the codebase with neither a `package` nor an `import` line --
            // it has a real explanatory comment sitting where those would normally anchor the
            // license-header delimiter below, so Spotless's boundary search walks past it and
            // treats that comment as replaceable header cruft (confirmed: it deleted the
            // comment on a real spotlessApply run). Header applied manually there instead;
            // excluded here rather than risk this recurring for any similar file added later.
            "**/DesktopVulkanCompanionWindow.kt"
        )
        // Broadened past Spotless's default `^(package |@file|import )` delimiter -- a few
        // files in this codebase have no package/import line at all (default-package,
        // top-level-only scripts), so the header would never find its end anchor otherwise.
        licenseHeader(
            """
            // Copyright (c) Ron June Valdoz
            // SPDX-License-Identifier: Apache-2.0
            """.trimIndent(),
            "^(package |@file|import |fun |class |object |interface |val |var |private |internal |public )"
        )
    }
}
