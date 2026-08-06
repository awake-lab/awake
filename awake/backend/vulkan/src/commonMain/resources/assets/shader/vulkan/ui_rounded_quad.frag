#version 450

layout(location = 0) in vec2 fragLocalPos;
layout(location = 1) in vec2 fragHalfSize;
layout(location = 2) in float fragRadius;
layout(location = 3) in float fragSmoothing;
layout(location = 4) in vec4 fragColor;

layout(location = 0) out vec4 outColor;

// Continuous curvature (squircle) + standard rounded-box signed distance field.
float roundedBoxSdf(vec2 p, vec2 halfSize, float radius, float smoothing) {
    vec2 q = abs(p) - halfSize + radius;
    if (smoothing > 0.0 && q.x > 0.0 && q.y > 0.0) {
        float pExp = 2.0 + 4.0 * smoothing;
        return pow(pow(q.x, pExp) + pow(q.y, pExp), 1.0 / pExp) - radius;
    }
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - radius;
}

void main() {
    float dist = roundedBoxSdf(fragLocalPos, fragHalfSize, fragRadius, fragSmoothing);
    float alpha = 1.0 - smoothstep(-1.0, 1.0, dist);
    outColor = vec4(fragColor.rgb, fragColor.a * alpha);
}
