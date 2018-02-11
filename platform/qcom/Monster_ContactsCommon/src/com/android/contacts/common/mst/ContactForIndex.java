package com.android.contacts.common.mst;

import java.util.ArrayList;

import android.util.Log;

/**
 * Created by caizhongting on 16-8-9.
 */
public class ContactForIndex {
    private static final String TAG = "Contact";
	public String name;
    public String pinyin;
    public int type = 0;
    public ArrayList<String> firstLetter;
    public ContactForIndex(){
        firstLetter = new ArrayList<>();
    }
    
    public String toString(){
    	StringBuilder sbBuilder=new StringBuilder();
    	sbBuilder.append("name:"+name+" pinyin:"+pinyin+" type:"+type+" firstLetter:"+firstLetter);
    	return sbBuilder.toString();
    	
    }
}
