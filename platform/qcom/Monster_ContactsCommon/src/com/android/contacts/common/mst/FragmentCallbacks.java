package com.android.contacts.common.mst;

public interface FragmentCallbacks {
	public static final int SWITCH_TO_SEARCH_MODE=0x00;
	public static final int DELETE_CONTACTS=0x01;
	public static final int MENU_CONTACTS_FILTER=0x02;
	
	public static final int HIDE_DIALPADFRAGMENT=0x03;
	
	public static final int SHOW_DIALPADFRAGMENT=0x04;
	
	public static final int SHOW_ADD_FAB=0x05;
	
	public static final int REMOVE_AUTO_RECORD_CONTACTS=0x06;
	
	public static final int SHOW_BUSINESS_CARD_LARGE_PHOTO=0x07;
	
	
	public Object onFragmentCallback(int what,Object obj);
}