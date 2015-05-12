package com.dam.meteodam;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity {

	private JSONObject json;
	private String city = "Donostia";
	private int numDays = 0;
	private static ProgressDialog progressDialog;
	MyTabListener<Fragment> fragmentDataListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ActionBar actBar = getActionBar();
		actBar.setHomeButtonEnabled(true);

		actBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		Tab dataTab = actBar.newTab();
		Tab mapTab = actBar.newTab();

		fragmentDataListener = new MyTabListener(this, R.id.contenedor,
				FragmentData.class);

		dataTab.setText(getString(R.string.tab_data)).setTabListener(
				fragmentDataListener);
		mapTab.setText(getString(R.string.tab_map)).setTabListener(
				new MyTabListener(this, R.id.contenedor, FragmentMap.class));

		actBar.addTab(dataTab);
		actBar.addTab(mapTab);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void setJSON(JSONObject json) {
		this.json = json;

	}

	public JSONObject getJSON() {
		return this.json;

	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {

		FragmentData dataFragment = (FragmentData) fragmentDataListener
				.getFragment();
		if (dataFragment != null) {
			this.city = city;
			dataFragment.setCity(city);
		}
	}

	public int getNumDays() {
		return numDays;
	}

	public void setNumDays(int numDays) {

		FragmentData dataFragment = (FragmentData) fragmentDataListener
				.getFragment();
		if (dataFragment != null) {
			this.numDays = numDays;
			dataFragment.setNumDays(numDays);
		}

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {

		FragmentData dataFragment = (FragmentData) fragmentDataListener
				.getFragment();
		if (dataFragment != null) {

			if (json != null)
				outState.putString("JSON", json.toString());
			outState.putString("city", city);
			outState.putInt("numDays", numDays);
			ArrayList<String> forecastList = dataFragment.getForecastList();
			if (forecastList != null)
				outState.putStringArrayList("forecast", forecastList);
		}
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		FragmentData dataFragment = (FragmentData) fragmentDataListener
				.getFragment();

		try {
			String jsonStr = savedInstanceState.getString("JSON");
			if (jsonStr != null)
				this.json = new JSONObject(savedInstanceState.getString("JSON"));
		} catch (JSONException e) {
			Log.e("JSON Parse", "onRestoreInstanceState:" + e.getMessage());
		}
		this.setCity(savedInstanceState.getString("city"));
		ArrayList<String> list = savedInstanceState
				.getStringArrayList("forecast");
		dataFragment.setForecastList(list);

		int days = savedInstanceState.getInt("numDays");
		this.setNumDays(days);

	}

	public void showProgessDlg(int titleID) {
		if (progressDialog == null || !progressDialog.isShowing()) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setCancelable(false);
			progressDialog.setTitle(this.getString(titleID));
			progressDialog.show();
		}
	}

	public void hideProgessDlg() {
		if (progressDialog != null)
			progressDialog.dismiss();
	}

}
