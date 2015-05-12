package com.dam.meteodam;


import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;

public class MyTabListener<T extends Fragment> implements TabListener {
	private Fragment fragment;
	private Activity activity;
	private Class<T> fragmentClass;
	private int fragmentContainer;

	public MyTabListener(Activity activity, int fragmentContainer,
			Class<T> fragmentClass) {
		this.activity = activity;
		this.fragmentContainer = fragmentContainer;
		this.fragmentClass = fragmentClass;
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		
		
		if (fragment == null) {
			String fragmentName = fragmentClass.getName();
			fragment = Fragment.instantiate(activity, fragmentName);
			ft.add(fragmentContainer, fragment, fragmentName);
		} else {
			ft.attach(fragment);
		}

	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		if(fragment!=null)
			ft.detach(fragment);

	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		if(fragment!=null)
			ft.attach(fragment);

	}
	
	public Fragment getFragment(){
		return fragment;
	}

}
