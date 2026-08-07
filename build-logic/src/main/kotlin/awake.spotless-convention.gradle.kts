import com.diffplug.gradle.spotless.SpotlessExtension

plugins {
    id("com.diffplug.spotless")
}

extensions.configure<SpotlessExtension> {
    kotlin {
        target("src/**/*.kt")
        // Rules configured here rather than in a root .editorconfig: Spotless resolves
        // .editorconfig relative to each subproject, and with this convention applied across
        // ~30 modules the root file was picked up inconsistently (no-wildcard-imports in
        // particular went unenforced in most of them). editorConfigOverride applies uniformly.
        ktlint("1.5.0").editorConfigOverride(
            mapOf(
                "ktlint_standard_no-wildcard-imports" to "enabled",
                // Off deliberately: this codebase's own convention is descriptive multi-line
                // explanations above a declaration, and the repo has many long string
                // literals (shader paths, error messages) that can't be wrapped meaningfully.
                "ktlint_standard_max-line-length" to "disabled",
                // Off: the UI DSL builds nested trailing-lambda trees where ktlint's preferred
                // wrapping fights the layout that makes the hierarchy readable.
                "ktlint_standard_function-signature" to "disabled",
                "ktlint_standard_multiline-expression-wrapping" to "disabled",
                "ktlint_standard_string-template-indent" to "disabled",
                // Off: this rule demands file RENAMES (matrix.kt -> Mat4.kt), which is a
                // separate decision from formatting and would bury real history in a
                // whitespace commit. Revisit as its own change if we want it.
                "ktlint_standard_filename" to "disabled",
                // Off: AwakeLogger.kt is deliberately content-free (license + package only)
                // and comments don't satisfy this rule. Deleting it is an API call to make on
                // its own, not inside a formatting pass.
                "ktlint_standard_no-empty-file" to "disabled",
                // Off: the hand-written Vk*CreateInfo structs annotate individual constructor
                // parameters with trailing comments naming the Vulkan spec field they map to.
                // Hoisting those above each parameter would separate the note from the field.
                "ktlint_standard_value-parameter-comment" to "disabled",
                // Off: vulkan-generator's packages mirror the Vulkan spec's own underscored
                // names so generated output stays traceable to the header it came from.
                "ktlint_standard_package-name" to "disabled"
            )
        )
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
