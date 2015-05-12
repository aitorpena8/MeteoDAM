package com.dam.meteodam;

import java.util.ArrayList;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class FragmentForecast extends Fragment {

	ListView listView;
	ArrayAdapter<String> listAdapter;
	FragmentData fragmentData;
	private ArrayList<String> weatherList;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		weatherList = new ArrayList<String>();
		listAdapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_1, weatherList);
		View view = inflater.inflate(R.layout.fragment_forecast, container,
				false);
		listView = (ListView) view.findViewById(R.id.weatherList);
		listView.setAdapter(listAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View view,
					int position, long id) {
				fragmentData.setSelectedPos(position);
				fragmentData.openDetailsActivity();

			}

		});

		return view;

	}

	// @Override
	// public void onSaveInstanceState(Bundle outState) {
	// outState.putStringArrayList("list", weatherList);
	// super.onSaveInstanceState(outState);
	//
	// }
	//
	// @Override
	// public void onCreate(Bundle savedInstanceState) {
	// super.onCreate(savedInstanceState);
	// if (savedInstanceState != null) {
	// ArrayList<String> list = savedInstanceState
	// .getStringArrayList("list");
	// addForecast(list);
	// }
	//
	// }

	public void addForecast(ArrayList<String> forecast) {
		if (forecast != null) {
			weatherList.clear();
			weatherList.addAll(forecast);
			listAdapter.notifyDataSetChanged();
		}

	}

	public void setParent(FragmentData fragment) {
		fragmentData = fragment;

	}

}
