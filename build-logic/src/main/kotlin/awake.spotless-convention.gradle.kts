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
        // Also covers `expect `/`actual ` top-level declarations (e.g. DebugPng.kt and its
        // platform actuals) -- without these, the regex can't find a boundary at all and
        // Spotless throws `Unable to find delimiter regex` instead of applying the header.
        //
        // `/**` is included too, and deliberately listed first: Spotless's licenseHeader step
        // replaces EVERYTHING before the first line matching this regex, not just recognized
        // license text -- so on a file shaped like `// license\n\n/** doc comment */\nexpect
        // fun foo()`, matching only on `expect ` would treat the doc comment as disposable old
        // header content and silently delete it (confirmed: this happened for real on
        // DebugPng.kt/DebugReadout.kt/etc). Matching `/**` first stops the boundary search
        // right before the doc comment instead, so it's preserved as code.
        licenseHeader(
            """
            // Copyright (c) Ron June Valdoz
            // SPDX-License-Identifier: Apache-2.0
            """.trimIndent(),
            "^(/\\*\\*|package |@file|import |expect |actual |fun |class |object |interface |val |var |private |internal |public )"
        )
    }
}
