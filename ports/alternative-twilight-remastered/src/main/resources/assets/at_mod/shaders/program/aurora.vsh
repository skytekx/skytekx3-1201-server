#version 150

in vec4 Position;

uniform mat4 ProjMat;
uniform vec2 OutSize;

out vec2 texCoord;

void main() {
    // PostChain 给的是一个全屏 quad，Position.xy 范围是 [0,OutSize]
    // 这里映射到 NDC，并把 uv 归一化到 [0,1]
    vec4 outPos = ProjMat * vec4(Position.xy, 0.0, 1.0);
    gl_Position = vec4(outPos.xy, 0.2, 1.0);
    texCoord = Position.xy / OutSize;
}
