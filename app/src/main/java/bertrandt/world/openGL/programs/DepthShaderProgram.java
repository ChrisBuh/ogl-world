package bertrandt.world.openGL.programs;

import android.content.Context;

import bertrandt.world.R;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_CUBE_MAP;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;

/**
 * Created by buhrmanc on 15.02.2018.
 */

public class DepthShaderProgram extends ShaderProgram {
    private final int aShadowPositionLocation;
    private final int uMatrixLocation;

    public DepthShaderProgram(Context context) {
        super(context, R.raw.depth_map_vertex_shader,
                R.raw.depth_map_fragment_shader);

        uMatrixLocation = glGetUniformLocation(program, U_MATRIX);
        aShadowPositionLocation = glGetAttribLocation(program, A_SHADOWPOSITION);

    }

    public void setUniforms(float[] matrix, int textureId) {
        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);

    }

    public int getShadowPositionAttributeLocation() {
        return aShadowPositionLocation;
    }

}
