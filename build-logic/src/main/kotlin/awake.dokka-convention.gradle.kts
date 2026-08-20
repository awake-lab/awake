import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier
import java.net.URI

plugins {
    id("org.jetbrains.dokka")
}

dokka {
    // 1. Configure the primary HTML website generation publication task
    dokkaPublications.html {
        // Explicitly set the output folder (Defaults to build/dokka/html)
        outputDirectory.set(layout.buildDirectory.dir("docs/api-reference"))

        // Fails the build if any warnings are found during generation
        failOnWarning.set(true)
    }

    // 2. Configure source set properties globally across all source sets
    dokkaSourceSets.configureEach {
        // Set the visible module description name
        moduleName.set("Awake Engine") // TODO: fix for sub projects

        // Crucial for libraries: Include your root README into the docs homepage
        includes.from(project.files("README.md"))

        // --- THE STRICT GUARDRAILS ---
        // Fails the build if any public element lacks KDocs
        reportUndocumented.set(true)

        // Prevent deprecated code from cluttering your clean API reference website
        skipDeprecated.set(true)

        // Configure visibilities using Dokka v2 syntax
        documentedVisibilities(VisibilityModifier.Public, VisibilityModifier.Protected)

        // Optional: Link your docs directly to your online source code repository
        sourceLink {
            localDirectory.set(projectDir.resolve("src/main/kotlin"))
            remoteUrl.set(URI("https://github.com/awake-label/awake"))
            remoteLineSuffix.set("#L")
        }
    }
}
