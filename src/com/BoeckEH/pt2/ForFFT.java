/**
 * 
 */
package com.BoeckEH.pt2;

/**
 * @author eric
 *
 */
public class ForFFT {
	/**
	 * 	/**
	 * Convert short[] to double[] (-1<0<1) buffer array for FFT processing
	 * @param bufferSizeInBytes
	 * @param audioData
	 * @param amplification
	 */
	public double[] ConvertShortToDouble(int bufferSizeInBytes, short[] audioData, double amplification)
	{
	//Conversion from short to double
	double[] micBufferData = new double[bufferSizeInBytes];//size may need to change
	    final int bytesPerSample = 2; // As it is 16bit PCM
	    for (int index = 0, floatIndex = 0; index < bufferSizeInBytes - bytesPerSample + 1; index += bytesPerSample, floatIndex++) {
	        double sample = 0;
	        for (int b = 0; b < bytesPerSample; b++) {
	            int v = audioData[index + b];
	            if (b < bytesPerSample - 1 || bytesPerSample == 1) {
	                v &= 0xFF;
	            }
	            sample += v << (b * 8);
	        }
	        double sample32 = amplification * (sample / 32768.0);
	        micBufferData[floatIndex] = sample32;
	    }
	    return micBufferData;
	}

	/**
	 * Convert short[] to double[] (-1<0<1) buffer array for FFT processing, amplification = 1.0
	 * @param bufferSizeInBytes
	 * @param audioData
	 */
	public double[] ConvertShortToDouble(int bufferSizeInBytes, short[] audioData)
	{
	//Conversion from short to double
	double[] micBufferData = new double[bufferSizeInBytes];//size may need to change
	    final int bytesPerSample = 2; // As it is 16bit PCM
	    for (int index = 0, floatIndex = 0; index < bufferSizeInBytes - bytesPerSample + 1; index += bytesPerSample, floatIndex++) {
	        final double amplification = 1.0;
	        double sample = 0;
	        for (int b = 0; b < bytesPerSample; b++) {
	            int v = audioData[index + b];
	            if (b < bytesPerSample - 1 || bytesPerSample == 1) {
	                v &= 0xFF;
	            }
	            sample += v << (b * 8);
	        }
	        double sample32 = amplification * (sample / 32768.0);
	        micBufferData[floatIndex] = sample32;
	    }
	    return micBufferData;
	}

	public double ComputeFrequency(int arrayIndex, int fftOutWindowSize, int sampleRate) {
	    return ((1.0 * sampleRate) / (1.0 * fftOutWindowSize)) * arrayIndex;
	}
	
	

}
