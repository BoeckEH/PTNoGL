package com.BoeckEH.pt2;


import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.os.AsyncTask;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class MainActivity extends Activity {

		
//		public final static int bitMapWidth = 512;
//		public final static int bitMapHeight = 512;
		public final static int minSample = 512;
		public static boolean coldStart = true;
		
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
	    float[] magnitude;
        float absMag = 255;
        
		ImageView primaryScopeView;
		ImageView secondaryScopeView;
		static int sampleFreq = 8000;
		static int bufferRequest = 1024;
		MonitorMic monTheMic;
		int iterLen;
		
		// used in the handler to get info from the thread callback
		int payloadInt = 0;
		long proctime = 0;
		String forDisplay = new String("Not reading yet!"); 	
		static boolean drawingRunning = false;
		long qLen =0;
    	int bufferRead = 0;
    	short[] buffer;
    	double maxMagnitude;
    	boolean started = false;
    	
    	int currentBitmap = 1;

    	int primaryBitmapHeight = 0, primaryBitmapWidth = 0;
    	int secondaryBitmapHeight = 0, secondaryBitmapWidth = 0;
    	
    	boolean gotPreDrawMeasure = false;
    	
    	ViewTreeObserver primaryVTO, secondaryVTO;
    	
    	Rect primaryDisplay;
    	Display display;
    	
    	Path mCurve1 = new Path();
    	Bitmap iv_bitmap1;
    	Canvas iv_canvas1;
    	Path mCurve2 = new Path();
    	Bitmap iv_bitmap2;
    	Canvas iv_canvas2;
    	Path mCurve3 = new Path();
    	Path mCurve4 = new Path();
    	Paint curvePaint;
    	Paint gridCurvePaint;
    	Paint wipeLinePaint;
    	int offsetX = 30;
    	int zz = offsetX;
    	int width, bottom;
        String freqString = new String();
        
    	public static LayoutParams myLP;
    	public static LinearLayout myLL;
    	
    	public int globalTimerSec = 1800;
    	public String globalTimerString = "00:30";
    	
    	private static Context myContext;
  	
    	@Override
	    public void onCreate(Bundle savedInstanceState) 
		{
	        super.onCreate(savedInstanceState);
	        
	        myContext = getApplicationContext();

	        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	        String strSCUserName = preferences.getString("pref_updateSCUsername", "myUserName");
	        
	        
	        setContentView(R.layout.activity_main);
	        getActionBar().setDisplayHomeAsUpEnabled(false);
	        
	        Display display = getWindowManager().getDefaultDisplay();

	        Button btnStart = (Button) findViewById(R.id.btnStartStopAudio);
	        btnStart.setOnClickListener(btnStartOnClick);

	        ProgressBar pbHeardSound = (ProgressBar) findViewById(R.id.pb_HeardSound);
	        pbHeardSound.setVisibility(ProgressBar.INVISIBLE);
	        
	        primaryScopeView = (ImageView) findViewById(R.id.iv_ScopePrimary);
	        primaryVTO = primaryScopeView.getViewTreeObserver();
	        
	        secondaryScopeView = (ImageView) findViewById(R.id.iv_ScopeSecondary);
	        secondaryVTO = secondaryScopeView.getViewTreeObserver();
	        
	        primaryVTO.addOnPreDrawListener(myPDListener);
	        
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

	        
	        
//	        StartMonitoring();
	 
	    };
	    
	    public static Context getAppContext()
	    {
	    	return myContext;
	    }
	    
	    public OnPreDrawListener myPDListener = new OnPreDrawListener() {
			
			@Override
			public boolean onPreDraw() {

				if (!gotPreDrawMeasure)
				{
					primaryBitmapHeight = primaryScopeView.getMeasuredHeight();
			        primaryBitmapWidth = primaryScopeView.getMeasuredWidth();
	
			        secondaryBitmapHeight = secondaryScopeView.getMeasuredHeight();
			        secondaryBitmapWidth = secondaryScopeView.getMeasuredWidth();
			        
			    	iv_bitmap1 = Bitmap.createBitmap(primaryBitmapWidth, primaryBitmapHeight, Bitmap.Config.ARGB_8888);
			    	iv_canvas1 = new Canvas(iv_bitmap1);
	
			    	iv_bitmap2 = Bitmap.createBitmap(primaryBitmapWidth, primaryBitmapHeight, Bitmap.Config.ARGB_8888);
			    	iv_canvas2 = new Canvas(iv_bitmap2);
	
			    	iv_canvas1.drawColor(Color.argb(255,64, 64, 64));
			        iv_canvas2.drawColor(Color.argb(255,64, 64, 64));
			        
			        width = primaryBitmapWidth;
			        bottom = primaryBitmapHeight;
			        
			        Log.d("Step", "In here again");
			        
			        gotPreDrawMeasure = true;
			        
				}
				return true;
			}
		};

	    @Override
	    public boolean onCreateOptionsMenu(Menu menu) {
	        getMenuInflater().inflate(R.menu.main_menu, menu);
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
	    		buffer = payloadBundle.getShortArray("MicBuffer");
	    		bufferRead = payloadBundle.getInt("Samples");
	            thisView = findViewById(R.layout.activity_main);
	    		// increment the call counter (debug)
	    		callsToHandlr++;
	    		// if the flag says we should put the string to the text value
    			
	    		if (shouldUpdate)
	    		{
	    			qLen = Debug.getNativeHeapAllocatedSize();
	    			// format and set the text into the textview
		    		forDisplay =  String.format("ms: %03d,  max: %07.0f,  calls: %06d, samples: %04d, m: %d",proctime, absMag, callsToHandlr, bufferRead, qLen);
		    		TextView myTextView = (TextView) findViewById(R.id.tv_test);
		    		myTextView.setText(forDisplay);
		    		if (bufferRead != 0)
		    			DrawingFFTBM();
//		    		thisView.refreshDrawableState();

	    		}
	    	}
	    };
	    
	    
	    
	    public OnClickListener btnStartOnClick = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (primaryBitmapHeight > 0 && secondaryBitmapHeight > 0)
				{
					Button btnStart = (Button) v;
					if (!started){
						btnStart.setText(R.string.stop_audio);
				    	StartMonitoring();
				    	started = true;
					} else
					{
						StopMonitoring();
						started = false;
						btnStart.setText(R.string.start_audio);
					}
				}
			}
		};
		
		
	    
	     
	    public void StartMonitoring() {
	    	coldStart = false;
	    	// create the mic monitoring class
			monTheMic = new MonitorMic(updateHandlr, sampleFreq, bufferRequest);
	        // create the thread pool that will monitor the mic in the background
	        toExecThread = new ThreadPoolExecutorPT();
	        // and pass it the runnable to execute
		    toExecThread.runTask(monTheMic.audioRunnable);
	    	Toast.makeText(getApplicationContext(), 
                    "Audio Engine Started", Toast.LENGTH_SHORT).show();
	    	Button btnStart = (Button) findViewById(R.id.btnStartStopAudio);
			btnStart.setText(R.string.stop_audio);
		    
	    }
	    
	    public void StopMonitoring() {
	    	if (monTheMic != null)
	    		monTheMic.StopMonitoringMic();
	    	if (toExecThread != null)
	    	{
		    	toExecThread.shutDown();
		    	toExecThread.shutDownNow();
		    	Toast.makeText(getApplicationContext(), 
	                    "Audio Engine Stopped", Toast.LENGTH_SHORT).show();
		    	Button btnStart = (Button) findViewById(R.id.btnStartStopAudio);
				btnStart.setText(R.string.start_audio);
	    	}
	    }
	    
	    public void DrawingFFTBM() 
	    {
	    		
//				android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO + android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE);
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
				    iterLen = re_buffer.length/2 ;
				    magnitude = new float[iterLen];
			        // TODO:  need to decide which way we are going to graph here
			        
			        // if drawing the line type graph
				    maxMagnitude = Math.hypot(re_buffer[0], im_buffer[0]); 
				    for(int jj = 0; jj < iterLen; jj++)
			        {
			        	// his is y
			        	magnitude[jj]= (float)((Math.hypot(re_buffer[jj], im_buffer[jj])));
//				        if (maxMagnitude < magnitude[i]) maxMagnitude = magnitude[i];
			        	// this is z
				    }
				    payloadInt = (int) maxMagnitude;

				    // if drawing spectrum
//				    tmpBitmap = drawFFTCurveViaBM();
 					// if drawing spectrum echogram type
 					tmpBitmap = drawFFTSpecCurveViaBM();
//					publishProgress(tmpBitmap);
			    	primaryScopeView.setImageDrawable(new BitmapDrawable(getResources(), tmpBitmap));
				    
				}
			    re_buffer = null;
			    im_buffer = null;
			    seg_re_buffer = null;
			    System.gc();
			}
			
		    	 
    
	    public Bitmap drawFFTSpecCurveViaBM()
	    {
	        return drawSpecOnBitmaps(iv_canvas1, mCurve1, mCurve3, curvePaint, gridCurvePaint, iv_bitmap1, iterLen);
	    }
	    
	    
	    public Bitmap drawSpecOnBitmaps(Canvas canvas, Path lineCurve, Path hatchCurve, Paint cPaint, Paint tPaint, Bitmap bitmap, int bufRead )
	    {
	    	lineCurve.reset();
	    	int left = 0;
	    	float bitMapHeight = (float) bitmap.getHeight();
	    	float bitMapWidth = (float) bitmap.getWidth();

	        double alphaRatio = 255.0 / absMag;
	        float yres = bitMapHeight / bufRead;
	        tPaint.setColor(Color.MAGENTA);
	        if (alphaRatio > 255) 
	        	alphaRatio = 255;
	        if (zz == offsetX || zz >= (bitMapWidth))
	        {
	        	zz = offsetX;
		        hatchCurve.reset();
		        hatchCurve.moveTo(left, bottom);
		        int counter = 0;
		        for(float yy = 0; yy < bitMapHeight ; yy+=yres)
		        {
			        if ((counter % 8) == 0)
			        {
			        	hatchCurve.moveTo(left, yy);
			        	hatchCurve.lineTo(left + offsetX , yy);
			        }
			        if ((counter % 32) == 0)
			        {
		        		freqString = String.format("%d", (int) (ComputeFrequency((int)(yy / yres), bufRead, sampleFreq) / 4) );
			        	drawText(canvas, tPaint, freqString, left, bottom - (int)yy, 255, 0);
			        }
			        if (absMag < magnitude[(int)(yy/yres)]) absMag = magnitude[(int)(yy/yres)];
			        
			        counter++;
			        
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
        	canvas.drawLine(aa, bottom, aa, 0, wipeLinePaint);
        	wipeLinePaint.setStrokeWidth(1);
        	wipeLinePaint.setColor(Color.GREEN);
        	canvas.drawLine(bb, bottom, bb, 0, wipeLinePaint);
        	float[] hsV = new float[3];
        	cPaint.setColor(Color.WHITE);
        	absMag = absMag - (absMag * 0.01f);
	        for(float ii = 0; ii < bitMapHeight ; ii+=yres)
	        {
	        	if (absMag < magnitude[(int)(ii/yres)]) absMag = magnitude[(int)(ii/yres)];
	        	alphaSet = (int)(alphaRatio * magnitude[(int)(ii/yres)]);
	        	cPaint.setAlpha(alphaSet);
	        	canvas.drawPoint(zz, bottom - (int)ii, cPaint);
	        	
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
		        		freqString = String.format(Locale.getDefault(), "%d", 0);
		        	else
		        		freqString = String.format(Locale.getDefault(),"%d", (int) (ComputeFrequency(i, bufRead, sampleFreq) / 4) );
		        	drawText(canvas, tPaint, freqString, i/xresolution, bottom - 20, 255, -90);
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

		
	
		public void GoDoPrefsHeader(MenuItem item)
		{
			Intent i = new Intent(this, PreferencesActivityHeaders.class);
		    startActivityForResult(i, 0);
		}
	    
	    //{{ Activity control 
	    @Override
	    protected void onPause()
	    {
	    	StopMonitoring();
	    	super.onPause();

	    }

	    @Override
	    protected void onResume()
	    {
	    	super.onResume();
	    	if (!coldStart)
	    		StartMonitoring();
	    }

	    @Override
	    protected void onStop()
	    {
	    	StopMonitoring();
	    	super.onStop();
	    }

	    @Override
	    protected void onRestart()
	    {
	    	super.onRestart();
	    	if (!coldStart)
	    		StartMonitoring();
	    }

	    @Override
	    protected void onDestroy()
	    {
	    	StopMonitoring();
	    	super.onDestroy();
	    	
	    }    
}



/**/	    
