// swift-tools-version:5.9
import PackageDescription

// Local SPM package pointing at the debug XCFramework produced by
// `./gradlew :sample-hello-cube:assembleSampleDebugXCFramework` -- same CocoaPods-free
// pattern as awake-demo/iosApp (see docs/MVP_PLAN.md for why). Rebuild the XCFramework
// after any Kotlin change, then clean build (Cmd+Shift+K) in Xcode to pick up the new
// binary. Product/target names stay "Shared" (cosmetic only) so this file's structure
// otherwise matches awake-demo's exactly -- only the binary path + framework name differ,
// since sample-hello-cube's own XCFramework registration is named "Sample".
let package = Package(
    name: "Shared",
    platforms: [.iOS(.v14)],
    products: [
        .library(name: "Shared", targets: ["Shared"])
    ],
    targets: [
        .binaryTarget(
            name: "Shared",
            path: "../../../build/XCFrameworks/debug/Sample.xcframework"
        )
    ]
)
