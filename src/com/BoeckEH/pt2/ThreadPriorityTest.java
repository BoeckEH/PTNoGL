package com.BoeckEH.pt2;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;


public class ThreadPriorityTest {

    public final String TAG="TestPriority";

    public void ShowThreadPrority(String ExtraStuff) {

        int tid=android.os.Process.myTid();

        Log.d(TAG,"priority OS: " + ExtraStuff + " = " + android.os.Process.getThreadPriority(tid));
        Log.d(TAG,"priority Thread: "+ ExtraStuff  +" = "+ Thread.currentThread().getPriority());

    }
	
    public void ShowThreadPrority(int changeThePriority, String ExtraStuff) {

        int tid=android.os.Process.myTid();

        Log.d(TAG,"priority before change OS: " + ExtraStuff + " = " + android.os.Process.getThreadPriority(tid));
        Log.d(TAG,"priority before change Thread: " + ExtraStuff + " = " + Thread.currentThread().getPriority());
        android.os.Process.setThreadPriority(changeThePriority);
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        Log.d(TAG,"priority after change OS: " + ExtraStuff + " = " + android.os.Process.getThreadPriority(tid));
        Log.d(TAG,"priority after change Thread: " + ExtraStuff + " = " + Thread.currentThread().getPriority());
    }

    
}
