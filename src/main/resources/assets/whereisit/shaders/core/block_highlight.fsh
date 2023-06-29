#version 150

uniform vec4 ColorModulator;
uniform vec2 ScreenSize;
uniform float GameTime;

out vec4 fragColor;

vec3 hsv2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

// https://github.com/hughsk/glsl-hsv2rgb/blob/master/index.glsl
void main() {
    vec2 coords = gl_FragCoord.xy / ScreenSize;
    float prog = mod((2 * coords.x) + (0.5 * coords.y) + (-250 * GameTime), 1.0);
    vec3 col = hsv2rgb(vec3(prog, 1.0, 1.0));
    fragColor = vec4(col, 1.0) * ColorModulator;
}
