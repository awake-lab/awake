// GPU-instanced camera-facing billboard, for VertexFormat.PositionUv (a shared unit quad --
// position/uv only, no normal/color). Locations continue right after PositionUv's own 2
// attributes (0/1) -- unlike instanced.wgsl's 3-attribute vertex format, PositionUv has no
// location 2, so the instance matrix starts there instead of at 3 (RenderPipeline's
// vertexInputState computes this same firstLocation dynamically from the vertex format, so
// this shader's literal locations must match that arithmetic, not instanced.wgsl's). Each
// instance's own model matrix (locations 2-5, same column-major instance buffer instanced.wgsl
// already documents) supplies world position and uniform scale; a per-instance RGBA color+alpha
// (location 6, DrawCall.instanceColors -- one vec4f/instance, alpha in .w) supplies independent
// per-particle fade/tint. No per-instance rotation: the quad always faces the camera, built
// from cameraRight/cameraUp rather than the instance matrix's own basis vectors.
struct Uniforms {
  viewProjection : mat4x4<f32>,
  // World-space camera basis, CPU-computed once per frame from Camera.eye/center/up (cross
  // products) -- not derivable from viewProjection alone without an inverse, and every
  // instance needs the same pair, so it rides in the uniform block rather than being
  // recomputed per vertex.
  cameraRight : vec4f,
  cameraUp : vec4f,
  // (frameCount, currentFrame, unused, unused) -- a horizontal sprite-strip atlas index, one
  // per EMITTER (every particle in a DrawCall shows the same frame simultaneously; this is a
  // synced flicker, not a per-particle-desynced animation -- see ParticleEmitter.frameCount's
  // own doc comment for that limitation). frameCount = 1 (the default) is a no-op: uv is used
  // unchanged.
  frameInfo : vec4f,
}
@binding(0) @group(0) var<uniform> uniforms : Uniforms;
@binding(1) @group(0) var particleTexture : texture_2d<f32>;
@binding(2) @group(0) var particleSampler : sampler;

struct VertexOutput {
  @builtin(position) position : vec4f,
  @location(0) uv : vec2f,
  @location(1) color : vec4f,
}

@vertex
fn vertexMain(
  @location(0) inPosition : vec3f,
  @location(1) inUv : vec2f,
  @location(2) model0 : vec4f,
  @location(3) model1 : vec4f,
  @location(4) model2 : vec4f,
  @location(5) model3 : vec4f,
  @location(6) inColor : vec4f,
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
  // Horizontal sprite-strip atlas: frame index picks a 1/frameCount-wide slice of the texture.
  let frameCount = uniforms.frameInfo.x;
  let currentFrame = uniforms.frameInfo.y;
  output.uv = vec2f((inUv.x + currentFrame) / frameCount, inUv.y);
  output.color = inColor;
  return output;
}

@fragment
fn fragmentMain(@location(0) uv : vec2f, @location(1) color : vec4f) -> @location(0) vec4f {
  let sampled = textureSample(particleTexture, particleSampler, uv);
  return vec4f(sampled.rgb * color.rgb, sampled.a * color.a);
}
