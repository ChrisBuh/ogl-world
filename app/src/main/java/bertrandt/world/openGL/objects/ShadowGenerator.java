package bertrandt.world.openGL.objects;

import bertrandt.world.openGL.programs.SimpleShaderProgram;
import bertrandt.world.openGL.util.FrameBuffer;

/**
 * Created by buhrmanc on 15.02.2018.
 */

public class ShadowGenerator {
    private FrameBuffer mFrameBuffer;

    public ShadowGenerator(){
    }

    public void generateShadow(int width, int height, boolean hasDepthTextureExtension){
        mFrameBuffer = new FrameBuffer();
        mFrameBuffer.generateShadowFBO(width, height, hasDepthTextureExtension);
    }
}
