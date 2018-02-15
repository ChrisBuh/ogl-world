package bertrandt.world.openGL.view;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import bertrandt.world.openGL.main.Renderer;

import static android.view.MotionEvent.actionToString;

/**
 * Created by buhrmanc on 14.02.2018.
 */

public class AdaptedGLSurfaceView extends GLSurfaceView {
    private bertrandt.world.openGL.main.Renderer mRenderer;

    private float previousX;
    private float previousY;

    private ScaleGestureDetector mScaleGestureDetector;
    private float mScaleFactor = 1.f;
    private float mScaleFactorPrev = 0.f;
    private long mMultiToSingleTouchBlock;


    public AdaptedGLSurfaceView(Context context) {
        super(context);
        //this.setOnTouchListener(this);

        mScaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());

        // Render the view only when there is a change in the drawing data
        //setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (motionEvent != null) {
            if (motionEvent.getAction() == MotionEvent.ACTION_POINTER_UP || motionEvent.getAction() == MotionEvent.ACTION_UP) {
                Log.i("View", "onTouchEvent: Up");
                mMultiToSingleTouchBlock = System.currentTimeMillis();
            }
        }
        if (mMultiToSingleTouchBlock + 300 < System.currentTimeMillis()) {

            if (motionEvent != null) {
                if (motionEvent.getPointerCount() <= 1) {
                    Log.d("Motion", "Single touch");

                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        previousX = motionEvent.getX();
                        previousY = motionEvent.getY();
                    } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                        final float deltaX = motionEvent.getX() - previousX;
                        final float deltaY = motionEvent.getY() - previousY;

                        previousX = motionEvent.getX();
                        previousY = motionEvent.getY();
                        Log.i("View", "onTouchEvent: " + deltaX + " " + deltaY);
                        this.queueEvent(new Runnable() {
                            @Override
                            public void run() {
                                mRenderer.handleTouchDrag(deltaX,
                                        deltaY);
                            }
                        });
                    }
                } else {
                    Log.d("Motion", "Multitouch");
                    mScaleGestureDetector.onTouchEvent(motionEvent);
                }
            }
        }
        return true;
    }

    private class ScaleListener
            extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            //mScaleFactor = Math.max(0.0f, Math.min(mScaleFactor, 0.5f));

            Log.i("View", "onScale: " + mScaleFactorPrev + " " + mScaleFactor);
            if (mScaleFactorPrev >= mScaleFactor) {
                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        mRenderer.handleTouchZoom(.2f);
                    }
                });

            } else {
                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        mRenderer.handleTouchZoom(-.2f);
                    }
                });
            }
            mScaleFactorPrev = mScaleFactor;

            invalidate();
            return true;
        }
    }

    public void setRenderer(bertrandt.world.openGL.main.Renderer renderer) {
        mRenderer = renderer;
        super.setRenderer(renderer);
    }

  /*  @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        int action = motionEvent.getActionMasked();
        int index = motionEvent.getActionIndex();
        int xPos = -1;
        int yPos = -1;

        Log.d("Motion","The action is " + actionToString(action));

        if(motionEvent.getPointerCount()>1){
            Log.d("Motion","Multitouch");
        }else{
            Log.d("Motion","Single touch");
        }

        if (motionEvent != null) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                previousX = motionEvent.getX();
                previousY = motionEvent.getY();
            } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                final float deltaX = motionEvent.getX() - previousX;
                final float deltaY = motionEvent.getY() - previousY;

                previousX = motionEvent.getX();
                previousY = motionEvent.getY();

                this.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        mRenderer.handleTouchDrag(deltaX,
                                deltaY);
                    }
                });
            }

            return true;
        } else {
            return false;
        }
        return false;
    }*/
}
