package com.monster.market.http.data;

import java.util.List;

import com.monster.market.bean.SearchKeyInfo;

public class SearchKeyListResultData {

	private List<SearchKeyInfo> keyList;

	public List<SearchKeyInfo> getKeyList() {
		return keyList;
	}

	public void setKeyList(List<SearchKeyInfo> keyList) {
		this.keyList = keyList;
	}

	@Override
	public String toString() {
		return "SearchKeyListResultData [keyList=" + keyList + "]";
	}

}
