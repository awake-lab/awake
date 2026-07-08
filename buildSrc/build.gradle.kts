plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    // required by the root build.gradle.kts's `subprojects { apply(plugin =
    // "org.jetbrains.dokka") } `; keep in sync with the dokka version in
    // gradle/libs.versions.toml
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:2.0.0")
}
