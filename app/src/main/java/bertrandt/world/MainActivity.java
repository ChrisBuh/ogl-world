package bertrandt.world;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import bertrandt.world.openGL.main.Renderer;
import bertrandt.world.openGL.view.AdaptedGLSurfaceView;

public class MainActivity extends AppCompatActivity {
    private AdaptedGLSurfaceView mGLView;
    private Renderer renderer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity
        mGLView = new AdaptedGLSurfaceView(this);

        // Create an OpenGL ES 2.0 context.
        mGLView.setEGLContextClientVersion(2);

        renderer = new Renderer(this);
        mGLView.setRenderer(renderer);

        setContentView(mGLView);
    }



    @Override
    protected void onPause() {
        super.onPause();
        mGLView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLView.onResume();
    }
}
