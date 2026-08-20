plugins {
    id("awake.kmp-library-convention")
    id("awake.dokka-convention")
    id("awake.detekt-convention")
    id("awake.spotless-convention")
    id("awake.ui-ownership-convention")
    id("awake.ui-authored-units-convention")
    id("awake.test-resources-convention")
}

kotlin {
    android {
        namespace = "io.github.ronjunevaldoz.awake.ui.designsystem"
    }

    sourceSets {
        commonMain.dependencies {
            // Core is exposed for its Style/UiLocal contract types, which appear in
            // public design-system signatures. Runtime-package imports are rejected by
            // verifyUiOwnership (awake.ui-ownership-convention).
            implementation(project(":awake:ui:ui-core"))
            api(project(":awake:ui:tailwind"))
            api(project(":awake:ui:headless"))
            api(project(":awake:ui:heroicons"))
        }
        commonTest.dependencies {
            // Core remains available to the frame/test harness; design-system recipes themselves
            // are verified without a compatibility-module dependency.
            implementation(project(":awake:ui:ui-core"))
            implementation(project(":awake:ui:testing"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(kotlin("test"))
        }
    }
}

awakeTestResources {
    roots.from(layout.projectDirectory.dir("src/commonMain/resources"))
}

// The Core-import boundary is enforced by verifyUiOwnership (awake.ui-ownership-convention);
// the former auditUiDesignsystemHeadlessBoundary/verifyUiDesignsystemClasspath/
// reportUiDesignsystemMigrationProgress tasks were deleted 2026-08-17 — the first duplicated
// the convention's rules without being wired into `check`, the second asserted the opposite
// of its own description, and the third read a module path deleted with designsystem-compat.

tasks.register("auditUiDesignsystemComponentNaming") {
    group = "verification"
    description =
        "Verifies design-system component files use Shadcn naming and matching family packages."
    val componentsRoot = layout.projectDirectory.dir(
        "src/commonMain/kotlin/io/github/ronjunevaldoz/awake/ui/designsystem/components",
    )
    val packagePrefix = "io.github.ronjunevaldoz.awake.ui.designsystem.components"
    doLast {
        val violations = componentsRoot.asFile.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .mapNotNull { file ->
                val relative = file.relativeTo(componentsRoot.asFile).invariantSeparatorsPath
                val family = relative.substringBeforeLast('/', missingDelimiterValue = "")
                val expectedPackage = if (family.isEmpty()) {
                    packagePrefix
                } else {
                    "$packagePrefix.${family.replace('/', '.')}"
                }
                val packageDeclaration = file.useLines { lines ->
                    lines.firstOrNull { it.startsWith("package ") }
                }
                when {
                    !file.name.startsWith("Shadcn") ->
                        "${file.relativeTo(rootProject.projectDir)} must start with Shadcn"

                    packageDeclaration != "package $expectedPackage" ->
                        "${file.relativeTo(rootProject.projectDir)} must declare package $expectedPackage"

                    else -> null
                }
            }
            .toList()
        check(violations.isEmpty()) {
            "ui-designsystem component naming/package violations:\n${violations.joinToString("\n")}"
        }
        println(
            "ui-designsystem component naming: ${
                componentsRoot.asFile.walkTopDown().count { it.isFile && it.extension == "kt" }
            } files verified"
        )
    }
}

tasks.register("auditUiDesignsystemRecipeDuplicates") {
    group = "verification"
    description =
        "Rejects the same-receiver Shadcn recipe declared in more than one file."
    val componentsRoot = layout.projectDirectory.dir(
        "src/commonMain/kotlin/io/github/ronjunevaldoz/awake/ui/designsystem/components",
    )
    val declaration = Regex("""fun\s+(?:<[^>]+>\s*)?((?:[\w.]+)\.)?(shadcn[A-Z]\w*)\s*\(""")
    doLast {
        // Keyed by file, not package: components live flat in one package by design, so a
        // package key can never fire (two shadcnEmpty implementations shipped behind it).
        // Same-file overloads (string convenience beside the slot form) stay legal.
        val declarations = componentsRoot.asFile.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .flatMap { file ->
                declaration.findAll(file.readText()).map { match ->
                    val receiver = match.groupValues[1].removeSuffix(".").ifEmpty { "<top-level>" }
                    val name = match.groupValues[2]
                    "$receiver.$name" to file.relativeTo(rootProject.projectDir)
                }.asSequence()
            }
            .groupBy({ it.first }, { it.second })
        val violations = declarations
            .filterValues { files -> files.distinct().size > 1 }
            .flatMap { (signature, files) ->
                files.distinct().map { file -> "$signature declared in $file" }
            }
        check(violations.isEmpty()) {
            "Same ui-designsystem Shadcn recipe declared in multiple files:\n${violations.joinToString("\n")}"
        }
        println("ui-designsystem recipe duplicates: none across files")
    }
}

tasks.register("auditUiDesignsystemComponentCoverage") {
    group = "verification"
    description = "Verifies every public design-system recipe is backed by Headless behavior."
    val componentsRoot = layout.projectDirectory.dir(
        "src/commonMain/kotlin/io/github/ronjunevaldoz/awake/ui/designsystem/components",
    )
    doLast {
        val componentFiles = componentsRoot.asFile.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .toList()
        val violations = componentFiles
            .mapNotNull { file ->
                val source = file.readText()
                val isContractOnly = file.name.endsWith("Contracts.kt")
                when {
                    isContractOnly -> null
                    !source.contains("io.github.ronjunevaldoz.awake.ui.headless") ->
                        "${file.relativeTo(rootProject.projectDir)} must delegate through ui-headless"

                    else -> null
                }
            }
            .toList()
        check(violations.isEmpty()) {
            "ui-designsystem component coverage violations:\n${violations.joinToString("\n")}"
        }
        println(
            "ui-designsystem component coverage: " +
                    componentFiles.count { !it.name.endsWith("Contracts.kt") } +
                    " public files are Headless-backed",
        )
    }
}

tasks.named("check") {
    dependsOn(
        "auditUiDesignsystemComponentNaming",
        "auditUiDesignsystemRecipeDuplicates",
        "auditUiDesignsystemComponentCoverage",
    )
}
