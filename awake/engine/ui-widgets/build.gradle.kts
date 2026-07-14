import java.util.Base64

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library.kmp)
    id("awake.dokka-convention")
    id("awake.detekt-convention")
    id("awake.spotless-convention")
}

kotlin {
    jvmToolchain(17)

    android {
        namespace = "io.github.ronjunevaldoz.awake.ui.widgets"
        compileSdk = (findProperty("android.compileSdk") as String).toInt()
        minSdk = (findProperty("android.minSdk") as String).toInt()
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "awake-engine-ui-widgets"
        }
    }

    jvm("desktop")

    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":awake:engine:ui-core"))
            implementation(project(":awake:base"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

tasks.named<Test>("desktopTest") {
    finalizedBy("uiSnapshotReport")
}

tasks.register("uiSnapshotReport") {
    group = "verification"
    description = "Generate an HTML gallery of every UI snapshot PNG from the last desktopTest run."
    val snapshotsDir = layout.buildDirectory.dir("ui-snapshots")
    val reportFile = layout.buildDirectory.file("reports/ui-snapshots/index.html")
    doLast {
        val root = snapshotsDir.get().asFile
        val snapshots = root.listFiles { file -> file.isFile && file.extension == "png" }
            ?.sortedBy { it.name } ?: emptyList()

        val cards = snapshots.joinToString("\n") { file ->
            val base64 = Base64.getEncoder().encodeToString(file.readBytes())
            """
            <div style="margin:0 1rem 1rem 0">
                <h3>${file.nameWithoutExtension}</h3>
                <img src="data:image/png;base64,$base64" style="image-rendering:pixelated;border:1px solid #444" />
            </div>
            """.trimIndent()
        }

        val body = if (snapshots.isEmpty()) "<p>No UI snapshots recorded.</p>" else
            """<div style="display:flex;flex-wrap:wrap">$cards</div>"""

        val html = """
            <!DOCTYPE html>
            <html><head><meta charset="utf-8"><title>UI snapshot report</title></head>
            <body style="font-family:sans-serif;background:#1e1e1e;color:#eee;padding:2rem">
                <h1>UI snapshot report</h1>
                $body
            </body></html>
        """.trimIndent()

        val out = reportFile.get().asFile
        out.parentFile.mkdirs()
        out.writeText(html)
        println("UI snapshot report: file://${out.absolutePath}")
    }
}
