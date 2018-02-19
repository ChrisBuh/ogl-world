package bertrandt.world.openGL.objects;

import android.content.Context;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import bertrandt.world.R;
import bertrandt.world.openGL.util.TextureHelper;


/**
 * Created by buhrmanc on 12.02.2018.
 */

public class Cube {
    private final FloatBuffer cubePosition;
    private final FloatBuffer cubeNormal;

    private FloatBuffer cubeTexel;
    //private final FloatBuffer cubeColor;

    private int mCubeTextureHandle;

    // 6 sides * 2 triangles * 3 vertices * 3 coordinates
    float[] cubePositionData = {
            -1.0f,-1.0f,-1.0f, // triangle 1 : begin
            -1.0f,-1.0f, 1.0f,
            -1.0f, 1.0f, 1.0f, // triangle 1 : end
            1.0f, 1.0f,-1.0f, // triangle 2 : begin
            -1.0f,-1.0f,-1.0f,
            -1.0f, 1.0f,-1.0f, // triangle 2 : end
            1.0f,-1.0f, 1.0f,
            -1.0f,-1.0f,-1.0f,
            1.0f,-1.0f,-1.0f,
            1.0f, 1.0f,-1.0f,
            1.0f,-1.0f,-1.0f,
            -1.0f,-1.0f,-1.0f,
            -1.0f,-1.0f,-1.0f,
            -1.0f, 1.0f, 1.0f,
            -1.0f, 1.0f,-1.0f,
            1.0f,-1.0f, 1.0f,
            -1.0f,-1.0f, 1.0f,
            -1.0f,-1.0f,-1.0f,
            -1.0f, 1.0f, 1.0f,
            -1.0f,-1.0f, 1.0f,
            1.0f,-1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            1.0f,-1.0f,-1.0f,
            1.0f, 1.0f,-1.0f,
            1.0f,-1.0f,-1.0f,
            1.0f, 1.0f, 1.0f,
            1.0f,-1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            1.0f, 1.0f,-1.0f,
            -1.0f, 1.0f,-1.0f,
            1.0f, 1.0f, 1.0f,
            -1.0f, 1.0f,-1.0f,
            -1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            -1.0f, 1.0f, 1.0f,
            1.0f,-1.0f, 1.0f
    };


    float[] cubeNormalData = {
            // nX, nY, nZ
            -1.0f, 0.0f, 0.0f, // triangle 1 : begin
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f, // triangle 1 : end
            0.0f, 0.0f, -1.0f, // triangle 2 : begin
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f, // triangle 2 : end
            0.0f,-1.0f, 0.0f, //
            0.0f,-1.0f, 0.0f,
            0.0f,-1.0f, 0.0f,
            0.0f, 0.0f,-1.0f, //
            0.0f, 0.0f,-1.0f,
            0.0f, 0.0f,-1.0f,
            -1.0f, 0.0f, 0.0f, //
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,
            0.0f,-1.0f, 0.0f, //
            0.0f,-1.0f, 0.0f,
            0.0f,-1.0f, 0.0f,
            0.0f, 0.0f, 1.0f, //
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, //
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f, //
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, //
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f, //
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 1.0f, //
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f
    };

    float[] cubeTexelData = {
            // nX, nY, nZ
            0.0f, 0.0f, // triangle 1 : begin
            0.0f, 1.0f,
            1.0f, 0.0f,  // triangle 1 : end
            0.0f, 1.0f,  // triangle 2 : begin
            1.0f, 0.0f,
            1.0f, 1.0f,  // triangle 2 : end
            0.0f, 0.0f, // triangle 1 : begin
            0.0f, 1.0f,
            1.0f, 0.0f,  // triangle 1 : end
            0.0f, 1.0f,  // triangle 2 : begin
            1.0f, 0.0f,
            1.0f, 1.0f,  // triangle 2 : end
            0.0f, 0.0f, // triangle 1 : begin
            0.0f, 1.0f,
            1.0f, 0.0f,  // triangle 1 : end
            0.0f, 1.0f,  // triangle 2 : begin
            1.0f, 0.0f,
            1.0f, 1.0f,  // triangle 2 : end
            0.0f, 0.0f, // triangle 1 : begin
            0.0f, 1.0f,
            1.0f, 0.0f,  // triangle 1 : end
            0.0f, 1.0f,  // triangle 2 : begin
            1.0f, 0.0f,
            1.0f, 1.0f,  // triangle 2 : end
            0.0f, 0.0f, // triangle 1 : begin
            0.0f, 1.0f,
            1.0f, 0.0f,  // triangle 1 : end
            0.0f, 1.0f,  // triangle 2 : begin
            1.0f, 0.0f,
            1.0f, 1.0f,  // triangle 2 : end
            0.0f, 0.0f, // triangle 1 : begin
            0.0f, 1.0f,
            1.0f, 0.0f,  // triangle 1 : end
            0.0f, 1.0f,  // triangle 2 : begin
            1.0f, 0.0f,
            1.0f, 1.0f,  // triangle 2 : end
    };

    //size of color data array: 6 sides * 2 triangles * 3 points * 4 color (RGBA) values
    float[] cubeColorData = new float[12*3*4];

    /**
     * Create a new cube with specified center position, size and color
     * @param center
     * @param size
     * @param color
     */
    public Cube(Context context, float[] center, float size, float[] color) {
        //set color data
        for (int v = 0; v < 12*3 ; v++){
            cubeColorData[4*v+0] = color[0];
            cubeColorData[4*v+1] = color[1];
            cubeColorData[4*v+2] = color[2];
            cubeColorData[4*v+3] = color[3];
        }

        //resize the cube
        for (int i = 0; i < 108; i++) {
            cubePositionData[i] = cubePositionData[i] * size/2;
        }

        //move the center of the cube to the place specified in parameter
        for (int j = 0; j < 36; j++) {
            cubePositionData[3*j] = cubePositionData[3*j] + center[0];
            cubePositionData[3*j + 1] = cubePositionData[3*j + 1] + center[1];
            cubePositionData[3*j + 2] = cubePositionData[3*j + 2] + center[2];
        }

        // Initialize the buffers.
        ByteBuffer bPos = ByteBuffer.allocateDirect(cubePositionData.length * 4);
        bPos.order(ByteOrder.nativeOrder());
        cubePosition = bPos.asFloatBuffer();

        ByteBuffer bNormal = ByteBuffer.allocateDirect(cubeNormalData.length * 4);
        bNormal.order(ByteOrder.nativeOrder());
        cubeNormal = bNormal.asFloatBuffer();

        cubeTexel = ByteBuffer.allocateDirect(cubeTexelData.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        cubeTexel.put(cubeTexelData).position(0);

        mCubeTextureHandle = TextureHelper.loadTexture(context, R.drawable.vwlogo);
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
/**
        ByteBuffer bColor = ByteBuffer.allocateDirect(cubeColorData.length * RenderConstants.FLOAT_SIZE_IN_BYTES);
        bColor.order(ByteOrder.nativeOrder());
        cubeColor = bColor.asFloatBuffer();*/

        cubePosition.put(cubePositionData).position(0);
        cubeNormal.put(cubeNormalData).position(0);
        /**cubeColor.put(cubeColorData).position(0);*/
    }

    public void render(int positionAttribute, int normalAttribute,int mTexelCoordinateHandle, int mTextureUniformHandle, boolean onlyPosition) {

        // Pass in the position information
        cubePosition.position(0);
        GLES20.glVertexAttribPointer(positionAttribute, 3, GLES20.GL_FLOAT, false,
                0, cubePosition);

        GLES20.glEnableVertexAttribArray(positionAttribute);


        if (!onlyPosition)
        {
            // Pass in the normal information
            cubeNormal.position(0);
            GLES20.glVertexAttribPointer(normalAttribute, 3, GLES20.GL_FLOAT, false,
                    0, cubeNormal);

            GLES20.glEnableVertexAttribArray(normalAttribute);

            // Pass in the color information
            /**cubeColor.position(0);
            GLES20.glVertexAttribPointer(colorAttribute, 4, GLES20.GL_FLOAT, false,
                    0, cubeColor);

            GLES20.glEnableVertexAttribArray(colorAttribute);*/

            // Pass in the texel information
            cubeTexel.rewind();
            GLES20.glVertexAttribPointer(mTexelCoordinateHandle, 2, GLES20.GL_FLOAT, false,
                    0, cubeTexel);
            GLES20.glEnableVertexAttribArray(mTexelCoordinateHandle);

            // Set the active texture unit to texture unit 0.
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
            // Bind the texture to this unit.
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mCubeTextureHandle);

            GLES20.glUniform1i(mTextureUniformHandle, 1);
        }

        // Draw the cube.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);
    }
}