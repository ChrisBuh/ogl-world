package bertrandt.world.openGL.objects;

import bertrandt.world.openGL.programs.SimpleShaderProgram;
import bertrandt.world.openGL.util.FrameBuffer;

/**
 * Created by buhrmanc on 15.02.2018.
 */

public class ShadowGenerator {
    private FrameBuffer mFrameBuffer;
    private boolean initialised=false;
    private int mWidth;
    private int mHeight;

    public ShadowGenerator(){
    }

    public void generateShadow(int width, int height, boolean hasDepthTextureExtension){
        this.mWidth = width;
        this.mHeight = height;
        mFrameBuffer = new FrameBuffer();
        mFrameBuffer.generateShadowFBO(width, height, hasDepthTextureExtension);
        initialised = true;
    }

    public int getFBOId(){
        if(!initialised){
            throw new RuntimeException("Framebuffer not generated");
        }
        return mFrameBuffer.getFrameBufferId();

    }

    public int getShadowTextureId(){
        if(!initialised){
            throw new RuntimeException("Framebuffer not generated");
        }
        return mFrameBuffer.getRenderTextureId();

    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }
}
