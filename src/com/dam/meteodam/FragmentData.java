package com.dam.meteodam;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

public class FragmentData extends Fragment implements OnClickListener {
	static LocationManager locationMng;
	private static SimpleDateFormat datFrmt = new SimpleDateFormat(
			"yyyy/MM/dd HH:mm", Locale.US);
	static TextView valLocation;
	static Spinner spinnerDay;
	Button sendBtn;
	ImageButton syncBtn, gpsBtn;
	private static final String UPDATE_LOCATION = "com.dam.meteodam.updatelocation";
	private static final String ALARM_ACTION = "com.dam.meteodam.alarmAction";
	private ArrayList<Date> dateList;
	private ArrayList<String> forecastList;

	private static final String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily";

	private static final int SPINER_VALS[] = { 1, 2, 3 };
	static FragmentForecast fragmentForecast;
	static Button detailsBtn;
	private static String JSONStr = null;
	private static MainActivity mainActivity;
	private static EditText inAlarmTime;
	private static Dialog alarmDialog;
	private static int alarmTime = -1;
	private static PendingIntent alarmIntent;
	private static AlarmManager alarmManager;
	private static Button alarmOkBtn, alarmCancelBtn, alarmClsBtn;
	private static JSONResourceReader jsonRsrcReader;
	// private static final int LOCATION_UPDATE_DISTANCE = 5;

	private static AlarmReceiver receiver;
	private static LocationUpdateReceiver locationReceiver;
	private int selItem = -1;
	private View view;
	private Date updateDate;
	private TextView valUpdt;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		mainActivity = (MainActivity) getActivity();
		view = inflater.inflate(R.layout.fragment_data, container, false);
		valLocation = (EditText) view.findViewById(R.id.valLocation);
		valUpdt = (TextView) view.findViewById(R.id.updTxt);
		valLocation.setText(mainActivity.getCity());
		spinnerDay = (Spinner) view.findViewById(R.id.valDay);

		sendBtn = (Button) view.findViewById(R.id.btnSend);
		sendBtn.setOnClickListener(this);
		// detailsBtn = (Button) view.findViewById(R.id.btnDetails);
		// detailsBtn.setOnClickListener(this);
		syncBtn = (ImageButton) view.findViewById(R.id.btnSync);
		syncBtn.setOnClickListener(this);

		gpsBtn = (ImageButton) view.findViewById(R.id.btnGPS);
		gpsBtn.setOnClickListener(this);

		String[] arraySpinner = { getString(R.string.sp_1day),
				getString(R.string.sp_2day), getString(R.string.sp_3day) };
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_spinner_item, arraySpinner);

		spinnerDay.setAdapter(adapter);
		int pos = mainActivity.getNumDays();
		spinnerDay.setSelection(pos);
		showUpdateDate();
		jsonRsrcReader = new JSONResourceReader(mainActivity, this, BASE_URL);
		fragmentForecast = new FragmentForecast();
		fragmentForecast.setParent(this);
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransation = fragmentManager
				.beginTransaction();
		fragmentTransation.add(R.id.forecastFrame, fragmentForecast);
		fragmentTransation.commit();

		/*
		 * fragmentForecast=(FragmentForecast)fragmentManager.findFragmentById(R.
		 * id.forecastFrame); if (forecast != null)
		 * fragmentForecast.addForecast(forecast);
		 */

		// initLocationTracker();
		return view;

	}

	private void initLocationTracker() {
		locationMng = (LocationManager) mainActivity
				.getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		criteria.setAltitudeRequired(false);
		criteria.setSpeedRequired(false);
		String bestProvider = locationMng.getBestProvider(criteria, true);
		int locationUpdateRC = 0;
		int t = 5000;// miliseconds int distance = 5; //metters
		int flags = PendingIntent.FLAG_UPDATE_CURRENT;
		Intent intent = new Intent(mainActivity, LocationUpdateReceiver.class);
		intent.setAction(UPDATE_LOCATION);
		PendingIntent pi = PendingIntent.getBroadcast(mainActivity,
				locationUpdateRC, intent, flags);
		registerLocationReceiver();
		Log.i("Location", bestProvider);

		locationMng.requestLocationUpdates(bestProvider, 0, 0, pi);
		mainActivity.showProgessDlg(R.string.progressGPSDlg_title);

	}

	public static void getWeaherInf() {
		jsonRsrcReader.download();
	}

	private void createAlarmDialog() {
		alarmDialog = new Dialog(mainActivity);
		alarmDialog.setContentView(R.layout.dialog_alarm);
		alarmDialog
				.setTitle(mainActivity.getString(R.string.dialog_alarm_name));
		alarmOkBtn = (Button) alarmDialog.findViewById(R.id.btnAlarmOk);
		alarmCancelBtn = (Button) alarmDialog.findViewById(R.id.btnAlarmCancel);

		alarmClsBtn = (Button) alarmDialog.findViewById(R.id.btnAlarmCls);
		inAlarmTime = (EditText) alarmDialog.findViewById(R.id.inAlarmTime);
		if (alarmTime != -1) {
			inAlarmTime.setText(alarmTime);
			alarmOkBtn.setEnabled(false);
		} else {
			alarmCancelBtn.setEnabled(false);

		}
		alarmClsBtn.setOnClickListener(this);
		alarmOkBtn.setOnClickListener(this);
		alarmCancelBtn.setOnClickListener(this);
	}

	public void openDetailsActivity() {
		Intent intent = new Intent(getActivity(), DetailsActvity.class);
		if (JSONStr != null) {
			try {
				JSONObject json = new JSONObject(JSONStr);
				JSONArray jsonArr = json.getJSONArray("list");
				if (jsonArr != null && selItem != -1
						&& selItem < jsonArr.length()) {
					JSONObject obj = (JSONObject) jsonArr.get(selItem);
					Date date = dateList.get(selItem);
					String objStr = obj.toString();
					intent.putExtra("weatherJSON", objStr);
					intent.putExtra("dateStr", datFrmt.format(date));
					startActivity(intent);
				}

			} catch (JSONException jsone) {
				Log.e("openDetailsActivity():JSONException", jsone.getMessage());
			}

		}

	}

	public void saveData() {
		String cityName = valLocation.getText().toString();
		mainActivity.setCity(cityName);
		int numDays = spinnerDay.getSelectedItemPosition();
		mainActivity.setNumDays(numDays);

	}

	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		saveData();
		super.onDetach();
	}

	private void initJSONReader() {
		if (jsonRsrcReader != null) {
			String cityName = valLocation.getText().toString();
			int numDays = SPINER_VALS[spinnerDay.getSelectedItemPosition()];
			Log.i("TRACE", "City name: " + cityName + ", Num. days: " + numDays);

			jsonRsrcReader.setCity(cityName);
			jsonRsrcReader.setNumDays(numDays);
		}

	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.btnSend:
			initJSONReader();
			getWeaherInf();
			break;

		// case R.id.btnDetails:
		// openDetailsActivity();
		//
		// break;

		case R.id.btnSync:
			// TODO Crear dialogo para configurar alarma que actualizara los
			// datos
			if (alarmDialog == null)
				createAlarmDialog();
			alarmDialog.show();
			break;
		case R.id.btnGPS:
			initLocationTracker();

			break;

		case R.id.btnAlarmCancel:
			alarmManager.cancel(alarmIntent);
			unregisterReceiver();
			if (alarmOkBtn != null)
				alarmOkBtn.setEnabled(true);
			break;

		case R.id.btnAlarmOk:
			alarmManager = (AlarmManager) mainActivity
					.getSystemService(Context.ALARM_SERVICE);
			int alarmType = AlarmManager.RTC_WAKEUP;
			Intent intent1 = new Intent(ALARM_ACTION);
			initJSONReader();
			alarmIntent = PendingIntent.getBroadcast(mainActivity, 0, intent1,
					0);
			alarmTime = Integer.parseInt(inAlarmTime.getText().toString());
			int millis = alarmTime * 60 * 1000;
			alarmManager.setRepeating(alarmType, millis, millis, alarmIntent);
			registerReceiver();
			alarmCancelBtn.setEnabled(true);
			alarmOkBtn.setEnabled(false);
			break;

		case R.id.btnAlarmCls:
			alarmDialog.dismiss();
			break;

		}

	}

	private void registerReceiver() {
		receiver = new AlarmReceiver(mainActivity);
		IntentFilter filter = new IntentFilter(ALARM_ACTION);
		mainActivity.registerReceiver(receiver, filter);

	}

	private void unregisterReceiver() {
		mainActivity.unregisterReceiver(receiver);
	}

	private void registerLocationReceiver() {
		locationReceiver = new LocationUpdateReceiver(mainActivity);
		IntentFilter filter = new IntentFilter(UPDATE_LOCATION);
		mainActivity.registerReceiver(locationReceiver, filter);

	}

	public void unregisterLocationReceiver() {
		mainActivity.unregisterReceiver(locationReceiver);
	}

	public void setForecastList(ArrayList<String> list) {
		this.forecastList = list;

	}

	public void updateForecast() {
		fragmentForecast.addForecast(forecastList);

	}

	public static void setJSONStr(String str) {
		// detailsBtn.setEnabled(true);
		JSONStr = str;

	}

	public void setSelectedPos(int pos) {
		selItem = pos;
		//
		// if (selItem == -1)
		// detailsBtn.setEnabled(false);
		// else
		// detailsBtn.setEnabled(true);

	}

	public static void setJSON(JSONObject json) {
		mainActivity.setJSON(json);

	}

	public void setUpdateDate(Date date) {
		this.updateDate = date;
		showUpdateDate();

	}

	public void showUpdateDate() {
		if (updateDate != null) {
			String str = getString(R.string.updTxt) + ":"
					+ datFrmt.format(updateDate);
			valUpdt.setText(str);
		}
	}

	public Date getUpdateDate() {
		return this.updateDate;

	}

	public void setCity(String city) {
		valLocation.setText(city);
	}

	public void setNumDays(int numDays) {
		spinnerDay.setSelection(numDays);
	}

	public void setDateList(ArrayList<Date> list) {
		dateList = list;

	}

	public ArrayList<String> getForecastList() {
		return forecastList;
	}

}
