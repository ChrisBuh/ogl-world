package bertrandt.world.openGL.objects;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import bertrandt.world.openGL.programs.DepthShaderProgram;
import bertrandt.world.openGL.programs.SceneShaderProgram;
import bertrandt.world.openGL.programs.SimpleShaderProgram;
import bertrandt.world.openGL.util.ImportObj;
import bertrandt.world.openGL.util.VertexArray;

/**
 * Created by buhrmanc on 05.02.2018.
 */

public class ObjFile {
    private static final int POSITION_COMPONENT_COUNT = 3;
    private static final int NORMAL_COMPONENT_COUNT = 3;
    private static final int TEXEL_COMPONENT_COUNT = 2;

    private VertexArray mVertexArrayPos;
    private VertexArray mVertexArrayTex;
    private VertexArray mVertexArrayNorm;

    private ImportObj mImportObj;

    private boolean initialised;

    private boolean dynamic = false;

    private final float[] mCubeRotation = new float[16];

    public ObjFile(final Context context, final String fileName) {

        mImportObj = new ImportObj(context, fileName);
        initialised = true;
        setArrays();

    }

    public ObjFile(final Context context, final String fileName,
                   final float moveX, final float moveY, final float moveZ, final float scale) {

        mImportObj = new ImportObj(context, fileName, moveX, moveY, moveZ, scale);
        initialised = true;
        setArrays();

    }

    private void setArrays(){
        mVertexArrayPos = new VertexArray(mImportObj.getVertices());
        mVertexArrayNorm = new VertexArray(mImportObj.getNormals());
        mVertexArrayTex = new VertexArray(mImportObj.getTexels());
    }



    private void rotate(float mRotationX, float mRotationY){
        //Cube rotation with touch events
        float[] cubeRotationX = new float[16];
        float[] cubeRotationY = new float[16];

        Matrix.setRotateM(cubeRotationX, 0, mRotationX, 0, 1.0f, 0);
        Matrix.setRotateM(cubeRotationY, 0, mRotationY, 1.0f, 0, 0);

        Matrix.multiplyMM(mCubeRotation, 0, cubeRotationX, 0, cubeRotationY, 0);
    }

    public float[] getCubeRotation() {
        return mCubeRotation;
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
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mImportObj.getVertices().length/3);
    }

    public boolean getInitialised() {
        return initialised;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }
}

