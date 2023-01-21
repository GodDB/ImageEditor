precision mediump float;

uniform sampler2D u_TextureUnit;
uniform int u_IsTexture;

// gl 3.x에서 in과 같은 역할
varying vec2 f_Texture_Position;

void main()
{

   if(u_IsTexture == 0) {
       gl_FragColor = texture2D(u_TextureUnit, f_Texture_Position);
    } else {
       gl_FragColor = vec4(1f, 0f, 0f, 1f);
   }
}
