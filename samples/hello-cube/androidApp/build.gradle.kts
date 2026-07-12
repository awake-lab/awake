// Plain Android app (AGP 9 built-in Kotlin) — AGP 9 forbids combining
// com.android.application with the Kotlin Multiplatform plugin.
plugins {
    alias(libs.plugins.android.application)
    id("awake.dokka-convention")
    id("awake.detekt-convention")
    id("awake.spotless-convention")
}

android {
    namespace = "io.github.ronjunevaldoz.awake.sample.hellocube.android"
    compileSdk = (findProperty("android.compileSdk") as String).toInt()

    defaultConfig {
        applicationId = "io.github.ronjunevaldoz.awake.sample.hellocube.android"
        minSdk = (findProperty("android.minSdk") as String).toInt()
        targetSdk = (findProperty("android.targetSdk") as String).toInt()
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // VulkanView (awake-engine) comes in transitively via sample-hello-cube's appMain
    // `api(project(":awake:engine"))` -- must resolve through this same edge, not a second
    // direct dependency, or Gradle can resolve the two to different variants and produce a
    // class-identity mismatch at compile time.
    implementation(project(":samples:hello-cube"))
    // Theme.AppCompat.Light.NoActionBar (AndroidManifest.xml) needs this on the classpath --
    // sample-hello-cube's own androidMain (unlike awake-demo/shared's) doesn't pull it in.
    implementation(libs.androidx.appcompat)
}
