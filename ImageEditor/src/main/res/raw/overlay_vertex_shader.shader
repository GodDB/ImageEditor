// gl 3.x에서 layout과 같은 역할
attribute vec3 v_Position;

uniform mat4 u_Model;
//uniform mat4 u_Camera;
//uniform mat4 u_Perspective;

void main()
{
    gl_Position = u_Model * vec4(v_Position, 1.0);
}
