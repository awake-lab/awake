#version 450

layout(location = 0) in vec2 fragLocalPos;
layout(location = 1) in vec2 fragHalfSize;
layout(location = 2) in float fragRadius;
layout(location = 3) in vec4 fragColor;

layout(location = 0) out vec4 outColor;

// Standard rounded-box signed distance field (Inigo Quilez) -- distance from fragLocalPos
// (pixels, relative to the quad's own center) to the rounded rect's edge. Negative inside,
// positive outside; a ~1px smoothstep band around 0 gives free antialiasing on the corners
// without a separate MSAA pass.
float roundedBoxSdf(vec2 p, vec2 halfSize, float radius) {
    vec2 q = abs(p) - halfSize + radius;
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - radius;
}

void main() {
    float dist = roundedBoxSdf(fragLocalPos, fragHalfSize, fragRadius);
    float alpha = 1.0 - smoothstep(-1.0, 1.0, dist);
    outColor = vec4(fragColor.rgb, fragColor.a * alpha);
}
