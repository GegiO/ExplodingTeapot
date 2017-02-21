package oniani.theexplodingteapot;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

public class MyGLSurfaceView extends GLSurfaceView {

    public final MyGLRenderer mRenderer;

    public MyGLSurfaceView(Context context)
    {
        super(context);
        setEGLContextClientVersion(2);

        mRenderer = new MyGLRenderer();
        setRenderer(mRenderer);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e)
    {
        switch (e.getAction()){
            case MotionEvent.ACTION_UP:
                mRenderer.explode();
                break;
        }
        return true;
    }
}
