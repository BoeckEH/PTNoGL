package com.BoeckEH.pt2;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.opengl.GLES20;

class LineGraphData {

    private FloatBuffer vertexBuffer;
    public int mProgram;
    public int  mMVPMatrixHandle;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;

	private final String vertexShaderCode =
        // This matrix member variable provides a hook to manipulate
        // the coordinates of the objects that use this vertex shader
        "uniform mat4 uMVPMatrix;" +
        "attribute vec4 vPosition;" +
        "void main() {" +
        // the matrix must be included as a modifier of gl_Position
        "  gl_Position = uMVPMatrix * vPosition;" +
        "}";
	   
	private final String fragmentShaderCode =
	    "precision mediump float;" +
	    "uniform vec4 vColor;" +
	    "void main() {" +
	    "  gl_FragColor = vColor;" +
	    "}";

    // Set color with red, green, blue and alpha (opacity) values
   float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };
   
   public static int loadShader(int type, String shaderCode){

	    // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
	    // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
	    int shader = GLES20.glCreateShader(type);

	    // add the source code to the shader and compile it
	    GLES20.glShaderSource(shader, shaderCode);
	    GLES20.glCompileShader(shader);

	    return shader;
	}

    
    public LineGraphData() {
         
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL ES Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // creates OpenGL ES program executables

    }
    
    public void draw(float[] mvpMatrix, float[][] lineBuffer, int numVertices) {

        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                lineBuffer[0].length * 4);

        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer();

    	// Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        int mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                                     GLES20.GL_FLOAT, false,
                                     12, vertexBuffer);

        // get handle to fragment shader's vColor member
        int mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        
        GLES20.glLineWidth(2f);
        
        // Draw the triangle
//        int vertexCount = 3;
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
        for (int ii = 0; ii < lineBuffer.length; ii++)
        {
            // add the coordinates to the FloatBuffer
            vertexBuffer.put(lineBuffer[ii]);
            // set the buffer to read the first coordinate
            vertexBuffer.position(0);
            GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, 0, numVertices);
        }

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
    
    public void changeToUpColour()
    {
    	color[0] = 0.63671875f;
    	color[1] = 0.76953125f;
    	color[2] = 0.22265625f;
    	color[3] = 1.0f;
    }
    
    public void changeToDnColour(float pressure)
    {
    	color[0] = pressure;
    	color[1] = 0f;
    	color[2] = 0f;
    	color[3] = 1.0f;
    	
    }
    
    
}

