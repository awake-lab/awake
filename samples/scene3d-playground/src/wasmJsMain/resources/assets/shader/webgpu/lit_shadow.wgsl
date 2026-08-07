// Vulkan-only variant of triangle.wgsl that additionally samples a shadow map. Kept as a
// separate file (not a triangle.wgsl edit) so WebGPU -- which has no shadow map, comparison
// sampler infra, or bind-group wiring for one yet -- keeps using the original triangle.wgsl
// unmodified; this file is still synced to a WebGPU .wgsl copy by the shared build convention,
// but nothing on the WebGPU side ever loads it, so that copy is inert. See
// Scene3DPlaygroundVulkanBootstrap.kt for the Vulkan-only wiring.
struct Uniforms {
  mvp : mat4x4<f32>,
  lightDirection : vec4f,
  lightColor : vec4f,
  // Model matrix combined with the light's own view-projection (same "model * viewProjection"
  // convention prepareDrawCalls already uses for mvp) -- lets the vertex shader project each
  // vertex into the shadow map's clip space without a separate model-matrix uniform. Appended
  // LAST (not after mvp) so the first 24 floats stay byte-identical to plain triangle.wgsl's
  // Uniforms -- see prepareDrawCalls' own doc comment.
  lightMvp : mat4x4<f32>,
}
@binding(0) @group(0) var<uniform> uniforms : Uniforms;
// Bindings 1/2 (base-color texture) are declared by triangle.wgsl's descriptor-set-layout
// twin but unused by this shader, same as triangle.wgsl already leaves them unused -- see
// Material.kt's own doc comment for why every material gets them regardless.
@binding(3) @group(0) var shadowMap : texture_2d<f32>;
@binding(4) @group(0) var shadowMapSampler : sampler;

struct VertexOutput {
  @builtin(position) position : vec4f,
  @location(0) color : vec3f,
  @location(1) normal : vec3f,
  @location(2) shadowPos : vec4f,
}

@vertex
fn vertexMain(
  @location(0) inPosition : vec3f,
  @location(1) inNormal : vec3f,
  @location(2) inColor : vec3f,
) -> VertexOutput {
  var output : VertexOutput;
  output.position = uniforms.mvp * vec4f(inPosition, 1.0);
  output.color = inColor;
  output.normal = inNormal;
  output.shadowPos = uniforms.lightMvp * vec4f(inPosition, 1.0);
  return output;
}

const AMBIENT_STRENGTH : f32 = 0.35;
// Slope-scaled: the depth gradient per shadow texel grows as a surface tilts away from the
// light, so one constant either leaves grazing-angle acne or detaches face-on contact shadows.
// Front-face culling would fix this structurally, but the cube's winding isn't reliable.
const SHADOW_BIAS_MIN : f32 = 0.0015;
const SHADOW_BIAS_MAX : f32 = 0.0090;
const PCF_RADIUS : i32 = 1;

// Vulkan/WebGPU NDC depth is already 0..1 (ClipSpace.depthZeroToOne), so shadowPos.z after the
// perspective divide is directly comparable to the shadow map's stored depth -- no OpenGL-style
// *0.5+0.5 remap needed.
fn sampleShadow(shadowPos : vec4f, nDotL : f32) -> f32 {
  if (shadowPos.w <= 0.0) {
    return 1.0;
  }
  let ndc = shadowPos.xyz / shadowPos.w;
  let uv = ndc.xy * vec2f(0.5, 0.5) + vec2f(0.5, 0.5);
  if (uv.x < 0.0 || uv.x > 1.0 || uv.y < 0.0 || uv.y > 1.0 || ndc.z < 0.0 || ndc.z > 1.0) {
    return 1.0;
  }
  let bias = max(SHADOW_BIAS_MAX * (1.0 - nDotL), SHADOW_BIAS_MIN);
  let texSize = vec2f(textureDimensions(shadowMap));
  let texel = 1.0 / texSize;
  var shadow = 0.0;
  var samples = 0.0;
  // Manual PCF, not a comparison sampler: this repo's Vulkan JNI binding layer
  // (VkSamplerCreateInfo) has no compareEnable/compareOp fields today, so the shadow map is
  // sampled as a plain texture and compared in-shader instead of via hardware Dref sampling.
  for (var dx = -PCF_RADIUS; dx <= PCF_RADIUS; dx = dx + 1) {
    for (var dy = -PCF_RADIUS; dy <= PCF_RADIUS; dy = dy + 1) {
      let offset = vec2f(f32(dx), f32(dy)) * texel;
      // Explicit LOD (not textureSample): implicit-derivative sampling inside a loop is the
      // kind of non-uniform control flow WGSL/SPIR-V leaves undefined.
      let closestDepth = textureSampleLevel(shadowMap, shadowMapSampler, uv + offset, 0.0).r;
      shadow = shadow + select(1.0, 0.0, ndc.z - bias > closestDepth);
      samples = samples + 1.0;
    }
  }
  return shadow / samples;
}

@fragment
fn fragmentMain(
  @location(0) color : vec3f,
  @location(1) normal : vec3f,
  @location(2) shadowPos : vec4f,
) -> @location(0) vec4f {
  let n = normalize(normal);
  let l = normalize(uniforms.lightDirection.xyz);
  let diffuse = max(dot(n, l), 0.0);
  let shadowFactor = sampleShadow(shadowPos, diffuse);
  let shade = AMBIENT_STRENGTH + (1.0 - AMBIENT_STRENGTH) * diffuse * shadowFactor;
  return vec4f(color * shade * uniforms.lightColor.xyz, 1.0);
}
