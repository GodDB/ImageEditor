precision mediump float;

uniform sampler2D u_TextureUnit;
uniform int u_IsTexture;

// gl 3.x에서 in과 같은 역할
varying vec2 f_Texture_Position;

void main()
{

   if(u_IsTexture == 0) {
       vec4 texColor = texture2D(u_TextureUnit, f_Texture_Position);
       // 알파가 0.1보다 작다면 버린다.
       if(texColor.a < 0.1) discard;
       gl_FragColor = texColor;
    } else {
       gl_FragColor = vec4(1f, 0f, 0f, 1f);
   }
}
