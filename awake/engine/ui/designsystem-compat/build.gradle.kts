plugins {
    id("awake.kmp-library-convention")
    id("awake.dokka-convention")
    id("awake.detekt-convention")
    id("awake.spotless-convention")
}

kotlin {
    android {
        namespace = "io.github.ronjunevaldoz.awake.ui.designsystem.compat"
    }

    sourceSets {
        commonMain {
            // Keep legacy Core receiver implementations physically isolated from the
            // public design-system source tree.  This module is intentionally temporary:
            // consumers migrate to the Headless recipes, then this module can be deleted.
            kotlin.srcDir("src/commonMain/kotlin")
            dependencies {
                // The bridge is a test/migration implementation detail. Its public legacy
                // signatures still reference the old Core receiver and Design System values,
                // so these dependencies must remain visible to migration consumers until the
                // last fixture is converted.
                api(project(":awake:engine:ui:designsystem"))
                api(project(":awake:engine:ui:ui-core"))
            }
        }
    }
}

tasks.register("auditUiDesignsystemCompatConsumers") {
    group = "verification"
    description = "Ensures the temporary Core-receiver bridge is consumed only by approved migration targets."
    val repositoryRoot = rootProject.layout.projectDirectory
    val allowedConsumers = setOf("samples/ui-showcase/build.gradle.kts")
    doLast {
        val dependencyMarker = "project(\":awake:engine:ui:designsystem-compat\")"
        val unexpected = repositoryRoot.asFile.walkTopDown()
            .filter { it.isFile && it.name.endsWith(".gradle.kts") }
            .filterNot { it.path.contains("/build/") || it.path.contains("/.gradle/") }
            .filter { it.readText().contains(dependencyMarker) }
            .map { it.relativeTo(repositoryRoot.asFile).invariantSeparatorsPath }
            .filterNot(allowedConsumers::contains)
            .toList()
        check(unexpected.isEmpty()) {
            "Unexpected ui-designsystem-compat consumers: ${unexpected.joinToString()}. " +
                "Migrate the consumer to ui-headless before adding a dependency."
        }
        val showcaseBuild = repositoryRoot.file("samples/ui-showcase/build.gradle.kts").asFile.readText()
        val showcaseMainDependencies = showcaseBuild
            .substringAfter("commonMain.dependencies", missingDelimiterValue = "")
            .substringBefore("commonTest.dependencies", missingDelimiterValue = "")
        check(!showcaseMainDependencies.contains(dependencyMarker)) {
            "samples/ui-showcase must keep ui-designsystem-compat test-scoped; " +
                "production commonMain cannot depend on the compatibility bridge."
        }
        println(
            "ui-designsystem-compat consumers (test/migration only): " +
                allowedConsumers.joinToString(),
        )
    }
}
