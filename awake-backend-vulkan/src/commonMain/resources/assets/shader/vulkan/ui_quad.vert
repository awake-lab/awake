#version 450

layout(binding = 0) uniform UiUBO {
    vec2 screenSize;
} ubo;

layout(location = 0) in vec2 inPosition;
layout(location = 1) in vec4 inColor;

layout(location = 0) out vec4 fragColor;

void main() {
    // Unlike OpenGL/WebGPU, Vulkan's NDC is already Y-down (Y=-1 top, Y=+1 bottom), matching
    // this pixel-space input -- no flip needed here. Negating this axis (a leftover
    // OpenGL-convention assumption) rendered every UI widget/glyph upside-down and at the
    // mirrored vertical position on screen.
    vec2 ndc = (inPosition / ubo.screenSize) * 2.0 - 1.0;
    gl_Position = vec4(ndc, 0.0, 1.0);
    fragColor = inColor;
}
