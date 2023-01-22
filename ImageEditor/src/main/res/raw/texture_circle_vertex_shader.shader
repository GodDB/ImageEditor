// gl 3.x에서 layout과 같은 역할
attribute vec3 v_Position;
attribute vec2 v_Texture_Position;

uniform mat4 u_Model;
uniform mat4 u_Projection;

// gl 3.x에서 out과 같은 역할
varying vec2 f_Texture_Position;

void main()
{
    gl_Position = u_Projection * u_Model * vec4(v_Position, 1.0);
    f_Texture_Position = v_Texture_Position;
}
