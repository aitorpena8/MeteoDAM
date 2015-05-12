package com.dam.meteodam;

import java.io.IOException;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

public class LocationUpdateReceiver extends BroadcastReceiver {
	private MainActivity activity;
	public LocationUpdateReceiver(MainActivity activity) {
		super();
		this.activity = activity;
	}


	@Override
	public void onReceive(Context context, Intent intent) {

		String key = LocationManager.KEY_LOCATION_CHANGED;
		Location location = (Location) intent.getExtras().get(key);
		activity.hideProgessDlg();
		FragmentData fragmentData=(FragmentData)activity.getFragmentManager().findFragmentById(R.id.dataFragment);
		fragmentData.unregisterLocationReceiver();
		Geocoder geoCoder = new Geocoder(context);
		List<Address> adresses;
		

		try {
			adresses = geoCoder.getFromLocation(location.getLatitude(),
					location.getLongitude(), 1);

			if (adresses != null) {
				Address address = adresses.get(0);
				activity.setCity(address.getLocality());

			}
		} catch (IOException e) {
			Log.e("GEOCODER", "Error getting address from location");
		}

	}

}
