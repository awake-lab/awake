import java.util.Properties

plugins {
    `maven-publish`
    signing
    id("org.jetbrains.dokka")
}

// Stub secrets to let the project sync and build without the publication values set up
ext["signing.keyId"] = null
ext["signing.password"] = null
ext["signing.secretKey"] = null
ext["ossrhUsername"] = null
ext["ossrhPassword"] = null

// Grabbing secrets from local.properties file or from environment variables, which could be used on CI
val secretPropsFile = project.rootProject.file("local.properties")
if (secretPropsFile.exists()) {
    secretPropsFile.reader().use {
        Properties().apply {
            load(it)
        }
    }.onEach { (name, value) ->
        ext[name.toString()] = value
    }
} else {
    ext["signing.keyId"] = System.getenv("SIGNING_KEY_ID")
    ext["signing.password"] = System.getenv("SIGNING_PASSWORD")
    ext["signing.secretKey"] = System.getenv("SIGNING_SECRET_KEY")
    ext["ossrhUsername"] = System.getenv("OSSRH_USERNAME")
    ext["ossrhPassword"] = System.getenv("OSSRH_PASSWORD")
}


// Dokka 2.x: dokkaGeneratePublicationHtml replaces the old dokkaHtml task
val javadocJar = tasks.register<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    from(tasks.named("dokkaGeneratePublicationHtml"))
}


fun getExtraString(name: String) = ext[name]?.toString()


publishing {
    // Configure maven central repository
    repositories {
        maven {
            name = "sonatype"
            val releasesRepoUrl =
                "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
            val snapshotsRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
            url = uri(
                if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
            )
            credentials {
                username = getExtraString("ossrhUsername")
                password = getExtraString("ossrhPassword")
            }
        }
    }

    // Configure all publications
    publications.withType<MavenPublication> {
        // Stub javadoc.jar artifact
        artifact(javadocJar.get())

        // Provide artifacts information requited by Maven Central
        pom {
            name.set("Awake Core")
            description.set("Cross-platform OpenGL & Vulkan graphics")
            licenses {
                license {
                    name.set("Apache License, Version 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            url.set("https://ronjunevaldoz.github.io/awake")
            issueManagement {
                system.set("Github")
                url.set("https://github.com/ronjunevaldoz/awake/issues")
            }
            scm {
                connection.set("https://github.com/ronjunevaldoz/awake.git")
                url.set("https://github.com/ronjunevaldoz/awake")
            }
            developers {
                developer {
                    name.set("Ron June Valdoz")
                    email.set("ronjune.lopez@gmail.com")
                }
            }
        }
    }
}

signing {
    useInMemoryPgpKeys(
        getExtraString("signing.keyId"),
        getExtraString("signing.secretKey"),
        getExtraString("signing.password")
    )
    sign(publishing.publications)
}