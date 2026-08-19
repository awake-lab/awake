// GPU-instanced camera-facing billboard, for VertexFormat.PositionUv (a shared unit quad --
// position/uv only, no normal/color). Locations continue right after PositionUv's own 2
// attributes (0/1) -- unlike instanced.wgsl's 3-attribute vertex format, PositionUv has no
// location 2, so the instance matrix starts there instead of at 3 (RenderPipeline's
// vertexInputState computes this same firstLocation dynamically from the vertex format, so
// this shader's literal locations must match that arithmetic, not instanced.wgsl's). Each
// instance's own model matrix (locations 2-5, same column-major instance buffer instanced.wgsl
// already documents) supplies world position and uniform scale; a per-instance RGBA color+alpha
// (location 6, DrawCall.instanceColors -- one vec4f/instance, alpha in .w) supplies independent
// per-particle fade/tint; a per-instance sprite-strip frame index (location 7,
// DrawCall.instanceFrames -- one f32/instance) lets particles sharing one emitter cycle frames
// desynced instead of in lockstep. No per-instance rotation: the quad always faces the camera,
// built from cameraRight/cameraUp rather than the instance matrix's own basis vectors.
//
// Column 1 (model1) is otherwise unused by this shader (only column 0's length and column 3's
// translation are read) -- ParticleVisual.stretchWithVelocity reuses it to carry an optional
// world-space stretch vector (direction * length, zero for a plain billboard), read below to
// elongate the quad toward its own on-screen motion direction instead of adding a whole new
// per-instance GPU buffer for one optional capability.
struct Uniforms {
  viewProjection : mat4x4<f32>,
  // World-space camera basis, CPU-computed once per frame from Camera.eye/center/up (cross
  // products) -- not derivable from viewProjection alone without an inverse, and every
  // instance needs the same pair, so it rides in the uniform block rather than being
  // recomputed per vertex.
  cameraRight : vec4f,
  cameraUp : vec4f,
  // (frameCount, unused/reserved, unused, unused) -- frameCount is the horizontal sprite-strip
  // atlas's fixed width (one per EMITTER/material). Which frame each instance shows is now
  // per-particle (inFrame, below), not frameInfo.y -- kept as a reserved pad slot rather than
  // reshuffling the struct. frameCount = 1 (the default) is a no-op: uv is used unchanged.
  frameInfo : vec4f,
}
@binding(0) @group(0) var<uniform> uniforms : Uniforms;
@binding(1) @group(0) var particleTexture : texture_2d<f32>;
@binding(2) @group(0) var particleSampler : sampler;

const STRETCH_EPSILON : f32 = 1e-5;

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
  @location(7) inFrame : f32,
) -> VertexOutput {
  let model = mat4x4<f32>(model0, model1, model2, model3);
  // Translation-only read: this system never writes rotation into an instance's model matrix
  // (see ParticleEmitter's own doc comment), so column 3 is exactly the particle's world
  // center and column 0's own length is exactly its uniform scale.
  let center = model[3].xyz;
  let width = length(model[0].xyz);
  // Velocity-stretch (see this file's top-of-file comment on column 1): project the world-space
  // stretch vector onto the camera-right/camera-up screen plane, and if it's non-zero, point the
  // quad's LONG axis toward that on-screen direction (with CROSS perpendicular to it) instead of
  // the plain cameraRight/cameraUp pair -- a zero stretch vector (the default) falls through to
  // exactly the old symmetric-quad formula, byte-for-byte.
  let stretch = model[1].xyz;
  let stretchScreen = vec2f(dot(stretch, uniforms.cameraRight.xyz), dot(stretch, uniforms.cameraUp.xyz));
  let stretchLength = length(stretchScreen);
  var longAxis = uniforms.cameraUp.xyz;
  var crossAxis = uniforms.cameraRight.xyz;
  var lengthScale = width;
  if (stretchLength > STRETCH_EPSILON) {
    let dir = stretchScreen / stretchLength;
    longAxis = uniforms.cameraRight.xyz * dir.x + uniforms.cameraUp.xyz * dir.y;
    crossAxis = uniforms.cameraRight.xyz * -dir.y + uniforms.cameraUp.xyz * dir.x;
    lengthScale = width + stretchLength;
  }
  let worldPos = center
    + (inPosition.x * width) * crossAxis
    + (inPosition.y * lengthScale) * longAxis;

  var output : VertexOutput;
  output.position = uniforms.viewProjection * vec4f(worldPos, 1.0);
  // Horizontal sprite-strip atlas: this instance's own frame index (desynced per-particle,
  // see inFrame's own doc comment above) picks a 1/frameCount-wide slice of the texture.
  let frameCount = uniforms.frameInfo.x;
  output.uv = vec2f((inUv.x + inFrame) / frameCount, inUv.y);
  output.color = inColor;
  return output;
}

@fragment
fn fragmentMain(@location(0) uv : vec2f, @location(1) color : vec4f) -> @location(0) vec4f {
  let sampled = textureSample(particleTexture, particleSampler, uv);
  return vec4f(sampled.rgb * color.rgb, sampled.a * color.a);
}
