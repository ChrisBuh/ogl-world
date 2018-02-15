package bertrandt.world.openGL.util;

import android.opengl.GLES20;
import android.util.Log;

/**
 * Created by buhrmanc on 15.02.2018.
 */

public class FrameBuffer {
    public FrameBuffer(){

    }

    private int[] fboId;
    private int[] depthTextureId;
    private int[] renderTextureId;

    public void generateShadowFBO(int width, int height, boolean mHasDepthTextureExtension){

        this.fboId = new int[1];
        this.depthTextureId = new int[1];
        this.renderTextureId = new int[1];

        // create a framebuffer object
        GLES20.glGenFramebuffers(1, fboId, 0);

        // create render buffer and bind 16-bit depth buffer
        GLES20.glGenRenderbuffers(1, depthTextureId, 0);
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, depthTextureId[0]);
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, width, height);

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
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);

            // specify texture as color attachment
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, renderTextureId[0], 0);

            // attach the texture to FBO depth attachment point
            // (not supported with gl_texture_2d)
            GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, depthTextureId[0]);
        } else {
            // Use a depth texture
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_DEPTH_COMPONENT, width, height, 0, GLES20.GL_DEPTH_COMPONENT, GLES20.GL_UNSIGNED_INT, null);

            // Attach the depth texture to FBO depth attachment point
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_TEXTURE_2D, renderTextureId[0], 0);
        }

        // check FBO status
        int FBOstatus = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (FBOstatus != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            Log.e("Framebuffer", "GL_FRAMEBUFFER_COMPLETE failed, CANNOT use FBO");
            throw new RuntimeException("GL_FRAMEBUFFER_COMPLETE failed, CANNOT use FBO");
        }
    }

    public int getFrameBufferId(){
        return fboId[0];
    }

    public int getRenderTextureId() {
        return renderTextureId[0];
    }


}
