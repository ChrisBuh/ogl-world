package bertrandt.world.openGL.main;


import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import bertrandt.world.R;
import bertrandt.world.openGL.objects.Cube;
import bertrandt.world.openGL.objects.ObjFile;
import bertrandt.world.openGL.objects.Plane;
import bertrandt.world.openGL.objects.ShadowGenerator;
import bertrandt.world.openGL.objects.SkyBox;
import bertrandt.world.openGL.programs.DepthShaderProgram;
import bertrandt.world.openGL.programs.SceneShaderProgram;
import bertrandt.world.openGL.programs.SimpleShaderProgram;
import bertrandt.world.openGL.programs.SkyboxShaderProgram;
import bertrandt.world.openGL.util.MatrixHelper;
import bertrandt.world.openGL.util.PickingRay;
import bertrandt.world.openGL.util.TextureHelper;
import bertrandt.world.openGL.util.Vector3f;

import static android.R.attr.viewportHeight;
import static android.R.attr.viewportWidth;
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
import static android.opengl.GLES20.glDepthMask;
import static android.opengl.GLES20.glDisable;
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

    private SceneShaderProgram mSceneProgram;

    private Plane mPlane;
    private int mPlaneTexture;

    private ObjFile mObjFile;
    private int mObjectTexture;

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
    private final float[] normalMatrix = new float[16];

    //Light
    private final float[] mLightProjectionMatrix = new float[16];
    private final float[] mLightMvpMatrix_staticShapes = new float[16];
    private final float[] mLightMvpMatrix_dynamicShapes = new float[16];
    private final float[] mLightViewMatrix = new float[16];
    private final float[] mLightPosInEyeSpace = new float[16];
    private final float[] mLightPosModel = new float[]
            {-20.0f, 15.0f, 0.0f, 1.0f};
    private float[] mActualLightPosition = new float[4];

    private float mPosX;
    private float mPosY;


    private ShadowGenerator mShadowGenerator;
    private DepthShaderProgram mDepthProgram;
    private boolean mHasDepthTextureExtension = false;

    public Renderer(Context context){
        this.mContext = context;
    }

    private void drawSkybox() {
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


    public void handleTouchDrag(float deltaX, float deltaY, float posX, float posY) {

        this.mPosX = posX;
        this.mPosY = posY;

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
        //float[] clean = new float[16];
        //System.arraycopy(clean, 0, viewMatrix, 0, clean.length);
        setIdentityM(viewMatrix, 0);
        rotateM(viewMatrix, 0, -yRotation, 1f, 0f, 0f);
        rotateM(viewMatrix, 0, -xRotation, 0f, 1f, 0f);
        System.arraycopy(viewMatrix, 0, viewMatrixForSkybox, 0, viewMatrix.length);

        translateM(viewMatrix, 0, -mScalingFactor*viewMatrix[2], -10f + -mScalingFactor*viewMatrix[6], -10f + -mScalingFactor*viewMatrix[10]);
        Log.i("Renderer", "updateViewMatrices: rotation x y" + xRotation + " " + yRotation +
                " Scale " + mScalingFactor);
    }

    private final float[] mCubeRotation = new float[16];

    private void updateLightViewMatrices() {
        // light rotates around Y axis in every 12 seconds
        long elapsedMilliSec = SystemClock.elapsedRealtime();
        long rotationCounter = elapsedMilliSec % 12000L;

        float lightRotationDegree = (360.0f / 12000.0f) * ((int) rotationCounter);

        float[] rotationMatrix = new float[16];

        Matrix.setIdentityM(rotationMatrix, 0);
        Matrix.rotateM(rotationMatrix, 0, lightRotationDegree, 0.0f, 1.0f, 0.0f);

        Matrix.multiplyMV(mActualLightPosition, 0, rotationMatrix, 0, mLightPosModel, 0);

        Matrix.setIdentityM(modelMatrix, 0);
        //Set view matrix from light source position
        Matrix.setLookAtM(mLightViewMatrix, 0,
                //lightX, lightY, lightZ,
                mActualLightPosition[0], mActualLightPosition[1], mActualLightPosition[2],
                //lookX, lookY, lookZ,
                //look in direction -y
                mActualLightPosition[0], -mActualLightPosition[1], mActualLightPosition[2],
                //upX, upY, upZ
                //up vector in the direction of axisY
                -mActualLightPosition[0], 0, -mActualLightPosition[2]);

        //Cube rotation with touch events
        float[] cubeRotationX = new float[16];
        float[] cubeRotationY = new float[16];

        Matrix.setRotateM(cubeRotationX, 0, lightRotationDegree, 0, 1.0f, 0);
        Matrix.setRotateM(cubeRotationY, 0, lightRotationDegree, 1.0f, 0, 0);
        //Matrix.translateM(modelMatrix,0,0,3.0f,-20.0f);

        Matrix.multiplyMM(mCubeRotation, 0, cubeRotationX, 0, cubeRotationY, 0);
    }

    private void updateMvpMatrix() {
        multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0);
        invertM(tempMatrix, 0, modelViewMatrix, 0);
        multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0);
    }

    private void renderShadowMap() {



        updateLightViewMatrices();
        //updateMvpMatrix();

        //Matrix.setIdentityM(modelMatrix, 0);

        GLES20.glCullFace(GLES20.GL_FRONT);

        // bind the generated framebuffer
        //GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mShadowGenerator.getFBOId());
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId[0]);

        GLES20.glViewport(0, 0, mDisplayWidth,
                mDisplayHeight);

        // Clear color and buffers
        //GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        // Start using the shader
        //GLES20.glUseProgram(mDepthMapProgram.getProgram());
        mDepthProgram.useProgram();
        //mDepthProgram.setUniforms(modelViewProjectionMatrix);

        float[] tempResultMatrix = new float[16];

        // Calculate matrices for standing objects

        // View matrix * Model matrix value is stored
        Matrix.multiplyMM(mLightMvpMatrix_staticShapes, 0, mLightViewMatrix, 0, modelMatrix, 0);

        // Model * view * projection matrix stored and copied for use at rendering from camera point of view
        Matrix.multiplyMM(tempResultMatrix, 0, mLightProjectionMatrix, 0, mLightMvpMatrix_staticShapes, 0);
        System.arraycopy(tempResultMatrix, 0, mLightMvpMatrix_staticShapes, 0, 16);

        // Pass in the combined matrix.
        //GLES20.glUniformMatrix4fv(shadow_mvpMatrixUniform, 1, false, mLightMvpMatrix_staticShapes, 0);

        mDepthProgram.setUniforms(mLightMvpMatrix_staticShapes);

        // Render all stationary shapes on scene
        mPlane.bindData(mDepthProgram);
        mPlane.draw();

        // Calculate matrices for moving objects

        //Matrix.translateM(modelMatrix, 0, 0.0f, -2.0f, -2.0f);

        // Rotate the model matrix with current rotation matrix
        Matrix.multiplyMM(tempResultMatrix, 0, modelMatrix, 0, mCubeRotation, 0);

        // View matrix * Model matrix value is stored
        Matrix.multiplyMM(mLightMvpMatrix_dynamicShapes, 0, mLightViewMatrix, 0, tempResultMatrix, 0);

        // Model * view * projection matrix stored and copied for use at rendering from camera point of view
        Matrix.multiplyMM(tempResultMatrix, 0, mLightProjectionMatrix, 0, mLightMvpMatrix_dynamicShapes, 0);
        System.arraycopy(tempResultMatrix, 0, mLightMvpMatrix_dynamicShapes, 0, 16);

        // Pass in the combined matrix.
        //GLES20.glUniformMatrix4fv(shadow_mvpMatrixUniform, 1, false, mLightMvpMatrix_dynamicShapes, 0);
        //mDepthMapProgram.setUniforms(mLightMvpMatrix_dynamicShapes);

        // Render all moving shapes on scene
        //drawObj.render(shadow_positionAttribute, 0, 0, 0, true);
        //drawAgl.render(shadow_positionAttribute, 0, 0, 0, true);
        //mCube.render(shadow_positionAttribute, 0, 0, 0, true);

        mDepthProgram.setUniforms(mLightMvpMatrix_dynamicShapes);

        mObjFile.bindData(mDepthProgram);
        mObjFile.draw();

        // Calculate matrices for moving objects

    }

    private void renderScene() {

        updateMvpMatrix();



        GLES20.glCullFace(GLES20.GL_BACK);

        // bind default framebuffer
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        mSceneProgram.useProgram();

        GLES20.glViewport(0, 0, mDisplayWidth, mDisplayHeight);

        //pass stepsize to map nearby points properly to depth map texture - used in PCF algorithm
        //GLES20.glUniform1f(scene_mapStepXUniform, (float) (1.0 / mShadowMapWidth));
        //GLES20.glUniform1f(scene_mapStepYUniform, (float) (1.0 / mShadowMapHeight));

        float[] tempResultMatrix = new float[16];

        float bias[] = new float[]{
                0.5f, 0.0f, 0.0f, 0.0f,
                0.0f, 0.5f, 0.0f, 0.0f,
                0.0f, 0.0f, 0.5f, 0.0f,
                0.5f, 0.5f, 0.5f, 1.0f};

        float[] depthBiasMVP = new float[16];

        //calculate MV matrix
        Matrix.multiplyMM(tempResultMatrix, 0, viewMatrix, 0, modelMatrix, 0);
        System.arraycopy(tempResultMatrix, 0, modelViewMatrix, 0, 16);

        //calculate Normal Matrix as uniform (invert transpose MV)
        Matrix.invertM(tempResultMatrix, 0, modelViewMatrix, 0);
        Matrix.transposeM(normalMatrix, 0, tempResultMatrix, 0);

        //calculate MVP matrix
        Matrix.multiplyMM(tempResultMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0);
        System.arraycopy(tempResultMatrix, 0, modelViewProjectionMatrix, 0, 16);

        Matrix.multiplyMV(mLightPosInEyeSpace, 0, viewMatrix, 0, mActualLightPosition, 0);
        //pass in light source position
        //GLES20.glUniform3f(scene_lightPosUniform, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);

        if (mHasDepthTextureExtension) {
            Matrix.multiplyMM(depthBiasMVP, 0, bias, 0, mLightMvpMatrix_staticShapes, 0);
            System.arraycopy(depthBiasMVP, 0, mLightMvpMatrix_staticShapes, 0, 16);
        }

        int[] textures = new int[2];
        //textures[0] = mShadowGenerator.getShadowTextureId();
        textures[0] = renderTextureId[0];
        textures[1] = mPlaneTexture;
        mSceneProgram.setUniforms(modelViewProjectionMatrix, textures ,
                mDisplayWidth, mDisplayHeight,
                modelViewMatrix, normalMatrix,
                mLightPosInEyeSpace, mLightMvpMatrix_staticShapes);
        mPlane.bindData(mSceneProgram);
        mPlane.draw();


        // Pass uniforms for moving objects (center cube) which are different from previously used uniforms
        // - MV matrix
        // - MVP matrix
        // - Normal matrix
        // - Light MVP matrix for dynamic objects

        Matrix.translateM(modelMatrix, 0, 0.0f, -2.0f, -2.0f);

        // Rotate the model matrix with current rotation matrix
        Matrix.multiplyMM(tempResultMatrix, 0, modelMatrix, 0, mCubeRotation, 0);

        //calculate MV matrix
        Matrix.multiplyMM(tempResultMatrix, 0, viewMatrix, 0, tempResultMatrix, 0);
        System.arraycopy(tempResultMatrix, 0, modelViewMatrix, 0, 16);

        //calculate Normal Matrix as uniform (invert transpose MV)
        Matrix.invertM(tempResultMatrix, 0, modelViewMatrix, 0);
        Matrix.transposeM(normalMatrix, 0, tempResultMatrix, 0);

        //calculate MVP matrix
        Matrix.multiplyMM(tempResultMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0);
        System.arraycopy(tempResultMatrix, 0, modelViewProjectionMatrix, 0, 16);


        if (mHasDepthTextureExtension) {
            Matrix.multiplyMM(depthBiasMVP, 0, bias, 0, mLightMvpMatrix_dynamicShapes, 0);
            System.arraycopy(depthBiasMVP, 0, mLightMvpMatrix_dynamicShapes, 0, 16);
        }


        textures[1] = mObjectTexture;
        mSceneProgram.setUniforms(modelViewProjectionMatrix, textures ,
                mDisplayWidth, mDisplayHeight,
                modelViewMatrix, normalMatrix,
                mLightPosInEyeSpace, mLightMvpMatrix_dynamicShapes);
        mObjFile.bindData(mSceneProgram);
        mObjFile.draw();

    }




    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        String extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS);
        if (extensions.contains("OES_depth_texture"))
            mHasDepthTextureExtension = true;


        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);


        mSkyBox = new SkyBox();
        mSkyBoxProgram = new SkyboxShaderProgram(mContext);
        mSkyBoxTexture = TextureHelper.loadCubeMap(mContext,
                new int[] { R.drawable.left, R.drawable.right,
                        R.drawable.bottom, R.drawable.top,
                        R.drawable.back, R.drawable.front});

        mSceneProgram = new SceneShaderProgram(mContext);

        mPlane = new Plane();
        mPlaneTexture = TextureHelper.loadTexture(mContext, R.drawable.floor);

        mObjFile = new ObjFile(mContext, "vw.obj", 0.0f, 0f, 0f, 8.0f);
        mObjectTexture = TextureHelper.loadTexture(mContext, R.drawable.vwlogo);


        mShadowGenerator = new ShadowGenerator();
        mDepthProgram = new DepthShaderProgram(mContext);

        //Set view matrix from eye position
        Matrix.setLookAtM(viewMatrix, 0,
                //eyeX, eyeY, eyeZ,
                0, 4, 5,
                //lookX, lookY, lookZ,
                0, 0, 0,
                //upX, upY, upZ
                0, 1, 0);
    }

    int mDisplayWidth;
    int mDisplayHeight;

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        glViewport(0, 0, width, height);
        mDisplayWidth = width;
        mDisplayHeight = height;

        //mShadowGenerator.generateShadow(width,height,mHasDepthTextureExtension);

        generateShadowFBO(width,height,mHasDepthTextureExtension);

        //ProjectionMatrix
        MatrixHelper.perspectiveM(projectionMatrix, 45, (float) width
                / (float) height, 1f, 150f);
        updateViewMatrices();

        MatrixHelper.perspectiveM(mLightProjectionMatrix, 45, (float) width
                / (float) height, 1f, 150f);
        updateLightViewMatrices();

/*
        // this projection matrix is applied at rendering scene
        // in the onDrawFrame() method
        float ratio = (float) mDisplayWidth / mDisplayHeight;
        float bottom = -1.0f;
        float top = 1.0f;
        float near = 1.0f;
        float far = 150.0f;

        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, bottom, top, near, far);
        updateViewMatrices();ö

        //updateViewMatrices();
        // this projection matrix is used at rendering shadow map
        Matrix.frustumM(mLightProjectionMatrix, 0, -1.1f * ratio, 1.1f * ratio, 1.1f * bottom, 1.1f * top, near, far);


        updateLightViewMatrices();*/

    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        renderShadowMap();
        renderScene();
        drawSkybox();
    }

    private int[] fboId;
    private int[] depthTextureId;
    private int[] renderTextureId;

    public void generateShadowFBO(int width, int height, boolean mHasDepthTextureExtension) {

        fboId = new int[1];
        depthTextureId = new int[1];
        renderTextureId = new int[1];

        // create a framebuffer object
        GLES20.glGenFramebuffers(1, fboId, 0);

        // create render buffer and bind 16-bit depth buffer
        GLES20.glGenRenderbuffers(1, depthTextureId, 0);
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, depthTextureId[0]);
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, mDisplayWidth, mDisplayHeight);

        // Try to use a texture depth component
        GLES20.glGenTextures(1, renderTextureId, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, renderTextureId[0]);

        // GL_LINEAR does not make sense for depth texture. However, next tutorial shows usage of GL_LINEAR and PCF. Using GL_NEAREST
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

        // Remove artifact on the edges of the shadowmap
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId[0]);

        if (!mHasDepthTextureExtension) {
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, mDisplayWidth, mDisplayHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);

            // specify texture as color attachment
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, renderTextureId[0], 0);

            // attach the texture to FBO depth attachment point
            // (not supported with gl_texture_2d)
            GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, depthTextureId[0]);
        } else {
            // Use a depth texture
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_DEPTH_COMPONENT, mDisplayWidth, mDisplayHeight, 0, GLES20.GL_DEPTH_COMPONENT, GLES20.GL_UNSIGNED_INT, null);

            // Attach the depth texture to FBO depth attachment point
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_TEXTURE_2D, renderTextureId[0], 0);
        }

        // check FBO status
        int FBOstatus = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (FBOstatus != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            Log.e("FrameBuffer", "GL_FRAMEBUFFER_COMPLETE failed, CANNOT use FBO");
            throw new RuntimeException("GL_FRAMEBUFFER_COMPLETE failed, CANNOT use FBO");
        }
    }
}
