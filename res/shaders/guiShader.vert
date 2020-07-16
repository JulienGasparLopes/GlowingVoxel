#version 330 core

layout (location = 0) in vec3 position;
layout (location = 2) in vec2 textureCoord;

uniform mat4 model;
uniform mat4 projection;

out vec2 TexCoord;

void main()
{
    gl_Position = projection * model * vec4(position, 1.0);
    TexCoord = textureCoord;
}