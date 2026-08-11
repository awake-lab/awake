val forbiddenUiDeclarationNames = when (project.path) {
    ":awake:engine:ui:ui-core" -> listOf(
        "anchoredColumn",
        "anchoredRow",
        "anchoredPanel",
        "anchoredSection",
        "propertyRow",
        "propertyCheckbox",
        "DefaultUiTheme",
        "DarkUiTheme",
        "LightUiTheme",
        "InspectorPane",
        "DebugOverlay",
        "HudOverlay"
    )
    ":awake:engine:ui:ui-headless" -> listOf(
        "anchoredColumn",
        "anchoredRow",
        "anchoredPanel",
        "anchoredSection",
        "propertyRow",
        "propertyCheckbox",
        "InspectorPane",
        "DebugOverlay",
        "HudOverlay"
    )
    else -> emptyList()
}

val forbiddenUiTypeReferences = when (project.path) {
    ":awake:engine:ui:ui-core",
    ":awake:engine:ui:ui-headless" -> listOf(
        "SceneGameRuntime",
        "HelloCubeRuntimeState",
        "HelloCubeDebugController",
        "DebugSnapshot",
        "DebugCommand"
    )
    else -> emptyList()
}

// Kotlin's `internal` is module-wide, so it cannot say "ui-headless may use this but
// ui-designsystem may not." Keep ui-designsystem on the public headless widget surface by
// rejecting its direct escape through UiScope.context. Core contract types (UiScope, Style,
// UiModifier, UiTheme) remain valid in public design-system signatures.
val forbiddenUiSourcePatterns = when (project.path) {
    ":awake:engine:ui:ui-designsystem" -> listOf(
        "\\bcontext\\s*\\.",
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
    forbiddenSourcePatterns.set(forbiddenUiSourcePatterns)
}

tasks.named("check").configure {
    dependsOn(verifyUiOwnership)
}
