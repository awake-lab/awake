#version 450

layout(binding = 0) uniform UBO {
    vec4 tint;
} ubo;

layout(location = 0) in vec3 fragColor;

layout(location = 0) out vec4 outColor;

void main() {
    outColor = vec4(fragColor * ubo.tint.rgb, 1.0);
}
