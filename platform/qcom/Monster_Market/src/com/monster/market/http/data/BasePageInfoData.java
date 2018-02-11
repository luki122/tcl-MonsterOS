package com.monster.market.http.data;

public class BasePageInfoData {

	protected int pageNum;
	protected int pageSize;

	public int getPageNum() {
		return pageNum;
	}

	public void setPageNum(int pageNum) {
		this.pageNum = pageNum;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	@Override
	public String toString() {
		return "BasePageInfoData [pageNum=" + pageNum + ", pageSize="
				+ pageSize + "]";
	}

}
