// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.hellocube.scene

import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.core.math.Vec3
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * The real Khronos glTF-Sample-Assets "Box" fixture (CC0,
 * github.com/KhronosGroup/glTF-Sample-Assets, `Models/Box/glTF-Binary/Box.glb`) -- the minimal
 * untextured box Track 0's `GltfParser.parseScene` was scoped for, base64-embedded directly in
 * commonMain source rather than bundled as a resource: same reasoning [gltfSampleQuadGlb] (in
 * `GltfSampleAsset.kt`) already gives for its own synthetic GLB -- no external asset/bundling
 * pipeline exists yet for wasmJs samples, and this way the fixture Just Works on every target
 * (desktop/Android/iOS/wasmJs) with zero resources.srcDir/webpack wiring. Distinct from
 * [gltfSampleQuadGlb]'s hand-built quad: this is a byte-for-byte copy of a real third-party
 * GLB file (fetched from the URL above), so a correct render is evidence the parser handles
 * an actual exporter's output (COLOR_0-less primitive, NORMAL attribute present but unused,
 * a node with an explicit 16-float `matrix` instead of TRS, a mesh node one level below the
 * scene root) -- not just this codebase's own synthetic fixtures.
 */
@OptIn(ExperimentalEncodingApi::class)
fun gltfBoxGlb(): ByteArray = Base64.decode(gltfBoxGlbBase64)

/**
 * [Mat4] has no `decompose()` (see [io.github.ronjunevaldoz.awake.core.mesh.gltf.GltfParser]'s
 * own doc comment on `Mat4.times` not composing the way its naming implies) -- this pulls
 * translation and per-axis scale directly out of the matrix's column-major
 * [Mat4.data]/[Mat4.m03] etc. accessors, the same way `GltfParser`'s own `trsMatrix`/`multiply`
 * read it, skipping rotation: `Box.glb`'s mesh node's world transform is a rotation-only
 * matrix (a Y-up/Z-up axis swap on its parent node), and `Box.glb`'s primitive has no
 * `COLOR_0` (defaults to solid white, see
 * `io.github.ronjunevaldoz.awake.core.mesh.gltf.GltfMesh.toInterleavedPositionColorUv`) on a
 * perfectly symmetric unit cube -- so its rendered silhouette is identical regardless of
 * which 90-degree-ish rotation gets applied, making an exact quaternion-to-Euler decompose
 * unnecessary for this fixture.
 *
 * ponytail: rotation decompose deferred -- add real quaternion->Euler extraction (matching
 * `io.github.ronjunevaldoz.awake.scene.components.Transform.localMatrix`'s own rotateZ/Y/X
 * convention) if a future fixture's node rotation needs to be visually correct, not just
 * axis-aligned-symmetric.
 */
fun Mat4.toPositionScale(): Pair<Vec3, Vec3> {
    val position = Vec3(m03, m13, m23)
    val scale = Vec3(
        Vec3(m00, m10, m20).length3(),
        Vec3(m01, m11, m21).length3(),
        Vec3(m02, m12, m22).length3()
    )
    return position to scale
}

@Suppress("MaxLineLength")
private val gltfBoxGlbBase64: String =
    "Z2xURgIAAACABgAA3AMAAEpTT057ImFzc2V0Ijp7ImdlbmVyYXRvciI6IkNPTExBREEyR0xURiIsInZlcnNpb24iOiIyLjAifSwic2NlbmUiOjAsInNj" +
    "ZW5lcyI6W3sibm9kZXMiOlswXX1dLCJub2RlcyI6W3siY2hpbGRyZW4iOlsxXSwibWF0cml4IjpbMS4wLDAuMCwwLjAsMC4wLDAuMCwwLjAsLTEuMCww" +
    "LjAsMC4wLDEuMCwwLjAsMC4wLDAuMCwwLjAsMC4wLDEuMF19LHsibWVzaCI6MH1dLCJtZXNoZXMiOlt7InByaW1pdGl2ZXMiOlt7ImF0dHJpYnV0ZXMi" +
    "OnsiTk9STUFMIjoxLCJQT1NJVElPTiI6Mn0sImluZGljZXMiOjAsIm1vZGUiOjQsIm1hdGVyaWFsIjowfV0sIm5hbWUiOiJNZXNoIn1dLCJhY2Nlc3Nv" +
    "cnMiOlt7ImJ1ZmZlclZpZXciOjAsImJ5dGVPZmZzZXQiOjAsImNvbXBvbmVudFR5cGUiOjUxMjMsImNvdW50IjozNiwibWF4IjpbMjNdLCJtaW4iOlsw" +
    "XSwidHlwZSI6IlNDQUxBUiJ9LHsiYnVmZmVyVmlldyI6MSwiYnl0ZU9mZnNldCI6MCwiY29tcG9uZW50VHlwZSI6NTEyNiwiY291bnQiOjI0LCJtYXgi" +
    "OlsxLjAsMS4wLDEuMF0sIm1pbiI6Wy0xLjAsLTEuMCwtMS4wXSwidHlwZSI6IlZFQzMifSx7ImJ1ZmZlclZpZXciOjEsImJ5dGVPZmZzZXQiOjI4OCwi" +
    "Y29tcG9uZW50VHlwZSI6NTEyNiwiY291bnQiOjI0LCJtYXgiOlswLjUsMC41LDAuNV0sIm1pbiI6Wy0wLjUsLTAuNSwtMC41XSwidHlwZSI6IlZFQzMi" +
    "fV0sIm1hdGVyaWFscyI6W3sicGJyTWV0YWxsaWNSb3VnaG5lc3MiOnsiYmFzZUNvbG9yRmFjdG9yIjpbMC44MDAwMDAwMTE5MjA5MjksMC4wLDAuMCwx" +
    "LjBdLCJtZXRhbGxpY0ZhY3RvciI6MC4wfSwibmFtZSI6IlJlZCJ9XSwiYnVmZmVyVmlld3MiOlt7ImJ1ZmZlciI6MCwiYnl0ZU9mZnNldCI6NTc2LCJi" +
    "eXRlTGVuZ3RoIjo3MiwidGFyZ2V0IjozNDk2M30seyJidWZmZXIiOjAsImJ5dGVPZmZzZXQiOjAsImJ5dGVMZW5ndGgiOjU3NiwiYnl0ZVN0cmlkZSI6" +
    "MTIsInRhcmdldCI6MzQ5NjJ9XSwiYnVmZmVycyI6W3siYnl0ZUxlbmd0aCI6NjQ4fV19iAIAAEJJTgAAAAAAAAAAAAAAgD8AAAAAAAAAAAAAgD8AAAAA" +
    "AAAAAAAAgD8AAAAAAAAAAAAAgD8AAAAAAACAvwAAAAAAAAAAAACAvwAAAAAAAAAAAACAvwAAAAAAAAAAAACAvwAAAAAAAIA/AAAAAAAAAAAAAIA/AAAA" +
    "AAAAAAAAAIA/AAAAAAAAAAAAAIA/AAAAAAAAAAAAAAAAAACAPwAAAAAAAAAAAACAPwAAAAAAAAAAAACAPwAAAAAAAAAAAACAPwAAAAAAAIC/AAAAAAAA" +
    "AAAAAIC/AAAAAAAAAAAAAIC/AAAAAAAAAAAAAIC/AAAAAAAAAAAAAAAAAAAAAAAAgL8AAAAAAAAAAAAAgL8AAAAAAAAAAAAAgL8AAAAAAAAAAAAAgL8A" +
    "AAC/AAAAvwAAAD8AAAA/AAAAvwAAAD8AAAC/AAAAPwAAAD8AAAA/AAAAPwAAAD8AAAA/AAAAvwAAAD8AAAC/AAAAvwAAAD8AAAA/AAAAvwAAAL8AAAC/" +
    "AAAAvwAAAL8AAAA/AAAAPwAAAD8AAAA/AAAAvwAAAD8AAAA/AAAAPwAAAL8AAAA/AAAAvwAAAL8AAAC/AAAAPwAAAD8AAAA/AAAAPwAAAD8AAAC/AAAA" +
    "PwAAAL8AAAA/AAAAPwAAAL8AAAC/AAAAvwAAAD8AAAC/AAAAPwAAAD8AAAC/AAAAvwAAAL8AAAC/AAAAPwAAAL8AAAC/AAAAvwAAAL8AAAC/AAAAPwAA" +
    "AL8AAAA/AAAAvwAAAL8AAAA/AAAAPwAAAL8AAAEAAgADAAIAAQAEAAUABgAHAAYABQAIAAkACgALAAoACQAMAA0ADgAPAA4ADQAQABEAEgATABIAEQAU" +
    "ABUAFgAXABYAFQA="
