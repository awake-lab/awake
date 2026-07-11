struct Uniforms {
    screenSize: vec2<f32>
};
@group(0) @binding(0) var<uniform> uniforms: Uniforms;
@group(0) @binding(1) var fontAtlas: texture_2d<f32>;
@group(0) @binding(2) var fontSampler: sampler;

struct VertexIn {
    @location(0) pos: vec2<f32>,
    @location(1) uv: vec2<f32>,
    @location(2) color: vec4<f32>
};

struct VertexOut {
    @builtin(position) position: vec4<f32>,
    @location(0) uv: vec2<f32>,
    @location(1) color: vec4<f32>
};

@vertex
fn vertexMain(in: VertexIn) -> VertexOut {
    var ndc = (in.pos / uniforms.screenSize) * 2.0 - 1.0;
    ndc.y = -ndc.y; // pixel-space is Y-down, NDC is Y-up
    var out: VertexOut;
    out.position = vec4<f32>(ndc, 0.0, 1.0);
    out.uv = in.uv;
    out.color = in.color;
    return out;
}

@fragment
fn fragmentMain(in: VertexOut) -> @location(0) vec4<f32> {
    let glyphAlpha = textureSample(fontAtlas, fontSampler, in.uv).a;
    return vec4<f32>(in.color.rgb, in.color.a * glyphAlpha);
}
