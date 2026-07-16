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

float resolveGlyphAlpha() {
    vec4 atlas = texture(fontAtlas, fragUV);
    if (ubo.fontInfo.x < 0.5) {
        return atlas.a;
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
