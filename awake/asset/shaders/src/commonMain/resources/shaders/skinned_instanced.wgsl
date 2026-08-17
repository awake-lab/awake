// skinned.wgsl's GPU-instanced companion: same PositionNormalColorSkin vertex layout and same
// weighted 4-joint blend, but N independently-posed copies in ONE draw call. Two things change
// versus skinned.wgsl, both forced by "one draw call covers many copies":
//   - the model matrix moves out of the uniform block into an instance-rate vertex buffer
//     (locations 5-8, continuing past this format's own 0-4), exactly as instanced.wgsl does;
//   - the joint palette moves out of the uniform block into a storage buffer indexed by
//     @builtin(instance_index). A uniform array can't be indexed per-instance here (it would
//     have to hold every copy's palette, and 64 mat4 per copy blows the 64 KB uniform limit
//     after two copies), and storage buffers are the only WGSL binding that can be runtime-sized.
const MAX_JOINTS : u32 = 64u;

struct Uniforms {
  viewProjection : mat4x4<f32>,
  lightDirection : vec4f,
  lightColor : vec4f,
}
@binding(0) @group(0) var<uniform> uniforms : Uniforms;

// Group 1, not group 0: group 0 is the material's own descriptor set on Vulkan (uniform buffer
// + texture bindings, shared by every pipeline), while this palette buffer is owned per
// instanced draw call and rewritten every frame. A separate set keeps the material layout
// byte-for-byte what every other shader already binds.
struct JointPalette {
  joints : array<mat4x4<f32>, MAX_JOINTS>,
}
@binding(0) @group(1) var<storage, read> palettes : array<JointPalette>;

struct VertexOutput {
  @builtin(position) position : vec4f,
  @location(0) color : vec3f,
  @location(1) normal : vec3f,
}

@vertex
fn vertexMain(
  @builtin(instance_index) instanceIndex : u32,
  @location(0) inPosition : vec3f,
  @location(1) inNormal : vec3f,
  @location(2) inColor : vec3f,
  @location(3) inJoints : vec4<u32>,
  @location(4) inWeights : vec4<f32>,
  @location(5) model0 : vec4f,
  @location(6) model1 : vec4f,
  @location(7) model2 : vec4f,
  @location(8) model3 : vec4f,
) -> VertexOutput {
  let palette = &palettes[instanceIndex];
  var skinMatrix =
    inWeights.x * (*palette).joints[inJoints.x] +
    inWeights.y * (*palette).joints[inJoints.y] +
    inWeights.z * (*palette).joints[inJoints.z] +
    inWeights.w * (*palette).joints[inJoints.w];

  let skinnedPosition = skinMatrix * vec4f(inPosition, 1.0);
  // w = 0 drops the translation column -- same convention as skinned.wgsl's own normal.
  let skinnedNormal = skinMatrix * vec4f(inNormal, 0.0);

  // model0..model3 are the four COLUMNS of this instance's model matrix, in the column-major
  // order Mat4.data already stores -- see instanced.wgsl.
  let model = mat4x4<f32>(model0, model1, model2, model3);

  var output : VertexOutput;
  output.position = uniforms.viewProjection * model * skinnedPosition;
  output.color = inColor;
  output.normal = (model * skinnedNormal).xyz;
  return output;
}

// Same object-space light as instanced.wgsl (which supplies it through the shared uniform
// block, unlike skinned.wgsl's hardcoded constant) -- see triangle.wgsl for the ceiling.
const AMBIENT_STRENGTH : f32 = 0.35;

@fragment
fn fragmentMain(@location(0) color : vec3f, @location(1) normal : vec3f) -> @location(0) vec4f {
  let n = normalize(normal);
  let l = normalize(uniforms.lightDirection.xyz);
  let diffuse = max(dot(n, l), 0.0);
  let shade = AMBIENT_STRENGTH + (1.0 - AMBIENT_STRENGTH) * diffuse;
  return vec4f(color * shade * uniforms.lightColor.xyz, 1.0);
}
