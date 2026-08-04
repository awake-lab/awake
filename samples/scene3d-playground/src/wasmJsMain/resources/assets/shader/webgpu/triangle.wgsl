struct Uniforms {
  mvp : mat4x4<f32>,
}
@binding(0) @group(0) var<uniform> uniforms : Uniforms;

struct VertexOutput {
  @builtin(position) position : vec4f,
  @location(0) color : vec3f,
  @location(1) normal : vec3f,
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
  return output;
}

// Object-space light direction (not transformed by the model matrix) -- shading rotates with
// the mesh rather than staying fixed relative to the world. A real ceiling of this simplified
// pass: uniforms only carries the combined MVP today, no separate model matrix a fragment
// could use to transform the normal into world space. Upgrade path: add a model-matrix (or
// normal-matrix) uniform once a demo actually needs light that stays fixed while the mesh spins.
const LIGHT_DIRECTION : vec3f = vec3f(0.4, 0.8, 0.4);
const AMBIENT_STRENGTH : f32 = 0.35;

@fragment
fn fragmentMain(@location(0) color : vec3f, @location(1) normal : vec3f) -> @location(0) vec4f {
  let n = normalize(normal);
  let l = normalize(LIGHT_DIRECTION);
  let diffuse = max(dot(n, l), 0.0);
  let shade = AMBIENT_STRENGTH + (1.0 - AMBIENT_STRENGTH) * diffuse;
  return vec4f(color * shade, 1.0);
}
