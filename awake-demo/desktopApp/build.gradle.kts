import org.gradle.kotlin.dsl.support.serviceOf
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose.compiler)
}

kotlin {
    jvmToolchain(17)
    jvm()
    sourceSets {
        val jvmMain by getting  {
            dependencies {
                implementation(compose.desktop.currentOs)
                // awake-core in intended for compose project, should be remove in the future
                implementation(project(":awake-core"))
                implementation(project(":awake-demo:shared"))
            }
        }
    }

}

val host: TargetMachine =
    gradle.serviceOf<org.gradle.nativeplatform.internal.DefaultTargetMachineFactory>().host()


compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "KotlinMultiplatformComposeDesktopApplication"
            packageVersion = "1.0.0"
        }
        if (host.operatingSystemFamily.isMacOs) {
            jvmArgs("-XstartOnFirstThread")
        }
    }
}