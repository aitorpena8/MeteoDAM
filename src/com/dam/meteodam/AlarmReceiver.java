package com.dam.meteodam;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {
	private MainActivity activity;

	public AlarmReceiver(MainActivity activity) {
		super();
		this.activity = activity;
		
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("TRACE", "onReceive" + intent.toString());
		activity.hideProgessDlg();
		FragmentData.getWeaherInf();
	}
}
