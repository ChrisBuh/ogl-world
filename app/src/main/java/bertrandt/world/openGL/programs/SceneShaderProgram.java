package bertrandt.world.openGL.programs;

import android.content.Context;

import bertrandt.world.R;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE1;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniform3f;
import static android.opengl.GLES20.glUniformMatrix4fv;

/**
 * Created by buhrmanc on 16.02.2018.
 */

public class SceneShaderProgram extends ShaderProgram {
    private final int aPositionLocation;
    private final int aTextureCoordinate;
    private final int aNormalCoordinate;
    private final int uMatrixLocation;
    private final int uMVMatrixLocation;
    private final int uNormalMatrixLocation;
    private final int uShadowProjection;
    private final int uLightPosition;
    private final int uShadowTextureUnitLocation;
    private final int uTextureUnitLocation;
    private final int uXMapStepUniform;
    private final int uYMapStepUniform;

    public SceneShaderProgram(Context context) {
        super(context, R.raw.scene_vertex_shader,
                R.raw.scene_fragment_shader);

        uMatrixLocation = glGetUniformLocation(program, U_MATRIX);
        uMVMatrixLocation = glGetUniformLocation(program, U_MV_MATRIX);
        uNormalMatrixLocation = glGetUniformLocation(program, U_NORMAL_MATRIX);
        uShadowProjection = glGetUniformLocation(program, U_SHADOW_PROJECTION_MATRIX);
        uLightPosition = glGetUniformLocation(program, U_LIGHT_POS);
        uShadowTextureUnitLocation = glGetUniformLocation(program, U_SHADOW_TEXTURE_UNIT);
        uTextureUnitLocation = glGetUniformLocation(program, U_TEXTURE_UNIT);
        uXMapStepUniform = glGetUniformLocation(program, U_MAP_STEP_UNIFORM_X);
        uYMapStepUniform = glGetUniformLocation(program, U_MAP_STEP_UNIFORM_Y);
        aPositionLocation = glGetAttribLocation(program, A_POSITION);
        aNormalCoordinate = glGetAttribLocation(program, A_NORMAL);
        aTextureCoordinate = glGetAttribLocation(program, A_TEXCOORDINATE);
    }

    public void setUniforms(float[] mMVPmatrix, int[] textureId,
                            int mShadowMapWidth, int mShadowMapHeight,
                            float[] mMVMatrix, float[] mNormalMatrix,
                            float[] mLightPosInEyeSpace, float[] mLightMvpMatrix) {

        glUniform1f(uXMapStepUniform, (float) (1.0 / mShadowMapWidth));
        glUniform1f(uYMapStepUniform, (float) (1.0 / mShadowMapHeight));
        glUniformMatrix4fv(uMVMatrixLocation, 1, false, mMVMatrix, 0);
        glUniformMatrix4fv(uNormalMatrixLocation, 1, false, mNormalMatrix, 0);
        glUniformMatrix4fv(uMatrixLocation, 1, false, mMVPmatrix, 0);
        glUniform3f(uLightPosition, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);
        glUniformMatrix4fv(uShadowTextureUnitLocation, 1, false, mLightMvpMatrix, 0);

        //ShadowTexture
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureId[0]);
        glUniform1i(uShadowTextureUnitLocation, 0);

        //ObjectTexture
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, textureId[1]);
        glUniform1i(uTextureUnitLocation, 1);
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
