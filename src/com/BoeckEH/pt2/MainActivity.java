package com.BoeckEH.pt2;


import java.util.Arrays;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.animation.ArgbEvaluator;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.view.MenuItem;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v4.app.NavUtils;

public class MainActivity extends Activity {

		public float mPreviousY = 0;
		public float mPreviousX = 0;
		public final static float TOUCH_SCALE_FACTOR = 180.0f / 320 ;
		
		public static int bitMapWidth = 512;
		public static int bitMapHeight = 512;
		public static int minSample = bitMapWidth * 2;
		
		// determines how many times the handler is called back
		long callsToHandlr = 0;
		// should the handler display the information (are we in the foreground and not hidden?)
		boolean shouldUpdate = true;
		// we need the view at some point
		View thisView;
		// create the object from the executor thread we made to handle reading the mic from NOT the UI thread
		ThreadPoolExecutorPT toExecThread;
		// instantiate the class to prep for the FFT
		ForFFT prepFFT = new ForFFT();
		// create the class to actually DO the FFT
		ColumFFT cFFT;
	    double[] magnitude;		
		ImageView scopeView;
		int sampleFreq = 44100;
		int bufferRequest = 4096;
		MonitorMic monTheMic;
		int iterLen;
		
		// used in the handler to get info from the thread callback
		int payloadInt = 0;
		long proctime = 0;
		String forDisplay = new String("Not reading yet!"); 	
		static boolean drawingRunning = false;
		int qLen =0;
    	int bufferRead = 0;
    	short[] buffer;
    	double maxMagnitude;
    	
    	int currentBitmap = 1;
    	Path mCurve1 = new Path();
    	Bitmap iv_bitmap1 = Bitmap.createBitmap(bitMapWidth, bitMapHeight, Bitmap.Config.ARGB_8888);
    	Canvas iv_canvas1 = new Canvas(iv_bitmap1);
    	Path mCurve2 = new Path();
    	Bitmap iv_bitmap2 = Bitmap.createBitmap(bitMapWidth, bitMapHeight, Bitmap.Config.ARGB_8888);
    	Canvas iv_canvas2 = new Canvas(iv_bitmap2);
    	Path mCurve3 = new Path();
    	Path mCurve4 = new Path();
    	Paint curvePaint;
    	Paint gridCurvePaint;
    	Paint wipeLinePaint;
    	int zz = 0;
    	int width, bottom;
        String freqString = new String();
    	private GLSurfaceView mGLView;
    	public boolean touchDn = false;
    	public float pressureDown;
  	
    	@Override
	    public void onCreate(Bundle savedInstanceState) 
		{
	        super.onCreate(savedInstanceState);

	        setContentView(R.layout.activity_main);
	        getActionBar().setDisplayHomeAsUpEnabled(false);

	        scopeView = (ImageView) findViewById(R.id.iv_Scope);
	        
	        curvePaint = new Paint();
	        curvePaint.setColor(Color.GREEN);
	        curvePaint.setStrokeWidth(2f);
	        curvePaint.setDither(true);
	        curvePaint.setStyle(Paint.Style.STROKE);
	        curvePaint.setStrokeJoin(Paint.Join.ROUND);
	        curvePaint.setStrokeCap(Paint.Cap.ROUND);
	        curvePaint.setPathEffect(new CornerPathEffect(50) );
	        curvePaint.setAntiAlias(true);

	        wipeLinePaint = new Paint();
	        wipeLinePaint.setColor(Color.argb(255,64, 64, 64));
	        wipeLinePaint.setStrokeWidth(2f);
	        wipeLinePaint.setDither(true);
	        wipeLinePaint.setStyle(Paint.Style.STROKE);
	        wipeLinePaint.setStrokeJoin(Paint.Join.ROUND);
	        wipeLinePaint.setStrokeCap(Paint.Cap.ROUND);
	        wipeLinePaint.setAntiAlias(true);
	        
	        gridCurvePaint = new Paint();
	        gridCurvePaint.setColor(Color.WHITE);
	        gridCurvePaint.setStrokeWidth(1f);
	        gridCurvePaint.setDither(true);
	        gridCurvePaint.setStyle(Paint.Style.FILL_AND_STROKE);
	        gridCurvePaint.setStrokeJoin(Paint.Join.ROUND);
	        gridCurvePaint.setStrokeCap(Paint.Cap.ROUND);
	        gridCurvePaint.setAntiAlias(true);

	        iv_canvas1.drawColor(Color.argb(255,64, 64, 64));
	        iv_canvas2.drawColor(Color.argb(255,64, 64, 64));
	        
	        width = iv_bitmap1.getWidth();
	        bottom = iv_bitmap1.getHeight();

	        StartMonitoring();
		  
//	        mGLView = new PTSurfaceView(this);
//	        setContentView(mGLView);
	 
	    };

	    @Override
	    public boolean onCreateOptionsMenu(Menu menu) {
	        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
	        return true;
	    };

	    @Override
	    public void onConfigurationChanged(Configuration newConfig) {
	      super.onConfigurationChanged(newConfig);
//	      setContentView(R.layout.myLayout);
	    }
	    
	    @Override
	    public boolean onOptionsItemSelected(MenuItem item) {
	        switch (item.getItemId()) {
	            case android.R.id.home:
	                NavUtils.navigateUpFromSameTask(this);
	                return true;
	        }
	        return super.onOptionsItemSelected(item);
	    };
	    
	    public Handler updateHandlr = new Handler()
	    {
	    	@Override
	    	/**
	    	 * We override the standard handler (which does whatever; who cares) 
	    	 */
	    	public void handleMessage(Message msg) 
	    	{
	    		// retrieve the payload from the message and get the encapsulated data
	    		Bundle payloadBundle = msg.getData();
//	    		payloadInt = payloadBundle.getInt("MaxVal");
	    		proctime = payloadBundle.getLong("ProcTime");
	    		buffer = payloadBundle.getShortArray("MicBuffer");
	    		bufferRead = payloadBundle.getInt("Samples");
	            thisView = findViewById(R.layout.activity_main);
	    		// increment the call counter (debug)
	    		callsToHandlr++;
	    		// if the flag says we should put the string to the text value
    			
	    		if (shouldUpdate)
	    		{
	    			qLen = toExecThread.GetQueueLength();
	    			// format and set the text into the textview
		    		forDisplay =  String.format("ms: %04d,  max: %06d,  calls: %08d, samples: %05d, q: %02d",proctime, payloadInt, callsToHandlr, bufferRead, qLen);
		    		TextView myTextView = (TextView) findViewById(R.id.tv_test);
		    		myTextView.setText(forDisplay);
		    		if (bufferRead != 0)
		    			new DrawingFFTBMAsync().execute();
		    		// thisView.refreshDrawableState();

	    		}
	    	}
	    };
	    
	     
	    public void StartMonitoring() {
	    	// create the mic monitoring class
			monTheMic = new MonitorMic(updateHandlr, sampleFreq, bufferRequest);
	        // create the thread pool that will monitor the mic in the background
	        toExecThread = new ThreadPoolExecutorPT();
	        // and pass it the runnable to execute
		    toExecThread.runTask(monTheMic.audioRunnable);
	    }
	    

	    public class DrawingFFTBMAsync extends AsyncTask<Void, Bitmap, Void>
	    {

			@Override
			protected Void doInBackground(Void... params) {
				android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO + android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE);
				// we need a float array, and we don't know at compile time how big the read might be, so runtime!
				// might as well do the FFT here in this thread
		    	double[] im_buffer = new double[minSample];
				short[] seg_re_buffer = new short[minSample];
				double[] re_buffer = new double[minSample];
				Bitmap tmpBitmap;
				Arrays.fill(im_buffer, 0);

				
				for (int aa = 0; aa < buffer.length; aa += minSample)
				{
//				    Log.i("Step", String.format("Sample: %d, length: %d, bitmap: %d", aa, minSample, currentBitmap));

					System.arraycopy(buffer, aa, seg_re_buffer, 0, minSample - 1);
			
					re_buffer = prepFFT.ConvertShortToDouble(seg_re_buffer.length, seg_re_buffer);
					Arrays.fill(im_buffer, 0);
					
					cFFT = new ColumFFT(minSample);
					cFFT.fft(re_buffer, im_buffer);
				    magnitude = new double[re_buffer.length];
				    iterLen = re_buffer.length/2;
			        // TODO:  need to decide which way we are going to graph here
			        
			        // if drawing the line type graph
				    maxMagnitude = Math.hypot(re_buffer[0], im_buffer[0]); 
			        for(int i = 0; i < iterLen ; i++)
			        {
				        magnitude[i]= Math.hypot(re_buffer[i], im_buffer[i]);
//				        if (maxMagnitude < magnitude[i]) maxMagnitude = magnitude[i];
				    }
				    payloadInt = (int) maxMagnitude;

				    // if drawing spectrum
//				    tmpBitmap = drawFFTCurveViaBM();
				
		        
 					// if drawing spectrum echogram type
 					tmpBitmap = drawFFTSpecCurveViaBM();


					publishProgress(tmpBitmap);

				    
				}
			    re_buffer = null;
			    im_buffer = null;
			    seg_re_buffer = null;
			    System.gc();
//			    Log.i("Step", "Garbage Collected");
			    return null;
			}
			
		     protected void onProgressUpdate(Bitmap... bitmap) {
				    Log.i("Step", "Update");
		    	 
		    	 scopeView.setImageDrawable(new BitmapDrawable(getResources(), bitmap[0]));
		     	}
		     
		     
	    	}

/*		     protected void onPostExecute(Bitmap bitmap) {
//			    Log.i("Step", "Garbage Collected");
		    	 scopeView.setImageDrawable(new BitmapDrawable(getResources(), bitmap));		     }
	    	
	    }
*/	   

	    
	    public Bitmap drawFFTSpecCurveViaBM()
	    {
	        return drawSpecOnBitmaps(iv_canvas1, mCurve1, mCurve3, curvePaint, gridCurvePaint, iv_bitmap1, iterLen);
	    }
	    
	    
	    public Bitmap drawSpecOnBitmaps(Canvas canvas, Path lineCurve, Path hatchCurve, Paint cPaint, Paint tPaint, Bitmap bitmap, int bufRead )
	    {
	    	lineCurve.reset();
	    	int left = 0;
	    	int offsetX = 30;
	        int absMag = 25000;
	        double alphaRatio = 255.0 / absMag;
	        float yres = bufRead / bitMapHeight;
	        tPaint.setColor(Color.MAGENTA);
	        if (alphaRatio > 255) 
	        	alphaRatio = 255;
	        if (zz > (width - offsetX) || zz == 0 )
	        {
	        	zz = 0;
		        hatchCurve.reset();
		        hatchCurve.moveTo(left, bottom);
		        for(int yy = 0, jj = 0; yy < bufRead ; yy+=yres, jj++)
		        {
			        if ((jj % 16) == 0)
			        {
			        	hatchCurve.moveTo(left, jj);
			        	hatchCurve.lineTo(left + offsetX ,jj);
			        }
			        if ((jj % 64) == 0)
			        {
		        		freqString = String.format("%d", (int) (ComputeFrequency(jj, bufRead, sampleFreq) / 4) );
			        	drawText(canvas, tPaint, freqString, left, bottom - yy, 255, 0);
			        }
			        
			        
		        }
		        canvas.drawPath(hatchCurve, tPaint);
        	}
	        
	        zz+=1;
	        int aa = ((zz + 1) % width);
	        int bb = ((zz + 2) % width);
	        
	        lineCurve.moveTo(left, bottom);
	        int alphaSet = 0;
        	wipeLinePaint.setColor(Color.argb(255,64, 64, 64));
        	wipeLinePaint.setStrokeWidth(2);
        	canvas.drawLine(aa+offsetX, bottom, aa+offsetX, 0, wipeLinePaint);
        	wipeLinePaint.setStrokeWidth(1);
        	wipeLinePaint.setColor(Color.GREEN);
        	canvas.drawLine(bb+offsetX, bottom, bb+offsetX, 0, wipeLinePaint);
        	float[] hsV = new float[3];
        	cPaint.setColor(Color.WHITE);
	        for(int i = 0; i < bufRead ; i++)
	        {
	        	alphaSet = (int)(alphaRatio * magnitude[i]);
	        	cPaint.setAlpha(alphaSet);
	        	canvas.drawPoint(zz+offsetX, bottom - i, cPaint);
	        	
	        }
    
	        return bitmap;

	    }
	    
	    
	    
	    
	    

	    // used to draw spectrum 
	    public Bitmap drawFFTCurveViaBM()
	    {
	    	Bitmap tmpBitmapdraw;
	    	if (currentBitmap == 1)
		    {
		        currentBitmap = 2;
		        tmpBitmapdraw = drawSpecCurveOnBitmaps(iv_canvas1, mCurve1, mCurve3, curvePaint, gridCurvePaint, iv_bitmap1, iterLen);
		        return tmpBitmapdraw;
		        
		    } else
		    {
		    	currentBitmap = 1;
		    	tmpBitmapdraw = drawSpecCurveOnBitmaps(iv_canvas2, mCurve2, mCurve4, curvePaint, gridCurvePaint, iv_bitmap2, iterLen);
		    	return tmpBitmapdraw;

		    }
	    }

	    public Bitmap drawSpecCurveOnBitmaps(Canvas canvas, Path lineCurve, Path hatchCurve, Paint cPaint, Paint tPaint, Bitmap bitmap, int bufRead )
	    {
 
/*	    	if ((callsToHandlr % 8192) == 8191)
    		{
    			iv_bitmap1 = null;
    			iv_bitmap2 = null;
    	    	mCurve1 = null;
    	    	mCurve2 = null;
    	    	mCurve3 = null;
    	    	mCurve4 = null;
    	    	iv_canvas1 = null;
    	    	iv_canvas2 = null;
    	    	System.gc();

    	    	mCurve1 = new Path();
    	    	mCurve2 = new Path();
    	    	iv_bitmap1 = Bitmap.createBitmap(bitMapWidth, bitMapHeight, Bitmap.Config.ARGB_8888);
    	    	iv_canvas1 = new Canvas(iv_bitmap1);
    	    	iv_bitmap2 = Bitmap.createBitmap(bitMapWidth, bitMapHeight, Bitmap.Config.ARGB_8888);
    	    	iv_canvas2 = new Canvas(iv_bitmap2);
    	    	mCurve3 = new Path();
    	    	mCurve4 = new Path();
    	    	
    	        iv_canvas1.drawColor(Color.argb(255,64, 64, 64));
    	        iv_canvas2.drawColor(Color.argb(255,64, 64, 64));
    	        

    		}
*/
	    	int left = 0;
	    	int offsetY = 60;
	        int bottom = canvas.getHeight();
	        int absMag = 64000;
	        int yresolution = absMag/bottom;
	        int width = canvas.getWidth();
	        int xresolution = bufRead / width;
	        String freqString = new String();
	        
    		lineCurve.reset();
	        lineCurve.moveTo(left, bottom - offsetY);
	        for(int i = 0; i < bufRead ; i += xresolution)
	        	lineCurve.lineTo(left + ((float)(i/xresolution)), (bottom - offsetY) - (int)magnitude[i]/(yresolution));
	        
	        hatchCurve.reset();
	        hatchCurve.moveTo(left, bottom);
	        for(int i = 0, jj = 0; i < bufRead - 10 ; i += xresolution, jj++)
	        {
		        if ((jj % 16) == 0)
		        {
		        	hatchCurve.moveTo(i/xresolution, bottom);
		        	hatchCurve.lineTo(i/xresolution, bottom - 20);
		        }
	        }
	        canvas.drawColor(Color.argb(255,64, 64, 64));
	        canvas.drawPath(lineCurve, cPaint);
	        canvas.drawPath(hatchCurve, tPaint);

	        for (int i = 0, jj = 0; i < bufRead ; i += (xresolution), jj++)
	        {
		        if (((jj % 64) == 0) || (i == (bufRead - (4 * xresolution))) || i == (4 * xresolution)) 
		        {
		        	if (i == (4 * xresolution))
		        		freqString = String.format("%d", 0);
		        	else
		        		freqString = String.format("%d", (int) (ComputeFrequency(i, bufRead, sampleFreq) / 4) );
		        	drawText(canvas, tPaint, freqString, (int) (i/xresolution), bottom - 20, 255, -90);
		        }
	        }
	        return bitmap;

	    }

	    
	    
	    /**
	     * 
	     * @param canvas
	     * @param paint
	     * @param text
	     * @param x
	     * @param y
	     * @param alpha
	     * @param rotate
	     */
	    public void drawText(Canvas canvas, Paint paint, String text, int x, int y, int alpha, double rotate)
	    {
	    	// draw bounding rect before rotating text
	    				Rect rect = new Rect();
	    				paint.getTextBounds(text, 0, text.length(), rect);
	    				canvas.save();
	    				paint.setAlpha(alpha);

	    				// rotate the canvas on center of the text to draw
	    				canvas.rotate((int) rotate, x + rect.left, y + rect.exactCenterY());
	    				// draw the rotated text
	    				canvas.drawText(text, x, y, paint);

	    				//undo the rotate
	    				canvas.restore();

	    }
	    
		public double ComputeFrequency(int arrayIndex, int fftOutWindowSize, int sampleRate) {
		    return ((1.0 * sampleRate) / (1.0 * fftOutWindowSize)) * arrayIndex;
		}
	    
	    
	

 class PTSurfaceView extends GLSurfaceView {

   public ScreenRenderer myRenderer;

	
	public PTSurfaceView(Context context){
      super( context );
      setEGLContextClientVersion(2);
      myRenderer = new ScreenRenderer( context );
      // Set the Renderer for drawing on the GLSurfaceView
      setRenderer( myRenderer );
   // Render the view only when there is a change in the drawing data
      setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
   }
   
   @Override
   public boolean onTouchEvent(MotionEvent e) {
       // MotionEvent reports input details from the touch screen
       // and other input controls. In this case, you are only
       // interested in events where the touch position changed.

       float x = e.getX();
       float y = e.getY();
       pressureDown = e.getPressure();

       switch (e.getAction()) {
           case MotionEvent.ACTION_MOVE:

               float dx = x - mPreviousX;
               float dy = y - mPreviousY;

               // reverse direction of rotation above the mid-line
               if (y > getHeight() / 2) {
                 dx = dx * -1 ;
               }

               // reverse direction of rotation to left of the mid-line
               if (x < getWidth() / 2) {
                 dy = dy * -1 ;
               }

               myRenderer.mAngle += (dx + dy) * TOUCH_SCALE_FACTOR;  // = 180.0f / 320
               requestRender();
           case MotionEvent.ACTION_DOWN:
        	   touchDn = true;
               requestRender();
               break;
           case MotionEvent.ACTION_UP:
        	   touchDn = false;
               requestRender();
        	   break;
       }

       mPreviousX = x;
       mPreviousY = y;
       return true;
   }
}
    
class ScreenRenderer implements GLSurfaceView.Renderer
{
	   private Context context;                           // Context (from Activity)
	   public Triangle mTriangle;
	   public float[] mProjMatrix = new float[16];
	   public float[] mMVPMatrix = new float[16];
	   public float[] mVMatrix = new float[16];
	   public float[] mRotationMatrix = new float[16];
	   public volatile float mAngle;
	   public ScreenRenderer(Context context)  {
	      super();
	      this.context = context;                         // Save Specified Context
	      this.mTriangle = new Triangle();
	      
	   }

	   public void onSurfaceCreated(GL10 gl, EGLConfig config) {
	      // Set the background frame color
//	      gl.glClearColor( 01f, 01f, 01f, 1.0f );
		   mTriangle = new Triangle();

	   }

	   public void onDrawFrame(GL10 gl) {
	      // Redraw background color
	      gl.glClear( GL10.GL_COLOR_BUFFER_BIT );

	      // Set to ModelView mode
	      gl.glMatrixMode( GL10.GL_MODELVIEW );           // Activate Model View Matrix
//	      gl.glMatrixMode(GL10.GL_PROJECTION);
	      gl.glLoadIdentity();                            // Load Identity Matrix


//	      gl.glTranslatef(0.0f, 0.0f, -5.0f);
	      
	      // Set the camera position (View matrix)
	      Matrix.setLookAtM(mVMatrix, 0, 0, 0, -4.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
//	      gl.glFrustumf(-1f, 1f, -1f, 1f, .1f, 10f);
	      // Calculate the projection and view transformation
	      Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);

	      // Create a rotation transformation for the triangle
//	      long time = SystemClock.uptimeMillis() % 4000L;
//	      float angle = 0.090f * ((int) time);

	      Matrix.setRotateM(mRotationMatrix, 0, mAngle, 0, 0, -1.0f);

	      // Combine the rotation matrix with the projection and camera view
	      Matrix.multiplyMM(mMVPMatrix, 0, mRotationMatrix, 0, mMVPMatrix, 0);
	      if (touchDn)
	    	  mTriangle.changeToDnColour(pressureDown * 2); else mTriangle.changeToUpColour();
//	      mSquare.draw(mMVPMatrix); 
	      mTriangle.draw(mMVPMatrix);
	       
	      
	   }

	   public void onSurfaceChanged(GL10 gl, int x, int y) {

		   // original
		   float aspect =  (float) x / (float) y;

	       if (x == 0) { // Prevent A Divide By Zero By
	           x = 1; // Making Height Equal One
	       }

	       gl.glViewport(0, 0, x, y); // Reset The Current Viewport
	       Matrix.frustumM(mProjMatrix, 0, -aspect, aspect, -1, 1, 2, 9);
	       
	   }

}
/*	    
	    //{{ Activity control 
	    @Override
	    protected void onPause()
	    {
	    	monTheMic.StopMonitoringMic();
	    	toExecThread.shutDown();
	    	toExecThread.shutDownNow();
	    	super.onPause();

	    }

	    @Override
	    protected void onResume()
	    {
	    	super.onResume();
	    	monTheMic.StartMonitoringMic();
	    	toExecThread.runTask(monTheMic.audioRunnable);

	    }

	    @Override
	    protected void onStop()
	    {
	    	monTheMic.StopMonitoringMic();
	    	toExecThread.shutDown();
	    	toExecThread.shutDownNow();
	    	super.onStop();
	 
	    }

	    @Override
	    protected void onRestart()
	    {
	    	super.onRestart();
	    	monTheMic.StartMonitoringMic();
	    	toExecThread.runTask(monTheMic.audioRunnable);
	    }

	    @Override
	    protected void onDestroy()
	    {

	    	monTheMic.StopMonitoringMic();
	    	toExecThread.shutDown();
	    	toExecThread.shutDownNow();
	    	super.onDestroy();
	    	
	    }
*/}



