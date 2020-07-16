#version 330 core
layout (location = 0) in vec3 position;
layout (location = 1) in vec3 normal;
layout (location = 2) in vec4 color;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

out vec3 Normal;
out vec3 FragPos;
out vec4 Color;

void main()
{
	gl_Position = (projection * (view * model)) * vec4(position, 1.0);
	Normal = normal;
	Color = color;
	FragPos = vec3(model * vec4(position, 1.0));
}
