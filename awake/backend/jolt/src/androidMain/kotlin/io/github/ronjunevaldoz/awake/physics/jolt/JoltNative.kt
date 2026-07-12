// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.physics.jolt

import com.github.stephengold.joltjni.Jolt
import com.github.stephengold.joltjni.JoltPhysicsObject

/**
 * Android-only one-time native-library load + Jolt factory bootstrap (see jolt-jni's own
 * "add to an existing project" doc) -- separated from [JoltPhysicsWorld] itself since it's
 * process-wide, idempotent, one-time state, not per-`PhysicsWorld` state (a game could
 * plausibly construct more than one `JoltPhysicsWorld`, but must load the native library and
 * register Jolt's factory/types exactly once per process). Unlike `desktopMain`'s twin, no
 * snaploader extraction step is needed here -- the `jolt-jni-Android` AAR already bundles
 * its native library per-ABI under the standard `jniLibs` layout, so a plain
 * `System.loadLibrary` finds it.
 */
internal object JoltNative {
    private var loaded = false

    @Synchronized
    fun ensureLoaded() {
        if (loaded) return

        System.loadLibrary("joltjni")

        JoltPhysicsObject.startCleaner() // reclaims native memory automatically on GC
        Jolt.registerDefaultAllocator() // tell Jolt Physics to use malloc/free
        Jolt.installDefaultAssertCallback()
        Jolt.installDefaultTraceCallback()

        val factoryCreated = Jolt.newFactory()
        check(factoryCreated) { "Jolt.newFactory() failed" }
        Jolt.registerTypes()

        loaded = true
    }
}
