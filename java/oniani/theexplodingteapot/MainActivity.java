package oniani.theexplodingteapot;

import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity{

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private MyGLSurfaceView mGLView;
    public FrameLayout frameLayout;

    public static float[] teaVertices;
    public static float[] teaNormals;
    public static short[] teaIndices;

    public float scale;
    public float brightness;
    public int numObjects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getTeapotFromObj();
        mGLView = new MyGLSurfaceView(this);
        frameLayout = (FrameLayout)findViewById(R.id.frameLayout1);
        frameLayout.addView(mGLView);

        final SeekBar sk = (SeekBar)findViewById(R.id.seekBar);
        sk.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                scale = (i/50) + 1;
                mGLView.mRenderer.size = scale;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        final SeekBar sk2 = (SeekBar)findViewById(R.id.seekBar2);
        sk2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                brightness = i;
                mGLView.mRenderer.brightness = brightness;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        final SeekBar sk3 = (SeekBar)findViewById(R.id.seekBarProg);
        sk3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                numObjects = i;
                mGLView.mRenderer.number = numObjects;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
    public native String stringFromJNI();

    private void getTeapotFromObj()
    {

        ArrayList<Float> teapotVertices = new ArrayList<Float>();
        ArrayList<Float> teapotNormals = new ArrayList<Float>();
        ArrayList<Short> teapotIndices = new ArrayList<Short>();

        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new InputStreamReader(getAssets().open("untitled.obj")));
            String mLine;

            while((mLine = reader.readLine()) != null)
            {
                String[] splitLine = mLine.split(" ");

                if(splitLine[0].equals("v"))
                {
                    teapotVertices.add(Float.parseFloat(splitLine[1]));
                    teapotVertices.add(Float.parseFloat(splitLine[2]));
                    teapotVertices.add(Float.parseFloat(splitLine[3]));
                }
                else if(splitLine[0].equals("vn"))
                {
                    teapotNormals.add(Float.parseFloat(splitLine[1]));
                    teapotNormals.add(Float.parseFloat(splitLine[2]));
                    teapotNormals.add(Float.parseFloat(splitLine[3]));
                }
                else if(splitLine[0].equals("f"))
                {
                    String[] splitIndices = splitLine[1].split("//");
                    teapotIndices.add(Short.parseShort(splitIndices[0]));
                    splitIndices = splitLine[2].split("//");
                    teapotIndices.add(Short.parseShort(splitIndices[0]));
                    splitIndices = splitLine[3].split("//");
                    teapotIndices.add(Short.parseShort(splitIndices[0]));
                }
            }

        }
        catch(IOException e)
        {
            Log.e("FILE", "OBJ file reader failure");
        }
        finally
        {
            if(reader != null)
            {
                try{
                    reader.close();
                }catch(IOException e)
                {
                    Log.e("FILE", "Reader failed to close");
                }
            }
        }

        teaVertices = arrayListToArray(teapotVertices);
        teaNormals = arrayListToArray(teapotNormals);
        teaIndices = arrayListToArrayShort(teapotIndices);
    }

    private float[] arrayListToArray(ArrayList<Float> list)
    {
        float[] array = new float[list.size()];
        int i = 0;

        for(Float f : list)
        {
            array[i] = f;
            i++;
        }

        return array;
    }

    private short[] arrayListToArrayShort(ArrayList<Short> list)
    {
        short[] array = new short[list.size()];
        int i = 0;

        for(Short f : list)
        {
            array[i] = (short)(f - 1);
            i++;
        }

        return array;
    }

}
