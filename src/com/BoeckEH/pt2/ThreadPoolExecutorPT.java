package com.BoeckEH.pt2;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.util.Log;

public class ThreadPoolExecutorPT {

	private static final String TAG = "MyActivity";
	int poolSize = 2;
    int maxPoolSize = 2;
    long keepAliveTime = 10;
    final ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(5);
    ThreadPoolExecutor threadPool = null;
    
    MonitorMic monTheMic; 
    
    public void runTask(Runnable task)
    {
        // set priority
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO );
		try 
		{
			threadPool.execute(task);
		}catch (RejectedExecutionException e)
        {
        	
        }
    }

    public ThreadPoolExecutorPT()
    {
        try
        {
    	threadPool = new ThreadPoolExecutor(poolSize, maxPoolSize,
                keepAliveTime, TimeUnit.SECONDS, queue);
        } catch (Exception e)
        {
        	Log.v(TAG, e.getMessage());
        }
 
    }

    public void shutDown()
    {
        threadPool.shutdown();
    }
    
    public void shutDownNow()
    {
    	threadPool.shutdownNow();
    }
    
    public int GetQueueLength()
    {
    	return queue.size();
    }
 
}
