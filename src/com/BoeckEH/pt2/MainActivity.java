package com.BoeckEH.pt2;


import java.util.Arrays;
import android.os.Bundle;
import android.app.Activity;
import android.app.Application;
import android.content.res.Configuration;
import android.view.Menu;

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
		MonitorMic monTheMic;
		
		// used in the handler to get info from the thread callback
		int payloadInt = 0;
		long proctime = 0;
		String forDisplay = new String("Not reading yet!"); 	
		boolean drawingRunning = false;
		int qLen =0;
    	int bufferRead = 0;
    	short[] buffer;
    	
    	int currentBitmap = 1;
    	Path mCurve1 = new Path();
    	Bitmap iv_bitmap1 = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888);
    	Canvas iv_canvas1 = new Canvas(iv_bitmap1);
    	Path mCurve2 = new Path();
    	Bitmap iv_bitmap2 = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888);
    	Canvas iv_canvas2 = new Canvas(iv_bitmap2);
    	Path mCurve3 = new Path();
    	Path mCurve4 = new Path();
    	Paint curvePaint;
    	Paint gridCurvePaint; 
    	
  	
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

	        gridCurvePaint = new Paint();
	        gridCurvePaint.setColor(Color.WHITE);
	        gridCurvePaint.setStrokeWidth(1f);
	        gridCurvePaint.setDither(true);
	        gridCurvePaint.setStyle(Paint.Style.FILL_AND_STROKE);
	        gridCurvePaint.setStrokeJoin(Paint.Join.ROUND);
	        gridCurvePaint.setStrokeCap(Paint.Cap.ROUND);
	        gridCurvePaint.setAntiAlias(true);

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
	    		payloadInt = payloadBundle.getInt("MaxVal");
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
			monTheMic = new MonitorMic(updateHandlr, sampleFreq);
	        // create the thread pool that will monitor the mic in the background
	        toExecThread = new ThreadPoolExecutorPT();
	        // and pass it the runnable to execute
		    toExecThread.runTask(monTheMic.audioRunnable);
	    }
	    

	    public class DrawingFFTBMAsync extends AsyncTask<Void, Void, Bitmap>
	    {

			@Override
			protected Bitmap doInBackground(Void... params) {
				android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO );
				drawingRunning = true;
				// might as well do the FFT here in this thread
				// we need a float array, and we don't know at compile time how big the read might be, so runtime!
				double[] re_buffer = new double[bufferRead];
				double[] im_buffer = new double[bufferRead];
				Arrays.fill(im_buffer, 0);
				
				re_buffer = prepFFT.ConvertShortToDouble(bufferRead, buffer);
				
				cFFT = new ColumFFT(bufferRead);
				cFFT.fft(re_buffer, im_buffer);
			    magnitude = new double[re_buffer.length];
			    int iterLen = re_buffer.length;
			    for (int i=0; i<iterLen; i++){
			        magnitude[i]= Math.hypot(re_buffer[i], im_buffer[i]);
			    }
			    re_buffer = null;
			    im_buffer = null;
			    System.gc();
				return drawFFTCurveViaBM();


			}

			@Override
			protected void onPostExecute(Bitmap iv_bitmap) {
				scopeView.setImageDrawable(new BitmapDrawable(getResources(), iv_bitmap));
				drawingRunning = false;

			}
	    	
	    }
	   
	    public Bitmap drawFFTCurveViaBM()
	    {

	    	if (currentBitmap == 1)
		    {
		        currentBitmap = 2;
		        return DrawOnBitmaps(iv_canvas1, mCurve1, mCurve3, curvePaint, gridCurvePaint, iv_bitmap1, bufferRead/2);
		        
		    } else
		    {
		    	currentBitmap = 1;
		    	return DrawOnBitmaps(iv_canvas2, mCurve2, mCurve4, curvePaint, gridCurvePaint, iv_bitmap2, bufferRead/2);

		    }
		    	
	    }

	    public Bitmap DrawOnBitmaps(Canvas canvas, Path lineCurve, Path hatchCurve, Paint cPaint, Paint tPaint, Bitmap bitmap, int bufRead )
	    {
	    	lineCurve.reset();
	    	int left = 0;
	    	int offsetY = 60;
	        int bottom = canvas.getHeight();
//	        int middle = bottom / 2;
	        int yresolution = 32768/(bottom - offsetY);
	        int width = canvas.getWidth();
	        int xresolution = bufRead / width;
	        String freqString = new String();

	        canvas.drawColor(Color.argb(255,64, 64, 64));
	        
	        lineCurve.moveTo(left, bottom);
	        for(int i = 0; i < bufRead - 1 ; i += xresolution)
	        	lineCurve.lineTo(left + ((float)(i/xresolution)), (bottom - offsetY) - (int)magnitude[i]/(yresolution));
	        canvas.drawPath(lineCurve, cPaint);
	        
	        hatchCurve.reset();
	        hatchCurve.moveTo(left, bottom);
	        for(int i = 0, jj = 0; i < bufRead - 1 ; i += xresolution, jj++)
	        {
		        if ((jj % 16) == 0)
		        {
		        	hatchCurve.moveTo(i, bottom);
		        	hatchCurve.lineTo(i, bottom - 20);
		        }
	        }
	        canvas.drawPath(hatchCurve, tPaint);

	        for (int i = 0, jj = 0; i < bufRead - 1 ; i += (xresolution), jj++)
	        {
		        if (((jj % 64) == 0) || (i == bufRead)) 
		        {
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
	    
	}	    
	
/*
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
	       }

	       mPreviousX = x;
	       mPreviousY = y;
	       return true;
	   }
	}
	    
	class ScreenRenderer implements GLSurfaceView.Renderer
	{
		   private Context context;                           // Context (from Activity)
		   public float[] mProjMatrix = new float[16];
		   public float[] mMVPMatrix = new float[16];
		   public float[] mVMatrix = new float[16];
		   public float[] mRotationMatrix = new float[16];
		   public volatile float mAngle;
		   public ScreenRenderer(Context context)  {
	       super();
		   this.context = context;                         // Save Specified Context
		      
		   }

		   public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		      // Set the background frame color
//		      gl.glClearColor( 01f, 01f, 01f, 1.0f );

		   }

		   public void onDrawFrame(GL10 gl) {
		      // Redraw background color
		      gl.glClear( GL10.GL_COLOR_BUFFER_BIT );

		      
		   }



		   public void onSurfaceChanged(GL10 gl, int x, int y) {

			   // original
			   float aspect =  (float) x / (float) y;

		       if (x == 0) { // Prevent A Divide By Zero By
		           x = 1; // Making Height Equal One
		       }

		       gl.glViewport(0, 0, x, y); // Reset The Current Viewport
		       
		   }
		   

	}
	    
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
	// }}
	 * 
	 */

