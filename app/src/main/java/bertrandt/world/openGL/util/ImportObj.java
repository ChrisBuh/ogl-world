package bertrandt.world.openGL.util;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by buhrmanc on 05.02.2018.
 */

public class ImportObj {
/*
    private static final String TAG = "ImportObj";
    private Context mContext;

    private String mFileName;

    private List<String> mFacesList;
    private List<String> mVerticesList;
    private List<String> mNormalsList;
    private List<String> mTexelsList;

    private float[] mVertices;
    private float[] mNormals;
    private float[] mTexels;

    private int mBytesPerFloat = 4;
    private int mFacesCount = 3;
    private int mVertexSize = 3;
    private int mNormalSize = 3;
    private int mTexelSize = 2;

    private float mMoveX=0.0f;
    private float mMoveY=0.0f;
    private float mMoveZ=0.0f;
    private float mScale=1.0f;

    private int mObjectTextureHandle;

    public ImportObj(Context context, String fileName) {
        this.mContext = context;
        this.mFileName = fileName;
        readRaw();
        allocateArrays();
        populateArrays();
    }

    public ImportObj(Context context, String fileName,
                     final float moveX, final float moveY, final float moveZ, final float scale) {
        this.mContext = context;
        this.mFileName = fileName;
        this.mMoveX = moveX;
        this.mMoveY = moveY;
        this.mMoveZ = moveZ;
        this.mScale = scale;
        readRaw();
        allocateArrays();
        populateArrays();
    }

    private void readRaw() {

        mFacesList = new ArrayList<>();
        mVerticesList = new ArrayList<>();
        mNormalsList = new ArrayList<>();
        mTexelsList = new ArrayList<>();

        try {
            Scanner scanner = new Scanner(mContext.getAssets().open(mFileName));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith("f ")) {
                    mFacesList.add(line.substring(2));
                } else if (line.startsWith("v ")) {
                    mVerticesList.add(line.substring(2));
                } else if (line.startsWith("vn ")) {
                    mNormalsList.add(line.substring(3));
                } else if (line.startsWith("vt ")) {
                    mTexelsList.add(line.substring(3));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "readRaw: object file could not be read");
        }
    }

    private void allocateArrays() {
        mVertices = new float[mFacesList.size() * mFacesCount * mVertexSize];
        mNormals = new float[mFacesList.size() * mFacesCount * mNormalSize];
        mTexels = new float[mFacesList.size() * mFacesCount * mTexelSize];
    }

    private void populateArrays() {
        for (int i=0; i<mFacesList.size(); i++) {
            String points[] = mFacesList.get(i).split(" ");
            for (int j=1; j<points.length+1; j++) {
                String element[] = points[j-1].split("/");
                String vertice[] = mVerticesList.get(Integer.valueOf(element[0]) - 1).split(" ");
                String normal[] = mNormalsList.get(Integer.valueOf(element[2]) - 1).split(" ");
                String texel[] = mTexelsList.get(Integer.valueOf(element[1]) - 1).split(" ");

                //Log.i(TAG, "populateArrays: " + vertice[0] + " " + vertice[1] + " " + vertice[2]);

                mVertices[i*j] = (Float.parseFloat(vertice[0])+mMoveX)*mScale/2;
                mVertices[i*j+1] = (Float.parseFloat(vertice[1])+mMoveX)*mScale/2;
                mVertices[i*j+2] = (Float.parseFloat(vertice[2])+mMoveX)*mScale/2;

                for (String value : normal) {
                    mNormalsBuffer.put(Float.parseFloat(value));
                }
                mTexelsBuffer.put(Float.parseFloat(texel[0]));
                mTexelsBuffer.put(Float.parseFloat(texel[1]));
            }
        }
        mObjectTextureHandle = TextureHelper.loadTexture(mContext, R.drawable.touareg);
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
    }

    public FloatBuffer getVerticesBuffer() {
        return mVerticesBuffer;
    }

    public FloatBuffer getNormalsBuffer() {
        return mNormalsBuffer;
    }

    public FloatBuffer getTexelsBuffer() {
        return mTexelsBuffer;
    }

    public int getObjectTextureHandle() {
        return mObjectTextureHandle;
    }

    public int getPositionSize(){
        return mFacesList.size()*3;
    }*/
}
