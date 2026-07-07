plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    // required by signing-publication-conventions.gradle.kts (applies org.jetbrains.dokka);
    // keep in sync with the dokka version in gradle/libs.versions.toml
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.9.20")
}

