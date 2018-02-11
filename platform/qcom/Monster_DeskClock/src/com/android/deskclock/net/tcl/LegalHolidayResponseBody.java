package com.android.deskclock.net.tcl;

import java.util.ArrayList;
import java.util.List;

public class LegalHolidayResponseBody {

	private List<Integer> holidayList = new ArrayList<Integer>();
	private List<Integer> workdayList = new ArrayList<Integer>();

	public List<Integer> getHolidayList() {
		return holidayList;
	}

	public void setHolidayList(List<Integer> holidayList) {
		this.holidayList = holidayList;
	}

	public List<Integer> getWorkdayList() {
		return workdayList;
	}

	public void setWorkdayList(List<Integer> workdayList) {
		this.workdayList = workdayList;
	}

}
