// Procedural sky drawn as the FIRST thing in the 3D pass, with depth test and depth write both
// off -- nothing has been drawn yet at that point, so there is nothing to z-fight with and no
// clear-depth-to-1.0 trick is needed.
//
// No vertex buffer: the vertex stage emits a single oversized triangle from vertex_index, which
// covers the whole viewport in three vertices (the standard full-screen-triangle trick; a
// full-screen quad would need two triangles and a seam along their shared diagonal).
struct Uniforms {
  // Inverse of the frame's view-projection -- unprojects a pixel's NDC back to a world-space
  // point, which minus cameraEye gives that pixel's view ray. Computed once per frame on the
  // CPU (Mat4.inverse) rather than per pixel.
  inverseViewProjection : mat4x4<f32>,
  // vec4f (not vec3f) for all four of these: vec3 in a uniform struct still occupies 16 bytes,
  // so packing them as vec4f makes the Kotlin-side float layout match what's actually read.
  cameraEye : vec4f,
  // The scene light's direction, i.e. the direction the light shines FROM -- so this is where
  // the sun disc is painted, and -this is where the moon goes.
  sunDirection : vec4f,
  horizonColor : vec4f,
  zenithColor : vec4f,
  sunColor : vec4f,
  moonColor : vec4f,
}
@binding(0) @group(0) var<uniform> uniforms : Uniforms;

struct VertexOutput {
  @builtin(position) position : vec4f,
  @location(0) ndc : vec2f,
}

@vertex
fn vertexMain(@builtin(vertex_index) vertexIndex : u32) -> VertexOutput {
  var corners = array<vec2f, 3>(vec2f(-1.0, -1.0), vec2f(3.0, -1.0), vec2f(-1.0, 3.0));
  let corner = corners[vertexIndex];
  var output : VertexOutput;
  // z = 1 (the far plane in both backends' 0..1 depth range). Depth testing is off, so this is
  // only about not writing a nearer depth if a future pipeline variant ever turns writes on.
  output.position = vec4f(corner, 1.0, 1.0);
  // Passed through unchanged: this IS the pixel's NDC, in whatever clip space the frame's
  // view-projection was built for, so the unprojection below stays convention-agnostic.
  output.ndc = corner;
  return output;
}

// Cosines of the angular radii, not angles: dot(rayDir, sunDirection) is already a cosine, so
// comparing in cosine space avoids an acos per pixel. INNER is the solid core, OUTER the outer
// edge of the soft falloff -- the real sun subtends about 0.5 degrees, widened here so the disc
// reads at typical field-of-view/resolution combinations.
const SUN_COS_OUTER : f32 = 0.9975;
const SUN_COS_INNER : f32 = 0.9995;
const MOON_COS_OUTER : f32 = 0.9985;
const MOON_COS_INNER : f32 = 0.9996;

@fragment
fn fragmentMain(@location(0) ndc : vec2f) -> @location(0) vec4f {
  // Any depth inside the frustum unprojects to a point along the same ray; the far plane (z=1)
  // is used because it is numerically the best-conditioned choice for a direction.
  let far = uniforms.inverseViewProjection * vec4f(ndc, 1.0, 1.0);
  let rayDir = normalize(far.xyz / far.w - uniforms.cameraEye.xyz);
  let sunDir = normalize(uniforms.sunDirection.xyz);

  var color = mix(uniforms.horizonColor.rgb, uniforms.zenithColor.rgb, saturate(rayDir.y));

  let sun = smoothstep(SUN_COS_OUTER, SUN_COS_INNER, dot(rayDir, sunDir));
  color += uniforms.sunColor.rgb * sun;
  // Scaled by (1 - sun) so the two discs can never both paint the same pixel, which they
  // otherwise would for a camera looking along the light axis at a grazing angle.
  let moon = smoothstep(MOON_COS_OUTER, MOON_COS_INNER, dot(rayDir, -sunDir));
  color += uniforms.moonColor.rgb * moon * (1.0 - sun);

  return vec4f(color, 1.0);
}
