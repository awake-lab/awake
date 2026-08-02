struct Uniforms {
    screenSize: vec2<f32>
};
@group(0) @binding(0) var<uniform> uniforms: Uniforms;

struct VertexIn {
    @location(0) pos: vec2<f32>,
    @location(1) localPos: vec2<f32>,
    @location(2) halfSize: vec2<f32>,
    @location(3) radius: f32,
    @location(4) color: vec4<f32>,
    // scale(xy) + pivot(zw) -- see ui_quad.wgsl's identical field. Only `pos` is transformed;
    // see the matching comment in ui_rounded_quad.vert for why leaving localPos/halfSize/
    // radius untouched still produces a proportionally-scaled rounded rect.
    @location(5) transform: vec4<f32>
};

struct VertexOut {
    @builtin(position) position: vec4<f32>,
    @location(0) localPos: vec2<f32>,
    @location(1) halfSize: vec2<f32>,
    @location(2) radius: f32,
    @location(3) color: vec4<f32>
};

@vertex
fn vertexMain(in: VertexIn) -> VertexOut {
    let scale = in.transform.xy;
    let pivot = in.transform.zw;
    let scaledPos = pivot + (in.pos - pivot) * scale;
    // Same pixel-space -> NDC mapping as ui_quad.wgsl (pixel-space is Y-down, NDC is Y-up).
    var ndc = (scaledPos / uniforms.screenSize) * 2.0 - 1.0;
    ndc.y = -ndc.y;
    var out: VertexOut;
    out.position = vec4<f32>(ndc, 0.0, 1.0);
    out.localPos = in.localPos;
    out.halfSize = in.halfSize;
    out.radius = in.radius;
    out.color = in.color;
    return out;
}

// Standard rounded-box signed distance field (Inigo Quilez) -- mirrors Vulkan's
// ui_rounded_quad.frag roundedBoxSdf: distance from p (pixels, relative to the quad's own
// center) to the rounded rect's edge. Negative inside, positive outside; a ~1px smoothstep
// band around 0 gives free antialiasing on the corners without a separate MSAA pass.
fn roundedBoxSdf(p: vec2<f32>, halfSize: vec2<f32>, radius: f32) -> f32 {
    let q = abs(p) - halfSize + radius;
    return length(max(q, vec2<f32>(0.0, 0.0))) + min(max(q.x, q.y), 0.0) - radius;
}

@fragment
fn fragmentMain(in: VertexOut) -> @location(0) vec4<f32> {
    let dist = roundedBoxSdf(in.localPos, in.halfSize, in.radius);
    let alpha = 1.0 - smoothstep(-1.0, 1.0, dist);
    return vec4<f32>(in.color.rgb, in.color.a * alpha);
}
