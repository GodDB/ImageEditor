// gl 3.x에서 layout과 같은 역할
attribute vec3 v_Position;
attribute vec2 v_Texture_Position;

uniform mat4 u_Trans;
uniform mat4 u_Rotate;
uniform mat4 u_Scale;
//uniform mat4 u_Camera;
//uniform mat4 u_Perspective;

// gl 3.x에서 out과 같은 역할
varying vec2 f_Texture_Position;

void main()
{
    gl_Position = u_Trans * u_Rotate * u_Scale * vec4(v_Position, 1.0);
    f_Texture_Position = v_Texture_Position;
}
