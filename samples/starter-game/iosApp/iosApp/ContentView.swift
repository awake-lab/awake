import UIKit
import SwiftUI
// The SPM package/product is still named "Shared" (Package.swift, cosmetic only), but the
// binaryTarget wraps the XCFramework's own compiled module, whose name is the Kotlin
// framework's baseName ("StarterGame") -- that's what Swift actually sees on import, regardless
// of the wrapping package's declared name.
import StarterGame

// No Compose here (unlike awake-demo/iosApp) -- makeStarterGameViewController() (main.ios.kt)
// returns a plain UIViewController hosting VulkanMetalView directly.
struct VulkanView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        Main_iosKt.makeStarterGameViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        VulkanView()
                .ignoresSafeArea()
    }
}


