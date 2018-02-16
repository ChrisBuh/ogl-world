package bertrandt.world.openGL.objects;

import android.opengl.GLES20;

import java.nio.ByteBuffer;

import bertrandt.world.openGL.programs.DepthShaderProgram;
import bertrandt.world.openGL.programs.SceneShaderProgram;
import bertrandt.world.openGL.programs.SimpleShaderProgram;
import bertrandt.world.openGL.util.VertexArray;

import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_BYTE;
import static android.opengl.GLES20.glDrawElements;

/**
 * Created by buhrmanc on 14.02.2018.
 */

public class Plane {
    private static final int POSITION_COMPONENT_COUNT = 3;
    private static final int NORMAL_COMPONENT_COUNT = 3;
    private static final int TEXEL_COMPONENT_COUNT = 2;

    private final VertexArray mVertexArrayPos;
    private final VertexArray mVertexArrayTex;
    private final VertexArray mVertexArrayNorm;

    private boolean dynamic = false;

    public Plane(){
        mVertexArrayPos = new VertexArray(new float[] {
                // X, Y, Z,
                -150.0f, 0.0f, -150.0f,
                -150.0f, 0.0f, 150.0f,
                150.0f, 0.0f, -150.0f,
                -150.0f, 0.0f, 150.0f,
                150.0f, 0.0f, 150.0f,
                150.0f, 0.0f, -150.0f
        });

        mVertexArrayNorm = new VertexArray(new float[] {
                // nX, nY, nZ
                0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f
        });

        mVertexArrayTex = new VertexArray(new float[] {
                // U, V
                0.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f,
                1.0f, 0.0f
        });
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }

    public void bindData(SceneShaderProgram sceneShaderProgram) {
        mVertexArrayPos.setVertexAttribPointer(0,
                sceneShaderProgram.getPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT, 0);

        mVertexArrayNorm.setVertexAttribPointer(0,
                sceneShaderProgram.getNormalAttributeLocation(),
                NORMAL_COMPONENT_COUNT, 0);

        mVertexArrayTex.setVertexAttribPointer(0,
                sceneShaderProgram.getTextureAttributeLocation(),
                TEXEL_COMPONENT_COUNT, 0);

    }

    public void bindData(DepthShaderProgram depthShaderProgram) {
        mVertexArrayPos.setVertexAttribPointer(0,
                depthShaderProgram.getShadowPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT, 0);
    }

    public void draw() {
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
    }
}
