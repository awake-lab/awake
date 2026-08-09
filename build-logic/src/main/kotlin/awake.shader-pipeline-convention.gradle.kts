plugins {
    base
}

val syncAwakeShaders = tasks.register<SyncWgslShaderPipelineTask>("syncAwakeShaders") {
    group = "build setup"
    description = "Sync canonical WGSL shaders into WebGPU resources and generate Vulkan SPIR-V artifacts."
    sourceDirectory.set(layout.projectDirectory.dir("src/commonMain/shaders"))
    webGpuOutputDirectory.set(layout.projectDirectory.dir("src/wasmJsMain/resources/assets/shader/webgpu"))
    vulkanOutputDirectory.set(layout.projectDirectory.dir("src/appMain/resources/assets/shader/vulkan"))
    nagaExecutable.convention(
        providers.gradleProperty("awake.shader.nagaBinary")
            .orElse(providers.environmentVariable("AWAKE_NAGA"))
            .orElse("naga")
    )
    vertexEntryPoint.convention("vertexMain")
    fragmentEntryPoint.convention("fragmentMain")
}

val validateAwakeShaders = tasks.register<ValidateWgslShadersTask>("validateAwakeShaders") {
    group = "verification"
    description = "Validate canonical WGSL shaders with naga-cli."
    sourceDirectory.set(layout.projectDirectory.dir("src/commonMain/shaders"))
    nagaExecutable.convention(
        providers.gradleProperty("awake.shader.nagaBinary")
            .orElse(providers.environmentVariable("AWAKE_NAGA"))
            .orElse("naga")
    )
}

syncAwakeShaders.configure {
    dependsOn(validateAwakeShaders)
}

tasks.register("generateAwakeShaders") {
    group = "build setup"
    description = "Generate synced backend shader artifacts from canonical WGSL."
    dependsOn(syncAwakeShaders)
}

tasks.named("check").configure {
    dependsOn(validateAwakeShaders)
}

// Every consumer of src/appMain/resources (or a directory tree that folds it in, like
// awake.test-resources-convention's merge Sync) must run after syncAwakeShaders writes into
// it, or Gradle's implicit-dependency validation fails the build. New consumers keep showing
// up under different names (*ProcessResources, process*JavaRes, syncAwakeTestResources) --
// matched by pattern here instead of adding one `dependsOn` per module per new consumer task.
tasks.matching {
    it.name.endsWith("ProcessResources") ||
        (it.name.startsWith("process") && it.name.endsWith("JavaRes")) ||
        it.name == "syncAwakeTestResources" ||
        it.name == "copyIosSimulatorTestResources"
}.configureEach {
    dependsOn(syncAwakeShaders)
}

// Ordering-only consumers of src/: they scan the tree syncAwakeShaders writes into but don't
// need it to have run -- mustRunAfter satisfies Gradle's implicit-dependency validation
// without making lint depend on shader tooling.
tasks.matching { it.name == "detekt" || it.name == "detektBaseline" || it.name == "verifyUiAuthoredUnits" }.configureEach {
    mustRunAfter(syncAwakeShaders)
}
