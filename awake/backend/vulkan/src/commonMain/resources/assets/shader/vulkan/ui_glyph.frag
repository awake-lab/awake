#version 450

layout(binding = 0) uniform UiUBO {
    vec2 screenSize;
    vec2 fontInfo;
} ubo;

layout(binding = 1) uniform sampler2D fontAtlas;

layout(location = 0) in vec2 fragUV;
layout(location = 1) in vec4 fragColor;

layout(location = 0) out vec4 outColor;

float median3(vec3 value) {
    return max(min(value.r, value.g), min(max(value.r, value.g), value.b));
}

// This renderer blends in GAMMA space, not linear: SwapchainManager deliberately picks a
// _UNORM surface format because every authored color is already sRGB-encoded bytes (see that
// class's format-selection doc comment). Coverage-alpha text blended in gamma space renders
// systematically thinner than correct -- the classic "why is my text so thin" artifact, since
// `text*a + bg*(1-a)` on gamma-encoded values under-weights the glyph's partial-coverage edge
// pixels. Fully-linear blending would fix it properly but would mean re-encoding every color
// in the engine, undoing that documented decision. Stem darkening is the standard remedy when
// linear blending isn't available (FreeType and Skia both ship a variant of it): bias coverage
// up by a power curve so the gamma-space blend lands at the perceptually intended weight.
const float GLYPH_GAMMA = 1.45;

float resolveGlyphAlpha() {
    vec4 atlas = texture(fontAtlas, fragUV);
    if (ubo.fontInfo.x < 0.5) {
        return pow(atlas.a, 1.0 / GLYPH_GAMMA);
    }
    vec2 atlasSize = vec2(textureSize(fontAtlas, 0));
    vec2 unitRange = vec2(ubo.fontInfo.y) / atlasSize;
    vec2 screenTexSize = vec2(1.0) / fwidth(fragUV);
    float screenPxRange = max(0.5 * dot(unitRange, screenTexSize), 1.0);
    float signedDistance = median3(atlas.rgb);
    return clamp(screenPxRange * (signedDistance - 0.5) + 0.5, 0.0, 1.0);
}

void main() {
    float glyphAlpha = resolveGlyphAlpha();
    outColor = vec4(fragColor.rgb, fragColor.a * glyphAlpha);
}
