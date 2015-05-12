package com.dam.meteodam;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class FragmentMap extends Fragment implements OnMapReadyCallback {
	private static final String ICON_PRE = "w";// http://openweathermap.org/img/w/
	private static final String ICON_SUB = ".png";;
	GoogleMap gMap;
	View view;
	private double defLat = 41.40338;
	double defLon = 2.17403;

	private float zoom = 10.0f;

	JSONObject json = null;




	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		if (view == null) {
			view = inflater.inflate(R.layout.fragment_map, container, false);
			MapFragment mapFragment = (MapFragment) getFragmentManager()
					.findFragmentById(R.id.map);
			mapFragment.getMapAsync(this);
		} else {
			if (gMap != null)
				onMapReady(gMap);
		}
		return view;
	}

	public void setLocation(double lat, double lon, float zoom) {
		LatLng latLng = new LatLng(lat, lon);
		gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
	}

	public Address getLatLongByCityName(String city) {
		List<Address> locations;

		Address location = null;

		if (city != null) {
			Geocoder geoCoder = new Geocoder(getActivity());
			try {
				locations = geoCoder.getFromLocationName(city, 1);
				location = locations.get(0);
			} catch (IOException ioe) {
				Log.e("FragmentMap", "ioe: " + ioe.getMessage());
			}
		}
		return location;

	}

	public void onMapReady(GoogleMap map) {
		gMap = map;
		json = ((MainActivity) getActivity()).getJSON();

		String city = ((MainActivity) getActivity()).getCity();
		Address location = getLatLongByCityName(city);
		double lon = defLon;
		double lat = defLat;
		if (location != null) {
			lon = location.getLongitude();
			lat = location.getLatitude();
		}
		setLocation(lat, lon, zoom);
		if (json != null) {
			try {
				JSONArray jsonArray = json.getJSONArray("list");

				if (jsonArray != null && jsonArray.length() > 0) {
					JSONObject obj = (JSONObject) jsonArray.get(0);
					if (obj == null)
						return;

					JSONArray weatherArr = obj.getJSONArray("weather");
					if (weatherArr == null || weatherArr.length() < 1)
						return;
					JSONObject weatherObj = (JSONObject) weatherArr.get(0);
					String iconStr = (String) weatherObj.getString("icon");
					String iconPath = ICON_PRE + iconStr;// + ICON_SUB;

					int iconId = getResources().getIdentifier(iconPath,
							"drawable", "com.dam.meteodam");
					;
					if (iconId != -1)
						setMarker(lat, lon, iconId);

				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}/*
			 * catch (MalformedURLException e) { // TODO Auto-generated catch
			 * block e.printStackTrace(); } catch (IOException e) { // TODO
			 * Auto-generated catch block e.printStackTrace(); }
			 */
		}

	}

	public void setMarker(double lat, double lon, int iconId) {
		LatLng center = new LatLng(lat, lon);
		gMap.addMarker(new MarkerOptions()
				.icon(BitmapDescriptorFactory.fromResource(iconId))
				.position(center).flat(true));
	}
}
