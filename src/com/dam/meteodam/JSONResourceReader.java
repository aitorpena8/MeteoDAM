package com.dam.meteodam;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

public class JSONResourceReader {
	DownloadManager downloadMng;

	private FragmentData fragment = null;
	private String baseUrl;
	private String cityName;
	private int numDays;
	Uri uri;
	Context context;
	IntentFilter filter;
	long ref;
	BroadcastReceiver receiver;
	static final String DATE_FORMAT = "dd-MM-yyyy hh:mm";
	ProgressDialog progress;
	MainActivity activity;

	public JSONResourceReader(Context context, FragmentData frag, String baseUrl) {
		this.context = context;
		this.baseUrl = baseUrl;
		String downloadService = Context.DOWNLOAD_SERVICE;
		downloadMng = (DownloadManager) context
				.getSystemService(downloadService);
		this.fragment = frag;
		this.activity = (MainActivity) frag.getActivity();
		initReceiver();
		filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);

	}

	public void setCity(String city) {
		this.cityName = city;

	}

	public void setNumDays(int cnt) {
		this.numDays = cnt;
	}

	private int generateUri() {
		if (cityName != null && numDays != -1) {
			String url = baseUrl + "?" + "q=" + cityName;
			url += "&cnt=" + numDays;
			url += "&units=metric";
			url += "&lang=es";
			uri = Uri.parse(url);
			return 0;
		} else {
			return -1;
		}

	}

	private void initReceiver() {

		receiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				context.unregisterReceiver(receiver);

				// To dismiss the dialog
				activity.hideProgessDlg();

				long myRef = intent.getLongExtra(
						DownloadManager.EXTRA_DOWNLOAD_ID, -1);
				if (myRef == ref) {
			
					StringBuffer strContent = new StringBuffer("");
					int ch;

					try {
						ParcelFileDescriptor fileDesc = downloadMng
								.openDownloadedFile(myRef);
						FileInputStream fileInputStream = new ParcelFileDescriptor.AutoCloseInputStream(
								fileDesc);

						while ((ch = fileInputStream.read()) != -1)
							strContent.append((char) ch);
						fileInputStream.close();
						downloadMng.remove(myRef);
						
						Toast.makeText(context, "Downloaded!", Toast.LENGTH_SHORT)
						.show();
						String jsonStr = strContent.toString();
						FragmentData.setJSONStr(jsonStr);
						JSONObject json = new JSONObject(jsonStr);
						FragmentData.setJSON(json);
						ArrayList<String> forecastList = new ArrayList<String>();
						ArrayList<Date> datesList = new ArrayList<Date>();
						JSONArray jsonArray = json.getJSONArray("list");
						if (jsonArray != null && jsonArray.length() > 0) {
							for (int i = 0; i < jsonArray.length(); i++) {
								JSONObject obj = (JSONObject) jsonArray.get(i);
								if (obj == null)
									break;
								long time = obj.getLong("dt");
								Calendar cal = Calendar
										.getInstance(Locale.ENGLISH);
								cal.setTimeInMillis(time * 1000);
								String dateStr = DateFormat.format(DATE_FORMAT,
										cal).toString();
							
								JSONArray weatherArr = obj
										.getJSONArray("weather");

								if (weatherArr == null
										|| weatherArr.length() < 1)
									break;
								JSONObject weatherObj = (JSONObject) weatherArr
										.get(0);
								String weatherStr = (String) weatherObj
										.getString("main");
								if (weatherStr == null)
									break;
								forecastList.add(dateStr + ": " + weatherStr);
								datesList.add(cal.getTime());

							}
						}
						if (forecastList.size() < 1) {
							Toast.makeText(context, "Format Error",
									Toast.LENGTH_SHORT).show();
						}
						fragment.setDateList(datesList);
						fragment.setForecastList(forecastList);
						fragment.updateForecast();
						fragment.setUpdateDate(new Date());
						Log.i("TRACE", jsonArray.toString());
					}

					catch (FileNotFoundException fnfe) {
						Log.e("Receicer",
								"FileNotFoundException: " + fnfe.getMessage());

						Toast.makeText(context,
								"Error descargando recurso intentalo de nuevo",
								Toast.LENGTH_SHORT).show();
					} catch (JSONException jsone) {
						Log.e("Receicer", "JSONExcepton: " + jsone.getMessage());
						Toast.makeText(context,
								"Error descargando recurso intentalo de nuevo",
								Toast.LENGTH_SHORT).show();
					} catch (IOException ioe) {
						Log.e("Receicer", "IOExcepton: " + ioe.getMessage());
						Toast.makeText(context,
								"Error descargando recurso intentalo de nuevo",
								Toast.LENGTH_SHORT).show();
					}

				}
			}

		};
	}

	public void download() {
		int res = generateUri();
		if (res == 0) {
			Request request = new Request(uri);
			request.setNotificationVisibility(Request.VISIBILITY_HIDDEN);
			// request.setAllowedNetworkTypes(Request.NETWORK_WIFI);
			context.registerReceiver(receiver, filter);
			ref = downloadMng.enqueue(request);
			activity.showProgessDlg(R.string.progressSyncDlg_title);

		}

	}
}
