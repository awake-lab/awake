import demo.VulkanApplication
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanWindow

/**
 * Real, visible desktop Vulkan render -- opens an actual GLFW window and runs
 * [VulkanApplication] (the same textured-cube demo verified on Android) against it via
 * MoltenVK. Run via `./gradlew :awake-demo:desktopApp:runVulkanDesktop`.
 *
 * A separate entry point from `main.kt` (the OpenGL/AWT demo) rather than replacing it --
 * Phase 1c (platform-neutral surface creation) hasn't unified the two yet, so this is the
 * GLFW-specific path proven in awake-vulkan's Round 11 verification, now wired into an
 * actual running app instead of a throwaway smoke test.
 */
fun main() {
    check(VulkanWindow.glfwInit()) { "glfwInit failed" }
    VulkanWindow.glfwWindowHint(0x00022001, 0) // GLFW_CLIENT_API, GLFW_NO_API
    val window = VulkanWindow.glfwCreateWindow(800, 600, "Awake Vulkan - Desktop")
    check(window != 0L) { "glfwCreateWindow returned null" }

    val app = VulkanApplication()
    app.create(window)

    while (!VulkanWindow.glfwWindowShouldClose(window)) {
        VulkanWindow.glfwPollEvents()
        app.update(0.016f)
    }

    app.dispose()
}
