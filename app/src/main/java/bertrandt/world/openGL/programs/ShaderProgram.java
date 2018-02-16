/***
 * Excerpted from "OpenGL ES for Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/kbogla for more book information.
***/
package bertrandt.world.openGL.programs;

import android.content.Context;

import bertrandt.world.openGL.util.ShaderHelper;
import bertrandt.world.openGL.util.TextResourceReader;

import static android.opengl.GLES20.glUseProgram;


abstract class ShaderProgram {
    // Uniform constants
    protected static final String U_MATRIX = "u_Matrix";
    protected static final String U_TEXTURE_UNIT = "u_TextureUnit";
    protected static final String U_MV_MATRIX = "u_MVMatrix";
    protected static final String U_NORMAL_MATRIX = "u_NormalMatrix";
    protected static final String U_SHADOW_PROJECTION_MATRIX = "u_ShadowProjMatrix";
    protected static final String U_LIGHT_POS = "u_LightPos";
    protected static final String U_SHADOW_TEXTURE_UNIT = "u_ShadowTexture";
    protected static final String U_MAP_STEP_UNIFORM_X = "u_xPixelOffset";
    protected static final String U_MAP_STEP_UNIFORM_Y = "u_yPixelOffset";

    // Attribute constants
    protected static final String A_POSITION = "a_Position";
    protected static final String A_TEXCOORDINATE = "a_TexCoordinate";
    protected static final String A_NORMAL = "a_Normal";
    protected static final String A_SHADOWPOSITION = "a_ShadowPosition";

    // Shader program
    protected final int program;

    protected ShaderProgram(Context context, int vertexShaderResourceId,
        int fragmentShaderResourceId) {
        // Compile the shaders and link the program.
        program = ShaderHelper.buildProgram(
            TextResourceReader
                .readTextFileFromResource(context, vertexShaderResourceId),
            TextResourceReader
                .readTextFileFromResource(context, fragmentShaderResourceId));
    }        

    public void useProgram() {
        // Set the current OpenGL shader program to this program.
        glUseProgram(program);
    }
}
