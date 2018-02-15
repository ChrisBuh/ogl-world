package bertrandt.world.openGL.main;


import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import bertrandt.world.R;
import bertrandt.world.openGL.objects.Plane;
import bertrandt.world.openGL.objects.ShadowGenerator;
import bertrandt.world.openGL.objects.SkyBox;
import bertrandt.world.openGL.programs.DepthShaderProgram;
import bertrandt.world.openGL.programs.SimpleShaderProgram;
import bertrandt.world.openGL.programs.SkyboxShaderProgram;
import bertrandt.world.openGL.util.MatrixHelper;
import bertrandt.world.openGL.util.TextureHelper;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_CULL_FACE;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_FRAMEBUFFER;
import static android.opengl.GLES20.GL_LEQUAL;
import static android.opengl.GLES20.GL_LESS;
import static android.opengl.GLES20.glBindFramebuffer;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDepthFunc;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glGetFloatv;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.invertM;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.perspectiveM;
import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;
import static android.opengl.Matrix.transposeM;

/**
 * Created by buhrmanc on 12.02.2018.
 */

public class Renderer implements GLSurfaceView.Renderer {

    private Context mContext;

    private SkyBox mSkyBox;
    private SkyboxShaderProgram mSkyBoxProgram;
    private int mSkyBoxTexture;

    private Plane mPlane;
    private SimpleShaderProgram mPlaneProgram;
    private int mPlaneTexture;

    private float xRotation;
    private float yRotation;
    private float mScalingFactor;

    private final float[] modelMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private final float[] viewMatrixForSkybox = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] tempMatrix = new float[16];
    private final float[] modelViewProjectionMatrix = new float[16];
    private final float[] modelViewMatrix = new float[16];

    //Light
    private final float[] mLightProjectionMatrix = new float[16];
    private final float[] mLightMvpMatrix_staticShapes = new float[16];
    private final float[] mLightMvpMatrix_dynamicShapes = new float[16];
    private final float[] mLightViewMatrix = new float[16];
    private final float[] mLightPosInEyeSpace = new float[16];
    private final float[] mLightPosModel = new float[]
            {-5.0f, 9.0f, 0.0f, 1.0f};
    private float[] mActualLightPosition = new float[4];

    private ShadowGenerator mShadowGenerator;
    private DepthShaderProgram mDepthProgram;
    private boolean mHasDepthTextureExtension = false;

    public Renderer(Context context){
        this.mContext = context;
    }

    private void drawSkybox() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        setIdentityM(modelMatrix, 0);
        updateMvpMatrixForSkybox();

        glDepthFunc(GL_LEQUAL); // This avoids problems with the skybox itself getting clipped.
        mSkyBoxProgram.useProgram();
        mSkyBoxProgram.setUniforms(modelViewProjectionMatrix, mSkyBoxTexture);
        mSkyBox.bindData(mSkyBoxProgram);
        mSkyBox.draw();
        glDepthFunc(GL_LESS);
    }

    private void updateMvpMatrixForSkybox() {
        multiplyMM(tempMatrix, 0, viewMatrixForSkybox, 0, modelMatrix, 0);
        multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, tempMatrix, 0);
    }

    public void handleTouchDrag(float deltaX, float deltaY) {

        yRotation += deltaY / 16f;

        if (yRotation < -90) {
            yRotation = -90;
        } else if (yRotation > 90) {
            yRotation = 90;
        }

        if(xRotation + deltaX / 16f >= 360){
            xRotation = 0;
        }if(xRotation + deltaX / 16f < 0){
            xRotation = 360;
        }
        Log.i("Renderer", "handleTouchDrag: " + xRotation);
        xRotation += deltaX / 16f;

        // Setup view matrix
        updateViewMatrices();
    }

    public void handleTouchZoom(float scalingFactor) {
        mScalingFactor += scalingFactor;

        // Setup view matrix
        updateViewMatrices();
    }

    private void updateViewMatrices() {
        float[] clean = new float[16];
        System.arraycopy(clean, 0, viewMatrix, 0, clean.length);
        setIdentityM(viewMatrix, 0);
        rotateM(viewMatrix, 0, -yRotation, 1f, 0f, 0f);
        rotateM(viewMatrix, 0, -xRotation, 0f, 1f, 0f);
        System.arraycopy(viewMatrix, 0, viewMatrixForSkybox, 0, viewMatrix.length);

        translateM(viewMatrix, 0, -mScalingFactor*viewMatrix[2], -10f + -mScalingFactor*viewMatrix[6], -10f + -mScalingFactor*viewMatrix[10]);
        Log.i("Renderer", "updateViewMatrices: rotation x y" + xRotation + " " + yRotation +
                " Scale " + mScalingFactor);
    }

    private void updateLightViewMatrices() {

    }

    private void drawPlane(){
        //glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        setIdentityM(modelMatrix, 0);
        updateMvpMatrix();

        mPlaneProgram.useProgram();
        mPlaneProgram.setUniforms(modelViewProjectionMatrix, mPlaneTexture);
        mPlane.bindData(mPlaneProgram);
        mPlane.draw();
    }

    private void updateMvpMatrix() {
        multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0);
        invertM(tempMatrix, 0, modelViewMatrix, 0);
        multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        String extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS);
        if (extensions.contains("OES_depth_texture"))
            mHasDepthTextureExtension = true;


        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);


        mSkyBox = new SkyBox();
        mSkyBoxProgram = new SkyboxShaderProgram(mContext);
        mSkyBoxTexture = TextureHelper.loadCubeMap(mContext,
                new int[] { R.drawable.left, R.drawable.right,
                        R.drawable.bottom, R.drawable.top,
                        R.drawable.back, R.drawable.front});

        mPlane = new Plane();
        mPlaneProgram = new SimpleShaderProgram(mContext);
        mPlaneTexture = TextureHelper.loadTexture(mContext, R.drawable.floor);

        mShadowGenerator = new ShadowGenerator();
        mDepthProgram = new DepthShaderProgram(mContext);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        glViewport(0, 0, width, height);

        mShadowGenerator.generateShadow(width,height,mHasDepthTextureExtension);

        MatrixHelper.perspectiveM(projectionMatrix, 45, (float) width
                / (float) height, 1f, 100f);
        updateViewMatrices();

        MatrixHelper.perspectiveM(mLightProjectionMatrix, 45, (float) width
                / (float) height, 1f, 100f);
        updateLightViewMatrices();

    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        glBindFramebuffer(GL_FRAMEBUFFER,0);
        drawSkybox();
        drawPlane();
    }
}
