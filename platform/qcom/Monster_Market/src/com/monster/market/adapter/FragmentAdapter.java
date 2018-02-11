package com.monster.market.adapter;

import java.util.List;

import android.app.Fragment;
import android.app.FragmentManager;

public class FragmentAdapter extends FragmentPagerAdapter {
	
	private List<Fragment> listFragment;
	private String[] titles;

	public FragmentAdapter(FragmentManager fm, List<Fragment> listFragment) {
		this(fm, listFragment, null);
	}
	
	public FragmentAdapter(FragmentManager fm, List<Fragment> listFragment, String[] titles) {
		super(fm);
		this.listFragment = listFragment;
		this.titles = titles;
	}

	@Override
	public Fragment getItem(int position) {
		return listFragment.get(position);
	}

	@Override
	public int getCount() {
		return listFragment == null ? 0 : listFragment.size();
	}

	@Override
	public CharSequence getPageTitle(int position) {
		if (titles != null) {
			return titles[position];
		}
		return super.getPageTitle(position);
	}
}
