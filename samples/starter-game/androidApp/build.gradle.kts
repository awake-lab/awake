plugins {
    alias(libs.plugins.android.application)
    id("awake.detekt-convention")
    id("awake.spotless-convention")
}

android {
    namespace = "io.github.ronjunevaldoz.awake.sample.startergame.android"
    compileSdk = (findProperty("android.compileSdk") as String).toInt()

    defaultConfig {
        applicationId = "io.github.ronjunevaldoz.awake.sample.startergame.android"
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
    implementation(project(":samples:starter-game"))
    implementation(libs.androidx.appcompat)
}
