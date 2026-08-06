plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.0")
    implementation("com.android.tools.build:gradle:9.2.1")
    implementation(libs.detekt.gradle.plugin)
    implementation(libs.spotless.gradle.plugin)
    implementation(libs.dokka.gradle.plugin)
    implementation(libs.download.gradle.plugin)
    implementation("com.vanniktech:gradle-maven-publish-plugin:0.36.0")
}
