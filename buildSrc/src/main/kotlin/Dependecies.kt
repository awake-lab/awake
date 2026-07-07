object Versions {

    const val atomicfu = "0.26.1"
    const val kotlin = "2.1.21"
    const val lwjgl = "3.3.6"
    const val napier = "2.7.1"
    const val detekt = "1.23.8"

}

object Deps {
    val lwjgl = Lwjgl
    val napier = "io.github.aakira:napier:${Versions.napier}"
}

object Lwjgl {
    private const val group = "org.lwjgl"

    val natives =  Natives
    val lwjgl = lwjgl("lwjgl")
    val bom = lwjgl("bom")
    val glfw = lwjgl("glfw")
    val opengl = lwjgl("opengl")
    val stb = lwjgl("stb")
    val assimp = lwjgl("assimp")
    val bgfx = lwjgl("bgfx")
    val nanovg = lwjgl("nanovg")
    val nuklear = lwjgl("nuklear")
    val openal = lwjgl("openal")
    val par = lwjgl("par")
    val vulkan = lwjgl("vulkan")

    object Natives {
        val lwjgl = lwjgl("lwjgl", true)
        val glfw = lwjgl("glfw", true)
        val opengl = lwjgl("opengl", true)
        val stb = lwjgl("stb", true)
    }
    fun lwjgl(name: String? =  null, native: Boolean = false): String {
        val sub =  when(name) {
            "lwjgl" -> ":$name"
            null -> ""
            else -> ":lwjgl-$name"
        }
        val lib = "$group$sub:${Versions.lwjgl}"
        return if (native) {
            "$lib:$lwjglNatives"
        } else {
            lib
        }
    }
}

private val lwjglNatives = Pair(
    System.getProperty("os.name")!!,
    System.getProperty("os.arch")!!
).let { (name, arch) ->
    println("Native Info: $name $arch")
    when {
        arrayOf("Linux", "FreeBSD", "SunOS", "Unit").any { name.startsWith(it) } ->
            if (arrayOf("arm", "aarch64").any { arch.startsWith(it) })
                "natives-linux${if (arch.contains("64") || arch.startsWith("armv8")) "-arm64" else "-arm32"}"
            else
                "natives-linux"

        arrayOf("Mac OS X", "Darwin").any { name.startsWith(it) } ->
            "natives-macos${if (arch.startsWith("aarch64")) "-arm64" else ""}"

        arrayOf("Windows").any { name.startsWith(it) } ->
            if (arch.contains("64"))
                "natives-windows${if (arch.startsWith("aarch64")) "-arm64" else ""}"
            else
                "natives-windows-x86"

        else -> throw Error("Unrecognized or unsupported platform. Please set \"lwjglNatives\" manually")
    }
}