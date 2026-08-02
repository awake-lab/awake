#version 450

layout(binding = 0) uniform UiUBO {
    vec2 screenSize;
} ubo;

layout(location = 0) in vec2 inPosition;
layout(location = 1) in vec4 inColor;
// scale(xy) + pivot(zw) -- graphicsLayer scale-only transform (see UiPrimitiveTransform),
// identity (1,1,0,0) for every primitive with no active graphicsLayer scale effect. Applied
// BEFORE the NDC transform below, in pixel space, so it composes with screenSize the same way
// for every primitive regardless of scale.
layout(location = 2) in vec4 inTransform;

layout(location = 0) out vec4 fragColor;

void main() {
    vec2 scale = inTransform.xy;
    vec2 pivot = inTransform.zw;
    vec2 scaledPosition = pivot + (inPosition - pivot) * scale;
    // Unlike OpenGL/WebGPU, Vulkan's NDC is already Y-down (Y=-1 top, Y=+1 bottom), matching
    // this pixel-space input -- no flip needed here. Negating this axis (a leftover
    // OpenGL-convention assumption) rendered every UI widget/glyph upside-down and at the
    // mirrored vertical position on screen.
    vec2 ndc = (scaledPosition / ubo.screenSize) * 2.0 - 1.0;
    gl_Position = vec4(ndc, 0.0, 1.0);
    fragColor = inColor;
}
