val forbiddenUiDeclarationNames = when (project.path) {
    ":awake:engine:ui-core" -> listOf(
        "anchoredColumn",
        "anchoredRow",
        "anchoredPanel",
        "anchoredSection",
        "InspectorPane",
        "DebugOverlay",
        "HudOverlay"
    )
    ":awake:engine:ui-widgets" -> listOf(
        "anchoredColumn",
        "anchoredRow",
        "anchoredPanel",
        "anchoredSection",
        "InspectorPane",
        "DebugOverlay",
        "HudOverlay"
    )
    ":awake:engine:ui-dsl" -> listOf(
        "anchoredColumn",
        "anchoredRow",
        "anchoredPanel",
        "anchoredSection"
    )
    else -> emptyList()
}

val forbiddenUiTypeReferences = when (project.path) {
    ":awake:engine:ui-core",
    ":awake:engine:ui-widgets",
    ":awake:engine:ui-dsl" -> listOf(
        "SceneGameRuntime",
        "HelloCubeRuntimeState",
        "HelloCubeDebugController",
        "DebugSnapshot",
        "DebugCommand"
    )
    else -> emptyList()
}

val verifyUiOwnership = tasks.register<VerifyUiOwnershipTask>("verifyUiOwnership") {
    group = "verification"
    description = "Reject helper-shaped or runtime-bound API drift in reusable UI modules."
    modulePath.set(project.path)
    sourceFiles.from(
        fileTree("src") {
            include("**/*Main/**/*.kt")
            exclude("**/*Test/**/*.kt")
        }
    )
    forbiddenDeclarationNames.set(forbiddenUiDeclarationNames)
    forbiddenTypeReferences.set(forbiddenUiTypeReferences)
}

tasks.named("check").configure {
    dependsOn(verifyUiOwnership)
}
