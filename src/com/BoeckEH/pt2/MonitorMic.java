package com.BoeckEH.pt2;

import android.os.Handler;

/**
 * @author Eric Boeck
 *
 */
public class MonitorMic
{
	private boolean shouldStop = false;
	public Handler maxHandler;
	public int sampleRate, bufferRequest;
	protected GetAudioFromMic micAudio;
	
	
	/**
	* the constructor requires the callback handler to be called
	* @return
	*/
	MonitorMic(Handler aHandler, int sampleRate, int bufferRequest)
	{
		maxHandler = aHandler;
		this.sampleRate = sampleRate;
		this.bufferRequest = bufferRequest;
	}
	/**
	* create the runnable part so that the meaty parts can be put on a thread
	* @return
	*/
	public Runnable audioRunnable = new Runnable() {

		public void run() {

			// instantiate the class that creates and reads the hardware
			micAudio = new GetAudioFromMic(maxHandler, sampleRate, bufferRequest);
//			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO );
//			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

		    //  and ......  GO!
		    micAudio.startRecording();
		    // infinite (but controlled) loop
			while (!shouldStop)
			{
			} // this ends the loop. If should stop is true, then this drops out, and the thread stops
			// and stop the hardware from recording
			micAudio.stopRecording();
		}
	};

	/**
	 *  call this to stop the thread
	 *  @return
	 */
	public void StopMonitoringMic()
	{
		shouldStop = true;
	}

	/**
	 *  call this to reset the stopper for the thread
	 *  @return
	 */
	public void StartMonitoringMic()
	{
		shouldStop = false;
	}
	
}
