package bertrandt.world.openGL.programs;

import android.content.Context;

import bertrandt.world.R;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_CUBE_MAP;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;

/**
 * Created by buhrmanc on 14.02.2018.
 */

public class SimpleShaderProgram extends ShaderProgram {
    private final int aPositionLocation;
    private final int aTextureCoordinate;
    private final int aNormalCoordinate;
    private final int uMatrixLocation;
    private final int uTextureUnitLocation;

    public SimpleShaderProgram(Context context) {
        super(context, R.raw.simple_vertex_shader,
                R.raw.simple_fragment_shader);

        uMatrixLocation = glGetUniformLocation(program, U_MATRIX);
        uTextureUnitLocation = glGetUniformLocation(program, U_TEXTURE_UNIT);
        aPositionLocation = glGetAttribLocation(program, A_POSITION);
        aNormalCoordinate = glGetAttribLocation(program, A_NORMAL);
        aTextureCoordinate = glGetAttribLocation(program, A_TEXCOORDINATE);
    }

    public void setUniforms(float[] matrix, int textureId) {
        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureId);
        glUniform1i(uTextureUnitLocation, 0);
    }

    public int getPositionAttributeLocation() {
        return aPositionLocation;
    }

    public int getTextureAttributeLocation() {
        return aTextureCoordinate;
    }

    public int getNormalAttributeLocation() {
        return aNormalCoordinate;
    }
}


