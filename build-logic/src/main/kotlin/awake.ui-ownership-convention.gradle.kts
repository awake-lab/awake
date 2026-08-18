// Every module applying this convention must be classified here. The `when` blocks below
// previously matched the pre-rename paths `:awake:ui:ui-headless`/`:awake:ui:ui-designsystem`,
// so both modules silently fell to `emptyList()` and the check passed vacuously for months.
// This guard makes that failure mode impossible: an unlisted module fails the build instead
// of getting an empty rule set.
val classifiedUiModules = setOf(
    ":awake:ui:ui-core",
    ":awake:ui:headless",
    ":awake:ui:designsystem",
    ":awake:ui:tailwind",
    ":awake:ui:heroicons",
)
check(project.path in classifiedUiModules) {
    "Unclassified module applies awake.ui-ownership-convention: ${project.path}. " +
        "Add it to classifiedUiModules with explicit (possibly empty) rules."
}

val forbiddenUiDeclarationNames = when (project.path) {
    ":awake:ui:ui-core" -> listOf(
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
    ":awake:ui:headless" -> listOf(
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
    ":awake:ui:ui-core",
    ":awake:ui:headless" -> listOf(
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
// rejecting its direct escape through UiScope.context and imports of Core's runtime
// internals. Core *contract* packages (style, theme, modifier, font) are licensed for
// design-system style/local infrastructure per docs/reference/ui-ownership.md; Core
// runtime packages (layouts, popup, scope, child, animate, context) are not.
// Naming lexicon (docs/audits/2026-08-17-ui-refactor-vs-recreate-audit.md row P2):
// draw* is the only painting verb for future scope-level API; render* belongs to backends;
// emit*/paint* are legacy dialects frozen to their current files (exempt below, die with P1);
// providers are camelCase; *Slot widget twins are overloading-by-name (row C10).
val uiNamingLexiconPatterns = listOf(
    "\\bfun\\s+\\w*Scope\\.(emit|paint|render)[A-Z]",
    "\\bfun\\s+[\\w.]*\\.?Provide[A-Z]",
    "\\bfun\\s+\\w*Scope\\.(?!claim)[a-z]\\w*Slot\\s*\\(",
)

val forbiddenUiSourcePatterns = when (project.path) {
    ":awake:ui:ui-core",
    ":awake:ui:headless" -> uiNamingLexiconPatterns
    ":awake:ui:designsystem" -> uiNamingLexiconPatterns + listOf(
        // The escape hatch is headless UiScope.primitive.context; a bare `context.` match
        // would also flag every `ui.context.X` import line and double-count them.
        "\\bprimitive\\s*\\.\\s*context\\b",
        "(?m)^import\\s+io\\.github\\.ronjunevaldoz\\.awake\\.ui\\.UiScope",
        "(?m)^import\\s+io\\.github\\.ronjunevaldoz\\.awake\\.ui\\.(layouts|popup|scope|animate|child|modifier|unstyled)",
        // Runtime context is off-limits; the UiLocal ambient-value contract is licensed
        // "local infrastructure" (docs/reference/ui-ownership.md, theme values section).
        "(?m)^import\\s+io\\.github\\.ronjunevaldoz\\.awake\\.ui\\.context\\.(?!UiLocal\\b|uiLocalOf\\b)",
    )
    else -> emptyList()
}

// Known pre-existing escapes, tracked as debt in
// docs/audits/2026-08-17-ui-refactor-vs-recreate-audit.md (rows B9/D2). Each entry exempts
// one file from forbiddenUiSourcePatterns only; declaration/type-reference rules still apply.
// Shrink this list — never grow it without an audit row.
val exemptUiSourcePatternFiles = when (project.path) {
    ":awake:ui:ui-core" -> listOf(
        // emit* painter family — becomes UiDrawScope draw* members in the P1 receiver split.
        "graphics/ShapePainter.kt",
    )
    ":awake:ui:headless" -> listOf(
        // paintSurface / renderTextBlock — same P1 fate as ShapePainter's emit family.
        "internal/controls/Surface.kt",
        "internal/text/BasicText.kt",
        // buttonSlot twins — die with the UiButtonVariant deletion (row E4, package 4).
        "internal/controls/Buttons.kt",
    )
    ":awake:ui:designsystem" -> listOf(
        "components/ShadcnButtonGroupRecipes.kt",
        "components/ShadcnThemeLocals.kt",
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
    exemptSourcePatternFiles.set(exemptUiSourcePatternFiles)
}

tasks.named("check").configure {
    dependsOn(verifyUiOwnership)
}
