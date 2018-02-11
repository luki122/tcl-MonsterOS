package com.mst.record;

import android.os.AsyncTask;

public abstract class SimpleAsynTask extends AsyncTask<Integer, Integer, Integer>{
	@Override
	protected abstract void onPostExecute(Integer result);
}

