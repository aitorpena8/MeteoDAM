package com.dam.meteodam;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class DetailsActvity extends Activity {
	JSONObject jsonObj;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_details_actvity);
		Intent intent = getIntent();
		String jsonStr = intent.getStringExtra("weatherJSON");
		String dateStr = intent.getStringExtra("dateStr");

		if (jsonStr != null) {
			try {
				JSONObject weatherObj = new JSONObject(jsonStr);
				JSONObject tempObj = weatherObj.getJSONObject("temp");
				if (tempObj != null) {
					String avg = tempObj.getString("day");
					String max = tempObj.getString("max");
					String min = tempObj.getString("min");
					String humid = weatherObj.getString("humidity");
					String press = weatherObj.getString("pressure");
					String wind = weatherObj.getString("speed");
					
					TextView dateView = (TextView) findViewById(R.id.valDate);
					dateView.setText(dateStr);
					TextView avgView = (TextView) findViewById(R.id.valDayAvg);
					avgView.setText(avg);
					TextView maxView = (TextView) findViewById(R.id.valDayMax);
					maxView.setText(max);
					TextView minView = (TextView) findViewById(R.id.valDayMin);
					minView.setText(min);
					TextView humidView = (TextView) findViewById(R.id.valHumid);
					humidView.setText(humid);
					TextView pressView = (TextView) findViewById(R.id.valPress);
					pressView.setText(press);
					TextView windView = (TextView) findViewById(R.id.valWind);
					windView.setText(wind);
				}

			} catch (JSONException jse) {
				Log.e("DetailsActivity:JSONException", jse.getMessage());
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.details_actvity, menu);
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
}
