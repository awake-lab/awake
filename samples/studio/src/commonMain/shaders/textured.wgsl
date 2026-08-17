// glTF metallic-roughness PBR for the PositionNormalColorUv format. Same Cook-Torrance BRDF
// lit_shadow.wgsl already uses (that file is the reference for the terms and for why the
// specular lobe alone is tonemapped) -- this one reads metallic/roughness/normal/occlusion/
// emissive from textures instead of from uniforms, and has no shadow map.
//
// A material with no map for a channel binds a 1x1 neutral placeholder there (see
// Renderer.createMaterial's defaultPbrTexture* on each backend), so this shader samples all
// five unconditionally rather than branching on presence.
struct Uniforms {
  mvp : mat4x4<f32>,
  // Same first 24 floats as triangle.wgsl (mvp + light) -- see its own Uniforms doc comment
  // for why both light fields are vec4f. model/cameraPosition follow, in lit_shadow.wgsl's
  // order, because a specular term needs a world-space position and view vector that mvp
  // alone can't be decomposed back into.
  lightDirection : vec4f,
  lightColor : vec4f,
  model : mat4x4<f32>,
  cameraPosition : vec4f,
  // glTF material factors -- x=metallicFactor, y=roughnessFactor (zw unused); multiplied into
  // the corresponding texture sample below, same convention glTF itself uses (factor * texture
  // channel, texture defaults to a neutral 1.0 sample when the material has no map for it).
  material : vec4f,
  baseColorFactor : vec4f,
  // xyz used, w unused -- glTF emissiveFactor has no alpha component.
  emissiveFactor : vec4f,
}
@binding(0) @group(0) var<uniform> uniforms : Uniforms;
@binding(1) @group(0) var baseColorTexture : texture_2d<f32>;
// One sampler for every material texture -- all five are sampled with the same filtering at
// the same uv, so per-map samplers would only double the binding count.
@binding(2) @group(0) var baseColorSampler : sampler;
// 3/4 are the shadow map in Material.kt's shared descriptor set layout (lit_shadow.wgsl's
// bindings) -- left unused here so the numbering stays stable across both shaders.
@binding(5) @group(0) var metallicRoughnessTexture : texture_2d<f32>;
@binding(6) @group(0) var normalTexture : texture_2d<f32>;
@binding(7) @group(0) var occlusionTexture : texture_2d<f32>;
@binding(8) @group(0) var emissiveTexture : texture_2d<f32>;

struct VertexOutput {
  @builtin(position) position : vec4f,
  @location(0) color : vec3f,
  @location(1) normal : vec3f,
  @location(2) uv : vec2f,
  @location(3) worldPos : vec3f,
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
  // World space, unlike triangle.wgsl's object-space normal: the light direction and camera
  // position this shader gets are both world-space, so shading stays put while the mesh spins.
  // The model matrix (not its inverse transpose) is used directly -- correct for the rigid/
  // uniformly-scaled transforms Transform can express today.
  output.normal = (uniforms.model * vec4f(inNormal, 0.0)).xyz;
  output.worldPos = (uniforms.model * vec4f(inPosition, 1.0)).xyz;
  // io.github.ronjunevaldoz.awake.core.graphics.createBitmap's own actuals all flip Y for
  // OpenGL's bottom-up UV convention (this file's caller reuses that shared decode utility
  // rather than duplicating it for Vulkan's top-down convention) -- undo it here instead.
  output.uv = vec2f(inUv.x, 1.0 - inUv.y);
  return output;
}

const AMBIENT_STRENGTH : f32 = 0.08;
const PI : f32 = 3.14159265359;
const EPSILON : f32 = 0.0001;
const DIELECTRIC_F0 : f32 = 0.04;
const MIN_ROUGHNESS : f32 = 0.05;

fn distributionGgx(nDotH : f32, roughness : f32) -> f32 {
  let a = roughness * roughness;
  let a2 = a * a;
  let d = nDotH * nDotH * (a2 - 1.0) + 1.0;
  return a2 / max(PI * d * d, EPSILON);
}

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

// ponytail: TBN reconstructed per-pixel from screen-space derivatives, not from a TANGENT
// vertex attribute -- neither the glTF parser nor VertexFormat carries tangents, and Duck.gltf
// (the only asset on this path) has none to carry. Costs 4 derivative ops per pixel and gives
// a slightly noisier basis on low-poly silhouettes. Upgrade path: read/generate tangents at
// load time (MikkTSpace) and add a Float4 tangent slot to PositionNormalColorUv.
fn perturbNormal(n : vec3f, worldPos : vec3f, uv : vec2f, tangentNormal : vec3f) -> vec3f {
  let dPosDx = dpdx(worldPos);
  let dPosDy = dpdy(worldPos);
  let dUvDx = dpdx(uv);
  let dUvDy = dpdy(uv);
  let perpDy = cross(dPosDy, n);
  let perpDx = cross(n, dPosDx);
  let tangent = perpDy * dUvDx.x + perpDx * dUvDy.x;
  let bitangent = perpDy * dUvDx.y + perpDx * dUvDy.y;
  // Degenerate uv (a mesh with a collapsed uv island, or the flat-normal placeholder's own
  // constant sample) leaves both vectors near zero -- fall back to the geometric normal.
  let scale = max(dot(tangent, tangent), dot(bitangent, bitangent));
  if (scale < EPSILON) {
    return n;
  }
  let invMax = inverseSqrt(scale);
  return normalize(mat3x3<f32>(tangent * invMax, bitangent * invMax, n) * tangentNormal);
}

@fragment
fn fragmentMain(
  @location(0) color : vec3f,
  @location(1) normal : vec3f,
  @location(2) uv : vec2f,
  @location(3) worldPos : vec3f,
) -> @location(0) vec4f {
  let baseColorSample = textureSample(baseColorTexture, baseColorSampler, uv);
  let albedo = baseColorSample.rgb * color * uniforms.baseColorFactor.rgb;
  // glTF metallic-roughness convention: G = roughness, B = metalness. Floored roughness --
  // see lit_shadow.wgsl for why a perfectly smooth surface aliases.
  let metallicRoughness = textureSample(metallicRoughnessTexture, baseColorSampler, uv);
  let metallic = clamp(metallicRoughness.b * uniforms.material.x, 0.0, 1.0);
  let roughness = clamp(metallicRoughness.g * uniforms.material.y, MIN_ROUGHNESS, 1.0);
  let occlusion = textureSample(occlusionTexture, baseColorSampler, uv).r;
  let emissive = textureSample(emissiveTexture, baseColorSampler, uv).rgb * uniforms.emissiveFactor.rgb;
  let tangentNormal = textureSample(normalTexture, baseColorSampler, uv).xyz * 2.0 - vec3f(1.0);

  let n = perturbNormal(normalize(normal), worldPos, uv, tangentNormal);
  let l = normalize(uniforms.lightDirection.xyz);
  let v = normalize(uniforms.cameraPosition.xyz - worldPos);
  let h = normalize(v + l);

  let nDotL = max(dot(n, l), 0.0);
  let nDotV = max(dot(n, v), EPSILON);
  let nDotH = max(dot(n, h), 0.0);

  let f0 = mix(vec3f(DIELECTRIC_F0), albedo, metallic);
  let fresnel = fresnelSchlick(max(dot(h, v), 0.0), f0);
  let specular = (distributionGgx(nDotH, roughness) * geometrySmith(nDotV, nDotL, roughness) *
    fresnel) / max(4.0 * nDotV * nDotL, EPSILON);
  let diffuse = (vec3f(1.0) - fresnel) * (1.0 - metallic) * albedo / PI;

  // lightColor is authored as reflectance, not radiance -- see lit_shadow.wgsl for why the
  // BRDF's 1/PI has to be paid back here.
  let radiance = uniforms.lightColor.xyz * PI * nDotL;
  let specularOut = specular * radiance;
  let ambient = albedo * AMBIENT_STRENGTH * occlusion;
  return vec4f(
    ambient + diffuse * radiance + specularOut / (specularOut + vec3f(1.0)) + emissive,
    baseColorSample.a * uniforms.baseColorFactor.a,
  );
}
