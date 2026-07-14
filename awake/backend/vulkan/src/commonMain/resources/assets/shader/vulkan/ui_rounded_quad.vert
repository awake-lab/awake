#version 450

layout(binding = 0) uniform UiUBO {
    vec2 screenSize;
} ubo;

layout(location = 0) in vec2 inPosition;
layout(location = 1) in vec2 inLocalPos;
layout(location = 2) in vec2 inHalfSize;
layout(location = 3) in float inRadius;
layout(location = 4) in vec4 inColor;

layout(location = 0) out vec2 fragLocalPos;
layout(location = 1) out vec2 fragHalfSize;
layout(location = 2) out float fragRadius;
layout(location = 3) out vec4 fragColor;

void main() {
    // Same pixel-space -> NDC mapping as ui_quad.vert (Vulkan NDC is already Y-down, no flip).
    vec2 ndc = (inPosition / ubo.screenSize) * 2.0 - 1.0;
    gl_Position = vec4(ndc, 0.0, 1.0);
    fragLocalPos = inLocalPos;
    fragHalfSize = inHalfSize;
    fragRadius = inRadius;
    fragColor = inColor;
}
