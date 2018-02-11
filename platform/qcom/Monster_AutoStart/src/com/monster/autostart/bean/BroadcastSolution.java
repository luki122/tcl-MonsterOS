package com.monster.autostart.bean;

import android.content.Context;

import com.monster.autostart.interfaces.IBaseSolution;

public class BroadcastSolution extends IBaseSolution{

	public BroadcastSolution(Context c) {
		// TODO Auto-generated constructor stub
		setFilter(new BroadcastFilterApp(c));
		setProccess(new BroadcastProccessApp(c));
		setDetected(new BroadcastDetectedApp());
	}
	

}
