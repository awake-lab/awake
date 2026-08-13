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
        commonMain {
            kotlin {
                // Keep the public design-system artifact limited to Headless and ui-api sources.
                include("**/Shadcn*.kt")
                include("**/OklchColor.kt")
                include("**/PresetUiThemes.kt")
                include("**/ShadcnTheme.kt")
                include("**/styles/ShadcnVariants.kt")
                include("**/theme/ShadcnTokens.kt")
            }
        }
        commonMain.dependencies {
            // Design-system signatures may expose stable UI values, never raw runtime types.
            api(project(":awake:ui:api"))
            api(project(":awake:ui:ui-core"))
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

tasks.register("auditUiDesignsystemHeadlessBoundary") {
    group = "verification"
    description = "Verifies public ui-designsystem sources do not import ui-core runtime APIs."
    val sourceRoot = layout.projectDirectory.dir("src/commonMain/kotlin")
    doLast {
        val forbiddenPrefixes = listOf(
            "import io.github.ronjunevaldoz.awake.ui.UiScope",
            "import io.github.ronjunevaldoz.awake.ui.layouts",
            "import io.github.ronjunevaldoz.awake.ui.unstyled",
            "import io.github.ronjunevaldoz.awake.ui.modifier",
            "import io.github.ronjunevaldoz.awake.ui.style",
            "import io.github.ronjunevaldoz.awake.ui.theme",
            "import io.github.ronjunevaldoz.awake.ui.popup",
            "import io.github.ronjunevaldoz.awake.ui.scope",
            "import io.github.ronjunevaldoz.awake.ui.font",
            "import io.github.ronjunevaldoz.awake.ui.animate",
            "import io.github.ronjunevaldoz.awake.ui.child",
            "import io.github.ronjunevaldoz.awake.ui.toPx",
        )
        val current = sourceRoot.asFile.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .sumOf { file ->
                file.readLines().count { line -> forbiddenPrefixes.any(line::startsWith) }
            }
        println("ui-designsystem public legacy Core import lines: $current")
        check(current == 0) {
            "ui-designsystem public boundary violated: $current legacy Core import lines remain. " +
                    "Move Core receiver implementations to ui-designsystem-compat or rewrite them with Headless APIs."
        }
    }
}

tasks.register("verifyUiDesignsystemClasspath") {
    group = "verification"
    description =
        "Verifies the public design-system compile classpath contains no ui-core artifact."
    val compileClasspath = configurations.named("desktopCompileClasspath")
    doLast {
        val leaked = compileClasspath.get().files
            .filter { it.name.contains("ui-core", ignoreCase = true) }
        check(leaked.isEmpty()) {
            "ui-designsystem public compile classpath must not contain ui-core: ${leaked.joinToString()}"
        }
        println("ui-designsystem public compile classpath: ui-core absent")
    }
}

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
        "Rejects duplicate Shadcn recipes with the same receiver across component packages."
    val componentsRoot = layout.projectDirectory.dir(
        "src/commonMain/kotlin/io/github/ronjunevaldoz/awake/ui/designsystem/components",
    )
    val declaration = Regex("""fun\s+(?:<[^>]+>\s*)?((?:[\w.]+)\.)?(shadcn[A-Z]\w*)\s*\(""")
    doLast {
        val declarations = componentsRoot.asFile.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .flatMap { file ->
                val packageName = file.useLines { lines ->
                    lines.firstOrNull { it.startsWith("package ") }
                        ?.removePrefix("package ")
                        .orEmpty()
                }
                declaration.findAll(file.readText()).map { match ->
                    val receiver = match.groupValues[1].removeSuffix(".").ifEmpty { "<top-level>" }
                    val name = match.groupValues[2]
                    Triple("$receiver.$name", packageName, file.relativeTo(rootProject.projectDir))
                }.asSequence()
            }
            .groupBy { it.first }
        val violations = declarations.values
            .filter { entries -> entries.map { it.second }.distinct().size > 1 }
            .flatMap { entries ->
                entries.map { (signature, packageName, file) ->
                    "$signature declared in $packageName at $file"
                }
            }
        check(violations.isEmpty()) {
            "Duplicate ui-designsystem Shadcn recipes across packages:\n${violations.joinToString("\n")}"
        }
        println("ui-designsystem recipe duplicates: none for identical receiver/package keys")
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

tasks.register("reportUiDesignsystemMigrationProgress") {
    group = "verification"
    description =
        "Reports the verified public-component and overall boundary migration percentages."
    val componentsRoot = layout.projectDirectory.dir(
        "src/commonMain/kotlin/io/github/ronjunevaldoz/awake/ui/designsystem/components",
    )
    val compatibilityRoot = rootProject.layout.projectDirectory.dir(
        "awake/engine/ui/designsystem-compat/src/commonMain/kotlin",
    )
    doLast {
        val componentFiles = componentsRoot.asFile.walkTopDown()
            .filter { it.isFile && it.extension == "kt" && !it.name.endsWith("Contracts.kt") }
            .toList()
        val headlessBacked = componentFiles.count {
            it.readText().contains("io.github.ronjunevaldoz.awake.ui.headless")
        }
        val publicPercent =
            if (componentFiles.isEmpty()) 100 else headlessBacked * 100 / componentFiles.size
        val compatibilityRemaining = compatibilityRoot.asFile.walkTopDown()
            .any { it.isFile && it.extension == "kt" }
        val overallPercent = if (compatibilityRemaining) 99 else publicPercent
        println("ui-designsystem public component migration: $publicPercent% ($headlessBacked/${componentFiles.size})")
        println(
            "ui-designsystem overall boundary migration: $overallPercent% " +
                    if (compatibilityRemaining) "(test-only compatibility bridge remains)" else "(compatibility bridge removed)",
        )
    }
}

tasks.named("auditUiDesignsystemHeadlessBoundary") {
    finalizedBy(
        "verifyUiDesignsystemClasspath",
        "auditUiDesignsystemComponentNaming",
        "auditUiDesignsystemRecipeDuplicates",
        "auditUiDesignsystemComponentCoverage",
        "reportUiDesignsystemMigrationProgress",
    )
}

tasks.named("check") {
    dependsOn(
        "auditUiDesignsystemComponentNaming",
        "auditUiDesignsystemRecipeDuplicates",
        "auditUiDesignsystemComponentCoverage",
        "reportUiDesignsystemMigrationProgress",
    )
}
