package oniani.theexplodingteapot;


import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Vector;

import static android.content.ContentValues.TAG;
import static oniani.theexplodingteapot.MainActivity.teaNormals;
import static oniani.theexplodingteapot.MainActivity.teaVertices;
import static oniani.theexplodingteapot.MainActivity.teaIndices;

public class Teapot {

    private float[] tempVertices = teaVertices.clone();

    private final String vertexShaderCode =
                    "uniform mat4 uMVPMatrix;" +
                    "uniform mat4 uMVMatrix;"+

                    "attribute vec4 aPosition;" +
                    "attribute vec3 aNormal;" +

                    "varying vec3 vPosition;"+
                    "varying vec3 vNormal;"+

                    "void main() {" +
                    "   vPosition = vec3(uMVMatrix * aPosition);"+
                    "   vNormal   = vec3(uMVMatrix * vec4(aNormal,0.0));"+
                    "   gl_Position = uMVPMatrix * aPosition;" +
                    "}";
    private final String fragmentShaderCode =
                    "precision mediump float;" +

                    "uniform vec4 uColor;" +
                    "uniform vec3 uLightPos;" +
                    "uniform float uLightPower;"+

                    "varying vec3 vNormal;" +
                    "varying vec3 vPosition;"+

                    "void main() {" +
                    "   float distance = length(uLightPos - vPosition);"+
                    "   vec3 lightVec = normalize(uLightPos - vPosition);"+
                    "   float diffuse = max(dot(vNormal, lightVec), 0.05);"+
                    "   diffuse = diffuse * (1.0 / (1.0 + ((0.25/uLightPower) * distance * distance)));"+
                    "   gl_FragColor = uColor * diffuse;" +
                    "}";


    static int COORDS_PER_VERTEX = 3;

    private float color[] = {1f,1f,1f,1.0f};
    private float lightPos[] = {4f,4f,4f};

    private FloatBuffer vertexBuffer;
    private FloatBuffer normalBuffer;
    private ShortBuffer indexBuffer;
    private int Program;
    private float isExploding = 0f;

    public Teapot()
    {
        //------------------buffers
        ByteBuffer bbuff = ByteBuffer.allocateDirect(tempVertices.length * 4);
        bbuff.order(ByteOrder.nativeOrder());
        vertexBuffer = bbuff.asFloatBuffer();
        vertexBuffer.put(tempVertices);
        vertexBuffer.position(0);


        ByteBuffer bytBuff = ByteBuffer.allocateDirect(teaNormals.length * 4);
        bytBuff.order(ByteOrder.nativeOrder());
        normalBuffer = bytBuff.asFloatBuffer();
        normalBuffer.put(teaNormals);
        normalBuffer.position(0);

        ByteBuffer bybuff = ByteBuffer.allocateDirect(teaIndices.length * 2);
        bybuff.order(ByteOrder.nativeOrder());
        indexBuffer = bybuff.asShortBuffer();
        indexBuffer.put(teaIndices);
        indexBuffer.position(0);

        //----------------shaders
        int vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vertexShader, vertexShaderCode);
        GLES20.glCompileShader(vertexShader);

        int fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragmentShader, fragmentShaderCode);
        GLES20.glCompileShader(fragmentShader);

        Program = GLES20.glCreateProgram();
        GLES20.glAttachShader(Program, vertexShader);
        GLES20.glAttachShader(Program, fragmentShader);
        GLES20.glLinkProgram(Program);

    }


    private final int vertexCount = teaVertices.length/ COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4;


    public void draw(float[] mMVPMatrix, float[] mMVMatrix, float brightness)
    {
        GLES20.glUseProgram(Program);

        ByteBuffer bbuff = ByteBuffer.allocateDirect(teaVertices.length * 4);
        bbuff.order(ByteOrder.nativeOrder());
        vertexBuffer = bbuff.asFloatBuffer();
        vertexBuffer.put(tempVertices);
        vertexBuffer.position(0);


        int positionHandle = GLES20.glGetAttribLocation(Program, "aPosition");
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        int normalHandle = GLES20.glGetAttribLocation(Program, "aNormal");
        GLES20.glVertexAttribPointer(normalHandle,3,GLES20.GL_FLOAT, false, 0, normalBuffer);
        GLES20.glEnableVertexAttribArray(normalHandle);

        int colorHandle = GLES20.glGetUniformLocation(Program, "uColor");
        GLES20.glUniform4fv(colorHandle,1,color,0);

        int lightHandle = GLES20.glGetUniformLocation(Program, "uLightPos");
        GLES20.glUniform3fv(lightHandle,1,lightPos,0);

        int lightHandlePower = GLES20.glGetUniformLocation(Program, "uLightPower");
        GLES20.glUniform1f(lightHandlePower, brightness);

        int mMVPMatrixHandle = GLES20.glGetUniformLocation(Program, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        int mMVMatrixHandle = GLES20.glGetUniformLocation(Program, "uMVMatrix");
        GLES20.glUniformMatrix4fv(mMVMatrixHandle,1,false, mMVMatrix,0);

        if(exploding) {
            explode();
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
        }
        else {
            tempVertices = teaVertices.clone();
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, teaIndices.length,
                    GLES20.GL_UNSIGNED_SHORT, indexBuffer);
        }

        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(normalHandle);
    }


    float launchspeed = 2f;
    boolean exploding = false;

    public void explode()
    {
        for(int x = 0; x <= tempVertices.length - 7; x += 9)
        {
            float normal[] = findNormal(tempVertices[x] ,  tempVertices[x+1], tempVertices[x+2],
                                        tempVertices[x+3], tempVertices[x+4], tempVertices[x+5],
                                        tempVertices[x+6], tempVertices[x+7], tempVertices[x+8]);

            for( int y = x; y < x + 9; y += 3)
            {
                tempVertices[y]   += normal[0] * launchspeed;
                tempVertices[y+1] += normal[1] * launchspeed;
                tempVertices[y+2] += normal[2] * launchspeed;
            }
        }
    }

    public float[] findNormal(float ax, float ay, float az,
                           float bx, float by, float bz,
                           float cx, float cy, float cz)
    {
        float Ux = bx - ax;
        float Uy = by - ay;
        float Uz = bz - az;

        float Vx = cx - ax;
        float Vy = cy - ay;
        float Vz = cz - az;

        float Nx = Uy*Vz - Uz*Vy;
        float Ny = Uz*Vx - Ux*Vz;
        float Nz = Ux*Vy - Uy*Vx;

        float normal[] = {Nx, Ny, Nz};
        return normal;
    }
}
