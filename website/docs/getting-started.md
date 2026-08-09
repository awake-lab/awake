# Getting Started

The easiest way to start with Awake is to use the `awake-backend-vulkan` or `awake-backend-webgpu` bootstrap.

## Installation

Add the following to your `libs.versions.toml`:

```toml
[versions]
awake = "1.0.0-SNAPSHOT"

[libraries]
awake-base = { group = "io.github.awake-lab", name = "awake-base", version.ref = "awake" }
awake-vulkan = { group = "io.github.awake-lab", name = "awake-backend-vulkan", version.ref = "awake" }
```

## Creating a Game

You can define a game using the `game {}` DSL:

```kotlin
--8<-- "samples/hello-cube/src/commonMain/kotlin/io/github/ronjunevaldoz/awake/sample/hellocube/app/HelloCubeGame.kt"
```

> [!NOTE]
> The example above is pulled directly from the `samples/hello-cube` module, ensuring it always compiles and stays up-to-date with the latest Engine APIs.
