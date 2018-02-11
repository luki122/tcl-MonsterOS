package com.android.contacts.common.mst;

public interface FragmentCallbacks {
	public static final int SWITCH_TO_SEARCH_MODE=0;
	public static final int DELETE_CONTACTS=1;
	public static final int MENU_CONTACTS_FILTER=2;

	public static final int HIDE_DIALPADFRAGMENT=3;

	public static final int SHOW_DIALPADFRAGMENT=4;

	public static final int SHOW_ADD_FAB=5;

	public static final int REMOVE_AUTO_RECORD_CONTACTS=6;

	public static final int SHOW_BUSINESS_CARD_LARGE_PHOTO=7;

	public static final int updateViewConfiguration=8;
	public static final int REMOVE_PRIVACY_CONTACTS=9;


	public Object onFragmentCallback(int what,Object obj);
}