// Textured variant of triangle.wgsl -- same MVP transform and Lambertian shading, plus a
// baseColorTexture sample multiplied into the vertex color. binding(1) is the same combined
// image/sampler slot every Material already builds (io.github.ronjunevaldoz.awake.vulkan
// .material.Material's descriptor set layout is unconditional -- a material with no real
// texture just binds a 1x1 white placeholder there), so this is the first shader that actually
// reads it.
struct Uniforms {
  mvp : mat4x4<f32>,
}
@binding(0) @group(0) var<uniform> uniforms : Uniforms;
@binding(1) @group(0) var baseColorTexture : texture_2d<f32>;
@binding(2) @group(0) var baseColorSampler : sampler;

struct VertexOutput {
  @builtin(position) position : vec4f,
  @location(0) color : vec3f,
  @location(1) normal : vec3f,
  @location(2) uv : vec2f,
}

@vertex
fn vertexMain(
  @location(0) inPosition : vec3f,
  @location(1) inNormal : vec3f,
  @location(2) inColor : vec3f,
  @location(3) inUv : vec2f,
) -> VertexOutput {
  var output : VertexOutput;
  output.position = uniforms.mvp * vec4f(inPosition, 1.0);
  output.color = inColor;
  output.normal = inNormal;
  // io.github.ronjunevaldoz.awake.core.graphics.createBitmap's own actuals all flip Y for
  // OpenGL's bottom-up UV convention (this file's caller reuses that shared decode utility
  // rather than duplicating it for Vulkan's top-down convention) -- undo it here instead.
  output.uv = vec2f(inUv.x, 1.0 - inUv.y);
  return output;
}

// Same lighting model as triangle.wgsl -- see that file's own comment for the object-space
// light-direction ceiling this inherits unchanged.
const LIGHT_DIRECTION : vec3f = vec3f(0.4, 0.8, 0.4);
const AMBIENT_STRENGTH : f32 = 0.35;

@fragment
fn fragmentMain(
  @location(0) color : vec3f,
  @location(1) normal : vec3f,
  @location(2) uv : vec2f,
) -> @location(0) vec4f {
  let n = normalize(normal);
  let l = normalize(LIGHT_DIRECTION);
  let diffuse = max(dot(n, l), 0.0);
  let shade = AMBIENT_STRENGTH + (1.0 - AMBIENT_STRENGTH) * diffuse;
  let baseColor = textureSample(baseColorTexture, baseColorSampler, uv);
  return vec4f(baseColor.rgb * color * shade, baseColor.a);
}
