import UIKit
import SwiftUI
// The SPM package/product is still named "Shared" (Package.swift, cosmetic only), but the
// binaryTarget wraps the XCFramework's own compiled module, whose name is the Kotlin
// framework's baseName ("Sample") -- that's what Swift actually sees on import, regardless
// of the wrapping package's declared name.
import Sample

// No Compose here (unlike awake-demo/iosApp) -- makeSampleViewController() (main.ios.kt)
// returns a plain UIViewController hosting VulkanMetalView directly.
struct VulkanView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        Main_iosKt.makeSampleViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        VulkanView()
                .ignoresSafeArea()
    }
}



