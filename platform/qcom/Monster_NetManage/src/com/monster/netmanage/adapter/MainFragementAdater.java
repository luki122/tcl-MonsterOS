package com.monster.netmanage.adapter;

import java.util.ArrayList;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * sim1/sim2之间切换
 * @author zhaolaichao
 */
public class MainFragementAdater extends FragmentPagerAdapter{

	private ArrayList<Fragment> mFragements;
	
	public MainFragementAdater(FragmentManager fm, ArrayList<Fragment> frageMents) {
		super(fm);
		mFragements = frageMents;
	}

	@Override
	public Fragment getItem(int arg0) {
		return mFragements.get(arg0);
	}

	@Override
	public int getCount() {
		return mFragements.size();
	}

}
