// Vulkan-only variant of triangle.wgsl that additionally samples a shadow map. Kept as a
// separate file (not a triangle.wgsl edit) so WebGPU -- which has no shadow map, comparison
// sampler infra, or bind-group wiring for one yet -- keeps using the original triangle.wgsl
// unmodified; this file is still synced to a WebGPU .wgsl copy by the shared build convention,
// but nothing on the WebGPU side ever loads it, so that copy is inert. See
// Scene3DPlaygroundVulkanBootstrap.kt for the Vulkan-only wiring.
struct Uniforms {
  mvp : mat4x4<f32>,
  // xyz = the direction the light shines FROM. w = the NDC depth one shadow-map texel of
  // lateral travel spans on a 45-degree surface, supplied by the renderer because only it knows
  // the light frustum's depth range and the map size. Re-deriving that here from copied
  // constants is how a depth bias ends up meaning something different in each file.
  lightDirection : vec4f,
  lightColor : vec4f,
  // Model matrix combined with the light's own view-projection (same "model * viewProjection"
  // convention prepareDrawCalls already uses for mvp) -- lets the vertex shader project each
  // vertex into the shadow map's clip space without a separate model-matrix uniform. Appended
  // LAST (not after mvp) so the first 24 floats stay byte-identical to plain triangle.wgsl's
  // Uniforms -- see prepareDrawCalls' own doc comment.
  lightMvp : mat4x4<f32>,
  // Model matrix on its own -- PBR needs a world-space position and normal to build the view
  // vector, which mvp/lightMvp can't be decomposed back into.
  model : mat4x4<f32>,
  cameraPosition : vec4f,
  // x = metallic, y = roughness. Packed as vec4f for the same 16-byte alignment reason as
  // lightDirection above.
  material : vec4f,
  // rgb = fog colour, a = fog DENSITY (not alpha) -- packed into the unused 4th component the
  // same way lightDirection.w above already carries the shadow depth scale. Density 0 (the
  // renderer default) makes applyFog a no-op.
  fogColor : vec4f,
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
  @location(3) worldPos : vec3f,
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
  output.worldPos = (uniforms.model * vec4f(inPosition, 1.0)).xyz;
  return output;
}

const AMBIENT_STRENGTH : f32 = 0.08;
const PI : f32 = 3.14159265359;
const EPSILON : f32 = 0.0001;
const DIELECTRIC_F0 : f32 = 0.04;
const MIN_ROUGHNESS : f32 = 0.05;
// Measured in shadow-map texels of depth (scaled by lightDirection.w), NOT raw NDC depth: the
// light's ortho frustum is ~40 units deep, so a bias written as raw NDC silently means ~40x more
// world-space offset than it reads and detaches the shadow from its caster. The constant term
// covers depth quantisation; the slope term covers the PCF kernel's +/-1 texel reach across a
// surface tilted away from the light. Front-face culling would fix acne structurally instead,
// but the cube's winding isn't reliable.
const SHADOW_BIAS_TEXELS : f32 = 1.0;
const SHADOW_SLOPE_TEXELS : f32 = 2.0;
const MAX_SHADOW_SLOPE : f32 = 8.0;
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
  let slope = min(sqrt(max(1.0 - nDotL * nDotL, 0.0)) / max(nDotL, EPSILON), MAX_SHADOW_SLOPE);
  let bias = uniforms.lightDirection.w * (SHADOW_BIAS_TEXELS + SHADOW_SLOPE_TEXELS * slope);
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

// GGX/Trowbridge-Reitz normal distribution: the share of microfacets aligned with the halfway
// vector, which is what makes a rough surface's highlight broad and a smooth one's tight.
fn distributionGgx(nDotH : f32, roughness : f32) -> f32 {
  let a = roughness * roughness;
  let a2 = a * a;
  let d = nDotH * nDotH * (a2 - 1.0) + 1.0;
  return a2 / max(PI * d * d, EPSILON);
}

// Smith geometry term with Schlick-GGX, accounting for microfacets shadowing each other.
fn geometrySmith(nDotV : f32, nDotL : f32, roughness : f32) -> f32 {
  let r = roughness + 1.0;
  let k = (r * r) / 8.0;
  let ggxV = nDotV / (nDotV * (1.0 - k) + k);
  let ggxL = nDotL / (nDotL * (1.0 - k) + k);
  return ggxV * ggxL;
}

fn fresnelSchlick(cosTheta : f32, f0 : vec3f) -> vec3f {
  return f0 + (vec3f(1.0) - f0) * pow(clamp(1.0 - cosTheta, 0.0, 1.0), 5.0);
}

// Standard exponential distance fog. A density of 0 leaves [color] untouched, which is what
// every scene renders with until a game sets Renderer.fogDensity.
fn applyFog(color : vec3f, worldPos : vec3f) -> vec3f {
  let dist = length(uniforms.cameraPosition.xyz - worldPos);
  let fogAmount = 1.0 - exp(-uniforms.fogColor.a * dist);
  return mix(color, uniforms.fogColor.rgb, saturate(fogAmount));
}

@fragment
fn fragmentMain(
  @location(0) color : vec3f,
  @location(1) normal : vec3f,
  @location(2) shadowPos : vec4f,
  @location(3) worldPos : vec3f,
) -> @location(0) vec4f {
  let n = normalize(normal);
  let l = normalize(uniforms.lightDirection.xyz);
  let v = normalize(uniforms.cameraPosition.xyz - worldPos);
  let h = normalize(v + l);

  let nDotL = max(dot(n, l), 0.0);
  let nDotV = max(dot(n, v), EPSILON);
  let nDotH = max(dot(n, h), 0.0);

  let metallic = clamp(uniforms.material.x, 0.0, 1.0);
  // Floored: a perfectly smooth surface collapses the GGX denominator into a single-pixel
  // highlight that aliases badly under any camera motion.
  let roughness = clamp(uniforms.material.y, MIN_ROUGHNESS, 1.0);

  // Metals have no diffuse and tint their specular with the base colour; dielectrics do the
  // opposite and reflect a flat 4%. Interpolating by `metallic` is the standard workflow.
  let f0 = mix(vec3f(DIELECTRIC_F0), color, metallic);
  let fresnel = fresnelSchlick(max(dot(h, v), 0.0), f0);
  let specular = (distributionGgx(nDotH, roughness) * geometrySmith(nDotV, nDotL, roughness) *
    fresnel) / max(4.0 * nDotV * nDotL, EPSILON);
  let diffuse = (vec3f(1.0) - fresnel) * (1.0 - metallic) * color / PI;

  let shadowFactor = sampleShadow(shadowPos, nDotL);
  // lightColor is authored as "a surface facing this light head-on reflects its own albedo"
  // (what the Lambert path this replaced did), not as radiance -- so the BRDF's 1/PI has to be
  // paid back here. Without it every lit surface renders at 1/PI of its former brightness.
  let radiance = uniforms.lightColor.xyz * PI * nDotL * shadowFactor;
  let specularOut = specular * radiance;
  let ambient = color * AMBIENT_STRENGTH;
  // Reinhard over the specular lobe alone -- that is the only term that can exceed 1.0 (GGX
  // spikes at low roughness). Running the whole image through it maps 1.0 to 0.5 and takes
  // every midtone down with it.
  let lit = ambient + diffuse * radiance + specularOut / (specularOut + vec3f(1.0));
  return vec4f(applyFog(lit, worldPos), 1.0);
}
