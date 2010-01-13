package com.lolitech.android.phone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import android.os.Handler;

public class CallScriptReceiver extends BroadcastReceiver {

	/* Log tag */
	static private final String TAG = "CallScriptReceiver";
	
	/* Exported constants */
	static public final String BYPASS_PREFIX = "**";

	int script_retval = 0;
	String error_message = ""; 
	Process process = null;
	String progarray[] = {"sh", "/sdcard/callscript.sh", ""};
	
	@Override
	public void onReceive(Context ctxt, Intent intent) {
		
		String action = intent.getAction();
		Log.d(TAG, "action " + action + " received");
		final Context context = ctxt;
		/* check connectivity and update notification on every received vent */
		boolean online = CallScriptUtil.updateNotification(context);
		
		/* and do this if it's an outgoing call */
		if (action.equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
			String number = getResultData(); 
			if (number == null) return;
			
			Log.d(TAG, "number = " + number);
			if (number.startsWith(BYPASS_PREFIX)) {
				setResultData(number.substring(BYPASS_PREFIX.length()));
			} else if (online /*&& number.startsWith("+")*/) {
				final Handler uiThreadCallback = new Handler();
				
				final Runnable ShowError = new Runnable() {
					public void run() {
						Toast toast = Toast.makeText(
								context,
								context.getString(R.string.toast_error) + " :\n" + error_message,
								Toast.LENGTH_LONG);
						toast.show();
					}
				};
				
				final Runnable ShowResult = new Runnable() {
					public void run() {
						Toast toast = Toast.makeText(
								context,
								context.getString(R.string.toast_done) + " (" + String.valueOf(script_retval) + ")",
								Toast.LENGTH_SHORT);
						toast.show();		
					}
				};
				
				setResultData(null);
				
				Toast toast = Toast.makeText(
						context, 
						R.string.toast_progress,
						Toast.LENGTH_SHORT);
				toast.show();
				
				progarray[2] = number;
				
				new Thread() {
					@Override public void run() {
						try {
							process = Runtime.getRuntime().exec(progarray);
							script_retval = process.waitFor();
							process.destroy();
						} catch (Exception e) {
							error_message = e.getMessage();
							uiThreadCallback.post(ShowError);
						}
						uiThreadCallback.post(ShowResult);
					}
				}.start();
			} else {
				setResultData(number);
			}
		}
	}
}