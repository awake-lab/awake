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

tasks.matching { it.name.endsWith("ProcessResources") }.configureEach {
    dependsOn(syncAwakeShaders)
}
