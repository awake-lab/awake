#version 450

layout(binding = 1) uniform sampler2D previewTexture;

layout(location = 0) in vec2 fragUV;

layout(location = 0) out vec4 outColor;

void main() {
    // Unlike ui_glyph.frag, the sampled image's RGB IS the content (a render-target's
    // color attachment), not a coverage mask -- passed through untouched.
    outColor = texture(previewTexture, fragUV);
}
