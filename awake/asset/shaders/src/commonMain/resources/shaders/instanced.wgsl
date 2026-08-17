// GPU-instanced companion of triangle.wgsl: identical Lambertian shading and vertex layout
// (VertexFormat.PositionNormalColor at locations 0/1/2), but the model matrix arrives per
// INSTANCE through a second, instance-rate vertex buffer at locations 3-6 instead of being
// folded into a per-draw-call `mvp` uniform on the CPU. One draw call covers every instance,
// so the uniform block can only hold what all of them share -- hence `viewProjection` here,
// not `mvp`.
struct Uniforms {
  viewProjection : mat4x4<f32>,
  lightDirection : vec4f,
  lightColor : vec4f,
}
@binding(0) @group(0) var<uniform> uniforms : Uniforms;

struct VertexOutput {
  @builtin(position) position : vec4f,
  @location(0) color : vec3f,
  @location(1) normal : vec3f,
}

// model0..model3 are the four COLUMNS of this instance's model matrix, in the same
// column-major order Mat4.data already stores (which is why the CPU side can memcpy a
// Mat4 straight into the instance buffer with no transpose). Both WebGPU and Vulkan step
// instance-rate attributes once per instance on their own, so no @builtin(instance_index)
// bookkeeping is needed here.
@vertex
fn vertexMain(
  @location(0) inPosition : vec3f,
  @location(1) inNormal : vec3f,
  @location(2) inColor : vec3f,
  @location(3) model0 : vec4f,
  @location(4) model1 : vec4f,
  @location(5) model2 : vec4f,
  @location(6) model3 : vec4f,
) -> VertexOutput {
  let model = mat4x4<f32>(model0, model1, model2, model3);
  var output : VertexOutput;
  output.position = uniforms.viewProjection * model * vec4f(inPosition, 1.0);
  output.color = inColor;
  output.normal = inNormal;
  return output;
}

// Object-space light, same simplification (and same ceiling) as triangle.wgsl's -- see its
// fragment-stage comment.
const AMBIENT_STRENGTH : f32 = 0.35;

@fragment
fn fragmentMain(@location(0) color : vec3f, @location(1) normal : vec3f) -> @location(0) vec4f {
  let n = normalize(normal);
  let l = normalize(uniforms.lightDirection.xyz);
  let diffuse = max(dot(n, l), 0.0);
  let shade = AMBIENT_STRENGTH + (1.0 - AMBIENT_STRENGTH) * diffuse;
  return vec4f(color * shade * uniforms.lightColor.xyz, 1.0);
}
