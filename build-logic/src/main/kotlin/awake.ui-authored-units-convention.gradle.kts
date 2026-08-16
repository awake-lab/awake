val verifyUiAuthoredUnits = tasks.register<VerifyUiAuthoredUnitsTask>("verifyUiAuthoredUnits") {
    group = "verification"
    description = "Reject numeric `.px` literals in authored shared/sample UI source."
    modulePath.set(project.path)
    sourceFiles.from(
        fileTree("src") {
            include("**/*Main/**/*.kt")
            exclude("**/*Test/**/*.kt")
        }
    )
}

tasks.named("check").configure {
    dependsOn(verifyUiAuthoredUnits)
}

if (project.path in setOf(":awake:ui:headless", ":awake:ui:designsystem")) {
    val verifyUiTestLifecycle = tasks.register<VerifyUiTestLifecycleTask>("verifyUiTestLifecycle") {
        group = "verification"
        description = "Reject manual UI frame setup in ordinary component test fixtures."
        modulePath.set(project.path)
        sourceFiles.from(
            fileTree("src") {
                include("**/*Test/**/*.kt")
            },
        )
    }

    tasks.named("check").configure {
        dependsOn(verifyUiTestLifecycle)
    }
}
