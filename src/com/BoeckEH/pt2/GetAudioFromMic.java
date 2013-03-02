package com.BoeckEH.pt2;

import java.util.Arrays;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.AudioRecord.OnRecordPositionUpdateListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * @author Eric Boeck  2/2/2013
 *
 */
public class GetAudioFromMic {
	
	private AudioRecord aRecorder; 
	
	private int audioSource = MediaRecorder.AudioSource.MIC;
	private int channelConfig = AudioFormat.CHANNEL_IN_MONO;
	private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
	private int minBufferSize;
	private int bufferReadNum;
	public static short[] buffer;
	public Handler mainThreadBufFullHandler;

	/**
	 * constructor ... no params, set up the audio recorder for monitoring 
	 * @return
	 */
	GetAudioFromMic(Handler bufFullHandler, int sampleRateInHz) {
		// determine the min buffer required
        minBufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
        // create the recorder
        aRecorder = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, minBufferSize * 10);
        // allocate a buffer to store the data in 
		buffer = new short[minBufferSize];
		// set it to 0 (not really necessary but can tell if allocated and then if reading 
		Arrays.fill(buffer, (short) 0);
		// set the call back handler 
		aRecorder.setRecordPositionUpdateListener(myListener);
		// and how often to call the handler
		aRecorder.setPositionNotificationPeriod(minBufferSize);
		// set a callback handler
		mainThreadBufFullHandler = bufFullHandler;
		
		
	}
	
	// handler?  listener?  whatever  :)
	private OnRecordPositionUpdateListener myListener = new OnRecordPositionUpdateListener() {
		
		public void onPeriodicNotification(AudioRecord recorder) {
			// on the notification, go read the recorder into the buffer
			bufferReadNum = recorder.read(buffer, 0, minBufferSize);
		    // init some vars
		    int maxSoundLevel = 0;
			// used to determine how long this loops takes
			long l_processTime = System.currentTimeMillis();

			// loop it looking for the |max|
//			for (int ii=0; ii < minBufferSize; ii++)
//			{
//				if (Math.abs(buffer[ii]) > maxSoundLevel) maxSoundLevel = Math.abs(buffer[ii]);
//			}
			// the bundle used as the message payload to the handler
			Bundle maxValueDataBundle = new Bundle();
			Message setMaxValMessage = new Message();
			// Bundle the max sound level into the capsule
			maxValueDataBundle.putInt("MaxVal",maxSoundLevel);
			// wait until the last millisec to calc the time this routine takes
			l_processTime = System.currentTimeMillis() - l_processTime;
			// Bundle the max sound level into the capsule
			maxValueDataBundle.putLong("ProcTime", l_processTime);
			// put the buffer into the capsule
			maxValueDataBundle.putShortArray("MicBuffer", buffer);
			// put how many samples there are into the capsule
			maxValueDataBundle.putInt("Samples", bufferReadNum);
			// put the capsule into the payload
			setMaxValMessage.setData(maxValueDataBundle);
			// and launch
			mainThreadBufFullHandler.sendMessage(setMaxValMessage);
			// clear the max for the next max calc
			maxSoundLevel = 0;
			
		}
		
		// not using this at the moment
		public void onMarkerReached(AudioRecord recorder) {
			
		}
	};
	
	
	/**
	 * Call this to start the recorder working.  It reads the recorder once to kick start it
	 * @return
	 */
	public short[] startRecording() {

		
        try {
			aRecorder.startRecording();
			// just do it once, and it's self sustaining it would seem
			bufferReadNum = aRecorder.read(buffer, 0, minBufferSize);
		} catch (IllegalStateException e) {
			aRecorder.stop();
			e.printStackTrace();
			Log.d("GetAudio", "GetAudio failed");
		}
		return buffer;

    }
	
	/**
	 * stops the recorder, and release the resource
	 * @return
	 */
	public void stopRecording(){
		aRecorder.stop();
//		aRecorder.release();
	}
	
	/**
	 * allows the caller to get what should be the buffer minimum size
	 * @return
	 */
	public int GetMinBufferSize() 
	{ 
		return minBufferSize; 
	}
	
	/**
	 * The buffer is updated via the callback above.  This grabs it async and returns it for inspection 
	 * @return
	 */
	public short[] getAudioBuffer(){
		return buffer;
	}
	
	public int GetBufferReadNum()
	{
		return bufferReadNum;
	}

}

