#version 150
uniform sampler2D DiffuseSampler;
in vec2 texCoord;
out vec4 fragColor;
void main() {
    // 最简单的 passthrough：把上一 pass 的输出原样写入下一个 fbo
    fragColor = vec4(texture(DiffuseSampler, texCoord).rgb, 1.0);
}
