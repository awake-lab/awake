#version 450

layout(binding = 0) uniform UBO {
    vec4 tint;
} ubo;
layout(binding = 1) uniform sampler2D texSampler;

layout(location = 0) in vec3 fragColor;
layout(location = 1) in vec2 fragUV;

layout(location = 0) out vec4 outColor;

void main() {
    vec4 texColor = texture(texSampler, fragUV);
    outColor = vec4(fragColor * ubo.tint.rgb, 1.0) * texColor;
}
