package com.mst.thememanager.job;

import java.util.List;

import com.mst.thememanager.entities.Theme;
import com.mst.thememanager.job.ThreadPool.Job;
import com.mst.thememanager.job.ThreadPool.JobContext;


public class LoadDatabaseThemeJob  implements Job<List<Theme>>{

	
	private int mType;
	public LoadDatabaseThemeJob(int type){
		mType = type;
	}
	@Override
	public List<Theme> run(JobContext jc) {
		// TODO Auto-generated method stub
		return null;
	}

	
}
