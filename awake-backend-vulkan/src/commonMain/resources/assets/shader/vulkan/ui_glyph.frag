#version 450

layout(binding = 1) uniform sampler2D fontAtlas;

layout(location = 0) in vec2 fragUV;
layout(location = 1) in vec4 fragColor;

layout(location = 0) out vec4 outColor;

void main() {
    float glyphAlpha = texture(fontAtlas, fragUV).a;
    outColor = vec4(fragColor.rgb, fragColor.a * glyphAlpha);
}
