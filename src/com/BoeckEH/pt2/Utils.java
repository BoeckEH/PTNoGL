package com.BoeckEH.pt2;

import java.util.Date;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
 
public class Utils {
 
    private static final int MESSAGE_ALERT = 1;
    private static final int CONFIRM_ALERT = 2;
    private static final int DECISION_ALERT = 3;
 
    public static void messageAlert(Context ctx, String title, String message) {
        showAlertDialog(MESSAGE_ALERT, ctx, title, message, null, "OK");
    }
 
    public static void confirmationAlert(Context ctx, String title, String message, DialogInterface.OnClickListener callBack) {
        showAlertDialog(CONFIRM_ALERT, ctx, title, message, callBack, "OK");
    }
 
    public static void decisionAlert(Context ctx, String title, String message, DialogInterface.OnClickListener posCallback, String... buttonNames) {
        showAlertDialog(DECISION_ALERT, ctx, title, message, posCallback, buttonNames);
    }
 
    public static void showAlertDialog(int alertType, Context ctx, String title, String message, DialogInterface.OnClickListener posCallback, String... buttonNames) {
        if ( title == null ) title = ctx.getResources().getString(R.string.app_name);
        if ( message == null ) message = "default message";
 
        final AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle(title)
                .setMessage(message)
 
                // false = pressing back button won't dismiss this alert
                .setCancelable(false)
 
                // icon on the left of title
                .setIcon(android.R.drawable.ic_dialog_alert);
 
        switch (alertType) {
            case MESSAGE_ALERT:
                break;
 
            case CONFIRM_ALERT:
                builder.setPositiveButton(buttonNames[0], posCallback);
                break;
 
            case DECISION_ALERT:
                break;
        }
 
        builder.setNegativeButton(buttonNames [buttonNames.length - 1], new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).create().show();
    }
    
    public void SaveStuff(Context context)
    {
    	 SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    	  SharedPreferences.Editor editor = preferences.edit();
    	  editor.putString("Name","Harneet");
    	  editor.putString("text", "what we are saving");
    	  editor.putInt("selection-start", 4);
    	  editor.putInt("selection-end", 8);
    	  editor.commit();
    	  // or
    	  SharedPreferences prefs = context.getSharedPreferences("com.example.app", Context.MODE_PRIVATE);
    	  long dt = new Date().getTime();
		  String dateTimeKey = "com.example.app.datetime";
    	  prefs.edit().putLong(dateTimeKey, new Date().getTime()).commit();
    
    }
    
    public void GetStuff(Context context)
    {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
	    String name = preferences.getString("Name","");

		// or
		SharedPreferences prefs = context.getSharedPreferences("com.example.app", Context.MODE_PRIVATE);
		String dateTimeKey = "com.example.app.datetime";

    	// use a default value using new Date()
    	long l = prefs.getLong(dateTimeKey,       new Date().getTime()); 
//    							^keyname string     ^default if doesn't exist
    }

    
    public void MakeAlertDialog(Context context, final CharSequence[] items, boolean[] selected)
    {
    	// items = {"Red", "Green", "Blue"};
    	// selected = {true, false, true};
    	 
    	AlertDialog.Builder builder = new AlertDialog.Builder(context);
    	builder.setTitle("Pick colors")
    	    .setMultiChoiceItems(items, selected, new DialogInterface.OnMultiChoiceClickListener() {
    	    public void onClick(DialogInterface dialogInterface, int item, boolean b) {
    	        Log.d("Myactivity", String.format("%s: %s", items[item], b));
    	    }
    	});
    	 
    	builder.create().show();
    }

	public void SetAndStoreTimerSetting(MenuItem item)
	{
		AlertDialog.Builder popupBuilder = new AlertDialog.Builder(null);
		LinearLayout myLL = new LinearLayout(null);
		EditText myEditText = new EditText(null);
		myEditText.setGravity(Gravity.CENTER_HORIZONTAL);
		myEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
		myEditText.setInputType(InputType.TYPE_CLASS_PHONE);
		popupBuilder.setView(myEditText);
		popupBuilder.setTitle("Enter Time in Minutes to Practice");
		popupBuilder.setCancelable(true);
		popupBuilder.setNegativeButton("Don't Save",new DialogInterface.OnClickListener() 
		{
			@Override
			public void onClick(DialogInterface dialog,int id) 
			{
				// if this button is clicked, just close
				// the dialog box and do nothing
				dialog.cancel();
			}
		});
		popupBuilder.setPositiveButton("Save Now",new DialogInterface.OnClickListener() 
		{
			@Override
			public void onClick(DialogInterface dialog,int id) 
			{
				
				
				// if this button is clicked, just close
				// the dialog box and do nothing
				dialog.cancel();
			}
		});
		popupBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() {
			
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER))
		        {
		           return true;
		        }
				return false;
			}
		});
		AlertDialog myAD = popupBuilder.create();
		myAD.show();
	}
	
	public void MsgBox(String sMessage)
	{
		AlertDialog ad = new AlertDialog.Builder(null).create();  
		ad.setCancelable(false); // This blocks the 'BACK' button  
		ad.setMessage(sMessage);  
		ad.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {  
		    @Override  
		    public void onClick(DialogInterface dialog, int which) {  
		        dialog.dismiss();                      
		    }  
		});  
		ad.show();
	}



    
}


/*
 * So, the frequency for each string is (in Hz):
E - 82.4
A - 110
D - 146.8
G - 196
B - 246.9
E - 329.6

*/
