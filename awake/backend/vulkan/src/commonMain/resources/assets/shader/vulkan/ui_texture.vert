#version 450

layout(binding = 0) uniform UiUBO {
    vec2 screenSize;
} ubo;

layout(location = 0) in vec2 inPosition;
layout(location = 1) in vec2 inUV;
layout(location = 2) in vec4 inColor;

layout(location = 0) out vec2 fragUV;

void main() {
    // Same NDC math as ui_glyph.vert -- Vulkan's NDC is already Y-down, matching this
    // pixel-space input, so no flip is needed (or wanted) here.
    vec2 ndc = (inPosition / ubo.screenSize) * 2.0 - 1.0;
    gl_Position = vec4(ndc, 0.0, 1.0);
    fragUV = inUV;
}
