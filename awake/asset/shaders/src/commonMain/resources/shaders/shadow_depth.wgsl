// Depth-only pass for the shadow map: same Uniforms layout as lit_shadow.wgsl (so it can bind
// the SAME per-draw-call uniform buffer/descriptor set the main pass already writes, instead
// of a second uniform-buffer scheme) but only ever reads lightMvp. No color attachment in this
// render pass, so fragmentMain writes nothing -- depth is written by the fixed-function
// pipeline alone.
struct Uniforms {
  mvp : mat4x4<f32>,
  lightDirection : vec4f,
  lightColor : vec4f,
  lightMvp : mat4x4<f32>,
}
@binding(0) @group(0) var<uniform> uniforms : Uniforms;

@vertex
fn vertexMain(
  @location(0) inPosition : vec3f,
  @location(1) inNormal : vec3f,
  @location(2) inColor : vec3f,
) -> @builtin(position) vec4f {
  return uniforms.lightMvp * vec4f(inPosition, 1.0);
}

@fragment
fn fragmentMain() {
}
