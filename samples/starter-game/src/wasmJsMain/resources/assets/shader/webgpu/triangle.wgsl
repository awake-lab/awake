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
