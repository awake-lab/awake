![Build And Publish](https://github.com/ronjunevaldoz/awake/actions/workflows/build-and-publish.yml/badge.svg)
![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/io.github.ronjunevaldoz/awake-core?server=https%3A%2F%2Fs01.oss.sonatype.org)
[![Kotlin](https://img.shields.io/badge/kotlin-1.8.20-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

# Awake

Awake is a cross-platform graphics wrapper and framework (OpenGL, Metal & Vulkan)

> **Note**
> The library is still unstable and might encounter issues. While the Android and iOS platform have stable and functional opengl support, the Desktop platform that utilizes the lwjgl/glfw window is still in the development and experimental phase.

### Features Supported

- OpenGL
    - [x] Android OpenGL
    - [x] iOS OpenGL
    - [x] Desktop OpenGL (JVM experimental due to in-compatibility to compose AWT)
- Vulkan
    - [ ] Android Vulkan (TODO)
    - [ ] iOS MoltenVK / Metal (TODO)
    - [ ] Desktop (TODO)

### Modules

- `awake-core` — math, renderer abstraction, asset utilities (installation below)
- [`awake-vulkan`](awake-vulkan) — Vulkan bindings, mesh/material/pipeline types
- [`awake-ecs`](awake-ecs/README.md) — sparse-set ECS runtime (entities, components, systems, queries)
- [`awake-scene`](awake-scene/README.md) — scene-graph components/systems (`Transform`, `MeshRenderer`, `Camera`, `Light`) built on `awake-ecs`

### Tools

Vulkan requires to use `.spv` shader, so i've provided a GlslValidator gradle plugin to convert glsl
to spv on pre-compilation.

1. To install `glslangValidator`
    ```
    brew install glslang 
    ```
2. To verify installation
    ```
    glslangValidator --version
    ```
3. Run gradle task `glslValidator`
    ```
   gradle glslValidator
   ```

### Installation

To use this library in your Kotlin Multiplatform project, follow the steps below:

1. Add the library as a dependency in your Gradle `build.gradle.kts`

```kotlin
implementation("io.github.ronjunevaldoz:awake-core:1.0.0-SNAPSHOT")
```

```kotlin
repositories {
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
}
```

### Example Usage

1. Initialize the AwakeContext

    ```kotlin
        // by default debug is enabled
        // default fps is set to 60
        // default gl version is set to 3
        AwakeContext.init()
    ```
2. Create a sample compose component

   ```kotlin
        
   ```

### License

This library is released under the Apache, Version 2.0 License..
