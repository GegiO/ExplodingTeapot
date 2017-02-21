package oniani.theexplodingteapot;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.content.ContentValues.TAG;


public class MyGLRenderer implements GLSurfaceView.Renderer{

    private Teapot mTeapot;

    private float[] mMVPMatrix = new float[16];
    private float[] mMVMatrix = new float[16];
    private float[] mModelMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];
    private float[] mViewMatrix = new float[16];

    public float size = 1;
    public float brightness;
    public int number = 0;

    public boolean countDown = false;
    public int triggerNum;

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES20.glClearColor(0,0,0,1);
        GLES20.glEnable(GL10.GL_DEPTH_TEST);

        mTeapot = new Teapot();
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i1) {
        GLES20.glViewport(0,0,i,i1);

        float ratio = (float) i/i1;
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1,1, 1,50);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {

        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT );
        Matrix.setLookAtM(mViewMatrix, 0, 16f, 16f,16f,0f,0f,0f,0f,1.0f,0.0f);


        for(int x = 0; x <= number; x++) {
            Matrix.setIdentityM(mModelMatrix, 0);
            TranslateMatrixPattern(x);
            Matrix.rotateM(mModelMatrix, 0, 1, 0, 1, 0);
            Matrix.scaleM(mModelMatrix, 0, size, size, size);

            Matrix.multiplyMM(mMVMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

            Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVMatrix, 0);

            mTeapot.draw(mMVPMatrix, mMVMatrix, brightness);
        }
    }


    public void TranslateMatrixPattern(int number)
    {
        int turn;
        if(number > 8)
            turn = number % 8;
        else
            turn = number;

        float xVal = 0;
        float zVal = 0;
        float distance = ((number/8) + 1) * size;

        if(turn == 1 || turn == 2 || turn == 8)
            xVal = 3.5f;
        else if(turn == 3 || turn == 7)
            xVal = 0;
        else if(turn == 4 || turn == 5 || turn == 6)
            xVal = -3.5f;

        if(turn == 2 || turn == 3 || turn == 4)
            zVal = 3.5f;
        else if(turn == 1 || turn == 5)
            zVal = 0;
        else if(turn == 6 || turn == 7 || turn == 8)
            zVal = -3.5f;

        Matrix.translateM(mModelMatrix, 0, xVal * distance, 0, zVal * distance);
    }


    public void explode()
    {
        if(!mTeapot.exploding)
            mTeapot.exploding = true;
        else
            mTeapot.exploding = false;


    }



}
