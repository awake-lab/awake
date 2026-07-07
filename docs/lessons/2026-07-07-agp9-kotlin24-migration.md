# Lesson: AGP 9 / Kotlin 2.4 / Compose MP 1.11 / Gradle 9.6 migration (Awake)

**Date:** 2026-07-07 · Same-day follow-up to the Kotlin 2.1/AGP 8.7 migration —
the user asked to go straight to the actual latest stable stack instead of stopping
at a slightly-behind intermediate version.

## Why this is a separate lesson from the Kotlin 2.1 migration

The 2.1.21/AGP 8.7.3 migration and this 2.4.0/AGP 9.2.1 migration hit almost entirely
different breakage classes. Landing on the true latest stable surfaced structural
plugin incompatibilities that a one-minor-version bump does not.

## What broke (AGP 8.7.3 → 9.2.1, Kotlin 2.1.21 → 2.4.0, CMP 1.8.2 → 1.11.1, Gradle 8.11.1 → 9.6.1)

1. **AGP 9 forbids `com.android.library`/`com.android.application` + `kotlin("multiplatform")`
   in the same module.** Any KMP module with an `androidTarget` must use the new
   `com.android.kotlin.multiplatform.library` plugin instead. A plain (non-KMP) Android
   app module can still use `com.android.application` normally.
2. **The new KMP Android plugin's `androidLibrary { }` DSL block is *itself* already
   deprecated** in favor of a plain `android { }` block nested inside `kotlin { }`. Skill
   templates dated slightly earlier show `androidLibrary { }`; the AGP 9.2 reality is
   `kotlin { android { namespace = ...; compileSdk = ...; minSdk = ... } }`.
3. **No `externalNativeBuild`/CMake support in the new KMP Android plugin.** A module with
   a native NDK/CMake build (like `awake-vulkan`) cannot express it there at all. Fix: split
   the native/JNI ownership into a separate plain `com.android.library` module
   (`:awake-vulkan:android-native`) that the KMP module depends on via `api(...)`.
4. **`sourceSets["main"].jniLibs.srcDirs(...)` crashes** with a classloader-boundary
   `ClassCastException` (`DefaultAndroidLibrarySourceSet_Decorated` cannot be cast to
   `AndroidLibrarySourceSet`) under AGP 9 — a real plugin bug, not a usage error. Workaround:
   don't override the source dir; move files to the conventional default location instead
   (`src/main/jniLibs`) so no override is needed.
5. **Compose Multiplatform dropped `iosX64`** (Intel simulator) — last published at
   1.11.0-alpha01. Any module still declaring `iosX64()` fails dependency resolution with
   a "KMP Dependencies Resolution Failure" error naming the unresolved platform. Fix: remove
   `iosX64()`, keep `iosArm64()` + `iosSimulatorArm64()` (Apple Silicon only).
6. **`val commonMain by getting { dependencies { ... } }` is deprecated** in Gradle 9.6 KMP
   source sets — use `commonMain.dependencies { ... }` (or `getByName("desktopMain").dependencies { }`
   for source sets without a typed accessor, e.g. `desktopMain` on a `jvm("desktop")` target).
7. **Dokka 1.x → 2.x**: `dokkaHtml` task renamed to `dokkaGeneratePublicationHtml`;
   `DokkaTask.outputDirectory` config no longer needed/settable the old way. Needs
   `org.jetbrains.dokka.experimental.gradle.pluginMode=V2Enabled` in `gradle.properties`
   while the plugin is in its V1→V2 transition period.
8. **`compose.runtime` / `compose.foundation` / `compose.components.resources` accessor
   DSL is deprecated** in CMP 1.11 — declare the modules directly
   (`org.jetbrains.compose.runtime:runtime`, etc.) instead of `compose.runtime`.
9. **`implementation(platform(x))` inside a KMP `sourceSets {}` dependencies block**
   no longer resolves `platform` as an extension function — needs the explicit receiver
   `project.dependencies.platform(x)`.
10. **`gradle.serviceOf<DefaultTargetMachineFactory>()` and
    `org.gradle.kotlin.dsl.support.serviceOf`** (both used for host-OS detection in build
    scripts) are gone/broken under Gradle 9 — use `System.getProperty("os.name")` instead.

## Process note

A build that fails immediately after a source-set move isn't necessarily a regression —
rerun clean before concluding a fix didn't work. One `assembleDebug` run failed right after
an aborted iOS compile attempt in the same Gradle daemon session; a clean rerun succeeded.
Always verify failures with a fresh `--rerun-tasks` or `clean` before chasing a phantom.

## Related

[[2026-07-07-toolchain-migration]] — the prior, more conservative bump (Kotlin 2.1/AGP 8.7)
that this migration supersedes same-day.
