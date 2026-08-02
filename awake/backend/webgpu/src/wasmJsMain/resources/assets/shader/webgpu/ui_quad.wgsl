struct Uniforms {
    screenSize: vec2<f32>
};
@group(0) @binding(0) var<uniform> uniforms: Uniforms;

struct VertexIn {
    @location(0) pos: vec2<f32>,
    @location(1) color: vec4<f32>,
    // scale(xy) + pivot(zw) -- graphicsLayer scale-only transform (see UiPrimitiveTransform),
    // identity (1,1,0,0) for every primitive with no active graphicsLayer scale effect.
    // Applied BEFORE the NDC transform below, in pixel space.
    @location(2) transform: vec4<f32>
};

struct VertexOut {
    @builtin(position) position: vec4<f32>,
    @location(0) color: vec4<f32>
};

@vertex
fn vertexMain(in: VertexIn) -> VertexOut {
    let scale = in.transform.xy;
    let pivot = in.transform.zw;
    let scaledPos = pivot + (in.pos - pivot) * scale;
    var ndc = (scaledPos / uniforms.screenSize) * 2.0 - 1.0;
    ndc.y = -ndc.y; // pixel-space is Y-down, NDC is Y-up
    var out: VertexOut;
    out.position = vec4<f32>(ndc, 0.0, 1.0);
    out.color = in.color;
    return out;
}

@fragment
fn fragmentMain(in: VertexOut) -> @location(0) vec4<f32> {
    return in.color;
}
