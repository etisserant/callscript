package com.lolitech.android.phone;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
//import android.telephony.TelephonyManager;

public class CallScriptUtil {

	/* Class variables */
	static private boolean mEnable = false;
	static private boolean mMobile = false;
	static private boolean mOnline = false;

	static public synchronized boolean checkConnectivity(Context context) {

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		mEnable = prefs.getBoolean(CallScriptPrefs.CALLSCRIPT_PREF_ENABLE, false);
		mMobile = prefs.getBoolean(CallScriptPrefs.CALLSCRIPT_PREF_MOBILE, false);
		mOnline = false;

		ConnectivityManager mCmgr = (ConnectivityManager) context
				.getSystemService(Service.CONNECTIVITY_SERVICE);

		NetworkInfo ni = mCmgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (ni != null && ni.isConnected()) { /* wifi is on */
			mOnline = true;
		} else {
			if (mMobile) {
				/* if mobile is active and EDGE or better, we're good */
				ni = mCmgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
				if (ni != null && ni.isConnected()) {
					//mOnline = (ni.getSubtype() >= TelephonyManager.NETWORK_TYPE_EDGE);
					mOnline = true;
				}
			}
		}
		return mOnline;
	}

	static public boolean updateNotification(Context context) {

		// check the current system status
		checkConnectivity(context);

		// clicking this will open the prefs pane
		PendingIntent pi = PendingIntent.getActivity(context, 0,
				new Intent(context, CallScriptPrefs.class), 0);

		NotificationManager mNmgr = (NotificationManager) context
				.getSystemService(Service.NOTIFICATION_SERVICE);

		int text, icon;
		if (mOnline && mEnable) {
			text = R.string.notify_callscript_enabled;
			icon = R.drawable.ic_callscript_on;
		} else if (mEnable) {
			text = R.string.notify_callscript_offline;
			icon = R.drawable.ic_callscript_offline;
		} else {
			text = R.string.notify_callscript_disabled;
			icon = R.drawable.ic_callscript_off;
		}

		String str1 = context.getString(text);
		String str2 = context.getString(R.string.notify_summary);

		Notification n = new Notification();
		n.icon = icon;
		n.flags |= Notification.FLAG_ONGOING_EVENT;
		n.when = 0;
		n.setLatestEventInfo(context, str1, str2, pi);
		mNmgr.notify(0, n);
		
		return mOnline;
	}
}
