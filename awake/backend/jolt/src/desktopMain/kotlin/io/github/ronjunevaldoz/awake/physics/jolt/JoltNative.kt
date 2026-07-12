// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.physics.jolt

import com.github.stephengold.joltjni.Jolt
import com.github.stephengold.joltjni.JoltPhysicsObject
import electrostatic4j.snaploader.LibraryInfo
import electrostatic4j.snaploader.LoadingCriterion
import electrostatic4j.snaploader.NativeBinaryLoader
import electrostatic4j.snaploader.filesystem.DirectoryPath
import electrostatic4j.snaploader.platform.NativeDynamicLibrary
import electrostatic4j.snaploader.platform.util.PlatformPredicate

/**
 * Desktop-only one-time native-library load + Jolt factory bootstrap (see jolt-jni's own
 * "add to an existing project" doc) -- separated from [JoltPhysicsWorld] itself since it's
 * process-wide, idempotent, one-time state, not per-`PhysicsWorld` state (a game could
 * plausibly construct more than one `JoltPhysicsWorld`, but must load the native library and
 * register Jolt's factory/types exactly once per process).
 */
internal object JoltNative {
    private var loaded = false

    @Synchronized
    fun ensureLoaded() {
        if (loaded) return

        // DirectoryPath.USER_DIR resolves to the JVM's "user.dir" system property, which
        // during a Gradle-run test/app is the *project* directory, not a scratch directory
        // (confirmed the hard way: it extracted `libjoltjni.dylib` straight into this
        // module's own source tree). The OS temp directory is the correct scratch location.
        val info = LibraryInfo(null, "joltjni", DirectoryPath(System.getProperty("java.io.tmpdir")))
        val loader = NativeBinaryLoader(info)
        val libraries = arrayOf(
            NativeDynamicLibrary("linux/aarch64/com/github/stephengold", PlatformPredicate.LINUX_ARM_64),
            NativeDynamicLibrary("linux/armhf/com/github/stephengold", PlatformPredicate.LINUX_ARM_32),
            NativeDynamicLibrary("linux/x86-64/com/github/stephengold", PlatformPredicate.LINUX_X86_64),
            NativeDynamicLibrary("osx/aarch64/com/github/stephengold", PlatformPredicate.MACOS_ARM_64),
            NativeDynamicLibrary("osx/x86-64/com/github/stephengold", PlatformPredicate.MACOS_X86_64),
            NativeDynamicLibrary("windows/x86-64/com/github/stephengold", PlatformPredicate.WIN_X86_64),
        )
        loader.registerNativeLibraries(libraries).initPlatformLibrary()
        loader.loadLibrary(LoadingCriterion.CLEAN_EXTRACTION)

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
