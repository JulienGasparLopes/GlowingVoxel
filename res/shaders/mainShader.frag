#version 330 core

out vec4 FragColor;

in vec3 Normal;
in vec3 FragPos;
in vec4 Color;

void main()
{
	vec3 lightDir = normalize(vec3(-400, 800, -400) - FragPos);
	float diff  = max(dot(lightDir, Normal)*2/3 + 0.333, 0.33);
	vec3 diffuse = diff * vec3(1, 1, 1);
	FragColor = vec4(Color.rgb * diffuse, Color.a);
}
