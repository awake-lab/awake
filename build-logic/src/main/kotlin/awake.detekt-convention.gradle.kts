import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    id("io.gitlab.arturbosch.detekt")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

extensions.configure<DetektExtension> {
    toolVersion = libs.findVersion("detekt").get().requiredVersion
    config.setFrom(rootProject.file("config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    baseline = layout.projectDirectory.file("detekt-baseline.xml").asFile
}

tasks.withType<Detekt>().configureEach {
    setSource(
        fileTree("src") {
            include("**/*.kt")
            exclude("**/build/**", "**/attic/**")
        }
    )
}

tasks.withType<DetektCreateBaselineTask>().configureEach {
    setSource(
        fileTree("src") {
            include("**/*.kt")
            exclude("**/build/**", "**/attic/**")
        }
    )
}
