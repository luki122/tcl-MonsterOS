package com.monster.cloud.service;

import android.app.ActivityManager;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

/**
 * Created by yubai on 16-11-10.
 */
public class CloudJobService extends JobService {
    private String TAG = "CloudJobService";

    private int jobId = 0;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        scheduleJob(getJobInfo());
        Log.e(TAG, "cloudJobService onCreate");
        return START_NOT_STICKY;
    }

    //Override this method with the callback logic for your job
    @Override
    public boolean onStartJob(JobParameters params) {
        Log.e(TAG, "cloudJobService onStart job");
        new SyncTask().execute(params);

        // return true if your service needs to process the work(on a separate thread)
        // false if there's no more work to be done for this job
        return true;
    }

    //this method is called if the system has determined that you must stop execution of your job
    //even before you've had a chance to call jobFinished(JobParameters, boolean)
    //当有jobFinished的时候  onStopJob不会被调用
    @Override
    public boolean onStopJob(JobParameters params) {
//        scheduleJob(getJobInfo());
        Log.e(TAG, "cloudJobService onStop job");
        // return true to indicate to the JobManager whether you'd like to reschedule this job
        // based on the retry criteria provided at a job creation-time
        // false to drop the job.
        return true;
    }

    private void scheduleJob(JobInfo jobInfo) {
        JobScheduler scheduler = (JobScheduler)getSystemService(Context.JOB_SCHEDULER_SERVICE);
        scheduler.schedule(jobInfo);
    }

    public JobInfo getJobInfo() {
        JobInfo.Builder builder = new JobInfo.Builder(jobId++, new ComponentName(getPackageName(), CloudJobService.class.getName()));
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        builder.setPersisted(true);
        builder.setRequiresCharging(false);
        builder.setRequiresDeviceIdle(false);
        builder.setMinimumLatency(1000);
        builder.setOverrideDeadline(5000);
        return builder.build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "cloudJobService onDestroy");
    }

    private boolean isServiceWork(Context context, String serviceName) {
        boolean isWork = false;
        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> list = activityManager.getRunningServices(100);
        if (list.size() <= 0) {
            // DO NOTHING
        } else {
            for (int i = 0; i < list.size(); ++i) {
                if (list.get(i).service.getClassName().equals(serviceName)) {
                    isWork = true;
                    break;
                }
            }
        }
        return isWork;
    }

    private class SyncTask extends AsyncTask<JobParameters, Void, String> {
        private JobParameters params;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(JobParameters... params) {
            this.params = params[0];
            boolean isSyncServiceWork = isServiceWork(CloudJobService.this, SyncService.class.getName());
            if (!isSyncServiceWork) {
                Log.e(TAG, "Syncservice start");
                CloudJobService.this.startService(new Intent(CloudJobService.this, SyncService.class));
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            //callback to inform the jobManager you've finished exectuing
            jobFinished(params, true);
        }
    }
}
