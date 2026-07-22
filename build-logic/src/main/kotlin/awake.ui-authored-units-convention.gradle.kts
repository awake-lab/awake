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
