// ╔══════════════════════════════════════════════════════════════════════════════╗
// ║      aurora.fsh —— 幻影骑士的极光后处理特效                                 ║
// ║                                                                              ║
// ║  本版相对初版的改动：                                                        ║
// ║   1. 锚点固定为 boss 召唤点（Java 端控制），shader 内不再追踪移动实体        ║
// ║   2. 加入"自身运动"三件套：                                                  ║
// ║      a) 极光条带绕锚点缓慢自转（rotation = AuroraTime * 0.06）              ║
// ║      b) fbm 流向多方向叠加，模拟极光丝带漂移                                ║
// ║      c) 整体强度做呼吸式脉动（breath = sin(t) 平滑）                         ║
// ║   3. 加入纵向条带：让 fbm 在垂直方向也产生明暗变化，形成"极光帘"质感         ║
// ║                                                                              ║
// ║  思路：                                  ║
// ║   1. 用 DepthSampler + InverseTransformMatrix 反推每个像素对应的世界坐标    ║
// ║   2. 计算"该像素世界点"到 AnchorPosition 的距离                              ║
// ║   3. 距离 < AuroraRadius 时按 fbm 噪声生成蓝色极光纹理                       ║
// ║   4. 用 screen-blend 把极光颜色叠加到原画面上                                ║
// ╚══════════════════════════════════════════════════════════════════════════════╝
#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;
uniform mat4 InverseTransformMatrix;
uniform mat4 InverseModelViewMatrix;
uniform vec3 CameraPosition;
uniform vec3 AnchorPosition;
uniform float AuroraRadius;
uniform float AuroraIntensity;
uniform float AuroraTime;
uniform vec3 AuroraColor;

in vec2 texCoord;
out vec4 fragColor;

// ─────────────────────────────────────────────────────────────────────────────
// 屏幕坐标 (uv, depth) → 世界坐标
// ─────────────────────────────────────────────────────────────────────────────
vec3 worldPos(vec2 uv, float depth) {
    vec3 ndc = vec3(uv, depth) * 2.0 - 1.0;
    vec4 clip = InverseTransformMatrix * vec4(ndc, 1.0);
    vec3 viewPos = clip.xyz / clip.w;
    return (InverseModelViewMatrix * vec4(viewPos, 1.0)).xyz + CameraPosition;
}

// ─────────────────────────────────────────────────────────────────────────────
// hash → value noise → fbm
// ─────────────────────────────────────────────────────────────────────────────
float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);
    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));
    return mix(mix(a, b, u.x), mix(c, d, u.x), u.y);
}

float fbm(vec2 p) {
    float sum = 0.0;
    float amp = 0.5;
    for (int i = 0; i < 5; i++) {
        sum += amp * noise(p);
        p *= 2.03;
        amp *= 0.5;
    }
    return sum;
}

// 二维旋转
vec2 rot(vec2 p, float a) {
    float s = sin(a), c = cos(a);
    return mat2(c, -s, s, c) * p;
}

void main() {
    vec3 original = texture(DiffuseSampler, texCoord).rgb;

    if (AuroraIntensity <= 0.001) {
        fragColor = vec4(original, 1.0);
        return;
    }

    float depth = texture(DepthSampler, texCoord).r;
    if (depth >= 0.9999) {
        // 天空像素直接透传，避免给天空染色
        fragColor = vec4(original, 1.0);
        return;
    }
    vec3 world = worldPos(texCoord, depth);

    // 局部坐标：以锚点为原点
    vec3 local = world - AnchorPosition;
    float horizDist = length(local.xz);
    float verticalDist = abs(local.y);

    if (horizDist >= AuroraRadius) {
        fragColor = vec4(original, 1.0);
        return;
    }

    // ─── 自身运动 1：整张极光纹理绕锚点缓慢自转 ────────────────────────────
    // 每秒约 3.4°，正好让人眼能感觉到"在动"但又不晕
    vec2 rotated = rot(local.xz, AuroraTime * 0.06);

    // ─── 自身运动 2：多方向 fbm 流动 ────────────────────────────────────────
    // band1 沿主方向飘
    vec2 q1 = rotated * 0.06 + vec2(AuroraTime * 0.10, AuroraTime * 0.05);
    float band1 = fbm(q1);
    // band2 反方向叠加，让条带感不死板
    vec2 q2 = rotated * 0.15 - vec2(AuroraTime * 0.07, AuroraTime * 0.03);
    float band2 = fbm(q2);
    // band3 引入"垂直分量"——让极光看起来有竖向条带帘的质感
    vec2 q3 = vec2(rotated.x * 0.08, local.y * 0.12 + AuroraTime * 0.20);
    float band3 = fbm(q3);

    float aurora = pow(band1 * band2 * 2.4, 1.4) * mix(0.7, 1.3, band3);
    aurora = clamp(aurora, 0.0, 1.0);

    // ─── 自身运动 3：呼吸式强度脉动 ─────────────────────────────────────────
    // 周期约 6 秒，幅度 ±15%
    float breath = 0.85 + 0.15 * sin(AuroraTime * 1.05);

    // 距离衰减
    float radialFalloff = smoothstep(AuroraRadius, AuroraRadius * 0.35, horizDist);
    float verticalFalloff = 1.0 - smoothstep(8.0, 32.0, verticalDist);

    float strength = aurora * radialFalloff * verticalFalloff * AuroraIntensity * breath;

    // 极光颜色：基础淡蓝 + 高亮处偏白
    vec3 base = AuroraColor;
    vec3 highlight = mix(base, vec3(0.85, 1.0, 1.0), 0.6);
    vec3 auroraColor = mix(base, highlight, smoothstep(0.5, 1.0, aurora));

    // screen-blend
    vec3 blended = 1.0 - (1.0 - original) * (1.0 - auroraColor * strength);

    fragColor = vec4(blended, 1.0);
}
