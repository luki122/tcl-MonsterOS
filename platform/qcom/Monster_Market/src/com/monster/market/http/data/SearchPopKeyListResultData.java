package com.monster.market.http.data;

import java.util.List;

import com.monster.market.bean.SearchPopKeyInfo;

public class SearchPopKeyListResultData {

	private List<SearchPopKeyInfo> keyList;

	public List<SearchPopKeyInfo> getKeyList() {
		return keyList;
	}

	public void setKeyList(List<SearchPopKeyInfo> keyList) {
		this.keyList = keyList;
	}

	@Override
	public String toString() {
		return "SearchPopKeyListResultData [keyList=" + keyList + "]";
	}

}
