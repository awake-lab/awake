// swift-tools-version:5.9
import PackageDescription

// Local SPM package pointing at the debug XCFramework produced by
// `./gradlew :awake-demo:shared:assembleSharedDebugXCFramework` -- replaces the previous
// CocoaPods integration (see docs/MVP_PLAN.md for why). Rebuild the XCFramework after any
// Kotlin change, then clean build (Cmd+Shift+K) in Xcode to pick up the new binary.
let package = Package(
    name: "Shared",
    platforms: [.iOS(.v14)],
    products: [
        .library(name: "Shared", targets: ["Shared"])
    ],
    targets: [
        .binaryTarget(
            name: "Shared",
            path: "../../../shared/build/XCFrameworks/debug/Shared.xcframework"
        )
    ]
)
