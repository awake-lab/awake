package io.github.ronjunevaldoz.awake.core.utils

// Phase 2.5 (Web/WebGPU, decision D7) milestone 1: compile-only stub. A real
// implementation needs an async browser fetch() (readResourceBytes' signature is
// synchronous, matching every other platform) -- see docs/MVP_PLAN.md.
actual fun readResourceBytes(path: String): ByteArray {
    TODO("Web resource loading not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")
}
