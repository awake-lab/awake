// GPU skinning variant of triangle.wgsl -- same MVP transform and Lambertian shading, plus a
// joint-matrix palette blended per-vertex before MVP is applied. MAX_JOINTS bounds the fixed-size
// uniform array WGSL requires (no dynamically-sized uniform arrays) -- CesiumMan's own skeleton
// has 19 joints; 64 leaves headroom for any other rigged asset this demo might load later without
// needing a second shader variant. Unused joint slots are harmless: a vertex only ever indexes
// the (up to 4) joints its own JOINTS_0/WEIGHTS_0 actually reference.
const MAX_JOINTS : u32 = 64u;

struct Uniforms {
  mvp : mat4x4<f32>,
  jointPalette : array<mat4x4<f32>, MAX_JOINTS>,
}
@binding(0) @group(0) var<uniform> uniforms : Uniforms;

struct VertexOutput {
  @builtin(position) position : vec4f,
  @location(0) color : vec3f,
  @location(1) normal : vec3f,
}

@vertex
fn vertexMain(
  @location(0) inPosition : vec3f,
  @location(1) inNormal : vec3f,
  @location(2) inColor : vec3f,
  @location(3) inJoints : vec4<u32>,
  @location(4) inWeights : vec4<f32>,
) -> VertexOutput {
  var skinMatrix =
    inWeights.x * uniforms.jointPalette[inJoints.x] +
    inWeights.y * uniforms.jointPalette[inJoints.y] +
    inWeights.z * uniforms.jointPalette[inJoints.z] +
    inWeights.w * uniforms.jointPalette[inJoints.w];

  let skinnedPosition = skinMatrix * vec4f(inPosition, 1.0);
  // Normals ignore translation -- w = 0 drops the matrix's translation column from the multiply,
  // same convention `triangle.wgsl`'s own model-space normal already relies on.
  let skinnedNormal = skinMatrix * vec4f(inNormal, 0.0);

  var output : VertexOutput;
  output.position = uniforms.mvp * skinnedPosition;
  output.color = inColor;
  output.normal = skinnedNormal.xyz;
  return output;
}

// Same lighting model as triangle.wgsl -- see that file's own comment for the object-space
// light-direction ceiling this inherits unchanged.
const LIGHT_DIRECTION : vec3f = vec3f(0.4, 0.8, 0.4);
const AMBIENT_STRENGTH : f32 = 0.35;

@fragment
fn fragmentMain(@location(0) color : vec3f, @location(1) normal : vec3f) -> @location(0) vec4f {
  let n = normalize(normal);
  let l = normalize(LIGHT_DIRECTION);
  let diffuse = max(dot(n, l), 0.0);
  let shade = AMBIENT_STRENGTH + (1.0 - AMBIENT_STRENGTH) * diffuse;
  return vec4f(color * shade, 1.0);
}
