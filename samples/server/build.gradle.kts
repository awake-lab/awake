// A small, protocol-agnostic Ktor WebSocket debug-control server -- see
// DebugControlServer.kt's own doc comment. No kotlinx.serialization/JSON dependency here on
// purpose: this module sends/receives raw text frames only, generic over whatever command/
// response types a consumer (e.g. samples:hello-cube) parses/encodes itself.
plugins {
    alias(libs.plugins.kotlin.jvm)
    id("awake.dokka-convention")
    id("awake.detekt-convention")
    id("awake.spotless-convention")
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.server.websockets)
    testImplementation(kotlin("test"))
}
