struct Uniforms {
    screenSize: vec2<f32>,
    fontInfo: vec2<f32>
};
@group(0) @binding(0) var<uniform> uniforms: Uniforms;
@group(0) @binding(1) var fontAtlas: texture_2d<f32>;
@group(0) @binding(2) var fontSampler: sampler;

struct VertexIn {
    @location(0) pos: vec2<f32>,
    @location(1) uv: vec2<f32>,
    @location(2) color: vec4<f32>,
    // scale(xy) + pivot(zw) -- see ui_quad.wgsl's identical field.
    @location(3) transform: vec4<f32>
};

struct VertexOut {
    @builtin(position) position: vec4<f32>,
    @location(0) uv: vec2<f32>,
    @location(1) color: vec4<f32>
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
    out.uv = in.uv;
    out.color = in.color;
    return out;
}

// This renderer blends in GAMMA space, not linear: the canvas is configured BGRA8Unorm and
// every authored color is already sRGB-encoded (mirrors Vulkan's SwapchainManager format
// choice -- see its doc comment). Coverage-alpha text blended in gamma space renders
// systematically thinner than correct, since `text*a + bg*(1-a)` on gamma-encoded values
// under-weights the glyph's partial-coverage edge pixels. Stem darkening is the standard
// remedy when linear blending isn't available (FreeType/Skia both ship a variant): bias
// coverage up by a power curve so the gamma-space blend lands at the intended weight. Kept
// numerically identical to vulkan/ui_glyph.frag's GLYPH_GAMMA so both backends match.
const GLYPH_GAMMA: f32 = 1.45;

@fragment
fn fragmentMain(in: VertexOut) -> @location(0) vec4<f32> {
    let atlas = textureSample(fontAtlas, fontSampler, in.uv);
    var glyphAlpha: f32;
    if (uniforms.fontInfo.x < 0.5) {
        glyphAlpha = pow(atlas.a, 1.0 / GLYPH_GAMMA);
    } else {
        let atlasSize = vec2<f32>(textureDimensions(fontAtlas));
        let unitRange = vec2<f32>(uniforms.fontInfo.y) / atlasSize;
        let screenTexSize = vec2<f32>(1.0, 1.0) / fwidth(in.uv);
        let screenPxRange = max(0.5 * dot(unitRange, screenTexSize), 1.0);
        let signedDistance = max(min(atlas.r, atlas.g), min(max(atlas.r, atlas.g), atlas.b));
        // Stem darkening applies to the distance-field path too -- it compensates for blending
        // in gamma space, a property of the framebuffer rather than of how coverage was
        // derived. Kept identical to vulkan/ui_glyph.frag so both backends match.
        let coverage = clamp(screenPxRange * (signedDistance - 0.5) + 0.5, 0.0, 1.0);
        glyphAlpha = pow(coverage, 1.0 / GLYPH_GAMMA);
    }
    return vec4<f32>(in.color.rgb, in.color.a * glyphAlpha);
}
