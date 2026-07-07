# Lesson: Kotlin 1.8 → 2.1 toolchain migration (Awake)

**Date:** 2026-07-07 · **Skill involved:** `kotlin-multiplatform-migration`

## Skill gap

The migration skill covers app-architecture migration (MVVM→MVI, monolith→modules, Hilt→Koin)
but has no path for **toolchain/version migration** (Kotlin 1.8→2.x, AGP 7→8, CMP 1.4→1.8) —
which is the first migration any dormant KMP project actually needs. Its "one verified step
at a time" discipline still applied well.

## What actually broke (Kotlin 1.8.20 / AGP 7.4.2 / CMP 1.4.1 → 2.1.21 / 8.7.3 / 1.8.2)

1. **`publishing {}` inside `kotlin {}`** resolved via leaked scope in old KGP; K2 DSL fails
   compilation. Move to project level, reference target publication names as strings.
2. **K2 enforces expect/actual parameter-name matching** — an actual `frontFace(face: Int)`
   vs expected `frontFace(mode: Int)` is now an error.
3. **Compose `resource()` API removed** (was experimental in 1.4). Replaced with our own
   `readResourceBytes` expect/actual (classloader on JVM/Android, NSBundle on iOS).
4. **`org.jetbrains.compose.components.resources.BuildConfig` removed** — was leaking from
   the resources library; replaced with engine's own `AwakeContext.config.debug`.
5. **material-icons no longer transitively provided** by material3 in CMP 1.8 — add
   `org.jetbrains.compose.material:material-icons-core:1.7.3` explicitly.
6. **Classes shaded inside KGP are not API:** build script used
   `org.jetbrains.kotlin.de.undercouch...Download`; gone in 2.x. Apply the real
   `de.undercouch.download` plugin.
7. **`toUpperCase()/capitalize()/decapitalize()`** are ERROR-level deprecated in K2.
8. **NDK 26 + 32-bit ABIs:** generated JNI `reinterpret_cast<VkHandle>(jlong)` only compiles
   where handles are pointer-typed (64-bit). Dropped armeabi-v7a/x86.
9. **`android` CLI (`~/.local/bin/android`) NDK install corrupts symlinks** — extracts them
   as text files containing the target name (`clang` → file containing "clang-17").
   Fix: convert fake-symlink files back to real symlinks, or install NDK via Android Studio.

## Environment notes

- Homebrew default JDK is 25; use Temurin 17 (`/Library/Java/JavaVirtualMachines/temurin-17.jdk`)
  via `JAVA_HOME` for Gradle.
- Pre-migration baseline was unbuildable anyway (AGP 7.4 wanted a JDK 11 toolchain not
  installed) — a "build the old state first" gate isn't always achievable; compile-gating
  the *new* state per step was the workable substitute.
