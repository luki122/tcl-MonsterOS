package com.monster.netmanage.adapter;

import java.util.ArrayList;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;

/**
 * sim1/sim2之间切换
 * 
 * @author zhaolaichao
 */
public class MainFragementAdater extends FragmentPagerAdapter {

	private ArrayList<Fragment> mFragements;
	private FragmentManager mFm;

	public MainFragementAdater(FragmentManager fm, ArrayList<Fragment> frageMents) {
		super(fm);
		mFm = fm;
		mFragements = frageMents;
	}

	
	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}


	@Override
	public Fragment getItem(int arg0) {
		return mFragements.get(arg0);
	}

	@Override
	public int getCount() {
		return mFragements.size();
	}

	public void clearFragments() {
		if (this.mFragements != null) {
			FragmentTransaction ft = mFm.beginTransaction();
			for (Fragment f : this.mFragements) {
				ft.remove(f);
			}
			ft.commit();
			ft = null;
			mFm.executePendingTransactions();
		}
		notifyDataSetChanged();
	}
	
	public void updateFragments(ArrayList<Fragment>  fragments) {
		if (fragments != null) {
			mFm.executePendingTransactions();
			this.mFragements = fragments;
			notifyDataSetChanged();
		}
	}
}
