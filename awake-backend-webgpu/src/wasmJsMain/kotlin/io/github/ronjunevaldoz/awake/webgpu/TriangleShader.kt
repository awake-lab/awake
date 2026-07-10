package io.github.ronjunevaldoz.awake.webgpu

/**
 * WGSL translation of `awake-demo/shared/.../assets/shader/vulkan/triangle.vert`/`.frag`
 * (Phase 2.5 milestone 2 slice 1, see docs/MVP_PLAN.md) -- vertex-color only, the `uv`/
 * `texSampler` parts are dropped since texture sampling is deferred to slice 2.
 * `ShaderModuleDescriptor.code` takes WGSL source text directly (confirmed via the wgpu4k
 * spike), not SPIR-V bytecode -- no cross-compiler needed for a shader this simple.
 */
// language=wgsl
const val triangleWgslShader = """
struct Uniforms {
  mvp : mat4x4<f32>,
}
@binding(0) @group(0) var<uniform> uniforms : Uniforms;

struct VertexOutput {
  @builtin(position) position : vec4f,
  @location(0) color : vec3f,
}

@vertex
fn vertexMain(
  @location(0) inPosition : vec3f,
  @location(1) inColor : vec3f,
) -> VertexOutput {
  var output : VertexOutput;
  output.position = uniforms.mvp * vec4f(inPosition, 1.0);
  output.color = inColor;
  return output;
}

@fragment
fn fragmentMain(@location(0) color : vec3f) -> @location(0) vec4f {
  return vec4f(color, 1.0);
}
"""
