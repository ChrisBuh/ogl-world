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

    private static final String TAG = "ImportObj";
    private Context mContext;

    private String mFileName;

    private List<String> mFacesList;
    private List<String> mVerticesListRaw;
    private List<String> mNormalsListRaw;
    private List<String> mTexelsListRaw;

    private List<String> mVerticesList;
    private List<String> mNormalsList;
    private List<String> mTexelsList;

    private float[] mVertices;
    private float[] mNormals;
    private float[] mTexels;

    private int mFacesCount = 3;
    private int mVertexSize = 3;
    private int mNormalSize = 3;
    private int mTexelSize = 2;

    private float mMoveX=0.0f;
    private float mMoveY=0.0f;
    private float mMoveZ=0.0f;
    private float mScale=1.0f;

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
        mVerticesListRaw = new ArrayList<>();
        mNormalsListRaw = new ArrayList<>();
        mTexelsListRaw = new ArrayList<>();

        try {
            Scanner scanner = new Scanner(mContext.getAssets().open(mFileName));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith("f ")) {
                    mFacesList.add(line.substring(2));
                } else if (line.startsWith("v ")) {
                    mVerticesListRaw.add(line.substring(2));
                } else if (line.startsWith("vn ")) {
                    mNormalsListRaw.add(line.substring(3));
                } else if (line.startsWith("vt ")) {
                    mTexelsListRaw.add(line.substring(3));
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

        mVerticesList = new ArrayList<>();
        mNormalsList = new ArrayList<>();
        mTexelsList = new ArrayList<>();

        for (int i=0; i<mFacesList.size(); i++) {
            String points[] = mFacesList.get(i).split(" ");
            for (int j=1; j<points.length+1; j++) {
                String element[] = points[j-1].split("/");
                String vertice[] = mVerticesListRaw.get(Integer.valueOf(element[0]) - 1).split(" ");
                String normal[] = mNormalsListRaw.get(Integer.valueOf(element[2]) - 1).split(" ");
                String texel[] = mTexelsListRaw.get(Integer.valueOf(element[1]) - 1).split(" ");

                for(String vert : vertice){
                    mVerticesList.add(vert);
                }

                for (String value : normal) {
                    mNormalsList.add(value);
                }

                for (String tex : texel) {
                    mTexelsList.add(tex);
                }
            }
        }

        for(int i=0; i<mVerticesList.size() ; i+=3){
            mVertices[i] = (Float.parseFloat(mVerticesList.get(i))+mMoveX)*mScale/2;
            mVertices[i+1] = (Float.parseFloat(mVerticesList.get(i+1))+mMoveY)*mScale/2;
            mVertices[i+2] = (Float.parseFloat(mVerticesList.get(i+2))+mMoveZ)*mScale/2;
        }

        for(int i=0; i<mNormalsList.size() ; i++){
            mNormals[i] = Float.parseFloat(mNormalsList.get(i));
        }

        for(int i=0; i<mTexelsList.size() ; i++){
            mTexels[i] = Float.parseFloat(mTexelsList.get(i));
        }
    }

    public float[] getVertices() {
        return mVertices;
    }

    public float[] getNormals() {
        return mNormals;
    }

    public float[] getTexels() {
        return mTexels;
    }
}
