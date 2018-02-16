package bertrandt.world.openGL.main;


import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import bertrandt.world.R;
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
            {-5.0f, 9.0f, 0.0f, 1.0f};
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
        //glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        //glBindFramebuffer(GL_FRAMEBUFFER,0);

        setIdentityM(modelMatrix, 0);
        updateMvpMatrixForSkybox();

        //glDisable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL); // This avoids problems with the skybox itself getting clipped.
        mSkyBoxProgram.useProgram();
        mSkyBoxProgram.setUniforms(modelViewProjectionMatrix, mSkyBoxTexture);
        mSkyBox.bindData(mSkyBoxProgram);
        mSkyBox.draw();
        glDepthFunc(GL_LESS);
        //glEnable(GL_DEPTH_TEST);
    }

    private void updateMvpMatrixForSkybox() {
        multiplyMM(tempMatrix, 0, viewMatrixForSkybox, 0, modelMatrix, 0);
        multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, tempMatrix, 0);
    }

    /**
     * Object picking
     * @param screenX
     * @param screenY
     * @param pickingRay
     */

    private Vector3f view = new Vector3f();

    /**public void picking(float screenX, float screenY, PickingRay pickingRay)
    {
     http://schabby.de/picking-opengl-ray-tracing/

        // look direction
        view.subAndAssign(lookAt, position).normalize();

        // screenX
        screenHoritzontally.crossAndAssign(view, up).normalize();

        // screenY
        screenVertically.crossAndAssign(screenHoritzontally, view).normalize();

        final float radians = (float) (viewAngle*Math.PI / 180f);
        float halfHeight = (float) (Math.tan(radians/2)*nearClippingPlaneDistance);
        float halfScaledAspectRatio = halfHeight*getViewportAspectRatio();

        screenVertically.scale(halfHeight);
        screenHoritzontally.scale(halfScaledAspectRatio);


        pickingRay.getClickPosInWorld().set(position);
        pickingRay.getClickPosInWorld().add(view);

        screenX -= (float)viewportWidth/2f;
        screenY -= (float)viewportHeight/2f;

        // normalize to 1
        screenX /= ((float)viewportWidth/2f);
        screenY /= ((float)viewportHeight/2f);

        pickingRay.getClickPosInWorld().x += screenHoritzontally.x*screenX + screenVertically.x*screenY;
        pickingRay.getClickPosInWorld().y += screenHoritzontally.y*screenX + screenVertically.y*screenY;
        pickingRay.getClickPosInWorld().z += screenHoritzontally.z*screenX + screenVertically.z*screenY;

        pickingRay.getDirection().set(pickingRay.getClickPosInWorld());
        pickingRay.getDirection().sub(position);
    }*/


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

    private void updateLightViewMatrices() {

    }

    private void drawPlane(){
        //glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        int[] textures = new int[2];
        textures[0] = mShadowGenerator.getShadowTextureId();
        textures[1] = mPlaneTexture;

        setIdentityM(modelMatrix, 0);
        updateMvpMatrix();

        //mSceneProgram.useProgram();
        //mSceneProgram.setUniforms(modelViewProjectionMatrix, textures);
        mPlane.bindData(mSceneProgram);
        mPlane.draw();
    }

    private void drawObject(){
        //glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        int[] textures = new int[2];
        textures[0] = mShadowGenerator.getShadowTextureId();
        textures[1] = mObjectTexture;

        setIdentityM(modelMatrix, 0);
        updateMvpMatrix();

        //mSceneProgram.useProgram();
        //mSceneProgram.setUniforms(modelViewProjectionMatrix, textures);
        mObjFile.bindData(mSceneProgram);
        mObjFile.draw();
    }

    private void updateMvpMatrix() {
        multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0);
        invertM(tempMatrix, 0, modelViewMatrix, 0);
        multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0);
    }

    private void renderShadowMap() {

        GLES20.glCullFace(GLES20.GL_FRONT);

        // bind the generated framebuffer
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mShadowGenerator.getFBOId());
        //GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffer.getFrameBufferId());

        GLES20.glViewport(0, 0, mShadowGenerator.getWidth(),
                mShadowGenerator.getHeight());

        // Clear color and buffers
        //GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        // Start using the shader
        //GLES20.glUseProgram(mDepthMapProgram.getProgram());
        mDepthProgram.useProgram();
        mDepthProgram.setUniforms(modelViewProjectionMatrix);

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

        mObjFile.bindData(mDepthProgram);
        mObjFile.draw();

        // Calculate matrices for moving objects
/**
        Matrix.translateM(modelMatrix, 0, 0.0f, -2.0f, -2.0f);

        // Rotate the model matrix with current rotation matrix
        Matrix.multiplyMM(tempResultMatrix, 0, mModelMatrix, 0, mCubeRotation, 0);

        // View matrix * Model matrix value is stored
        Matrix.multiplyMM(mLightMvpMatrix_dynamicShapes, 0, mLightViewMatrix, 0, tempResultMatrix, 0);

        // Model * view * projection matrix stored and copied for use at rendering from camera point of view
        Matrix.multiplyMM(tempResultMatrix, 0, mLightProjectionMatrix, 0, mLightMvpMatrix_dynamicShapes, 0);
        System.arraycopy(tempResultMatrix, 0, mLightMvpMatrix_dynamicShapes, 0, 16);

        // Pass in the combined matrix.
        GLES20.glUniformMatrix4fv(shadow_mvpMatrixUniform, 1, false, mLightMvpMatrix_dynamicShapes, 0);


        // Render all moving shapes on scene
        drawObj.render(shadow_positionAttribute, 0, 0, 0, true);
        //drawAgl.render(shadow_positionAttribute, 0, 0, 0, true);
        //mCube.render(shadow_positionAttribute, 0, 0, 0, true);*/
    }

    private void renderScene() {

        updateMvpMatrix();

        GLES20.glCullFace(GLES20.GL_BACK);

        // bind default framebuffer
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        mSceneProgram.useProgram();

        GLES20.glViewport(0, 0, mShadowGenerator.getWidth(), mShadowGenerator.getHeight());

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

        //pass in MV Matrix as uniform
        //GLES20.glUniformMatrix4fv(scene_mvMatrixUniform, 1, false, mMVMatrix, 0);

        //calculate Normal Matrix as uniform (invert transpose MV)
        Matrix.invertM(tempResultMatrix, 0, modelViewMatrix, 0);
        Matrix.transposeM(normalMatrix, 0, tempResultMatrix, 0);

        //pass in Normal Matrix as uniform
        //GLES20.glUniformMatrix4fv(scene_normalMatrixUniform, 1, false, mNormalMatrix, 0);

        //calculate MVP matrix
        Matrix.multiplyMM(tempResultMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0);
        System.arraycopy(tempResultMatrix, 0, modelViewProjectionMatrix, 0, 16);

        //pass in MVP Matrix as uniform
        //GLES20.glUniformMatrix4fv(scene_mvpMatrixUniform, 1, false, mMVPMatrix, 0);

        Matrix.multiplyMV(mLightPosInEyeSpace, 0, viewMatrix, 0, mActualLightPosition, 0);
        //pass in light source position
        //GLES20.glUniform3f(scene_lightPosUniform, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);

        if (mHasDepthTextureExtension) {
            Matrix.multiplyMM(depthBiasMVP, 0, bias, 0, mLightMvpMatrix_staticShapes, 0);
            System.arraycopy(depthBiasMVP, 0, mLightMvpMatrix_staticShapes, 0, 16);
        }

        //MVP matrix that was used during depth map render
        //GLES20.glUniformMatrix4fv(scene_schadowProjMatrixUniform, 1, false, mLightMvpMatrix_staticShapes, 0);

        //pass in texture where depth map is stored
        //GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        //GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, renderTextureId[0]);
        //GLES20.glUniform1i(scene_textureUniform, 0);
        //mFrameBuffer.bindTexture(scene_textureUniform);

        int[] textures = new int[2];
        textures[0] = mShadowGenerator.getShadowTextureId();
        textures[1] = mPlaneTexture;
        mSceneProgram.setUniforms(modelViewProjectionMatrix, textures ,
                mShadowGenerator.getWidth(), mShadowGenerator.getHeight(),
                modelViewMatrix, normalMatrix,
                mLightPosInEyeSpace, mLightMvpMatrix_staticShapes);
        mPlane.bindData(mSceneProgram);
        mPlane.draw();

        textures[1] = mObjectTexture;
        mSceneProgram.setUniforms(modelViewProjectionMatrix, textures ,
                mShadowGenerator.getWidth(), mShadowGenerator.getHeight(),
                modelViewMatrix, normalMatrix,
                mLightPosInEyeSpace, mLightMvpMatrix_staticShapes);
        mObjFile.bindData(mSceneProgram);
        mObjFile.draw();
/**
        mSmallCube0.render(scene_positionAttribute, scene_normalAttribute, scene_textureAttribute, scene_texture1Uniform, false);
        mSmallCube1.render(scene_positionAttribute, scene_normalAttribute, scene_textureAttribute, scene_texture1Uniform, false);
        mSmallCube2.render(scene_positionAttribute, scene_normalAttribute, scene_textureAttribute, scene_texture1Uniform, false);
        mSmallCube3.render(scene_positionAttribute, scene_normalAttribute, scene_textureAttribute, scene_texture1Uniform, false);
        mPlane.render(scene_positionAttribute, scene_normalAttribute, scene_textureAttribute, scene_texture1Uniform, false);
*/
        // Pass uniforms for moving objects (center cube) which are different from previously used uniforms
        // - MV matrix
        // - MVP matrix
        // - Normal matrix
        // - Light MVP matrix for dynamic objects
/*
        Matrix.translateM(mModelMatrix, 0, 0.0f, -2.0f, -2.0f);

        // Rotate the model matrix with current rotation matrix
        Matrix.multiplyMM(tempResultMatrix, 0, mModelMatrix, 0, mCubeRotation, 0);

        //calculate MV matrix
        Matrix.multiplyMM(tempResultMatrix, 0, mViewMatrix, 0, tempResultMatrix, 0);
        System.arraycopy(tempResultMatrix, 0, mMVMatrix, 0, 16);

        //pass in MV Matrix as uniform
        GLES20.glUniformMatrix4fv(scene_mvMatrixUniform, 1, false, mMVMatrix, 0);

        //calculate Normal Matrix as uniform (invert transpose MV)
        Matrix.invertM(tempResultMatrix, 0, mMVMatrix, 0);
        Matrix.transposeM(mNormalMatrix, 0, tempResultMatrix, 0);

        //pass in Normal Matrix as uniform
        GLES20.glUniformMatrix4fv(scene_normalMatrixUniform, 1, false, mNormalMatrix, 0);

        //calculate MVP matrix
        Matrix.multiplyMM(tempResultMatrix, 0, mProjectionMatrix, 0, mMVMatrix, 0);
        System.arraycopy(tempResultMatrix, 0, mMVPMatrix, 0, 16);

        //pass in MVP Matrix as uniform
        GLES20.glUniformMatrix4fv(scene_mvpMatrixUniform, 1, false, mMVPMatrix, 0);

        if (mHasDepthTextureExtension) {
            Matrix.multiplyMM(depthBiasMVP, 0, bias, 0, mLightMvpMatrix_dynamicShapes, 0);
            System.arraycopy(depthBiasMVP, 0, mLightMvpMatrix_dynamicShapes, 0, 16);
        }

        //MVP matrix that was used during depth map render
        GLES20.glUniformMatrix4fv(scene_schadowProjMatrixUniform, 1, false, mLightMvpMatrix_dynamicShapes, 0);


        drawObj.render(scene_positionAttribute, scene_normalAttribute, scene_textureAttribute, scene_texture1Uniform, false);
        //drawAgl.render(scene_positionAttribute, scene_normalAttribute, scene_textureAttribute, scene_texture1Uniform, false);

   */
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

        mSceneProgram = new SceneShaderProgram(mContext);

        mPlane = new Plane();
        mPlaneTexture = TextureHelper.loadTexture(mContext, R.drawable.floor);

        mObjFile = new ObjFile(mContext, "vw.obj", 0.0f, 1.0f, -2.0f, 5.0f);
        mObjectTexture = TextureHelper.loadTexture(mContext, R.drawable.vwlogo);


        mShadowGenerator = new ShadowGenerator();
        mDepthProgram = new DepthShaderProgram(mContext);

        //Set view matrix from eye position
        Matrix.setLookAtM(viewMatrix, 0,
                //eyeX, eyeY, eyeZ,
                0, 4, -20,
                //lookX, lookY, lookZ,
                0, 0, 0,
                //upX, upY, upZ
                0, 1, 0);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        glViewport(0, 0, width, height);

        mShadowGenerator.generateShadow(width,height,mHasDepthTextureExtension);

        //ProjectionMatrix
        MatrixHelper.perspectiveM(projectionMatrix, 45, (float) width
                / (float) height, 1f, 150f);
        updateViewMatrices();

        MatrixHelper.perspectiveM(mLightProjectionMatrix, 45, (float) width
                / (float) height, 1f, 150f);
        updateLightViewMatrices();

    }

    @Override
    public void onDrawFrame(GL10 gl10) {



        renderShadowMap();
        renderScene();
        //drawPlane();

        drawSkybox();



    }
}
