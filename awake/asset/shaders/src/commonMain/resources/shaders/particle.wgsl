// GPU-instanced camera-facing billboard, for VertexFormat.PositionUv (a shared unit quad --
// position/uv only, no normal/color). Each instance's own model matrix (locations 3-6, same
// column-major instance buffer instanced.wgsl already documents) supplies world position and
// uniform scale; a SEPARATE per-instance alpha (location 7, its own small instance-rate
// buffer -- see DrawCall.instanceAlphas' own doc comment) supplies independent fade. No
// per-instance rotation: the quad always faces the camera, built from cameraRight/cameraUp
// rather than the instance matrix's own basis vectors.
struct Uniforms {
  viewProjection : mat4x4<f32>,
  // World-space camera basis, CPU-computed once per frame from Camera.eye/center/up (cross
  // products) -- not derivable from viewProjection alone without an inverse, and every
  // instance needs the same pair, so it rides in the uniform block rather than being
  // recomputed per vertex.
  cameraRight : vec4f,
  cameraUp : vec4f,
}
@binding(0) @group(0) var<uniform> uniforms : Uniforms;
@binding(1) @group(0) var particleTexture : texture_2d<f32>;
@binding(2) @group(0) var particleSampler : sampler;

struct VertexOutput {
  @builtin(position) position : vec4f,
  @location(0) uv : vec2f,
  @location(1) alpha : f32,
}

@vertex
fn vertexMain(
  @location(0) inPosition : vec3f,
  @location(1) inUv : vec2f,
  @location(3) model0 : vec4f,
  @location(4) model1 : vec4f,
  @location(5) model2 : vec4f,
  @location(6) model3 : vec4f,
  @location(7) inAlpha : f32,
) -> VertexOutput {
  let model = mat4x4<f32>(model0, model1, model2, model3);
  // Translation-only read: this system never writes rotation into an instance's model matrix
  // (see ParticleEmitter's own doc comment), so column 3 is exactly the particle's world
  // center and column 0's own length is exactly its uniform scale.
  let center = model[3].xyz;
  let scale = length(model[0].xyz);
  let worldPos = center
    + (inPosition.x * scale) * uniforms.cameraRight.xyz
    + (inPosition.y * scale) * uniforms.cameraUp.xyz;

  var output : VertexOutput;
  output.position = uniforms.viewProjection * vec4f(worldPos, 1.0);
  output.uv = inUv;
  output.alpha = inAlpha;
  return output;
}

@fragment
fn fragmentMain(@location(0) uv : vec2f, @location(1) alpha : f32) -> @location(0) vec4f {
  let sampled = textureSample(particleTexture, particleSampler, uv);
  return vec4f(sampled.rgb, sampled.a * alpha);
}
